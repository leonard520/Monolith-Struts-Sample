# Business Logic Inventory

**Source Application**: SkiShop Monolith (Struts 1.3.10)
**Extraction Date**: 2026-03-21
**Total Business Logic Units**: 28

## Summary by Category

| Category | Count | Complexity |
|----------|-------|------------|
| Authentication & Security | 4 | High |
| Cart & Checkout | 5 | High |
| Payment & Billing | 3 | Medium-High |
| Coupon & Discount | 3 | Medium |
| Points / Loyalty | 4 | Medium |
| Order Lifecycle | 4 | High |
| User & Address Management | 3 | Low-Medium |
| Validation | 2 | Low |

## Business Logic Units

### Authentication & Security

#### BL-001: User Authentication
- **Source**: `AuthService.java:21-42`
- **Purpose**: Validates email/password credentials, enforces account lock policy
- **Inputs**: email (String), passwordRaw (String), ipAddress (String), userAgent (String)
- **Outputs**: AuthResult (success with User, or failure with reason code)
- **Business Rules**:
  - Null email or password → INVALID_INPUT
  - User not found → USER_NOT_FOUND
  - Account locked (status="LOCKED") → USER_LOCKED
  - Password mismatch (PasswordHasher.matches) → INVALID_CREDENTIALS, record failure
  - 5+ consecutive failures → account auto-locked
- **Dependencies**: UserDao, SecurityLogDao, PasswordHasher
- **Side Effects**: SecurityLog insert on failure; user status update to LOCKED on threshold
- **Rewrite Notes**: Preserve PasswordHasher for hash+salt compatibility with existing DB credentials

#### BL-002: CSRF Token Management
- **Source**: `AuthRequestProcessor.java:26-42`
- **Purpose**: Generate and validate CSRF tokens on GET/POST requests
- **Business Rules**:
  - On GET: initialize token in session if missing (avoid invalidating across tabs)
  - On POST: validate `_csrfToken` request parameter against session token
  - Invalid token → HTTP 403
  - After successful validation → reset token (one-time use)
- **Rewrite Notes**: Custom implementation with HandlerInterceptor; do NOT use Spring Security CSRF

#### BL-003: Role-Based Access Control
- **Source**: `AuthRequestProcessor.java:44-60`
- **Purpose**: Enforce USER/ADMIN role requirements per URL
- **Business Rules**:
  - No roles configured → allow all
  - Unauthenticated user on protected URL → redirect to `/login`
  - Authenticated but wrong role → HTTP 403
  - Role check is comma-separated string match (e.g., "USER,ADMIN")
- **Rewrite Notes**: HandlerInterceptor with URL→roles mapping derived from struts-config.xml

#### BL-004: Password Reset Flow
- **Source**: `UserService.java`, `PasswordForgotAction.java`, `PasswordResetAction.java`
- **Purpose**: Token-based password reset with user enumeration prevention
- **Business Rules**:
  - Forgot: always show success message regardless of email existence
  - Token generated with expiry; stored in DB
  - Reset: validate token exists and not expired
  - Invalid/expired token → error message
  - On success: update password hash+salt, delete token

### Cart & Checkout

#### BL-005: Cart Subtotal Calculation
- **Source**: `CartService.java:66-75`
- **Purpose**: Calculate cart subtotal from line items
- **Business Rules**:
  - subtotal = SUM(unitPrice × quantity) for each CartItem
  - Null items → BigDecimal.ZERO

#### BL-006: Add to Cart (merge or create)
- **Source**: `CartService.java:36-57`
- **Purpose**: Add product to cart; merge quantity if already exists
- **Business Rules**:
  - quantity ≤ 0 → no-op
  - Product already in cart → increment quantity
  - New product → create CartItem with resolved unit price from PriceDao
- **Side Effects**: CartItem insert or update

