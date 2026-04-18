<!--
Sync Impact Report
- Version change: N/A → 1.0.0 (initial)
- Added sections: all (initial constitution)
- Removed sections: none
- Templates requiring updates: none (first version)
- Follow-up TODOs: none
-->

# SkiShop Monolith Migration Constitution

## Migration Mode

**Mode**: REWRITE

**Justification**: The source application uses Struts 1.3 (2008-era framework, EOL) on Java 5 with a WAR-based Tomcat 6 deployment. An in-place upgrade is infeasible because Struts 1.x has no migration path to Spring MVC — the programming models (ActionForm/ActionMapping vs. annotated controllers) are fundamentally different. A fresh Spring Boot 3.x project with business logic extracted from the source preserves functional equivalence while achieving a modern, maintainable stack.

## Core Principles

### I. Functional Equivalence — Match the Source, Not Improve It

Every URL, form submission, redirect, error page, and session behavior in the source application MUST be reproduced in the target. The `api-test.sh` test script is the acceptance oracle — all tests MUST pass. Screenshots in `screenshot/` are the visual truth for page appearance.

**Rules:**
- Do NOT add features, refactor business logic, or "improve" behavior that already works.
- Do NOT change URL paths. The test script expects exact paths: `/home`, `/products`, `/product?id=...`, `/login`, `/register`, `/password/forgot`, `/password/reset`, `/cart`, `/coupons`, `/coupons/apply`, `/checkout`, `/orders`, `/orders/{id}/cancel`, `/orders/{id}/return`, `/points`, `/account/addresses`, `/account/addresses/edit`, `/account/addresses/save`, `/admin/products`, `/admin/orders`, `/admin/coupons`, `/admin/shipping`, `/logout`.
- Do NOT change HTTP status code semantics. The test script validates specific status codes per endpoint.
- CSRF token field MUST be named `_csrfToken` to match the test script's `extract_csrf` function.
- Session-based authentication MUST be preserved (cookie jar flow in test script).

### II. Keep the JSPs — Rewrite Only the Server Side

The user explicitly requires that JSP views are kept. The view layer (JSPs under `WEB-INF/jsp/`) MUST be migrated to work with Spring MVC's JSP resolver, NOT replaced with Thymeleaf or another template engine.

**Rules:**
- JSP files MUST remain under `src/main/webapp/WEB-INF/jsp/` (Spring Boot embedded Tomcat serves them from there).
- Struts tag libraries (`<s:property>`, `<bean:write>`, `<html:form>`, etc.) MUST be replaced with JSTL (`<c:out>`, `<c:forEach>`, `<c:if>`) and Spring form tags (`<form:form>`, `<form:input>`, etc.) or plain EL (`${...}`).
- OGNL expressions MUST be converted to standard JSP EL.
- Tiles layout definitions MUST be replaced. Use JSP includes (`<jsp:include>`) or Spring Tiles 3 if the layout structure is complex. The base layout pattern (header/body/footer) from `tiles-defs.xml` MUST be preserved.
- Static assets in `src/main/webapp/assets/` MUST remain accessible at the same paths.

### III. New Project, Clean Separation

The target application MUST be a new Maven project in the `monolith-new/` directory (as expected by `api-test.sh` which references `${SCRIPT_DIR}/monolith-new`). It MUST NOT modify the original source under `src/`.

