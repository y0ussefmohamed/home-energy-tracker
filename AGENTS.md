# AGENTS.md — Home Energy Tracker

This guide applies to the entire repository. It records the implemented architecture and the user's working conventions. Verify details against the current source before changing them; update this guide when an authorized change makes it outdated.

## 1. Working with the user

- **Do not use sub-agents or delegate implementation.**
- **Do not create, modify, or enable tests unless the user requests it.** Existing data-population tests must remain disabled unless explicitly requested.
- Work within the user's requested scope. A request for a skeleton means no implementation; a request for an explanation does not authorize unrelated code changes.
- The order of changes matters to the user. Explain changes in dependency order and identify exactly what was added.
- Before moving to a new development stage, check the current project state and report mistakes.
- Preserve the user's established conventions: standard DTO classes, separate requests/responses, plural SQL tables, and existing folder/package names.
- **Recorded checkpoint: 2026-10-21, project revision and the pre-CV review are complete; current capabilities and known limitations are documented in README.md.** Update this checkpoint when the project advances. Do not implement additional features automatically.
- Use the checked-in implementation and approved requirements as the baseline. Consult official library documentation for version-specific behavior and keep unrelated changes out of scope.
- Inspect Git status before editing and preserve existing work. Do not reset files, commit, or push unless requested.
- Use the user's IntelliJ HTTP-client plugin for interactive API work; Postman is unnecessary.
- Report what was actually verified and any remaining limitations. A successful build or HTTP response does not establish that every behavior is correct.

## 2. Repository layout and technology

Each service is an independent Maven project with its own `pom.xml`, `mvnw`, and `src/`. There is no root Maven aggregator. Folder names contain spaces; quote paths in commands.

| Folder | Base package | Port | Responsibility |
| --- | --- | --- | --- |
| `API Gateway` | `com.projects.API.Gateway` | `9000` | Spring Cloud Gateway WebMVC, JWT authentication, circuit breakers, Swagger aggregation, Actuator |
| `User Service` | `com.energy.User.Service` | `8080` | User CRUD; owns all application Flyway migrations |
| `Device Service` | `com.projects.Device.Service` | `8081` | Device CRUD and device lookup by user |
| `Ingestion Service` | `com.projects.Ingestion.Service` | `8082` | Receives readings, publishes Kafka events, runs the simulator |
| `Usage Service` | `com.projects.Usage.Service` | `8083` | Kafka consumption, InfluxDB storage/queries, scheduled threshold checks, alert production |
| `Alert Service` | `com.projects.Alert.Service` | `8084` | Kafka alert consumption, email delivery through Mailpit, delivery-result persistence |
| `Insight Service` | `com.projects.Insight.Service` | `8085` | Fetches usage and generates local AI insights through Spring AI/Ollama |

Current POM versions: Java **21**, Spring Boot **4.1.1**, Spring Cloud **2025.1.3** in the gateway, Spring AI **2.0.1** in Insight, and springdoc **3.0.2** in services with OpenAPI documentation. Check the POMs before adding dependencies; do not substitute Spring Boot 3 examples without checking compatibility.

## 3. Build and startup

From the repository root:

```sh
docker compose config --quiet
docker compose up -d
docker compose ps
```

Docker Compose runs infrastructure only. Java services run separately in IntelliJ or through Maven. Ollama also runs separately on the host.

From any service folder, using JDK 21:

```sh
./mvnw -Dmaven.test.skip=true compile
./mvnw -Dmaven.test.skip=true package
./mvnw -Dmaven.test.skip=true spring-boot:run
```

Use `-o` only when the required dependencies are already cached. `-Dmaven.test.skip=true` skips test compilation and execution.

Recommended full-pipeline startup order:

