<!--
Sync Impact Report
==================
- Version change: N/A → 1.0.0 (initial creation)
- Added sections:
  - Migration Mode (REWRITE)
  - Core Principles (5 principles)
  - Target Technology Stack
  - Migration Constraints
  - Development Workflow
  - Governance
- Templates requiring updates:
  - plan-template.md: ✅ reviewed — Constitution Check aligns
  - spec-template.md: ✅ reviewed — scope/requirements compatible
  - tasks-template.md: ✅ reviewed — task categorization compatible
- Follow-up TODOs: None
-->

# SkiShop Monolith Constitution

## Migration Mode

**Mode**: REWRITE

**Justification**: The existing application is built on Struts 1.3.10 with Java 1.5 syntax, Servlet 2.5, and JSP views — a technology stack that is end-of-life with no security patches. An in-place upgrade is not feasible because Struts 1.x has no migration path to a modern framework; the controller model, form-bean lifecycle, and XML-driven action mappings have no direct equivalents in Spring MVC. A full rewrite to Spring Boot + Spring MVC allows adopting modern Java (JDK 21), embedded server deployment, annotation-driven controllers, and a maintainable layered architecture while preserving all existing business logic and database schema.

## Core Principles

### I. Functional Equivalence (NON-NEGOTIABLE)

Every user-facing behavior in the Struts application MUST be preserved in the Spring MVC rewrite. This includes all HTTP endpoints (mapped from `*.do` patterns to clean REST-style URLs), form validation rules, session management, authentication/authorization enforcement, error handling, and HTML response content. The `api-test.sh` integration test script MUST pass against the rewritten application without modification to its assertions. No feature may be dropped, altered in behavior, or deferred without explicit documented justification.

**Rationale**: The rewrite exists to modernize the technology stack, not to change product behavior. Functional regression is the primary risk of any rewrite project.

### II. Layered Architecture Preservation

The existing layered architecture (Action → Service → DAO → Domain) MUST be mapped to the Spring equivalent (Controller → Service → Repository → Entity/DTO). Each layer's responsibilities MUST remain cleanly separated:
- **Controllers** handle HTTP request/response mapping only — no business logic.
- **Services** contain all business logic and transaction coordination.
- **Repositories** encapsulate all database access via Spring JDBC or JPA.
- **Domain objects** remain plain data carriers with no framework coupling.

Cross-layer calls MUST follow the dependency direction: Controller → Service → Repository. Reverse dependencies are forbidden.

**Rationale**: The source application already follows this layering. Preserving it reduces rewrite risk and ensures the team's domain knowledge transfers directly.

### III. Database Schema Integrity (NON-NEGOTIABLE)

The existing PostgreSQL schema (`schema.sql`) and seed data (`data.sql`) MUST NOT be modified. The rewritten application MUST work against the identical database schema. All table names, column names, data types, indexes, and constraints MUST remain unchanged. SQL queries MUST produce identical results for identical inputs.

**Rationale**: The database is the system of record. Schema changes would require data migration, increase risk, and invalidate the functional equivalence guarantee.

### IV. Incremental Verifiability

The rewrite MUST be structured so that each implemented batch can be independently compiled and tested. Every batch MUST:
- Compile without errors (`mvn compile`).
- Pass all applicable unit tests.
- Maintain backward compatibility with previously completed batches.

The `api-test.sh` script serves as the final end-to-end validation gate. Runtime validation using `api-test.sh` MUST be performed after all batches are complete.

**Rationale**: Large rewrites fail when integration is deferred. Incremental verification catches regressions early and maintains confidence throughout the migration.

### V. Modern Stack, Minimal Complexity

The rewrite MUST use Spring Boot 3.2.x with Spring MVC, targeting JDK 21 and Maven as the build tool. However, the migration MUST NOT introduce unnecessary complexity:
- Use Spring JDBC (JdbcTemplate) for data access to preserve existing SQL queries — do NOT introduce JPA/Hibernate unless explicitly justified.
- Use JSP views with JSTL (not Thymeleaf or other template engines) to minimize view-layer rewrite scope.
- Do NOT add microservices, message queues, caching layers, or other infrastructure that the original application does not use.
- Keep the application as a single deployable WAR/JAR, matching the monolithic deployment model.

**Rationale**: The goal is a modernized Struts application, not a redesigned system. YAGNI applies — introduce new patterns only when the existing application's behavior demands them.

## Target Technology Stack

| Component | Target Version | Notes |
|-----------|---------------|-------|
| JDK | 21 | LTS release, required by Spring Boot 3.x |
| Framework | Spring Boot 3.2.x | Spring MVC for web layer |
| Build Tool | Maven 3.9.x | Preserves existing Maven build workflow |
| Database | PostgreSQL 9.2+ | Existing schema unchanged |
| View Layer | JSP + JSTL | Minimizes view migration scope |
| Testing | JUnit 5 + Spring Test | Modern testing framework |
| Logging | SLF4J + Logback | Spring Boot default, replaces log4j 1.2 |

## Migration Constraints

- **URL Mapping**: All Struts `*.do` URL patterns MUST be mapped to equivalent Spring MVC `@RequestMapping` paths. If the test script uses specific URL patterns, those exact patterns MUST be supported.
- **Session Handling**: The Struts session-scoped form beans MUST be replicated using Spring's `@SessionAttributes` or `HttpSession` as appropriate.
- **Authentication**: The `AuthRequestProcessor` role-based access control MUST be replicated using Spring Security or servlet filters with equivalent behavior.
- **Form Validation**: Struts XML validation rules (`validation.xml`) MUST be replicated using Spring Validation (Bean Validation / `@Valid` annotations or programmatic validation).
- **Tiles Layout**: The Tiles-based page composition MUST be preserved — either via Apache Tiles integration with Spring MVC or an equivalent JSP include mechanism that produces identical HTML structure.
- **Configuration**: `app.properties` configuration with `${TOKEN}` placeholder resolution MUST continue to work, leveraging Spring Boot's native property resolution.
- **Email**: SMTP-based email queue processing MUST be preserved using Spring's `JavaMailSender`.
- **Error Handling**: Global exception handling (`global-exceptions` in struts-config.xml) MUST be replicated via `@ControllerAdvice` / `@ExceptionHandler`.

## Development Workflow

- **Build command**: `mvn -B clean package`
- **Test command**: `mvn -B test`
- **Integration test**: `./api-test.sh` (requires running application + database)
- **Code review**: All changes MUST be reviewed against this constitution before merge.
- **Branch strategy**: Feature branches per migration batch, merged to main after verification.

## Governance

This constitution is the authoritative reference for all migration decisions. Any deviation from these principles MUST be documented with explicit justification and approved before implementation.

- **Amendment procedure**: Propose change → document rationale → update constitution → increment version.
- **Versioning policy**: MAJOR for principle removal/redefinition, MINOR for new principle/section, PATCH for clarifications.
- **Compliance review**: Every implementation batch MUST be verified against Principles I–V before being considered complete.

**Version**: 1.0.0 | **Ratified**: 2026-03-20 | **Last Amended**: 2026-03-20
