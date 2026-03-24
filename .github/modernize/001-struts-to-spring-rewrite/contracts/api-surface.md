# API Contracts: SkiShop Spring MVC Endpoints

**Feature**: 001-struts-to-spring-rewrite
**Date**: 2026-03-21
**Format**: HTML web application (JSP-rendered, not REST API)

> These are not REST API contracts. The SkiShop is a server-rendered web application.
> All responses are `text/html` (JSP views) unless noted. Form submissions use `application/x-www-form-urlencoded`.

## Public Endpoints

### GET /home
- **Controller**: HomeController
- **Auth**: None
- **Response**: Home page view (HTTP 200)

### GET /login
- **Controller**: LoginController
- **Auth**: None
- **Response**: Login form view

### POST /login
- **Controller**: LoginController
- **Auth**: None
- **Params**: email, password, _csrfToken
- **Success**: Redirect to /home (set session user)
- **Failure**: Login view with error message

### GET /register
- **Controller**: RegisterController
- **Auth**: None
- **Response**: Registration form view

### POST /register
- **Controller**: RegisterController
- **Auth**: None
- **Params**: email, username, password, confirmPassword, _csrfToken
- **Validation**: Required fields, password match, unique email
- **Success**: Redirect to /login
- **Failure**: Register view with errors

### GET /logout
- **Controller**: LogoutController
- **Auth**: None (invalidates session if present)
- **Response**: Redirect to /home

### GET /products
- **Controller**: ProductController
- **Auth**: None
- **Params**: page (int), size (int), keyword (String), categoryId (String)
- **Response**: Product list view with pagination

### GET /product
- **Controller**: ProductController
- **Auth**: None
- **Params**: id (String, required)
- **Response**: Product detail view, or not-found view if ID invalid

### GET /coupons
- **Controller**: CouponController
- **Auth**: None
- **Response**: Active coupon list view

### GET /cart
- **Controller**: CartController
- **Auth**: None (session-based cart)
- **Response**: Cart view (empty or with items)

### POST /cart
- **Controller**: CartController
- **Auth**: None
- **Params**: productId, quantity, _csrfToken
- **Response**: Redirect to /cart

### GET /password/forgot
- **Controller**: PasswordController
- **Auth**: None
- **Response**: Forgot password form

### POST /password/forgot
- **Controller**: PasswordController
- **Auth**: None
- **Params**: email, _csrfToken
- **Response**: Success message (always, prevents enumeration)

### GET /password/reset
- **Controller**: PasswordController
- **Auth**: None
- **Params**: token (String)
- **Response**: Reset form, or error if invalid token

### POST /password/reset
- **Controller**: PasswordController
- **Auth**: None
- **Params**: token, password, confirmPassword, _csrfToken
- **Response**: Redirect to /login on success, error on invalid token

## Authenticated Endpoints (USER,ADMIN)

### POST /coupons/apply
- **Controller**: CouponController
- **Auth**: USER, ADMIN
- **Params**: couponCode, _csrfToken
- **Response**: Redirect to /cart with discount applied or error

### GET /checkout
- **Controller**: CheckoutController
- **Auth**: USER, ADMIN
- **Response**: Checkout form view

### POST /checkout
- **Controller**: CheckoutController
- **Auth**: USER, ADMIN
- **Params**: cardNumber, cardExpMonth, cardExpYear, cardCvv, billingZip, usePoints, _csrfToken
- **Response**: Order confirmation view, or error

### GET /orders
- **Controller**: OrderController
- **Auth**: USER, ADMIN
- **Response**: Order history list view

### GET /orders/detail
- **Controller**: OrderController
- **Auth**: USER, ADMIN
- **Params**: id (orderId)
- **Response**: Order detail view

### POST /orders/cancel
- **Controller**: OrderController
- **Auth**: USER, ADMIN
- **Params**: id (orderId), _csrfToken
- **Response**: Redirect to /orders

### POST /orders/return
- **Controller**: OrderController
- **Auth**: USER, ADMIN
- **Params**: id (orderId), reason, _csrfToken
- **Response**: Redirect to /orders

### GET /points
- **Controller**: PointController
- **Auth**: USER, ADMIN
- **Response**: Point balance and transaction history view

### GET /account/addresses
- **Controller**: AddressController
- **Auth**: USER, ADMIN
- **Response**: Address list view

### GET /account/addresses/edit
- **Controller**: AddressController
- **Auth**: USER, ADMIN
- **Params**: id (optional — new if absent)
- **Response**: Address edit form view

### POST /account/addresses/save
- **Controller**: AddressController
- **Auth**: USER, ADMIN
- **Params**: label, recipientName, postalCode, prefecture, address1, address2, address3, phoneNumber, _csrfToken
- **Validation**: Required: label, recipientName, postalCode, prefecture, address1
- **Success**: Redirect to /account/addresses
- **Failure**: Edit form with errors

## Admin Endpoints (ADMIN only)

### GET /admin/products
- **Controller**: AdminProductController
- **Auth**: ADMIN
- **Response**: Product management list

### GET /admin/product/edit
- **Controller**: AdminProductController
- **Auth**: ADMIN
- **Params**: id (optional — new if absent)
- **Response**: Product edit form

### POST /admin/product/edit
- **Controller**: AdminProductController
- **Auth**: ADMIN
- **Params**: product form fields, _csrfToken
- **Response**: Redirect to /admin/products

### POST /admin/product/delete
- **Controller**: AdminProductController
- **Auth**: ADMIN
- **Params**: id, _csrfToken
- **Response**: Redirect to /admin/products

### GET /admin/orders
- **Controller**: AdminOrderController
- **Auth**: ADMIN
- **Response**: All orders list

### GET /admin/orders/detail
- **Controller**: AdminOrderController
- **Auth**: ADMIN
- **Params**: id
- **Response**: Order detail view

### POST /admin/order/update
- **Controller**: AdminOrderController
- **Auth**: ADMIN
- **Params**: id, status, _csrfToken
- **Response**: Redirect to /admin/orders

### POST /admin/order/refund
- **Controller**: AdminOrderController
- **Auth**: ADMIN
- **Params**: id, _csrfToken
- **Response**: Redirect to /admin/orders

### GET /admin/coupons
- **Controller**: AdminCouponController
- **Auth**: ADMIN
- **Response**: Coupon management list

### GET /admin/coupon/edit
- **Controller**: AdminCouponController
- **Auth**: ADMIN
- **Params**: id (optional)
- **Response**: Coupon edit form

### POST /admin/coupon/edit
- **Controller**: AdminCouponController
- **Auth**: ADMIN
- **Params**: coupon form fields, _csrfToken
- **Response**: Redirect to /admin/coupons

### GET /admin/shipping
- **Controller**: AdminShippingController
- **Auth**: ADMIN
- **Response**: Shipping method list

### GET /admin/shipping/edit
- **Controller**: AdminShippingController
- **Auth**: ADMIN
- **Params**: id (optional)
- **Response**: Shipping method edit form

### POST /admin/shipping/edit
- **Controller**: AdminShippingController
- **Auth**: ADMIN
- **Params**: shipping method form fields, _csrfToken
- **Response**: Redirect to /admin/shipping
