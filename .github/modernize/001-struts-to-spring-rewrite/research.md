# Research: Struts 1.x to Spring Boot 3.2 MVC Rewrite

**Feature**: 001-struts-to-spring-rewrite
**Date**: 2026-03-21
**Mode**: REWRITE

## Technical Decisions

### TD-001: Target Framework — Spring Boot 3.2.x with Spring MVC

- **Decision**: Use Spring Boot 3.2.x with embedded Tomcat and Spring MVC for the web layer
- **Rationale**: Constitution mandates Spring Boot 3.2.x (Principle V). Spring MVC provides annotation-driven controllers that naturally replace Struts Action classes. Embedded Tomcat eliminates external application server dependency.
- **Alternatives considered**:
  - Spring Boot 3.3.x — rejected; constitution specifies 3.2.x
  - Jakarta EE 10 — rejected; no team familiarity advantage, constitution mandates Spring Boot

### TD-002: Data Access — Spring JDBC (JdbcTemplate)

- **Decision**: Use Spring JDBC (`JdbcTemplate`) for all data access, preserving existing SQL queries
- **Rationale**: Constitution Principle V explicitly prohibits JPA/Hibernate. The existing 20 DAO implementations use raw JDBC via `commons-dbutils`. `JdbcTemplate` provides a clean 1:1 migration path — same SQL, same result-set mapping, but with Spring-managed connection pooling (HikariCP) and declarative transaction management (`@Transactional`).
- **Alternatives considered**:
  - JPA/Hibernate — rejected; constitution forbids it (NFR-004)
  - Spring Data JDBC — rejected; requires entity-mapping annotations that would change the domain model; JdbcTemplate is closer to existing code
  - Keep commons-dbutils — rejected; would retain a dead dependency; JdbcTemplate is the idiomatic Spring approach

### TD-003: View Layer — JSP + JSTL (preserve, not replace)

- **Decision**: Keep JSP views with JSTL. Replace Struts tag libraries (`<html:form>`, `<bean:write>`, `<logic:iterate>`) with JSTL and Spring form tags. Replicate Tiles layout with JSP includes.
- **Rationale**: Constitution Principle V prohibits Thymeleaf. JSP + JSTL minimizes view-layer rewrite scope. Existing ~30 JSP files can be migrated by tag substitution rather than full rewrite.
- **Alternatives considered**:
  - Thymeleaf — rejected; constitution forbids it
  - Apache Tiles 3 with Spring MVC — considered but rejected; Tiles 3 adds unnecessary dependency complexity. JSP includes (`<%@ include %>` or `<jsp:include>`) can replicate the base-layout composition pattern (header/messages/body/footer) with less coupling.

### TD-004: Authentication — HandlerInterceptor + HttpSession (not Spring Security)

- **Decision**: Replicate `AuthRequestProcessor` behavior using a Spring `HandlerInterceptor` that checks `HttpSession` for the authenticated user and validates role-based access. Do NOT use Spring Security.
- **Rationale**: The existing auth model is simple: session attribute holds `User` object, role checking is a string comparison against a URL→roles mapping. Spring Security would be massive overkill and would change the authentication behavior (filter chain, SecurityContext, CSRF mechanism). Using a `HandlerInterceptor` preserves the exact behavioral semantics of the existing `AuthRequestProcessor`.
- **Alternatives considered**:
  - Spring Security — rejected; introduces complex filter chain, auto-CSRF (different token mechanism), form login redirect behavior that may not match existing behavior precisely. Would risk functional equivalence violations.

### TD-005: CSRF Protection — Custom Token (match existing behavior)

- **Decision**: Implement a custom CSRF token mechanism matching the existing `TokenTag` + `AuthRequestProcessor` validation pattern. Generate tokens stored in session, validated on form POSTs via a hidden `_csrfToken` field.
- **Rationale**: The existing Struts application uses Struts `TokenProcessor` for CSRF. The api-test.sh likely depends on this exact token mechanism. Using Spring Security's CSRF would produce different token names, header expectations, and error responses.
- **Alternatives considered**:
  - Spring Security CSRF — rejected; different token generation, different field names, different error behavior

### TD-006: Connection Pooling — HikariCP (Spring Boot default)

- **Decision**: Replace Apache Commons DBCP 1.2.2 with HikariCP (Spring Boot default DataSource)
- **Rationale**: HikariCP is the Spring Boot default, vastly superior to DBCP 1.x, and requires zero additional configuration. Environment variables (DB_HOST, DB_PORT, etc.) will be mapped to `spring.datasource.*` properties. Pool size parameters (DB_POOL_MAX_ACTIVE, DB_POOL_MAX_IDLE, DB_POOL_MAX_WAIT) will map to HikariCP equivalents.
- **Alternatives considered**:
  - DBCP2 — rejected; HikariCP is better and already included in Spring Boot

### TD-007: Email — Spring JavaMailSender

- **Decision**: Replace direct `javax.mail` usage with Spring's `JavaMailSender` via `spring-boot-starter-mail`
- **Rationale**: Direct 1:1 replacement. `JavaMailSender` supports the same SMTP transport with Spring Boot auto-configuration. The `EmailQueue` pattern (store in DB, process asynchronously) will be preserved in the service layer.
- **Alternatives considered**:
  - Jakarta Mail directly — rejected; Spring JavaMailSender is the idiomatic approach and integrates with Spring Boot auto-configuration

