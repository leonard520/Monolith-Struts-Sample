# t11 — Checkout, Orders, Account & Points Controllers + Forms

## Summary
6 files created: CheckoutController, CheckoutForm, OrderController, AddressController, AddressForm, PointController. All faithfully port source Struts Action behavior to Spring MVC `@Controller` with constructor-injected services. URL routing matches the constitution's contract exactly. Build passes (zero errors).

## Deliverables

### Controller Files (4)
| File | URL Routes | Dependencies |
|------|-----------|-------------|
| `web/controller/CheckoutController.java` | GET/POST `/checkout` | OrderFacade, CartService |
| `web/controller/OrderController.java` | GET `/orders`, GET `/orders/detail`, POST `/orders/{id}/cancel`, POST `/orders/{id}/return` | OrderService, OrderFacade |
| `web/controller/AddressController.java` | GET `/account/addresses`, GET `/account/addresses/edit`, POST `/account/addresses/save` | UserAddressDao |
| `web/controller/PointController.java` | GET `/points` | PointService |

### Form Files (2)
| File | Fields | Notes |
|------|--------|-------|
| `web/form/CheckoutForm.java` | cartId, couponCode, paymentMethod, cardNumber, cardExpMonth, cardExpYear, cardCvv, billingZip, usePoints | `toPaymentInfo()` helper preserved from source |
| `web/form/AddressForm.java` | id, label, recipientName, postalCode, prefecture, address1, address2, phone, isDefault | `@NotBlank` on required fields for validation |

## Migration Pattern
- Source: `ServiceLocator.getOrderFacade()` / `new XxxDaoImpl()` → Target: constructor-injected Spring beans
- Source: `mapping.getInputForward()` / `mapping.findForward("success")` → Target: return view name string or `redirect:`
- Source: `ActionMessages` + `saveErrors(request, errors)` → Target: `model.addAttribute("errors", list)` or BindingResult
- Source: `request.getAttribute("loginUser")` → Target: `session.getAttribute("loginUser")` (same session key)
- Source: `request.getParameter("orderId")` → Target: `@PathVariable` for `/orders/{id}/cancel|return`, `@RequestParam` for detail
- CheckoutForm.toPaymentInfo() preserved verbatim from source
- OrderController: ADMIN sees all orders (listAll), USER sees own orders (listByUserId) — matches source OrderHistoryAction
- AddressController: 10-address limit preserved from source AddressSaveAction

## Build Status
`mvn -f monolith-new/pom.xml clean compile -DskipTests -q` — **PASS** (zero errors)
