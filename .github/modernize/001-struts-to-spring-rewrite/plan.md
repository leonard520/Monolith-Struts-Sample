# Implementation Plan: Struts 1.x to Spring Boot 3.2 MVC Rewrite

**Branch**: `001-struts-to-spring-rewrite` | **Date**: 2026-03-21 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `001-struts-to-spring-rewrite/spec.md`
**Mode**: REWRITE

## Summary

Rewrite the SkiShop monolith from Struts 1.3.10 (Java 1.5, Servlet 2.5, Tomcat 6) to Spring Boot 3.2.x (JDK 21, Spring MVC, embedded Tomcat). The rewrite preserves all 28 endpoints, 20 DAO implementations (migrated to JdbcTemplate), 16 services, and ~30 JSP views while replacing the Struts framework with annotation-driven Spring MVC controllers. The existing PostgreSQL schema and `api-test.sh` integration test must pass unchanged. The plan is structured in 7 phases: project scaffolding, infrastructure/cross-cutting setup, then 3 feature phases (P1→P2→P3 priority order), test migration, and deployment/validation.

## Technical Context

**Source Stack**: Java 1.5 / Struts 1.3.10 / Servlet 2.5 / Tomcat 6.0.53 / PostgreSQL 9.2 / commons-dbcp 1.2.2 / commons-dbutils 1.1 / log4j 1.2.17 / JUnit 4.12

**Target Stack**: JDK 21 / Spring Boot 3.2.x / Spring MVC 6.1.x / Embedded Tomcat / PostgreSQL 9.2+ / HikariCP / JdbcTemplate / SLF4J+Logback / JUnit 5+Spring Test

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: spring-boot-starter-web, spring-boot-starter-jdbc, spring-boot-starter-mail, spring-boot-starter-validation, spring-boot-starter-test, postgresql driver 42.x, jakarta.servlet.jsp-api, jakarta.servlet.jsp.jstl
**Storage**: PostgreSQL (existing schema unchanged — Constitution Principle III, NON-NEGOTIABLE)
**Testing**: JUnit 5 + Spring Test (MockMvc for controllers, @JdbcTest for DAOs) + api-test.sh (integration)
**Target Platform**: Linux server (Docker) / any JDK 21 environment
**Project Type**: Web application (single module)
**Constraints**: No JPA/Hibernate, no Thymeleaf, no microservices, no message queues, no caching (Constitution Principle V / NFR-004)
**Scale/Scope**: 134 Java types, 28 endpoints, ~30 JSP views, 38 test files

### Schema Migration
- **Strategy**: No schema migration. Existing `schema.sql` and `data.sql` are preserved unchanged.
- **Connection pooling**: Commons DBCP → HikariCP (Spring Boot default)
- **Data access**: commons-dbutils (QueryRunner) → Spring JdbcTemplate with identical SQL

### Test Strategy
- **DAO tests (14)**: Migrate to JUnit 5 + `@JdbcTest` + H2 (portable — SQL is framework-independent)
- **Action tests (22)**: Full rewrite to `@WebMvcTest` + MockMvc (StrutsTestCase is not portable)
- **Service tests (2)**: Migrate to JUnit 5 + `@SpringBootTest` or plain unit tests with mocks
- **Integration**: `api-test.sh` unchanged — final validation gate (REQ-042)

### Deployment
- **Dockerfile**: Replace JDK 1.5 + Tomcat 6 with JDK 21 + Spring Boot JAR
- **docker-compose.yml**: Update app service; preserve PostgreSQL 9.2 service unchanged
- **Artifact**: Single executable JAR (spring-boot-maven-plugin)

### Migration Risks
| Risk | Severity | Mitigation |
|------|----------|------------|
| OrderFacadeImpl transaction orchestration | CRITICAL | Comprehensive test coverage; @Transactional with manual rollback for payment/inventory |
| AuthRequestProcessor CSRF + role checking | CRITICAL | Custom HandlerInterceptor; test each protected endpoint |
| Form bean validation parity | HIGH | Map all validation.xml rules to Bean Validation; test each form |
| Session-scoped cart form bean | HIGH | Use HttpSession directly or @SessionAttributes |
| Tiles layout composition | MEDIUM | JSP includes pattern; verify visual parity |
| URL mapping (*.do → clean paths) | LOW | Spring MVC @RequestMapping natively handles clean URLs |

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