#### BL-007: Checkout Orchestration (CRITICAL)
- **Source**: `OrderFacadeImpl.java:72-130`
- **Purpose**: Complete checkout — the most complex transaction boundary
- **Inputs**: cartId, couponCode, usePoints, PaymentInfo, userId
- **Outputs**: Order (created)
- **Business Rules (sequential)**:
  1. Load cart and items (fail if null/empty)
  2. Calculate subtotal
  3. Validate coupon and calculate discount
  4. Normalize points for redemption
  5. Redeem loyalty points
  6. Calculate tax on (discounted - points) amount; floor at ZERO
  7. Calculate shipping fee
  8. totalAmount = taxable + tax + shippingFee
  9. Reserve inventory
  10. Authorize payment (fail → rollback)
  11. Create order + order items
  12. Mark coupon used
  13. Award loyalty points on totalAmount
  14. Save shipping record
  15. Clear cart
  16. Send order confirmation email
- **Rollback on failure**: void payment, release inventory, refund points
- **Dependencies**: CartService, CouponService, InventoryService, PaymentService, OrderService, PointService, ShippingService, TaxService, MailService
- **Rewrite Notes**: Wrap in @Transactional; preserve exact calculation order and rollback semantics

#### BL-008: Cart Session Management
- **Source**: `CartAction.java`, `CartService.java:21-30`  
- **Purpose**: Create cart with userId + sessionId, 30-day expiry
- **Business Rules**: Cart has status ACTIVE, expires in 30 days

#### BL-009: Shipping Fee Calculation
- **Source**: `ShippingService.java`
- **Purpose**: Calculate shipping fee based on order amount
- **Rewrite Notes**: Preserve exact calculation logic

### Payment & Billing

#### BL-010: Payment Authorization
- **Source**: `PaymentService.java:13-30`
- **Purpose**: Validate card and authorize payment
- **Business Rules**:
  - Validate card number (Luhn algorithm, 12-19 digits)
  - Validate expiry date (not expired)
  - On success: create Payment record with status AUTHORIZED
  - On failure: create Payment record with status FAILED
- **Side Effects**: Payment record inserted in DB

#### BL-011: Payment Void
- **Source**: `PaymentService.java:32-34`
- **Purpose**: Void a previously authorized payment
- **Business Rules**: Find payment by orderId, update status to VOID

#### BL-012: Payment Refund
- **Source**: `PaymentService.java:36-38`
- **Purpose**: Refund payment for returned/cancelled order
- **Business Rules**: Find payment by orderId, update status to REFUNDED

### Coupon & Discount

#### BL-013: Coupon Validation
- **Source**: `CouponService.java:19-42`
- **Purpose**: Validate coupon code against business rules
- **Business Rules**:
  - Null/empty code → return null (no coupon)
  - Coupon not found → throw "Coupon not found"
  - Inactive → throw "Coupon inactive"
  - Usage limit exceeded → throw "Coupon usage limit reached"
  - Subtotal below minimum → throw "Subtotal below minimum"
  - Expired → throw "Coupon expired"

#### BL-014: Discount Calculation
- **Source**: `CouponService.java:48-65`
- **Purpose**: Calculate discount amount from coupon
- **Business Rules**:
  - PERCENT type: discount = amount × discountValue / 100 (rounded HALF_UP, scale 2)
  - FIXED type: discount = discountValue
  - Cap at maximumDiscount if set
  - Cap at amount (cannot exceed subtotal)

#### BL-015: Coupon Usage Tracking
- **Source**: `CouponService.java:67-80`
- **Purpose**: Record coupon usage after order placement
- **Side Effects**: Increment usedCount on Coupon; insert CouponUsage record

### Points / Loyalty

#### BL-016: Award Points
- **Source**: `PointService.java:23-47`
- **Purpose**: Calculate and award loyalty points after order placement
- **Business Rules**:
  - Points = totalAmount × rate (1 point per 100 yen equivalent)
  - Ensure account exists (create if not)
  - Create EARN transaction with 365-day expiry
- **Side Effects**: PointAccount balance increment; PointTransaction insert