1. Start Docker Desktop and the Compose infrastructure; check actual service readiness.
2. Ensure host Ollama is running and `qwen2.5:1.5b` is installed. Check with `ollama list`; use `ollama pull qwen2.5:1.5b` if missing. Reuse an existing Ollama process.
3. Start **User Service** and wait for Flyway to finish.
4. Start **Device Service**, then **Alert Service** and **Usage Service**.
5. Start **Insight Service** and **API Gateway** once their dependencies are ready.
6. Start **Ingestion Service** last when intentionally generating readings.

For quiet HTTP diagnostics, start Ingestion with this temporary override:

```sh
./mvnw -Dmaven.test.skip=true spring-boot:run -Dspring-boot.run.arguments='--simulation.requests-per-interval=0'
```

For Usage or Alert HTTP/startup checks, `--spring.kafka.listener.auto-startup=false` temporarily prevents Kafka consumption. **This does not disable Usage's scheduled aggregation job.** Do not persist diagnostic overrides as normal application settings.

Stop infrastructure with `docker compose down`. Avoid `down -v`, pruning storage, truncating tables, renumbering IDs, or deleting bind-mounted data as a troubleshooting shortcut. Stop only temporary Java processes started for verification; leave the user's IntelliJ processes alone.

## 4. Infrastructure and persistence

The following credentials are existing local development defaults, not production configuration. Keep client secrets, tokens, and private realm exports out of source and logs.

| Component | Address / container | Configuration |
| --- | --- | --- |
| Application MySQL | `localhost:3306` / `home-energy-mysql` | MySQL 8.3.0; database `home_energy_tracker`; `root` / `password` |
| Kafka | Host apps: `localhost:9094`; Docker clients: `kafka:9092` / `home-energy-kafka` | Single-node KRaft; controller listener `9093`; replication factor 1 |
| Kafka UI | `http://localhost:8070` / `home-energy-kafka-ui` | Connects internally to `kafka:9092` |
| InfluxDB | `http://localhost:8072` / `home-energy-influxdb` | InfluxDB 2.7; org `projects`; bucket `usage-bucket`; token `my-token`; retention `1w` |
| Mailpit | SMTP `localhost:1025`; UI `http://localhost:8025` / `home-energy-mailpit` | Local captured mail; SMTP authentication and STARTTLS disabled |
| Ollama | `http://localhost:11434` on the host | Model `qwen2.5:1.5b`; no Compose service |
| Keycloak | `http://localhost:8091` / `home-energy-keycloak` | Keycloak 24.0.1; local admin `admin` / `admin`; realm `het-security-realm` |
| Keycloak MySQL | Docker-only `keycloak-db:3306` / `home-energy-keycloak-mysql` | Database `keycloak`; application account `keycloak` / `password`; separate from application MySQL |
| Prometheus | `http://localhost:9090` / `home-energy-prometheus` | Prometheus 3.1.0; scrapes every 15 seconds; retains metrics for 5 days |
| Grafana | `http://localhost:3000` / `home-energy-grafana` | Grafana 11.4.0; local admin `admin` / `admin`; provisioned Home Energy Tracker dashboard |

Persistent storage:

- Application MySQL: Compose named volume `db-data`.
- Kafka: `docker/kafka_data/`.
- InfluxDB: `docker/influxdb_data/`.
- Keycloak MySQL: `volume-data/mysql_keycloak_data/`.
- Prometheus and Grafana: Compose named volumes `prometheus-data` and `grafana-data`.
- Private migration backups: `backups/`; private realm-import directory: `docker/keycloak/realms/`. Both are Git-ignored through the root ignore rules.

Keycloak uses `KC_DB=mysql` and `KC_DB_URL=jdbc:mysql://keycloak-db:3306/keycloak`. Its database health check executes an authenticated query before Keycloak starts. Legacy `DB_VENDOR` / `DB_ADDR` variables do not configure this Keycloak image.