### Pre-Design Check ✅

| # | Principle | Status | Notes |
|---|-----------|--------|-------|
| I | Functional Equivalence (NON-NEGOTIABLE) | ✅ PASS | All 28 endpoints mapped; api-test.sh is final gate (REQ-042) |
| II | Layered Architecture Preservation | ✅ PASS | Action→Controller, Service→@Service, DAO→@Repository, Domain unchanged |
| III | Database Schema Integrity (NON-NEGOTIABLE) | ✅ PASS | schema.sql & data.sql preserved unchanged; JdbcTemplate uses same SQL |
| IV | Incremental Verifiability | ✅ PASS | 7 phases; each batch compiles + passes applicable tests |
| V | Modern Stack, Minimal Complexity | ✅ PASS | Spring Boot 3.2.x, JDK 21, JdbcTemplate (no JPA), JSP (no Thymeleaf) |

### Post-Design Check ✅

| # | Principle | Status | Notes |
|---|-----------|--------|-------|
| I | Functional Equivalence | ✅ PASS | Business logic inventory (28 BL units) fully cataloged; all preserved |
| II | Layered Architecture | ✅ PASS | AddressListAction direct-DAO-access violation will be fixed (route through service) |
| III | Schema Integrity | ✅ PASS | Data model mirrors existing schema exactly; no renames/restructures |
| IV | Incremental Verifiability | ✅ PASS | Each phase has independent compile+test gate |
| V | Minimal Complexity | ✅ PASS | No Spring Security (HandlerInterceptor instead); JSP includes (no Tiles 3); no extra infrastructure |

### Applied Guidelines

| Guideline | Source | Integration |
|-----------|--------|-------------|
| Struts-to-Spring Migration | `.github/skills/guidelines/struts-to-spring/` | 13 skills integrated across phases (config, action, view, interceptor, test, xml) |
| Spring Boot Scaffolding | `.github/skills/guidelines/spring-boot-scaffolding/` | Single-module structure, Spring Boot parent POM, starter dependencies |

## Project Structure

### Documentation (this feature)

```text
.github/modernize/001-struts-to-spring-rewrite/
├── spec.md                      # Feature specification
├── plan.md                      # This file
├── research.md                  # Phase 0: technical decisions
├── business-logic-inventory.md  # Phase 0: extracted BL units (rewrite mode)
├── data-model.md                # Phase 1: entity documentation
├── quickstart.md                # Phase 1: build/run instructions
├── contracts/                   # Phase 1: API surface documentation
│   └── api-surface.md
├── checkpoints/
│   └── spec-to-plan.yaml       # Traceability checkpoint
└── tasks.md                     # Phase 2 output (NOT created by plan)
```

### Source Code (target — Spring Boot application)

