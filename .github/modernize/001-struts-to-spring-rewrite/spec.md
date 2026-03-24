# Feature Specification: Struts 1.x to Spring Boot 3.2 MVC Rewrite

> Governed by: `.github/modernize/constitution.md`

**Feature Branch**: `001-struts-to-spring-rewrite`
**Created**: 2026-03-20
**Status**: Draft
**Mode**: REWRITE
**Input**: User description: "Rewrite the project from Struts to Spring MVC. Use all batch to do implementation. Test script is api-test.sh under root path of the project."

## Scope Baseline

- **Discovery method**: Knowledge graph analysis (134 Java types); struts-config.xml action-mappings (28 actions); tiles-defs.xml (24 tile definitions); pom.xml dependency tree; `api-test.sh` test script endpoint inventory
- **Total items discovered**: 134 Java types (114 classes, 20 interfaces), 28 Struts action mappings, 24 tile definitions, 38 test files, ~30 JSP views
- **Items in scope**: All 134 types, all 28 endpoints, all JSP views, all tests (full rewrite — no items excluded)

### Source Stack
| Component | Version |
|-----------|---------|
| Java | 1.5 |
| Struts | 1.3.10 |
| Servlet API | 2.5 |
| Tomcat | 6.0.53 |
| PostgreSQL Driver | 9.2-1004-jdbc3 |
| JUnit | 4.12 |
| log4j | 1.2.17 |

### Target Stack (per constitution)
| Component | Version |
|-----------|---------|
| Java | 21 (LTS) |
| Spring Boot | 3.2.x |
| Spring MVC | 6.1.x |
| Maven | 3.9.x |
| PostgreSQL Driver | 42.x |
| JUnit | 5.x + Spring Test |
| SLF4J + Logback | Spring Boot defaults |

---

## User Scenarios & Testing

### User Story 1 — Browse and Search Products (Priority: P1)

A visitor arrives at the SkiShop homepage and browses the product catalog. They can view all products, search by keyword, filter by category (Ski, Boot, Wear, Helmet, Glove, Pole, Wax), paginate through results, and view product detail pages. This is the primary discovery flow and requires no authentication.

**Why this priority**: Product browsing is the core storefront experience. Without it, no commerce can occur.

**Independent Test**: Navigate to `/products`, search for "Atomic", filter by category `c-1`, paginate to page 2, and view product detail for PSK001.

**Acceptance Scenarios**:

1. **Given** the application is running with seed data, **When** a visitor requests `GET /products`, **Then** a paginated product list is rendered with HTTP 200.
2. **Given** seed products exist, **When** a visitor requests `GET /products?keyword=Atomic`, **Then** the response contains matching products with "Atomic" in their name.
3. **Given** product PSK001 exists, **When** a visitor requests `GET /product?id=PSK001`, **Then** the product detail page renders showing "Atomic Redster".
4. **Given** a non-existent product ID, **When** a visitor requests `GET /product?id=NONEXISTENT`, **Then** a not-found page is rendered (HTTP 200 with error content).

---

### User Story 2 — User Registration and Authentication (Priority: P1)

A new visitor registers an account, logs in, and logs out. The system validates registration inputs (required fields, password confirmation match, duplicate email), authenticates credentials, establishes a session, and allows logout.

**Why this priority**: Authentication gates all protected functionality (orders, addresses, points, admin).

**Independent Test**: Register a new user, login with their credentials, verify session is established, logout and verify session is destroyed.

**Acceptance Scenarios**:

1. **Given** the login page loads, **When** a user submits valid credentials, **Then** a session is created and the user is redirected to the home page.
2. **Given** the login page loads, **When** a user submits invalid credentials, **Then** the login page redisplays with an error message.
3. **Given** the registration page loads, **When** a new user submits valid registration data with unique email, **Then** the account is created and the user is redirected to the login page.
4. **Given** a user submits registration with mismatched passwords, **When** the form is submitted, **Then** validation errors are displayed.
5. **Given** an authenticated user session exists, **When** the user requests `/logout`, **Then** the session is invalidated and the user is redirected to the home page.

