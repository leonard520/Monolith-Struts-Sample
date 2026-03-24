# Data Model: Struts 1.x to Spring Boot 3.2 MVC Rewrite

**Feature**: 001-struts-to-spring-rewrite
**Date**: 2026-03-21
**Mode**: REWRITE

> ⚠️ **Schema Preservation Rule**: The target data model MUST preserve the existing PostgreSQL schema exactly (table names, column names, data types, relationships). See Constitution Principle III (NON-NEGOTIABLE).

## Entity Inventory

### Order Domain

#### Order
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| orderId | String (UUID) | PK | |
| orderNumber | String | UNIQUE | Format: "ORD-{timestamp}" |
| userId | String | FK → users | |
| status | String | NOT NULL | CREATED → CONFIRMED → SHIPPED → DELIVERED / CANCELLED / RETURNED |
| subtotal | BigDecimal | NOT NULL | |
| taxAmount | BigDecimal | | |
| shippingFee | BigDecimal | | |
| discountAmount | BigDecimal | | |
| totalAmount | BigDecimal | NOT NULL | |
| couponCode | String | | Applied coupon code |
| redeemedPoints | int | | Points used |
| paymentStatus | String | | From PaymentResult |
| orderDate | Date | NOT NULL | |

#### OrderItem
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| orderItemId | String (UUID) | PK | |
| orderId | String | FK → orders | |
| productId | String | FK → products | |
| quantity | int | NOT NULL | |
| unitPrice | BigDecimal | NOT NULL | Price snapshot at order time |

#### OrderShipping
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | String (UUID) | PK | |
| orderId | String | FK → orders | |
| shippingMethodId | String | FK → shipping_methods | |
| trackingNumber | String | | |
| shippingFee | BigDecimal | | |

#### Shipment
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| shipmentId | String (UUID) | PK | |
| orderId | String | FK → orders | |
| status | String | | |
| shippedDate | Date | | |

#### Return
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| returnId | String (UUID) | PK | |
| orderId | String | FK → orders | |
| reason | String | | |
| status | String | | |
| requestDate | Date | | |

### User Domain

#### User
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| userId | String (UUID) | PK | |
| email | String | UNIQUE, NOT NULL | |
| username | String | NOT NULL | |
| passwordHash | String | NOT NULL | |
| salt | String | NOT NULL | |
| role | String | NOT NULL | "USER" or "ADMIN" |
| status | String | | "ACTIVE" or "LOCKED" |

#### PasswordResetToken
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| tokenId | String (UUID) | PK | |
| userId | String | FK → users | |
| token | String | UNIQUE | |
| expiresAt | Date | NOT NULL | |

#### SecurityLog
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| logId | String (UUID) | PK | |
| userId | String | FK → users | |
| eventType | String | NOT NULL | LOGIN_FAILURE, ACCOUNT_LOCKED, etc. |
| ipAddress | String | | |
| userAgent | String | | |
| detailsJson | String | | JSON string |
| timestamp | Date | | |

### Product Domain

#### Product
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| productId | String | PK | Format: "PSK001", etc. |
| name | String | NOT NULL | |
| description | String | | |
| categoryId | String | FK → categories | |
| imageUrl | String | | |

#### Price
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| priceId | String (UUID) | PK | |
| productId | String | FK → products | |
| regularPrice | BigDecimal | | |
| currency | String | | Default: "JPY" |
| effectiveDate | Date | | |

#### Category
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| categoryId | String | PK | Format: "c-1", etc. |
| name | String | NOT NULL | Ski, Boot, Wear, Helmet, Glove, Pole, Wax |
| description | String | | |

### Cart Domain

#### Cart
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| cartId | String (UUID) | PK | |
| userId | String | FK → users | Nullable for anonymous |
| sessionId | String | | |
| status | String | | ACTIVE, CHECKED_OUT |
| expiresAt | Date | | 30 days from creation |

