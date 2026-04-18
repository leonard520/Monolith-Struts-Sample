# t17 — Architecture Review Findings

## Findings Summary

| # | Severity | Layer | Finding | File | Fix |
|---|----------|-------|---------|------|-----|
| F1 | **BUG** | View | `addresses.jsp` uses `${address.default}` — `default` is reserved keyword | `WEB-INF/jsp/account/addresses.jsp:49-50` | Change to `${address.isDefault}` |
| F2 | **ADVISORY** | Service | Manual JDBC transaction management bypasses Spring TX manager | `InventoryService`, `PointService`, `ProductService` | Per decisions.md: intentional (behavioral fidelity). Future risk if connection pool behavior differs. |
| F3 | **ADVISORY** | Controller | AddressController `/account/addresses/edit?id=` ignores the `id` parameter — always loads empty form | `AddressController.java` | Should populate form with existing address when `id` param is provided |
| F4 | **INFO** | Config | WebMvcConfig references old Struts TLD path for document root workaround | `WebMvcConfig.java:42` | Correct behavior — prevents Jasper scanning old project's TLDs |
| F5 | **INFO** | View | Two CSRF token patterns used (`${_csrf.token}` and `${_csrfToken}`) | Various JSPs | Both valid — CsrfTokenConfig interceptor exposes both. Cosmetic inconsistency only. |
| F6 | **INFO** | View | No `index.jsp` in webapp root | — | Not needed — HomeController maps `/` directly |

---

## F1: addresses.jsp `${address.default}` — BUG

**Severity:** BUG — causes HTTP 500 on production path
**Test Impact:** Fails `GET /account/addresses - List (new user)` (test #42/65)

**Root Cause:**
```jsp
<!-- addresses.jsp lines 49-50 -->
<c:if test="${address.default}">はい</c:if>
<c:if test="${not address.default}">-</c:if>
```

The Address domain class has:
```java
private boolean isDefault;
public boolean isDefault() { return isDefault; }
```

EL resolves `${address.default}` by looking for `getDefault()` — which doesn't exist. The correct EL property is `isDefault` (mapping to `isDefault()` for boolean getters).

**Fix:**
```jsp
<c:if test="${address.isDefault}">はい</c:if>
<c:if test="${not address.isDefault}">-</c:if>
```

**Assigned to:** frontend role (JSP owner)

---

## F2: Manual JDBC Transaction Management — ADVISORY

**Severity:** ADVISORY (not a bug per decisions.md)
**Affected Services:**
- `InventoryService.reserveItems()` / `releaseItems()` — `FOR UPDATE` with manual commit
- `PointService.expirePoints()` — manual commit
- `ProductService.deactivateProduct()` — manual commit/rollback

**Pattern:**
```java
Connection con = jdbcTemplate.getDataSource().getConnection();
con.setAutoCommit(false);
// ... operations ...
con.commit();
```

**Risk:** The connection obtained via `getDataSource().getConnection()` is NOT the same connection Spring's DataSourceTransactionManager would use. If the connection pool returns a different connection mid-operation, transaction isolation is broken. In practice with HikariCP defaults this works, but it's fragile.

**Decision Reference:** decisions.md — "OrderFacade.placeOrder uses compensating actions (no @Transactional). Source pattern uses manual compensation."

**Recommendation for future:** Replace manual JDBC with `@Transactional` + `JdbcTemplate` operations when behavioral fidelity constraint is relaxed.

---

## F3: AddressController Edit Ignores ID — ADVISORY

**Severity:** ADVISORY — cosmetic regression, edit form always starts empty
**Location:** `AddressController.java` — GET `/account/addresses/edit?id=`

The `id` request parameter is accepted but not used to pre-populate the form with existing address data. Users must re-enter all fields even when editing an existing address.

**Impact:** UX regression vs source. Not caught by test script (test only checks HTTP 200).

**Assigned to:** backend role (controller logic)
