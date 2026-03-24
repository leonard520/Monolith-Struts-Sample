## Phase 3: P1 Features — Product Catalog, Auth, Cart & Checkout

**Batch**: 3
**Goal**: Implement all Priority 1 features: product browsing, authentication, cart, checkout.
**User Stories**: US-1 (Browse Products), US-2 (Registration & Auth), US-3 (Cart & Checkout)
**Task ID Range**: T300–T363

---

### 3.1 HomeController

- [ ] T300 [US-1] [Plan:3.1] Create `HomeController` `@Controller` class in `src/main/java/com/skishop/web/controller/HomeController.java`. Map `GET /home` → return view name `home`. No service dependencies. No business logic.
- [ ] T301 [P] [US-1] [Plan:3.1] Create unit test `src/test/java/com/skishop/web/controller/HomeControllerTest.java`. Use `MockMvc` to verify `GET /home` returns HTTP 200 and resolves to `home` view.

---

### 3.2 ProductController

- [ ] T302 [US-1] [Plan:3.2] Create `ProductController` `@Controller` class in `src/main/java/com/skishop/web/controller/ProductController.java`. Inject `ProductService` and `CategoryService` via constructor injection.
- [ ] T303 [US-1] [Plan:3.2] Implement `GET /products` handler in `ProductController`. Accept `@RequestParam` for `page` (default 1), `size` (default 10), `keyword`, `categoryId`, `sort`. Compute offset as `(page-1)*size`. Call `productService.search(keyword, categoryId, sort, offset, size)` and `categoryService.listAll()`. Build category options list (add "指定なし" empty-value option first). Set model attributes: `productList`, `categoryOptions`, `page`, `size`. Return view name `products/list`. Replicate `ProductListAction` behavior exactly.
- [ ] T304 [US-1] [Plan:3.2] Implement `GET /product` handler in `ProductController`. Accept `@RequestParam String id`. If `id` is null/empty or `productService.findById(id)` returns null → return view name `products/notfound`. Otherwise set `product` model attribute and return view name `products/detail`. Replicate `ProductDetailAction` behavior exactly.
- [ ] T305 [P] [US-1] [Plan:3.2] Create unit test `src/test/java/com/skishop/web/controller/ProductControllerTest.java`. Test scenarios: (a) `GET /products` with no params → 200 with default pagination; (b) `GET /products?keyword=Atomic&categoryId=c-1&page=2` → 200 with filtered results; (c) `GET /product?id=PSK001` → 200 with product detail view; (d) `GET /product?id=NONEXISTENT` → 200 with `products/notfound` view; (e) `GET /product` with no id param → `products/notfound` view.

---

### 3.3 LoginController

- [ ] T306 [US-2] [Plan:3.3] Create `LoginController` `@Controller` class in `src/main/java/com/skishop/web/controller/LoginController.java`. Inject `AuthService` via constructor injection.
- [ ] T307 [US-2] [Plan:3.3] Implement `GET /login` handler in `LoginController`. Return view name `auth/login`. No model attributes needed.
- [ ] T308 [US-2] [Plan:3.3] Implement `POST /login` handler in `LoginController`. Accept `@ModelAttribute LoginForm`, `HttpServletRequest`, `HttpSession`, `Model`. Call `authService.authenticate(form.getEmail(), form.getPassword(), request.getRemoteAddr(), request.getHeader("User-Agent"))`. On failure: add error message attribute, return view `auth/login`. On success: invalidate old session, create new session, set `loginUser` session attribute to `result.getUser()`, redirect to `/home`.
- [ ] T309 [P] [US-2] [Plan:3.3] Create unit test `src/test/java/com/skishop/web/controller/LoginControllerTest.java`. Test scenarios: (a) `GET /login` → 200 with login view; (b) `POST /login` valid credentials → redirect to `/home` with session `loginUser` set; (c) `POST /login` invalid credentials → 200 with `auth/login` view and error message.

---

### 3.4 RegisterController

- [ ] T310 [US-2] [Plan:3.4] Create `RegisterController` `@Controller` class in `src/main/java/com/skishop/web/controller/RegisterController.java`. Inject `UserService` via constructor injection.
- [ ] T311 [US-2] [Plan:3.4] Implement `GET /register` handler in `RegisterController`. Return view name `auth/register`.
- [ ] T312 [US-2] [Plan:3.4] Implement `POST /register` handler in `RegisterController`. Accept `@ModelAttribute RegisterForm`, `BindingResult`, `Model`. Validate form (required fields: email, username, password, passwordConfirm; password match check). Check `userService.findByEmail(email)` for duplicate. On validation/duplicate error: add errors to model, return `auth/register`. On success: create `User` with `PasswordHasher.generateSalt()` / `PasswordHasher.hash()`, call `userService.register(user)`, redirect to `/login`.
- [ ] T313 [P] [US-2] [Plan:3.4] Create unit test `src/test/java/com/skishop/web/controller/RegisterControllerTest.java`. Test scenarios: (a) `GET /register` → 200 with register view; (b) `POST /register` valid data → redirect to `/login`; (c) `POST /register` duplicate email → 200 with error; (d) `POST /register` password mismatch → 200 with validation error; (e) `POST /register` missing required fields → 200 with validation errors.