```text
src/
├── main/
│   ├── java/com/skishop/
│   │   ├── Application.java                    # Spring Boot entry point
│   │   ├── common/
│   │   │   ├── config/
│   │   │   │   └── AppConfig.java              # Spring @Configuration (property binding)
│   │   │   ├── util/
│   │   │   │   └── PasswordHasher.java         # Preserved unchanged
│   │   │   └── web/
│   │   │       ├── AuthInterceptor.java        # Replaces AuthRequestProcessor
│   │   │       ├── CsrfInterceptor.java        # CSRF token management
│   │   │       ├── WebMvcConfig.java            # @Configuration implementing WebMvcConfigurer
│   │   │       └── GlobalExceptionHandler.java  # @ControllerAdvice
│   │   ├── domain/                              # Preserved unchanged (POJOs)
│   │   │   ├── address/
│   │   │   ├── cart/
│   │   │   ├── coupon/
│   │   │   ├── inventory/
│   │   │   ├── mail/
│   │   │   ├── order/
│   │   │   ├── payment/
│   │   │   ├── point/
│   │   │   ├── product/
│   │   │   ├── shipping/
│   │   │   └── user/
│   │   ├── dao/                                 # @Repository + JdbcTemplate
│   │   │   ├── address/
│   │   │   ├── cart/
│   │   │   ├── category/
│   │   │   ├── coupon/
│   │   │   ├── inventory/
│   │   │   ├── mail/
│   │   │   ├── order/
│   │   │   ├── payment/
│   │   │   ├── point/
│   │   │   ├── product/
│   │   │   ├── shipping/
│   │   │   └── user/
│   │   ├── service/                             # @Service, business logic preserved
│   │   │   ├── address/
│   │   │   │   └── AddressService.java          # NEW: fix layering violation
│   │   │   ├── auth/
│   │   │   ├── cart/
│   │   │   ├── catalog/
│   │   │   ├── coupon/
│   │   │   ├── inventory/
│   │   │   ├── mail/
│   │   │   ├── order/
│   │   │   ├── payment/
│   │   │   ├── point/
│   │   │   ├── shipping/
│   │   │   ├── tax/
│   │   │   └── user/
│   │   └── web/
│   │       ├── controller/                      # @Controller classes
│   │       │   ├── HomeController.java
│   │       │   ├── LoginController.java
│   │       │   ├── RegisterController.java
│   │       │   ├── ProductController.java
│   │       │   ├── CartController.java
│   │       │   ├── CheckoutController.java
│   │       │   ├── CouponController.java
│   │       │   ├── OrderController.java
│   │       │   ├── PointController.java
│   │       │   ├── AddressController.java
│   │       │   ├── PasswordController.java
│   │       │   ├── LogoutController.java
│   │       │   └── admin/
│   │       │       ├── AdminProductController.java
│   │       │       ├── AdminOrderController.java
│   │       │       ├── AdminCouponController.java
│   │       │       └── AdminShippingController.java
│   │       ├── form/                            # Form-backing objects (no Struts dependency)
│   │       │   ├── LoginForm.java
│   │       │   ├── RegisterForm.java
│   │       │   ├── ProductSearchForm.java
│   │       │   ├── AddCartForm.java
│   │       │   ├── CheckoutForm.java
│   │       │   ├── CouponForm.java
│   │       │   ├── AddressForm.java
│   │       │   ├── PasswordResetRequestForm.java
│   │       │   ├── PasswordResetForm.java
│   │       │   └── admin/
│   │       │       ├── AdminProductForm.java
│   │       │       ├── AdminCouponForm.java
│   │       │       └── AdminShippingMethodForm.java
│   │       └── tag/
│   │           └── CsrfTokenTag.java            # Custom JSP tag for CSRF tokens
│   ├── resources/
│   │   ├── application.properties               # Spring Boot configuration
│   │   ├── messages.properties                  # Preserved
│   │   └── db/
│   │       ├── schema.sql                       # Preserved unchanged
│   │       └── data.sql                         # Preserved unchanged
│   └── webapp/
│       ├── assets/                              # Preserved unchanged
│       └── WEB-INF/
│           ├── jsp/
│           │   ├── layouts/
│           │   │   └── base.jsp                 # Layout template (replaces Tiles)
│           │   ├── common/
│           │   │   ├── header.jsp
│           │   │   ├── footer.jsp
│           │   │   └── messages.jsp
│           │   ├── auth/
│           │   ├── products/
│           │   ├── cart/
│           │   ├── orders/
│           │   ├── coupons/
│           │   ├── points/
│           │   ├── account/
│           │   ├── admin/
│           │   └── error.jsp
│           └── tags/
│               └── csrf.tld                     # CSRF token tag descriptor
└── test/
    └── java/com/skishop/
        ├── dao/                                 # @JdbcTest + H2
        ├── service/                             # @SpringBootTest
        └── web/controller/                      # @WebMvcTest + MockMvc
```