**Rules:**
- Target project root: `monolith-new/`
- GroupId: `com.skishop`, ArtifactId: `skishop-app`, Version: `2.0.0`
- Package structure: `com.skishop` (same base package as source)
- Final artifact: `skishop-app-2.0.0.jar` (executable Spring Boot JAR — matches test script's `java -jar` command)
- Spring profile `h2` MUST activate an H2 in-memory database with the same schema/seed data for testing.
- The original `src/` directory MUST NOT be modified. It is read-only reference material.

### IV. Preserve the Data Model Exactly

The database schema (`schema.sql`) and seed data (`data.sql`) define the contract. All table names, column names, types, and relationships MUST be preserved.

**Rules:**
- The H2 profile MUST load `schema.sql` and `data.sql` at startup (Spring Boot's `spring.sql.init` mechanism).
- The PostgreSQL profile MUST connect to the existing schema without DDL changes.
- Domain classes MUST map 1:1 to database tables. Do NOT introduce JPA/Hibernate — keep the existing JDBC/commons-dbutils data access pattern and port it to Spring's `JdbcTemplate` or `NamedParameterJdbcTemplate`.
- String-based IDs (UUIDs as VARCHAR(36)) MUST be preserved, not replaced with auto-increment.

### V. Security Parity

The source application uses a custom `AuthRequestProcessor` for role-based access control and custom password hashing with salt. The target MUST achieve equivalent security.

**Rules:**
- Use Spring Security for authentication and authorization.
- Role checks: `USER` and `ADMIN` roles MUST gate the same endpoints as in `struts-config.xml` `roles=""` attributes.
- Password hashing: replicate the source's hash+salt algorithm exactly so existing user records remain valid.
- CSRF protection MUST use a hidden field named `_csrfToken` (matching the test script).
- Login form fields: `email` and `password`. Register form fields: `email`, `username`, `password`, `passwordConfirm`.
- Unauthenticated access to protected pages MUST redirect to `/login`.
- `/logout` MUST invalidate the session and redirect to home.

### VI. Build and Run Contract

The test script defines the build and run contract. The target MUST satisfy it exactly.

**Rules:**
- `mvn -f monolith-new/pom.xml clean package -DskipTests -q` MUST succeed.
- `java -jar monolith-new/target/skishop-app-2.0.0.jar --spring.profiles.active=h2` MUST start the application.
- The application MUST respond on port 8080 with HTTP 200 at `/` within 120 seconds.
- The application MUST be fully functional with H2 in-memory database (no external dependencies for testing).

### VII. No Over-Engineering

This is a faithful rewrite, not a modernization showcase.

**Rules:**
- Do NOT introduce microservices, event sourcing, CQRS, or other architectural patterns not present in the source.
- Do NOT add OpenAPI/Swagger, actuator endpoints beyond what Spring Boot provides by default, or monitoring infrastructure.
- Do NOT add Lombok, MapStruct, or other code-generation libraries. Keep POJOs simple.
- Do NOT add Spring Data JPA. Use JdbcTemplate to match the source's raw JDBC approach.
- Exception handling: a global `@ControllerAdvice` replacing Struts' `global-exceptions` is acceptable.

## Target Technology Stack

| Component | Target Version | Notes |
|-----------|---------------|-------|
| JDK | 21 | LTS, required by Spring Boot 3.x |
| Framework | Spring Boot 3.2.x | Spring MVC + embedded Tomcat for JSP |
| Build Tool | Maven 3.9.x | Same build tool as source |
| View | JSP + JSTL + Spring Form Tags | Keeping JSPs per user requirement |
| Database (prod) | PostgreSQL 16 | Same RDBMS family as source |
| Database (test) | H2 in-memory | For `api-test.sh` / CI |
| Security | Spring Security 6.x | Replaces custom AuthRequestProcessor |
| JDBC | Spring JdbcTemplate | Replaces commons-dbutils |
| Logging | SLF4J + Logback | Spring Boot default, replaces Log4j 1.x |
| Packaging | Executable JAR | Replaces WAR + external Tomcat |

## URL Routing Contract

The test script defines these exact URL mappings. Every one MUST work:

| Method | Path | Auth | Notes |
|--------|------|------|-------|
| GET | `/` | Public | Home page, HTTP 200 |
| GET | `/home` | Public | Home alias, HTTP 200 |
| GET | `/products` | Public | Product list with pagination (`page`, `size`) and search (`keyword`, `categoryId`) |
| GET | `/product?id={id}` | Public | Product detail |
| GET | `/login` | Public | Login form |
| POST | `/login` | Public | Authenticate (email, password, _csrfToken) |
| GET | `/register` | Public | Registration form |
| POST | `/register` | Public | Register (email, username, password, passwordConfirm, _csrfToken) |
| GET | `/password/forgot` | Public | Forgot password form |
| POST | `/password/forgot` | Public | Request reset (email, _csrfToken) |
| GET | `/password/reset` | Public | Reset form (optional `token` param) |
| POST | `/password/reset` | Public | Execute reset (token, password, passwordConfirm, _csrfToken) |
| GET | `/cart` | Public | View cart |
| GET | `/coupons` | Public | List coupons |
| POST | `/coupons/apply` | Public | Apply coupon (code, _csrfToken) |
| GET | `/checkout` | Public | Checkout form |
| POST | `/checkout` | Public | Place order (payment fields, _csrfToken) |
| GET | `/orders` | USER,ADMIN | Order history |
| POST | `/orders/{id}/cancel` | USER,ADMIN | Cancel order |
| POST | `/orders/{id}/return` | USER,ADMIN | Return order |
| GET | `/points` | USER,ADMIN | Point balance |
| GET | `/account/addresses` | USER,ADMIN | Address list |
| GET | `/account/addresses/edit` | USER,ADMIN | Address form |
| POST | `/account/addresses/save` | USER,ADMIN | Save address |
| GET | `/logout` | USER,ADMIN | Logout |
| GET | `/admin/products` | ADMIN | Admin product list |
| GET | `/admin/orders` | ADMIN | Admin order list |
| GET | `/admin/coupons` | ADMIN | Admin coupon list |
| GET | `/admin/shipping` | ADMIN | Admin shipping methods |

## Source-to-Target Layer Mapping

| Source Layer | Source Artifact | Target Layer | Target Artifact |
|-------------|----------------|--------------|-----------------|
| Action classes | `com.skishop.web.action.*` | Controllers | `com.skishop.web.controller.*` |
| ActionForms | `com.skishop.web.form.*` | Form beans (POJOs) | `com.skishop.web.form.*` |
| Service layer | `com.skishop.service.*` | Service layer | `com.skishop.service.*` |
| DAO layer | `com.skishop.dao.*` | Repository layer | `com.skishop.dao.*` |
| Domain objects | `com.skishop.domain.*` | Domain objects | `com.skishop.domain.*` |
| Common utilities | `com.skishop.common.*` | Common utilities | `com.skishop.common.*` |
| Struts filters | `com.skishop.web.filter.*` | Spring Security filters | Spring Security config |
| `struts-config.xml` | Action mappings | `@Controller` annotations | Method-level `@RequestMapping` |
| `tiles-defs.xml` | Layout definitions | JSP includes | `<jsp:include>` or equivalent |
| `validation.xml` | Struts Validator | Bean Validation | `@Valid` + `jakarta.validation` annotations |
| `web.xml` | Servlet config | Spring Boot auto-config | `application.properties` + Java config |

## Applied Guidelines
- **Guideline**: struts-to-spring
- **Reference**: skills/guidelines/struts-to-spring/SKILL.md

## Governance

- This constitution is the authoritative source for all migration decisions. All roles MUST comply.
- Any deviation from this constitution MUST be escalated to the teamlead via `[notify:teamlead]`.
- Amendments require documentation of the change, rationale, and impact assessment.
- The `api-test.sh` script is the final acceptance criterion. A passing test suite is required for completion.

**Version**: 1.0.0 | **Ratified**: 2026-04-18 | **Last Amended**: 2026-04-18
