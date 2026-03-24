## Phase 7: Deployment & End-to-End Validation

### 7.1 Dockerfile Rewrite

- [ ] T700 [Plan:7.1] Rewrite `Dockerfile` Stage 1 (build): use `maven:3.9-eclipse-temurin-21` base image, copy `pom.xml` and `src/`, run `mvn -B clean package -DskipTests` to produce executable Spring Boot JAR
- [ ] T701 [Plan:7.1] Rewrite `Dockerfile` Stage 2 (runtime): use `eclipse-temurin:21-jre` base image, `COPY --from=build` the Spring Boot JAR, set `ENTRYPOINT ["java", "-jar", ...]` — no Tomcat installation needed (embedded server)
- [ ] T702 [Plan:7.1] Remove all JDK 5, Tomcat 6, Debian Stretch, and legacy binary references from `Dockerfile` (JDK installer, `catalina.sh`, `postgresql.jar` driver copy, etc.)
- [ ] T703 [Plan:7.1] Remove `platform: linux/amd64` restriction — JDK 21 images support multi-arch (amd64/arm64)
- [ ] T704 [Plan:7.1] Add `EXPOSE 8080` and configure health check label or Spring Boot actuator readiness probe in Dockerfile

### 7.2 docker-compose.yml Update

- [ ] T705 [Plan:7.2] Update `app` service in `docker-compose.yml`: remove legacy build args (`JDK_LICENSE`, `JDK_URL`, `JDK_SHA256`), remove `platform: linux/amd64`, update `container_name` to `skishop-app`
- [ ] T706 [Plan:7.2] Pass Spring-style environment variables to `app` service: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, plus mail env vars as needed
- [ ] T707 [P] [Plan:7.2] Preserve `db` service (postgres:9.2) configuration unchanged — same ports, volumes, healthcheck, init scripts
- [ ] T708 [Plan:7.2] Remove `tomcat-logs` volume, add `depends_on.db.condition: service_healthy` for proper startup ordering
- [ ] T709 [Plan:7.2] Verify `app` service exposes port `8080:8080` and can reach `db` service via Docker network hostname `db`

### 7.3 Entrypoint Script Update

- [ ] T710 [Plan:7.3] Replace `docker/entrypoint.sh` content: remove all Tomcat/Catalina XML generation logic, replace with a simple script that passes environment variables and executes `java -jar` (or remove entrypoint entirely if Dockerfile CMD is sufficient)
- [ ] T711 [Plan:7.3] Ensure DB connection environment variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`) are mapped to Spring datasource properties via `SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}` in entrypoint or docker-compose environment block

### 7.4 End-to-End Validation

- [ ] T712 [Plan:7.4] Run `docker compose down -v && docker compose up --build -d` and verify both `app` and `db` containers start successfully
- [ ] T713 [Plan:7.4] Wait for Spring Boot application startup (poll `http://localhost:8080` or check container logs for "Started" message)
- [ ] T714 [Plan:7.4] Execute `./api-test.sh http://localhost:8080` and verify all ~50+ assertions pass with 0 failures
- [ ] T715 [Plan:7.4] If any api-test assertions fail, diagnose from container logs (`docker compose logs app`), fix the application or configuration, and re-run until 100% pass rate
- [ ] T716 [Plan:7.4] Verify `docker compose down` cleanly shuts down all containers

### 7.5 Final Verification — All Success Criteria

- [ ] T717 [Plan:7.5] **SC-001**: Run `mvn -B clean package` outside Docker — verify zero compilation errors and all unit tests pass
- [ ] T718 [Plan:7.5] **SC-002**: Confirm `api-test.sh` achieves 100% pass rate (all ~50+ assertions) against the running application
- [ ] T719 [Plan:7.5] **SC-003**: Verify all 28 Struts action endpoints are accessible via clean URL paths with correct HTTP status codes and HTML content
- [ ] T720 [Plan:7.5] **SC-004**: Verify all 20 functional domains produce identical observable behavior for identical user inputs
- [ ] T721 [P] [Plan:7.5] **SC-005**: Verify PostgreSQL database schema and seed data remain unmodified — application works against same database state
- [ ] T722 [P] [Plan:7.5] **SC-006**: Confirm each implementation phase compiled independently and passed applicable tests
- [ ] T723 [Plan:7.5] **SC-007**: Verify `docker compose up` successfully builds and runs complete application stack (app + PostgreSQL) with rewritten codebase
- [ ] T724 [Plan:7.5] Create final verification summary documenting all SC results in `docs/verification-report.md`
