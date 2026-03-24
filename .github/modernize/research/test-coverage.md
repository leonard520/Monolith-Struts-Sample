# Test Coverage Research

## Test Inventory

- **Total test files**: 38
- **Test framework**: JUnit 4.12 + StrutsTestCase 2.1.4

### By Category

| Category | Count | Framework | Base Class |
|----------|-------|-----------|------------|
| DAO unit tests | 14 | JUnit 4 + H2 | `DaoTestBase` |
| Action tests (user) | 18 | JUnit 4 + StrutsTestCase | `StrutsActionTestBase` |
| Action tests (admin) | 4 | JUnit 4 + StrutsTestCase | `StrutsActionTestBase` |
| Service tests | 2 | JUnit 4 | — |
| Integration (e2e) | 1 | Bash/curl (`api-test.sh`) | — |

### DAO Tests (14 files)
| Test File | DAO Under Test |
|-----------|---------------|
| CartDaoTest | CartDaoImpl |
| CouponDaoTest | CouponDaoImpl |
| EmailQueueDaoTest | EmailQueueDaoImpl |
| InventoryDaoTest | InventoryDaoImpl |
| OrderDaoTest | OrderDaoImpl |
| PasswordResetTokenDaoTest | PasswordResetTokenDaoImpl |
| PointAccountDaoTest | PointAccountDaoImpl |
| PointTransactionDaoTest | PointTransactionDaoImpl |
| ProductDaoTest | ProductDaoImpl |
| ShippingMethodDaoTest | ShippingMethodDaoImpl |
| UserAddressDaoTest | UserAddressDaoImpl |
| UserDaoTest | UserDaoImpl |
| DaoTestBase | (base class — H2 setup) |

Missing DAO tests: CouponUsageDaoImpl, CategoryDaoImpl, PriceDaoImpl, PaymentDaoImpl, ReturnDaoImpl, OrderShippingDaoImpl, SecurityLogDaoImpl

### Action Tests (22 files)
| Test File | Action Under Test |
|-----------|------------------|
| LoginActionTest | LoginAction |
| LogoutActionTest | LogoutAction |
| RegisterActionTest | RegisterAction |
| PasswordForgotActionTest | PasswordForgotAction |
| PasswordResetActionTest | PasswordResetAction |
| ProductListActionTest | ProductListAction |
| ProductDetailActionTest | ProductDetailAction |
| CartActionTest | CartAction |
| CheckoutActionTest | CheckoutAction |
| CouponApplyActionTest | CouponApplyAction |
| OrderHistoryActionTest | OrderHistoryAction |
| OrderCancelActionTest | OrderCancelAction |
| OrderReturnActionTest | OrderReturnAction |
| PointBalanceActionTest | PointBalanceAction |
| AddressSaveActionTest | AddressSaveAction |
| AddressListActionTest | AddressListAction |
| AuthRequestProcessorTest | AuthRequestProcessor |
| AdminProductEditActionTest | AdminProductEditAction |
| AdminCouponEditActionTest | AdminCouponEditAction |
| AdminOrderUpdateActionTest | AdminOrderUpdateAction |
| AdminOrderRefundActionTest | AdminOrderRefundAction |
| AdminShippingMethodEditActionTest | AdminShippingMethodEditAction |
| StrutsActionTestBase | (base class) |

Missing Action tests: CouponAvailableAction, AdminProductListAction, AdminProductDeleteAction, AdminOrderListAction, AdminOrderDetailAction, AdminCouponListAction, AdminShippingMethodListAction

### Service Tests (2 files)
| Test File | Service Under Test |
|-----------|-------------------|
| OrderFacadeTest | OrderFacadeImpl |
| ScenarioFlowTest | End-to-end business flow |

Missing service tests: AuthService, MailService, PaymentService, ProductService, CategoryService, CartService, CouponService, InventoryService, UserService, ShippingService, TaxService, PointService

## Portability Assessment

### Non-Portable (Struts-coupled) — 23 files
- All 22 Action tests + `StrutsActionTestBase` depend on `StrutsTestCase` which extends `MockStrutsTestCase` — tightly coupled to Struts servlet mock. **These cannot be migrated; they must be rewritten as Spring MVC tests using `MockMvc`.**

### Portable (Framework-independent) — 15 files
- All 13 DAO tests + `DaoTestBase` — use H2 in-memory DB + JUnit 4. Can be migrated to JUnit 5 with Spring's `@JdbcTest` or kept as-is with JUnit 4 compatibility.
- `OrderFacadeTest` and `ScenarioFlowTest` — likely test business logic only, should be portable to Spring test context.

## Test Data Strategy

- **DAO tests**: H2 in-memory database initialized via `DaoTestBase` (likely loads `schema.sql`)
- **Action tests**: `StrutsTestCase` mock framework (in-process servlet container)
- **Integration tests**: `api-test.sh` requires seed data from `data.sql` (product catalog, seed users, etc.)

## Test Coverage Gaps

1. **Service layer**: Only 2 of ~14 services have test files — major gap
2. **Missing DAO tests**: 7 DAO implementations untested
3. **Missing Action tests**: 7 list/view-only actions untested
4. **No Mockito usage**: Tests rely on either real H2 DB or StrutsTestCase mocks
5. **No performance/load tests**
6. **Integration test**: Only the bash-based `api-test.sh` script — no Java-based integration tests
