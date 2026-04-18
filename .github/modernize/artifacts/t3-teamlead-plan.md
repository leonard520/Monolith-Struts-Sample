# Implementation Plan: SkiShop Struts 1.3 → Spring Boot 3.2 Rewrite

**Date**: 2026-04-18 | **Constitution**: t1-teamlead-constitution.md | **Architecture**: t2-architect-target-architecture.md

## Summary

Rewrite the SkiShop monolith from Struts 1.3 / Java 5 / WAR to Spring Boot 3.2 / Java 21 / executable JAR. JSP views are kept but migrated from Struts/Tiles tags to JSTL + Spring form tags with `<jsp:include>` layout. The new project lives in `monolith-new/`. The `api-test.sh` script (55 test cases) is the acceptance oracle.

**Approach**: Build a new Spring Boot project in `monolith-new/`, port layers bottom-up (domain → DAO → service → controller → JSP), organized by **vertical business module** (not horizontal layer). Each task is independently executable by one agent in one session.

## Technical Context

**Language/Version**: Java 21 (LTS)
**Framework**: Spring Boot 3.2.5 (Spring MVC + Spring Security 6.x)
**Primary Dependencies**: spring-boot-starter-web, spring-boot-starter-security, spring-boot-starter-validation, spring-boot-starter-jdbc, tomcat-embed-jasper, JSTL 3.0, H2, PostgreSQL
**Storage**: H2 in-memory (test profile) / PostgreSQL 16 (prod profile), JdbcTemplate (no JPA)
**View**: JSP + JSTL + Spring form tags, `<jsp:include>` layout (replaces Tiles)
**Testing**: `api-test.sh` (55 HTTP test cases covering all endpoints), MockMvc integration tests
**Target Platform**: Executable JAR (embedded Tomcat)
**Constraints**: No source modification (`src/` is read-only). URL parity with test script. CSRF field `_csrfToken`. Behavioral fidelity (no feature additions).

## Constitution Check

| # | Principle | Status | Notes |
|---|-----------|--------|-------|
| I | Functional Equivalence | ✅ PASS | Every URL from test script mapped to controller methods. api-test.sh is acceptance oracle. |
| II | Keep the JSPs | ✅ PASS | JSPs migrated to JSTL/Spring tags, kept under `src/main/webapp/WEB-INF/jsp/`. No Thymeleaf. |
| III | New Project, Clean Separation | ✅ PASS | All work in `monolith-new/`. Original `src/` untouched. Artifact name `skishop-app-2.0.0.jar`. |
| IV | Preserve Data Model | ✅ PASS | Same schema.sql/data.sql. JdbcTemplate only. VARCHAR(36) UUID keys preserved. |
| V | Security Parity | ✅ PASS | Spring Security for URL auth only. Manual login in AuthController. PasswordHasher copied verbatim. CSRF `_csrfToken`. |
| VI | Build and Run Contract | ✅ PASS | `mvn clean package -DskipTests`, `java -jar ... --spring.profiles.active=h2`, port 8080. |
| VII | No Over-Engineering | ✅ PASS | No JPA, no Lombok, no MapStruct, no Swagger, no microservices. JdbcTemplate + POJOs. |

## Applied Guidelines

- **Guideline**: `struts-to-spring` (skills/guidelines/struts-to-spring/)
- **Applicable rules**: Action→Controller conversion, JSP tag mapping (Struts→JSTL/Spring), validation migration (Struts Validator→Jakarta Validation), web.xml→Spring Boot auto-config
- **Note**: Source is Struts **1.3** (ActionForm/ActionMapping), not Struts 2 (ActionSupport/OGNL). Guidelines reference Struts 2 patterns but the conversion principles apply. Key differences: Struts 1 uses `<html:form>`, `<bean:write>`, `<logic:iterate>` tags (not `<s:property>`, `<s:form>`).

## Implementation Steps

### Phase 1: Project Scaffold & Configuration [REQ: Build/Run Contract]

**1.1** Create `monolith-new/` Maven project with Spring Boot 3.2.5 parent, Java 21, all dependencies per architecture design (spring-boot-starter-web, -security, -validation, -jdbc, tomcat-embed-jasper, JSTL, H2, PostgreSQL).

**1.2** Create `Application.java` (`@SpringBootApplication`), `application.properties` (port 8080, JSP view resolver, session timeout, SQL init), `application-h2.properties`, `application-postgresql.properties`.

**1.3** Create Spring configuration classes: `DataSourceConfig.java`, `WebMvcConfig.java` (view resolver, static resource handler `/assets/**`), `SecurityConfig.java` (URL authorization, CSRF with `_csrfToken`, formLogin disabled, manual entry point), `CsrfTokenConfig.java`.

**1.4** Copy static resources: `schema.sql`, `data.sql` from source `db/`, `messages.properties`, `assets/` directory (CSS, JS, images).

