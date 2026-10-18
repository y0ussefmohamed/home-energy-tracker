# Home Energy Tracker

## Current progress

- **User-Service:** user CRUD, Flyway migration, service logging, and controller execution timing through AOP.
- **Device Service:** initial Spring Boot project created; lesson 4 implementation is in progress.
- **Docker:** MySQL 8.3.0 with persistent storage for the `home_energy_tracker` database.

The services use Java 21, Spring Boot 4.1.1, and their own Maven wrappers. Database migrations currently belong to User-Service.

## Run locally

Start Docker Desktop, then run this from the project root:

```sh
docker compose up -d
```

Run `UserServiceApplication` from IntelliJ, or start it with:

```sh
cd User-Service
./mvnw spring-boot:run
```

User-Service listens on port `8080`, with endpoints under `/api/v1/user`. Use IntelliJ's HTTP client plugin to send requests.

The checked-in MySQL credentials (`root` / `password`) are tutorial defaults for local development. Database contents, private environment files, IDE settings, and build output are excluded from Git.
