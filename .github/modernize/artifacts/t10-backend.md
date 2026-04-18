# t10 — Product, Cart & Coupon Controllers + Forms

## Summary
6 files created: ProductController (pagination/search/filter with LabelValue helper), CartController (session+cookie CART_ID), CouponController (list + apply to cart), ProductSearchForm, AddCartForm, CouponForm. All source Struts Action behavior ported faithfully — same URL paths, same session/cookie management, same model attributes.

## Deliverables

### Controllers (3)
| File | Endpoints | Dependencies |
|------|-----------|-------------|
| `web/controller/ProductController.java` | `GET /products` (pagination, search, category filter), `GET /product` (detail by id) | ProductService, CategoryService |
| `web/controller/CartController.java` | `GET /cart` (view), `POST /cart` (add item, redirect) | CartService |
| `web/controller/CouponController.java` | `GET /coupons` (list active), `POST /coupons/apply` (validate + discount calc) | CouponService, CartService |

### Forms (3)
| File | Fields | Notes |
|------|--------|-------|
| `web/form/ProductSearchForm.java` | keyword, categoryId, page(=1), size(=10), sort | Plain POJO, no validation annotations needed |
| `web/form/AddCartForm.java` | productId, quantity(=1) | Plain POJO |
| `web/form/CouponForm.java` | code | Plain POJO |

## Key Design Decisions
- **ProductController.LabelValue**: Static inner class replaces Struts `LabelValueBean` for category dropdown options — keeps it simple without adding a separate file
- **CartController**: Uses `Set-Cookie` header directly (matching source pattern) for CART_ID cookie with 30-day expiry, HttpOnly, path-scoped
- **CouponController.apply**: Returns `cart/view` (not redirect) — matches source which renders cart view inline with coupon/discount attributes; handles missing cart gracefully
- **No @Valid on forms**: ProductSearchForm and AddCartForm don't need validation annotations; CouponForm code is validated by CouponService

## Build Status
`mvn -f monolith-new/pom.xml clean compile -DskipTests -q` — **PASS** (zero errors)