The original H2 database and full realm/user exports were backed up before migration to MySQL. Preserve the current realms, users, clients, credentials, and signing keys during future changes. Perform full CLI exports/imports with Keycloak stopped; startup `--import-realm` skips existing realms. A fresh checkout does not include private realm exports or existing database contents, so verify realm/client provisioning before expecting JWT authentication to work.

## 5. Database and model ownership

User, Device, and Alert share the application MySQL database. Service responsibilities remain separate; this is not a database-per-service deployment.

**Only User Service owns Flyway migrations**, under `User Service/src/main/resources/db/migration/`:

| Migration | Table | Entity owner |
| --- | --- | --- |
| `V1__user_table.sql` | `users` | User Service |
| `V2__device_table.sql` | `devices` | Device Service |
| `V3__alert_table.sql` | `alerts` | Alert Service |

- `docker/mysql/init.sql` creates/selects the database on first initialization; Flyway creates the application tables.
- Existing JPA services use `spring.jpa.hibernate.ddl-auto=none`. Preserve Flyway schema ownership.
- Add the next numbered migration for schema changes. Do not edit already applied migrations or repair checksums to hide a mismatch.
- `devices.user_id` has a SQL foreign key to `users.id` with `ON DELETE CASCADE`. `Device` stores a scalar `Long userId`, not a cross-service JPA relationship. Do not introduce `@ManyToOne` / `@JoinColumn` to another service's entity.
- Preserve plural SQL names and explicit mappings where Java and SQL names differ.
- Existing sample-data methods in the User and Device test classes are disabled. Never enable or run them as part of routine verification. Do not assume IDs are consecutive or that simulator device IDs exist.

## 6. HTTP contracts and service clients

Paths below are relative to the indicated service. Gateway-exposed routes use the same paths on port `9000`.

| Service | Implemented endpoints |
| --- | --- |
| User | `POST /api/v1/user`; `GET`, `PUT`, `DELETE /api/v1/user/{userId}` |
| Device | `POST /api/v1/device`; `GET`, `PUT`, `DELETE /api/v1/device/{id}`; `GET /api/v1/device/user/{userId}` |
| Ingestion | `POST /api/v1/ingestion` |
| Usage | `GET /api/v1/usage/{userId}?days=3`; `days` defaults to 3 |
| Insight | `GET /api/v1/insight/overview/{userId}`; `GET /api/v1/insight/saving-tips/{userId}` |
| Alert | No REST controller; port `8084` still hosts its Spring web application |

Creation returns `201`; User/Device deletion returns `204`. Ingestion returns no response body and publishes asynchronously. Its `201` does not confirm that Kafka consumption, InfluxDB storage, or alert delivery has finished.

Clients currently use `RestTemplate` and direct service URLs:

- Usage `DeviceClient`: `device.service.url=http://localhost:8081/api/v1/device`.
- Usage `UserClient`: `user.service.url=http://localhost:8080/api/v1/user`.
- Insight `UsageClient`: `usage.service.url=http://localhost:8083/api/v1/usage`.

Do not route these clients through the authenticated gateway without implementing the required authentication deliberately. JWT security currently applies at the gateway; downstream HTTP services remain directly accessible in the local setup.

Keep metadata and aggregation models distinct:

- `DeviceResponse` describes a device and its owner.
- `DeviceUsageResponse` contains `id`, `name`, `type`, `location`, and `energyConsumed`.
- `UserDevicesResponse` contains `userId` and `List<DeviceUsageResponse> devices`.
- Usage and Insight hold matching local copies of their shared HTTP response contracts. Update both sides when changing that contract; do not merge unrelated DTOs.

## 7. Kafka, storage, and scheduled work

### Contracts

| Topic | Producer | Consumer / group | Event |
| --- | --- | --- | --- |
| `energy-usage` | Ingestion | Usage / `usage-service` | `EnergyUsageEvent` |
| `energy-alerts` | Usage | Alert / `alert-service` | `AlertingEvent` |

