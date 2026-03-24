# API Surface Research

## Endpoint Type
**HTTP/HTML web application** — not a REST API. Returns HTML (JSP-rendered) responses. Session-based authentication with form POST for data submission.

## Endpoint Inventory (from struts-config.xml action-mappings)

### Public Endpoints (no `roles` attribute)

| Struts Path | HTTP Methods | Action Class | Form Bean | View (Tiles) |
|-------------|-------------|--------------|-----------|---------------|
| `/home` | GET | ForwardAction | — | home |
| `/login` | GET, POST | LoginAction | loginForm | auth.login |
| `/register` | GET, POST | RegisterAction | registerForm | auth.register |
| `/password/forgot` | GET, POST | PasswordForgotAction | passwordResetRequestForm | auth.password.forgot |
| `/password/reset` | GET, POST | PasswordResetAction | passwordResetForm | auth.password.reset |
| `/products` | GET | ProductListAction | productSearchForm | products.list |
| `/product` | GET | ProductDetailAction | — | products.detail |
| `/coupons/available` | GET | CouponAvailableAction | — | coupons.available |
| `/cart` | GET, POST | CartAction | addCartForm (session) | cart.view |

### USER/ADMIN Endpoints (`roles="USER,ADMIN"`)

| Struts Path | HTTP Methods | Action Class | Form Bean | View (Tiles) |
|-------------|-------------|--------------|-----------|---------------|
| `/checkout` | GET, POST | CheckoutAction | checkoutForm | cart.checkout / cart.confirmation |
| `/coupon/apply` | POST | CouponApplyAction | couponForm | cart.view |
| `/orders` | GET | OrderHistoryAction | — | orders.history |
| `/orders/detail` | GET | OrderDetailAction | — | orders.detail |
| `/orders/cancel` | POST | OrderCancelAction | — | (redirect) |
| `/orders/return` | POST | OrderReturnAction | — | (redirect) |
| `/points` | GET | PointBalanceAction | — | points.balance |
| `/addresses` | GET | AddressListAction | — | account.addresses |
| `/addresses/save` | POST | AddressSaveAction | addressForm | account.address_edit |
| `/logout` | GET | LogoutAction | — | (redirect to /home) |

### ADMIN-Only Endpoints (`roles="ADMIN"`)

| Struts Path | HTTP Methods | Action Class | Form Bean | View (Tiles) |
|-------------|-------------|--------------|-----------|---------------|
| `/admin/products` | GET | AdminProductListAction | — | admin.products.list |
| `/admin/product/edit` | GET, POST | AdminProductEditAction | adminProductForm | admin.products.edit |
| `/admin/product/delete` | POST | AdminProductDeleteAction | — | (redirect) |
| `/admin/orders` | GET | AdminOrderListAction | — | admin.orders.list |
| `/admin/orders/detail` | GET | AdminOrderDetailAction | — | admin.orders.detail |
| `/admin/order/update` | POST | AdminOrderUpdateAction | — | (redirect) |
| `/admin/order/refund` | POST | AdminOrderRefundAction | — | (redirect) |
| `/admin/coupons` | GET | AdminCouponListAction | — | admin.coupons.list |
| `/admin/coupon/edit` | GET, POST | AdminCouponEditAction | adminCouponForm | admin.coupons.edit |
| `/admin/shipping` | GET | AdminShippingMethodListAction | — | admin.shipping.list |
| `/admin/shipping/edit` | GET, POST | AdminShippingMethodEditAction | adminShippingMethodForm | admin.shipping.edit |

## URL Mapping (Test Script vs Struts)

The `api-test.sh` script uses **clean URLs** that differ from Struts `*.do` paths:

| Test URL | Struts Path | Notes |
|----------|-------------|-------|
| `/home` | `/home.do` | |
| `/login` | `/login.do` | |
| `/register` | `/register.do` | |
| `/products` | `/products.do` | Query params: `page`, `size`, `keyword`, `categoryId` |
| `/product?id=PSK001` | `/product.do?id=PSK001` | |
| `/coupons` | `/coupons/available.do` | **Path difference** — test uses `/coupons` |
| `/coupons/apply` | `/coupon/apply.do` | **Path difference** — test uses `/coupons/apply` |
| `/cart` | `/cart.do` | |
| `/checkout` | `/checkout.do` | |
| `/orders` | `/orders.do` | |
| `/points` | `/points.do` | |
| `/account/addresses` | `/addresses.do` | **Path difference** — test uses `/account/addresses` |
| `/account/addresses/edit` | (no direct mapping) | Test expects this path for add-address form |
| `/account/addresses/save` | `/addresses/save.do` | **Path difference** — test uses `/account/addresses/save` |
| `/password/forgot` | `/password/forgot.do` | |
| `/password/reset` | `/password/reset.do` | |
| `/logout` | `/logout.do` | |
| `/admin/products` | `/admin/products.do` | |
| `/admin/orders` | `/admin/orders.do` | |
| `/admin/coupons` | `/admin/coupons.do` | |
| `/admin/shipping` | `/admin/shipping.do` | |

## Authentication/Authorization

- **Mechanism**: Session-based (no REST tokens); `AuthRequestProcessor` checks `roles` attribute from struts-config.xml action-mapping
- **CSRF protection**: Custom token-based — `_csrfToken` hidden field in forms, validated by `AuthRequestProcessor`
- **Login flow**: POST `/login` with email + password → session creation → redirect to home
- **Unauthenticated access to protected pages**: Redirect to `/login`
- **Admin access by non-admin**: Redirect to login or 403

## Request/Response Format

- **Content-Type**: `text/html; charset=UTF-8`
- **Form submission**: `application/x-www-form-urlencoded` (standard HTML form POST)
- **No JSON APIs** — all responses are HTML (JSP-rendered via Tiles layout)

## Query Parameters (Product Search)

| Parameter | Type | Purpose |
|-----------|------|---------|
| `page` | int | Pagination page number |
| `size` | int | Items per page |
| `keyword` | string | Search term |
| `categoryId` | string | Filter by category ID (e.g., `c-1`, `c-boots`) |
| `id` | string | Product ID for detail view |