**1.5** Create common layout JSPs: `layouts/base.jsp` (placeholder), `common/header.jsp`, `common/footer.jsp`, `common/messages.jsp`, `error.jsp`, `index.jsp` (redirect to /home). Migrate Struts/Tiles tags to JSTL + EL.

**1.6** Create `GlobalExceptionHandler.java` (`@ControllerAdvice`) and `RequestIdInterceptor.java`.

**1.7** Copy `PasswordHasher.java` verbatim from source (SHA-256 + salt, 1000 iterations).

**Verification**: `mvn clean package -DskipTests` succeeds, `java -jar` starts, GET `/` returns HTTP 200.

### Phase 2: Domain & Common Layer [REQ: Data Model Preservation]

**2.1** Copy all 24 domain POJOs from source, update package declarations. Remove any Struts/legacy imports. Organize into sub-packages: `domain/user/`, `domain/product/`, `domain/cart/`, `domain/order/`, `domain/payment/`, `domain/coupon/`, `domain/inventory/`, `domain/shipping/`, `domain/address/`, `domain/point/`, `domain/mail/`.

**2.2** Create `AppConfig.java` (Spring `@Configuration`, reads `app.properties` values via `@Value`).

### Phase 3: DAO Layer — JdbcTemplate Migration [REQ: Data Model, Functional Equivalence]

Port all 19 DAO interface+impl pairs from Commons DBUtils to Spring JdbcTemplate. Each DAO becomes `@Repository` with constructor-injected `JdbcTemplate`. SQL statements preserved verbatim. RowMapper lambdas replace BeanHandler/BeanListHandler.

**3.1** User domain DAOs: `UserDao/UserDaoImpl`, `SecurityLogDao/SecurityLogDaoImpl`, `PasswordResetTokenDao/PasswordResetTokenDaoImpl`

**3.2** Product domain DAOs: `ProductDao/ProductDaoImpl`, `PriceDao/PriceDaoImpl`, `CategoryDao/CategoryDaoImpl`

**3.3** Cart domain DAOs: `CartDao/CartDaoImpl`, `CartItemDao/CartItemDaoImpl` (note: CartDaoImpl uses `CartItem` inner queries)

**3.4** Order domain DAOs: `OrderDao/OrderDaoImpl`, `OrderShippingDao/OrderShippingDaoImpl`, `ReturnDao/ReturnDaoImpl`

**3.5** Payment & Inventory DAOs: `PaymentDao/PaymentDaoImpl`, `InventoryDao/InventoryDaoImpl` (inventory uses `@Transactional` for `FOR UPDATE` locking)

**3.6** Coupon & Marketing DAOs: `CouponDao/CouponDaoImpl`, `CouponUsageDao/CouponUsageDaoImpl`

**3.7** Supporting DAOs: `ShippingMethodDao/ShippingMethodDaoImpl`, `UserAddressDao/UserAddressDaoImpl`, `PointAccountDao/PointAccountDaoImpl`, `PointTransactionDao/PointTransactionDaoImpl`, `EmailQueueDao/EmailQueueDaoImpl`

### Phase 4: Service Layer — Spring DI Migration [REQ: Functional Equivalence, Business Logic]

Port all services from `ServiceLocator`/`new DaoImpl()` to Spring `@Service` with constructor injection. Business logic stays identical.

**4.1** Auth services: `AuthService.java` (authenticate, security logging, account locking), `AuthResult.java`

**4.2** User & catalog services: `UserService.java` (register, find, update), `ProductService.java` (list with pagination/search/filter, findById), `CategoryService.java`

**4.3** Cart & coupon services: `CartService.java` (create, add item, get, clear), `CouponService.java` (validate, apply, mark used)

**4.4** Order orchestration: `OrderService.java` (create, find, cancel, return), `OrderFacade.java` interface, `OrderFacadeImpl.java` (18-step placeOrder with compensating actions — NO `@Transactional`)

**4.5** Payment, inventory, shipping, tax: `PaymentService.java` (Luhn validation, authorize, void), `InventoryService.java` (`@Transactional` reserveItems/releaseItems with `FOR UPDATE`), `ShippingService.java`, `TaxService.java` (10% flat)

**4.6** Points & mail: `PointService.java` (`@Transactional` expirePoints, earn/redeem/refund), `MailService.java` (queue email)

### Phase 5: Auth & Home Module [REQ: Security Parity, URL Contract — Tests 1, 3, 4, 5, 6, 10]

**5.1** Forms: `LoginForm.java`, `RegisterForm.java`, `PasswordResetRequestForm.java`, `PasswordResetForm.java` (Jakarta Validation annotations)

**5.2** `AuthController.java`: GET/POST /login, GET /logout, GET/POST /register, GET/POST /password/forgot, GET/POST /password/reset. Manual authentication → SecurityContext. Session fixation prevention.

