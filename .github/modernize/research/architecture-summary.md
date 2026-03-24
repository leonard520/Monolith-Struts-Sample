# Architecture Summary Research

_Source: knowledge-graph.json (134 types, 677 dependency edges)_

## Architectural Pattern

**Layered monolith** with manual dependency injection (Service Locator / Factory pattern):

```
[Web Layer]         → [Service Layer]      → [DAO Layer]          → [Domain Layer]
Actions (24)          Services (16)           DAOs (20+20)           POJOs (23)
Forms (12)            Facades (2)             AbstractDao base
Processor (1)         ServiceLocator          DaoFactory
Filter (1)            
Tag (1)
```

## Key Architectural Observations

### 1. Service Locator as DI Container
`ServiceLocator` is the central dependency hub, importing 15 services and 1 factory. All Struts Actions obtain services via `ServiceLocator.getXxxService()`. This is the primary refactoring target — must become Spring `@Autowired` injection.

### 2. High Fan-Out Classes
- **OrderFacadeImpl** (HIGHEST coupling): imports from 14+ different services and DAOs — coordinates cart, inventory, order, payment, points, shipping, tax, coupon, mail, and user services
- **ServiceLocator**: imports all 15 service classes — hub pattern, high fan-out
- **DaoFactory**: imports all DAO interfaces and implementations

### 3. DAO Inheritance Hierarchy
All DAO implementations extend `AbstractDao`:
```
AbstractDao (base class with connection management)
├── OrderDaoImpl
├── UserDaoImpl  
├── ProductDaoImpl
├── CartDaoImpl
├── PaymentDaoImpl
├── CouponDaoImpl
├── CouponUsageDaoImpl
├── CategoryDaoImpl
├── InventoryDaoImpl
├── UserAddressDaoImpl
├── PointAccountDaoImpl
├── PointTransactionDaoImpl
├── SecurityLogDaoImpl
├── PasswordResetTokenDaoImpl
├── EmailQueueDaoImpl
├── ShippingMethodDaoImpl
├── ReturnDaoImpl
├── PriceDaoImpl
└── OrderShippingDaoImpl
```

### 4. Action→Service Dependencies
| Action | Services Used |
|--------|--------------|
| LoginAction | AuthService |
| RegisterAction | UserService, AuthService |
| CartAction | CartService, ProductService |
| CheckoutAction | OrderFacade, CartService |
| CouponApplyAction | CouponService, CartService |
| OrderHistoryAction | OrderService |
| OrderCancelAction | OrderFacade |
| OrderReturnAction | OrderFacade |
| PasswordForgotAction | UserService, MailService |
| PasswordResetAction | UserService |
| ProductListAction | ProductService, CategoryService |
| ProductDetailAction | ProductService |
| PointBalanceAction | PointService |
| AddressListAction | UserAddressDao (direct DAO access — layering violation) |
| AdminProductEditAction | ProductService, CategoryService |
| AdminOrderUpdateAction | OrderFacade |
| AdminOrderRefundAction | OrderFacade |

### 5. Cross-Cutting Concerns
- **Configuration**: `AppConfig` used by `DataSourceFactory`, `MailService`, `MailConfig`
- **Security**: `AuthRequestProcessor` — central auth enforcement via Struts request processing pipeline
- **Logging**: log4j 1.x with `commons-logging` facade in some classes
- **Error handling**: Global exception handler in struts-config.xml → `/error.jsp`

### 6. Tightly Coupled Areas
- **AuthRequestProcessor ↔ Struts internals**: Extends `TilesRequestProcessor`, uses `ActionMapping`, `ActionForm`, `ActionForward`, `Globals`, `TokenProcessor`, `Constants`
- **Form beans ↔ Struts Validator**: All 12 forms extend `ValidatorForm`, override `validate(ActionMapping, HttpServletRequest)`
- **AddressListAction ↔ DAO layer**: Directly accesses `UserAddressDaoImpl` bypassing service layer — architectural inconsistency

### 7. Module Dependencies
Single module — no inter-module dependencies. All 134 types reside in the same Maven module (`skishop-monolith`).
