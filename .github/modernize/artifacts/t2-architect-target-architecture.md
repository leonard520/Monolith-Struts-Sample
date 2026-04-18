# t2 — Target Spring MVC Architecture Design

## 1. Project Structure

```
monolith-new/
├── pom.xml
└── src/
    └── main/
        ├── java/com/skishop/
        │   ├── Application.java                        ← @SpringBootApplication
        │   ├── common/
        │   │   ├── config/AppConfig.java               ← @Configuration, reads app.properties
        │   │   └── util/PasswordHasher.java            ← Copied verbatim (SHA-256 + salt, 1000 iterations)
        │   ├── config/
        │   │   ├── DataSourceConfig.java               ← @Configuration, DataSource beans (H2 + PostgreSQL profiles)
        │   │   ├── SecurityConfig.java                 ← @Configuration, Spring Security filter chain
        │   │   ├── WebMvcConfig.java                   ← @Configuration, JSP view resolver, static resources, interceptors
        │   │   └── CsrfTokenConfig.java                ← Custom CSRF token handling (_csrfToken field name)
        │   ├── domain/                                 ← Copied verbatim from source (24 POJOs)
        │   │   ├── user/User.java, Role.java, SecurityLog.java, PasswordResetToken.java
        │   │   ├── product/Product.java, Category.java, Price.java
        │   │   ├── cart/Cart.java, CartItem.java
        │   │   ├── order/Order.java, OrderItem.java, Return.java
        │   │   ├── payment/Payment.java
        │   │   ├── coupon/Coupon.java, Campaign.java, CouponUsage.java
        │   │   ├── inventory/Inventory.java
        │   │   ├── shipping/ShippingMethod.java
        │   │   ├── address/Address.java
        │   │   ├── point/PointAccount.java, PointTransaction.java
        │   │   └── mail/EmailQueue.java, MailEvent.java
        │   ├── dao/                                    ← Interface + Impl pairs, ported to JdbcTemplate
        │   │   ├── user/UserDao.java, UserDaoImpl.java, SecurityLogDao(Impl), PasswordResetTokenDao(Impl)
        │   │   ├── product/ProductDao(Impl), PriceDao(Impl)
        │   │   ├── category/CategoryDao(Impl)
        │   │   ├── cart/CartDao(Impl), CartItemDao(Impl)
        │   │   ├── order/OrderDao(Impl), ReturnDao(Impl), OrderShippingDao(Impl)
        │   │   ├── payment/PaymentDao(Impl)
        │   │   ├── coupon/CouponDao(Impl), CouponUsageDao(Impl)
        │   │   ├── inventory/InventoryDao(Impl)
        │   │   ├── shipping/ShippingMethodDao(Impl)
        │   │   ├── address/UserAddressDao(Impl)
        │   │   ├── point/PointDao(Impl)
        │   │   └── mail/EmailQueueDao(Impl)
        │   ├── service/                                ← Ported with Spring DI, same business logic
        │   │   ├── auth/AuthService.java, AuthResult.java
        │   │   ├── user/UserService.java
        │   │   ├── catalog/ProductService.java, CategoryService.java
        │   │   ├── cart/CartService.java
        │   │   ├── order/OrderService.java, OrderFacade.java, OrderFacadeImpl.java
        │   │   ├── payment/PaymentService.java, PaymentInfo.java, PaymentResult.java
        │   │   ├── coupon/CouponService.java
        │   │   ├── inventory/InventoryService.java
        │   │   ├── shipping/ShippingService.java
        │   │   ├── tax/TaxService.java
        │   │   ├── point/PointService.java
        │   │   └── mail/MailService.java
        │   └── web/
        │       ├── controller/                         ← Spring MVC @Controller classes
        │       │   ├── HomeController.java
        │       │   ├── AuthController.java
        │       │   ├── ProductController.java
        │       │   ├── CartController.java
        │       │   ├── CheckoutController.java
        │       │   ├── CouponController.java
        │       │   ├── OrderController.java
        │       │   ├── PointController.java
        │       │   ├── AddressController.java
        │       │   ├── admin/AdminProductController.java
        │       │   ├── admin/AdminOrderController.java
        │       │   ├── admin/AdminCouponController.java
        │       │   └── admin/AdminShippingController.java
        │       ├── form/                               ← Form-backing POJOs with Jakarta Validation
        │       │   ├── LoginForm.java
        │       │   ├── RegisterForm.java
        │       │   ├── PasswordResetRequestForm.java
        │       │   ├── PasswordResetForm.java
        │       │   ├── AddCartForm.java
        │       │   ├── CheckoutForm.java
        │       │   ├── CouponForm.java
        │       │   ├── AddressForm.java
        │       │   └── admin/AdminProductForm.java, AdminCouponForm.java, AdminShippingMethodForm.java
        │       ├── interceptor/
        │       │   └── RequestIdInterceptor.java       ← Replaces RequestIdFilter
        │       └── GlobalExceptionHandler.java         ← @ControllerAdvice
        └── resources/
        │   ├── application.properties                  ← Spring Boot config (port, JSP resolver, DB)
        │   ├── application-h2.properties               ← H2 in-memory profile
        │   ├── application-postgresql.properties        ← PostgreSQL profile
        │   ├── schema.sql                              ← Copied from source db/schema.sql
        │   ├── data.sql                                ← Copied from source db/data.sql
        │   ├── messages.properties                     ← Copied from source messages.properties
        │   └── log4j2.properties                       ← (Optional) or use Spring Boot defaults
        └── webapp/
            ├── WEB-INF/
            │   └── jsp/                                ← Migrated JSPs (Struts tags → JSTL/Spring tags)
            │       ├── layouts/base.jsp                ← Tiles → jsp:include pattern
            │       ├── common/header.jsp, footer.jsp, messages.jsp
            │       ├── home.jsp
            │       ├── auth/login.jsp, register.jsp, password/forgot.jsp, reset.jsp, forgot_complete.jsp
            │       ├── products/list.jsp, detail.jsp, notfound.jsp
            │       ├── cart/view.jsp, checkout.jsp, confirmation.jsp
            │       ├── orders/history.jsp, detail.jsp
            │       ├── account/addresses.jsp, address_edit.jsp
            │       ├── coupons/available.jsp
            │       ├── points/balance.jsp
            │       └── admin/ (products/, orders/, coupons/, shipping/ — list.jsp + edit.jsp each)
            ├── assets/                                 ← Copied verbatim from source
            ├── error.jsp                               ← Global error page
            └── index.jsp                               ← Redirect to /home
```