**Structure Decision**: Single-module web application. The existing monolithic structure is preserved with the same package hierarchy (`com.skishop`). Struts-specific packages (`web.action`, `web.processor`, `web.filter`, `web.tag`) are replaced with Spring equivalents (`web.controller`, `common.web`). The `common.dao` infrastructure (AbstractDao, DaoFactory, DataSourceLocator, ServiceLocator) is eliminated — Spring DI replaces all of it.

## Complexity Tracking

> No constitution violations requiring justification. All decisions align with Principles I–V.

| Decision | Justification | Simpler Alternative Rejected Because |
|----------|---------------|-------------------------------------|
| Custom CSRF (not Spring Security) | Preserves exact token behavior for api-test.sh compatibility | Spring Security CSRF uses different token names, headers, and error responses |
| HandlerInterceptor (not Spring Security) | Simple role-check from session; matches AuthRequestProcessor semantics exactly | Spring Security filter chain changes auth flow, redirect behavior, and error codes |
| JSP include layout (not Tiles 3) | Zero additional dependencies; same HTML composition result | Tiles 3 requires XML configuration and has Spring Boot integration complexity |

---

## Implementation Phases

### Phase 1: Project Foundation & Spring Boot Scaffolding
**Batch**: 1
**Goal**: Replace the Struts project infrastructure with Spring Boot; establish compile-pass baseline.
**Guideline skills**: `migrate-maven-dependencies`, `create-spring-boot-application`, `create-application-properties`, `migrate-web-xml`

| # | Item | Requirements | Description |
|---|------|-------------|-------------|
| 1.1 | Maven POM rewrite | REQ-001, REQ-002 | Replace Struts parent/dependencies with Spring Boot 3.2.x parent POM. Add spring-boot-starter-web, spring-boot-starter-jdbc, spring-boot-starter-mail, spring-boot-starter-validation, spring-boot-starter-test, postgresql:42.x, jakarta.servlet.jsp-api, glassfish-jstl. Add spring-boot-maven-plugin. Remove all Struts, commons-dbcp, commons-dbutils, log4j, strutstestcase dependencies. Set Java 21 compiler properties. |
| 1.2 | Spring Boot Application class | REQ-001, REQ-002 | Create `com.skishop.Application` with `@SpringBootApplication`. |
| 1.3 | application.properties | REQ-003, REQ-004, REQ-005, NFR-006 | Create Spring Boot configuration: `spring.datasource.*` from DB_HOST/PORT/NAME/USER/PASSWORD env vars, HikariCP pool settings, JSP view resolver (`spring.mvc.view.prefix=/WEB-INF/jsp/`, `spring.mvc.view.suffix=.jsp`), logging config (SLF4J+Logback). |
| 1.4 | Remove Struts infrastructure | REQ-009 | Delete: struts-config.xml, tiles-defs.xml, validation.xml, validator-rules.xml, web.xml, log4j.properties. Delete: ServiceLocator, DaoFactory, DataSourceLocator, DataSourceFactory, AbstractDao, DaoException, AppConfig (replaced by Spring property binding). |
| 1.5 | Domain objects preservation | REQ-005 | Retain all 23 domain POJOs unchanged. Update any `java.util.Date` imports if needed for compilation but preserve field types. |
| 1.6 | DAO interface preservation + JdbcTemplate impl | REQ-006, REQ-007, REQ-008 | Preserve all 20 DAO interfaces. Rewrite all 20 DAO implementations: replace `extends AbstractDao` + commons-dbutils with `@Repository` + `JdbcTemplate` injection. Preserve exact SQL queries. Use `BeanPropertyRowMapper` or custom `RowMapper` for result mapping. |
| 1.7 | Service layer Spring conversion | REQ-009 | Add `@Service` to all 16 service classes. Replace `new XxxDaoImpl()` direct instantiation with constructor injection of DAO interfaces. Replace `ServiceLocator` calls with `@Autowired`. Add `@Transactional` to service methods that coordinate multi-DAO operations. |
| 1.8 | Compile gate | NFR-001 | `mvn -B compile` must succeed with zero errors. All classes compile against Spring Boot dependencies. |

**Exit Criteria**: `mvn -B compile` passes. No Struts, commons-dbcp, commons-dbutils, or log4j references remain in production code.

