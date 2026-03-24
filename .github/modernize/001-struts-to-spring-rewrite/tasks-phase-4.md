## Phase 4: P2 Features — Orders, Addresses, Points, Password Reset, Email

**Batch**: 4
**Goal**: Implement all Priority 2 features: order history/detail/cancel/return, address management, point balance, password reset flow, and Spring mail service conversion.
**User Stories**: US-4 (Order Management), US-5 (Address Management), US-6 (Password Reset), US-7 (Points/Loyalty)
**Task ID Range**: T400–T442

---

### 4.1 OrderController

- [ ] T400 [US-4] [Plan:4.1] Create `OrderController` `@Controller` class in `src/main/java/com/skishop/web/controller/OrderController.java`. Inject `OrderService` and `OrderFacade` via constructor injection. All handler methods must require authenticated user (USER or ADMIN role) — redirect to `/login` if session `loginUser` is null.
- [ ] T401 [US-4] [Plan:4.1] Implement `GET /orders` handler in `OrderController`. Retrieve `loginUser` from session. If user role is ADMIN, call `orderService.listAll(50)`; otherwise call `orderService.listByUserId(user.getId())`. Set `orders` model attribute. Return view name `orders/history`. Replicate `OrderHistoryAction` behavior exactly.
- [ ] T402 [US-4] [Plan:4.1] Implement `GET /orders/detail` handler in `OrderController`. Accept `@RequestParam String id`. Validate: if `id` is null/empty, return view `orders/detail` with no data. Call `orderService.findById(id)`. Verify ownership: `user.getId().equals(order.getUserId())` (skip check for ADMIN). If valid, call `orderService.listItems(id)` and set model attributes `order` and `orderItems`. Return view name `orders/detail`. Replicate `OrderDetailAction` behavior, noting the Struts action uses param name `orderId` — the Spring contract uses `id`.
- [ ] T403 [US-4] [Plan:4.1] Implement `POST /orders/cancel` handler in `OrderController`. Accept `@RequestParam String id`. Validate `id` not null/empty — on failure, add error message to redirect attributes and redirect to `/orders`. Call `orderFacade.cancelOrder(id, user.getId())`. On success, redirect to `/orders`. On `RuntimeException`, add flash error message and redirect to `/orders`. Replicate `OrderCancelAction` behavior.
- [ ] T404 [US-4] [Plan:4.1] Implement `POST /orders/return` handler in `OrderController`. Accept `@RequestParam String id` and `@RequestParam(required=false) String reason`. Validate `id` not null/empty. Call `orderFacade.returnOrder(id, user.getId())`. On success, redirect to `/orders`. On `RuntimeException`, add flash error and redirect to `/orders`. Replicate `OrderReturnAction` behavior.
- [ ] T405 [P] [US-4] [Plan:4.1] Create unit test `src/test/java/com/skishop/web/controller/OrderControllerTest.java`. Test scenarios: (a) `GET /orders` unauthenticated → redirect to login; (b) `GET /orders` as USER → 200 with order history for that user; (c) `GET /orders` as ADMIN → 200 with all orders; (d) `GET /orders/detail?id=ORD-1` as owner → 200 with order + items; (e) `GET /orders/detail?id=ORD-1` as non-owner → 200 with no order data; (f) `POST /orders/cancel` with valid id → redirect to `/orders`; (g) `POST /orders/cancel` with empty id → redirect with error; (h) `POST /orders/return` with valid id → redirect to `/orders`.

---

### 4.2 AddressController