## 2. Maven Dependencies (pom.xml)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.5</version>
</parent>

<groupId>com.skishop</groupId>
<artifactId>skishop-app</artifactId>
<version>2.0.0</version>
<packaging>jar</packaging>

<properties>
    <java.version>21</java.version>
</properties>

<dependencies>
    <!-- Spring MVC + embedded Tomcat (JSP requires tomcat-embed-jasper) -->
    <dependency>spring-boot-starter-web</dependency>
    <dependency>spring-boot-starter-security</dependency>
    <dependency>spring-boot-starter-validation</dependency>
    <dependency>spring-boot-starter-jdbc</dependency>

    <!-- JSP support -->
    <dependency>org.apache.tomcat.embed:tomcat-embed-jasper (provided→compile)</dependency>
    <dependency>jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api</dependency>
    <dependency>org.glassfish.web:jakarta.servlet.jsp.jstl (runtime)</dependency>

    <!-- Spring form tags (included in spring-webmvc, just needs taglib declaration in JSP) -->

    <!-- Database -->
    <dependency>com.h2database:h2 (runtime)</dependency>
    <dependency>org.postgresql:postgresql (runtime)</dependency>

    <!-- Mail (optional, for MailService) -->
    <dependency>spring-boot-starter-mail</dependency>

    <!-- Testing -->
    <dependency>spring-boot-starter-test (test)</dependency>
</dependencies>
```

**Critical**: `tomcat-embed-jasper` must be `compile` scope (not `provided`) for Spring Boot executable JAR.

## 3. Spring Boot Configuration

### application.properties
```properties
server.port=8080
spring.mvc.view.prefix=/WEB-INF/jsp/
spring.mvc.view.suffix=.jsp

# Disable Whitelabel error page, use custom error.jsp
server.error.whitelabel.enabled=false
server.error.path=/error

# Session config
server.servlet.session.timeout=30m

# SQL init (schema + data loaded on startup)
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
spring.sql.init.data-locations=classpath:data.sql

