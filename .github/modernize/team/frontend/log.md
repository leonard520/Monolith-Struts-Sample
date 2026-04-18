## [t6] Common layout & static assets — Struts/Tiles → JSTL + jsp:include

- **JSP packaging in JAR**: `src/main/webapp` is NOT included in JAR packaging by default. Must add `<resource><directory>src/main/webapp</directory><targetPath>META-INF/resources</targetPath></resource>` to pom.xml. This puts files at `META-INF/resources/` in the JAR root (not under BOOT-INF/classes/), which embedded Tomcat recognizes per Servlet 3.0 spec.
- **No Tiles replacement needed**: Simple `<jsp:include>` for fragments is sufficient. Each page JSP is a complete HTML document — no tag files or custom view resolvers needed.
- **Login state pattern**: Source uses `session.getAttribute("loginUser")` → use `${sessionScope.loginUser}` in JSTL. Role check: `${sessionScope.loginUser.role == 'ADMIN'}`.
- **Error page dual placement**: Need error.jsp at BOTH `webapp/error.jsp` (servlet-level) and `WEB-INF/jsp/error.jsp` (view resolver target for GlobalExceptionHandler returning "error").
- **index.jsp as welcome file**: With Spring Boot's DispatcherServlet handling all requests, `index.jsp` at webapp root is NOT automatically served for `/`. The HomeController (t9) needs to handle both `/` and `/home`. The index.jsp redirect acts as a fallback only.
- **Screenshot labels are Japanese**, source code labels are Chinese. Used Japanese to match screenshots (visual truth per constitution).

## [t13] Auth & Home JSPs — Struts tags → JSTL + plain HTML forms

- **Form model attribute names must match controller @ModelAttribute exactly**: `loginForm`, `registerForm`, `passwordResetRequestForm`, `passwordResetForm`. If they don't match, Spring creates a new empty object and form re-population on validation error breaks.
- **CSRF token attribute order matters for api-test.sh**: The `extract_csrf` regex in the test script tries `name` before `value` first, then `value` before `name`. Both orders work, but `name="_csrfToken" value="${_csrf.token}"` is the primary pattern.
- **No Spring form taglib needed**: Plain `<input name="...">` with `<c:out>` for value escaping is sufficient and simpler. Spring's `<form:input path="...">` would also work but requires `commandName`/`modelAttribute` on the form tag and generates more complex HTML.
- **Home page text from screenshot**: Hero section uses Japanese ("Ski Resort Shop へようこそ", "最新のスキー用品をご覧ください。", "商品を見る") to match the screenshot. Product section kept Chinese from source since no screenshot available for that area.
- **forgot_complete.jsp has no form**: Display-only page, no CSRF token needed. Shows `resetToken` for dev/test use via `<c:out>` (XSS-safe).

## [t14] Product, Cart & Coupon JSPs
- Product list uses `.products-grid`/`.product-card` card layout matching screenshot — NOT table layout from original source JSP
- Pagination preserves query params (keyword, categoryId, sort) across page links
- `productList.size() >= size` is used as heuristic for "has next page" since controller doesn't provide total count
- Cart view serves dual purpose: CartController.viewCart() and CouponController.apply() both render `cart/view`
- `errorMessage` attribute for coupon errors is separate from the `error`/`errors` contract in messages.jsp — rendered directly in cart/view.jsp
- Category dropdown uses `LabelValue` inner class from ProductController — `opt.label` and `opt.value` properties

## [t17.1] Fix review bugs: addresses.jsp EL + admin JSP CSRF tokens
- **EL boolean gotcha**: `isDefault()` getter maps to `${address.isDefault}` in EL, NOT `${address.default}` (reserved keyword). This caused HTTP 500.
- **CSRF token two-path problem**: `${_csrfToken}` (interceptor model attr) vs `${_csrf.token}` (Spring Security request attr). The interceptor path breaks on redirect (postHandle not called). Always use `${_csrf.token}`.
- **Parameter name vs value**: `name="_csrfToken"` is the configured parameter name and stays. Only the value expression changes.
