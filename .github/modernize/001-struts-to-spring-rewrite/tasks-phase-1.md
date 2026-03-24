## Phase 1: Project Foundation & Spring Boot Scaffolding

**Batch**: 1
**Goal**: Replace the Struts project infrastructure with Spring Boot; establish compile-pass baseline.
**Exit Criteria**: `mvn -B compile` passes. No Struts, commons-dbcp, commons-dbutils, or log4j references remain in production code.

---

### 1.1 Maven POM Rewrite

- [ ] T100 [Plan:1.1] Add Spring Boot 3.2.x parent POM (`org.springframework.boot:spring-boot-starter-parent:3.2.x`) to `pom.xml`. Set `<java.version>21</java.version>`. Change `<packaging>` from `war` to `jar`.
- [ ] T101 [Plan:1.1] Add Spring Boot starter dependencies to `pom.xml`: `spring-boot-starter-web`, `spring-boot-starter-jdbc`, `spring-boot-starter-mail`, `spring-boot-starter-validation`. Add `spring-boot-starter-test` with `<scope>test</scope>`.
- [ ] T102 [Plan:1.1] Add non-starter dependencies to `pom.xml`: `org.postgresql:postgresql:42.x` (runtime), `jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api`, `org.glassfish.web:jakarta.servlet.jsp.jstl` (runtime), `org.apache.tomcat.embed:tomcat-embed-jasper` (provided), `jakarta.servlet.jsp:jakarta.servlet.jsp-api` (provided).
- [ ] T103 [Plan:1.1] Remove all legacy dependencies from `pom.xml`: struts-core, struts-taglib, struts-tiles, struts-extras, commons-dbcp, commons-pool, commons-dbutils, commons-beanutils, commons-digester, commons-validator, commons-chain, commons-fileupload, commons-io, log4j, oro, strutstestcase. Remove any explicit servlet-api 2.5 dependency.
- [ ] T104 [Plan:1.1] Add `spring-boot-maven-plugin` to `pom.xml` `<build><plugins>` section. Remove `maven-war-plugin` if present. Set `<finalName>skishop-monolith</finalName>`.

### 1.2 Spring Boot Application Class

- [ ] T105 [Plan:1.2] Create `src/main/java/com/skishop/Application.java` with `@SpringBootApplication` annotation and `main()` method calling `SpringApplication.run()`.

### 1.3 Application Properties

- [ ] T106 [Plan:1.3] Create `src/main/resources/application.properties` with Spring DataSource configuration: `spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:skishop}`, `spring.datasource.username=${DB_USER:skishop}`, `spring.datasource.password=${DB_PASSWORD:skishop}`. Add HikariCP pool settings: `spring.datasource.hikari.maximum-pool-size=${DB_POOL_MAX_ACTIVE:20}`, `spring.datasource.hikari.minimum-idle=${DB_POOL_MAX_IDLE:5}`, `spring.datasource.hikari.connection-timeout=${DB_POOL_MAX_WAIT:30000}`.
- [ ] T107 [Plan:1.3] Add JSP view resolver settings to `application.properties`: `spring.mvc.view.prefix=/WEB-INF/jsp/`, `spring.mvc.view.suffix=.jsp`. Add `server.port=8080`.
- [ ] T108 [Plan:1.3] Add logging configuration to `application.properties`: `logging.level.root=INFO`, `logging.level.com.skishop=DEBUG`, `logging.pattern.console` matching existing log format. Add `spring.sql.init.mode=always`, `spring.sql.init.schema-locations=classpath:db/schema.sql`, `spring.sql.init.data-locations=classpath:db/data.sql` for DB init.
- [ ] T109 [P] [Plan:1.3] Add mail configuration to `application.properties`: `spring.mail.host`, `spring.mail.port`, `spring.mail.username`, `spring.mail.password` with env-var defaults from existing `app.properties` mail settings.

### 1.4 Remove Struts Infrastructure

- [ ] T110 [Plan:1.4] Delete Struts XML configuration files: `src/main/webapp/WEB-INF/struts-config.xml`, `src/main/webapp/WEB-INF/tiles-defs.xml`, `src/main/webapp/WEB-INF/validation.xml`, `src/main/webapp/WEB-INF/validator-rules.xml`, `src/main/webapp/WEB-INF/web.xml`, `src/main/webapp/META-INF/context.xml`.
- [ ] T111 [Plan:1.4] Delete `src/main/resources/log4j.properties` (replaced by Spring Boot Logback defaults).
- [ ] T112 [Plan:1.4] Delete Struts infrastructure Java classes: `com.skishop.common.service.ServiceLocator`, `com.skishop.common.dao.DaoFactory`, `com.skishop.common.dao.DataSourceLocator`, `com.skishop.common.dao.DataSourceFactory`, `com.skishop.common.dao.AbstractDao`, `com.skishop.common.dao.DaoException`, `com.skishop.common.config.AppConfig`.

