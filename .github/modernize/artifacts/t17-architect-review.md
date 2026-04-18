# t17 — Architecture Conformance Report

## 1. Build & Functional Verification

| Check | Result |
|-------|--------|
| `mvn clean package -DskipTests` | **PASS** — zero errors |
| Spring Boot startup (H2 profile) | **PASS** — starts in ~3s |
| API test suite (`api-test.sh`) | **64/65 PASS** (98.5%) |
| Failed test | `GET /account/addresses` (HTTP 500 for new user) |

## 2. Controller Layer — CONFORMANT

All 14 controllers match the t2 URL routing contract exactly:

| Controller | Prefix | Methods | URL Match | View Names |
|-----------|--------|---------|-----------|------------|
| HomeController | — | GET `/`, `/home` | ✅ | `home` |
| AuthController | — | GET/POST `/login`, `/logout`, `/register`, `/password/*` | ✅ | `auth/*` |
| ProductController | — | GET `/products`, `/product` | ✅ | `products/*` |
| CartController | — | GET/POST `/cart` | ✅ | `cart/view`, redirect |
| CheckoutController | — | GET/POST `/checkout` | ✅ | `cart/checkout`, `cart/confirmation` |
| CouponController | — | GET `/coupons`, POST `/coupons/apply` | ✅ | `coupons/available`, `cart/view` |
| OrderController | — | GET `/orders`, `/orders/detail`, POST `/{id}/cancel`, `/{id}/return` | ✅ | `orders/*` |
| PointController | — | GET `/points` | ✅ | `points/balance` |
| AddressController | — | GET `/account/addresses`, `/account/addresses/edit`, POST `/account/addresses/save` | ✅ | `account/*` |
| AdminProductController | `/admin` | GET/POST products CRUD | ✅ | `admin/products/*` |
| AdminOrderController | `/admin` | GET/POST orders management | ✅ | `admin/orders/*` |
| AdminCouponController | `/admin` | GET/POST coupons CRUD | ✅ | `admin/coupons/*` |
| AdminShippingController | `/admin` | GET/POST shipping CRUD | ✅ | `admin/shipping/*` |
| GlobalExceptionHandler | — | `@ControllerAdvice` catch-all | ✅ | `error` |

**Patterns verified:**
- ✅ `@Controller` annotation on all
- ✅ `@RequestMapping("/admin")` prefix on admin controllers
- ✅ HTTP method annotations (`@GetMapping`/`@PostMapping`)
- ✅ `@RequestParam`, `@PathVariable`, `@ModelAttribute`, `@Valid` + `BindingResult` patterns
- ✅ Session attribute access (`loginUser`, `cartId`) via `HttpServletRequest`
- ✅ Manual authentication in AuthController with Spring Security context integration
- ✅ Session fixation prevention on login

## 3. Service Layer — CONFORMANT (with advisory)

All 15 services use correct Spring patterns:

| Service | @Service | Constructor Injection | Legacy Refs | Status |
|---------|----------|----------------------|-------------|--------|
| AuthService | ✅ | ✅ UserDao, SecurityLogDao | None | Clean |
| UserService | ✅ | ✅ UserDao | None | Clean |
| CategoryService | ✅ | ✅ CategoryDao | None | Clean |
| ProductService | ✅ | ✅ ProductDao, JdbcTemplate | None | ⚠️ Manual JDBC |
| CartService | ✅ | ✅ CartDao, PriceDao | None | Clean |
| CouponService | ✅ | ✅ CouponDao, CouponUsageDao | None | Clean |
| InventoryService | ✅ | ✅ JdbcTemplate | None | ⚠️ Manual JDBC |
| MailService | ✅ | ✅ EmailQueueDao, JavaMailSender | None | Clean |
| OrderService | ✅ | ✅ OrderDao, ReturnDao | None | Clean |
| OrderFacadeImpl | ✅ | ✅ 12 deps | None | Clean |
| PaymentService | ✅ | ✅ PaymentDao | None | Clean |
| PointService | ✅ | ✅ PointAccountDao, PointTransactionDao, JdbcTemplate | None | ⚠️ Manual JDBC |
| ShippingService | ✅ | ✅ OrderShippingDao | None | Clean |
| TaxService | ✅ | ✅ None (pure calc) | None | Clean |

**Advisory — Manual JDBC Transaction Management:**
Three services (InventoryService, PointService, ProductService) use `con.setAutoCommit(false)` with manual commit/rollback. Per decisions.md, this preserves source behavioral fidelity. However, the connection obtained via `jdbcTemplate.getDataSource().getConnection()` is NOT managed by Spring's DataSourceTransactionManager — this is a deliberate design trade-off, not a bug.

## 4. DAO Layer — CONFORMANT

All 18 DAO implementations follow the target pattern:

- ✅ `@Repository` annotation on all implementations
- ✅ Constructor-injected `JdbcTemplate`
- ✅ Lambda `RowMapper` patterns (no BeanHandler/BeanListHandler)
- ✅ No leftover Commons DBUtils references (QueryRunner, AbstractDao)
- ✅ Interface/Impl separation maintained

## 5. Domain Layer — CONFORMANT

24+ POJOs verified as plain Java objects:
- ✅ No framework dependencies (no Struts, no JPA annotations, no Spring annotations)
- ✅ Standard getter/setter patterns
- ✅ Clean package organization per t2 design (user/, product/, cart/, order/, etc.)

## 6. Configuration Layer — CONFORMANT