---

### User Story 3 — Shopping Cart and Checkout (Priority: P1)

An authenticated user adds products to their session-based cart, views the cart, applies a coupon code, proceeds to checkout with payment details, and receives order confirmation. The checkout process coordinates inventory, payment, points, shipping, and email notification.

**Why this priority**: Checkout is the core revenue path — the most complex transactional flow.

**Independent Test**: View cart, apply coupon SAVE10, proceed to checkout with credit card details, verify order confirmation.

**Acceptance Scenarios**:

1. **Given** a visitor (no login required), **When** they request `GET /cart`, **Then** the cart view page renders (empty or with items).
2. **Given** an authenticated user with an active session, **When** they apply coupon code "SAVE10" via `POST /coupons/apply`, **Then** the coupon discount is applied to the cart.
3. **Given** an authenticated user at checkout, **When** they submit valid payment details via `POST /checkout`, **Then** an order is created, payment processed, and confirmation page displayed.
4. **Given** an invalid coupon code, **When** submitted via `POST /coupons/apply`, **Then** an error message is displayed and the cart is unchanged.

---

### User Story 4 — Order Management (Priority: P2)

An authenticated user views their order history, views order details, cancels a pending order, or requests a return. Each action requires the user to own the order. Admin users can manage all orders.

**Why this priority**: Order lifecycle management is essential for post-purchase experience.

**Independent Test**: Login, view `/orders` (order history), attempt to cancel/return an order.

**Acceptance Scenarios**:

1. **Given** an authenticated user, **When** they request `GET /orders`, **Then** their order history is displayed.
2. **Given** an unauthenticated visitor, **When** they request `GET /orders`, **Then** they are redirected to the login page.
3. **Given** an authenticated user with order `order-1`, **When** they request cancellation, **Then** the order status is updated and they are redirected to order history.

---

### User Story 5 — Address Management (Priority: P2)

An authenticated user manages their shipping addresses — listing, adding new addresses with validation (required fields: label, recipient, postal code, prefecture, address line), and setting a default.

**Why this priority**: Addresses are required for order fulfillment.

**Independent Test**: Login, navigate to `/account/addresses`, add a new address with valid data, verify validation errors on empty fields.

**Acceptance Scenarios**:

1. **Given** an authenticated user, **When** they request `GET /account/addresses`, **Then** their address list is displayed.
2. **Given** the address edit form, **When** valid address data is submitted, **Then** the address is saved and the user is redirected to the address list.
3. **Given** the address edit form, **When** required fields are blank, **Then** validation errors are displayed.

---

### User Story 6 — Password Reset Flow (Priority: P2)

A user who forgot their password can request a reset email by providing their email address. They receive a token-based link and can set a new password. The system prevents user enumeration — always showing a success message regardless of whether the email exists.

**Why this priority**: Essential for user self-service and security.

**Independent Test**: Submit forgot-password form with existing email, submit with non-existing email (both show success), attempt reset with invalid token.

**Acceptance Scenarios**:

1. **Given** the forgot password page, **When** a user submits an existing email, **Then** a success message is shown (reset email queued).
2. **Given** the forgot password page, **When** a user submits a non-existing email, **Then** a success message is shown (prevents enumeration).
3. **Given** an invalid/expired reset token, **When** the user submits a new password, **Then** an error message about the invalid token is displayed.

---

### User Story 7 — Points/Loyalty Balance (Priority: P2)

An authenticated user views their loyalty point balance and transaction history.

**Why this priority**: Part of the commerce experience; displayed during checkout.

**Independent Test**: Login, navigate to `/points`, verify balance page renders.

**Acceptance Scenarios**:

1. **Given** an authenticated user, **When** they request `GET /points`, **Then** their point balance and transactions are displayed.
2. **Given** an unauthenticated visitor, **When** they request `GET /points`, **Then** they are redirected to the login page.

