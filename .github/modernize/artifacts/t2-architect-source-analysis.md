# t2 — Source Codebase Analysis

## Project Classification
- **Type**: Monolithic MVC web application (WAR)
- **Framework**: Apache Struts 1.3.10 with Tiles layout
- **Language**: Java 5 (source/target in pom.xml)
- **Build**: Maven
- **DB**: PostgreSQL (JDBC via Commons DBUtils, pooled by Commons DBCP)
- **View**: JSP + Struts taglibs (`html:`, `bean:`, `logic:`) + Tiles composite views

## Layer Summary

| Layer | Package | File Count | Pattern |
|-------|---------|-----------|---------|
| Common/Infra | `com.skishop.common.*` | 8 | Singletons: AppConfig, DataSourceLocator, DaoFactory, ServiceLocator, PasswordHasher |
| Domain | `com.skishop.domain.*` | 24 | Plain POJOs (no JPA annotations), VARCHAR(36) UUID primary keys |
| DAO | `com.skishop.dao.*` | 38 | Interface + Impl pairs, extend AbstractDao, Commons DBUtils QueryRunner |
| Service | `com.skishop.service.*` | 18 | Business logic, direct `new` for DAO instantiation, OrderFacade orchestrator |
| Web/Actions | `com.skishop.web.action.*` | 33 | Struts Action subclasses (22 user + 11 admin) |
| Web/Forms | `com.skishop.web.form.*` | 13 | ValidatorForm subclasses with Struts XML validation |
| Web/Security | `com.skishop.web.processor` | 1 | AuthRequestProcessor extends TilesRequestProcessor |
| Web/Filter | `com.skishop.web.filter` | 1 | RequestIdFilter for request correlation |
| Web/Tag | `com.skishop.web.tag` | 1 | TokenTag for CSRF hidden field |
| JSP Views | `WEB-INF/jsp/` | 32 | Tiles layout: base.jsp → header/messages/body/footer |

## Database Schema (22 tables)

### User Domain
- `roles` — id(VARCHAR 36), name
- `users` — id, email (UNIQUE), username, password_hash, salt, status, role, created_at, updated_at
- `security_logs` — id, user_id(FK), event_type, ip_address, user_agent, details_json
- `password_reset_tokens` — id, user_id, token(UNIQUE), expires_at, used_at

### Product Domain
- `categories` — id, name, parent_id (7 categories: Ski, Boots, Wear, Helmet, Glove, Pole, Wax)
- `products` — id(VARCHAR 20), name, brand, description, category_id(FK), sku, status, created_at, updated_at
- `prices` — id, product_id, regular_price, sale_price, currency_code (JPY), sale_start_date, sale_end_date
- `inventory` — id, product_id(UNIQUE), quantity, reserved_quantity, status

### Cart Domain
- `carts` — id, user_id (nullable for guests), session_id, status, expires_at
- `cart_items` — id, cart_id, product_id, quantity, unit_price; UNIQUE(cart_id, product_id)

### Order Domain
- `orders` — id, order_number(UNIQUE), user_id, status, payment_status, subtotal, tax, shipping_fee, discount_amount, total_amount, coupon_code, used_points, created_at, updated_at
- `order_items` — id, order_id, product_id, product_name, sku, unit_price, quantity, subtotal
- `order_shipping` — id, order_id, recipient_name, postal_code, prefecture, address1, address2, phone, shipping_method_code, shipping_fee, requested_delivery_date
- `shipments` — id, order_id, carrier, tracking_number, status, shipped_at, delivered_at
- `returns` — id, order_id, order_item_id, reason, quantity, refund_amount, status
- `payments` — id, order_id, cart_id, amount, currency, status, payment_intent_id, created_at

### Loyalty Domain
- `point_accounts` — id, user_id, balance, lifetime_earned, lifetime_redeemed
- `point_transactions` — id, user_id, type(EARN/REDEEM/REFUND/REVOKE), amount, reference_id, description, expires_at, is_expired, created_at

### Marketing Domain
- `campaigns` — id, name, description, type, start_date, end_date, is_active, rules_json
- `coupons` — id, campaign_id, code(UNIQUE), coupon_type, discount_value, discount_type, minimum_amount, maximum_discount, usage_limit, used_count, is_active, expires_at
- `coupon_usage` — id, coupon_id, user_id, order_id, discount_applied, used_at

### Other
- `user_addresses` — id, user_id, label, recipient_name, postal_code, prefecture, address1, address2, phone, is_default, created_at, updated_at
- `shipping_methods` — id, code(UNIQUE), name, fee, is_active, sort_order
- `email_queue` — id, to_addr, subject, body, status, retry_count, last_error, scheduled_at, sent_at

## Service Layer Key Patterns

### Connection Management
- `DataSourceLocator` singleton → JNDI lookup (`java:comp/env/jdbc/skishop`) with fallback to `DataSourceFactory.createFromConfig()` (Commons DBCP pool)
- Every DAO method: get connection → use → closeQuietly in finally
- No connection reuse across DAO calls within a service method

