# t18 — Security Audit Findings

## Audit Scope

| Area | Status | Notes |
|------|--------|-------|
| Authentication flow | **PASS** | Manual AuthService.authenticate() + Spring Security context setup |
| Authorization (default-deny) | **PASS** | SecurityConfig enforces role-based access; unauthenticated → /login redirect |
| CSRF (`_csrfToken`) | **PASS** | HttpSessionCsrfTokenRepository with `_csrfToken` parameter name |
| Session handling | **PASS** | Session fixation prevention, invalidation on logout |
| Password hashing | **PASS** | SHA-256 + salt + 1000 iterations (matches source) |
| Account lockout | **PASS** | 5 failed attempts → LOCKED, logged to security_logs |
| SQL injection | **PASS** | All DAOs use parameterized JdbcTemplate queries |
| XSS | **PASS** | JSPs use `<c:out>` for user-controlled output |
| Cookie security | **PASS** | CART_ID cookie: HttpOnly + Secure (conditional on HTTPS) |
| Dependency CVEs | **PASS** | Spring Boot 3.2.5 BOM manages all dependency versions |

---

## Findings

### F-01: Inconsistent CSRF token EL expression in admin JSPs [MEDIUM]

**Severity**: MEDIUM  
**Category**: CSRF  
**Assign to**: frontend

