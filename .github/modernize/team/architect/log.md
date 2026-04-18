## [t2] Analyze source and design target Spring MVC architecture

**Task**: Deep analysis of Struts 1.3 SkiShop codebase + design Spring Boot 3.2 target architecture
**Duration**: 03:38Z → 03:56Z (~18 min)

### Key Discoveries
- **Struts 1.x** (not Struts 2): Actions extend `Action` (not `ActionSupport`), forms extend `ValidatorForm`, no OGNL — standard JSP EL
- **22 DB tables** with VARCHAR(36) UUID keys — no auto-increment anywhere
- **Password hashing**: SHA-256 with per-user UUID salt, 1000 iterations — MUST be replicated exactly or all seed users break
- **Only 2 explicit DB transactions** in entire codebase (InventoryService.reserveItems, PointService.expirePoints) — everything else relies on compensating actions
- **OrderFacade.placeOrder** is an 18-step orchestration with manual rollback (void payment, release inventory, refund points) — NOT a DB transaction
- **CSRF token**: Struts `TokenProcessor` sets per-session token; test script extracts `name="_csrfToken"` — Spring Security must use this exact parameter name
- **Test script expects no .do suffix** — URLs are clean (`/products`, `/login`, etc.) with curl following redirects
- **JSP taglib usage**: `html:form`, `bean:write`, `logic:iterate` are the dominant tags; `tiles:insert` for layout
- **Session management**: Only 2 session attributes (`loginUser`, `cartId`) + CART_ID cookie (30-day HttpOnly)
- **AuthRequestProcessor** enforces roles via `mapping.getRoles()` — maps directly to Spring Security's `hasRole()`
- **H2 PostgreSQL mode** needed for ILIKE compatibility in product search SQL

### Wrong Assumptions Corrected
- Initially assumed Struts 2 patterns — this is Struts 1.3, fundamentally different (no `ActionSupport`, no `struts.xml`, uses `struts-config.xml`)
- The guidelines skill is for Struts 2 → Spring Boot, but the actual source is Struts 1.3. Core migration patterns still apply but specifics differ (no OGNL, no `ActionSupport` base class)

### Patterns Worth Reusing
- Test script analysis is critical — it defines the exact URL contract, CSRF field name, and expected behavior
- Parallel sub-agent dispatch for multi-dimensional codebase analysis (4 agents, each covering a different layer) saved significant time

## [t17] Architecture review: verify implementation matches target architecture
- Parallel sub-agents for controller, service/DAO, config, and JSP layers — efficient for comprehensive review
- Boolean EL property gotcha: `isDefault()` getter maps to `${address.isDefault}` NOT `${address.default}` (reserved keyword)
- CsrfTokenConfig interceptor adds both `_csrf` (object) and `_csrfToken` (string) — both JSP patterns are valid
- Manual JDBC connections from `jdbcTemplate.getDataSource().getConnection()` bypass Spring TX — works but fragile
- Running api-test.sh is the fastest way to catch real bugs — found the addresses.jsp 500 error that code review alone might miss
- Admin controllers don't need explicit @Secured — SecurityConfig filter chain handles it at `/admin/**` level
