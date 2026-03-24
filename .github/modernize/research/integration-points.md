# Integration Points Research

## External Integrations

### 1. SMTP Email Service
- **Classes**: `MailService`, `MailConfig`
- **Protocol**: SMTP via `javax.mail` API
- **Pattern**: `EmailQueue` records stored in DB → `MailService` reads queue and sends emails
- **Configuration**: SMTP host, port, username, password from `AppConfig` (loaded from `app.properties`)
- **Usage**: Order confirmation emails, password reset emails
- **Migration impact**: Replace `javax.mail` with Spring Boot's `JavaMailSender` backed by `spring-boot-starter-mail`

### 2. PostgreSQL Database
- **Connection method**: JNDI DataSource (`jdbc/skishop`) via `DataSourceLocator`, or direct `BasicDataSource` via `DataSourceFactory`
- **Pool**: Apache Commons DBCP 1.2.2
- **Driver**: PostgreSQL JDBC3 driver 9.2
- **Migration impact**: Replace with Spring Boot auto-configured HikariCP DataSource; use `spring.datasource.*` properties

## Internal Service Boundaries

### ServiceLocator Dependencies
`ServiceLocator` provides access to all services:
- AuthService, CartService, ProductService, CategoryService, CouponService
- InventoryService, MailService, OrderFacade, OrderService, PaymentService
- PointService, ShippingService, TaxService, UserService

### Payment Processing
- `PaymentService` handles credit card processing
- `PaymentInfo` DTO carries card details from form → service
- `PaymentResult` returned with success/failure status
- No external payment gateway integration visible — appears to be simulated/internal

## No External Integrations Detected
- No REST/SOAP client calls to external services
- No message queue producers/consumers (Kafka, RabbitMQ, etc.)
- No cache layer (Redis, Memcached)
- No cloud storage (S3, etc.)
- No OAuth/SSO providers
- No third-party SDK integrations
