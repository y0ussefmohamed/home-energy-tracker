# AGENTS.md — Guidance for Automated Coding & Operational Agents

**Purpose:** Give AI coding agents the exact architecture, dataflows, commands, and strict coding conventions needed to build, run, and maintain the **Home Energy Tracker** microservices repository without guessing or hallucinating.

---

## 1. Quick Start & Commands

### Infrastructure (Docker Compose)
- **Start all infrastructure:** `docker compose up -d` (from repository root)
- **Stop all infrastructure:** `docker compose down`
- **Database bootstrap:** `docker/mysql/init.sql` (MySQL database `home_energy_tracker`) and Flyway migrations in `User Service/src/main/resources/db/migration/` (`V1__user_table.sql`, `V2__device_table.sql`, `V3__alert_table.sql`).

### Build & Run a Microservice
Each microservice is an independent Maven project in its own top-level folder with its own Maven Wrapper (`./mvnw`):
- **Compile:** `cd "<Service Folder>" && ./mvnw -DskipTests compile`
- **Package:** `cd "<Service Folder>" && ./mvnw -DskipTests package`
- **Run:** `cd "<Service Folder>" && ./mvnw spring-boot:run`

---

## 2. Microservices Architecture & Ports

| Service Folder | Base Package | Port | Responsibility & Tech Stack |
| :--- | :--- | :--- | :--- |
| **`API Gateway`** | `com.projects.API.Gateway` | `9000` | Single entry point proxy (Spring Cloud Gateway WebMVC), Resilience4j Circuit Breakers, fallback routes, Actuator |
| **`User Service`** | `com.energy.User.Service` | `8080` | User CRUD, MySQL (`users` table), Flyway schema migrations (`V1`–`V3`) |
| **`Device Service`** | `com.projects.Device.Service` | `8081` | Device CRUD & lookup by `userId`, MySQL (`devices` table) |
| **`Ingestion Service`** | `com.projects.Ingestion.Service` | `8082` | Receives energy readings (`POST /api/v1/ingestion`), publishes `EnergyUsageEvent` to Kafka, runs `ParallelDataSimulator` |
| **`Usage Service`** | `com.projects.Usage.Service` | `8083` | Consumes `energy-usage` from Kafka, writes/queries InfluxDB (`usage-bucket`), scheduled threshold checks, publishes `AlertingEvent`, exposes `GET /api/v1/usage/{userId}` |
| **`Alert Service`** | `com.projects.Alert.Service` | `8084` | Consumes `energy-alerts` from Kafka, sends email alerts via Mailpit SMTP (`EmailService`), persists sent alerts to MySQL (`alerts` table) |
| **`Insight Service`** | `com.projects.Insight.Service` | `8085` | Calls `Usage Service` for multi-day per-device usage, prompts local **Ollama** (`qwen2.5:1.5b`) via Spring AI (`OllamaChatModel`) for energy-saving tips |

---

## 3. Infrastructure Ports (`docker-compose.yml`)

- **MySQL (`home-energy-mysql`):** `localhost:3306` (DB: `home_energy_tracker`, user/pass: `root` / `rootpassword` or `energy_user` / `energy_pass`)
- **Kafka (`home-energy-kafka`):** `localhost:9094` (external host listener) / `kafka:9092` (internal Docker network)
- **Kafka UI (`home-energy-kafka-ui`):** `http://localhost:8070`
- **InfluxDB (`home-energy-influxdb`):** `http://localhost:8072` (org: `home-energy`, bucket: `usage-bucket`)
- **Mailpit (`home-energy-mailpit`):** SMTP `localhost:1025`, Web UI `http://localhost:8025`
- **Ollama (Host):** `http://localhost:11434` (model: `qwen2.5:1.5b`)

---

## 4. Critical Integration Points & Dataflows

