## Inbox — frontend

### [teamlead] t1 — 2026-04-18T03:36Z (broadcast)
Constitution v1.0.0 ratified for SkiShop Monolith migration (Struts 1.3 → Spring Boot 3.2.x). Key rules:
- **Mode**: REWRITE into `monolith-new/` directory (do NOT modify `src/`)
- **JSPs kept**: migrate Struts tags → JSTL + Spring form tags, replace Tiles with JSP includes
- **Exact URL parity**: `api-test.sh` defines the URL contract
- **CSRF field**: `_csrfToken` (not Spring's default `_csrf`)
- **No JPA**: use `JdbcTemplate`
- **Final artifact**: `skishop-app-2.0.0.jar`, runnable with `--spring.profiles.active=h2`
Full constitution: `.github/modernize/artifacts/t1-teamlead-constitution.md`

### [architect] t17 — 2026-04-18T06:43Z
BUG in `addresses.jsp` lines 49-50: `${address.default}` must be changed to `${address.isDefault}`. `default` is a Java reserved keyword — EL cannot resolve it. This causes HTTP 500 on `GET /account/addresses` for all users. Fix is a 2-line change.

### [security] t18 — 2026-04-18T06:27Z
F-01 (MEDIUM): Six admin JSPs use `${_csrfToken}` instead of `${_csrf.token}` for the CSRF hidden field. Standardize to `${_csrf.token}` to avoid potential 403 errors when interceptor `postHandle()` doesn't fire (e.g., on redirects). Affected files: `admin/orders/list.jsp`, `admin/orders/detail.jsp`, `admin/coupons/edit.jsp`, `admin/shipping/edit.jsp`, `admin/products/list.jsp`, `admin/products/edit.jsp`.

F-02 (LOW): Unescaped `${product.id}` and `${order.id}` in href attributes in `home.jsp` and `orders/history.jsp`. Low risk since these are server-generated UUIDs, but `<c:out>` wrapping would be defense-in-depth.