- [ ] T406 [US-5] [Plan:4.2] Create `AddressController` `@Controller` class in `src/main/java/com/skishop/web/controller/AddressController.java`. Inject `AddressService` via constructor injection. All handlers require authenticated user.
- [ ] T407 [US-5] [Plan:4.2] Implement `GET /account/addresses` handler in `AddressController`. Retrieve user from session. Call `addressService.listByUserId(user.getId())`. Set `addresses` model attribute. Return view name `account/addresses`.
- [ ] T408 [US-5] [Plan:4.2] Implement `GET /account/addresses/edit` handler in `AddressController`. Accept `@RequestParam(required=false) String id`. If `id` is present, load existing address via `addressService.findById(id)` and verify ownership. Set `addressForm` model attribute (populated for edit, empty for new). Return view name `account/address_edit`.
- [ ] T409 [US-5] [Plan:4.2] Implement `POST /account/addresses/save` handler in `AddressController`. Accept `@Valid @ModelAttribute AddressForm`, `BindingResult`, `HttpSession`, `RedirectAttributes`. On validation errors, return `account/address_edit`. Check address count limit (MAX 10 per user) — if exceeded, add error and return `account/address_edit`. Build `Address` domain object from form, set `userId` from session user, generate UUID if new. Call `addressService.save(address)`. Redirect to `/account/addresses`. Replicate `AddressSaveAction` behavior including the 10-address limit check.
- [ ] T410 [P] [US-5] [Plan:4.2] Create unit test `src/test/java/com/skishop/web/controller/AddressControllerTest.java`. Test scenarios: (a) `GET /account/addresses` unauthenticated → redirect to login; (b) `GET /account/addresses` authenticated → 200 with address list; (c) `GET /account/addresses/edit` (new) → 200 with empty form; (d) `GET /account/addresses/edit?id=addr-1` → 200 with populated form; (e) `POST /account/addresses/save` valid data → redirect to addresses; (f) `POST /account/addresses/save` missing required fields → 200 with validation errors; (g) `POST /account/addresses/save` when at limit (10) → error.

---

### 4.3 AddressService (NEW)

- [ ] T411 [US-5] [Plan:4.3] Create `AddressService` `@Service` class in `src/main/java/com/skishop/service/address/AddressService.java`. Inject `UserAddressDao` via constructor. Provide methods: `listByUserId(String userId)` → `List<Address>`, `findById(String id)` → `Address`, `save(Address address)` → void, `countByUserId(String userId)` → int. This fixes the `AddressListAction` direct-DAO-access layering violation by routing all address operations through a proper service layer.
- [ ] T412 [P] [US-5] [Plan:4.3] Create unit test `src/test/java/com/skishop/service/address/AddressServiceTest.java`. Mock `UserAddressDao`. Test: (a) `listByUserId` delegates to DAO; (b) `findById` delegates to DAO; (c) `save` delegates to DAO; (d) `countByUserId` returns correct count.

---

### 4.4 PointController

- [ ] T413 [US-7] [Plan:4.4] Create `PointController` `@Controller` class in `src/main/java/com/skishop/web/controller/PointController.java`. Inject `PointService` via constructor injection. Requires authenticated user.
- [ ] T414 [US-7] [Plan:4.4] Implement `GET /points` handler in `PointController`. Retrieve user from session. Call `pointService.getAccount(user.getId())`. Set `pointBalance` model attribute. Return view name `points/balance`. Replicate `PointBalanceAction` behavior exactly.
- [ ] T415 [P] [US-7] [Plan:4.4] Create unit test `src/test/java/com/skishop/web/controller/PointControllerTest.java`. Test scenarios: (a) `GET /points` unauthenticated → redirect to login; (b) `GET /points` authenticated → 200 with point balance attribute.

---

### 4.5 PasswordController

- [ ] T416 [US-6] [Plan:4.5] Create `PasswordController` `@Controller` class in `src/main/java/com/skishop/web/controller/PasswordController.java`. Inject `UserService`, `PasswordResetTokenDao`, and `MailService` via constructor injection. No authentication required for any endpoint.
- [ ] T417 [US-6] [Plan:4.5] Implement `GET /password/forgot` handler in `PasswordController`. Return view name `auth/password/forgot`. No model attributes needed.
- [ ] T418 [US-6] [Plan:4.5] Implement `POST /password/forgot` handler in `PasswordController`. Accept `@ModelAttribute PasswordResetRequestForm`. Look up user by email via `userService.findByEmail(form.getEmail())`. If user exists: generate UUID token, create `PasswordResetToken` with 1-hour expiry, save via `tokenDao.insert(token)`, call `mailService.enqueuePasswordReset(email, token)`. Always return view `auth/password/forgot_complete` regardless of whether user was found (prevents user enumeration). Replicate `PasswordForgotAction` behavior exactly.
- [ ] T419 [US-6] [Plan:4.5] Implement `GET /password/reset` handler in `PasswordController`. Accept `@RequestParam String token`. Validate token: look up via `tokenDao.findByToken(token)`, check not null, not used (`usedAt == null`), not expired. If invalid, add error message and return `auth/password/reset` with error flag. If valid, set `token` model attribute and return view `auth/password/reset`.
- [ ] T420 [US-6] [Plan:4.5] Implement `POST /password/reset` handler in `PasswordController`. Accept `@Valid @ModelAttribute PasswordResetForm`, `BindingResult`, `Model`. Validate token (same checks as GET). If invalid, add error and return `auth/password/reset`. Validate password == confirmPassword. Look up user by token's userId. Generate new salt via `PasswordHasher.generateSalt()`, hash new password via `PasswordHasher.hash()`. Update via `userService.updatePassword(userId, hash, salt)`. Mark token used via `tokenDao.markUsed(tokenId)`. Redirect to `/login`. Replicate `PasswordResetAction` behavior exactly.
- [ ] T421 [P] [US-6] [Plan:4.5] Create unit test `src/test/java/com/skishop/web/controller/PasswordControllerTest.java`. Test scenarios: (a) `GET /password/forgot` → 200 with forgot form; (b) `POST /password/forgot` with existing email → 200 success page, token created; (c) `POST /password/forgot` with non-existing email → 200 success page (prevents enumeration); (d) `GET /password/reset?token=valid` → 200 with reset form; (e) `GET /password/reset?token=expired` → 200 with error; (f) `POST /password/reset` valid token + matching passwords → redirect to `/login`; (g) `POST /password/reset` invalid token → error; (h) `POST /password/reset` password mismatch → validation error.