| Config Class | Purpose | Matches t2 |
|-------------|---------|------------|
| Application.java | @SpringBootApplication + @EnableScheduling | ✅ |
| SecurityConfig | CSRF `_csrfToken`, formLogin disabled, admin role check | ✅ |
| WebMvcConfig | Interceptor registration, /assets/** resources | ✅ |
| CsrfTokenConfig | Interceptor: exposes `_csrf` + `_csrfToken` to JSPs | ✅ |
| DataSourceConfig | @EnableTransactionManagement, JdbcTemplate + TxManager beans | ✅ |

**Properties verified:**
- ✅ `application.properties`: port 8080, JSP resolver, session 30m, SQL init, messages
- ✅ `application-h2.properties`: H2 mem + PostgreSQL mode
- ✅ `application-postgresql.properties`: env-var driven credentials

**pom.xml verified:**
- ✅ Spring Boot 3.2.5 parent
- ✅ Java 21
- ✅ All required starters (web, security, validation, jdbc, mail)
- ✅ tomcat-embed-jasper (compile scope — default, not provided)
- ✅ Jakarta JSTL API + impl
- ✅ H2 + PostgreSQL runtime drivers
- ✅ spring-boot-maven-plugin
- ✅ webapp → META-INF/resources build resource mapping

## 7. JSP View Layer — CONFORMANT

**32 JSP files** — all required views present per t2 spec.

| Category | Expected | Found | Status |
|----------|----------|-------|--------|
| Common (header, footer, messages) | 3 | 3 | ✅ |
| Layout (base.jsp) | 1 | 1 | ✅ |
| Home | 1 | 1 | ✅ |
| Auth (login, register, password/*) | 5 | 5 | ✅ |
| Products (list, detail, notfound) | 3 | 3 | ✅ |
| Cart (view, checkout, confirmation) | 3 | 3 | ✅ |
| Orders (history, detail) | 2 | 2 | ✅ |
| Account (addresses, address_edit) | 2 | 2 | ✅ |
| Coupons (available) | 1 | 1 | ✅ |
| Points (balance) | 1 | 1 | ✅ |
| Admin (products, orders, coupons, shipping ×2 each) | 8 | 8 | ✅ |
| Error pages | 1 | 2 | ✅ (extra) |

**Migration verification:**
- ✅ Zero Struts tag imports (`html:`, `bean:`, `logic:`, `tiles:`, `s:`)
- ✅ All use Jakarta JSTL (`c:`, `fmt:`)
- ✅ Consistent layout pattern: `<jsp:include>` for header/messages/footer
- ✅ CSRF tokens present in all forms (using `${_csrf.token}` or `${_csrfToken}`)
- ✅ `<c:out>` used for XSS-safe output escaping
- ✅ No Spring form tags (`<form:form>`) — plain HTML forms with EL

**Note on index.jsp:** Not present (t2 listed it as optional). HomeController maps `/` directly, making index.jsp unnecessary.

## 8. Security Architecture — CONFORMANT

| Requirement | Status |
|------------|--------|
| CSRF param name `_csrfToken` | ✅ HttpSessionCsrfTokenRepository configured |
| formLogin disabled | ✅ `.formLogin(form -> form.disable())` |
| Manual auth in AuthController | ✅ SecurityContextHolder + UsernamePasswordAuthenticationToken |
| `/admin/**` requires ADMIN | ✅ `.hasRole("ADMIN")` in SecurityConfig |
| Public endpoints permitted | ✅ All public URLs listed in permitAll() |
| Authenticated endpoints | ✅ orders, points, account require USER or ADMIN |
| FORWARD/ERROR dispatchers permitted | ✅ Prevents 403 on JSP includes |
| Session fixation prevention | ✅ Old session invalidated on login |
| Logout | ✅ AuthController invalidates session manually |

## 9. Module Boundaries & Naming — CONFORMANT

Package structure matches t2 exactly:
```
com.skishop/
├── Application.java
├── common/util/PasswordHasher
├── config/ (SecurityConfig, WebMvcConfig, CsrfTokenConfig, DataSourceConfig)
├── domain/ (user, product, cart, order, coupon, inventory, shipping, address, point, mail, payment)
├── dao/ (matching domain subpackages, Interface + Impl pairs)
├── service/ (auth, user, catalog, cart, order, payment, coupon, inventory, shipping, tax, point, mail)
└── web/
    ├── controller/ (14 controllers)
    ├── form/ (LoginForm, RegisterForm, etc.)
    ├── interceptor/ (via CsrfTokenConfig in config/)
    └── GlobalExceptionHandler
```

**Layering verified:**
- ✅ Controllers depend on Services only (no direct DAO access)
- ✅ Services depend on DAOs only (no controller references)
- ✅ DAOs depend on JdbcTemplate + domain objects only
- ✅ Domain objects have zero dependencies
- ✅ No circular dependencies detected

## 10. Overall Verdict

| Layer | Status | Notes |
|-------|--------|-------|
| Build | ✅ PASS | Clean compile, executable JAR |
| Controllers | ✅ PASS | All URLs match t2 contract |
| Services | ✅ PASS | Constructor injection, clean DI |
| DAOs | ✅ PASS | JdbcTemplate, RowMapper lambdas |
| Domain | ✅ PASS | Plain POJOs |
| Config | ✅ PASS | All beans, properties, security |
| JSPs | ✅ PASS | 32 files, Struts tags fully removed |
| Security | ✅ PASS | CSRF, roles, session management |
| API Tests | ⚠️ 64/65 | 1 bug: addresses.jsp |
| Naming/Layering | ✅ PASS | Matches t2 spec |