### Transaction Boundaries
Only **two** explicit DB transactions exist:
1. **InventoryService.reserveItems/releaseItems** — `setAutoCommit(false)` + `SELECT ... FOR UPDATE` pessimistic locking on inventory rows
2. **PointService.expirePoints** — `setAutoCommit(false)` + `FOR UPDATE` on point_accounts

All other multi-DAO operations (including OrderFacade.placeOrder with 16+ DB calls) rely on **exception-based compensating actions** (void payment, release inventory, refund points).

### OrderFacade (Critical Orchestrator)
`placeOrder(cartId, couponCode, usePoints, paymentInfo, userId)` — 18-step workflow:
1. Validate cart → 2. Load items → 3. Calculate subtotal → 4. Validate coupon → 5. Calculate discount → 6. Redeem points → 7. Calculate tax (10% flat) → 8. Calculate shipping (free over ¥10,000, else ¥800) → 9. Reserve inventory (TX) → 10. Authorize payment (Luhn validation) → 11. Create order + items → 12. Mark coupon used → 13. Award points (1% of total) → 14. Save shipping → 15. Clear cart → 16. Queue confirmation email

Compensating actions on failure: void payment, release inventory, refund points.

### Password Hashing
`PasswordHasher` — SHA-256 with per-user UUID salt, 1000 iterations. Algorithm:
```
hash = SHA256(password + salt)  // repeated 1000 times
```
Existing user password hashes in `data.sql` use this exact algorithm — the target **must replicate it** or existing users can't log in.

### Authentication Flow (AuthService)
1. Look up user by email
2. Check status (LOCKED → reject)
3. Hash provided password with user's salt → compare with stored hash
4. On failure: log security event, increment failure count, lock after 5 failures
5. On success: return `AuthResult.success(user)`

### Session Attributes
| Key | Type | Set By | Lifecycle |
|-----|------|--------|-----------|
| `loginUser` | `User` | LoginAction (after session invalidation + new session) | Login → Logout |
| `cartId` | `String` | CartAction (also CART_ID cookie, 30-day, HttpOnly) | First cart access → Checkout |

### CSRF Token (AuthRequestProcessor)
- `TokenProcessor.getInstance().saveToken(request)` → sets `org.apache.struts.action.TOKEN` in session
- `TokenTag` outputs `<input type="hidden" name="_csrfToken" value="..."/>`
- Note: Token is per-session, not per-request

## JSP View Layer

### Tiles Layout Structure
`baseLayout` (base.jsp) → header.jsp, messages.jsp, {body}, footer.jsp

27 tile definitions extending baseLayout — each overrides `title` and `body`.

### Struts Tag Usage (to be replaced)
| Struts Tag | Replacement | Instances |
|-----------|-------------|-----------|
| `<html:form action="...">` | `<form:form modelAttribute="..." action="...">` or plain `<form>` | All form JSPs |
| `<html:text property="..."/>` | `<form:input path="..."/>` or `<input name="..."/>` | All form fields |
| `<html:password property="..."/>` | `<form:password path="..."/>` | Login, register, reset |
| `<html:hidden property="..."/>` | `<form:hidden path="..."/>` or `<input type="hidden">` | Cart, checkout, admin forms |
| `<html:token/>` | `<input type="hidden" name="_csrfToken" value="${_csrfToken}"/>` | All POST forms |
| `<html:submit/>` | `<button type="submit">` or `<input type="submit">` | All forms |
| `<html:link page="...">` | `<a href="...">` | Navigation links |
| `<html:select property="...">` + `<html:option>` | `<select name="...">` + `<option>` | Product search, checkout |
| `<html:optionsCollection>` | `<c:forEach>` + `<option>` | Category dropdown |
| `<html:checkbox property="..."/>` | `<form:checkbox path="..."/>` or `<input type="checkbox">` | Address, admin forms |
| `<html:textarea property="..."/>` | `<textarea name="...">` | Product description edit |
| `<bean:write name="..." property="..." filter="true"/>` | `<c:out value="${obj.field}"/>` or `${obj.field}` | All display JSPs |
| `<bean:define id="..." name="..." property="..."/>` | `<c:set var="..." value="${...}"/>` | Temp variables |
| `<bean:message key="..."/>` | `<spring:message code="..."/>` or direct text | Labels, messages |
| `<logic:iterate id="..." name="..." property="...">` | `<c:forEach var="..." items="${...}">` | Product lists, order items, etc. |
| `<logic:present name="...">` | `<c:if test="${not empty ...}">` | Conditional display |
| `<logic:notPresent name="...">` | `<c:if test="${empty ...}">` | Fallback content |
| `<logic:empty name="...">` | `<c:if test="${empty ...}">` | Empty list handling |
| `<logic:notEmpty name="...">` | `<c:if test="${not empty ...}">` | List rendering |
| `<logic:equal name="..." property="..." value="...">` | `<c:if test="${obj.field == 'value'}">` | Status checks |
| `<tiles:insert attribute="..."/>` | `<jsp:include page="..."/>` | Layout slots |
| `<tiles:getAsString name="title"/>` | `${title}` via model or `<c:out>` | Page title |
| `<html:errors/>` | `<form:errors path="*"/>` or custom error display | Validation errors |

### Static Assets
Located at `src/main/webapp/assets/` — CSS, JS, images. Must remain at same URL paths.