---

### 3.5 LogoutController

- [ ] T314 [US-2] [Plan:3.5] Create `LogoutController` `@Controller` class in `src/main/java/com/skishop/web/controller/LogoutController.java`. Implement `GET /logout` handler: invalidate session if present, redirect to `/home`. No service dependencies. Replicate `LogoutAction` behavior.
- [ ] T315 [P] [US-2] [Plan:3.5] Create unit test `src/test/java/com/skishop/web/controller/LogoutControllerTest.java`. Test scenarios: (a) `GET /logout` with active session → session invalidated, redirect to `/home`; (b) `GET /logout` with no session → redirect to `/home`.

---

### 3.6 CartController

- [ ] T316 [US-3] [Plan:3.6] Create `CartController` `@Controller` class in `src/main/java/com/skishop/web/controller/CartController.java`. Inject `CartService` via constructor injection.
- [ ] T317 [US-3] [Plan:3.6] Implement `GET /cart` handler in `CartController`. Get or create cart via session: read `cartId` from session; if null, call `cartService.createCart(userId, sessionId)` and store `cartId` in session + set `CART_ID` HttpOnly cookie (Max-Age 30 days). Retrieve items via `cartService.getItems(cartId)`, compute subtotal via `cartService.calculateSubtotal(items)`. Set model attributes: `cartItems`, `cartSubtotal`, `cartId`. Return view `cart/view`. Replicate `CartAction` behavior including cookie logic.
- [ ] T318 [US-3] [Plan:3.6] Implement `POST /cart` handler in `CartController`. Accept `@ModelAttribute AddCartForm`. Read/create `cartId` from session (same logic as GET). If `productId` is non-empty, call `cartService.addItem(cartId, productId, quantity)`. Redirect to `/cart`.
- [ ] T319 [P] [US-3] [Plan:3.6] Create unit test `src/test/java/com/skishop/web/controller/CartControllerTest.java`. Test scenarios: (a) `GET /cart` no existing session cart → creates cart, sets session attribute, returns view; (b) `GET /cart` with existing cart → shows items; (c) `POST /cart` with valid productId → adds item, redirects to `/cart`; (d) `POST /cart` with empty productId → redirects to `/cart` without adding.

---

### 3.7 CouponController

- [ ] T320 [US-3] [Plan:3.7] Create `CouponController` `@Controller` class in `src/main/java/com/skishop/web/controller/CouponController.java`. Inject `CouponService` and `CartService` via constructor injection.
- [ ] T321 [US-3] [Plan:3.7] Implement `GET /coupons` handler in `CouponController`. Call `couponService.listActiveCoupons()`, set `coupons` model attribute, return view `coupons/available`. This is a public endpoint (no auth required). Replicate `CouponAvailableAction`.
- [ ] T322 [US-3] [Plan:3.7] Implement `POST /coupons/apply` handler in `CouponController`. Requires authentication (enforced by `AuthInterceptor`). Accept `@ModelAttribute CouponForm`, `HttpSession`, `Model`. Read `cartId` from session; if null → add error, redirect to `/cart`. Get cart items + subtotal, call `couponService.validateCoupon(code, subtotal)`. On success: compute discount via `couponService.calculateDiscount(coupon, subtotal)`, store coupon info in session, set model attributes (`cartItems`, `cartSubtotal`, `coupon`, `discountAmount`), return view `cart/view`. On `IllegalArgumentException`: add error message, set cart model attributes, return view `cart/view`. Replicate `CouponApplyAction` behavior.
- [ ] T323 [P] [US-3] [Plan:3.7] Create unit test `src/test/java/com/skishop/web/controller/CouponControllerTest.java`. Test scenarios: (a) `GET /coupons` → 200 with coupons list; (b) `POST /coupons/apply` valid coupon → success with discount; (c) `POST /coupons/apply` invalid coupon → error message; (d) `POST /coupons/apply` no cart in session → error.

---

### 3.8 CheckoutController