Both event classes live in **`com.projects.kafka.event`** in their respective producer and consumer projects; no shared Maven event module currently exists. Preserve matching fields, field types, package names, and JSON/type-header compatibility across copies.

- `EnergyUsageEvent`: `Long deviceId`, `double energyConsumed`, `Instant timestamp`.
- `AlertingEvent`: `Long userId`, `String message`, `double threshold`, `double energyConsumed`, `String email`.
- HTTP `EnergyUsageRequest` is a separate class. Ingestion explicitly maps it to `EnergyUsageEvent`.
- Timestamps are Java `Instant` values serialized as JSON strings with `@JsonFormat(shape = JsonFormat.Shape.STRING)`; do not replace the Java field with `String`.
- Producers use `StringSerializer` / `JsonSerializer`; consumers use `StringDeserializer` / `JsonDeserializer`. Consumer type mappings include `energyUsageEvent:com.projects.kafka.event.EnergyUsageEvent` and `alertingEvent:com.projects.kafka.event.AlertingEvent` respectively. Check actual producer headers when changing mappings.

### Implemented flow

1. Ingestion receives a reading and publishes `EnergyUsageEvent` to `energy-usage`.
2. Usage `consumeEnergyUsageEvent` writes an InfluxDB point: measurement `energy_usage`, tag `deviceId`, field `energyConsumed`, timestamp precision `MS`.
3. Usage `aggregateDeviceEnergyUsage` runs **every 10 seconds**. It sums the **past hour** per device, resolves owners through Device Service, fetches users through User Service, and considers users with `alerting=true`.
4. If a user's summed consumption is strictly greater than `energyAlertingThreshold`, Usage publishes `AlertingEvent` to `energy-alerts`.
5. Alert `consumeAlertingEvent` calls `EmailService.sendEmail(...)`. That helper returns a boolean for SMTP success/failure.
6. Alert persists an `Alert` with `userId`, `createdAt`, and the returned `sent` value, including failed delivery attempts.

The separate `getUserDevicesUsage(userId, days)` query fetches the user's devices and sums InfluxDB values over the requested number of days. It returns an empty device list for no devices, and zero consumption for devices without readings.

The simulator defaults to **10,000 requests per 60 seconds**, divided among **10 threads**, with random device IDs **1–199**. Starting Ingestion normally generates data automatically. Usage aggregation can also repeatedly publish alerts while a user remains above the threshold; there is no alert deduplication/cooldown in the current implementation.

## 8. AI insights

- Insight fetches the last **3 days** through `UsageClient.getUserUsage(userId, 3)`.
- Both insight endpoints use `InsightService.createInsightResponse`. The numeric `energyUsage` is calculated in Java from device totals; the model supplies the `tips` text.
- `OllamaConfig` creates the configured Spring AI `ChatClient`. Use that bean so the system instructions apply.
- Current settings: model `qwen2.5:1.5b`, temperature `0.2`, `num-predict=512`, and automatic model pulling disabled with `pull-model-strategy=never`.
- Prompts include only the three highest-consuming devices (name, type, and period total), plus the household total and full device count. Calculate numeric `energyUsage` from **all** devices before selecting the top three.
- Saving tips request three numbered, single-sentence actions; overview requests two short sentences with one action. Avoid introductions, repeated device inventories/figures, unsupported units or savings estimates, and advice about unlisted device types.
- Read the full `ChatResponse` and its generation finish reason. A `length` finish reason returns `502 BAD_GATEWAY` instead of passing incomplete advice to the caller. Missing or blank model responses also return `502`.
- An empty device list returns explanatory text without calling Ollama. Missing usage data raises an error.
- Keep recorded totals distinct from daily averages. Do not invent units, tariffs, or missing data.

**Remaining model-quality limitation:** `qwen2.5:1.5b` can still suggest unrelated devices or inappropriate replacements and repeat figures despite the prompt rules. The truncation guard does not validate the meaning of its advice. Inspect actual answer content and completion metadata when validating AI work; HTTP `200` and nonempty text alone are insufficient.

