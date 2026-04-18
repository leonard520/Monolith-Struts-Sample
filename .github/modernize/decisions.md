## [architect] [t2] — 2026-04-18
**Decision**: Authentication uses Spring Security for URL authorization only; login/logout handled manually in AuthController (matching source pattern). `formLogin()` DISABLED.
**Rationale**: Source uses manual AuthService.authenticate() with session-based auth. Spring Security's formLogin would intercept POST /login before the controller, breaking the test script.

## [architect] [t2] — 2026-04-18
**Decision**: CSRF uses `HttpSessionCsrfTokenRepository` with `parameterName("_csrfToken")` instead of Spring's default `_csrf`.
**Rationale**: Test script api-test.sh expects `name="_csrfToken"` in all forms. Changing would break all POST tests.

## [architect] [t2] — 2026-04-18
**Decision**: DAO layer uses JdbcTemplate (no JPA/Hibernate). SQL statements preserved from source.
**Rationale**: Constitution mandates no JPA. Source uses Commons DBUtils with raw JDBC — JdbcTemplate is the minimal Spring equivalent.

## [architect] [t2] — 2026-04-18
**Decision**: JSP layout uses `<jsp:include>` for header/messages/footer replacing Tiles.
**Rationale**: Tiles not available in Spring Boot. Simple includes match the source's layout composition with less complexity.

## [architect] [t2] — 2026-04-18
**Decision**: OrderFacade.placeOrder uses compensating actions (no @Transactional).
**Rationale**: Source pattern uses manual compensation. Constitution requires behavioral fidelity.