- [ ] T324 [US-3] [Plan:3.8] Create `CheckoutController` `@Controller` class in `src/main/java/com/skishop/web/controller/CheckoutController.java`. Inject `OrderFacade` via constructor injection.
- [ ] T325 [US-3] [Plan:3.8] Implement `GET /checkout` handler in `CheckoutController`. Requires authentication (enforced by `AuthInterceptor`). Return view `cart/checkout`.
- [ ] T326 [US-3] [Plan:3.8] Implement `POST /checkout` handler in `CheckoutController`. Accept `@ModelAttribute CheckoutForm`, `HttpSession`, `Model`. Read `cartId` from session (fall back to `checkoutForm.getCartId()`). Get `loginUser` from session for `userId`. Call `orderFacade.placeOrder(cartId, checkoutForm.getCouponCode(), checkoutForm.getUsePoints(), checkoutForm.toPaymentInfo(), userId)`. On success: set `order` model attribute, return view `cart/confirmation`. On `RuntimeException`: add error message, return view `cart/checkout`. Replicate `CheckoutAction` behavior.
- [ ] T327 [P] [US-3] [Plan:3.8] Create unit test `src/test/java/com/skishop/web/controller/CheckoutControllerTest.java`. Test scenarios: (a) `GET /checkout` → 200 with checkout view; (b) `POST /checkout` successful order → 200 with confirmation view and order; (c) `POST /checkout` failure (RuntimeException from facade) → 200 with checkout view and error.

---

### 3.9 Form Objects (P1)

- [ ] T328 [US-2] [Plan:3.9] Rewrite `LoginForm` in `src/main/java/com/skishop/web/form/LoginForm.java`. Remove `ValidatorForm` extends. Convert to plain POJO. Add `@NotBlank` Bean Validation annotations on `email` and `password` fields. Keep getters/setters.
- [ ] T329 [US-2] [Plan:3.9] Rewrite `RegisterForm` in `src/main/java/com/skishop/web/form/RegisterForm.java`. Remove `ValidatorForm` extends. Convert to plain POJO. Add `@NotBlank` on `email`, `username`, `password`, `passwordConfirm`. Remove Struts `validate()` method. Password match validation will be done in controller (programmatic check or custom validator).
- [ ] T330 [US-1] [Plan:3.9] Rewrite `ProductSearchForm` in `src/main/java/com/skishop/web/form/ProductSearchForm.java`. Remove `ValidatorForm` extends. Convert to plain POJO. Fields: `page` (int), `size` (int), `keyword` (String), `categoryId` (String), `sort` (String). No validation annotations needed (all optional with defaults).
- [ ] T331 [US-3] [Plan:3.9] Rewrite `AddCartForm` in `src/main/java/com/skishop/web/form/AddCartForm.java`. Remove `ValidatorForm` extends. Convert to plain POJO. Add `@NotBlank` on `productId`, ensure `quantity` defaults to 1.
- [ ] T332 [US-3] [Plan:3.9] Rewrite `CheckoutForm` in `src/main/java/com/skishop/web/form/CheckoutForm.java`. Remove `ValidatorForm` extends. Convert to plain POJO. Add `@NotBlank` on `cardNumber`, `cardExpMonth`, `cardExpYear`, `cardCvv`, `billingZip`. Preserve `toPaymentInfo()` method. Remove Struts `validate()` method.
- [ ] T333 [US-3] [Plan:3.9] Rewrite `CouponForm` in `src/main/java/com/skishop/web/form/CouponForm.java`. Remove `ValidatorForm` extends (if present). Convert to plain POJO. Add `@NotBlank` on `code` field.
- [ ] T334 [P] [Plan:3.9] Remove `FormValidationUtils.java` dependency on Struts types. Update `src/main/java/com/skishop/web/form/FormValidationUtils.java` to use Spring `Errors`/`BindingResult` instead of `ActionErrors` for password mismatch validation helper, or inline the logic into controllers that need it.

---

### 3.10 JSP Views (P1)