---

### 4.6 MailService Spring Conversion

- [ ] T422 [Plan:4.6] Create new `MailService` `@Service` class in `src/main/java/com/skishop/service/mail/MailService.java` (replaces the existing Struts-era class). Inject Spring `JavaMailSender` and `EmailQueueDao` via constructor. Replace direct `javax.mail` Session/Transport usage with `JavaMailSender.send(MimeMessage)`.
- [ ] T423 [Plan:4.6] Implement `enqueuePasswordReset(String email, String token)` method. Load password reset email template from `mail/password_reset.txt`. Replace `{{token}}` placeholder. Create `EmailQueue` record with status `PENDING`, persist via `emailQueueDao.insert()`. Preserve exact behavior of existing `MailService.enqueuePasswordReset()`.
- [ ] T424 [Plan:4.6] Implement `enqueueOrderConfirmation(String email, Order order)` method. Load order confirmation template from `mail/order_confirmation.txt`. Replace placeholders (`{{orderNumber}}`, `{{totalAmount}}`, etc.). Create `EmailQueue` record with status `PENDING`, persist. Preserve existing behavior.
- [ ] T425 [Plan:4.6] Implement `processQueue()` method. Query pending emails from `emailQueueDao`. For each: construct `MimeMessage` via `JavaMailSender.createMimeMessage()`, set to/subject/body, call `JavaMailSender.send()`. On success, update status to `SENT`. On failure, increment retry count; mark `FAILED` after MAX_RETRY (3). Preserve the retry and max-retry semantics of the original.
- [ ] T426 [Plan:4.6] Configure `spring.mail.*` properties in `application.properties` (or `application.yml`). Set `spring.mail.host`, `spring.mail.port`, `spring.mail.username`, `spring.mail.password`, `spring.mail.properties.mail.smtp.auth=true`, `spring.mail.properties.mail.smtp.starttls.enable=true`. Values should reference environment variables or defaults matching existing `mail.properties` config.
- [ ] T427 [P] [Plan:4.6] Create unit test `src/test/java/com/skishop/service/mail/MailServiceTest.java`. Mock `JavaMailSender` and `EmailQueueDao`. Test: (a) `enqueuePasswordReset` creates EmailQueue record with correct template content; (b) `enqueueOrderConfirmation` creates EmailQueue record; (c) `processQueue` sends pending emails and updates status; (d) `processQueue` marks as FAILED after max retries.

---

### 4.7 Form Objects (P2)

- [ ] T428 [Plan:4.7] Create `AddressForm` POJO in `src/main/java/com/skishop/web/form/AddressForm.java` (Spring version replacing Struts `ValidatorForm`). Fields: `id`, `label` (`@NotBlank`), `recipientName` (`@NotBlank`), `postalCode` (`@NotBlank`), `prefecture` (`@NotBlank`), `address1` (`@NotBlank`), `address2`, `phone`, `isDefault`. Use Bean Validation (`jakarta.validation.constraints` or `javax.validation.constraints` per Spring Boot version). Include getters/setters.
- [ ] T429 [Plan:4.7] Create `PasswordResetRequestForm` POJO in `src/main/java/com/skishop/web/form/PasswordResetRequestForm.java` (Spring version). Fields: `email` (`@NotBlank @Email`). Include getter/setter.
- [ ] T430 [Plan:4.7] Create `PasswordResetForm` POJO in `src/main/java/com/skishop/web/form/PasswordResetForm.java` (Spring version). Fields: `token` (`@NotBlank`), `password` (`@NotBlank`), `passwordConfirm` (`@NotBlank`). Include getter/setter. Password match validation should be handled in controller (matching existing `FormValidationUtils.addPasswordMismatch` pattern).
- [ ] T431 [P] [Plan:4.7] Create unit test `src/test/java/com/skishop/web/form/P2FormValidationTest.java`. Use `jakarta.validation.Validator` to test: (a) `AddressForm` with all required fields → no violations; (b) `AddressForm` with blank `label`/`recipientName`/`postalCode`/`prefecture`/`address1` → violations; (c) `PasswordResetRequestForm` with valid email → no violations; (d) `PasswordResetRequestForm` with blank/invalid email → violations; (e) `PasswordResetForm` with blank token or password → violations.

