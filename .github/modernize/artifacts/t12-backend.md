# t12 — Admin Controllers + Forms

## Summary
7 files created: 4 admin Spring MVC `@Controller` classes and 3 admin form POJOs. All faithfully port source Struts Action behavior to Spring MVC with constructor-injected services/DAOs. URL routing matches the architect's contract exactly (`/admin/products`, `/admin/orders`, `/admin/coupons`, `/admin/shipping`). Build passes (zero errors).

## Deliverables

### Admin Controllers (4)
| File | Source Action(s) | Endpoints |
|------|-----------------|-----------|
| `web/controller/admin/AdminProductController.java` | AdminProductListAction, AdminProductEditAction, AdminProductDeleteAction | GET `/admin/products`, GET/POST `/admin/product/edit`, POST `/admin/product/delete` |
| `web/controller/admin/AdminOrderController.java` | AdminOrderListAction, AdminOrderDetailAction, AdminOrderUpdateAction, AdminOrderRefundAction | GET `/admin/orders`, GET `/admin/orders/detail`, POST `/admin/order/update`, POST `/admin/order/refund` |
| `web/controller/admin/AdminCouponController.java` | AdminCouponListAction, AdminCouponEditAction | GET `/admin/coupons`, GET/POST `/admin/coupon/edit` |
| `web/controller/admin/AdminShippingController.java` | AdminShippingMethodListAction, AdminShippingMethodEditAction | GET `/admin/shipping`, GET/POST `/admin/shipping/edit` |

### Admin Forms (3)
| File | Source Form | Fields |
|------|------------|--------|
| `web/form/admin/AdminProductForm.java` | AdminProductForm (Struts ValidatorForm) | id, name, brand, description, categoryId, price, status, inventoryQty |
| `web/form/admin/AdminCouponForm.java` | AdminCouponForm (Struts ValidatorForm) | id, campaignId, code, couponType, discountValue, discountType, minimumAmount, maximumDiscount, usageLimit, active, expiresAt |
| `web/form/admin/AdminShippingMethodForm.java` | AdminShippingMethodForm (Struts ValidatorForm) | id, code, name, fee, active, sortOrder |

## Migration Patterns Applied
- Source `new XxxDaoImpl()` → constructor-injected DAO interface via Spring DI
- Source `new OrderFacadeImpl()` → constructor-injected `OrderFacade` interface
- Source `request.setAttribute("xxx", ...)` → `model.addAttribute("xxx", ...)`
- Source `mapping.getInputForward()` / `mapping.findForward("success")` → return view name string / `redirect:`
- Source `ActionMessages` error handling → `model.addAttribute("error", ...)` + re-render form view
- Source `!request.getMethod().equals("POST")` for GET/POST dispatch → separate `@GetMapping` / `@PostMapping` methods
- All business logic (validation, BigDecimal parsing, date formatting, UUID generation) preserved identically from source

## Build Status
`mvn -f monolith-new/pom.xml clean compile -DskipTests -q` — **PASS** (zero errors)