#### BL-017: Redeem Points
- **Source**: `PointService.java:49-62`
- **Purpose**: Redeem loyalty points during checkout
- **Business Rules**:
  - Expire stale points first
  - Check balance ≥ requested points; throw if insufficient
  - Decrement balance; create REDEEM transaction

#### BL-018: Refund Points
- **Source**: `PointService.java:64-72`
- **Purpose**: Refund points on order cancellation/failure
- **Side Effects**: Increment balance; create REFUND transaction

#### BL-019: Revoke Points
- **Source**: `PointService.java:74-82`
- **Purpose**: Revoke earned points on order cancellation
- **Side Effects**: Decrement balance; create REVOKE transaction

### Order Lifecycle

#### BL-020: Order Cancellation
- **Source**: `OrderFacadeImpl.java:132+`
- **Purpose**: Cancel an order with rollback of reservations and payments
- **Business Rules**:
  - Only CREATED or CONFIRMED orders can be cancelled
  - Release inventory
  - Void payment
  - Revoke earned points
  - Update order status to CANCELLED

#### BL-021: Order Return
- **Source**: `OrderFacadeImpl.java`
- **Purpose**: Process a return request for a delivered order
- **Business Rules**:
  - Validate order exists and belongs to user
  - Create Return record with reason
  - Update order status

#### BL-022: Order History
- **Source**: `OrderService.java`
- **Purpose**: List orders for a user with pagination
- **Business Rules**: Filter by userId, order by date descending

#### BL-023: Order Building
- **Source**: `OrderService.java`
- **Purpose**: Construct Order + OrderItem records from checkout data
- **Business Rules**: Map cart items to order items with pricing snapshot

### User & Address Management

#### BL-024: User Registration
- **Source**: `UserService.java`, `RegisterAction.java`
- **Purpose**: Create new user account with validation
- **Business Rules**:
  - Validate required fields (email, username, password)
  - Check password confirmation match
  - Check email uniqueness (no duplicate)
  - Hash password with salt using PasswordHasher
  - Assign USER role

#### BL-025: Address Validation
- **Source**: `AddressForm.java`, `AddressSaveAction.java`
- **Purpose**: Validate address fields before save
- **Business Rules**: Required fields: label, recipientName, postalCode, prefecture, address1

#### BL-026: Address Management
- **Source**: `UserAddressDao.java`, `AddressListAction.java`, `AddressSaveAction.java`
- **Purpose**: CRUD operations for user addresses
- **Rewrite Notes**: Route through AddressService (fix existing layering violation where AddressListAction accesses DAO directly)

### Tax Calculation

#### BL-027: Tax Calculation
- **Source**: `TaxService.java:8-13`
- **Purpose**: Calculate tax on taxable amount
- **Business Rules**:
  - Fixed 10% tax rate
  - Null amount → ZERO
  - Rounding: HALF_UP, scale 2

### Email Notification

#### BL-028: Order Confirmation Email
- **Source**: `MailService.java`, `OrderFacadeImpl.java`
- **Purpose**: Queue and send order confirmation email
- **Business Rules**: Store in EmailQueue (DB), process via MailService using SMTP
- **Rewrite Notes**: Use Spring JavaMailSender; preserve EmailQueue pattern

## Cross-Cutting Concerns

### Authentication/Authorization
- **Location**: AuthRequestProcessor, AuthService
- **Pattern**: Session-based with role checking via URL mapping
- **Rewrite approach**: HandlerInterceptor + HttpSession

### Transaction Management
- **Location**: OrderFacadeImpl (primary), all DAOs via AbstractDao
- **Pattern**: Manual connection management; no declarative transactions
- **Rewrite approach**: @Transactional on service methods; JdbcTemplate handles connections

### Error Handling
- **Location**: struts-config.xml global-exceptions → /error.jsp
- **Pattern**: Global exception mapping to error view
- **Rewrite approach**: @ControllerAdvice + @ExceptionHandler

### Dependency Injection
- **Location**: ServiceLocator, DaoFactory, direct instantiation in services
- **Pattern**: Static factory / service locator
- **Rewrite approach**: Spring @Component/@Service/@Repository + constructor injection
