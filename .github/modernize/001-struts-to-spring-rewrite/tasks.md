# Tasks: Struts 1.x to Spring Boot 3.2 MVC Rewrite

## Format: `[ID] [P?] [Story?] [Plan:X.Y] Description`

---

## Phase 1: Project Foundation & Spring Boot Scaffolding

**Batch**: 1
**Goal**: Replace the Struts project infrastructure with Spring Boot; establish compile-pass baseline.
**Exit Criteria**: `mvn -B compile` passes. No Struts, commons-dbcp, commons-dbutils, or log4j references remain in production code.

---

### 1.1 Maven POM Rewrite

- [x] T100 [Plan:1.1] Add Spring Boot 3.2.x parent POM (`org.springframework.boot:spring-boot-starter-parent:3.2.x`) to `pom.xml`. Set `<java.version>21</java.version>`. Change `<packaging>` from `war` to `jar`.
- [x] T101 [Plan:1.1] Add Spring Boot starter dependencies to `pom.xml`: `spring-boot-starter-web`, `spring-boot-starter-jdbc`, `spring-boot-starter-mail`, `spring-boot-starter-validation`. Add `spring-boot-starter-test` with `<scope>test</scope>`.
- [x] T102 [Plan:1.1] Add non-starter dependencies to `pom.xml`: `org.postgresql:postgresql:42.x` (runtime), `jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api`, `org.glassfish.web:jakarta.servlet.jsp.jstl` (runtime), `org.apache.tomcat.embed:tomcat-embed-jasper` (provided), `jakarta.servlet.jsp:jakarta.servlet.jsp-api` (provided).
- [x] T103 [Plan:1.1] Remove all legacy dependencies from `pom.xml`: struts-core, struts-taglib, struts-tiles, struts-extras, commons-dbcp, commons-pool, commons-dbutils, commons-beanutils, commons-digester, commons-validator, commons-chain, commons-fileupload, commons-io, log4j, oro, strutstestcase. Remove any explicit servlet-api 2.5 dependency.
- [x] T104 [Plan:1.1] Add `spring-boot-maven-plugin` to `pom.xml` `<build><plugins>` section. Remove `maven-war-plugin` if present. Set `<finalName>skishop-monolith</finalName>`.

### 1.2 Spring Boot Application Class

- [x] T105 [Plan:1.2] Create `src/main/java/com/skishop/Application.java` with `@SpringBootApplication` annotation and `main()` method calling `SpringApplication.run()`.

### 1.3 Application Properties

- [x] T106 [Plan:1.3] Create `src/main/resources/application.properties` with Spring DataSource configuration: `spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:skishop}`, `spring.datasource.username=${DB_USER:skishop}`, `spring.datasource.password=${DB_PASSWORD:skishop}`. Add HikariCP pool settings: `spring.datasource.hikari.maximum-pool-size=${DB_POOL_MAX_ACTIVE:20}`, `spring.datasource.hikari.minimum-idle=${DB_POOL_MAX_IDLE:5}`, `spring.datasource.hikari.connection-timeout=${DB_POOL_MAX_WAIT:30000}`.
- [x] T107 [Plan:1.3] Add JSP view resolver settings to `application.properties`: `spring.mvc.view.prefix=/WEB-INF/jsp/`, `spring.mvc.view.suffix=.jsp`. Add `server.port=8080`.
- [x] T108 [Plan:1.3] Add logging configuration to `application.properties`: `logging.level.root=INFO`, `logging.level.com.skishop=DEBUG`, `logging.pattern.console` matching existing log format. Add `spring.sql.init.mode=always`, `spring.sql.init.schema-locations=classpath:db/schema.sql`, `spring.sql.init.data-locations=classpath:db/data.sql` for DB init.
- [x] T109 [P] [Plan:1.3] Add mail configuration to `application.properties`: `spring.mail.host`, `spring.mail.port`, `spring.mail.username`, `spring.mail.password` with env-var defaults from existing `app.properties` mail settings.

### 1.4 Remove Struts Infrastructure

- [x] T110 [Plan:1.4] Delete Struts XML configuration files: `src/main/webapp/WEB-INF/struts-config.xml`, `src/main/webapp/WEB-INF/tiles-defs.xml`, `src/main/webapp/WEB-INF/validation.xml`, `src/main/webapp/WEB-INF/validator-rules.xml`, `src/main/webapp/WEB-INF/web.xml`, `src/main/webapp/META-INF/context.xml`.
- [x] T111 [Plan:1.4] Delete `src/main/resources/log4j.properties` (replaced by Spring Boot Logback defaults).
- [x] T112 [Plan:1.4] Delete Struts infrastructure Java classes: `com.skishop.common.service.ServiceLocator`, `com.skishop.common.dao.DaoFactory`, `com.skishop.common.dao.DataSourceLocator`, `com.skishop.common.dao.DataSourceFactory`, `com.skishop.common.dao.AbstractDao`, `com.skishop.common.dao.DaoException`, `com.skishop.common.config.AppConfig`.

### 1.5 Domain Objects Preservation

- [x] T113 [P] [Plan:1.5] Verify all 24 domain POJOs compile under Java 21 without modification. Check for any `java.util.Date` → import issues or deprecated API usage in: `com.skishop.domain.address.Address`, `com.skishop.domain.cart.{Cart, CartItem}`, `com.skishop.domain.coupon.{Campaign, Coupon, CouponUsage}`, `com.skishop.domain.inventory.Inventory`, `com.skishop.domain.mail.EmailQueue`, `com.skishop.domain.order.{Order, OrderItem, OrderShipping, Return, Shipment}`, `com.skishop.domain.payment.Payment`, `com.skishop.domain.point.{PointAccount, PointTransaction}`, `com.skishop.domain.product.{Category, Price, Product}`, `com.skishop.domain.shipping.ShippingMethod`, `com.skishop.domain.user.{PasswordResetToken, Role, SecurityLog, User}`.
- [x] T114 [P] [Plan:1.5] Fix any Java 21 compilation issues in domain POJOs (e.g., update deprecated `Date` constructors, raw type usage). Preserve all field types, getter/setter signatures, and class structure exactly.

### 1.6 DAO Interface Preservation + JdbcTemplate Implementation

