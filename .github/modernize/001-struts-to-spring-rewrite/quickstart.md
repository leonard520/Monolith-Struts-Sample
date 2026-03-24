# Quickstart: SkiShop Spring Boot Application

**Feature**: 001-struts-to-spring-rewrite
**Date**: 2026-03-21

## Prerequisites

- JDK 21
- Maven 3.9.x
- Docker & Docker Compose (for PostgreSQL)
- curl (for api-test.sh)

## Build & Run

### 1. Start Database

```bash
docker compose up -d db
```

Wait for PostgreSQL to be ready (healthcheck: `pg_isready -U skishop`).

### 2. Build Application

```bash
mvn -B clean package
```

This compiles the application, runs all unit tests, and produces a runnable JAR.

### 3. Run Application

```bash
java -jar target/skishop-monolith.jar
```

Or with environment variables:

```bash
DB_HOST=localhost DB_PORT=5432 DB_NAME=skishop DB_USER=skishop DB_PASSWORD=skishop \
  java -jar target/skishop-monolith.jar
```

The application starts on `http://localhost:8080`.

### 4. Run Integration Tests

```bash
./api-test.sh
```

This runs ~50+ curl-based assertions against the running application.

### 5. Docker Compose (Full Stack)

```bash
docker compose up --build
```

Starts both the application and PostgreSQL database.

## Key URLs

| URL | Description |
|-----|-------------|
| `/home` | Home page |
| `/products` | Product catalog |
| `/login` | Login form |
| `/register` | Registration form |
| `/cart` | Shopping cart |
| `/admin/products` | Admin product management (requires ADMIN login) |

## Test Users (from seed data)

Refer to `src/main/resources/db/data.sql` for seed user credentials.

## Configuration

Application configuration is in `src/main/resources/application.properties` with Spring Boot property resolution. Environment variables override properties:

| Environment Variable | Property | Default |
|---------------------|----------|---------|
| `DB_HOST` | `spring.datasource.url` (host part) | `localhost` |
| `DB_PORT` | `spring.datasource.url` (port part) | `5432` |
| `DB_NAME` | `spring.datasource.url` (database part) | `skishop` |
| `DB_USER` | `spring.datasource.username` | `skishop` |
| `DB_PASSWORD` | `spring.datasource.password` | `skishop` |
| `DB_POOL_MAX_ACTIVE` | `spring.datasource.hikari.maximum-pool-size` | `20` |

## Verification Steps

1. `mvn -B clean package` → zero errors, all tests pass
2. Application starts within 30 seconds
3. `GET /home` returns HTTP 200
4. `GET /products` returns product listings
5. Login/logout flow works
6. `./api-test.sh` passes all assertions