**5.3** `HomeController.java`: GET `/`, GET `/home` → "home" view.

**5.4** JSP views: `auth/login.jsp`, `auth/register.jsp`, `auth/password/forgot.jsp`, `auth/password/forgot_complete.jsp`, `auth/password/reset.jsp`, `home.jsp`. Migrate Struts tags → JSTL/Spring form tags. Include `_csrfToken` hidden field in all POST forms.

### Phase 6: Product Catalog Module [REQ: URL Contract — Tests 2, 13]

**6.1** `ProductController.java`: GET `/products` (keyword, categoryId, page, size, sort params), GET `/product?id=` → detail or notfound view.

**6.2** `ProductSearchForm.java` (search/filter params).

**6.3** JSP views: `products/list.jsp` (pagination, search, category filter), `products/detail.jsp`, `products/notfound.jsp`. Migrate tags.

### Phase 7: Cart & Coupons Module [REQ: URL Contract — Tests 7, 8]

**7.1** Forms: `AddCartForm.java`, `CouponForm.java`.

**7.2** `CartController.java`: GET `/cart`, POST `/cart` (add item). Session + cookie (`CART_ID`, 30-day, HttpOnly) management.

**7.3** `CouponController.java`: GET `/coupons`, POST `/coupons/apply`.

**7.4** JSP views: `cart/view.jsp` (with CSRF for add-to-cart + coupon apply), `coupons/available.jsp`. Migrate tags.

### Phase 8: Checkout & Orders Module [REQ: URL Contract — Tests 9, 11.7, 12]

**8.1** Forms: `CheckoutForm.java` (payment fields, Jakarta Validation).

**8.2** `CheckoutController.java`: GET `/checkout`, POST `/checkout` → OrderFacade.placeOrder → confirmation or error. JSP views: `cart/checkout.jsp`, `cart/confirmation.jsp`.

**8.3** `OrderController.java`: GET `/orders` (history), GET `/orders/detail?id=`, POST `/orders/{id}/cancel`, POST `/orders/{id}/return`. JSP views: `orders/history.jsp`, `orders/detail.jsp`.

### Phase 9: Account, Points & Address Module [REQ: URL Contract — Tests 11.1–11.6]

**9.1** Forms: `AddressForm.java` (Jakarta Validation).

**9.2** `AddressController.java`: GET `/account/addresses`, GET `/account/addresses/edit`, POST `/account/addresses/save`. JSP views: `account/addresses.jsp`, `account/address_edit.jsp`.

**9.3** `PointController.java`: GET `/points`. JSP view: `points/balance.jsp`.

### Phase 10: Admin Module [REQ: URL Contract — Tests 10.3–10.6]

**10.1** Admin forms: `AdminProductForm.java`, `AdminCouponForm.java`, `AdminShippingMethodForm.java`.

**10.2** `AdminProductController.java`: GET `/admin/products`, GET `/admin/product/edit`, POST `/admin/product/edit`, POST `/admin/product/delete`. JSP views: `admin/products/list.jsp`, `admin/products/edit.jsp`.

**10.3** `AdminOrderController.java`: GET `/admin/orders`, GET `/admin/orders/detail`, POST `/admin/order/update`, POST `/admin/order/refund`. JSP views: `admin/orders/list.jsp`, `admin/orders/detail.jsp`.

**10.4** `AdminCouponController.java`: GET `/admin/coupons`, GET `/admin/coupon/edit`, POST `/admin/coupon/edit`. JSP views: `admin/coupons/list.jsp`, `admin/coupons/edit.jsp`.

**10.5** `AdminShippingController.java`: GET `/admin/shipping`, GET `/admin/shipping/edit`, POST `/admin/shipping/edit`. JSP views: `admin/shipping/list.jsp`, `admin/shipping/edit.jsp`.

### Phase 11: Integration & Polish [REQ: All — Full Test Suite]

**11.1** Run `api-test.sh`, fix failures iteratively.

**11.2** Visual verification against `screenshot/` images.

**11.3** Edge case fixes (empty IDs, huge page numbers, size=0, non-existent pages).

## Project Structure