- [x] T115 [P] [Plan:1.6] Rewrite `UserDaoImpl` (`com.skishop.dao.user.UserDaoImpl`): replace `extends AbstractDao` + commons-dbutils with `@Repository` + constructor-injected `JdbcTemplate`. Preserve `UserDao` interface. Preserve exact SQL queries. Use `BeanPropertyRowMapper<User>` or custom `RowMapper`.
- [x] T116 [P] [Plan:1.6] Rewrite `SecurityLogDaoImpl` (`com.skishop.dao.user.SecurityLogDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `SecurityLogDao` interface and SQL queries.
- [x] T117 [P] [Plan:1.6] Rewrite `PasswordResetTokenDaoImpl` (`com.skishop.dao.user.PasswordResetTokenDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `PasswordResetTokenDao` interface and SQL queries.
- [x] T118 [P] [Plan:1.6] Rewrite `ProductDaoImpl` (`com.skishop.dao.product.ProductDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `ProductDao` interface and SQL queries.
- [x] T119 [P] [Plan:1.6] Rewrite `PriceDaoImpl` (`com.skishop.dao.product.PriceDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `PriceDao` interface and SQL queries.
- [x] T120 [P] [Plan:1.6] Rewrite `CategoryDaoImpl` (`com.skishop.dao.category.CategoryDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `CategoryDao` interface and SQL queries.
- [x] T121 [P] [Plan:1.6] Rewrite `CartDaoImpl` (`com.skishop.dao.cart.CartDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `CartDao` interface and SQL queries.
- [x] T122 [P] [Plan:1.6] Rewrite `OrderDaoImpl` (`com.skishop.dao.order.OrderDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `OrderDao` interface and SQL queries.
- [x] T123 [P] [Plan:1.6] Rewrite `OrderShippingDaoImpl` (`com.skishop.dao.order.OrderShippingDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `OrderShippingDao` interface and SQL queries.
- [x] T124 [P] [Plan:1.6] Rewrite `ReturnDaoImpl` (`com.skishop.dao.order.ReturnDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `ReturnDao` interface and SQL queries.
- [x] T125 [P] [Plan:1.6] Rewrite `PaymentDaoImpl` (`com.skishop.dao.payment.PaymentDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `PaymentDao` interface and SQL queries.
- [x] T126 [P] [Plan:1.6] Rewrite `CouponDaoImpl` (`com.skishop.dao.coupon.CouponDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `CouponDao` interface and SQL queries.
- [x] T127 [P] [Plan:1.6] Rewrite `CouponUsageDaoImpl` (`com.skishop.dao.coupon.CouponUsageDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `CouponUsageDao` interface and SQL queries.
- [x] T128 [P] [Plan:1.6] Rewrite `InventoryDaoImpl` (`com.skishop.dao.inventory.InventoryDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `InventoryDao` interface and SQL queries.
- [x] T129 [P] [Plan:1.6] Rewrite `PointAccountDaoImpl` (`com.skishop.dao.point.PointAccountDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `PointAccountDao` interface and SQL queries.
- [x] T130 [P] [Plan:1.6] Rewrite `PointTransactionDaoImpl` (`com.skishop.dao.point.PointTransactionDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `PointTransactionDao` interface and SQL queries.
- [x] T131 [P] [Plan:1.6] Rewrite `UserAddressDaoImpl` (`com.skishop.dao.address.UserAddressDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `UserAddressDao` interface and SQL queries.
- [x] T132 [P] [Plan:1.6] Rewrite `ShippingMethodDaoImpl` (`com.skishop.dao.shipping.ShippingMethodDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `ShippingMethodDao` interface and SQL queries.
- [x] T133 [P] [Plan:1.6] Rewrite `EmailQueueDaoImpl` (`com.skishop.dao.mail.EmailQueueDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `EmailQueueDao` interface and SQL queries.

### 1.7 Service Layer Spring Conversion

- [x] T134 [P] [Plan:1.7] Convert `AuthService` (`com.skishop.service.auth.AuthService`): add `@Service`, replace `new UserDaoImpl()` / `DaoFactory` / `ServiceLocator` calls with constructor injection of `UserDao`, `SecurityLogDao`. Add `@Transactional` if multi-DAO operations exist.
- [x] T135 [P] [Plan:1.7] Convert `UserService` (`com.skishop.service.user.UserService`): add `@Service`, replace direct DAO instantiation with constructor injection of `UserDao`. Add `@Transactional` where needed.
- [x] T136 [P] [Plan:1.7] Convert `ProductService` (`com.skishop.service.catalog.ProductService`): add `@Service`, replace direct DAO instantiation with constructor injection of `ProductDao`, `PriceDao`. Add `@Transactional` where needed.
- [x] T137 [P] [Plan:1.7] Convert `CategoryService` (`com.skishop.service.catalog.CategoryService`): add `@Service`, constructor injection of `CategoryDao`.
- [x] T138 [P] [Plan:1.7] Convert `CartService` (`com.skishop.service.cart.CartService`): add `@Service`, constructor injection of `CartDao`. Add `@Transactional` where needed.
- [x] T139 [P] [Plan:1.7] Convert `OrderService` (`com.skishop.service.order.OrderService`): add `@Service`, constructor injection of `OrderDao`, `OrderShippingDao`. Add `@Transactional` for multi-DAO operations.
- [x] T140 [P] [Plan:1.7] Convert `OrderFacadeImpl` (`com.skishop.service.order.OrderFacadeImpl`): add `@Service`, constructor injection of all coordinated services (OrderService, InventoryService, PaymentService, PointService, ShippingService, CartService, CouponService, MailService). Add `@Transactional` for checkout orchestration.
- [x] T141 [P] [Plan:1.7] Convert `PaymentService` (`com.skishop.service.payment.PaymentService`): add `@Service`, constructor injection of `PaymentDao`. Add `@Transactional` where needed.
- [x] T142 [P] [Plan:1.7] Convert `CouponService` (`com.skishop.service.coupon.CouponService`): add `@Service`, constructor injection of `CouponDao`, `CouponUsageDao`. Add `@Transactional` where needed.
- [x] T143 [P] [Plan:1.7] Convert `InventoryService` (`com.skishop.service.inventory.InventoryService`): add `@Service`, constructor injection of `InventoryDao`. Add `@Transactional` where needed.
- [x] T144 [P] [Plan:1.7] Convert `PointService` (`com.skishop.service.point.PointService`): add `@Service`, constructor injection of `PointAccountDao`, `PointTransactionDao`. Add `@Transactional` for multi-DAO operations.
- [x] T145 [P] [Plan:1.7] Convert `ShippingService` (`com.skishop.service.shipping.ShippingService`): add `@Service`, constructor injection of `ShippingMethodDao`, `OrderShippingDao`.
- [x] T146 [P] [Plan:1.7] Convert `MailService` (`com.skishop.service.mail.MailService`): add `@Service`, constructor injection of `EmailQueueDao`. Replace any direct SMTP code with Spring `JavaMailSender` injection.
- [x] T147 [P] [Plan:1.7] Convert `TaxService` (`com.skishop.service.tax.TaxService`): add `@Service`, constructor injection of any dependencies.

### 1.8 Compile Gate

- [x] T148 [Plan:1.8] Run `mvn -B compile` and verify zero compilation errors. All classes must compile against Spring Boot dependencies with no Struts, commons-dbcp, commons-dbutils, or log4j imports remaining in production code.
- [x] T149 [Plan:1.8] Verify no residual references to deleted infrastructure: grep production source for `ServiceLocator`, `DaoFactory`, `DataSourceLocator`, `DataSourceFactory`, `AbstractDao`, `DaoException`, `AppConfig`, `struts`, `commons.dbcp`, `commons.dbutils`, `log4j`. All must return zero hits.

---

## Phase 2: Cross-Cutting Infrastructure

**Batch**: 2
**Goal**: Implement authentication, CSRF, error handling, layout, and filter infrastructure before any controller.
**Exit Criteria**: Auth + CSRF interceptors functional. Layout template renders. Error handler catches exceptions. `mvn -B compile` passes.

---

### 2.1 AuthInterceptor

- [x] T200 [Plan:2.1] Create `AuthInterceptor` implementing `HandlerInterceptor` in `src/main/java/com/skishop/web/interceptor/AuthInterceptor.java`. In `preHandle`: check `HttpSession` for `loginUser` attribute; if null on a protected path, redirect to `/login` and return `false`. If user present but role insufficient, send 403. Define URL→role mapping derived from struts-config.xml: `/cart/**`, `/checkout/**`, `/orders/**`, `/points/**`, `/coupons/apply`, `/account/**` require role `USER` or `ADMIN`; `/admin/**` requires role `ADMIN`. Public paths (`/home`, `/login`, `/register`, `/products`, `/product`, `/logout`, `/password/**`, `/coupons` (GET list)) must pass through without auth check.
- [x] T201 [P] [Plan:2.1] Create unit test `src/test/java/com/skishop/web/interceptor/AuthInterceptorTest.java`. Test scenarios: (a) unauthenticated request to protected path → redirect to `/login`; (b) authenticated USER accessing USER path → allowed; (c) authenticated USER accessing ADMIN path → 403; (d) authenticated ADMIN accessing ADMIN path → allowed; (e) request to public path (no session) → allowed.

### 2.2 CsrfInterceptor

- [x] T202 [Plan:2.2] Create `CsrfInterceptor` implementing `HandlerInterceptor` in `src/main/java/com/skishop/web/interceptor/CsrfInterceptor.java`. In `preHandle`: on non-POST requests, generate a UUID-based CSRF token and store in `HttpSession` under key `_csrfToken` if not already present. On POST requests: read `_csrfToken` parameter from request; compare against session token; if missing or mismatched, send 403 and return `false`; on match, reset (remove) the session token so it is regenerated on next GET. This matches the existing `AuthRequestProcessor` + `TokenProcessor` behavior.
- [x] T203 [P] [Plan:2.2] Create unit test `src/test/java/com/skishop/web/interceptor/CsrfInterceptorTest.java`. Test scenarios: (a) GET request generates token in session when missing; (b) GET request preserves existing token; (c) POST with valid token → allowed, token reset; (d) POST with invalid token → 403; (e) POST with no token parameter → 403.

### 2.3 CsrfTokenTag (Custom JSP Tag)

- [x] T204 [Plan:2.3] Create `CsrfTokenTag` JSP tag class in `src/main/java/com/skishop/web/tag/CsrfTokenTag.java`. Extends `TagSupport`. In `doStartTag`: read `_csrfToken` from `HttpSession`; write `<input type="hidden" name="_csrfToken" value="{token}" />` to `JspWriter`. This replaces the existing Struts-dependent `TokenTag` that uses `Globals.TRANSACTION_TOKEN_KEY` and `Constants.TOKEN_KEY`. Remove the old `TokenTag.java` file (or overwrite it).
- [x] T205 [Plan:2.3] Create TLD file `src/main/webapp/WEB-INF/tags/skishop.tld`. Register `CsrfTokenTag` with tag name `csrfToken` under URI `http://skishop.com/tags` (or similar). JSPs will use `<%@ taglib uri="http://skishop.com/tags" prefix="skishop" %>` and `<skishop:csrfToken/>` to output the hidden field.

### 2.4 WebMvcConfig

- [x] T206 [Plan:2.4] Create `WebMvcConfig` `@Configuration` class in `src/main/java/com/skishop/web/config/WebMvcConfig.java` implementing `WebMvcConfigurer`. Override `addInterceptors(InterceptorRegistry)`: register `CsrfInterceptor` for `/**` (all paths); register `AuthInterceptor` for `/**` excluding static resources (`/assets/**`). Both interceptors should be Spring-managed beans (use `@Component` or `@Bean` factory methods). Configure interceptor ordering: CsrfInterceptor before AuthInterceptor.

### 2.5 GlobalExceptionHandler

- [x] T207 [Plan:2.5] Create `GlobalExceptionHandler` with `@ControllerAdvice` in `src/main/java/com/skishop/web/handler/GlobalExceptionHandler.java`. Add `@ExceptionHandler(Exception.class)` method: log the exception, set `errorMessage` model attribute, return view name `error/general`. This replicates the Struts `global-exceptions` entry that maps `java.lang.Exception` to `/error.jsp`.
- [x] T208 [Plan:2.5] Create `src/main/webapp/WEB-INF/jsp/error/general.jsp`. Include base layout via `<jsp:include>`. Display `${errorMessage}` in the page body. Set page title to error label from messages.properties.
- [x] T209 [P] [Plan:2.5] Create unit test `src/test/java/com/skishop/web/handler/GlobalExceptionHandlerTest.java`. Test: (a) unhandled `RuntimeException` → resolves to `error/general` view with `errorMessage` attribute; (b) `IllegalArgumentException` → same error view.

### 2.6 JSP Layout Template

- [x] T210 [Plan:2.6] Rewrite `src/main/webapp/WEB-INF/jsp/layouts/base.jsp`. Remove all Tiles taglibs (`tiles:getAsString`, `tiles:insert`) and Struts taglibs (`html:rewrite`). Replace with: `<title>${pageTitle}</title>` (set by controllers as request attribute); `<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>` for header; `<jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>` for messages; `<jsp:include page="${bodyContent}"/>` for body (controllers set `bodyContent` request attribute); `<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>` for footer. Use `<c:url value='/assets/css/app.css'/>` for static resource link.
- [x] T211 [P] [Plan:2.6] Rewrite `src/main/webapp/WEB-INF/jsp/common/header.jsp`. Remove Struts taglibs. Use JSTL: `<c:url>` for navigation links, `<c:if test="${not empty sessionScope.loginUser}">` for user-specific nav (logout, account), `<c:if test="${sessionScope.loginUser.role == 'ADMIN'}">` for admin links.
- [x] T212 [P] [Plan:2.6] Rewrite `src/main/webapp/WEB-INF/jsp/common/footer.jsp`. Remove any Struts tag references. Use plain HTML and JSTL if needed.
- [x] T213 [P] [Plan:2.6] Rewrite `src/main/webapp/WEB-INF/jsp/common/messages.jsp`. Replace `<html:errors/>` and `<logic:messagesPresent>` with JSTL: use `<c:if test="${not empty errors}"><div class="messages"><c:forEach items="${errors}" var="msg">...</c:forEach></div></c:if>`. Support both error and success message display from model attributes set by controllers.

### 2.7 RequestIdFilter Conversion

- [x] T214 [Plan:2.7] Rewrite `src/main/java/com/skishop/web/filter/RequestIdFilter.java` as Spring `@Component` implementing `jakarta.servlet.Filter`. Update all imports from `javax.servlet.*` to `jakarta.servlet.*`. Replace `org.apache.log4j.MDC` with `org.slf4j.MDC`. Preserve existing behavior: read `X-Request-Id` header, fall back to UUID, set MDC key `reqId`, set response header, clean up MDC in `finally` block.
- [x] T215 [P] [Plan:2.7] Add UTF-8 encoding configuration to `src/main/resources/application.properties`: `server.servlet.encoding.charset=UTF-8`, `server.servlet.encoding.enabled=true`, `server.servlet.encoding.force=true`. This replaces the `CharacterEncodingFilter` from `web.xml`.

### 2.8 MessageSource Configuration

- [x] T216 [Plan:2.8] Configure `MessageSource` bean. Add a `@Bean` method in `WebMvcConfig` (or a dedicated `MessageConfig` class) returning `ReloadableResourceBundleMessageSource` with basename `classpath:messages` and default encoding `UTF-8`. This enables `<spring:message>` tag in JSPs and `MessageSource` injection in controllers for resolving keys from `messages.properties`.

### 2.9 PasswordHasher Preservation

- [x] T217 [Plan:2.9] Verify and update `src/main/java/com/skishop/common/util/PasswordHasher.java` for JDK 21 compilation. Replace the `"UTF-8"` string in `getBytes("UTF-8")` with `java.nio.charset.StandardCharsets.UTF_8` to eliminate `UnsupportedEncodingException`. Update the `hash()` method signature if needed (remove checked exception). Preserve all field values (`HASH_ITERATIONS = 1000`), algorithm (`SHA-256`), salt generation, matching logic, and constant-time comparison exactly.

### 2.10 Compile + Unit Test Gate

- [x] T218 [Plan:2.10] Run `mvn -B compile` — verify all Phase 2 source code (interceptors, tags, config, handler, filter, layout JSPs) compiles with zero errors.
- [x] T219 [Plan:2.10] Run `mvn -B test -pl . -Dtest="AuthInterceptorTest,CsrfInterceptorTest,GlobalExceptionHandlerTest"` — verify all Phase 2 unit tests pass. Fix any failures before proceeding to Phase 3.

---

## Phase 3: P1 Features — Product Catalog, Auth, Cart & Checkout

**Batch**: 3
**Goal**: Implement all Priority 1 features: product browsing, authentication, cart, checkout.
**User Stories**: US-1 (Browse Products), US-2 (Registration & Auth), US-3 (Cart & Checkout)
**Task ID Range**: T300–T363

---

### 3.1 HomeController

- [x] T300 [US-1] [Plan:3.1] Create `HomeController` `@Controller` class in `src/main/java/com/skishop/web/controller/HomeController.java`. Map `GET /home` → return view name `home`. No service dependencies. No business logic.
- [x] T301 [P] [US-1] [Plan:3.1] Create unit test `src/test/java/com/skishop/web/controller/HomeControllerTest.java`. Use `MockMvc` to verify `GET /home` returns HTTP 200 and resolves to `home` view.

---

### 3.2 ProductController

- [x] T302 [US-1] [Plan:3.2] Create `ProductController` `@Controller` class in `src/main/java/com/skishop/web/controller/ProductController.java`. Inject `ProductService` and `CategoryService` via constructor injection.
- [x] T303 [US-1] [Plan:3.2] Implement `GET /products` handler in `ProductController`. Accept `@RequestParam` for `page` (default 1), `size` (default 10), `keyword`, `categoryId`, `sort`. Compute offset as `(page-1)*size`. Call `productService.search(keyword, categoryId, sort, offset, size)` and `categoryService.listAll()`. Build category options list (add "指定なし" empty-value option first). Set model attributes: `productList`, `categoryOptions`, `page`, `size`. Return view name `products/list`. Replicate `ProductListAction` behavior exactly.
- [x] T304 [US-1] [Plan:3.2] Implement `GET /product` handler in `ProductController`. Accept `@RequestParam String id`. If `id` is null/empty or `productService.findById(id)` returns null → return view name `products/notfound`. Otherwise set `product` model attribute and return view name `products/detail`. Replicate `ProductDetailAction` behavior exactly.
- [x] T305 [P] [US-1] [Plan:3.2] Create unit test `src/test/java/com/skishop/web/controller/ProductControllerTest.java`. Test scenarios: (a) `GET /products` with no params → 200 with default pagination; (b) `GET /products?keyword=Atomic&categoryId=c-1&page=2` → 200 with filtered results; (c) `GET /product?id=PSK001` → 200 with product detail view; (d) `GET /product?id=NONEXISTENT` → 200 with `products/notfound` view; (e) `GET /product` with no id param → `products/notfound` view.

---

### 3.3 LoginController

- [x] T306 [US-2] [Plan:3.3] Create `LoginController` `@Controller` class in `src/main/java/com/skishop/web/controller/LoginController.java`. Inject `AuthService` via constructor injection.
- [x] T307 [US-2] [Plan:3.3] Implement `GET /login` handler in `LoginController`. Return view name `auth/login`. No model attributes needed.
- [x] T308 [US-2] [Plan:3.3] Implement `POST /login` handler in `LoginController`. Accept `@ModelAttribute LoginForm`, `HttpServletRequest`, `HttpSession`, `Model`. Call `authService.authenticate(form.getEmail(), form.getPassword(), request.getRemoteAddr(), request.getHeader("User-Agent"))`. On failure: add error message attribute, return view `auth/login`. On success: invalidate old session, create new session, set `loginUser` session attribute to `result.getUser()`, redirect to `/home`.
- [x] T309 [P] [US-2] [Plan:3.3] Create unit test `src/test/java/com/skishop/web/controller/LoginControllerTest.java`. Test scenarios: (a) `GET /login` → 200 with login view; (b) `POST /login` valid credentials → redirect to `/home` with session `loginUser` set; (c) `POST /login` invalid credentials → 200 with `auth/login` view and error message.

---

### 3.4 RegisterController

- [x] T310 [US-2] [Plan:3.4] Create `RegisterController` `@Controller` class in `src/main/java/com/skishop/web/controller/RegisterController.java`. Inject `UserService` via constructor injection.
- [x] T311 [US-2] [Plan:3.4] Implement `GET /register` handler in `RegisterController`. Return view name `auth/register`.
- [x] T312 [US-2] [Plan:3.4] Implement `POST /register` handler in `RegisterController`. Accept `@ModelAttribute RegisterForm`, `BindingResult`, `Model`. Validate form (required fields: email, username, password, passwordConfirm; password match check). Check `userService.findByEmail(email)` for duplicate. On validation/duplicate error: add errors to model, return `auth/register`. On success: create `User` with `PasswordHasher.generateSalt()` / `PasswordHasher.hash()`, call `userService.register(user)`, redirect to `/login`.
- [x] T313 [P] [US-2] [Plan:3.4] Create unit test `src/test/java/com/skishop/web/controller/RegisterControllerTest.java`. Test scenarios: (a) `GET /register` → 200 with register view; (b) `POST /register` valid data → redirect to `/login`; (c) `POST /register` duplicate email → 200 with error; (d) `POST /register` password mismatch → 200 with validation error; (e) `POST /register` missing required fields → 200 with validation errors.

---

### 3.5 LogoutController

- [x] T314 [US-2] [Plan:3.5] Create `LogoutController` `@Controller` class in `src/main/java/com/skishop/web/controller/LogoutController.java`. Implement `GET /logout` handler: invalidate session if present, redirect to `/home`. No service dependencies. Replicate `LogoutAction` behavior.
- [x] T315 [P] [US-2] [Plan:3.5] Create unit test `src/test/java/com/skishop/web/controller/LogoutControllerTest.java`. Test scenarios: (a) `GET /logout` with active session → session invalidated, redirect to `/home`; (b) `GET /logout` with no session → redirect to `/home`.

---

### 3.6 CartController

- [x] T316 [US-3] [Plan:3.6] Create `CartController` `@Controller` class in `src/main/java/com/skishop/web/controller/CartController.java`. Inject `CartService` via constructor injection.
- [x] T317 [US-3] [Plan:3.6] Implement `GET /cart` handler in `CartController`. Get or create cart via session: read `cartId` from session; if null, call `cartService.createCart(userId, sessionId)` and store `cartId` in session + set `CART_ID` HttpOnly cookie (Max-Age 30 days). Retrieve items via `cartService.getItems(cartId)`, compute subtotal via `cartService.calculateSubtotal(items)`. Set model attributes: `cartItems`, `cartSubtotal`, `cartId`. Return view `cart/view`. Replicate `CartAction` behavior including cookie logic.
- [x] T318 [US-3] [Plan:3.6] Implement `POST /cart` handler in `CartController`. Accept `@ModelAttribute AddCartForm`. Read/create `cartId` from session (same logic as GET). If `productId` is non-empty, call `cartService.addItem(cartId, productId, quantity)`. Redirect to `/cart`.
- [x] T319 [P] [US-3] [Plan:3.6] Create unit test `src/test/java/com/skishop/web/controller/CartControllerTest.java`. Test scenarios: (a) `GET /cart` no existing session cart → creates cart, sets session attribute, returns view; (b) `GET /cart` with existing cart → shows items; (c) `POST /cart` with valid productId → adds item, redirects to `/cart`; (d) `POST /cart` with empty productId → redirects to `/cart` without adding.

---

### 3.7 CouponController

- [x] T320 [US-3] [Plan:3.7] Create `CouponController` `@Controller` class in `src/main/java/com/skishop/web/controller/CouponController.java`. Inject `CouponService` and `CartService` via constructor injection.
- [x] T321 [US-3] [Plan:3.7] Implement `GET /coupons` handler in `CouponController`. Call `couponService.listActiveCoupons()`, set `coupons` model attribute, return view `coupons/available`. This is a public endpoint (no auth required). Replicate `CouponAvailableAction`.
- [x] T322 [US-3] [Plan:3.7] Implement `POST /coupons/apply` handler in `CouponController`. Requires authentication (enforced by `AuthInterceptor`). Accept `@ModelAttribute CouponForm`, `HttpSession`, `Model`. Read `cartId` from session; if null → add error, redirect to `/cart`. Get cart items + subtotal, call `couponService.validateCoupon(code, subtotal)`. On success: compute discount via `couponService.calculateDiscount(coupon, subtotal)`, store coupon info in session, set model attributes (`cartItems`, `cartSubtotal`, `coupon`, `discountAmount`), return view `cart/view`. On `IllegalArgumentException`: add error message, set cart model attributes, return view `cart/view`. Replicate `CouponApplyAction` behavior.
- [x] T323 [P] [US-3] [Plan:3.7] Create unit test `src/test/java/com/skishop/web/controller/CouponControllerTest.java`. Test scenarios: (a) `GET /coupons` → 200 with coupons list; (b) `POST /coupons/apply` valid coupon → success with discount; (c) `POST /coupons/apply` invalid coupon → error message; (d) `POST /coupons/apply` no cart in session → error.

---

### 3.8 CheckoutController

- [x] T324 [US-3] [Plan:3.8] Create `CheckoutController` `@Controller` class in `src/main/java/com/skishop/web/controller/CheckoutController.java`. Inject `OrderFacade` via constructor injection.
- [x] T325 [US-3] [Plan:3.8] Implement `GET /checkout` handler in `CheckoutController`. Requires authentication (enforced by `AuthInterceptor`). Return view `cart/checkout`.
- [x] T326 [US-3] [Plan:3.8] Implement `POST /checkout` handler in `CheckoutController`. Accept `@ModelAttribute CheckoutForm`, `HttpSession`, `Model`. Read `cartId` from session (fall back to `checkoutForm.getCartId()`). Get `loginUser` from session for `userId`. Call `orderFacade.placeOrder(cartId, checkoutForm.getCouponCode(), checkoutForm.getUsePoints(), checkoutForm.toPaymentInfo(), userId)`. On success: set `order` model attribute, return view `cart/confirmation`. On `RuntimeException`: add error message, return view `cart/checkout`. Replicate `CheckoutAction` behavior.
- [x] T327 [P] [US-3] [Plan:3.8] Create unit test `src/test/java/com/skishop/web/controller/CheckoutControllerTest.java`. Test scenarios: (a) `GET /checkout` → 200 with checkout view; (b) `POST /checkout` successful order → 200 with confirmation view and order; (c) `POST /checkout` failure (RuntimeException from facade) → 200 with checkout view and error.

---

### 3.9 Form Objects (P1)

- [x] T328 [US-2] [Plan:3.9] Rewrite `LoginForm` in `src/main/java/com/skishop/web/form/LoginForm.java`. Remove `ValidatorForm` extends. Convert to plain POJO. Add `@NotBlank` Bean Validation annotations on `email` and `password` fields. Keep getters/setters.
- [x] T329 [US-2] [Plan:3.9] Rewrite `RegisterForm` in `src/main/java/com/skishop/web/form/RegisterForm.java`. Remove `ValidatorForm` extends. Convert to plain POJO. Add `@NotBlank` on `email`, `username`, `password`, `passwordConfirm`. Remove Struts `validate()` method. Password match validation will be done in controller (programmatic check or custom validator).
- [x] T330 [US-1] [Plan:3.9] Rewrite `ProductSearchForm` in `src/main/java/com/skishop/web/form/ProductSearchForm.java`. Remove `ValidatorForm` extends. Convert to plain POJO. Fields: `page` (int), `size` (int), `keyword` (String), `categoryId` (String), `sort` (String). No validation annotations needed (all optional with defaults).
- [x] T331 [US-3] [Plan:3.9] Rewrite `AddCartForm` in `src/main/java/com/skishop/web/form/AddCartForm.java`. Remove `ValidatorForm` extends. Convert to plain POJO. Add `@NotBlank` on `productId`, ensure `quantity` defaults to 1.
- [x] T332 [US-3] [Plan:3.9] Rewrite `CheckoutForm` in `src/main/java/com/skishop/web/form/CheckoutForm.java`. Remove `ValidatorForm` extends. Convert to plain POJO. Add `@NotBlank` on `cardNumber`, `cardExpMonth`, `cardExpYear`, `cardCvv`, `billingZip`. Preserve `toPaymentInfo()` method. Remove Struts `validate()` method.
- [x] T333 [US-3] [Plan:3.9] Rewrite `CouponForm` in `src/main/java/com/skishop/web/form/CouponForm.java`. Remove `ValidatorForm` extends (if present). Convert to plain POJO. Add `@NotBlank` on `code` field.
- [x] T334 [P] [Plan:3.9] Remove `FormValidationUtils.java` dependency on Struts types. Update `src/main/java/com/skishop/web/form/FormValidationUtils.java` to use Spring `Errors`/`BindingResult` instead of `ActionErrors` for password mismatch validation helper, or inline the logic into controllers that need it.

---

### 3.10 JSP Views (P1)

- [x] T335 [US-1] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/home.jsp`. Replace any Struts tag libraries (`<html:*>`, `<bean:*>`, `<logic:*>`) with JSTL (`<c:*>`, `<fmt:*>`) and Spring form tags. Ensure it renders within the base layout template (`layouts/base.jsp`) via `<jsp:include>` or SiteMesh/Tiles-equivalent approach established in Phase 2.
- [x] T336 [US-1] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/products/list.jsp`. Replace Struts iteration (`<logic:iterate>`) with `<c:forEach>`. Replace `<bean:write>` with `<c:out>`. Replace Struts form tags with Spring/JSTL equivalents. Ensure pagination links use `page`, `size`, `keyword`, `categoryId` query params. Category filter dropdown uses `categoryOptions` model attribute.
- [x] T337 [US-1] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/products/detail.jsp`. Replace Struts tags with JSTL. Display product name, description, price, category from `product` model attribute. Include "Add to Cart" form posting to `/cart` with `productId` and `quantity` fields plus `<skishop:csrfToken/>`.
- [x] T338 [P] [US-1] [Plan:3.10] Verify `src/main/webapp/WEB-INF/jsp/products/notfound.jsp` has no Struts tag dependencies. Update if needed to use JSTL only.
- [x] T339 [US-2] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/auth/login.jsp`. Replace `<html:form>` with `<form>` posting to `/login`. Replace `<html:text>`, `<html:password>` with standard `<input>` tags. Add `<skishop:csrfToken/>` hidden field. Display error messages from model attribute using `<c:if>` / `<c:out>`.
- [x] T340 [US-2] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/auth/register.jsp`. Replace `<html:form>` with `<form>` posting to `/register`. Replace Struts input tags with standard HTML inputs. Add `<skishop:csrfToken/>`. Display validation errors using Spring `<form:errors>` or JSTL conditionals.
- [x] T341 [US-3] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/cart/view.jsp`. Replace Struts iteration with `<c:forEach>` for `cartItems`. Display subtotal, discount (if coupon applied), cart total. Include coupon apply form (POST to `/coupons/apply` with `code` field + CSRF token). Include link to `/checkout`.
- [x] T342 [US-3] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/cart/checkout.jsp`. Replace `<html:form>` with `<form>` posting to `/checkout`. Include payment fields: `cardNumber`, `cardExpMonth`, `cardExpYear`, `cardCvv`, `billingZip`, `usePoints`. Add `<skishop:csrfToken/>`. Display errors from model attribute.
- [x] T343 [US-3] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/cart/confirmation.jsp`. Replace Struts tags with JSTL. Display `order` model attributes: order number, total, status. No form needed.
- [x] T344 [US-3] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/coupons/available.jsp`. Replace Struts iteration with `<c:forEach>` for `coupons` list. Display coupon code, type, discount value, expiration. No form needed (read-only listing).

---

### 3.11 Compile + Test Gate

- [x] T345 [Plan:3.11] Run `mvn -B compile` and fix any compilation errors in Phase 3 controllers, forms, and JSPs. Ensure all new `@Controller` classes are component-scanned. Ensure all injected services (`ProductService`, `CategoryService`, `AuthService`, `UserService`, `CartService`, `CouponService`, `OrderFacade`) are available as Spring beans (annotated with `@Service` or `@Component`).
- [x] T346 [Plan:3.11] Verify service layer Spring compatibility. Ensure `ProductService`, `CategoryService`, `AuthService`, `UserService`, `CartService`, `CouponService`, `OrderFacade`/`OrderFacadeImpl` have `@Service` annotation and use constructor injection for their DAO dependencies (instead of direct instantiation). This is required for controllers to inject them.
- [x] T347 [Plan:3.11] Delete Struts Action classes replaced by Phase 3 controllers: `ProductListAction.java`, `ProductDetailAction.java`, `LoginAction.java`, `RegisterAction.java`, `LogoutAction.java`, `CartAction.java`, `CouponApplyAction.java`, `CouponAvailableAction.java`, `CheckoutAction.java` from `src/main/java/com/skishop/web/action/`.
- [x] T348 [Plan:3.11] Delete or migrate Struts test classes replaced by Phase 3 controller tests: `ProductListActionTest.java`, `ProductDetailActionTest.java`, `LoginActionTest.java`, `RegisterActionTest.java`, `LogoutActionTest.java`, `CartActionTest.java`, `CouponApplyActionTest.java`, `CheckoutActionTest.java` from `src/test/java/com/skishop/web/action/`.
- [x] T349 [Plan:3.11] Run all Phase 3 controller unit tests. Verify: HomeController, ProductController, LoginController, RegisterController, LogoutController, CartController, CouponController, CheckoutController tests pass.
- [x] T350 [Plan:3.11] End-to-end smoke test: verify key P1 flows work — `GET /home` (200), `GET /products` (200 with list), `GET /product?id=PSK001` (200 with detail), `GET /login` (200), `POST /login` (redirect), `GET /cart` (200), `GET /coupons` (200), `GET /checkout` (200 when authenticated). Run `mvn -B compile` one final time to confirm clean build.

---

## Phase 4: P2 Features — Orders, Addresses, Points, Password Reset, Email

**Batch**: 4
**Goal**: Implement all Priority 2 features: order history/detail/cancel/return, address management, point balance, password reset flow, and Spring mail service conversion.
**User Stories**: US-4 (Order Management), US-5 (Address Management), US-6 (Password Reset), US-7 (Points/Loyalty)
**Task ID Range**: T400–T442

---

### 4.1 OrderController

- [x] T400 [US-4] [Plan:4.1] Create `OrderController` `@Controller` class in `src/main/java/com/skishop/web/controller/OrderController.java`. Inject `OrderService` and `OrderFacade` via constructor injection. All handler methods must require authenticated user (USER or ADMIN role) — redirect to `/login` if session `loginUser` is null.
- [x] T401 [US-4] [Plan:4.1] Implement `GET /orders` handler in `OrderController`. Retrieve `loginUser` from session. If user role is ADMIN, call `orderService.listAll(50)`; otherwise call `orderService.listByUserId(user.getId())`. Set `orders` model attribute. Return view name `orders/history`. Replicate `OrderHistoryAction` behavior exactly.
- [x] T402 [US-4] [Plan:4.1] Implement `GET /orders/detail` handler in `OrderController`. Accept `@RequestParam String id`. Validate: if `id` is null/empty, return view `orders/detail` with no data. Call `orderService.findById(id)`. Verify ownership: `user.getId().equals(order.getUserId())` (skip check for ADMIN). If valid, call `orderService.listItems(id)` and set model attributes `order` and `orderItems`. Return view name `orders/detail`. Replicate `OrderDetailAction` behavior, noting the Struts action uses param name `orderId` — the Spring contract uses `id`.
- [x] T403 [US-4] [Plan:4.1] Implement `POST /orders/cancel` handler in `OrderController`. Accept `@RequestParam String id`. Validate `id` not null/empty — on failure, add error message to redirect attributes and redirect to `/orders`. Call `orderFacade.cancelOrder(id, user.getId())`. On success, redirect to `/orders`. On `RuntimeException`, add flash error message and redirect to `/orders`. Replicate `OrderCancelAction` behavior.
- [x] T404 [US-4] [Plan:4.1] Implement `POST /orders/return` handler in `OrderController`. Accept `@RequestParam String id` and `@RequestParam(required=false) String reason`. Validate `id` not null/empty. Call `orderFacade.returnOrder(id, user.getId())`. On success, redirect to `/orders`. On `RuntimeException`, add flash error and redirect to `/orders`. Replicate `OrderReturnAction` behavior.
- [x] T405 [P] [US-4] [Plan:4.1] Create unit test `src/test/java/com/skishop/web/controller/OrderControllerTest.java`. Test scenarios: (a) `GET /orders` unauthenticated → redirect to login; (b) `GET /orders` as USER → 200 with order history for that user; (c) `GET /orders` as ADMIN → 200 with all orders; (d) `GET /orders/detail?id=ORD-1` as owner → 200 with order + items; (e) `GET /orders/detail?id=ORD-1` as non-owner → 200 with no order data; (f) `POST /orders/cancel` with valid id → redirect to `/orders`; (g) `POST /orders/cancel` with empty id → redirect with error; (h) `POST /orders/return` with valid id → redirect to `/orders`.

---

### 4.2 AddressController

- [x] T406 [US-5] [Plan:4.2] Create `AddressController` `@Controller` class in `src/main/java/com/skishop/web/controller/AddressController.java`. Inject `AddressService` via constructor injection. All handlers require authenticated user.
- [x] T407 [US-5] [Plan:4.2] Implement `GET /account/addresses` handler in `AddressController`. Retrieve user from session. Call `addressService.listByUserId(user.getId())`. Set `addresses` model attribute. Return view name `account/addresses`.
- [x] T408 [US-5] [Plan:4.2] Implement `GET /account/addresses/edit` handler in `AddressController`. Accept `@RequestParam(required=false) String id`. If `id` is present, load existing address via `addressService.findById(id)` and verify ownership. Set `addressForm` model attribute (populated for edit, empty for new). Return view name `account/address_edit`.
- [x] T409 [US-5] [Plan:4.2] Implement `POST /account/addresses/save` handler in `AddressController`. Accept `@Valid @ModelAttribute AddressForm`, `BindingResult`, `HttpSession`, `RedirectAttributes`. On validation errors, return `account/address_edit`. Check address count limit (MAX 10 per user) — if exceeded, add error and return `account/address_edit`. Build `Address` domain object from form, set `userId` from session user, generate UUID if new. Call `addressService.save(address)`. Redirect to `/account/addresses`. Replicate `AddressSaveAction` behavior including the 10-address limit check.
- [x] T410 [P] [US-5] [Plan:4.2] Create unit test `src/test/java/com/skishop/web/controller/AddressControllerTest.java`. Test scenarios: (a) `GET /account/addresses` unauthenticated → redirect to login; (b) `GET /account/addresses` authenticated → 200 with address list; (c) `GET /account/addresses/edit` (new) → 200 with empty form; (d) `GET /account/addresses/edit?id=addr-1` → 200 with populated form; (e) `POST /account/addresses/save` valid data → redirect to addresses; (f) `POST /account/addresses/save` missing required fields → 200 with validation errors; (g) `POST /account/addresses/save` when at limit (10) → error.

---

### 4.3 AddressService (NEW)

- [x] T411 [US-5] [Plan:4.3] Create `AddressService` `@Service` class in `src/main/java/com/skishop/service/address/AddressService.java`. Inject `UserAddressDao` via constructor. Provide methods: `listByUserId(String userId)` → `List<Address>`, `findById(String id)` → `Address`, `save(Address address)` → void, `countByUserId(String userId)` → int. This fixes the `AddressListAction` direct-DAO-access layering violation by routing all address operations through a proper service layer.
- [x] T412 [P] [US-5] [Plan:4.3] Create unit test `src/test/java/com/skishop/service/address/AddressServiceTest.java`. Mock `UserAddressDao`. Test: (a) `listByUserId` delegates to DAO; (b) `findById` delegates to DAO; (c) `save` delegates to DAO; (d) `countByUserId` returns correct count.

---

### 4.4 PointController

- [x] T413 [US-7] [Plan:4.4] Create `PointController` `@Controller` class in `src/main/java/com/skishop/web/controller/PointController.java`. Inject `PointService` via constructor injection. Requires authenticated user.
- [x] T414 [US-7] [Plan:4.4] Implement `GET /points` handler in `PointController`. Retrieve user from session. Call `pointService.getAccount(user.getId())`. Set `pointBalance` model attribute. Return view name `points/balance`. Replicate `PointBalanceAction` behavior exactly.
- [x] T415 [P] [US-7] [Plan:4.4] Create unit test `src/test/java/com/skishop/web/controller/PointControllerTest.java`. Test scenarios: (a) `GET /points` unauthenticated → redirect to login; (b) `GET /points` authenticated → 200 with point balance attribute.

---

### 4.5 PasswordController

- [x] T416 [US-6] [Plan:4.5] Create `PasswordController` `@Controller` class in `src/main/java/com/skishop/web/controller/PasswordController.java`. Inject `UserService`, `PasswordResetTokenDao`, and `MailService` via constructor injection. No authentication required for any endpoint.
- [x] T417 [US-6] [Plan:4.5] Implement `GET /password/forgot` handler in `PasswordController`. Return view name `auth/password/forgot`. No model attributes needed.
- [x] T418 [US-6] [Plan:4.5] Implement `POST /password/forgot` handler in `PasswordController`. Accept `@ModelAttribute PasswordResetRequestForm`. Look up user by email via `userService.findByEmail(form.getEmail())`. If user exists: generate UUID token, create `PasswordResetToken` with 1-hour expiry, save via `tokenDao.insert(token)`, call `mailService.enqueuePasswordReset(email, token)`. Always return view `auth/password/forgot_complete` regardless of whether user was found (prevents user enumeration). Replicate `PasswordForgotAction` behavior exactly.
- [x] T419 [US-6] [Plan:4.5] Implement `GET /password/reset` handler in `PasswordController`. Accept `@RequestParam String token`. Validate token: look up via `tokenDao.findByToken(token)`, check not null, not used (`usedAt == null`), not expired. If invalid, add error message and return `auth/password/reset` with error flag. If valid, set `token` model attribute and return view `auth/password/reset`.
- [x] T420 [US-6] [Plan:4.5] Implement `POST /password/reset` handler in `PasswordController`. Accept `@Valid @ModelAttribute PasswordResetForm`, `BindingResult`, `Model`. Validate token (same checks as GET). If invalid, add error and return `auth/password/reset`. Validate password == confirmPassword. Look up user by token's userId. Generate new salt via `PasswordHasher.generateSalt()`, hash new password via `PasswordHasher.hash()`. Update via `userService.updatePassword(userId, hash, salt)`. Mark token used via `tokenDao.markUsed(tokenId)`. Redirect to `/login`. Replicate `PasswordResetAction` behavior exactly.
- [x] T421 [P] [US-6] [Plan:4.5] Create unit test `src/test/java/com/skishop/web/controller/PasswordControllerTest.java`. Test scenarios: (a) `GET /password/forgot` → 200 with forgot form; (b) `POST /password/forgot` with existing email → 200 success page, token created; (c) `POST /password/forgot` with non-existing email → 200 success page (prevents enumeration); (d) `GET /password/reset?token=valid` → 200 with reset form; (e) `GET /password/reset?token=expired` → 200 with error; (f) `POST /password/reset` valid token + matching passwords → redirect to `/login`; (g) `POST /password/reset` invalid token → error; (h) `POST /password/reset` password mismatch → validation error.

---

### 4.6 MailService Spring Conversion

- [x] T422 [Plan:4.6] Create new `MailService` `@Service` class in `src/main/java/com/skishop/service/mail/MailService.java` (replaces the existing Struts-era class). Inject Spring `JavaMailSender` and `EmailQueueDao` via constructor. Replace direct `javax.mail` Session/Transport usage with `JavaMailSender.send(MimeMessage)`.
- [x] T423 [Plan:4.6] Implement `enqueuePasswordReset(String email, String token)` method. Load password reset email template from `mail/password_reset.txt`. Replace `{{token}}` placeholder. Create `EmailQueue` record with status `PENDING`, persist via `emailQueueDao.insert()`. Preserve exact behavior of existing `MailService.enqueuePasswordReset()`.
- [x] T424 [Plan:4.6] Implement `enqueueOrderConfirmation(String email, Order order)` method. Load order confirmation template from `mail/order_confirmation.txt`. Replace placeholders (`{{orderNumber}}`, `{{totalAmount}}`, etc.). Create `EmailQueue` record with status `PENDING`, persist. Preserve existing behavior.
- [x] T425 [Plan:4.6] Implement `processQueue()` method. Query pending emails from `emailQueueDao`. For each: construct `MimeMessage` via `JavaMailSender.createMimeMessage()`, set to/subject/body, call `JavaMailSender.send()`. On success, update status to `SENT`. On failure, increment retry count; mark `FAILED` after MAX_RETRY (3). Preserve the retry and max-retry semantics of the original.
- [x] T426 [Plan:4.6] Configure `spring.mail.*` properties in `application.properties` (or `application.yml`). Set `spring.mail.host`, `spring.mail.port`, `spring.mail.username`, `spring.mail.password`, `spring.mail.properties.mail.smtp.auth=true`, `spring.mail.properties.mail.smtp.starttls.enable=true`. Values should reference environment variables or defaults matching existing `mail.properties` config.
- [x] T427 [P] [Plan:4.6] Create unit test `src/test/java/com/skishop/service/mail/MailServiceTest.java`. Mock `JavaMailSender` and `EmailQueueDao`. Test: (a) `enqueuePasswordReset` creates EmailQueue record with correct template content; (b) `enqueueOrderConfirmation` creates EmailQueue record; (c) `processQueue` sends pending emails and updates status; (d) `processQueue` marks as FAILED after max retries.

---

### 4.7 Form Objects (P2)

- [x] T428 [Plan:4.7] Create `AddressForm` POJO in `src/main/java/com/skishop/web/form/AddressForm.java` (Spring version replacing Struts `ValidatorForm`). Fields: `id`, `label` (`@NotBlank`), `recipientName` (`@NotBlank`), `postalCode` (`@NotBlank`), `prefecture` (`@NotBlank`), `address1` (`@NotBlank`), `address2`, `phone`, `isDefault`. Use Bean Validation (`jakarta.validation.constraints` or `javax.validation.constraints` per Spring Boot version). Include getters/setters.
- [x] T429 [Plan:4.7] Create `PasswordResetRequestForm` POJO in `src/main/java/com/skishop/web/form/PasswordResetRequestForm.java` (Spring version). Fields: `email` (`@NotBlank @Email`). Include getter/setter.
- [x] T430 [Plan:4.7] Create `PasswordResetForm` POJO in `src/main/java/com/skishop/web/form/PasswordResetForm.java` (Spring version). Fields: `token` (`@NotBlank`), `password` (`@NotBlank`), `passwordConfirm` (`@NotBlank`). Include getter/setter. Password match validation should be handled in controller (matching existing `FormValidationUtils.addPasswordMismatch` pattern).
- [x] T431 [P] [Plan:4.7] Create unit test `src/test/java/com/skishop/web/form/P2FormValidationTest.java`. Use `jakarta.validation.Validator` to test: (a) `AddressForm` with all required fields → no violations; (b) `AddressForm` with blank `label`/`recipientName`/`postalCode`/`prefecture`/`address1` → violations; (c) `PasswordResetRequestForm` with valid email → no violations; (d) `PasswordResetRequestForm` with blank/invalid email → violations; (e) `PasswordResetForm` with blank token or password → violations.

---

### 4.8 JSP Views (P2)

- [x] T432 [US-4] [Plan:4.8] Migrate `orders/history.jsp` view to `src/main/webapp/WEB-INF/jsp/orders/history.jsp`. Convert Struts tags (`<bean:write>`, `<logic:iterate>`, `<html:link>`) to JSTL/EL equivalents (`${order.orderNumber}`, `<c:forEach>`, `<a href>`). Preserve layout, CSS classes, and all displayed order fields. Include links to order detail, cancel, return actions.
- [x] T433 [US-4] [Plan:4.8] Migrate `orders/detail.jsp` view to `src/main/webapp/WEB-INF/jsp/orders/detail.jsp`. Display order header (order number, date, status, totals) and line items table. Convert Struts tags to JSTL/EL. Preserve all fields from original JSP.
- [x] T434 [US-5] [Plan:4.8] Migrate `account/addresses.jsp` view to `src/main/webapp/WEB-INF/jsp/account/addresses.jsp`. Display address list with edit links and "Add New" button. Convert Struts tags to JSTL/EL.
- [x] T435 [US-5] [Plan:4.8] Migrate `account/address_edit.jsp` view to `src/main/webapp/WEB-INF/jsp/account/address_edit.jsp`. Build Spring-compatible form with fields: label, recipientName, postalCode, prefecture, address1, address2, phone, isDefault. Display Bean Validation error messages via `<form:errors>` or `${errors}`. Include hidden `id` field for edits. Use `<form>` with `action="/account/addresses/save"` and CSRF token.
- [x] T436 [US-7] [Plan:4.8] Migrate `points/balance.jsp` view to `src/main/webapp/WEB-INF/jsp/points/balance.jsp`. Display point balance (from `pointBalance` model attribute) and transaction history if available. Convert Struts tags to JSTL/EL.
- [x] T437 [US-6] [Plan:4.8] Migrate `auth/password/forgot.jsp` view to `src/main/webapp/WEB-INF/jsp/auth/password/forgot.jsp`. Simple form with email input field and submit button. POST to `/password/forgot`. Include CSRF token.
- [x] T438 [US-6] [Plan:4.8] Migrate `auth/password/forgot_complete.jsp` view to `src/main/webapp/WEB-INF/jsp/auth/password/forgot_complete.jsp`. Display success message ("If the email exists, a reset link has been sent."). No conditional logic — always shows success to prevent enumeration.
- [x] T439 [US-6] [Plan:4.8] Migrate `auth/password/reset.jsp` view to `src/main/webapp/WEB-INF/jsp/auth/password/reset.jsp`. Form with hidden `token` field, `password` and `confirmPassword` inputs, submit button. POST to `/password/reset`. Display error messages if token is invalid/expired. Include CSRF token.

---

### 4.9 Compile + Test Gate

- [x] T440 [Plan:4.9] Run `mvn -B compile` and verify all Phase 4 source files compile without errors. Fix any compilation issues in controllers, services, forms, or views.
- [x] T441 [Plan:4.9] Run all Phase 4 unit tests (`OrderControllerTest`, `AddressControllerTest`, `AddressServiceTest`, `PointControllerTest`, `PasswordControllerTest`, `MailServiceTest`, `P2FormValidationTest`). All tests must pass.
- [x] T442 [Plan:4.9] Smoke-test P2 endpoints manually or via integration test: verify `GET /orders` (auth required), `GET /orders/detail?id=...`, `POST /orders/cancel`, `POST /orders/return`, `GET /account/addresses`, `GET /account/addresses/edit`, `POST /account/addresses/save`, `GET /points`, `GET /password/forgot`, `POST /password/forgot`, `GET /password/reset?token=...`, `POST /password/reset` all respond correctly.

---

## Phase 5: P3 Features — Admin Management

**Batch**: 5
**Goal**: Implement all Priority 3 admin features: product management, order management, coupon management, and shipping method management. All endpoints require ADMIN role.
**User Stories**: US-9 (Admin Product Management), US-10 (Admin Order Management), US-11 (Admin Coupon and Shipping Management)
**Task ID Range**: T500–T540

---

### 5.1 AdminProductController

- [x] T500 [US-9] [Plan:5.1] Create `AdminProductController` `@Controller` class in `src/main/java/com/skishop/web/controller/AdminProductController.java`. Annotate with `@RequestMapping("/admin")`. Inject `ProductService`, `ProductDao`, `PriceDao`, and `InventoryDao` via constructor injection. All handler methods must require ADMIN role — check session `loginUser` is not null and has role `ADMIN`; redirect to `/login` if unauthenticated, return 403 if non-admin.
- [x] T501 [US-9] [Plan:5.1] Implement `GET /admin/products` handler in `AdminProductController`. Call `productService.search(null, null, 0, 200)` (MAX_ADMIN_PRODUCTS = 200). Set `products` model attribute. Return view name `admin/products/list`. Replicate `AdminProductListAction` behavior exactly.
- [x] T502 [US-9] [Plan:5.1] Implement `GET /admin/product/edit` handler in `AdminProductController`. Accept `@RequestParam(required=false) String id`. If `id` is present and non-empty, load product via `productService.findById(id)`, load price via `priceDao.findByProductId(id)`, load inventory via `inventoryDao.findByProductId(id)`, and populate `AdminProductForm` with all fields. If `id` is absent, initialize form with generated product ID (format: `"P" + UUID substring(0,12)`), status `"ACTIVE"`, price `"0"`, inventoryQty `0`. Set `adminProductForm` model attribute. Return view name `admin/products/edit`. Replicate `AdminProductEditAction` GET behavior exactly.
- [x] T503 [US-9] [Plan:5.1] Implement `POST /admin/product/edit` handler in `AdminProductController`. Accept `@ModelAttribute AdminProductForm`, `BindingResult`, `RedirectAttributes`. Validate: `id` not null/empty — on failure, add error and return `admin/products/edit`. Parse `price` as `BigDecimal` — on `NumberFormatException`, add error and return `admin/products/edit`. Check if product exists via `productService.findById(id)`. Build `Product` domain object from form. If new: set `sku` = `"SKU-" + id`, set `createdAt`/`updatedAt` = now, call `productDao.insert(product)`. If existing: preserve `sku`, call `productDao.update(product)`. Upsert `Price` with UUID id, product ID, parsed price, currency `"JPY"`. Upsert `Inventory`: if null insert new (with status based on qty > 0), else call `inventoryDao.updateQuantity()`. Redirect to `/admin/products`. Replicate `AdminProductEditAction` POST behavior exactly.
- [x] T504 [US-9] [Plan:5.1] Implement `POST /admin/product/delete` handler in `AdminProductController`. Accept `@RequestParam String id`, `RedirectAttributes`. Validate `id` not null/empty — on failure, add flash error and redirect to `/admin/products`. Look up product via `productService.findById(id)` — if null, add flash error and redirect. Call `productService.deactivateProduct(id)`. Redirect to `/admin/products`. Replicate `AdminProductDeleteAction` behavior exactly.
- [x] T505 [P] [US-9] [Plan:5.1] Create unit test `src/test/java/com/skishop/web/controller/AdminProductControllerTest.java`. Test scenarios: (a) `GET /admin/products` as ADMIN → 200 with product list; (b) `GET /admin/products` as non-admin → 403; (c) `GET /admin/products` unauthenticated → redirect to login; (d) `GET /admin/product/edit` with no id → 200 with empty form (generated ID); (e) `GET /admin/product/edit?id=PSK001` → 200 with populated form; (f) `POST /admin/product/edit` valid data → redirect to `/admin/products`; (g) `POST /admin/product/edit` invalid price → 200 with error; (h) `POST /admin/product/delete?id=PSK001` → redirect to `/admin/products`; (i) `POST /admin/product/delete` missing id → redirect with error.

---

### 5.2 AdminOrderController

- [x] T506 [US-10] [Plan:5.2] Create `AdminOrderController` `@Controller` class in `src/main/java/com/skishop/web/controller/AdminOrderController.java`. Annotate with `@RequestMapping("/admin")`. Inject `OrderService` and `OrderFacade` via constructor injection. All handler methods require ADMIN role — same auth pattern as `AdminProductController`.
- [x] T507 [US-10] [Plan:5.2] Implement `GET /admin/orders` handler in `AdminOrderController`. Call `orderService.listAll(200)` (MAX_ADMIN_ORDERS = 200). Set `orders` model attribute. Return view name `admin/orders/list`. Replicate `AdminOrderListAction` behavior exactly.
- [x] T508 [US-10] [Plan:5.2] Implement `GET /admin/orders/detail` handler in `AdminOrderController`. Accept `@RequestParam(required=false) String id`. If `id` is present and non-empty, call `orderService.findById(id)`. If order found, call `orderService.listItems(id)` and set model attributes `order` and `orderItems`. Return view name `admin/orders/detail`. Note: the Struts action uses param name `orderId` — the Spring contract uses `id`. Replicate `AdminOrderDetailAction` behavior exactly.
- [x] T509 [US-10] [Plan:5.2] Implement `POST /admin/order/update` handler in `AdminOrderController`. Accept `@RequestParam String id` (maps from Struts `orderId`), `@RequestParam(required=false) String status`, `@RequestParam(required=false) String paymentStatus`, `RedirectAttributes`. Validate `id` not null/empty — on failure, add flash error and redirect to `/admin/orders`. Look up order via `orderService.findById(id)` — if null, add flash error and redirect. If `status` is non-empty, call `orderService.updateStatus(id, status)`. If `paymentStatus` is non-empty, call `orderService.updatePaymentStatus(id, paymentStatus)`. Redirect to `/admin/orders`. Replicate `AdminOrderUpdateAction` behavior exactly.
- [x] T510 [US-10] [Plan:5.2] Implement `POST /admin/order/refund` handler in `AdminOrderController`. Accept `@RequestParam String id`, `RedirectAttributes`. Validate `id` not null/empty (also check fallback param like Struts does). Call `orderFacade.returnOrder(id, null)`. On success, redirect to `/admin/orders`. On `RuntimeException`, add flash error message and redirect to `/admin/orders`. Replicate `AdminOrderRefundAction` behavior exactly.
- [x] T511 [P] [US-10] [Plan:5.2] Create unit test `src/test/java/com/skishop/web/controller/AdminOrderControllerTest.java`. Test scenarios: (a) `GET /admin/orders` as ADMIN → 200 with all orders; (b) `GET /admin/orders` as non-admin → 403; (c) `GET /admin/orders/detail?id=ORD-1` → 200 with order and items; (d) `GET /admin/orders/detail` with no id → 200 with no order data; (e) `POST /admin/order/update` valid id + status → redirect to `/admin/orders`; (f) `POST /admin/order/update` missing id → redirect with error; (g) `POST /admin/order/refund?id=ORD-1` success → redirect to `/admin/orders`; (h) `POST /admin/order/refund?id=ORD-1` RuntimeException → redirect with error.

---

### 5.3 AdminCouponController

- [x] T512 [US-11] [Plan:5.3] Create `AdminCouponController` `@Controller` class in `src/main/java/com/skishop/web/controller/AdminCouponController.java`. Annotate with `@RequestMapping("/admin")`. Inject `CouponDao` via constructor injection. All handler methods require ADMIN role — same auth pattern as other admin controllers.
- [x] T513 [US-11] [Plan:5.3] Implement `GET /admin/coupons` handler in `AdminCouponController`. Call `couponDao.listAll()`. Set `coupons` model attribute. Return view name `admin/coupons/list`. Replicate `AdminCouponListAction` behavior exactly.
- [x] T514 [US-11] [Plan:5.3] Implement `GET /admin/coupon/edit` handler in `AdminCouponController`. Accept `@RequestParam(required=false) String code`. If `code` is present and non-empty, load coupon via `couponDao.findByCode(code)` and populate `AdminCouponForm` with all fields (id, campaignId, code, couponType, discountValue, discountType, minimumAmount, maximumDiscount, usageLimit, active, expiresAt formatted as `yyyy-MM-dd`). If `code` is absent, initialize form with generated UUID id and `active=true`. Set `adminCouponForm` model attribute. Return view name `admin/coupons/edit`. Replicate `AdminCouponEditAction` GET behavior exactly.
- [x] T515 [US-11] [Plan:5.3] Implement `POST /admin/coupon/edit` handler in `AdminCouponController`. Accept `@ModelAttribute AdminCouponForm`, `BindingResult`, `RedirectAttributes`. Validate: `code` not null/empty — on failure, add error and return `admin/coupons/edit`. Parse `discountValue` as BigDecimal — on error, add error and return edit view. Parse optional `minimumAmount` and `maximumDiscount` as BigDecimal — on error, add error and return. Parse optional `expiresAt` as Date (`yyyy-MM-dd`) — on error, add error and return. Build `Coupon` domain object from form. Check if existing via `couponDao.findByCode(code)`. If new, call `couponDao.insert(coupon)`. If existing, call `couponDao.update(coupon)`. Redirect to `/admin/coupons`. Replicate `AdminCouponEditAction` POST behavior exactly.
- [x] T516 [P] [US-11] [Plan:5.3] Create unit test `src/test/java/com/skishop/web/controller/AdminCouponControllerTest.java`. Test scenarios: (a) `GET /admin/coupons` as ADMIN → 200 with coupon list; (b) `GET /admin/coupons` as non-admin → 403; (c) `GET /admin/coupon/edit` with no code → 200 with empty form (generated UUID); (d) `GET /admin/coupon/edit?code=SAVE10` → 200 with populated form; (e) `POST /admin/coupon/edit` valid data → redirect to `/admin/coupons`; (f) `POST /admin/coupon/edit` missing code → 200 with error; (g) `POST /admin/coupon/edit` invalid discountValue → 200 with error.

---

### 5.4 AdminShippingController

- [x] T517 [US-11] [Plan:5.4] Create `AdminShippingController` `@Controller` class in `src/main/java/com/skishop/web/controller/AdminShippingController.java`. Annotate with `@RequestMapping("/admin")`. Inject `ShippingMethodDao` via constructor injection. All handler methods require ADMIN role.
- [x] T518 [US-11] [Plan:5.4] Implement `GET /admin/shipping` handler in `AdminShippingController`. Call `shippingMethodDao.listAll()`. Set `shippingMethods` model attribute. Return view name `admin/shipping/list`. Replicate `AdminShippingMethodListAction` behavior exactly.
- [x] T519 [US-11] [Plan:5.4] Implement `GET /admin/shipping/edit` handler in `AdminShippingController`. Accept `@RequestParam(required=false) String code`. If `code` is present and non-empty, load method via `shippingMethodDao.findByCode(code)` and populate `AdminShippingMethodForm` with all fields (id, code, name, fee as string, active, sortOrder). If `code` is absent, initialize form with generated UUID id and `active=true`. Set `adminShippingMethodForm` model attribute. Return view name `admin/shipping/edit`. Replicate `AdminShippingMethodEditAction` GET behavior exactly.
- [x] T520 [US-11] [Plan:5.4] Implement `POST /admin/shipping/edit` handler in `AdminShippingController`. Accept `@ModelAttribute AdminShippingMethodForm`, `BindingResult`, `RedirectAttributes`. Validate: `code` not null/empty — on failure, add error and return `admin/shipping/edit`. Parse `fee` as BigDecimal — on `NumberFormatException`, add error and return edit view. Check if existing via `shippingMethodDao.findByCode(code)`. Build `ShippingMethod` domain object — use existing id if found, else use form id or generate UUID. If new, call `shippingMethodDao.insert(method)`. If existing, call `shippingMethodDao.update(method)`. Redirect to `/admin/shipping`. Replicate `AdminShippingMethodEditAction` POST behavior exactly.
- [x] T521 [P] [US-11] [Plan:5.4] Create unit test `src/test/java/com/skishop/web/controller/AdminShippingControllerTest.java`. Test scenarios: (a) `GET /admin/shipping` as ADMIN → 200 with shipping method list; (b) `GET /admin/shipping` as non-admin → 403; (c) `GET /admin/shipping/edit` with no code → 200 with empty form; (d) `GET /admin/shipping/edit?code=STANDARD` → 200 with populated form; (e) `POST /admin/shipping/edit` valid data → redirect to `/admin/shipping`; (f) `POST /admin/shipping/edit` missing code → 200 with error; (g) `POST /admin/shipping/edit` invalid fee → 200 with error.

---

### 5.5 Admin Form Objects

- [x] T522 [Plan:5.5] Create `AdminProductForm` POJO in `src/main/java/com/skishop/web/form/AdminProductForm.java` (Spring version replacing Struts `ValidatorForm`). Fields: `id` (String), `name` (String, `@NotBlank`), `brand` (String), `description` (String), `categoryId` (String), `price` (String, `@NotBlank`), `status` (String), `inventoryQty` (int). Include getters/setters. No Struts dependencies — pure POJO with Bean Validation annotations.
- [x] T523 [Plan:5.5] Create `AdminCouponForm` POJO in `src/main/java/com/skishop/web/form/AdminCouponForm.java` (Spring version replacing Struts `ValidatorForm`). Fields: `id` (String), `campaignId` (String), `code` (String, `@NotBlank`), `couponType` (String), `discountValue` (String, `@NotBlank`), `discountType` (String), `minimumAmount` (String), `maximumDiscount` (String), `usageLimit` (int), `active` (boolean), `expiresAt` (String). Include getters/setters.
- [x] T524 [Plan:5.5] Create `AdminShippingMethodForm` POJO in `src/main/java/com/skishop/web/form/AdminShippingMethodForm.java` (Spring version replacing Struts `ValidatorForm`). Fields: `id` (String), `code` (String, `@NotBlank`), `name` (String, `@NotBlank`), `fee` (String, `@NotBlank`), `active` (boolean), `sortOrder` (int). Include getters/setters.
- [x] T525 [P] [Plan:5.5] Create unit test `src/test/java/com/skishop/web/form/AdminFormValidationTest.java`. Use Bean Validation `Validator` to test: (a) `AdminProductForm` with all required fields → no violations; (b) `AdminProductForm` with blank `name`/`price` → violations; (c) `AdminCouponForm` with valid code + discountValue → no violations; (d) `AdminCouponForm` with blank `code`/`discountValue` → violations; (e) `AdminShippingMethodForm` with valid code + name + fee → no violations; (f) `AdminShippingMethodForm` with blank `code`/`name`/`fee` → violations.

---

### 5.6 Admin JSP Views

- [x] T526 [US-9] [Plan:5.6] Migrate `admin/products/list.jsp` view to `src/main/webapp/WEB-INF/jsp/admin/products/list.jsp`. Convert Struts tags (`<bean:write>`, `<logic:iterate>`, `<html:link>`) to JSTL/EL equivalents (`${product.name}`, `<c:forEach items="${products}" var="product">`, `<a href>`). Include links for edit (`/admin/product/edit?id=...`) and delete (`/admin/product/delete` with form POST + CSRF token). Preserve layout integration with base Tiles-equivalent template (header, messages, body, footer). Display error messages from `${errors}` / flash attributes.
- [x] T527 [US-9] [Plan:5.6] Migrate `admin/products/edit.jsp` view to `src/main/webapp/WEB-INF/jsp/admin/products/edit.jsp`. Convert Struts form tags to Spring form tags / JSTL. Render form fields: id (hidden or read-only), name, brand, description, categoryId (select), price, status (select), inventoryQty. Display validation errors inline. Form action posts to `/admin/product/edit`. Include CSRF token. Show success/error messages.
- [x] T528 [US-10] [Plan:5.6] Migrate `admin/orders/list.jsp` view to `src/main/webapp/WEB-INF/jsp/admin/orders/list.jsp`. Convert Struts tags to JSTL/EL. Display all orders with columns: order number, user, date, status, total, payment status. Include link to detail (`/admin/orders/detail?id=...`). Preserve layout integration.
- [x] T529 [US-10] [Plan:5.6] Migrate `admin/orders/detail.jsp` view to `src/main/webapp/WEB-INF/jsp/admin/orders/detail.jsp`. Convert Struts tags to JSTL/EL. Display order header (number, user, date, status, payment status) and order items table (`<c:forEach items="${orderItems}">`). Include forms for update status (`POST /admin/order/update`) and process refund (`POST /admin/order/refund`) with CSRF tokens. Display error messages.
- [x] T530 [US-11] [Plan:5.6] Migrate `admin/coupons/list.jsp` view to `src/main/webapp/WEB-INF/jsp/admin/coupons/list.jsp`. Convert Struts tags to JSTL/EL. Display coupon list: code, type, discount value, active status, expiry. Include edit link (`/admin/coupon/edit?code=...`). Preserve layout integration.
- [x] T531 [US-11] [Plan:5.6] Migrate `admin/coupons/edit.jsp` view to `src/main/webapp/WEB-INF/jsp/admin/coupons/edit.jsp`. Render form fields: code, couponType (select), discountValue, discountType, minimumAmount, maximumDiscount, usageLimit, active (checkbox), expiresAt (date input). Form posts to `/admin/coupon/edit`. Include CSRF token. Display validation errors.
- [x] T532 [US-11] [Plan:5.6] Migrate `admin/shipping/list.jsp` view to `src/main/webapp/WEB-INF/jsp/admin/shipping/list.jsp`. Convert Struts tags to JSTL/EL. Display shipping methods: code, name, fee, active status, sort order. Include edit link (`/admin/shipping/edit?code=...`). Preserve layout integration.
- [x] T533 [US-11] [Plan:5.6] Migrate `admin/shipping/edit.jsp` view to `src/main/webapp/WEB-INF/jsp/admin/shipping/edit.jsp`. Render form fields: code, name, fee, active (checkbox), sortOrder. Form posts to `/admin/shipping/edit`. Include CSRF token. Display validation errors.

---

### 5.7 Compile + Test Gate

- [x] T534 [Plan:5.7] Run `mvn -B compile` and verify all Phase 5 classes compile without errors. Fix any compilation issues in admin controllers, forms, or views.
- [x] T535 [Plan:5.7] Run all admin controller unit tests (`AdminProductControllerTest`, `AdminOrderControllerTest`, `AdminCouponControllerTest`, `AdminShippingControllerTest`) and verify they pass.
- [x] T536 [Plan:5.7] Run admin form validation tests (`AdminFormValidationTest`) and verify they pass.
- [x] T537 [Plan:5.7] Verify ADMIN role enforcement: confirm that accessing any `/admin/*` endpoint without ADMIN role returns 403 Forbidden or redirects to login. Verify this in at least one controller test for each admin controller.
- [x] T538 [Plan:5.7] Run `mvn -B clean package` and verify the full project (all phases) still compiles and all tests pass. Fix any regressions introduced by Phase 5.

---

## Phase 6: Test Migration

> **Plan ref**: Phase 6 (Batch 6) — Migrate all unit tests to JUnit 5 + Spring Test
> **Requirements**: REQ-039, REQ-040, REQ-041, NFR-001
> **Exit Criteria**: `mvn -B test` passes with zero failures. All 38 test files migrated.

---

### 6.1 Test Infrastructure

- [x] T600 [Plan:6.1] Create `src/test/resources/application-test.properties` with H2 in-memory datasource (`jdbc:h2:mem:skishop;DB_CLOSE_DELAY=-1`), `spring.sql.init.schema-locations=classpath:db/schema.sql`, `spring.sql.init.data-locations=classpath:db/data.sql`, and `spring.sql.init.mode=always`
- [x] T601 [Plan:6.1] Rewrite `src/test/java/com/skishop/dao/DaoTestBase.java` as a Spring Test base class annotated with `@JdbcTest`, `@ActiveProfiles("test")`, `@AutoConfigureTestDatabase(replace=NONE)`. Inject `JdbcTemplate` and `DataSource`. Replace manual H2 setup and `DataSourceLocator` with Spring-managed datasource. Provide `resetDatabase()` using `@Sql` or `JdbcTemplate` execution of schema.sql + data.sql
- [x] T602 [Plan:6.1] Remove old `DaoTestBase` dependency on `DataSourceLocator.getInstance().setDataSource()` — all test datasource wiring must go through Spring context
- [x] T603 [Plan:6.1] Verify `src/test/resources/db/schema.sql` and `src/test/resources/db/data.sql` exist or are classpath-accessible for H2 initialization; adjust H2-incompatible DDL if needed (e.g., PostgreSQL-specific syntax)

### 6.2 DAO Test Migration (12 tests)

- [x] T604 [P] [Plan:6.2] Migrate `CartDaoTest` to JUnit 5 + `@JdbcTest`: replace `extends DaoTestBase` / `TestCase` with `@ExtendWith(SpringExtension.class)`, JUnit 5 `@Test`, inject DAO via `@Autowired` or manual instantiation with injected `DataSource`. Preserve all SQL assertions
- [x] T605 [P] [Plan:6.2] Migrate `CouponDaoTest` to JUnit 5 + `@JdbcTest`: same pattern — JUnit 5 annotations, Spring-managed datasource, preserve assertion logic
- [x] T606 [P] [Plan:6.2] Migrate `EmailQueueDaoTest` to JUnit 5 + `@JdbcTest`
- [x] T607 [P] [Plan:6.2] Migrate `InventoryDaoTest` to JUnit 5 + `@JdbcTest`
- [x] T608 [P] [Plan:6.2] Migrate `OrderDaoTest` to JUnit 5 + `@JdbcTest`
- [x] T609 [P] [Plan:6.2] Migrate `PasswordResetTokenDaoTest` to JUnit 5 + `@JdbcTest`
- [x] T610 [P] [Plan:6.2] Migrate `PointAccountDaoTest` to JUnit 5 + `@JdbcTest`
- [x] T611 [P] [Plan:6.2] Migrate `PointTransactionDaoTest` to JUnit 5 + `@JdbcTest`
- [x] T612 [P] [Plan:6.2] Migrate `ProductDaoTest` to JUnit 5 + `@JdbcTest`
- [x] T613 [P] [Plan:6.2] Migrate `ShippingMethodDaoTest` to JUnit 5 + `@JdbcTest`
- [x] T614 [P] [Plan:6.2] Migrate `UserAddressDaoTest` to JUnit 5 + `@JdbcTest`
- [x] T615 [P] [Plan:6.2] Migrate `UserDaoTest` to JUnit 5 + `@JdbcTest`

### 6.3 Controller Test Rewrite (22 tests)

- [x] T616 [Plan:6.3] Create `src/test/java/com/skishop/web/controller/ControllerTestBase.java` — base class or shared configuration for `@WebMvcTest` tests. Configure common `MockMvc` setup, shared mock beans (e.g., session-scoped user), and `@MockBean` declarations for cross-cutting services. Replace `StrutsActionTestBase` / `MockStrutsTestCase` pattern entirely
- [x] T617 [P] [Plan:6.3] Rewrite `ProductListActionTest` → `ProductControllerTest` (product list): `@WebMvcTest(ProductController.class)`, `@MockBean` ProductService, `mockMvc.perform(get("/products").param("keyword","Ski"))`, assert status 200 + view name + model attribute `productList`
- [x] T618 [P] [Plan:6.3] Rewrite `ProductDetailActionTest` → `ProductControllerTest` (product detail): test `GET /products/{id}`, assert model attribute `product` and view name
- [x] T619 [P] [Plan:6.3] Rewrite `LoginActionTest` → `AuthControllerTest` (login): `@WebMvcTest(AuthController.class)`, `@MockBean` AuthService, test `POST /login` with form params, assert redirect on success, assert error view on failure
- [x] T620 [P] [Plan:6.3] Rewrite `LogoutActionTest` → `AuthControllerTest` (logout): test `GET /logout`, assert session invalidated and redirect to home
- [x] T621 [P] [Plan:6.3] Rewrite `RegisterActionTest` → `AuthControllerTest` (register): test `POST /register`, assert redirect on success, assert validation errors on invalid input
- [x] T622 [P] [Plan:6.3] Rewrite `AuthRequestProcessorTest` → Spring Security or filter integration test: verify unauthenticated requests to protected URLs return 302/401, authenticated requests pass through
- [x] T623 [P] [Plan:6.3] Rewrite `CartActionTest` → `CartControllerTest`: `@WebMvcTest(CartController.class)`, `@MockBean` CartService, test add/remove/view cart operations, assert model attributes
- [x] T624 [P] [Plan:6.3] Rewrite `CheckoutActionTest` → `CheckoutControllerTest`: `@WebMvcTest(CheckoutController.class)`, `@MockBean` OrderFacade, test `POST /checkout`, assert order created and redirect
- [x] T625 [P] [Plan:6.3] Rewrite `CouponApplyActionTest` → `CouponControllerTest` or `CartControllerTest` (coupon apply): test `POST /cart/coupon`, assert coupon applied to session/model
- [x] T626 [P] [Plan:6.3] Rewrite `OrderHistoryActionTest` → `OrderControllerTest` (order list): `@WebMvcTest(OrderController.class)`, `@MockBean` OrderService, test `GET /orders`, assert model attribute `orders`
- [x] T627 [P] [Plan:6.3] Rewrite `OrderCancelActionTest` → `OrderControllerTest` (cancel): test `POST /orders/{id}/cancel`, assert status change and redirect
- [x] T628 [P] [Plan:6.3] Rewrite `OrderReturnActionTest` → `OrderControllerTest` (return): test `POST /orders/{id}/return`, assert status change and redirect
- [x] T629 [P] [Plan:6.3] Rewrite `AddressListActionTest` → `AddressControllerTest` (list): `@WebMvcTest(AddressController.class)`, `@MockBean` AddressService, test `GET /addresses`, assert model attribute
- [x] T630 [P] [Plan:6.3] Rewrite `AddressSaveActionTest` → `AddressControllerTest` (save): test `POST /addresses`, assert redirect on success
- [x] T631 [P] [Plan:6.3] Rewrite `PointBalanceActionTest` → `PointControllerTest`: `@WebMvcTest(PointController.class)`, `@MockBean` PointService, test `GET /points`, assert model attribute `balance`
- [x] T632 [P] [Plan:6.3] Rewrite `PasswordForgotActionTest` → `PasswordControllerTest` (forgot): test `POST /password/forgot`, `@MockBean` PasswordResetService + EmailService, assert success view
- [x] T633 [P] [Plan:6.3] Rewrite `PasswordResetActionTest` → `PasswordControllerTest` (reset): test `POST /password/reset` with token, assert redirect on success
- [x] T634 [P] [Plan:6.3] Rewrite `AdminProductEditActionTest` → `AdminProductControllerTest`: `@WebMvcTest(AdminProductController.class)`, test `POST /admin/products/{id}`, assert product updated
- [x] T635 [P] [Plan:6.3] Rewrite `AdminCouponEditActionTest` → `AdminCouponControllerTest`: test `POST /admin/coupons/{id}`, assert coupon updated
- [x] T636 [P] [Plan:6.3] Rewrite `AdminOrderUpdateActionTest` → `AdminOrderControllerTest` (update): test `POST /admin/orders/{id}/status`, assert order status changed
- [x] T637 [P] [Plan:6.3] Rewrite `AdminOrderRefundActionTest` → `AdminOrderControllerTest` (refund): test `POST /admin/orders/{id}/refund`, assert refund processed
- [x] T638 [P] [Plan:6.3] Rewrite `AdminShippingMethodEditActionTest` → `AdminShippingControllerTest`: test `POST /admin/shipping-methods/{id}`, assert shipping method updated

### 6.4 Service Test Migration (2 tests)

- [x] T639 [Plan:6.4] Migrate `OrderFacadeTest` to JUnit 5 + `@SpringBootTest` or plain unit test: replace `extends DaoTestBase` with Spring test context or manual mock setup, replace JUnit 4 `Assert.*` with JUnit 5 `Assertions.*`, preserve all financial calculation assertions (checkout with coupon + points, cancel, return)
- [x] T640 [Plan:6.4] Migrate `ScenarioFlowTest` to JUnit 5 + `@SpringBootTest`: replace `extends DaoTestBase` with Spring test context, replace JUnit 4 assertions with JUnit 5, preserve full scenario flow (register → login → search → cart → checkout → cancel → return)

### 6.5 Full Test Suite Gate

- [x] T641 [Plan:6.5] Remove `StrutsActionTestBase.java` and all Struts test dependencies (`strutstestcase`, `servletunit`) from `pom.xml`
- [x] T642 [Plan:6.5] Ensure `pom.xml` includes JUnit 5 (`junit-jupiter`), Spring Boot Test (`spring-boot-starter-test`), and H2 (`com.h2database:h2`) as test-scoped dependencies
- [x] T643 [Plan:6.5] Run `mvn -B test` and verify zero test failures across all 36 migrated test classes (12 DAO + 22 controller + 2 service)
- [x] T644 [Plan:6.5] Fix any H2-vs-PostgreSQL SQL dialect issues in schema.sql / data.sql that cause test failures (e.g., `SERIAL` → `IDENTITY`, `BYTEA` → `VARBINARY`, unsupported functions)

---

## Phase 7: Deployment & End-to-End Validation

### 7.1 Dockerfile Rewrite

- [x] T700 [Plan:7.1] Rewrite `Dockerfile` Stage 1 (build): use `maven:3.9-eclipse-temurin-21` base image, copy `pom.xml` and `src/`, run `mvn -B clean package -DskipTests` to produce executable Spring Boot JAR
- [x] T701 [Plan:7.1] Rewrite `Dockerfile` Stage 2 (runtime): use `eclipse-temurin:21-jre` base image, `COPY --from=build` the Spring Boot JAR, set `ENTRYPOINT ["java", "-jar", ...]` — no Tomcat installation needed (embedded server)
- [x] T702 [Plan:7.1] Remove all JDK 5, Tomcat 6, Debian Stretch, and legacy binary references from `Dockerfile` (JDK installer, `catalina.sh`, `postgresql.jar` driver copy, etc.)
- [x] T703 [Plan:7.1] Remove `platform: linux/amd64` restriction — JDK 21 images support multi-arch (amd64/arm64)
- [x] T704 [Plan:7.1] Add `EXPOSE 8080` and configure health check label or Spring Boot actuator readiness probe in Dockerfile

### 7.2 docker-compose.yml Update

- [x] T705 [Plan:7.2] Update `app` service in `docker-compose.yml`: remove legacy build args (`JDK_LICENSE`, `JDK_URL`, `JDK_SHA256`), remove `platform: linux/amd64`, update `container_name` to `skishop-app`
- [x] T706 [Plan:7.2] Pass Spring-style environment variables to `app` service: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, plus mail env vars as needed
- [x] T707 [P] [Plan:7.2] Preserve `db` service (postgres:9.2) configuration unchanged — same ports, volumes, healthcheck, init scripts
- [x] T708 [Plan:7.2] Remove `tomcat-logs` volume, add `depends_on.db.condition: service_healthy` for proper startup ordering
- [x] T709 [Plan:7.2] Verify `app` service exposes port `8080:8080` and can reach `db` service via Docker network hostname `db`

### 7.3 Entrypoint Script Update

- [x] T710 [Plan:7.3] Replace `docker/entrypoint.sh` content: remove all Tomcat/Catalina XML generation logic, replace with a simple script that passes environment variables and executes `java -jar` (or remove entrypoint entirely if Dockerfile CMD is sufficient)
- [x] T711 [Plan:7.3] Ensure DB connection environment variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`) are mapped to Spring datasource properties via `SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}` in entrypoint or docker-compose environment block

### 7.4 End-to-End Validation

- [x] T712 [Plan:7.4] Run `docker compose down -v && docker compose up --build -d` and verify both `app` and `db` containers start successfully
- [x] T713 [Plan:7.4] Wait for Spring Boot application startup (poll `http://localhost:8080` or check container logs for "Started" message)
- [x] T714 [Plan:7.4] Execute `./api-test.sh http://localhost:8080` and verify all ~50+ assertions pass with 0 failures
- [x] T715 [Plan:7.4] If any api-test assertions fail, diagnose from container logs (`docker compose logs app`), fix the application or configuration, and re-run until 100% pass rate
- [x] T716 [Plan:7.4] Verify `docker compose down` cleanly shuts down all containers

### 7.5 Final Verification — All Success Criteria

- [x] T717 [Plan:7.5] **SC-001**: Run `mvn -B clean package` outside Docker — verify zero compilation errors and all unit tests pass
- [x] T718 [Plan:7.5] **SC-002**: Confirm `api-test.sh` achieves 100% pass rate (all ~50+ assertions) against the running application
- [x] T719 [Plan:7.5] **SC-003**: Verify all 28 Struts action endpoints are accessible via clean URL paths with correct HTTP status codes and HTML content
- [x] T720 [Plan:7.5] **SC-004**: Verify all 20 functional domains produce identical observable behavior for identical user inputs
- [x] T721 [P] [Plan:7.5] **SC-005**: Verify PostgreSQL database schema and seed data remain unmodified — application works against same database state
- [x] T722 [P] [Plan:7.5] **SC-006**: Confirm each implementation phase compiled independently and passed applicable tests
- [x] T723 [Plan:7.5] **SC-007**: Verify `docker compose up` successfully builds and runs complete application stack (app + PostgreSQL) with rewritten codebase
- [x] T724 [Plan:7.5] Create final verification summary documenting all SC results in `docs/verification-report.md`

---

## Dependencies

```
Phase 1 (Foundation) ─────► Phase 2 (Cross-Cutting) ─────► Phase 3 (P1 Features)
                                                                    │
                                                                    ▼
                                                            Phase 4 (P2 Features)
                                                                    │
                                                                    ▼
                                                            Phase 5 (P3 Admin)
                                                                    │
                                                                    ▼
                                                            Phase 6 (Test Migration)
                                                                    │
                                                                    ▼
                                                            Phase 7 (Deployment & E2E)
```

Phase completion order: 1 → 2 → 3 → 4 → 5 → 6 → 7

## Parallel Execution

- **Phase 1**: Must run first (establishes Spring Boot foundation)
- **Phase 2**: Depends on Phase 1 (needs Spring Boot app structure)
- **Phase 3**: Depends on Phase 2 (needs interceptors, layout, error handling)
- **Phase 4**: Depends on Phase 3 (needs P1 controllers as pattern; shares services)
- **Phase 5**: Depends on Phase 4 (needs P2 patterns; shares service layer)
- **Phase 6**: Depends on Phases 1–5 (migrates tests for all prior phases)
- **Phase 7**: Depends on Phase 6 (needs all code + tests passing for deployment)

**Concurrency opportunities within phases:**
- Within Phase 1: T115–T133 (DAO rewrites) can run in parallel; T134–T147 (service conversions) can run in parallel
- Within Phase 3: T300–T327 (controllers) can run in parallel after forms (T328–T334) are defined
- Within Phase 4: T400–T421 (controllers) can run in parallel after forms (T428–T430) are defined
- Within Phase 5: T500–T521 (admin controllers) can run in parallel after forms (T522–T524) are defined
- Within Phase 6: T604–T615 (DAO tests) can all run in parallel; T617–T638 (controller tests) can all run in parallel
