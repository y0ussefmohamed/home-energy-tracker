# Home Energy Tracker

Spring Boot microservices for users, devices, energy ingestion, usage aggregation, email alerts, and local AI insights. An authenticated API Gateway provides the public entry point. Lesson 14 adds Prometheus and Grafana monitoring through configuration.

The services use Java 21, Spring Boot 4.1.1, and independent Maven wrappers. See [AGENTS.md](AGENTS.md) for service contracts, coding conventions, data flows, and known limitations.

## Run locally

Start Docker Desktop, then run this from the project root:

```sh
docker compose up -d
```

Ensure host Ollama is running with the `qwen2.5:1.5b` model installed. Docker Compose starts infrastructure; Java services run separately.

Reload Maven changes in IntelliJ, then start User Service first so Flyway applies migrations. Start Device, Alert, Usage, Insight, and API Gateway next. Start Ingestion last when ready to generate simulated readings.

| Java application | Port |
| --- | --- |
| User Service | 8080 |
| Device Service | 8081 |
| Ingestion Service | 8082 |
| Usage Service | 8083 |
| Alert Service | 8084 |
| Insight Service | 8085 |
| API Gateway | 9000 |

Alternatively, run a service through its own Maven wrapper:

```sh
cd "User Service"
./mvnw -Dmaven.test.skip=true spring-boot:run
```

Use IntelliJ's HTTP-client plugin for requests. Gateway APIs require a Keycloak Bearer access token; the combined documentation is at [Swagger UI](http://localhost:9000/swagger-ui.html).

## Lesson 14 monitoring

- [Prometheus target health](http://localhost:9090/targets): seven application targets and Prometheus itself should be `UP` when all services are running.
- [Grafana](http://localhost:3000): sign in with the local tutorial credentials `admin` / `admin`, then open **Dashboards → Home Energy Tracker → Home Energy Tracker - Overview**.
- Each Java service exposes `/actuator/prometheus` on its own port. Prometheus collects these metrics every 15 seconds and Grafana refreshes the dashboard every 10 seconds.
- The dashboard shows request rates, HTTP statuses and error rates, JVM heap usage, target reachability, and the busiest endpoints. Allow at least two scrapes for request-rate graphs to appear.
- Prometheus and Grafana data persist in named Docker volumes. A stopped Java application appears as a down target; Docker infrastructure does not start Java applications automatically.

Ingestion normally generates 10,000 requests each minute. For quiet manual checks, temporarily use `--simulation.requests-per-interval=0`. Existing sample-data test methods remain disabled.

The checked-in credentials are tutorial defaults for local development. Application data, private realm exports, backups, IDE settings, and build output are excluded from Git. Stop infrastructure with `docker compose down`; preserve its volumes to keep existing data.