### A. Real-Time Energy Ingestion & Alerting Pipeline
1. **Ingestion (`Ingestion Service`):**
   - `POST /api/v1/ingestion` (or `ParallelDataSimulator`) sends `EnergyUsageRequest` to `IngestionService`.
   - `IngestionService` publishes `EnergyUsageEvent` (`com.projects.kafka.event.EnergyUsageEvent`) to Kafka topic **`energy-usage`**.
2. **Storage & Threshold Aggregation (`Usage Service`):**
   - `UsageService.consumeEnergyUsageEvent` consumes from **`energy-usage`** and writes points to InfluxDB (`energy_usage` measurement).
   - Scheduled job `UsageService.aggregateDeviceEnergyUsage` queries InfluxDB, fetches device & user metadata via REST clients (`DeviceClient` $\rightarrow$ `8081`, `UserClient` $\rightarrow$ `8080`), and compares usage against each user's threshold.
   - When threshold is exceeded, `UsageService` publishes `AlertingEvent` (`com.projects.kafka.event.AlertingEvent`) to Kafka topic **`energy-alerts`**.
3. **Notification & Persistence (`Alert Service`):**
   - `AlertService.consumeUsageAlertEvent` consumes from **`energy-alerts`**.
   - Calls `EmailService.sendEmail(...)` (via Mailpit SMTP `1025`), and if sent (`true`), saves an `Alert` entity to the MySQL `alerts` table.

### B. AI Energy Insights Pipeline
1. Client calls `GET /api/v1/insight/saving-tips/{userId}` or `GET /api/v1/insight/overview/{userId}` (directly on `8085` or via `API Gateway` on `9000`).
2. `InsightService` calls `UsageClient` (`GET http://localhost:8083/api/v1/usage/{userId}?days=3`).
3. `UsageService` calls `DeviceClient` (`GET http://localhost:8081/api/v1/device/user/{userId}`) to fetch all user devices, then queries InfluxDB Flux (`sum()`) over the last `X` days to aggregate total `energyConsumed` per device.
4. `InsightService` builds a prompt containing the per-device breakdown and calls `OllamaChatModel.call(prompt)` to return an `InsightResponse`.

### C. API Gateway & Circuit Breakers
- `API Gateway` (`9000`) routes `/api/v1/user/**` (`8080`), `/api/v1/device/**` (`8081`), `/api/v1/ingestion/**` (`8082`), and `/api/v1/insight/**` (`8085`).
- Each route is protected by a Resilience4j circuit breaker (`*ServiceCircuitBreaker`) that forwards failures to `/fallbackRoute` returning `503 SERVICE_UNAVAILABLE`.
- Monitor circuit breakers at: `GET http://localhost:9000/actuator/circuitbreakers`.

---

## 5. Strict Coding Rules & Project Conventions

AI agents modifying or adding code in this repository **MUST** follow these rules:

1. **NO Java `record`s:**
   - Never use Java `record` types for DTOs or Kafka events.
   - Always use standard Java classes annotated with Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`).
2. **Uppercase `DTO` Package & Request/Response Separation:**
   - Place DTOs inside the uppercase `DTO` package (`com.<group>.<Service>.DTO`).
   - Keep `*Request` and `*Response` models separate (e.g., `UserRequest` / `UserResponse`, `DeviceRequest` / `DeviceResponse`).
3. **Shared Kafka Event Package:**
   - Keep Kafka event classes (`EnergyUsageEvent`, `AlertingEvent`) in package `com.projects.kafka.event` so producer/consumer type mappings align cleanly.
4. **Plural SQL Table Names:**
   - Always use plural table names in `@Table(name = "...")` and Flyway SQL migrations: `users`, `devices`, `alerts`.
5. **Single Responsibility Principle (SRP) & Clean Layering:**
   - Controllers must only handle HTTP routing/responses and delegate immediately to a `*Service`.
   - Keep helper services focused on a single responsibility (for example, in `Alert Service`, `EmailService` only sends emails and returns a `boolean`, while `AlertService` handles `AlertRepository` persistence).
6. **Do Not Write Tests Unless Requested:**
   - Only add or modify unit/integration tests when explicitly asked by the user.
