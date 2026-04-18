# t14 — Created Files

## JSP Files

| File | View Name | Controller | Migration Notes |
|------|-----------|------------|-----------------|
| `monolith-new/src/main/webapp/WEB-INF/jsp/products/list.jsp` | `products/list` | `ProductController.list()` | `<html:form>` → `<form>`, `<html:select>` → `<select>`, `<logic:iterate>` → `<c:forEach>`, `<html:optionsCollection>` → `<c:forEach>` with `<option>`. Product grid uses `.products-grid`/`.product-card` card layout matching screenshot (name link, brand, status tag, detail button). Pagination links preserve keyword/categoryId/sort params. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/products/detail.jsp` | `products/detail` | `ProductController.detail()` | `<bean:write>` → `<c:out>`, `<logic:present>` → `<c:if>`. Add-to-cart form POSTs to `/cart` with CSRF token. Shows name, brand, SKU, category, description, price. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/products/notfound.jsp` | `products/notfound` | `ProductController.detail()` | `<html:link>` → `<a href="<c:url>">`. Simple message with back link to `/products`. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/cart/view.jsp` | `cart/view` | `CartController.viewCart()`, `CouponController.apply()` | `<logic:empty/notEmpty>` → `<c:if>`, `<bean:write>` → `<c:out>`. Shows items table (name, quantity, unit price), subtotal, coupon/discount when applied. Coupon form POSTs to `/coupons/apply` with CSRF token. `errorMessage` attribute displayed for coupon errors. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/coupons/available.jsp` | `coupons/available` | `CouponController.available()` | `<logic:iterate>` → `<c:forEach>`, `<bean:write>` → `<c:out>`. Table with code, type, discount value, min amount, max discount, expiry. |

## Struts Tag Migration Summary

| Struts Tag | JSTL/Spring Replacement |
|-----------|------------------------|
| `<html:form action="...">` | `<form action="<c:url value='...'/>">` |
| `<html:text property="...">` | `<input type="text" name="..." value="<c:out value='${param....}'/>">` |
| `<html:select property="...">` | `<select name="...">` |
| `<html:optionsCollection>` | `<c:forEach>` + `<option>` |
| `<html:hidden property="..." value="...">` | `<input type="hidden" name="..." value="...">` |
| `<html:submit value="...">` | `<button type="submit" class="btn">...</button>` |
| `<html:token/>` | `<input type="hidden" name="_csrfToken" value="${_csrf.token}"/>` |
| `<html:link page="..." paramId="..." ...>` | `<a href="<c:url value='...'/>?id=...">` |
| `<bean:write name="..." property="...">` | `<c:out value="${...}"/>` |
| `<logic:iterate id="..." name="...">` | `<c:forEach var="..." items="${...}">` |
| `<logic:empty name="...">` | `<c:if test="${empty ...}">` |
| `<logic:notEmpty name="...">` | `<c:if test="${not empty ...}">` |
| `<logic:present name="...">` | `<c:if test="${not empty ...}">` |

## Model Attribute Contracts

### ProductController → products/list.jsp
- `productList` — `List<Product>` (search results)
- `categoryOptions` — `List<LabelValue>` (dropdown options with label/value)
- `page` — `int` (current page number, 1-based)
- `size` — `int` (items per page)

### ProductController → products/detail.jsp
- `product` — `Product` (single product)

### CartController / CouponController → cart/view.jsp
- `cartItems` — `List<CartItem>` (items in cart)
- `cartSubtotal` — `BigDecimal` (sum of items)
- `cartId` — `String`
- `coupon` — `Coupon` (applied coupon, optional)
- `discountAmount` — `BigDecimal` (discount, optional)
- `errorMessage` — `String` (coupon error, optional)

### CouponController → coupons/available.jsp
- `coupons` — `List<Coupon>` (active coupons)

## Verification
- `mvn -f monolith-new/pom.xml clean package -DskipTests -q` → **PASS**
- All 5 JSPs present in JAR under `META-INF/resources/WEB-INF/jsp/`
- CSRF token field: `name="_csrfToken"` on all POST forms
- Layout pattern: full HTML doc with header/messages/footer includes
