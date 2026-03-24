## Phase 6: Test Migration

> **Plan ref**: Phase 6 (Batch 6) — Migrate all unit tests to JUnit 5 + Spring Test
> **Requirements**: REQ-039, REQ-040, REQ-041, NFR-001
> **Exit Criteria**: `mvn -B test` passes with zero failures. All 38 test files migrated.

---

### 6.1 Test Infrastructure

- [ ] T600 [Plan:6.1] Create `src/test/resources/application-test.properties` with H2 in-memory datasource (`jdbc:h2:mem:skishop;DB_CLOSE_DELAY=-1`), `spring.sql.init.schema-locations=classpath:db/schema.sql`, `spring.sql.init.data-locations=classpath:db/data.sql`, and `spring.sql.init.mode=always`
- [ ] T601 [Plan:6.1] Rewrite `src/test/java/com/skishop/dao/DaoTestBase.java` as a Spring Test base class annotated with `@JdbcTest`, `@ActiveProfiles("test")`, `@AutoConfigureTestDatabase(replace=NONE)`. Inject `JdbcTemplate` and `DataSource`. Replace manual H2 setup and `DataSourceLocator` with Spring-managed datasource. Provide `resetDatabase()` using `@Sql` or `JdbcTemplate` execution of schema.sql + data.sql
- [ ] T602 [Plan:6.1] Remove old `DaoTestBase` dependency on `DataSourceLocator.getInstance().setDataSource()` — all test datasource wiring must go through Spring context
- [ ] T603 [Plan:6.1] Verify `src/test/resources/db/schema.sql` and `src/test/resources/db/data.sql` exist or are classpath-accessible for H2 initialization; adjust H2-incompatible DDL if needed (e.g., PostgreSQL-specific syntax)

### 6.2 DAO Test Migration (12 tests)

- [ ] T604 [P] [Plan:6.2] Migrate `CartDaoTest` to JUnit 5 + `@JdbcTest`: replace `extends DaoTestBase` / `TestCase` with `@ExtendWith(SpringExtension.class)`, JUnit 5 `@Test`, inject DAO via `@Autowired` or manual instantiation with injected `DataSource`. Preserve all SQL assertions
- [ ] T605 [P] [Plan:6.2] Migrate `CouponDaoTest` to JUnit 5 + `@JdbcTest`: same pattern — JUnit 5 annotations, Spring-managed datasource, preserve assertion logic
- [ ] T606 [P] [Plan:6.2] Migrate `EmailQueueDaoTest` to JUnit 5 + `@JdbcTest`
- [ ] T607 [P] [Plan:6.2] Migrate `InventoryDaoTest` to JUnit 5 + `@JdbcTest`
- [ ] T608 [P] [Plan:6.2] Migrate `OrderDaoTest` to JUnit 5 + `@JdbcTest`
- [ ] T609 [P] [Plan:6.2] Migrate `PasswordResetTokenDaoTest` to JUnit 5 + `@JdbcTest`
- [ ] T610 [P] [Plan:6.2] Migrate `PointAccountDaoTest` to JUnit 5 + `@JdbcTest`
- [ ] T611 [P] [Plan:6.2] Migrate `PointTransactionDaoTest` to JUnit 5 + `@JdbcTest`
- [ ] T612 [P] [Plan:6.2] Migrate `ProductDaoTest` to JUnit 5 + `@JdbcTest`
- [ ] T613 [P] [Plan:6.2] Migrate `ShippingMethodDaoTest` to JUnit 5 + `@JdbcTest`
- [ ] T614 [P] [Plan:6.2] Migrate `UserAddressDaoTest` to JUnit 5 + `@JdbcTest`
- [ ] T615 [P] [Plan:6.2] Migrate `UserDaoTest` to JUnit 5 + `@JdbcTest`

### 6.3 Controller Test Rewrite (22 tests)