# Message source
spring.messages.basename=messages
spring.messages.encoding=UTF-8

# Multipart (matches source: 2MB max)
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=2MB
```

### application-h2.properties
```properties
spring.datasource.url=jdbc:h2:mem:skishop;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.sql.init.platform=h2
```

### application-postgresql.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/skishop
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=${DB_USER:skishop}
spring.datasource.password=${DB_PASS:skishop}
spring.sql.init.mode=never
```

## 4. Controller Design

### URL Routing Contract

Every controller method maps to the **exact URL** tested by `api-test.sh`. No `.do` suffixes.

#### HomeController
```java
@Controller
public class HomeController {
    @GetMapping({"/", "/home"})
    public String home(Model model) → "home"
}
```

#### AuthController
```java
@Controller
public class AuthController {
    @GetMapping("/login")      → "auth/login"      (populate empty LoginForm)
    @PostMapping("/login")     → redirect:/home on success, "auth/login" on failure
    @GetMapping("/logout")     → redirect:/home     (Spring Security handles session invalidation)
    @GetMapping("/register")   → "auth/register"    (populate empty RegisterForm)
    @PostMapping("/register")  → redirect:/login on success, "auth/register" on failure
    @GetMapping("/password/forgot")   → "auth/password/forgot"
    @PostMapping("/password/forgot")  → "auth/password/forgot_complete" on success
    @GetMapping("/password/reset")    → "auth/password/reset" (optional ?token= pre-fill)
    @PostMapping("/password/reset")   → redirect:/login on success, "auth/password/reset" on failure
}
```

**Login handling**: Spring Security's `formLogin()` is NOT used because the test script expects:
1. POST to `/login` with `email`, `password`, `_csrfToken` fields
2. On success: redirect to `/home` (follows redirect → HTTP 200)
3. On failure: re-render login page (HTTP 200 with form)

Instead, the AuthController **manually calls AuthService.authenticate()** and manages the session, just like the source LoginAction. Spring Security is used only for:
- URL-based authorization (role checks)
- Session management
- CSRF token generation

#### ProductController
```java
@Controller
public class ProductController {
    @GetMapping("/products")   → "products/list"    (params: keyword, categoryId, page, size, sort)
    @GetMapping("/product")    → "products/detail" or "products/notfound"  (param: id)
}
```

#### CartController
```java
@Controller
public class CartController {
    @GetMapping("/cart")       → "cart/view"         (create cart if needed, store in session + cookie)
    @PostMapping("/cart")      → redirect:/cart      (add item: productId, quantity)
}
```

#### CouponController
```java
@Controller
public class CouponController {
    @GetMapping("/coupons")         → "coupons/available"
    @PostMapping("/coupons/apply")  → "cart/view"    (apply coupon code to cart)
}
```

#### CheckoutController
```java
@Controller
public class CheckoutController {
    @GetMapping("/checkout")   → "cart/checkout"
    @PostMapping("/checkout")  → "cart/confirmation" on success, "cart/checkout" on failure
}
```

#### OrderController
```java
@Controller
public class OrderController {
    @GetMapping("/orders")              → "orders/history"
    @GetMapping("/orders/detail")       → "orders/detail"  (param: id)
    @PostMapping("/orders/{id}/cancel") → redirect:/orders  (path variable)
    @PostMapping("/orders/{id}/return") → redirect:/orders  (path variable)
}
```

#### PointController
```java
@Controller
public class PointController {
    @GetMapping("/points")     → "points/balance"
}
```

#### AddressController
```java
@Controller
public class AddressController {
    @GetMapping("/account/addresses")       → "account/addresses"
    @GetMapping("/account/addresses/edit")  → "account/address_edit"  (optional ?id= for existing)
    @PostMapping("/account/addresses/save") → redirect:/account/addresses on success
}
```

#### AdminProductController
```java
@Controller @RequestMapping("/admin")
public class AdminProductController {
    @GetMapping("/products")       → "admin/products/list"
    @GetMapping("/product/edit")   → "admin/products/edit"
    @PostMapping("/product/edit")  → redirect:/admin/products
    @PostMapping("/product/delete") → redirect:/admin/products
}
```