- [ ] T335 [US-1] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/home.jsp`. Replace any Struts tag libraries (`<html:*>`, `<bean:*>`, `<logic:*>`) with JSTL (`<c:*>`, `<fmt:*>`) and Spring form tags. Ensure it renders within the base layout template (`layouts/base.jsp`) via `<jsp:include>` or SiteMesh/Tiles-equivalent approach established in Phase 2.
- [ ] T336 [US-1] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/products/list.jsp`. Replace Struts iteration (`<logic:iterate>`) with `<c:forEach>`. Replace `<bean:write>` with `<c:out>`. Replace Struts form tags with Spring/JSTL equivalents. Ensure pagination links use `page`, `size`, `keyword`, `categoryId` query params. Category filter dropdown uses `categoryOptions` model attribute.
- [ ] T337 [US-1] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/products/detail.jsp`. Replace Struts tags with JSTL. Display product name, description, price, category from `product` model attribute. Include "Add to Cart" form posting to `/cart` with `productId` and `quantity` fields plus `<skishop:csrfToken/>`.
- [ ] T338 [P] [US-1] [Plan:3.10] Verify `src/main/webapp/WEB-INF/jsp/products/notfound.jsp` has no Struts tag dependencies. Update if needed to use JSTL only.
- [ ] T339 [US-2] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/auth/login.jsp`. Replace `<html:form>` with `<form>` posting to `/login`. Replace `<html:text>`, `<html:password>` with standard `<input>` tags. Add `<skishop:csrfToken/>` hidden field. Display error messages from model attribute using `<c:if>` / `<c:out>`.
- [ ] T340 [US-2] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/auth/register.jsp`. Replace `<html:form>` with `<form>` posting to `/register`. Replace Struts input tags with standard HTML inputs. Add `<skishop:csrfToken/>`. Display validation errors using Spring `<form:errors>` or JSTL conditionals.
- [ ] T341 [US-3] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/cart/view.jsp`. Replace Struts iteration with `<c:forEach>` for `cartItems`. Display subtotal, discount (if coupon applied), cart total. Include coupon apply form (POST to `/coupons/apply` with `code` field + CSRF token). Include link to `/checkout`.
- [ ] T342 [US-3] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/cart/checkout.jsp`. Replace `<html:form>` with `<form>` posting to `/checkout`. Include payment fields: `cardNumber`, `cardExpMonth`, `cardExpYear`, `cardCvv`, `billingZip`, `usePoints`. Add `<skishop:csrfToken/>`. Display errors from model attribute.
- [ ] T343 [US-3] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/cart/confirmation.jsp`. Replace Struts tags with JSTL. Display `order` model attributes: order number, total, status. No form needed.
- [ ] T344 [US-3] [Plan:3.10] Migrate `src/main/webapp/WEB-INF/jsp/coupons/available.jsp`. Replace Struts iteration with `<c:forEach>` for `coupons` list. Display coupon code, type, discount value, expiration. No form needed (read-only listing).

---

### 3.11 Compile + Test Gate

- [ ] T345 [Plan:3.11] Run `mvn -B compile` and fix any compilation errors in Phase 3 controllers, forms, and JSPs. Ensure all new `@Controller` classes are component-scanned. Ensure all injected services (`ProductService`, `CategoryService`, `AuthService`, `UserService`, `CartService`, `CouponService`, `OrderFacade`) are available as Spring beans (annotated with `@Service` or `@Component`).
- [ ] T346 [Plan:3.11] Verify service layer Spring compatibility. Ensure `ProductService`, `CategoryService`, `AuthService`, `UserService`, `CartService`, `CouponService`, `OrderFacade`/`OrderFacadeImpl` have `@Service` annotation and use constructor injection for their DAO dependencies (instead of direct instantiation). This is required for controllers to inject them.
- [ ] T347 [Plan:3.11] Delete Struts Action classes replaced by Phase 3 controllers: `ProductListAction.java`, `ProductDetailAction.java`, `LoginAction.java`, `RegisterAction.java`, `LogoutAction.java`, `CartAction.java`, `CouponApplyAction.java`, `CouponAvailableAction.java`, `CheckoutAction.java` from `src/main/java/com/skishop/web/action/`.
- [ ] T348 [Plan:3.11] Delete or migrate Struts test classes replaced by Phase 3 controller tests: `ProductListActionTest.java`, `ProductDetailActionTest.java`, `LoginActionTest.java`, `RegisterActionTest.java`, `LogoutActionTest.java`, `CartActionTest.java`, `CouponApplyActionTest.java`, `CheckoutActionTest.java` from `src/test/java/com/skishop/web/action/`.
- [ ] T349 [Plan:3.11] Run all Phase 3 controller unit tests. Verify: HomeController, ProductController, LoginController, RegisterController, LogoutController, CartController, CouponController, CheckoutController tests pass.
- [ ] T350 [Plan:3.11] End-to-end smoke test: verify key P1 flows work — `GET /home` (200), `GET /products` (200 with list), `GET /product?id=PSK001` (200 with detail), `GET /login` (200), `POST /login` (redirect), `GET /cart` (200), `GET /coupons` (200), `GET /checkout` (200 when authenticated). Run `mvn -B compile` one final time to confirm clean build.