---

### User Story 8 — Coupon Browsing (Priority: P2)

Any visitor can view the list of available (active) coupons.

**Why this priority**: Drives engagement and conversion.

**Independent Test**: Request `GET /coupons`, verify coupon list renders.

**Acceptance Scenarios**:

1. **Given** active coupons exist in the database, **When** a visitor requests `GET /coupons`, **Then** the available coupons are listed.

---

### User Story 9 — Admin Product Management (Priority: P3)

An admin user manages the product catalog — listing products, editing product details (with form validation), and deleting products. Non-admin users are denied access.

**Why this priority**: Admin functionality supports operations but is secondary to customer-facing features.

**Independent Test**: Access `/admin/products` as admin (should render), access as non-admin (should be denied).

**Acceptance Scenarios**:

1. **Given** an admin user is authenticated, **When** they request `GET /admin/products`, **Then** the product management list is displayed.
2. **Given** a non-admin user, **When** they request `GET /admin/products`, **Then** they receive a 403 Forbidden or are redirected to login.
3. **Given** an admin user editing a product, **When** valid product data is submitted, **Then** the product is updated and admin is redirected to the product list.

---

### User Story 10 — Admin Order Management (Priority: P3)

An admin user views all orders, views order details, updates order status, and processes refunds.

**Why this priority**: Operational support for order lifecycle.

**Acceptance Scenarios**:

1. **Given** an admin user, **When** they request `GET /admin/orders`, **Then** all orders are listed.
2. **Given** an admin viewing an order, **When** they process a refund, **Then** payment is reversed and order status updated.

---

### User Story 11 — Admin Coupon and Shipping Management (Priority: P3)

An admin user manages coupons (list, create, edit) and shipping methods (list, create, edit).

**Why this priority**: Configuration management for commerce operations.

**Acceptance Scenarios**:

1. **Given** an admin user, **When** they request `GET /admin/coupons`, **Then** the coupon management list is displayed.
2. **Given** an admin user, **When** they request `GET /admin/shipping`, **Then** the shipping method list is displayed.

---

### Edge Cases

- What happens when a product ID parameter is empty (`/product?id=`)? → Should display error/not-found page, not crash.
- What happens when pagination page number exceeds available pages (`/products?page=99999`)? → Should display empty product list, HTTP 200.
- What happens when `size=0` is passed in pagination? → Should handle gracefully (default or error).
- What happens when an unauthenticated user tries to cancel/return a non-existent order? → Should redirect to login or return 200/302/403.
- What happens when a user submits a form without the CSRF token? → Should reject the request.
- What happens when requesting a non-existent URL path? → Should return 404 or a friendly error page.

---

## Requirements

### Functional Requirements

#### Spring Boot Foundation

- **REQ-001**: System MUST be built on Spring Boot 3.2.x with Spring MVC, targeting JDK 21 and Maven as the build tool. _(Constitution Principle V)_
- **REQ-002**: System MUST produce a runnable artifact via `mvn -B clean package` that starts an embedded web server without requiring an external application server. _(Constitution: Development Workflow)_
- **REQ-003**: System MUST use Spring Boot's native property resolution to load application configuration, supporting `${TOKEN}` placeholder substitution and environment variable overrides for DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, and mail settings. _(Constitution: Migration Constraints — Configuration)_
- **REQ-004**: System MUST use SLF4J + Logback (Spring Boot defaults) for all logging, replacing log4j 1.2. _(Constitution Principle V)_

#### Database and Data Access

