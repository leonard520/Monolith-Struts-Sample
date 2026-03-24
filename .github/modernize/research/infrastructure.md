# Infrastructure Research

## Database Dependencies

- **Database**: PostgreSQL 9.2 (Docker image: `postgres:9.2`)
- **Schema management**: Manual SQL scripts — `schema.sql` and `data.sql` in `src/main/resources/db/`
- **Init method**: Docker entrypoint mounts `./src/main/resources/db` → `/docker-entrypoint-initdb.d:ro`
- **Connection pool**: Apache Commons DBCP 1.2.2 (`BasicDataSource`)
- **Connection configuration** (from docker-compose.yml environment):
  - `DB_HOST=db`, `DB_PORT=5432`, `DB_NAME=skishop`
  - `DB_USER=skishop`, `DB_PASSWORD=skishop`
  - `DB_POOL_MAX_ACTIVE=20`, `DB_POOL_MAX_IDLE=5`, `DB_POOL_MAX_WAIT=10000`
- **JNDI resource**: `jdbc/skishop` defined in web.xml and Tomcat context

## Messaging
None — no message queues or event buses.

## Caching
None — no Redis, Memcached, or in-process caching detected.

## External Services
- **SMTP email**: `javax.mail` API for sending emails via configured SMTP server
- No payment gateway integration (payment appears simulated internally)

## File Storage
None — no local filesystem writes, no S3, no FTP.

## Test Execution Requirements

### Unit Tests (DAO layer)
- Use **H2 in-memory database** (`com.h2database:h2:1.3.176`)
- `DaoTestBase` likely initializes H2 with schema.sql for test isolation
- No real PostgreSQL required for DAO tests

### Unit Tests (Action layer)
- Use **StrutsTestCase** (MockStrutsTestCase) for testing Struts Actions
- `StrutsActionTestBase` provides common setup
- Uses in-process servlet container mock — no real Tomcat required

### Integration Tests
- **api-test.sh**: Bash script using `curl` — requires running application + PostgreSQL database
- Runs against `http://localhost:8080` (or custom BASE_URL)
- Requires seed data from `data.sql` to be loaded
- Tests ~50+ scenarios across all public and protected endpoints
- Session-based testing with cookie jars

### Docker Infrastructure
- **docker-compose.yml**: Two services — `app` (Tomcat 6 + JDK 5) and `db` (PostgreSQL 9.2)
- **Dockerfile**: Multi-stage build (JDK 5 + Maven 2.2.1 → Tomcat 6.0.53 runtime)
- **Volumes**: `db-data` (persistent DB), `tomcat-logs` (app logs)
- **Health check**: PostgreSQL healthcheck via `pg_isready`
