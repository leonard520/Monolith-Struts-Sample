# Data Model Research

## Database

- **Type**: PostgreSQL 9.2
- **Schema management**: Manual SQL scripts (`schema.sql`, `data.sql` in `src/main/resources/db/`)
- **Connection pooling**: Apache Commons DBCP 1.2.2
- **ORM**: None — raw JDBC via `commons-dbutils` (`QueryRunner`, `BeanHandler`, `BeanListHandler`)
- **JNDI DataSource**: `jdbc/skishop` configured in web.xml and Tomcat context

## Entity Inventory

### Order Domain (`com.skishop.domain.order`)
| Entity | Key Fields | Description |
|--------|-----------|-------------|
| `Order` | orderId, userId, status, totalAmount, orderDate | Customer order record |
| `OrderItem` | orderItemId, orderId, productId, quantity, unitPrice | Line item in an order |
| `OrderShipping` | id, orderId, shippingMethodId, trackingNumber | Shipping info for an order |
| `Shipment` | shipmentId, orderId, status, shippedDate | Shipment tracking |
| `Return` | returnId, orderId, reason, status, requestDate | Return/refund request |

### User Domain (`com.skishop.domain.user`)
| Entity | Key Fields | Description |
|--------|-----------|-------------|
| `User` | userId, email, username, passwordHash, salt, role | User account with role (USER/ADMIN) |
| `Role` | roleId, roleName | User role definition |
| `PasswordResetToken` | tokenId, userId, token, expiresAt | Time-limited password reset token |
| `SecurityLog` | logId, userId, action, ipAddress, timestamp | Security audit trail |

### Product Domain (`com.skishop.domain.product`)
| Entity | Key Fields | Description |
|--------|-----------|-------------|
| `Product` | productId, name, description, categoryId, imageUrl | Ski equipment product |
| `Price` | priceId, productId, amount, currency, effectiveDate | Product pricing (supports history) |
| `Category` | categoryId, name, description | Product category (Ski, Boot, Wear, etc.) |

### Cart Domain (`com.skishop.domain.cart`)
| Entity | Key Fields | Description |
|--------|-----------|-------------|
| `Cart` | cartId, userId, createdAt | Shopping cart header |
| `CartItem` | cartItemId, cartId, productId, quantity | Item in a shopping cart |

### Payment Domain (`com.skishop.domain.payment`)
| Entity | Key Fields | Description |
|--------|-----------|-------------|
| `Payment` | paymentId, orderId, method, amount, status, transactionDate | Payment record |

### Coupon Domain (`com.skishop.domain.coupon`)
| Entity | Key Fields | Description |
|--------|-----------|-------------|
| `Coupon` | couponId, code, discountType, discountValue, validFrom, validTo | Discount coupon |
| `CouponUsage` | usageId, couponId, userId, orderId, usedAt | Coupon redemption tracking |
| `Campaign` | campaignId, name, startDate, endDate | Marketing campaign |

### Other Domains
| Entity | Package | Description |
|--------|---------|-------------|
| `PointAccount` | `domain.point` | Loyalty point balance per user |
| `PointTransaction` | `domain.point` | Point earn/redeem transaction log |
| `Address` | `domain.address` | User shipping/billing address |
| `Inventory` | `domain.inventory` | Product stock level |
| `ShippingMethod` | `domain.shipping` | Available shipping options with rates |
| `EmailQueue` | `domain.mail` | Queued outbound emails |

## Relationships

- `Order` → `User` (many-to-one via userId)
- `Order` → `OrderItem` (one-to-many)
- `Order` → `OrderShipping` (one-to-one)
- `Order` → `Payment` (one-to-many)
- `Order` → `Return` (one-to-many)
- `Cart` → `CartItem` (one-to-many)
- `CartItem` → `Product` (many-to-one)
- `Product` → `Category` (many-to-one)
- `Product` → `Price` (one-to-many)
- `Coupon` → `CouponUsage` (one-to-many)
- `User` → `Address` (one-to-many)
- `User` → `PointAccount` (one-to-one)
- `PointAccount` → `PointTransaction` (one-to-many)

## Transaction Boundaries

- Services manage transactions manually — no declarative `@Transactional`
- `DataSourceLocator`/`DataSourceFactory` provides connections
- DAO implementations call `AbstractDao.getConnection()` and handle connection lifecycle
- `OrderFacadeImpl` coordinates multi-step operations (cart → inventory → order → payment → points → email) — this is a critical transaction boundary

## Key Entities Summary (Top 8)

1. **Order** — Central business entity; tracks customer purchases with status lifecycle
2. **User** — User account with email, credentials, and role-based access (USER/ADMIN)
3. **Product** — Ski equipment catalog item with category and pricing
4. **Cart/CartItem** — Session-based shopping cart with product selections
5. **Payment** — Payment transaction record linked to orders
6. **Coupon** — Discount codes with usage tracking and campaign association
7. **PointAccount** — Loyalty program balance per user
8. **Address** — User shipping/billing addresses for order fulfillment
