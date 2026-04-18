# t7 — Domain & DAO Layer

## Summary
24 domain POJOs + 38 DAO files (19 interfaces + 19 implementations) created. All source SQL preserved verbatim. DAO layer migrated from Commons DBUtils / AbstractDao to Spring JdbcTemplate @Repository with constructor injection. Build passes cleanly.

## Deliverables

### Domain POJOs (24 files)
- `domain/user/` — User.java, Role.java, SecurityLog.java, PasswordResetToken.java
- `domain/product/` — Product.java, Category.java, Price.java
- `domain/cart/` — Cart.java, CartItem.java
- `domain/order/` — Order.java, OrderItem.java, OrderShipping.java, Return.java, Shipment.java
- `domain/payment/` — Payment.java
- `domain/coupon/` — Campaign.java, Coupon.java, CouponUsage.java
- `domain/inventory/` — Inventory.java
- `domain/shipping/` — ShippingMethod.java
- `domain/address/` — Address.java
- `domain/point/` — PointAccount.java, PointTransaction.java
- `domain/mail/` — EmailQueue.java

### DAO Interfaces + Implementations (38 files)
- `dao/user/` — UserDao, UserDaoImpl, SecurityLogDao, SecurityLogDaoImpl, PasswordResetTokenDao, PasswordResetTokenDaoImpl
- `dao/product/` — ProductDao, ProductDaoImpl, PriceDao, PriceDaoImpl
- `dao/category/` — CategoryDao, CategoryDaoImpl
- `dao/cart/` — CartDao, CartDaoImpl
- `dao/order/` — OrderDao, OrderDaoImpl, OrderShippingDao, OrderShippingDaoImpl, ReturnDao, ReturnDaoImpl
- `dao/payment/` — PaymentDao, PaymentDaoImpl
- `dao/coupon/` — CouponDao, CouponDaoImpl, CouponUsageDao, CouponUsageDaoImpl
- `dao/inventory/` — InventoryDao, InventoryDaoImpl
- `dao/shipping/` — ShippingMethodDao, ShippingMethodDaoImpl
- `dao/address/` — UserAddressDao, UserAddressDaoImpl
- `dao/point/` — PointAccountDao, PointAccountDaoImpl, PointTransactionDao, PointTransactionDaoImpl
- `dao/mail/` — EmailQueueDao, EmailQueueDaoImpl

## Migration Pattern
- Source: `AbstractDao` base class + manual `getConnection()`/`closeQuietly()` + `PreparedStatement` + `BeanHandler`
- Target: `@Repository` + constructor-injected `JdbcTemplate` + `RowMapper` lambdas
- SQL statements preserved identically from source
- Domain POJOs copied verbatim (no JPA, no Lombok per constitution)

## Build Status
`mvn -f monolith-new/pom.xml clean compile -DskipTests` — **PASS** (zero errors)

## Notes
- MailEvent.java from architect spec does NOT exist in source — omitted per constitution (source-anchored rewrite)
- Domain field `toAddr` in EmailQueue matches source; DAO maps to column `to_address`
- Domain field `referenceId` in PointTransaction matches source; DAO maps to column `reference_id`