## 9. Gateway, security, and OpenAPI

The gateway forwards `/api/v1/user/**` to `8080`, `/api/v1/device/**` to `8081`, `/api/v1/ingestion/**` to `8082`, and `/api/v1/insight/**` to `8085`. There are no Usage or Alert API routes.

Each service route has a matching circuit-breaker ID (`userServiceCircuitBreaker`, `deviceServiceCircuitBreaker`, `ingestionServiceCircuitBreaker`, `insightServiceCircuitBreaker`) and its own `forward:/fallback/{service}` target. Preserve unique route IDs and fallback paths. Fallback responses are `503` with the correct service name.

The Insight time limiter is **60 seconds** via `resilience4j.timelimiter.instances.insightServiceCircuitBreaker.timeoutDuration`. Other routes retain the library default time limit; read current properties before changing resilience behavior. Circuit-breaker settings live in the gateway's `application.properties`.

Authentication:

- Realm: `het-security-realm`; client: `home-energy-tracker-client`.
- Issuer property: `http://localhost:8091/realms/het-security-realm`.
- JWK-set property: `http://localhost:8091/realms/het-security-realm/protocol/openid-connect/certs`.
- `SecurityConfig` supplies a custom `NimbusJwtDecoder` using the JWK URL. Inspect that bean before assuming an issuer/audience validation policy from properties alone.
- Gateway API requests require a Bearer access token. The paths listed in `security.excluded.urls` permit documentation and Actuator access. Authentication does not implement per-user resource ownership checks.
- The existing confidential client supports client credentials. Keep its secret private; do not enable additional grants or create users just to verify a route.

OpenAPI:

- Combined UI: `http://localhost:9000/swagger-ui.html`.
- Forwarded definitions: `/docs/user-service/v3/api-docs`, `/docs/device-service/v3/api-docs`, `/docs/ingestion-service/v3/api-docs`, `/docs/insight-service/v3/api-docs`.
- Each corresponding `OpenApiConfig` sets the server to `http://localhost:9000` (overridable with `gateway.url`), declares HTTP Bearer/JWT authentication, and applies the security requirement.
- In **Authorize**, enter the access token; Swagger adds the `Bearer` prefix. **Try it out** must target the gateway.
- Usage has separate direct documentation on `8083`; do not point it at the gateway unless a Usage route is added intentionally.
- Gateway exposes Actuator `health`, `info`, `metrics`, `circuitbreakers`, and `prometheus`. Useful checks: `/actuator/health`, `/actuator/circuitbreakers`, and `/actuator/prometheus`.

## 10. Monitoring

- All seven services include Actuator and `micrometer-registry-prometheus`, with versions managed by Spring Boot.
- All services expose `/actuator/prometheus` and add `management.metrics.tags.application=${spring.application.name}`. Preserve the user's application names; Prometheus job names use lowercase names independently.
- The six downstream services expose `health,info,prometheus` with health details set to `when_authorized`. Preserve the gateway's additional Actuator endpoints, circuit breakers, security exclusions, and AI timeout.
- `docker/prometheus/prometheus.yml` scrapes Prometheus itself plus the seven host-run Java applications. Docker-to-host targets use `host.docker.internal` with ports `8080`–`8085` and `9000`, not container-local `localhost`.
- Grafana provisioning files are `docker/grafana/provisioning/datasources/prometheus.yml`, `docker/grafana/provisioning/dashboards/dashboards.yml`, and `docker/grafana/provisioning/dashboards/json/het-overview.json`.
- Grafana's data source UID is `prometheus`, URL `http://prometheus:9090`; dashboard UID is `het-overview`, title **Home Energy Tracker - Overview**, folder **Home Energy Tracker**. Keep dashboard/data-source UIDs aligned.
- Dashboard panels cover HTTP rates by application/status, JVM heap usage, scrape target reachability, 4xx/5xx rates, and the busiest endpoints.
- Prometheus target health is available at `http://localhost:9090/targets`; the API is `/api/v1/targets`. With all applications running, expect seven application targets plus Prometheus itself to be `UP`.
- Scrape `UP` means the metrics endpoint is reachable; it does not prove all business flows work. Request-rate panels need traffic and at least two scrapes; an unused error series can initially have no data.
- After dependency/property changes, reload Maven and restart the Java applications. Keep monitoring-only changes limited to the required dependencies and configuration.