#### AdminOrderController
```java
@Controller @RequestMapping("/admin")
public class AdminOrderController {
    @GetMapping("/orders")         → "admin/orders/list"
    @GetMapping("/orders/detail")  → "admin/orders/detail"
    @PostMapping("/order/update")  → redirect:/admin/orders
    @PostMapping("/order/refund")  → redirect:/admin/orders
}
```

#### AdminCouponController
```java
@Controller @RequestMapping("/admin")
public class AdminCouponController {
    @GetMapping("/coupons")        → "admin/coupons/list"
    @GetMapping("/coupon/edit")    → "admin/coupons/edit"
    @PostMapping("/coupon/edit")   → redirect:/admin/coupons
}
```

#### AdminShippingController
```java
@Controller @RequestMapping("/admin")
public class AdminShippingController {
    @GetMapping("/shipping")       → "admin/shipping/list"
    @GetMapping("/shipping/edit")  → "admin/shipping/edit"
    @PostMapping("/shipping/edit") → redirect:/admin/shipping
}
```

## 5. Spring Security Configuration

### SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF: Use custom token repository that writes "_csrfToken" field
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            // Authorization: Match struts-config.xml roles
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/home", "/products", "/product",
                    "/login", "/register",
                    "/password/forgot", "/password/reset",
                    "/cart", "/coupons", "/checkout",
                    "/assets/**", "/error", "/favicon.ico").permitAll()
                // USER+ADMIN endpoints
                .requestMatchers("/orders/**", "/points",
                    "/account/**", "/logout",
                    "/coupons/apply").hasAnyRole("USER", "ADMIN")
                // ADMIN-only endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            // Disable Spring Security's default form login — we handle it in AuthController
            .formLogin(form -> form.disable())
            // Custom login page redirect for unauthorized access
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, resp, authEx) ->
                    resp.sendRedirect("/login"))
            )
            // Logout handled by AuthController (GET /logout → invalidate + redirect)
            .logout(logout -> logout.disable())
            // Session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );
        return http.build();
    }

    // Custom CSRF token repository: parameter name = "_csrfToken"
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repo = new HttpSessionCsrfTokenRepository();
        repo.setParameterName("_csrfToken");
        repo.setHeaderName("X-CSRF-TOKEN");
        return repo;
    }
}
```

### Authentication Strategy

Spring Security manages **authorization** (URL access control), but **authentication is manual**:

1. **AuthController.login()** calls `AuthService.authenticate(email, password, ip, ua)`
2. On success: invalidate old session, create new session (fixation prevention), store `loginUser` in session
3. **Also**: create a Spring Security `Authentication` token and set it in `SecurityContextHolder`:
   ```java
   UsernamePasswordAuthenticationToken auth =
       new UsernamePasswordAuthenticationToken(user, null,
           List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
   SecurityContextHolder.getContext().setAuthentication(auth);
   ```
4. This allows `hasRole("USER")` / `hasRole("ADMIN")` checks to work on subsequent requests.

**Logout**: AuthController invalidates session → Spring Security context cleared automatically.

### CSRF Token in JSPs

Every form JSP must include:
```jsp
<input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
```
Spring Security auto-exposes `_csrf` as a request attribute. The `parameterName` is set to `_csrfToken` in the repository config.

**Important**: The test script's `extract_csrf` function looks for `name="_csrfToken"` in the HTML. The hidden input must use this exact name.

## 6. DAO Layer Migration: Commons DBUtils → JdbcTemplate

### Pattern

**Source** (Commons DBUtils):
```java
public class UserDaoImpl extends AbstractDao implements UserDao {
    public User findByEmail(String email) {
        Connection con = null;
        try {
            con = DataSourceLocator.getInstance().getDataSource().getConnection();
            QueryRunner qr = new QueryRunner();
            return qr.query(con, "SELECT ... WHERE email = ?",
                new BeanHandler<>(User.class), email);
        } finally { closeQuietly(null, null, con); }
    }
}
```

**Target** (Spring JdbcTemplate):
```java
@Repository
public class UserDaoImpl implements UserDao {
    private final JdbcTemplate jdbc;

    public UserDaoImpl(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public User findByEmail(String email) {
        List<User> users = jdbc.query(
            "SELECT ... WHERE email = ?",
            (rs, rowNum) -> mapUser(rs),
            email);
        return users.isEmpty() ? null : users.get(0);
    }
}
```

### Key Changes
- Remove `AbstractDao` base class, `DataSourceLocator`, `DaoFactory`
- All DAOs become `@Repository` beans with constructor-injected `JdbcTemplate`
- `QueryRunner.query()` → `JdbcTemplate.query()` with `RowMapper` lambda
- `QueryRunner.update()` → `JdbcTemplate.update()`
- `BeanHandler` / `BeanListHandler` → manual `RowMapper` (maps ResultSet → POJO)
- Connection management is automatic (Spring manages DataSource and connections)
- **SQL statements stay identical** — same column names, same parameterized queries

### Transaction Management for InventoryService

The source uses manual `con.setAutoCommit(false)` in InventoryService. In the target:

```java
@Service
public class InventoryService {
    private final JdbcTemplate jdbc;
    private final PlatformTransactionManager txManager;

    @Transactional
    public void reserveItems(List<CartItem> items) {
        for (CartItem item : items) {
            // SELECT ... FOR UPDATE (pessimistic lock)
            jdbc.queryForObject(
                "SELECT ... FROM inventory WHERE product_id = ? FOR UPDATE",
                (rs, n) -> mapInventory(rs), item.getProductId());
            // UPDATE reserved_quantity
            jdbc.update("UPDATE inventory SET reserved_quantity = ...",
                item.getQuantity(), item.getProductId());
        }
    }
}
```

Similarly for `PointService.expirePoints()` — use `@Transactional`.

## 7. Service Layer Migration

### Dependency Injection

**Source**: Services instantiate DAOs via `new DaoImpl()` or get singletons from `ServiceLocator`.

**Target**: All services become `@Service` beans with constructor injection:

```java
@Service
public class OrderFacadeImpl implements OrderFacade {
    private final CartService cartService;
    private final CouponService couponService;
    private final InventoryService inventoryService;
    // ... 11 more dependencies
    
    public OrderFacadeImpl(CartService cartService, CouponService couponService, ...) {
        this.cartService = cartService;
        // ...
    }
}
```

**ServiceLocator is eliminated.** Spring's DI container replaces it.

### Business Logic

All service method signatures and business logic remain **identical** to source. The only change is:
1. DAO access via injected `@Repository` beans instead of `new DaoImpl()`
2. `DataSourceLocator.getConnection()` replaced by `JdbcTemplate` (in DAOs)
3. Manual `setAutoCommit(false)` replaced by `@Transactional` (in InventoryService, PointService)

## 8. JSP View Migration

### Layout Pattern: Tiles → JSP Includes

**Source** (Tiles base.jsp):
```jsp
<tiles:getAsString name="title"/>
<tiles:insert attribute="header"/>
<tiles:insert attribute="messages"/>
<tiles:insert attribute="body"/>
<tiles:insert attribute="footer"/>
```

**Target** (JSP includes base.jsp):
```jsp
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head><title><c:out value="${title}" default="SkiShop"/></title></head>
<body>
  <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
  <jsp:include page="${body}"/>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
```

**Alternative**: Each page JSP directly includes header/footer via `<jsp:include>` without a base template. This is simpler and avoids the `${body}` dynamic include issue.

**Recommended approach**: **Direct include in each page JSP** — each content JSP wraps itself:
```jsp
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head><title>商品一覧</title>
  <link rel="stylesheet" href="/assets/css/style.css"/>
</head>
<body>
  <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
  
  <!-- page content here -->
  
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
```

### Error/Message Display

**Source**: `<html:errors/>` displays Struts ActionMessages.

**Target**: Controllers add error/success messages to the model:
```java
model.addAttribute("errors", List.of("Invalid email or password"));
model.addAttribute("messages", List.of("Registration successful"));
```

**messages.jsp** displays them:
```jsp
<c:if test="${not empty errors}">
  <div class="error-messages">
    <c:forEach var="err" items="${errors}"><p>${err}</p></c:forEach>
  </div>
</c:if>
<c:if test="${not empty messages}">
  <div class="info-messages">
    <c:forEach var="msg" items="${messages}"><p>${msg}</p></c:forEach>
  </div>
</c:if>
```

### Form Tag Migration

For forms that need validation error binding, use Spring form tags:
```jsp
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<form:form modelAttribute="loginForm" action="/login" method="post">
  <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
  <form:input path="email"/>
  <form:errors path="email" cssClass="error"/>
  <form:password path="password"/>
  <form:errors path="password" cssClass="error"/>
  <button type="submit">Login</button>
</form:form>
```

For simpler forms, plain HTML `<form>` + `<input>` with EL expressions is acceptable.

## 9. Session Management Design

| Attribute | Type | Set By | Read By | Notes |
|-----------|------|--------|---------|-------|
| `loginUser` | `User` | AuthController (after authentication) | All controllers needing user context | Stored in both HttpSession and Spring SecurityContext |
| `cartId` | `String` | CartController (on first cart access) | CartController, CheckoutController, CouponController | Also set as CART_ID cookie (30-day, HttpOnly) |
| `SPRING_SECURITY_CONTEXT` | SecurityContext | Spring Security (auto) | Spring Security (auto) | Contains Authentication with ROLE_USER/ROLE_ADMIN |

### Session Lifecycle
1. **Login**: Invalidate existing session → create new → set `loginUser` + SecurityContext
2. **Cart**: Create cart row in DB → store `cartId` in session + CART_ID cookie
3. **Checkout**: Use `cartId` from session → place order → clear cart
4. **Logout**: Invalidate session → redirect to /home

## 10. Form Validation Design

### Jakarta Validation Annotations on Form POJOs

```java
public class LoginForm {
    @NotBlank @Email
    private String email;
    
    @NotBlank @Size(min = 8)
    private String password;
}

public class RegisterForm {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String username;
    @NotBlank @Size(min = 8)
    private String password;
    @NotBlank
    private String passwordConfirm;
    // Custom validation: password == passwordConfirm (in controller or custom validator)
}

public class CheckoutForm {
    private String cartId;          // from session, not user input
    @NotBlank
    private String paymentMethod;
    @NotBlank @Pattern(regexp = "^[0-9]{12,19}$")
    private String cardNumber;      // Luhn validation in PaymentService
    @NotNull @Min(1) @Max(12)
    private Integer cardExpMonth;
    @NotNull
    private Integer cardExpYear;
    @NotBlank @Pattern(regexp = "^[0-9]{3,4}$")
    private String cardCvv;
    @NotBlank
    private String billingZip;
    private int usePoints;          // defaults to 0
    private String couponCode;      // optional
}

public class AddressForm {
    private String id;              // null for new, populated for edit
    @NotBlank private String label;
    @NotBlank private String recipientName;
    @NotBlank @Pattern(regexp = "^[0-9]{3}-?[0-9]{4}$")
    private String postalCode;
    @NotBlank private String prefecture;
    @NotBlank private String address1;
    private String address2;
    @NotBlank @Pattern(regexp = "^0[0-9]{9,10}$")
    private String phone;
    private boolean isDefault;
}
```

### Controller Pattern for Validation

```java
@PostMapping("/login")
public String login(@Valid @ModelAttribute LoginForm form,
                    BindingResult bindingResult,
                    HttpServletRequest request, Model model) {
    if (bindingResult.hasErrors()) {
        return "auth/login";  // Re-render with validation errors
    }
    // Business logic...
}
```

## 11. Naming Conventions

| Artifact | Convention | Example |
|----------|-----------|---------|
| Controller class | `{Domain}Controller` | `ProductController`, `AdminProductController` |
| Service class | `{Domain}Service` | `ProductService`, `CartService` |
| DAO interface | `{Domain}Dao` | `ProductDao`, `UserDao` |
| DAO impl | `{Domain}DaoImpl` | `ProductDaoImpl` |
| Form class | `{Action}Form` | `LoginForm`, `CheckoutForm` |
| Domain class | `{Entity}` | `Product`, `Order`, `User` |
| JSP view | `{module}/{action}.jsp` | `auth/login.jsp`, `products/list.jsp` |
| Config class | `{Concern}Config` | `SecurityConfig`, `WebMvcConfig` |
| View name (returned by controller) | `{module}/{action}` | `"auth/login"`, `"products/list"` |

## 12. Static Resource Serving

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("/assets/");
    }
}
```

This keeps all CSS, JS, and images at the same `/assets/...` paths as the source.

## 13. Error Handling

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public String handleError(Exception ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";  // → /WEB-INF/jsp/error.jsp
    }
}
```

This replaces Struts' `<global-exceptions>` configuration.
