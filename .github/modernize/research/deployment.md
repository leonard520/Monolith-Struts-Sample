# Deployment Research

## Docker Configuration

### Dockerfile (Multi-stage build)
- **Stage 1: Build** — Debian Stretch + JDK 1.5.0_22 + Maven 2.2.1
  - Copies local `binaries/jdk-1_5_0_22-linux-amd64.bin` (or downloads from archive.org)
  - Pre-populates Maven repository from `binaries/m2/`
  - Runs `mvn -B -Dmaven.test.skip=true package`
- **Stage 2: Runtime** — Debian Stretch + JDK 1.5.0_22 + Tomcat 6.0.53
  - Copies WAR from build stage to Tomcat webapps
  - Entrypoint: `docker/entrypoint.sh`

### Dockerfile.tomcat6
- Alternative Dockerfile (presumably similar targeting)

### docker-compose.yml
```yaml
services:
  app:
    platform: linux/amd64
    build: . (Dockerfile)
    ports: 8080:8080
    environment:
      DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
      DB_POOL_MAX_ACTIVE=20, DB_POOL_MAX_IDLE=5, DB_POOL_MAX_WAIT=10000
    depends_on: db
    volumes: tomcat-logs

  db:
    image: postgres:9.2
    platform: linux/amd64
    environment:
      POSTGRES_DB=skishop, POSTGRES_USER=skishop, POSTGRES_PASSWORD=skishop
    ports: 5432:5432
    volumes:
      - db-data (persistent)
      - ./src/main/resources/db → /docker-entrypoint-initdb.d:ro
    healthcheck: pg_isready -U skishop
```

### Entrypoint Script
- `docker/entrypoint.sh` — startup script for Tomcat, likely handles env var substitution

## CI/CD Pipeline
- No CI/CD configuration files detected (no `.github/workflows/`, no `Jenkinsfile`, no `.gitlab-ci.yml`)

## Environment Variables
| Variable | Default | Purpose |
|----------|---------|---------|
| `DB_HOST` | db | PostgreSQL hostname |
| `DB_PORT` | 5432 | PostgreSQL port |
| `DB_NAME` | skishop | Database name |
| `DB_USER` | skishop | Database username |
| `DB_PASSWORD` | skishop | Database password |
| `DB_POOL_MAX_ACTIVE` | 20 | DBCP max active connections |
| `DB_POOL_MAX_IDLE` | 5 | DBCP max idle connections |
| `DB_POOL_MAX_WAIT` | 10000 | DBCP max wait (ms) |

## Infrastructure as Code
None — Docker Compose only, no Terraform/CloudFormation/Kubernetes manifests.

## Monitoring/Logging
- **Logging**: log4j 1.2 (`log4j.properties`)
- **No structured logging** — plain text output
- **No monitoring** endpoints (no health check, metrics, or actuator)
- Tomcat logs volume-mounted to host