## 11. Coding conventions

1. **No Java records.** DTOs and Kafka events use standard classes with Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, and `@AllArgsConstructor`.
2. **Uppercase `DTO` package.** Keep `*Request` and `*Response` separate. Preserve the actual service base packages and casing.
3. **Shared Kafka package.** Keep event copies under `com.projects.kafka.event` with compatible contracts.
4. **Plural tables.** Use `users`, `devices`, and `alerts` consistently in JPA and SQL.
5. **Clean layering.** Controllers handle HTTP and delegate to services. Services implement business flow; repositories handle persistence. Clients handle outbound HTTP. Do not put database or AI logic in controllers.
6. **Focused helpers.** `EmailService` sends mail and reports success; `AlertService` owns alert persistence.
7. **Constructor injection.** Follow existing constructor/`@RequiredArgsConstructor` patterns. Use the existing Lombok builder/mapping style.
8. **Existing error conventions.** User uses `ResponseStatusException` for missing users and duplicate emails; Device uses `DeviceNotFoundException` with `RestExceptionHandler`. Preserve meaningful HTTP statuses rather than turning failures into successful responses.
9. **AOP scope.** User and Device have service-call logging in `AOP/LoggingAspect` and controller timing in `AOP/ExecutionTimeAspect`. Check pointcuts after package changes and avoid duplicating their logs unnecessarily.
10. **Bounded changes.** Do not rename packages, introduce shared modules, upgrade frameworks, alter security policy, or redesign persistence as incidental cleanup.

## 12. Verification and troubleshooting

- For documentation-only edits, check facts, paths, and `git diff --check`; no application build is needed.
- For implementation changes, compile/package affected services with tests skipped. Check all affected contract copies and callers.
- For runtime work, verify required infrastructure and actual startup logs. A running Docker container alone does not prove its application is ready.
- Use existing data for read checks. Do not generate sample users/devices, flood ingestion, send mail, or modify records just to prove startup.
- For gateway/security changes, check missing/invalid-token `401`, valid-token forwarding, matching service-specific `503` fallbacks, and the Swagger server/security definitions. Use malformed JSON to check POST routing without producing a valid reading.
- For AI changes, check both insight endpoints through the gateway, compare numeric usage with Usage Service, and inspect whether the generated text actually answers the requested question.
- For monitoring changes, validate Compose and the Prometheus configuration, confirm all target scrapes, application labels, Grafana provisioning, and actual dashboard queries. Check a controlled stop/restart using only agent-started services to verify `UP`/`DOWN` transitions.
- For alerting changes, verify both the email result and persisted `sent` status using an intentional, bounded scenario.
- Usage currently catches InfluxDB query failures in `getUserDevicesUsage` and returns zero device totals. Check logs before treating zeros as proof of no consumption; this behavior is not a health guarantee.
- If a port is occupied, identify its owning process and service before stopping it. Do not kill all Java processes or change agreed ports to conceal a duplicate instance.
- If IntelliJ fails but the packaged application starts, inspect the launch configuration and injected plugins/agents before changing working Spring code.
- If Docker reports an unknown Compose command/flag on this Mac, check Docker Desktop and its CLI-plugin configuration before editing valid YAML.
- Before a completion claim, inspect the final diff, preserve existing work, and state exactly which checks passed. Record unresolved behavior honestly instead of calling the entire project "100% correct."