- **REQ-005**: System MUST connect to the existing PostgreSQL database using Spring Boot auto-configured DataSource (HikariCP) without modifying the database schema (`schema.sql`) or seed data (`data.sql`). _(Constitution Principle III — NON-NEGOTIABLE)_
- **REQ-006**: System MUST use Spring JDBC (`JdbcTemplate`) for all data access, preserving existing SQL queries from the 20 DAO implementations. _(Constitution Principle V)_
- **REQ-007**: All 20 DAO interface contracts MUST be preserved. Each DAO implementation MUST be converted to a Spring `@Repository` component using `JdbcTemplate` with the same query semantics. _(Constitution Principle II)_
- **REQ-008**: The `AbstractDao` base class with manual connection management MUST be replaced by Spring-managed `JdbcTemplate` injection. SQL queries MUST produce identical results for identical inputs. _(Constitution Principle III)_

#### Dependency Injection

- **REQ-009**: The manual `ServiceLocator` and `DaoFactory` patterns MUST be replaced with Spring's `@Autowired` dependency injection. All services MUST be annotated as `@Service` and all DAOs as `@Repository`. _(Constitution Principle II)_
- **REQ-010**: All 24 Struts Action classes MUST be converted to Spring `@Controller` classes with `@RequestMapping` methods. Controllers MUST contain only HTTP request/response mapping — no business logic. _(Constitution Principle II)_

#### Authentication and Security

- **REQ-011**: The `AuthRequestProcessor` role-based access control MUST be replicated. Protected endpoints (`roles="USER,ADMIN"` and `roles="ADMIN"`) MUST enforce authentication and authorization with equivalent behavior — unauthenticated users redirected to `/login`, unauthorized users receiving 403 or redirect. _(Constitution: Migration Constraints — Authentication)_
- **REQ-012**: CSRF protection MUST be implemented. Forms MUST include a `_csrfToken` hidden field and the server MUST validate it on form submission. _(Constitution Principle I)_
- **REQ-013**: Session-based authentication MUST be preserved. User credentials (email + password) are validated against the database, and a session is established on successful login. _(Constitution Principle I)_
- **REQ-014**: Password hashing using the existing `PasswordHasher` utility MUST be preserved to maintain compatibility with existing user credentials in the database. _(Constitution Principle III)_

#### URL Mapping and Routing

- **REQ-015**: All endpoints MUST be accessible via clean URL paths as specified in `api-test.sh` — without the `.do` suffix. The following path mappings MUST be implemented: `/home`, `/login`, `/register`, `/products`, `/product`, `/cart`, `/checkout`, `/orders`, `/points`, `/coupons`, `/coupons/apply`, `/account/addresses`, `/account/addresses/edit`, `/account/addresses/save`, `/password/forgot`, `/password/reset`, `/logout`, and all `/admin/*` paths. _(Constitution Principle I — NON-NEGOTIABLE)_
- **REQ-016**: Product search MUST support query parameters: `page` (pagination), `size` (page size), `keyword` (text search), and `categoryId` (category filter). _(Constitution Principle I)_

#### Form Validation

- **REQ-017**: All 12 Struts form beans MUST be converted to Spring form-backing objects. Validation rules from `validation.xml` and custom `validate()` methods MUST be replicated using Spring Validation (Bean Validation annotations and/or programmatic `Validator`). _(Constitution: Migration Constraints — Form Validation)_
- **REQ-018**: Validation errors MUST be displayed on the same form page (re-render with error messages), matching the existing Struts `input` forward behavior. _(Constitution Principle I)_

#### View Layer

- **REQ-019**: System MUST use JSP views with JSTL for the view layer, matching the existing Tiles-based page composition. Each page MUST be rendered within a base layout (header, messages, body, footer) producing equivalent HTML structure. _(Constitution Principle V, Migration Constraints — Tiles Layout)_
- **REQ-020**: All existing JSP views (~30 files) MUST be migrated. Struts tag libraries (`<html:form>`, `<bean:write>`, `<logic:iterate>`, etc.) MUST be replaced with JSTL and Spring form tags. _(Constitution Principle I)_
- **REQ-021**: The messages/internationalization system (`messages.properties`) MUST continue to work, using Spring's `MessageSource` for resolving message keys. _(Constitution Principle I)_

#### Product Catalog

