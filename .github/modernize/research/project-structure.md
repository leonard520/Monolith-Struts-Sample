# Project Structure Research

## Project Type
**Backend web application** — Java Servlet-based monolith with JSP views. No frontend SPA.

## Module/Package Breakdown

Single Maven module: `com.skishop:skishop-monolith:1.0.0` (WAR packaging)

### Top-level packages under `com.skishop`:

| Package | Layer | Description |
|---------|-------|-------------|
| `com.skishop.domain.*` | Model | 23 domain/entity classes across 9 sub-packages |
| `com.skishop.dao.*` | Repository | 20 DAO interfaces + 20 DAO implementations |
| `com.skishop.service.*` | Service | 16 service classes (incl. facades, configs, results) |
| `com.skishop.web.action.*` | Controller | 24 Struts Action classes (incl. 10 admin actions) |
| `com.skishop.web.form.*` | Controller | 12 Struts form beans (incl. 3 admin forms) |
| `com.skishop.web.processor` | Controller | 1 custom request processor (AuthRequestProcessor) |
| `com.skishop.web.filter` | Controller | 1 servlet filter (RequestIdFilter) |
| `com.skishop.web.tag` | Controller | 1 custom JSP tag (TokenTag) |
| `com.skishop.common.*` | Cross-cutting | Config, DAO base, ServiceLocator, DaoFactory, utilities |

### File Counts

- **Total Java types**: 134 (114 classes, 20 interfaces)
- **Source files**: 133
- **Test files**: 38
- **JSP views**: ~30 (across WEB-INF/jsp/)
- **Config files**: struts-config.xml, tiles-defs.xml, validation.xml, validator-rules.xml, web.xml, app.properties, log4j.properties, messages.properties

## Layer Architecture

```
Action (Controller) → Service → DAO (Repository) → Domain (Model)
```

- **Actions** (24): Extend `org.apache.struts.action.Action`, use `ServiceLocator` to obtain services
- **Services** (16): Business logic classes, directly instantiate DAO implementations (no DI framework)
- **DAOs** (40 = 20 interfaces + 20 impls): All extend `AbstractDao`, use `commons-dbutils` for JDBC, manual connection management via `DataSourceLocator`/`DataSourceFactory`
- **Domain** (23): Plain Java beans (POJOs), no ORM annotations
- **Forms** (12): Extend `ValidatorForm`, contain validation logic

## Dependency Injection Pattern

**Manual service locator / factory pattern** — no Spring or CDI:
- `ServiceLocator` — factory that instantiates all services
- `DaoFactory` — factory that instantiates all DAOs
- Actions call `ServiceLocator.getXxxService()` to obtain service instances
- Services directly instantiate DAOs via `new XxxDaoImpl()`

## Entry Points

- **web.xml**: Servlet 2.5 descriptor, maps `*.do` to Struts `ActionServlet`
- **struts-config.xml**: 28 action mappings, 12 form-bean declarations
- **tiles-defs.xml**: 24 Tiles layout definitions (baseLayout + 23 page definitions)
- **validation.xml**: Struts Validator rules for form validation
- **AuthRequestProcessor**: Custom `TilesRequestProcessor` handling role-based auth + CSRF token validation

## Functional Domains

1. **Authentication** — Login, logout, session management (LoginAction, LogoutAction, AuthRequestProcessor)
2. **User Registration** — Account creation with validation (RegisterAction)
3. **Password Management** — Forgot password, reset with token (PasswordForgotAction, PasswordResetAction)
4. **Product Catalog** — Product listing, search, filtering, detail view (ProductListAction, ProductDetailAction)
5. **Category Management** — Product categories (CategoryService, CategoryDao)
6. **Shopping Cart** — Session-based cart with add/remove/update (CartAction)
7. **Coupons** — View available coupons, apply to cart (CouponAvailableAction, CouponApplyAction)
8. **Checkout & Orders** — Order placement with payment processing (CheckoutAction, OrderFacade)
9. **Order Management** — Order history, detail, cancel, return (OrderHistoryAction, OrderDetailAction, OrderCancelAction, OrderReturnAction)
10. **Points/Loyalty** — Point balance, earn/redeem (PointBalanceAction, PointService)
11. **Address Management** — User address CRUD (AddressListAction, AddressSaveAction)
12. **Email/Notifications** — Email queue processing, order confirmations (MailService, MailConfig)
13. **Payment Processing** — Credit card payment handling (PaymentService)
14. **Inventory** — Stock management (InventoryService)
15. **Shipping** — Shipping methods and rates (ShippingService)
16. **Tax** — Tax calculation (TaxService)
17. **Admin: Product Management** — CRUD products (AdminProductListAction, AdminProductEditAction, AdminProductDeleteAction)
18. **Admin: Order Management** — View/update/refund orders (AdminOrderListAction, AdminOrderDetailAction, AdminOrderUpdateAction, AdminOrderRefundAction)
19. **Admin: Coupon Management** — CRUD coupons (AdminCouponListAction, AdminCouponEditAction)
20. **Admin: Shipping Management** — CRUD shipping methods (AdminShippingMethodListAction, AdminShippingMethodEditAction)

## View Layer

- **Template engine**: JSP + JSTL + Struts tag libraries (struts-html, struts-bean, struts-logic, struts-tiles)
- **Layout**: Apache Tiles 1.x with `baseLayout` (header, messages, body, footer)
- **JSP directories**: auth/, admin/, cart/, coupons/, orders/, points/, products/, account/, common/, layouts/
- **Static assets**: `/assets/` directory
- **Error handling**: Global exception handler → `/error.jsp`

## URL Pattern

All Struts actions use `*.do` suffix pattern (e.g., `/login.do`, `/products.do`, `/admin/products.do`).
The `api-test.sh` test script uses **clean URLs without `.do`** suffix (e.g., `/login`, `/products`, `/admin/products`), indicating the Spring MVC rewrite must use clean URL patterns.
