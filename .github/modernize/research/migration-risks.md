# Migration Risks Research

## Risk Assessment by Module

### CRITICAL Risk

| Module | Risk Factor | Detail |
|--------|------------|--------|
| **AuthRequestProcessor** | Tightly coupled to Struts TilesRequestProcessor | Extends `TilesRequestProcessor`, implements role-checking via action mapping `roles` attribute, CSRF token validation via `TokenProcessor`. Must be fully rewritten as Spring Security filter chain or `HandlerInterceptor`. |
| **OrderFacadeImpl** | Complex transaction orchestration | Coordinates cart, inventory, order, payment, points, shipping, tax, coupon, mail, and user services in a single flow. High fan-out (14+ imports). Transaction boundaries must be preserved exactly. |

### HIGH Risk

| Module | Risk Factor | Detail |
|--------|------------|--------|
| **Form Beans (12)** | Struts ValidatorForm coupling | All extend `org.apache.struts.validator.ValidatorForm`. Custom `validate()` methods use `ActionErrors`/`ActionMessages`. Must be converted to Spring form-backing objects with Bean Validation or programmatic validation. |
| **validation.xml** | Struts Validator rules | XML-based field validation rules — must be replicated in Java (Bean Validation annotations or Spring `Validator` interface). |
| **Session-scoped CartAction** | `scope="session"` form bean | `addCartForm` is session-scoped in Struts config. Spring MVC handles session attributes differently (`@SessionAttributes`). |
| **ServiceLocator/DaoFactory** | Manual DI replacement | All Actions and Services obtain dependencies via static factory pattern. Must be replaced with Spring `@Autowired` injection across entire codebase. |
| **Tiles Layout** | Struts Tiles 1.x to Spring MVC Tiles or JSP includes | 24 tile definitions using Struts Tiles plugin. Must replicate page composition — either via Apache Tiles 3 with Spring MVC integration, or converted to JSP includes. |

### MEDIUM Risk

| Module | Risk Factor | Detail |
|--------|------------|--------|
| **MailService/MailConfig** | javax.mail → Jakarta Mail or Spring JavaMailSender | Direct `javax.mail` API usage with SMTP configuration and email queue processing. Must be adapted to Spring's `JavaMailSender`. |
| **DataSourceLocator** | JNDI lookup → Spring Boot DataSource | Uses `javax.naming.InitialContext` for JNDI DataSource lookup. Must be replaced with Spring Boot auto-configured DataSource. |
| **AbstractDao** | Base DAO with manual connection management | All 20 DAO implementations extend this. Must replace with Spring `JdbcTemplate`-based approach while preserving all SQL queries. |
| **TokenTag (custom JSP tag)** | Framework-specific | Custom JSP tag for CSRF tokens — must be replaced with Spring's CSRF support or equivalent custom tag. |
| **URL Mapping** | `*.do` to clean URLs | Struts uses `*.do` pattern; test script expects clean URLs (`/products`, `/login`). Spring MVC `@RequestMapping` naturally supports clean URLs. |

### LOW Risk

| Module | Risk Factor | Detail |
|--------|------------|--------|
| **Domain objects (23)** | Plain POJOs | No framework coupling; can be reused with minimal/no changes. |
| **Business logic in services** | Minimal framework coupling | Services use manual DAO instantiation but business logic is framework-independent. |
| **JSP views** | Struts taglib → JSTL/Spring tags | Need to replace `<html:form>`, `<bean:write>`, `<logic:iterate>` with JSTL equivalents or Spring tags. |
| **Static assets** | No migration needed | CSS/JS/images in `/assets/` — no changes required. |
| **SQL queries** | Preserved as-is | All queries are in DAO implementations using raw JDBC — can be ported to JdbcTemplate with same SQL. |

## Framework-Coupled Patterns

1. **Action class pattern**: 24 Actions extending `org.apache.struts.action.Action` with `execute(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)` signature
2. **Form bean pattern**: 12 forms extending `ValidatorForm` with `validate()` and `reset()` methods
3. **ActionForward navigation**: Forward/redirect decisions via `ActionForward` — replaced by Spring MVC view names and `redirect:` prefix
4. **ActionMessages for user feedback**: Struts message queue in request scope — replaced by Spring `RedirectAttributes.addFlashAttribute()` or model attributes
5. **Tiles integration**: `TilesRequestProcessor`, `TilesPlugin`, tile definition names used as forward paths
6. **Struts Globals**: `Globals.MESSAGES_KEY`, `Constants.TOKEN_KEY` — internal Struts state management

## Session and Auth Patterns

- **Session state**: User object stored in `HttpSession` after login (session attribute key: likely `"user"`)
- **Role-based access**: `AuthRequestProcessor.processRoles()` checks `roles` attribute from action-mapping config against `User.getRole()`
- **CSRF protection**: `AuthRequestProcessor` validates Struts `TokenProcessor` tokens; custom `TokenTag` generates tokens in forms
- **Login redirect**: Unauthenticated users accessing protected pages are redirected to `/login.do`
- **Admin isolation**: Admin actions require `roles="ADMIN"` — separate from USER-level access

## Test Coverage Gaps in High-Complexity Areas

- **OrderFacadeImpl**: Only 1 test file (`OrderFacadeTest.java`) for the most complex service — transaction orchestration may be under-tested
- **AuthRequestProcessor**: 1 test file — security-critical component
- **No integration tests**: All tests are unit tests with mocked dependencies (StrutsTestCase + H2)
- **No service-level tests** for: MailService, PaymentService, ShippingService, TaxService, InventoryService, CouponService