- **REQ-022**: Product listing MUST support pagination, keyword search, and category filtering with the same query parameter names (`page`, `size`, `keyword`, `categoryId`). _(Constitution Principle I)_
- **REQ-023**: Product detail page MUST display product information including name, description, price, and category. For non-existent product IDs, a not-found view MUST be rendered. _(Constitution Principle I)_

#### Shopping Cart

- **REQ-024**: Shopping cart MUST be session-based, allowing visitors (no login required) to view their cart. Cart items MUST persist across requests within the same session. _(Constitution Principle I)_
- **REQ-025**: Coupon application MUST validate coupon codes against the database, apply discounts to the cart for valid codes, and display error messages for invalid codes. Coupon application requires authentication. _(Constitution Principle I)_

#### Checkout and Order Processing

- **REQ-026**: The checkout flow MUST coordinate inventory verification, tax calculation, order creation, payment processing, point accrual, shipping assignment, coupon usage tracking, and email notification — replicating the `OrderFacadeImpl` orchestration. _(Constitution Principle I)_
- **REQ-027**: Checkout MUST require authentication (USER or ADMIN role). Payment MUST accept credit card details (card number, expiration, CVV, billing zip). _(Constitution Principle I)_

#### Order Lifecycle

- **REQ-028**: Order history MUST be viewable by authenticated users at `/orders`. Order detail MUST be viewable at `/orders/detail`. _(Constitution Principle I)_
- **REQ-029**: Order cancellation and return requests MUST be supported for authenticated users via POST endpoints. _(Constitution Principle I)_

#### User Account Features

- **REQ-030**: Address management (list, add, edit) MUST be accessible to authenticated users at `/account/addresses`. Address form MUST validate required fields (label, recipientName, postalCode, prefecture, address1). _(Constitution Principle I)_
- **REQ-031**: Point balance view MUST be accessible to authenticated users at `/points`, displaying current balance and transaction history. _(Constitution Principle I)_
- **REQ-032**: Password reset flow MUST support: forgot request (email submission), token generation, and password reset with token validation. The system MUST NOT reveal whether an email exists (prevent user enumeration). _(Constitution Principle I)_

#### Email Service

- **REQ-033**: SMTP-based email processing MUST be preserved using Spring's `JavaMailSender`, replacing the direct `javax.mail` API usage. Email queue processing (order confirmations, password reset emails) MUST produce equivalent behavior. _(Constitution: Migration Constraints — Email)_

#### Admin Features

- **REQ-034**: Admin product management (list, create/edit, delete) MUST be accessible only to ADMIN-role users at `/admin/products`, `/admin/product/edit`, `/admin/product/delete`. _(Constitution Principle I)_
- **REQ-035**: Admin order management (list, detail, update status, refund) MUST be accessible only to ADMIN-role users at `/admin/orders`, `/admin/orders/detail`, `/admin/order/update`, `/admin/order/refund`. _(Constitution Principle I)_
- **REQ-036**: Admin coupon management (list, create/edit) MUST be accessible only to ADMIN-role users at `/admin/coupons`, `/admin/coupon/edit`. _(Constitution Principle I)_
- **REQ-037**: Admin shipping method management (list, create/edit) MUST be accessible only to ADMIN-role users at `/admin/shipping`, `/admin/shipping/edit`. _(Constitution Principle I)_

#### Error Handling

- **REQ-038**: Global exception handling MUST be implemented via `@ControllerAdvice` / `@ExceptionHandler`, replicating the Struts `global-exceptions` behavior that routes unhandled exceptions to an error page. _(Constitution: Migration Constraints — Error Handling)_

#### Tax Calculation

- **REQ-044**: Tax calculation MUST be preserved as a service layer component (`TaxService`), replicating the existing tax computation logic used during checkout. Tax amounts MUST be calculated identically for identical order inputs. _(Constitution Principle I)_

#### Testing