---

### 4.8 JSP Views (P2)

- [ ] T432 [US-4] [Plan:4.8] Migrate `orders/history.jsp` view to `src/main/webapp/WEB-INF/jsp/orders/history.jsp`. Convert Struts tags (`<bean:write>`, `<logic:iterate>`, `<html:link>`) to JSTL/EL equivalents (`${order.orderNumber}`, `<c:forEach>`, `<a href>`). Preserve layout, CSS classes, and all displayed order fields. Include links to order detail, cancel, return actions.
- [ ] T433 [US-4] [Plan:4.8] Migrate `orders/detail.jsp` view to `src/main/webapp/WEB-INF/jsp/orders/detail.jsp`. Display order header (order number, date, status, totals) and line items table. Convert Struts tags to JSTL/EL. Preserve all fields from original JSP.
- [ ] T434 [US-5] [Plan:4.8] Migrate `account/addresses.jsp` view to `src/main/webapp/WEB-INF/jsp/account/addresses.jsp`. Display address list with edit links and "Add New" button. Convert Struts tags to JSTL/EL.
- [ ] T435 [US-5] [Plan:4.8] Migrate `account/address_edit.jsp` view to `src/main/webapp/WEB-INF/jsp/account/address_edit.jsp`. Build Spring-compatible form with fields: label, recipientName, postalCode, prefecture, address1, address2, phone, isDefault. Display Bean Validation error messages via `<form:errors>` or `${errors}`. Include hidden `id` field for edits. Use `<form>` with `action="/account/addresses/save"` and CSRF token.
- [ ] T436 [US-7] [Plan:4.8] Migrate `points/balance.jsp` view to `src/main/webapp/WEB-INF/jsp/points/balance.jsp`. Display point balance (from `pointBalance` model attribute) and transaction history if available. Convert Struts tags to JSTL/EL.
- [ ] T437 [US-6] [Plan:4.8] Migrate `auth/password/forgot.jsp` view to `src/main/webapp/WEB-INF/jsp/auth/password/forgot.jsp`. Simple form with email input field and submit button. POST to `/password/forgot`. Include CSRF token.
- [ ] T438 [US-6] [Plan:4.8] Migrate `auth/password/forgot_complete.jsp` view to `src/main/webapp/WEB-INF/jsp/auth/password/forgot_complete.jsp`. Display success message ("If the email exists, a reset link has been sent."). No conditional logic — always shows success to prevent enumeration.
- [ ] T439 [US-6] [Plan:4.8] Migrate `auth/password/reset.jsp` view to `src/main/webapp/WEB-INF/jsp/auth/password/reset.jsp`. Form with hidden `token` field, `password` and `confirmPassword` inputs, submit button. POST to `/password/reset`. Display error messages if token is invalid/expired. Include CSRF token.

---

### 4.9 Compile + Test Gate

- [ ] T440 [Plan:4.9] Run `mvn -B compile` and verify all Phase 4 source files compile without errors. Fix any compilation issues in controllers, services, forms, or views.
- [ ] T441 [Plan:4.9] Run all Phase 4 unit tests (`OrderControllerTest`, `AddressControllerTest`, `AddressServiceTest`, `PointControllerTest`, `PasswordControllerTest`, `MailServiceTest`, `P2FormValidationTest`). All tests must pass.
- [ ] T442 [Plan:4.9] Smoke-test P2 endpoints manually or via integration test: verify `GET /orders` (auth required), `GET /orders/detail?id=...`, `POST /orders/cancel`, `POST /orders/return`, `GET /account/addresses`, `GET /account/addresses/edit`, `POST /account/addresses/save`, `GET /points`, `GET /password/forgot`, `POST /password/forgot`, `GET /password/reset?token=...`, `POST /password/reset` all respond correctly.

---