---

### Phase 2: Cross-Cutting Infrastructure
**Batch**: 2
**Goal**: Implement authentication, CSRF, error handling, layout, and filter infrastructure before any controller.
**Guideline skills**: `convert-interceptor`, `create-exception-handler`

| # | Item | Requirements | Description |
|---|------|-------------|-------------|
| 2.1 | AuthInterceptor | REQ-011, REQ-013 | Create `AuthInterceptor` implementing `HandlerInterceptor`. In `preHandle`: check `HttpSession` for user attribute; compare user role against URL→role mapping (derived from struts-config.xml roles). Unauthenticated → redirect to `/login`. Wrong role → 403. |
| 2.2 | CsrfInterceptor | REQ-012 | Create `CsrfInterceptor`. On GET: generate token into session if missing. On POST: validate `_csrfToken` parameter against session; reject with 403 if mismatch; reset token after validation. |
| 2.3 | CsrfTokenTag (custom JSP tag) | REQ-012, REQ-020 | Create custom JSP tag that outputs a hidden input field with the session CSRF token. Register in TLD file. |
| 2.4 | WebMvcConfig | REQ-011, REQ-012, REQ-015 | Create `@Configuration` class implementing `WebMvcConfigurer`. Register AuthInterceptor and CsrfInterceptor with path patterns. Configure view resolver if needed beyond application.properties. |
| 2.5 | GlobalExceptionHandler | REQ-038 | Create `@ControllerAdvice` with `@ExceptionHandler` for Exception.class → error view. Map specific exceptions (IllegalArgumentException, IllegalStateException) to appropriate views. |
| 2.6 | JSP layout template | REQ-019 | Create `layouts/base.jsp` with JSP include pattern: header, messages, content (via `<jsp:include>`), footer. Create common JSP fragments (header.jsp, footer.jsp, messages.jsp). |
| 2.7 | RequestIdFilter conversion | NFR-003 | Convert `RequestIdFilter` to Spring `@Component` Filter or `FilterRegistrationBean`. Ensure UTF-8 encoding filter is configured. |
| 2.8 | MessageSource configuration | REQ-021 | Configure Spring `ReloadableResourceBundleMessageSource` to load `messages.properties`. |
| 2.9 | PasswordHasher preservation | REQ-014 | Retain `PasswordHasher` utility class unchanged. Ensure it compiles under JDK 21. |
| 2.10 | Compile + unit test gate | NFR-001 | `mvn -B compile` passes. Interceptor and exception handler unit tests pass. |

**Exit Criteria**: Auth + CSRF interceptors functional. Layout template renders. Error handler catches exceptions. `mvn -B compile` passes.

---

### Phase 3: P1 Features — Product Catalog, Auth, Cart & Checkout
**Batch**: 3
**Goal**: Implement all Priority 1 features: product browsing, authentication, cart, checkout.
**Guideline skills**: `convert-action-to-controller`, `convert-action-properties`, `convert-validation`, `convert-jsp-tags`, `convert-result-types`