### 1.5 Domain Objects Preservation

- [ ] T113 [P] [Plan:1.5] Verify all 24 domain POJOs compile under Java 21 without modification. Check for any `java.util.Date` → import issues or deprecated API usage in: `com.skishop.domain.address.Address`, `com.skishop.domain.cart.{Cart, CartItem}`, `com.skishop.domain.coupon.{Campaign, Coupon, CouponUsage}`, `com.skishop.domain.inventory.Inventory`, `com.skishop.domain.mail.EmailQueue`, `com.skishop.domain.order.{Order, OrderItem, OrderShipping, Return, Shipment}`, `com.skishop.domain.payment.Payment`, `com.skishop.domain.point.{PointAccount, PointTransaction}`, `com.skishop.domain.product.{Category, Price, Product}`, `com.skishop.domain.shipping.ShippingMethod`, `com.skishop.domain.user.{PasswordResetToken, Role, SecurityLog, User}`.
- [ ] T114 [P] [Plan:1.5] Fix any Java 21 compilation issues in domain POJOs (e.g., update deprecated `Date` constructors, raw type usage). Preserve all field types, getter/setter signatures, and class structure exactly.

### 1.6 DAO Interface Preservation + JdbcTemplate Implementation

- [ ] T115 [P] [Plan:1.6] Rewrite `UserDaoImpl` (`com.skishop.dao.user.UserDaoImpl`): replace `extends AbstractDao` + commons-dbutils with `@Repository` + constructor-injected `JdbcTemplate`. Preserve `UserDao` interface. Preserve exact SQL queries. Use `BeanPropertyRowMapper<User>` or custom `RowMapper`.
- [ ] T116 [P] [Plan:1.6] Rewrite `SecurityLogDaoImpl` (`com.skishop.dao.user.SecurityLogDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `SecurityLogDao` interface and SQL queries.
- [ ] T117 [P] [Plan:1.6] Rewrite `PasswordResetTokenDaoImpl` (`com.skishop.dao.user.PasswordResetTokenDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `PasswordResetTokenDao` interface and SQL queries.
- [ ] T118 [P] [Plan:1.6] Rewrite `ProductDaoImpl` (`com.skishop.dao.product.ProductDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `ProductDao` interface and SQL queries.
- [ ] T119 [P] [Plan:1.6] Rewrite `PriceDaoImpl` (`com.skishop.dao.product.PriceDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `PriceDao` interface and SQL queries.
- [ ] T120 [P] [Plan:1.6] Rewrite `CategoryDaoImpl` (`com.skishop.dao.category.CategoryDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `CategoryDao` interface and SQL queries.
- [ ] T121 [P] [Plan:1.6] Rewrite `CartDaoImpl` (`com.skishop.dao.cart.CartDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `CartDao` interface and SQL queries.
- [ ] T122 [P] [Plan:1.6] Rewrite `OrderDaoImpl` (`com.skishop.dao.order.OrderDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `OrderDao` interface and SQL queries.
- [ ] T123 [P] [Plan:1.6] Rewrite `OrderShippingDaoImpl` (`com.skishop.dao.order.OrderShippingDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `OrderShippingDao` interface and SQL queries.
- [ ] T124 [P] [Plan:1.6] Rewrite `ReturnDaoImpl` (`com.skishop.dao.order.ReturnDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `ReturnDao` interface and SQL queries.
- [ ] T125 [P] [Plan:1.6] Rewrite `PaymentDaoImpl` (`com.skishop.dao.payment.PaymentDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `PaymentDao` interface and SQL queries.
- [ ] T126 [P] [Plan:1.6] Rewrite `CouponDaoImpl` (`com.skishop.dao.coupon.CouponDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `CouponDao` interface and SQL queries.
- [ ] T127 [P] [Plan:1.6] Rewrite `CouponUsageDaoImpl` (`com.skishop.dao.coupon.CouponUsageDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `CouponUsageDao` interface and SQL queries.
- [ ] T128 [P] [Plan:1.6] Rewrite `InventoryDaoImpl` (`com.skishop.dao.inventory.InventoryDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `InventoryDao` interface and SQL queries.
- [ ] T129 [P] [Plan:1.6] Rewrite `PointAccountDaoImpl` (`com.skishop.dao.point.PointAccountDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `PointAccountDao` interface and SQL queries.
- [ ] T130 [P] [Plan:1.6] Rewrite `PointTransactionDaoImpl` (`com.skishop.dao.point.PointTransactionDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `PointTransactionDao` interface and SQL queries.
- [ ] T131 [P] [Plan:1.6] Rewrite `UserAddressDaoImpl` (`com.skishop.dao.address.UserAddressDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `UserAddressDao` interface and SQL queries.
- [ ] T132 [P] [Plan:1.6] Rewrite `ShippingMethodDaoImpl` (`com.skishop.dao.shipping.ShippingMethodDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `ShippingMethodDao` interface and SQL queries.
- [ ] T133 [P] [Plan:1.6] Rewrite `EmailQueueDaoImpl` (`com.skishop.dao.mail.EmailQueueDaoImpl`): `@Repository` + `JdbcTemplate`. Preserve `EmailQueueDao` interface and SQL queries.

### 1.7 Service Layer Spring Conversion

- [ ] T134 [P] [Plan:1.7] Convert `AuthService` (`com.skishop.service.auth.AuthService`): add `@Service`, replace `new UserDaoImpl()` / `DaoFactory` / `ServiceLocator` calls with constructor injection of `UserDao`, `SecurityLogDao`. Add `@Transactional` if multi-DAO operations exist.
- [ ] T135 [P] [Plan:1.7] Convert `UserService` (`com.skishop.service.user.UserService`): add `@Service`, replace direct DAO instantiation with constructor injection of `UserDao`. Add `@Transactional` where needed.
- [ ] T136 [P] [Plan:1.7] Convert `ProductService` (`com.skishop.service.catalog.ProductService`): add `@Service`, replace direct DAO instantiation with constructor injection of `ProductDao`, `PriceDao`. Add `@Transactional` where needed.
- [ ] T137 [P] [Plan:1.7] Convert `CategoryService` (`com.skishop.service.catalog.CategoryService`): add `@Service`, constructor injection of `CategoryDao`.
- [ ] T138 [P] [Plan:1.7] Convert `CartService` (`com.skishop.service.cart.CartService`): add `@Service`, constructor injection of `CartDao`. Add `@Transactional` where needed.
- [ ] T139 [P] [Plan:1.7] Convert `OrderService` (`com.skishop.service.order.OrderService`): add `@Service`, constructor injection of `OrderDao`, `OrderShippingDao`. Add `@Transactional` for multi-DAO operations.
- [ ] T140 [P] [Plan:1.7] Convert `OrderFacadeImpl` (`com.skishop.service.order.OrderFacadeImpl`): add `@Service`, constructor injection of all coordinated services (OrderService, InventoryService, PaymentService, PointService, ShippingService, CartService, CouponService, MailService). Add `@Transactional` for checkout orchestration.
- [ ] T141 [P] [Plan:1.7] Convert `PaymentService` (`com.skishop.service.payment.PaymentService`): add `@Service`, constructor injection of `PaymentDao`. Add `@Transactional` where needed.
- [ ] T142 [P] [Plan:1.7] Convert `CouponService` (`com.skishop.service.coupon.CouponService`): add `@Service`, constructor injection of `CouponDao`, `CouponUsageDao`. Add `@Transactional` where needed.
- [ ] T143 [P] [Plan:1.7] Convert `InventoryService` (`com.skishop.service.inventory.InventoryService`): add `@Service`, constructor injection of `InventoryDao`. Add `@Transactional` where needed.
- [ ] T144 [P] [Plan:1.7] Convert `PointService` (`com.skishop.service.point.PointService`): add `@Service`, constructor injection of `PointAccountDao`, `PointTransactionDao`. Add `@Transactional` for multi-DAO operations.
- [ ] T145 [P] [Plan:1.7] Convert `ShippingService` (`com.skishop.service.shipping.ShippingService`): add `@Service`, constructor injection of `ShippingMethodDao`, `OrderShippingDao`.
- [ ] T146 [P] [Plan:1.7] Convert `MailService` (`com.skishop.service.mail.MailService`): add `@Service`, constructor injection of `EmailQueueDao`. Replace any direct SMTP code with Spring `JavaMailSender` injection.
- [ ] T147 [P] [Plan:1.7] Convert `TaxService` (`com.skishop.service.tax.TaxService`): add `@Service`, constructor injection of any dependencies.

### 1.8 Compile Gate

- [ ] T148 [Plan:1.8] Run `mvn -B compile` and verify zero compilation errors. All classes must compile against Spring Boot dependencies with no Struts, commons-dbcp, commons-dbutils, or log4j imports remaining in production code.
- [ ] T149 [Plan:1.8] Verify no residual references to deleted infrastructure: grep production source for `ServiceLocator`, `DaoFactory`, `DataSourceLocator`, `DataSourceFactory`, `AbstractDao`, `DaoException`, `AppConfig`, `struts`, `commons.dbcp`, `commons.dbutils`, `log4j`. All must return zero hits.