#### CartItem
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| cartItemId | String (UUID) | PK | |
| cartId | String | FK → carts | |
| productId | String | FK → products | |
| quantity | int | NOT NULL | |
| unitPrice | BigDecimal | | Resolved from Price at add time |

### Payment Domain

#### Payment
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| paymentId | String (UUID) | PK | |
| orderId | String | FK → orders | |
| cartId | String | | |
| amount | BigDecimal | NOT NULL | |
| currency | String | | Default: "JPY" |
| status | String | NOT NULL | AUTHORIZED, FAILED, VOID, REFUNDED |
| paymentIntentId | String | | |
| createdAt | Date | | |

### Coupon Domain

#### Coupon
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| couponId | String (UUID) | PK | |
| code | String | UNIQUE, NOT NULL | e.g. "SAVE10" |
| couponType | String | | "PERCENT" or "FIXED" |
| discountValue | BigDecimal | | |
| minimumAmount | BigDecimal | | |
| maximumDiscount | BigDecimal | | |
| usageLimit | int | | 0 = unlimited |
| usedCount | int | | |
| isActive | boolean | | |
| expiresAt | Date | | |

#### CouponUsage
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| usageId | String (UUID) | PK | |
| couponId | String | FK → coupons | |
| userId | String | FK → users | |
| orderId | String | FK → orders | |
| discountApplied | BigDecimal | | |
| usedAt | Date | | |

#### Campaign
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| campaignId | String (UUID) | PK | |
| name | String | | |
| startDate | Date | | |
| endDate | Date | | |

### Points Domain

#### PointAccount
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | String (UUID) | PK | |
| userId | String | FK → users, UNIQUE | |
| balance | int | NOT NULL | |

#### PointTransaction
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | String (UUID) | PK | |
| userId | String | FK → users | |
| type | String | NOT NULL | EARN, REDEEM, REFUND, REVOKE |
| amount | int | NOT NULL | Positive for earn/refund, negative for redeem/revoke |
| referenceId | String | | Order ID |
| description | String | | |
| expiresAt | Date | | |
| expired | boolean | | |
| createdAt | Date | | |

### Other Domains

#### Address
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | String (UUID) | PK | |
| userId | String | FK → users | |
| label | String | NOT NULL | |
| recipientName | String | NOT NULL | |
| postalCode | String | NOT NULL | |
| prefecture | String | NOT NULL | |
| address1 | String | NOT NULL | |
| address2 | String | | |
| address3 | String | | |
| phoneNumber | String | | |
| isDefault | boolean | | |

#### Inventory
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | String | PK | |
| productId | String | FK → products | |
| quantity | int | NOT NULL | |

#### ShippingMethod
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | String (UUID) | PK | |
| name | String | NOT NULL | |
| rate | BigDecimal | | |
| estimatedDays | int | | |
| isActive | boolean | | |

#### EmailQueue
| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | String (UUID) | PK | |
| toAddress | String | NOT NULL | |
| subject | String | | |
| body | String | | |
| status | String | | PENDING, SENT, FAILED |
| createdAt | Date | | |
| sentAt | Date | | |

## Relationships

```
User 1──* Order
User 1──* Address
User 1──1 PointAccount
PointAccount 1──* PointTransaction
Order 1──* OrderItem
Order 1──1 OrderShipping
Order 1──* Payment
Order 1──* Return
Product *──1 Category
Product 1──* Price
Cart 1──* CartItem
CartItem *──1 Product
Coupon 1──* CouponUsage
```

## State Transitions

### Order Status
```
CREATED → CONFIRMED → SHIPPED → DELIVERED
CREATED → CANCELLED
CONFIRMED → CANCELLED
DELIVERED → RETURNED
```

### Payment Status
```
AUTHORIZED → VOID (on cancellation)
AUTHORIZED → REFUNDED (on return)
FAILED (terminal)
```

### Cart Status
```
ACTIVE → CHECKED_OUT (on order placement)
```