```
monolith-new/
├── pom.xml
└── src/main/
    ├── java/com/skishop/
    │   ├── Application.java
    │   ├── common/
    │   │   ├── config/AppConfig.java
    │   │   └── util/PasswordHasher.java
    │   ├── config/
    │   │   ├── DataSourceConfig.java
    │   │   ├── SecurityConfig.java
    │   │   ├── WebMvcConfig.java
    │   │   └── CsrfTokenConfig.java
    │   ├── domain/{user,product,cart,order,payment,coupon,inventory,shipping,address,point,mail}/
    │   ├── dao/{user,product,category,cart,order,payment,coupon,inventory,shipping,address,point,mail}/
    │   ├── service/{auth,user,catalog,cart,order,payment,coupon,inventory,shipping,tax,point,mail}/
    │   └── web/
    │       ├── controller/{HomeController,AuthController,ProductController,...}
    │       ├── controller/admin/{AdminProductController,...}
    │       ├── form/{LoginForm,RegisterForm,...}
    │       ├── form/admin/{AdminProductForm,...}
    │       ├── interceptor/RequestIdInterceptor.java
    │       └── GlobalExceptionHandler.java
    ├── resources/
    │   ├── application.properties
    │   ├── application-h2.properties
    │   ├── application-postgresql.properties
    │   ├── schema.sql, data.sql
    │   └── messages.properties
    └── webapp/
        ├── WEB-INF/jsp/{layouts,common,home,auth,products,cart,orders,account,coupons,points,admin}/
        ├── assets/{css,js,images}/
        ├── error.jsp
        └── index.jsp
```

## Testing Strategy