- [ ] T616 [Plan:6.3] Create `src/test/java/com/skishop/web/controller/ControllerTestBase.java` — base class or shared configuration for `@WebMvcTest` tests. Configure common `MockMvc` setup, shared mock beans (e.g., session-scoped user), and `@MockBean` declarations for cross-cutting services. Replace `StrutsActionTestBase` / `MockStrutsTestCase` pattern entirely
- [ ] T617 [P] [Plan:6.3] Rewrite `ProductListActionTest` → `ProductControllerTest` (product list): `@WebMvcTest(ProductController.class)`, `@MockBean` ProductService, `mockMvc.perform(get("/products").param("keyword","Ski"))`, assert status 200 + view name + model attribute `productList`
- [ ] T618 [P] [Plan:6.3] Rewrite `ProductDetailActionTest` → `ProductControllerTest` (product detail): test `GET /products/{id}`, assert model attribute `product` and view name
- [ ] T619 [P] [Plan:6.3] Rewrite `LoginActionTest` → `AuthControllerTest` (login): `@WebMvcTest(AuthController.class)`, `@MockBean` AuthService, test `POST /login` with form params, assert redirect on success, assert error view on failure
- [ ] T620 [P] [Plan:6.3] Rewrite `LogoutActionTest` → `AuthControllerTest` (logout): test `GET /logout`, assert session invalidated and redirect to home
- [ ] T621 [P] [Plan:6.3] Rewrite `RegisterActionTest` → `AuthControllerTest` (register): test `POST /register`, assert redirect on success, assert validation errors on invalid input
- [ ] T622 [P] [Plan:6.3] Rewrite `AuthRequestProcessorTest` → Spring Security or filter integration test: verify unauthenticated requests to protected URLs return 302/401, authenticated requests pass through
- [ ] T623 [P] [Plan:6.3] Rewrite `CartActionTest` → `CartControllerTest`: `@WebMvcTest(CartController.class)`, `@MockBean` CartService, test add/remove/view cart operations, assert model attributes
- [ ] T624 [P] [Plan:6.3] Rewrite `CheckoutActionTest` → `CheckoutControllerTest`: `@WebMvcTest(CheckoutController.class)`, `@MockBean` OrderFacade, test `POST /checkout`, assert order created and redirect
- [ ] T625 [P] [Plan:6.3] Rewrite `CouponApplyActionTest` → `CouponControllerTest` or `CartControllerTest` (coupon apply): test `POST /cart/coupon`, assert coupon applied to session/model
- [ ] T626 [P] [Plan:6.3] Rewrite `OrderHistoryActionTest` → `OrderControllerTest` (order list): `@WebMvcTest(OrderController.class)`, `@MockBean` OrderService, test `GET /orders`, assert model attribute `orders`
- [ ] T627 [P] [Plan:6.3] Rewrite `OrderCancelActionTest` → `OrderControllerTest` (cancel): test `POST /orders/{id}/cancel`, assert status change and redirect
- [ ] T628 [P] [Plan:6.3] Rewrite `OrderReturnActionTest` → `OrderControllerTest` (return): test `POST /orders/{id}/return`, assert status change and redirect
- [ ] T629 [P] [Plan:6.3] Rewrite `AddressListActionTest` → `AddressControllerTest` (list): `@WebMvcTest(AddressController.class)`, `@MockBean` AddressService, test `GET /addresses`, assert model attribute
- [ ] T630 [P] [Plan:6.3] Rewrite `AddressSaveActionTest` → `AddressControllerTest` (save): test `POST /addresses`, assert redirect on success
- [ ] T631 [P] [Plan:6.3] Rewrite `PointBalanceActionTest` → `PointControllerTest`: `@WebMvcTest(PointController.class)`, `@MockBean` PointService, test `GET /points`, assert model attribute `balance`
- [ ] T632 [P] [Plan:6.3] Rewrite `PasswordForgotActionTest` → `PasswordControllerTest` (forgot): test `POST /password/forgot`, `@MockBean` PasswordResetService + EmailService, assert success view
- [ ] T633 [P] [Plan:6.3] Rewrite `PasswordResetActionTest` → `PasswordControllerTest` (reset): test `POST /password/reset` with token, assert redirect on success
- [ ] T634 [P] [Plan:6.3] Rewrite `AdminProductEditActionTest` → `AdminProductControllerTest`: `@WebMvcTest(AdminProductController.class)`, test `POST /admin/products/{id}`, assert product updated
- [ ] T635 [P] [Plan:6.3] Rewrite `AdminCouponEditActionTest` → `AdminCouponControllerTest`: test `POST /admin/coupons/{id}`, assert coupon updated
- [ ] T636 [P] [Plan:6.3] Rewrite `AdminOrderUpdateActionTest` → `AdminOrderControllerTest` (update): test `POST /admin/orders/{id}/status`, assert order status changed
- [ ] T637 [P] [Plan:6.3] Rewrite `AdminOrderRefundActionTest` → `AdminOrderControllerTest` (refund): test `POST /admin/orders/{id}/refund`, assert refund processed
- [ ] T638 [P] [Plan:6.3] Rewrite `AdminShippingMethodEditActionTest` → `AdminShippingControllerTest`: test `POST /admin/shipping-methods/{id}`, assert shipping method updated

### 6.4 Service Test Migration (2 tests)

- [ ] T639 [Plan:6.4] Migrate `OrderFacadeTest` to JUnit 5 + `@SpringBootTest` or plain unit test: replace `extends DaoTestBase` with Spring test context or manual mock setup, replace JUnit 4 `Assert.*` with JUnit 5 `Assertions.*`, preserve all financial calculation assertions (checkout with coupon + points, cancel, return)
- [ ] T640 [Plan:6.4] Migrate `ScenarioFlowTest` to JUnit 5 + `@SpringBootTest`: replace `extends DaoTestBase` with Spring test context, replace JUnit 4 assertions with JUnit 5, preserve full scenario flow (register → login → search → cart → checkout → cancel → return)

### 6.5 Full Test Suite Gate

- [ ] T641 [Plan:6.5] Remove `StrutsActionTestBase.java` and all Struts test dependencies (`strutstestcase`, `servletunit`) from `pom.xml`
- [ ] T642 [Plan:6.5] Ensure `pom.xml` includes JUnit 5 (`junit-jupiter`), Spring Boot Test (`spring-boot-starter-test`), and H2 (`com.h2database:h2`) as test-scoped dependencies
- [ ] T643 [Plan:6.5] Run `mvn -B test` and verify zero test failures across all 36 migrated test classes (12 DAO + 22 controller + 2 service)
- [ ] T644 [Plan:6.5] Fix any H2-vs-PostgreSQL SQL dialect issues in schema.sql / data.sql that cause test failures (e.g., `SERIAL` → `IDENTITY`, `BYTEA` → `VARBINARY`, unsupported functions)