### TD-008: Testing — JUnit 5 + Spring Test + MockMvc

- **Decision**: Migrate all tests to JUnit 5. DAO tests use `@JdbcTest` with H2. Action tests are rewritten as `@WebMvcTest` with `MockMvc`. Service tests use `@SpringBootTest` or plain JUnit 5 with mocks.
- **Rationale**: Constitution mandates JUnit 5. StrutsTestCase has no migration path — all 22 action tests must be rewritten. The 14 DAO tests are mostly portable (SQL + H2 are framework-independent). MockMvc provides equivalent functional testing for controllers.
- **Alternatives considered**:
  - Keep JUnit 4 — rejected; constitution mandates JUnit 5
  - TestContainers for DAO tests — considered but deferred; H2 works for unit tests, PostgreSQL testing happens via `api-test.sh`

### TD-009: Logging — SLF4J + Logback

- **Decision**: Replace log4j 1.2 with SLF4J + Logback (Spring Boot defaults)
- **Rationale**: log4j 1.2 is EOL with known CVEs. SLF4J + Logback is the Spring Boot default, configured via `application.properties` or `logback-spring.xml`.
- **Alternatives considered**:
  - Log4j 2 — rejected; Logback is the Spring Boot default, simpler to configure

### TD-010: Tiles Layout Replacement — JSP Include Pattern

- **Decision**: Replace Tiles definitions with a JSP include-based layout. Create a `layouts/base.jsp` that includes header, messages, body content, and footer via `<jsp:include>`. Each page JSP sets content attributes and includes the layout.
- **Rationale**: Tiles 3 integration with Spring Boot requires additional dependencies and XML configuration. A JSP include pattern achieves the same HTML composition with zero additional dependencies.
- **Alternatives considered**:
  - Apache Tiles 3 — rejected; additional dependency, XML config baggage
  - SiteMesh — rejected; another library dependency

### TD-011: Dependency Injection — Spring @Autowired

- **Decision**: Replace `ServiceLocator` and `DaoFactory` static factory patterns with Spring `@Component`/`@Service`/`@Repository` annotations and constructor injection
- **Rationale**: This is the core Spring pattern. Constructor injection is preferred over field injection for testability.
- **Alternatives considered**:
  - Field injection (@Autowired on fields) — rejected for services/DAOs; constructor injection is more testable
  - Manual bean registration — rejected; component scanning is simpler for this single-module project

### TD-012: Transaction Management — @Transactional

- **Decision**: Replace manual connection management in `AbstractDao` and manual transaction coordination in `OrderFacadeImpl` with Spring's `@Transactional` annotation on service methods
- **Rationale**: Spring's declarative transaction management is the standard approach. The `OrderFacadeImpl` orchestration (cart → inventory → order → payment → points → email) must be wrapped in a single transaction to preserve the existing atomicity guarantee.
- **Alternatives considered**:
  - Programmatic transactions (TransactionTemplate) — rejected for most cases; @Transactional is simpler. May use TransactionTemplate selectively if needed for the OrderFacade's complex multi-step flow.

## Applied Guidelines

### Struts-to-Spring Migration Guideline

**Source**: `.github/skills/guidelines/struts-to-spring/`

Applicable skills integrated into plan phases:
- `migrate-maven-dependencies` → Phase 1 (Project Setup)
- `create-spring-boot-application` → Phase 1 (Project Setup)
- `create-application-properties` → Phase 1 (Project Setup)
- `migrate-web-xml` → Phase 1 (Project Setup)
- `convert-action-to-controller` → Phase 3–5 (Controller Implementation)
- `convert-action-properties` → Phase 3–5 (Controller Implementation)
- `convert-validation` → Phase 3–5 (Form Validation)
- `convert-interceptor` → Phase 2 (Auth Interceptor)
- `convert-jsp-tags` → Phase 3–5 (JSP Migration)
- `convert-ognl-to-el` → Phase 3–5 (JSP Migration)
- `convert-result-types` → Phase 3–5 (View Resolution)
- `convert-test-classes` → Phase 3–5 (Test Migration)
- `create-exception-handler` → Phase 2 (Error Handling)

### Spring Boot Scaffolding Guideline

**Source**: `.github/skills/guidelines/spring-boot-scaffolding/`

Applicable decisions:
- Single-module project (not multi-module) — matches existing monolith architecture
- JDK 21 (LTS) — per constitution
- Spring Boot 3.2.x parent POM
- `spring-boot-starter-web`, `spring-boot-starter-jdbc`, `spring-boot-starter-mail`, `spring-boot-starter-validation`, `spring-boot-starter-test`

## Resolved Unknowns

All specification items were fully resolved during the DesignAgent phase. No `[NEEDS CLARIFICATION]` markers remain.

| Unknown | Resolution |
|---------|-----------|
| URL mapping from `*.do` to clean paths | Clean URLs per api-test.sh. Spring MVC `@RequestMapping` natively supports clean paths. |
| Tiles layout replacement strategy | JSP include pattern (TD-010) |
| Auth mechanism (Spring Security vs custom) | Custom HandlerInterceptor (TD-004) |
| CSRF token mechanism | Custom matching existing behavior (TD-005) |
| JPA vs JDBC | JdbcTemplate per constitution (TD-002) |
| Session-scoped cart form bean handling | Spring `@SessionAttributes` or `HttpSession` direct manipulation |
| AddressListAction direct DAO access | Fix layering violation — route through AddressService |