- **appType**: Server-rendered HTML (JSP) with session-based auth
- **Critical user journeys** (from api-test.sh):
  1. **Home & Product Browsing**: GET /, /home, /products (pagination/search/filter), /product?id=
  2. **Auth Flow**: Register → Login → Authenticated access → Logout
  3. **Cart & Checkout Flow**: View cart → Apply coupon → Checkout → Order confirmation
  4. **Account Management**: Address CRUD, point balance, order history
  5. **Admin Operations**: Product/order/coupon/shipping management (ADMIN role only)
  6. **Security Enforcement**: Unauthenticated access → redirect to /login, non-ADMIN → 403 for /admin/**

- **primaryValidationStack**: `api-test.sh` (shell-based HTTP test suite, 55 test cases) — this IS the acceptance oracle
- **fallbackMatrix**:
  - `infra-tier`: H2 in-memory (embedded, no Docker needed). No fallback required.
  - `browser-tier`: Not applicable — `api-test.sh` uses `curl` (HTTP-level testing, not browser rendering). Visual verification done manually against screenshots.
- **Environment requirements**: Java 21, Maven 3.9.x, curl (for api-test.sh). No Docker required. No Node.js required.
- **knownGaps**: api-test.sh tests HTTP status + body content but NOT JavaScript behavior or CSS rendering. Visual fidelity verified manually against screenshots.
- **Test data strategy**: H2 in-memory with `schema.sql` + `data.sql` loaded at startup. Each test run starts fresh (app restart). api-test.sh registers unique users via timestamp suffix for isolation.
- **Acceptance criteria**: All 55 api-test.sh tests pass (PASS=55, FAIL=0). Application starts within 120s. `mvn clean package -DskipTests` succeeds.
- **Validation review expectations**: Tester runs `api-test.sh`, reports pass/fail counts. Visual spot-check of 3-5 screenshots. Any FAIL triggers investigation + fix cycle.

## Task Breakdown

### Phase 1: Project Scaffold & Configuration

- [ ] T001 [Plan:1.1,1.2] Create `monolith-new/` Maven project: pom.xml (Spring Boot 3.2.5 parent, all dependencies), `Application.java`, `application.properties`, `application-h2.properties`, `application-postgresql.properties`
- [ ] T002 [Plan:1.3] Create Spring config classes: `SecurityConfig.java` (URL auth, CSRF _csrfToken, formLogin disabled), `WebMvcConfig.java` (JSP resolver, /assets/** handler), `DataSourceConfig.java`, `CsrfTokenConfig.java` [Source: src/main/java/com/skishop/web/processor/AuthRequestProcessor.java]
- [ ] T003 [Plan:1.4,1.5,1.6,1.7] Copy resources (schema.sql, data.sql, messages.properties, assets/), create layout JSPs (header, footer, messages, error, index), `GlobalExceptionHandler.java`, `RequestIdInterceptor.java`, copy `PasswordHasher.java` verbatim [Source: src/main/java/com/skishop/common/util/PasswordHasher.java] [Source: src/main/java/com/skishop/web/filter/RequestIdFilter.java] [Source: src/main/webapp/WEB-INF/jsp/layouts/base.jsp] [Source: src/main/webapp/WEB-INF/jsp/common/header.jsp] [Source: src/main/webapp/WEB-INF/jsp/common/footer.jsp] [Source: src/main/webapp/WEB-INF/jsp/common/messages.jsp]

### Phase 2: Domain & Common

- [ ] T004 [P] [Plan:2.1,2.2] Copy all 24 domain POJOs to `monolith-new/`, update package declarations, create `AppConfig.java` [Source: src/main/java/com/skishop/domain/]

### Phase 3: DAO Layer

- [ ] T005 [P] [Plan:3.1] Port User DAOs to JdbcTemplate: UserDao/UserDaoImpl, SecurityLogDao/SecurityLogDaoImpl, PasswordResetTokenDao/PasswordResetTokenDaoImpl [Source: src/main/java/com/skishop/dao/user/UserDaoImpl.java] [Source: src/main/java/com/skishop/dao/user/SecurityLogDaoImpl.java] [Source: src/main/java/com/skishop/dao/user/PasswordResetTokenDaoImpl.java]
- [ ] T006 [P] [Plan:3.2] Port Product DAOs to JdbcTemplate: ProductDao/ProductDaoImpl (with pagination/search/filter SQL), PriceDao/PriceDaoImpl, CategoryDao/CategoryDaoImpl [Source: src/main/java/com/skishop/dao/product/ProductDaoImpl.java] [Source: src/main/java/com/skishop/dao/product/PriceDaoImpl.java] [Source: src/main/java/com/skishop/dao/category/CategoryDaoImpl.java]
- [ ] T007 [P] [Plan:3.3] Port Cart DAOs to JdbcTemplate: CartDao/CartDaoImpl, CartItemDao/CartItemDaoImpl [Source: src/main/java/com/skishop/dao/cart/CartDaoImpl.java]
- [ ] T008 [P] [Plan:3.4] Port Order DAOs to JdbcTemplate: OrderDao/OrderDaoImpl, OrderShippingDao/OrderShippingDaoImpl, ReturnDao/ReturnDaoImpl [Source: src/main/java/com/skishop/dao/order/OrderDaoImpl.java] [Source: src/main/java/com/skishop/dao/order/OrderShippingDaoImpl.java] [Source: src/main/java/com/skishop/dao/order/ReturnDaoImpl.java]
- [ ] T009 [P] [Plan:3.5] Port Payment & Inventory DAOs to JdbcTemplate: PaymentDao/PaymentDaoImpl, InventoryDao/InventoryDaoImpl (with @Transactional for FOR UPDATE) [Source: src/main/java/com/skishop/dao/payment/PaymentDaoImpl.java] [Source: src/main/java/com/skishop/dao/inventory/InventoryDaoImpl.java]
- [ ] T010 [P] [Plan:3.6] Port Coupon DAOs to JdbcTemplate: CouponDao/CouponDaoImpl, CouponUsageDao/CouponUsageDaoImpl [Source: src/main/java/com/skishop/dao/coupon/CouponDaoImpl.java] [Source: src/main/java/com/skishop/dao/coupon/CouponUsageDaoImpl.java]
- [ ] T011 [P] [Plan:3.7] Port Supporting DAOs to JdbcTemplate: ShippingMethodDao/Impl, UserAddressDao/Impl, PointAccountDao/Impl, PointTransactionDao/Impl, EmailQueueDao/Impl [Source: src/main/java/com/skishop/dao/shipping/ShippingMethodDaoImpl.java] [Source: src/main/java/com/skishop/dao/address/UserAddressDaoImpl.java] [Source: src/main/java/com/skishop/dao/point/PointAccountDaoImpl.java] [Source: src/main/java/com/skishop/dao/point/PointTransactionDaoImpl.java] [Source: src/main/java/com/skishop/dao/mail/EmailQueueDaoImpl.java]

### Phase 4: Service Layer

- [ ] T012 [P] [Plan:4.1] Port AuthService + AuthResult to Spring DI [Source: src/main/java/com/skishop/service/auth/AuthService.java] [Source: src/main/java/com/skishop/service/auth/AuthResult.java]
- [ ] T013 [P] [Plan:4.2] Port UserService, ProductService, CategoryService to Spring DI [Source: src/main/java/com/skishop/service/user/UserService.java] [Source: src/main/java/com/skishop/service/catalog/ProductService.java] [Source: src/main/java/com/skishop/service/catalog/CategoryService.java]
- [ ] T014 [P] [Plan:4.3] Port CartService, CouponService to Spring DI [Source: src/main/java/com/skishop/service/cart/CartService.java] [Source: src/main/java/com/skishop/service/coupon/CouponService.java]
- [ ] T015 [P] [Plan:4.4] Port OrderFacade + OrderFacadeImpl + OrderService to Spring DI (preserve compensating actions, NO @Transactional on placeOrder) [Source: src/main/java/com/skishop/service/order/OrderFacade.java] [Source: src/main/java/com/skishop/service/order/OrderFacadeImpl.java] [Source: src/main/java/com/skishop/service/order/OrderService.java]
- [ ] T016 [P] [Plan:4.5] Port PaymentService (Luhn, authorize, void), InventoryService (@Transactional reserve/release), ShippingService, TaxService to Spring DI [Source: src/main/java/com/skishop/service/payment/PaymentService.java] [Source: src/main/java/com/skishop/service/inventory/InventoryService.java] [Source: src/main/java/com/skishop/service/shipping/ShippingService.java] [Source: src/main/java/com/skishop/service/tax/TaxService.java]
- [ ] T017 [P] [Plan:4.6] Port PointService (@Transactional expirePoints), MailService to Spring DI. Copy PaymentInfo + PaymentResult DTOs. [Source: src/main/java/com/skishop/service/point/PointService.java] [Source: src/main/java/com/skishop/service/mail/MailService.java] [Source: src/main/java/com/skishop/service/payment/PaymentInfo.java] [Source: src/main/java/com/skishop/service/payment/PaymentResult.java]

### Phase 5: Auth & Home Module (User Story: Authentication)

- [ ] T018 [US-AUTH] [Plan:5.1] Create form POJOs with Jakarta Validation: LoginForm, RegisterForm, PasswordResetRequestForm, PasswordResetForm [Source: src/main/java/com/skishop/web/form/LoginForm.java] [Source: src/main/java/com/skishop/web/form/RegisterForm.java] [Source: src/main/java/com/skishop/web/form/PasswordResetRequestForm.java] [Source: src/main/java/com/skishop/web/form/PasswordResetForm.java]
- [ ] T019 [US-AUTH] [Plan:5.2] Create AuthController: GET/POST /login (manual auth → SecurityContext), GET /logout (session invalidate + redirect), GET/POST /register, GET/POST /password/forgot, GET/POST /password/reset [Source: src/main/java/com/skishop/web/action/LoginAction.java] [Source: src/main/java/com/skishop/web/action/LogoutAction.java] [Source: src/main/java/com/skishop/web/action/RegisterAction.java] [Source: src/main/java/com/skishop/web/action/PasswordForgotAction.java] [Source: src/main/java/com/skishop/web/action/PasswordResetAction.java]
- [ ] T020 [US-AUTH] [Plan:5.3] Create HomeController: GET /, GET /home → "home" view [Source: src/main/webapp/WEB-INF/jsp/home.jsp]
- [ ] T021 [US-AUTH] [Plan:5.4] Migrate auth JSP views: login.jsp, register.jsp, password/forgot.jsp, forgot_complete.jsp, reset.jsp, home.jsp — replace Struts tags with JSTL/Spring form tags, add _csrfToken hidden field to all POST forms, add jsp:include for layout [Source: src/main/webapp/WEB-INF/jsp/auth/login.jsp] [Source: src/main/webapp/WEB-INF/jsp/auth/register.jsp] [Source: src/main/webapp/WEB-INF/jsp/auth/password/forgot.jsp] [Source: src/main/webapp/WEB-INF/jsp/auth/password/forgot_complete.jsp] [Source: src/main/webapp/WEB-INF/jsp/auth/password/reset.jsp] [Source: src/main/webapp/WEB-INF/jsp/home.jsp]

### Phase 6: Product Catalog Module (User Story: Browsing)

- [ ] T022 [US-CATALOG] [Plan:6.1,6.2] Create ProductController (GET /products with pagination/search/filter, GET /product?id=) and ProductSearchForm [Source: src/main/java/com/skishop/web/action/ProductListAction.java] [Source: src/main/java/com/skishop/web/action/ProductDetailAction.java] [Source: src/main/java/com/skishop/web/form/ProductSearchForm.java]
- [ ] T023 [US-CATALOG] [Plan:6.3] Migrate product JSP views: products/list.jsp (pagination, search form, category filter), products/detail.jsp, products/notfound.jsp [Source: src/main/webapp/WEB-INF/jsp/products/list.jsp] [Source: src/main/webapp/WEB-INF/jsp/products/detail.jsp] [Source: src/main/webapp/WEB-INF/jsp/products/notfound.jsp]

### Phase 7: Cart & Coupons Module (User Story: Shopping)

- [ ] T024 [US-CART] [Plan:7.1,7.2] Create CartController (GET/POST /cart, session+cookie CART_ID management) and AddCartForm [Source: src/main/java/com/skishop/web/action/CartAction.java] [Source: src/main/java/com/skishop/web/form/AddCartForm.java]
- [ ] T025 [US-CART] [Plan:7.3] Create CouponController (GET /coupons, POST /coupons/apply) and CouponForm [Source: src/main/java/com/skishop/web/action/CouponAvailableAction.java] [Source: src/main/java/com/skishop/web/action/CouponApplyAction.java] [Source: src/main/java/com/skishop/web/form/CouponForm.java]
- [ ] T026 [US-CART] [Plan:7.4] Migrate cart/coupon JSP views: cart/view.jsp (with CSRF hidden field), coupons/available.jsp [Source: src/main/webapp/WEB-INF/jsp/cart/view.jsp] [Source: src/main/webapp/WEB-INF/jsp/coupons/available.jsp]

### Phase 8: Checkout & Orders Module (User Story: Purchasing)

- [ ] T027 [US-ORDER] [Plan:8.1,8.2] Create CheckoutController (GET/POST /checkout → OrderFacade.placeOrder) and CheckoutForm with Jakarta Validation [Source: src/main/java/com/skishop/web/action/CheckoutAction.java] [Source: src/main/java/com/skishop/web/form/CheckoutForm.java]
- [ ] T028 [US-ORDER] [Plan:8.2] Migrate checkout JSP views: cart/checkout.jsp, cart/confirmation.jsp [Source: src/main/webapp/WEB-INF/jsp/cart/checkout.jsp] [Source: src/main/webapp/WEB-INF/jsp/cart/confirmation.jsp]
- [ ] T029 [US-ORDER] [Plan:8.3] Create OrderController (GET /orders, GET /orders/detail?id=, POST /orders/{id}/cancel, POST /orders/{id}/return) [Source: src/main/java/com/skishop/web/action/OrderHistoryAction.java] [Source: src/main/java/com/skishop/web/action/OrderDetailAction.java] [Source: src/main/java/com/skishop/web/action/OrderCancelAction.java] [Source: src/main/java/com/skishop/web/action/OrderReturnAction.java]
- [ ] T030 [US-ORDER] [Plan:8.3] Migrate order JSP views: orders/history.jsp, orders/detail.jsp [Source: src/main/webapp/WEB-INF/jsp/orders/history.jsp] [Source: src/main/webapp/WEB-INF/jsp/orders/detail.jsp]

### Phase 9: Account, Points & Address Module (User Story: Account)

- [ ] T031 [US-ACCOUNT] [Plan:9.1,9.2] Create AddressController (GET /account/addresses, GET /account/addresses/edit, POST /account/addresses/save) and AddressForm with Jakarta Validation [Source: src/main/java/com/skishop/web/action/AddressListAction.java] [Source: src/main/java/com/skishop/web/action/AddressSaveAction.java] [Source: src/main/java/com/skishop/web/form/AddressForm.java]
- [ ] T032 [US-ACCOUNT] [Plan:9.2] Migrate account JSP views: account/addresses.jsp, account/address_edit.jsp [Source: src/main/webapp/WEB-INF/jsp/account/addresses.jsp] [Source: src/main/webapp/WEB-INF/jsp/account/address_edit.jsp]
- [ ] T033 [US-ACCOUNT] [Plan:9.3] Create PointController (GET /points) and migrate points/balance.jsp [Source: src/main/java/com/skishop/web/action/PointBalanceAction.java] [Source: src/main/webapp/WEB-INF/jsp/points/balance.jsp]

### Phase 10: Admin Module (User Story: Admin)

- [ ] T034 [US-ADMIN] [Plan:10.1,10.2] Create AdminProductController (GET /admin/products, GET/POST /admin/product/edit, POST /admin/product/delete) and AdminProductForm; migrate admin/products/list.jsp, admin/products/edit.jsp [Source: src/main/java/com/skishop/web/action/admin/AdminProductListAction.java] [Source: src/main/java/com/skishop/web/action/admin/AdminProductEditAction.java] [Source: src/main/java/com/skishop/web/action/admin/AdminProductDeleteAction.java] [Source: src/main/java/com/skishop/web/form/admin/AdminProductForm.java] [Source: src/main/webapp/WEB-INF/jsp/admin/products/list.jsp] [Source: src/main/webapp/WEB-INF/jsp/admin/products/edit.jsp]
- [ ] T035 [US-ADMIN] [Plan:10.3] Create AdminOrderController (GET /admin/orders, GET /admin/orders/detail, POST /admin/order/update, POST /admin/order/refund); migrate admin/orders/list.jsp, admin/orders/detail.jsp [Source: src/main/java/com/skishop/web/action/admin/AdminOrderListAction.java] [Source: src/main/java/com/skishop/web/action/admin/AdminOrderDetailAction.java] [Source: src/main/java/com/skishop/web/action/admin/AdminOrderUpdateAction.java] [Source: src/main/java/com/skishop/web/action/admin/AdminOrderRefundAction.java] [Source: src/main/webapp/WEB-INF/jsp/admin/orders/list.jsp] [Source: src/main/webapp/WEB-INF/jsp/admin/orders/detail.jsp]
- [ ] T036 [US-ADMIN] [Plan:10.4] Create AdminCouponController (GET /admin/coupons, GET/POST /admin/coupon/edit) and AdminCouponForm; migrate admin/coupons/list.jsp, admin/coupons/edit.jsp [Source: src/main/java/com/skishop/web/action/admin/AdminCouponListAction.java] [Source: src/main/java/com/skishop/web/action/admin/AdminCouponEditAction.java] [Source: src/main/java/com/skishop/web/form/admin/AdminCouponForm.java] [Source: src/main/webapp/WEB-INF/jsp/admin/coupons/list.jsp] [Source: src/main/webapp/WEB-INF/jsp/admin/coupons/edit.jsp]
- [ ] T037 [US-ADMIN] [Plan:10.5] Create AdminShippingController (GET /admin/shipping, GET/POST /admin/shipping/edit) and AdminShippingMethodForm; migrate admin/shipping/list.jsp, admin/shipping/edit.jsp [Source: src/main/java/com/skishop/web/action/admin/AdminShippingMethodListAction.java] [Source: src/main/java/com/skishop/web/action/admin/AdminShippingMethodEditAction.java] [Source: src/main/java/com/skishop/web/form/admin/AdminShippingMethodForm.java] [Source: src/main/webapp/WEB-INF/jsp/admin/shipping/list.jsp] [Source: src/main/webapp/WEB-INF/jsp/admin/shipping/edit.jsp]

### Phase 11: Integration & Polish

- [ ] T038 [Plan:11.1,11.2,11.3] Run api-test.sh, fix failures iteratively, verify visual parity with screenshots, fix edge cases (empty IDs, huge pagination, 404 pages)

## Requirement Mapping

Requirements are derived from the constitution (C-I through C-VII), the URL routing contract, the test script sections (TS-1 through TS-14), and the risk register (R1-R14).

| REQ ID | Description | Plan Items | Implementation Evidence |
|--------|-------------|------------|------------------------|
| C-I | Functional equivalence — every URL works identically | All phases | All controllers, all JSPs, api-test.sh 55/55 pass |
| C-II | Keep JSPs — Struts tags → JSTL/Spring, Tiles → jsp:include | 1.5, 5.4, 6.3, 7.4, 8.2, 8.3, 9.2, 9.3, 10.2–10.5 | All JSPs under WEB-INF/jsp/ |
| C-III | New project in monolith-new/ | 1.1 | monolith-new/pom.xml, Application.java |
| C-IV | Preserve data model exactly | 1.4, 2.1, 3.1–3.7 | schema.sql, data.sql, domain POJOs, all DAO impls |
| C-V | Security parity | 1.3, 1.7, 5.1, 5.2 | SecurityConfig.java, AuthController.java, PasswordHasher.java |
| C-VI | Build/run contract | 1.1, 1.2 | pom.xml, application.properties, Application.java |
| C-VII | No over-engineering | All | No JPA, no Lombok, no Swagger, no CQRS |
| TS-1 | Home page (GET /, /home) | 5.3 | HomeController.java, home.jsp |
| TS-2 | Product pages (list, detail, search, filter) | 6.1, 6.2, 6.3 | ProductController.java, products/*.jsp |
| TS-3 | Authentication (login, invalid credentials, validation) | 5.1, 5.2, 5.4 | AuthController.java, LoginForm.java, login.jsp |
| TS-4 | Registration (validation, duplicate, success) | 5.1, 5.2, 5.4 | AuthController.java, RegisterForm.java, register.jsp |
| TS-5 | Login with new user | 5.2 | AuthController.java |
| TS-6 | Password reset (forgot, reset, invalid token) | 5.1, 5.2, 5.4 | AuthController.java, password/*.jsp |
| TS-7 | Cart (view, add items) | 7.1, 7.2, 7.4 | CartController.java, cart/view.jsp |
| TS-8 | Coupons (list, apply valid/invalid) | 7.3, 7.4 | CouponController.java, coupons/available.jsp |
| TS-9 | Checkout & orders | 8.1, 8.2, 8.3 | CheckoutController.java, OrderController.java |
| TS-10 | Protected pages (unauthenticated → login redirect, ADMIN → 403) | 1.3 | SecurityConfig.java |
| TS-11 | Authenticated user flows (address CRUD, points, orders) | 9.1, 9.2, 9.3 | AddressController.java, PointController.java |
| TS-12 | Order operations (cancel, return) | 8.3 | OrderController.java |
| TS-13 | Product search variations (category filter, pagination) | 6.1 | ProductController.java |
| TS-14 | Edge cases (404, empty ID, huge page) | 11.3 | GlobalExceptionHandler.java, controller param handling |
| R1 | Password hashing algorithm exact replication | 1.7 | PasswordHasher.java (verbatim copy) |
| R2 | CSRF token field name _csrfToken | 1.3 | SecurityConfig.java, all POST form JSPs |
| R3 | Spring Security formLogin disabled | 1.3 | SecurityConfig.java |
| R4 | JSP embedded Tomcat packaging | 1.1 | pom.xml (tomcat-embed-jasper compile scope) |
| R5 | H2 PostgreSQL compatibility | 1.2, 1.4 | application-h2.properties (MODE=PostgreSQL) |
| R8 | OrderFacade no @Transactional | 4.4 | OrderFacadeImpl.java |