**Evidence**: Six admin JSP forms use `${_csrfToken}` (a model attribute injected by `CsrfTokenConfig` interceptor), while all other JSPs use `${_csrf.token}` (Spring Security's standard request attribute).

Files using `${_csrfToken}` (via interceptor model attribute):
- [admin/orders/list.jsp](monolith-new/src/main/webapp/WEB-INF/jsp/admin/orders/list.jsp#L44)
- [admin/orders/detail.jsp](monolith-new/src/main/webapp/WEB-INF/jsp/admin/orders/detail.jsp#L25)
- [admin/coupons/edit.jsp](monolith-new/src/main/webapp/WEB-INF/jsp/admin/coupons/edit.jsp#L61)
- [admin/shipping/edit.jsp](monolith-new/src/main/webapp/WEB-INF/jsp/admin/shipping/edit.jsp#L41)
- [admin/products/list.jsp](monolith-new/src/main/webapp/WEB-INF/jsp/admin/products/list.jsp#L43)
- [admin/products/edit.jsp](monolith-new/src/main/webapp/WEB-INF/jsp/admin/products/edit.jsp#L53)

Files using `${_csrf.token}` (standard):
- All auth, cart, checkout, orders, account, products JSPs

**Risk**: Both expressions resolve to the same value at runtime because `CsrfTokenConfig` interceptor exposes both `_csrf` and `_csrfToken` model attributes. However, `${_csrfToken}` relies on the interceptor's `postHandle()` being called — if a controller returns `null` ModelAndView (e.g., on redirect), the interceptor may not fire, producing an empty token and a 403 on the next POST.

**Recommendation**: Standardize all JSPs to use `${_csrf.token}` which is populated by Spring Security's filter chain regardless of interceptor execution.

---

### F-02: Unescaped `${product.id}` and `${order.id}` in URL attribute contexts [LOW]

**Severity**: LOW  
**Category**: XSS  
**Assign to**: frontend

**Evidence**: Several JSPs embed UUIDs directly in `href` attributes without `<c:out>` escaping:

- [home.jsp](monolith-new/src/main/webapp/WEB-INF/jsp/home.jsp#L31): `href="<c:url value='/product'/>?id=${product.id}"`
- [home.jsp](monolith-new/src/main/webapp/WEB-INF/jsp/home.jsp#L40): `href="<c:url value='/product'/>?id=${product.id}"`
- [orders/history.jsp](monolith-new/src/main/webapp/WEB-INF/jsp/orders/history.jsp#L35): `href="<c:url value='/orders/detail?id=${order.id}'/>"`
- [orders/history.jsp](monolith-new/src/main/webapp/WEB-INF/jsp/orders/history.jsp#L36): `action="<c:url value='/orders/${order.id}/cancel'/>"`
- [orders/history.jsp](monolith-new/src/main/webapp/WEB-INF/jsp/orders/history.jsp#L40): `action="<c:url value='/orders/${order.id}/return'/>"`

**Risk**: LOW — these are server-generated UUID strings from the database, not user-supplied input. An attacker would need to control database ID values to exploit this. In practice, UUID format prevents XSS payload injection.

**Recommendation**: For defense-in-depth, wrap IDs with `<c:out>` or use `<c:url><c:param>` for query parameters. This is a stylistic improvement, not a functional security issue.

---

### F-03: `POST /cart` is CSRF-protected but `/cart` is `permitAll()` [LOW]

**Severity**: LOW  
**Category**: CSRF/Authorization  
**Assign to**: backend

**Evidence**: In [SecurityConfig.java](monolith-new/src/main/java/com/skishop/config/SecurityConfig.java#L33), `/cart` is listed under `permitAll()`. Spring Security's authorization rules apply to both GET and POST. However, CSRF is still enforced on POST since it is enabled globally. This means:
- Unauthenticated users CAN add items to cart (which is correct — same as source behavior)
- CSRF token IS required for POST /cart (correct)

**Risk**: LOW — this is functionally correct. Unauthenticated cart access is by design (source parity). The CSRF protection is not bypassed. However, the `permitAll()` matcher is path-based, not method-based, which could be confusing for future maintainers.

**Recommendation**: No action needed. Document that `/cart` is intentionally accessible to anonymous users (cart is session-based, not user-based until checkout).

---

### F-04: Custom password hashing uses SHA-256 instead of bcrypt/scrypt/argon2 [LOW]

**Severity**: LOW  
**Category**: Cryptography  
**Assign to**: N/A (constitutional constraint — source parity)

**Evidence**: [PasswordHasher.java](monolith-new/src/main/java/com/skishop/common/util/PasswordHasher.java#L1) uses SHA-256 with salt and 1000 iterations. This is a faithful port of the source application's hashing.

**Risk**: SHA-256 with 1000 iterations is weaker than modern password hashing (bcrypt work factor 12 = ~4096 iterations of a much more expensive function). However, the constitution mandates source parity — existing password hashes in the database must remain valid.

**Recommendation**: This is by design per the constitution (Principle V: "replicate the source's hash+salt algorithm exactly so existing user records remain valid"). No action for this migration. Note for future: consider progressive rehashing (re-hash with bcrypt on next successful login).

---

### F-05: `GET /logout` instead of `POST /logout` [INFORMATIONAL]

**Severity**: INFORMATIONAL  
**Category**: Session Management  
**Assign to**: N/A (source parity)

**Evidence**: [AuthController.java](monolith-new/src/main/java/com/skishop/web/controller/AuthController.java#L107) handles logout via `@GetMapping("/logout")`. OWASP recommends POST for state-changing operations to prevent CSRF-based logout attacks.

**Risk**: An attacker could force logout by embedding `<img src="/logout">` on any page. However, logout is a low-impact action and the source application also uses GET for logout.

**Recommendation**: Source parity requirement. The `api-test.sh` test script expects GET logout. No action for this migration.

---

## Verified Security Controls (No Findings)

### Authentication Flow ✅
- `AuthService.authenticate()` properly validates email/password, checks locked status, records failures
- Login creates Spring Security `UsernamePasswordAuthenticationToken` with `ROLE_USER`/`ROLE_ADMIN`
- Session fixation prevention: old session invalidated, new session created, CSRF token preserved across sessions
- Logout invalidates session and clears SecurityContextHolder

### Default-Deny Authorization ✅
- SecurityConfig uses `.anyRequest().authenticated()` as the final rule — default deny
- Public endpoints explicitly whitelisted: `/`, `/home`, `/products`, `/product`, `/login`, `/register`, `/password/*`, `/cart`, `/coupons`, `/checkout`, `/assets/**`, `/error`
- USER+ADMIN endpoints: `/orders/**`, `/points`, `/account/**`, `/logout`, `/coupons/apply`
- ADMIN-only: `/admin/**`
- `DispatcherType.FORWARD` and `DispatcherType.ERROR` are permitted (required for JSP includes and error pages)

### CSRF Protection ✅
- `HttpSessionCsrfTokenRepository` with `parameterName="_csrfToken"` — matches test script's `extract_csrf` function
- `CsrfTokenRequestAttributeHandler` correctly configured for Spring Security 6.x deferred token behavior
- All POST forms include `<input type="hidden" name="_csrfToken" value="..."/>` (20+ forms verified)

### SQL Injection Protection ✅
- All DAO methods use `JdbcTemplate.query()` and `JdbcTemplate.update()` with `?` parameterized placeholders
- Dynamic query builder in `ProductDaoImpl.findPaged()` uses `StringBuilder` + `params.add()` — no string concatenation of user input into SQL

### XSS Protection ✅
- All user-visible data rendered via `<c:out value="${...}"/>` which HTML-escapes by default
- Form input values use `<c:out>` inside `value=""` attributes
- Error messages rendered via `<c:out>` in `messages.jsp`
- `error.jsp` uses `<c:out value="${errorMessage}"/>` for exception messages

### Cookie Security ✅
- CART_ID cookie: HttpOnly flag set, Secure flag conditional on HTTPS, path scoped
- Session cookie: managed by Spring Boot defaults (HttpOnly by default)

### Account Lockout ✅
- 5 failed login attempts → user status set to `LOCKED`
- Each failure logged to `security_logs` table with IP, user agent, JSON details
- Locked users cannot authenticate (checked before password validation)

### Dependency Security ✅
- Spring Boot 3.2.5 parent BOM manages all transitive dependency versions
- No direct third-party libraries outside the Spring ecosystem
- H2 and PostgreSQL drivers are runtime-only dependencies
- No known high-severity CVEs in Spring Boot 3.2.5 dependency tree at time of release