| # | Item | Requirements | Description |
|---|------|-------------|-------------|
| 3.1 | HomeController | REQ-015 | `GET /home` → home view. Simple forward. |
| 3.2 | ProductController | REQ-010, REQ-015, REQ-016, REQ-022, REQ-023 | `GET /products` with page/size/keyword/categoryId params → product list view with pagination. `GET /product?id=` → product detail view or not-found view. Inject ProductService, CategoryService. |
| 3.3 | LoginController | REQ-010, REQ-013, REQ-015 | `GET /login` → login form. `POST /login` → authenticate via AuthService; on success set session user + redirect /home; on failure re-render with error. |
| 3.4 | RegisterController | REQ-010, REQ-015, REQ-017, REQ-018 | `GET /register` → register form. `POST /register` → validate RegisterForm (required fields, password match, unique email); on success create user + redirect /login; on failure re-render with errors. |
| 3.5 | LogoutController | REQ-010, REQ-015 | `GET /logout` → invalidate session + redirect /home. |
| 3.6 | CartController | REQ-010, REQ-015, REQ-024 | `GET /cart` → cart view (session-based). `POST /cart` → add item to cart. Manage cart via session attribute or CartService with session ID. |
| 3.7 | CouponController (apply) | REQ-010, REQ-015, REQ-025 | `POST /coupons/apply` → validate coupon code, apply discount to cart. `GET /coupons` → list active coupons (public). |
| 3.8 | CheckoutController | REQ-010, REQ-015, REQ-026, REQ-027, REQ-044 | `GET /checkout` → checkout form. `POST /checkout` → call OrderFacade.placeOrder with payment info, points; render confirmation or error. |
| 3.9 | Form objects (P1) | REQ-017, REQ-018 | Rewrite LoginForm, RegisterForm, ProductSearchForm, AddCartForm, CheckoutForm, CouponForm as plain POJOs with Bean Validation annotations. Remove ValidatorForm extends. |
| 3.10 | JSP views (P1) | REQ-019, REQ-020 | Migrate JSPs for: home, products/list, products/detail, auth/login, auth/register, cart/view, cart/checkout, cart/confirmation, coupons/available. Replace Struts tags with JSTL + Spring form tags. Apply layout template. |
| 3.11 | Compile + test gate | NFR-001, REQ-042 | `mvn -B compile` passes. Core user flows functional. |

**Exit Criteria**: Product browsing, authentication, cart, and checkout flows are functional end-to-end. All P1 endpoints respond correctly.

---

### Phase 4: P2 Features — Orders, Addresses, Points, Password Reset, Email
**Batch**: 4
**Goal**: Implement all Priority 2 features.

| # | Item | Requirements | Description |
|---|------|-------------|-------------|
| 4.1 | OrderController | REQ-010, REQ-015, REQ-028, REQ-029 | `GET /orders` → order history. `GET /orders/detail?id=` → order detail. `POST /orders/cancel` → cancel order. `POST /orders/return` → return request. All require USER/ADMIN auth. |
| 4.2 | AddressController | REQ-010, REQ-015, REQ-030 | `GET /account/addresses` → address list. `GET /account/addresses/edit` → address form. `POST /account/addresses/save` → validate + save. |
| 4.3 | AddressService (NEW) | REQ-009, REQ-030 | Create AddressService wrapping UserAddressDao. Fix AddressListAction's direct-DAO-access layering violation. |
| 4.4 | PointController | REQ-010, REQ-015, REQ-031 | `GET /points` → balance + transaction history. Requires auth. |
| 4.5 | PasswordController | REQ-010, REQ-015, REQ-032 | `GET /password/forgot` + `POST /password/forgot` → always show success. `GET /password/reset?token=` + `POST /password/reset` → validate token, reset password. |
| 4.6 | MailService Spring conversion | REQ-033 | Replace `javax.mail` direct API with Spring `JavaMailSender`. Preserve EmailQueue DB pattern. Configure via `spring.mail.*` properties. |
| 4.7 | Form objects (P2) | REQ-017, REQ-018 | Rewrite AddressForm, PasswordResetRequestForm, PasswordResetForm with Bean Validation. |
| 4.8 | JSP views (P2) | REQ-019, REQ-020 | Migrate JSPs for: orders/history, orders/detail, account/addresses, account/address_edit, points/balance, auth/password_forgot, auth/password_reset. |
| 4.9 | Compile + test gate | NFR-001 | `mvn -B compile` passes. P2 endpoints functional. |

**Exit Criteria**: All P2 endpoints respond correctly. Order lifecycle, address management, points, password reset flows are operational.

---

### Phase 5: P3 Features — Admin Management
**Batch**: 5
**Goal**: Implement all Priority 3 admin features.