- **REQ-039**: All 14 DAO unit tests MUST be migrated to JUnit 5 with Spring Test (`@JdbcTest` or equivalent). SQL test assertions MUST remain unchanged. _(Constitution Principle IV)_
- **REQ-040**: All 22 Struts Action tests MUST be rewritten as Spring MVC tests using `MockMvc`. Test scenarios MUST verify equivalent behavior (HTTP status, view names, model attributes). _(Constitution Principle IV)_
- **REQ-041**: The 2 service tests (`OrderFacadeTest`, `ScenarioFlowTest`) MUST be migrated to JUnit 5 with Spring Test context. _(Constitution Principle IV)_
- **REQ-042**: The `api-test.sh` integration test script MUST pass against the rewritten application without modification to its assertions. _(Constitution Principle I — NON-NEGOTIABLE)_

#### Deployment

- **REQ-043**: The Dockerfile and docker-compose.yml MUST be updated to build and run the Spring Boot application with JDK 21, while preserving the PostgreSQL 9.2+ database service and environment variable configuration. _(Constitution Principle V)_

### Non-Functional Requirements

- **NFR-001**: The application MUST compile and pass all unit tests via `mvn -B clean package`. _(Constitution Principle IV)_
- **NFR-002**: The application MUST start and serve requests within 30 seconds on a standard development machine. _(Reasonable default for Spring Boot)_
- **NFR-003**: All user-facing HTTP responses MUST use `UTF-8` encoding. _(Preserves existing encoding behavior from web.xml)_
- **NFR-004**: The application MUST NOT introduce JPA/Hibernate, Thymeleaf, microservices, message queues, caching layers, or other infrastructure not present in the original application. _(Constitution Principle V)_
- **NFR-005**: The application MUST maintain the single deployable artifact model (JAR or WAR). _(Constitution Principle V)_
- **NFR-006**: Database connection pooling MUST support configuration via environment variables (DB_POOL_MAX_ACTIVE, DB_POOL_MAX_IDLE, DB_POOL_MAX_WAIT) for operational continuity with existing deployment. _(Constitution: Development Workflow)_

### Key Entities

1. **Order** — Central business entity; tracks customer purchases with status lifecycle (PENDING → CONFIRMED → SHIPPED → DELIVERED / CANCELLED / RETURNED)
2. **User** — User account with email, username, hashed password with salt, and role-based access control (USER / ADMIN)
3. **Product** — Ski equipment catalog item with name, description, image, category reference, and associated Price records
4. **Cart / CartItem** — Session-based shopping cart header and line items; cart links to user, items link to products with quantity
5. **Payment** — Payment transaction record linked to orders; captures method, amount, status, and transaction date
6. **Coupon / CouponUsage** — Discount codes with type (percentage/fixed), validity dates, and per-user usage tracking
7. **PointAccount / PointTransaction** — Loyalty program balance per user with earn/redeem transaction history
8. **Address** — User-managed shipping/billing addresses with label, recipient, postal code, prefecture, and address lines

---

## Success Criteria

### Measurable Outcomes

- **SC-001**: `mvn -B clean package` produces a runnable artifact with zero compilation errors and all unit tests passing.
- **SC-002**: The `api-test.sh` integration test script achieves 100% pass rate (all ~50+ test assertions pass) against the rewritten application — matching the same pass rate as the original Struts application.
- **SC-003**: All 28 Struts action endpoints are accessible via the clean URL paths documented in `api-test.sh`, returning equivalent HTTP status codes and HTML content.
- **SC-004**: All 20 functional domains (Authentication through Admin Shipping Management) produce identical observable behavior for identical user inputs.
- **SC-005**: The PostgreSQL database schema and seed data remain unmodified — the rewritten application works against the same database state.
- **SC-006**: Each implementation batch compiles independently and passes all applicable tests before proceeding to the next batch.
- **SC-007**: The Docker-based deployment (`docker-compose up`) successfully builds and runs the complete application stack (app + PostgreSQL) with the rewritten codebase.
