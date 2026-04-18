# t8 — Service Layer

## Summary
18 service files created across 9 packages. All source business logic ported faithfully from Struts singleton + `new XxxDaoImpl()` pattern to Spring `@Service` with constructor-injected DAO interfaces. Transactional services (InventoryService, PointService, ProductService) retain manual JDBC transaction management via `JdbcTemplate.getDataSource().getConnection()` with `FOR UPDATE` pessimistic locking — matching source behavior exactly. OrderFacadeImpl uses compensating actions (no `@Transactional`). MailService uses Spring `JavaMailSender` + `@Scheduled` queue processor replacing the source's `javax.mail` + `Timer`.

## Deliverables

### Service Files (18 total)
| Package | File | Pattern | Dependencies |
|---------|------|---------|-------------|
| `service/auth/` | AuthResult.java | Plain POJO | — |
| `service/auth/` | AuthService.java | @Service | UserDao, SecurityLogDao |
| `service/user/` | UserService.java | @Service | UserDao |
| `service/catalog/` | ProductService.java | @Service | ProductDao, JdbcTemplate |
| `service/catalog/` | CategoryService.java | @Service | CategoryDao |
| `service/cart/` | CartService.java | @Service | CartDao, PriceDao |
| `service/coupon/` | CouponService.java | @Service | CouponDao, CouponUsageDao |
| `service/order/` | OrderService.java | @Service | OrderDao, ReturnDao |
| `service/order/` | OrderFacade.java | Interface | — |
| `service/order/` | OrderFacadeImpl.java | @Service | 12 dependencies (all services + UserAddressDao) |
| `service/payment/` | PaymentInfo.java | Plain POJO | — |
| `service/payment/` | PaymentResult.java | Plain POJO | — |
| `service/payment/` | PaymentService.java | @Service | PaymentDao |
| `service/inventory/` | InventoryService.java | @Service | JdbcTemplate (manual TX) |
| `service/shipping/` | ShippingService.java | @Service | OrderShippingDao |
| `service/tax/` | TaxService.java | @Service | — |
| `service/point/` | PointService.java | @Service | PointAccountDao, PointTransactionDao, JdbcTemplate (manual TX) |
| `service/mail/` | MailService.java | @Service | EmailQueueDao, JavaMailSender |

### Supporting Resources
- `src/main/resources/mail/password_reset.txt` — email template (copied from source)
- `src/main/resources/mail/order_confirmation.txt` — email template (copied from source)
- `application.properties` — added `spring.mail.from=no-reply@skishop.local`
- `Application.java` — added `@EnableScheduling` for MailService queue processing

## Migration Pattern
- Source: `private final XxxDao xxxDao = new XxxDaoImpl()` → Target: constructor-injected `XxxDao` interface
- Source: `DataSourceLocator.getInstance().getDataSource().getConnection()` manual TX → Target: `JdbcTemplate.getDataSource().getConnection()` manual TX (InventoryService, PointService, ProductService)
- Source: `javax.mail.Session` + `Timer` queue → Target: `JavaMailSender` + `@Scheduled`
- Source: `AppConfig.getInstance().getString("smtp.host")` → Target: `spring.mail.*` properties + `@Value`
- OrderFacade: compensating-action orchestration preserved verbatim (no @Transactional)
- All business logic, validation rules, and calculation algorithms preserved identically

## Build Status
`mvn -f monolith-new/pom.xml clean compile -DskipTests -q` — **PASS** (zero errors)