| # | Item | Requirements | Description |
|---|------|-------------|-------------|
| 5.1 | AdminProductController | REQ-010, REQ-015, REQ-034 | `GET /admin/products` → product list. `GET/POST /admin/product/edit` → edit form. `POST /admin/product/delete` → delete. ADMIN role required. |
| 5.2 | AdminOrderController | REQ-010, REQ-015, REQ-035 | `GET /admin/orders` → all orders. `GET /admin/orders/detail` → detail. `POST /admin/order/update` → update status. `POST /admin/order/refund` → process refund. ADMIN role required. |
| 5.3 | AdminCouponController | REQ-010, REQ-015, REQ-036 | `GET /admin/coupons` → coupon list. `GET/POST /admin/coupon/edit` → create/edit. ADMIN role required. |
| 5.4 | AdminShippingController | REQ-010, REQ-015, REQ-037 | `GET /admin/shipping` → shipping method list. `GET/POST /admin/shipping/edit` → create/edit. ADMIN role required. |
| 5.5 | Admin form objects | REQ-017, REQ-018 | Rewrite AdminProductForm, AdminCouponForm, AdminShippingMethodForm with Bean Validation. |
| 5.6 | Admin JSP views | REQ-019, REQ-020 | Migrate admin JSPs: products/list, products/edit, orders/list, orders/detail, coupons/list, coupons/edit, shipping/list, shipping/edit. |
| 5.7 | Compile + test gate | NFR-001 | `mvn -B compile` passes. All admin endpoints functional. Non-admin access returns 403. |

**Exit Criteria**: All admin endpoints operational. Role enforcement verified (ADMIN only).

---

### Phase 6: Test Migration
**Batch**: 6
**Goal**: Migrate all unit tests to JUnit 5 + Spring Test.

| # | Item | Requirements | Description |
|---|------|-------------|-------------|
| 6.1 | Test infrastructure | REQ-039 | Create `DaoTestBase` equivalent using `@JdbcTest` + H2. Configure test `application-test.properties` with H2 datasource and schema.sql initialization. |
| 6.2 | DAO test migration (14 tests) | REQ-039 | Migrate all 14 DAO tests to JUnit 5 + `@JdbcTest`. Replace JUnit 4 `@Test` with JUnit 5 `@Test`. Use `@Sql` for test data setup. Preserve all SQL assertions. |
| 6.3 | Controller test rewrite (22 tests) | REQ-040 | Rewrite all 22 Struts Action tests as `@WebMvcTest` + `MockMvc` tests. For each test: mock service dependencies, perform request, assert HTTP status + view name + model attributes. |
| 6.4 | Service test migration (2 tests) | REQ-041 | Migrate OrderFacadeTest and ScenarioFlowTest to JUnit 5 + `@SpringBootTest` or plain unit tests with mock dependencies. |
| 6.5 | Full test suite gate | NFR-001 | `mvn -B test` passes all unit tests. |

**Exit Criteria**: `mvn -B test` passes with zero failures. All 38 test files migrated.

---

### Phase 7: Deployment & End-to-End Validation
**Batch**: 7
**Goal**: Update Docker configuration and pass the integration test suite.

| # | Item | Requirements | Description |
|---|------|-------------|-------------|
| 7.1 | Dockerfile rewrite | REQ-043 | Replace multi-stage build: Stage 1 = Maven 3.9 + JDK 21 (build JAR). Stage 2 = JDK 21 runtime (run JAR). No Tomcat needed (embedded). |
| 7.2 | docker-compose.yml update | REQ-043 | Update app service: build from new Dockerfile, expose 8080, pass DB + mail env vars. Preserve db service (postgres:9.2) unchanged. |
| 7.3 | Entrypoint script update | REQ-043 | Update `docker/entrypoint.sh` or replace with `java -jar` command in Dockerfile. |
| 7.4 | End-to-end validation | REQ-042, SC-002 | `docker compose up --build`, wait for app startup, run `./api-test.sh`. All ~50+ assertions must pass. |
| 7.5 | Final verification | SC-001 through SC-007 | Verify all 7 success criteria: mvn package (SC-001), api-test.sh (SC-002), all 28 endpoints (SC-003), 20 functional domains (SC-004), schema unchanged (SC-005), incremental compilation (SC-006), Docker deployment (SC-007). |

**Exit Criteria**: `api-test.sh` passes 100%. `docker compose up` builds and runs. All success criteria met.