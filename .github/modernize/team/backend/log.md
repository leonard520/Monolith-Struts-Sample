## [t7] Domain & DAO layer — 24 POJOs + 19 DAO interface+impl pairs
- Domain POJOs copied verbatim from source. No Lombok, no JPA annotations.
- DAO migration pattern: AbstractDao + manual JDBC → @Repository + JdbcTemplate constructor injection + RowMapper lambdas.
- SQL statements preserved identically. Column-to-field mappings verified against domain POJOs.
- Gotcha: source EmailQueue uses field name `toAddr` (not `toAddress`) and PointTransaction uses `referenceId` (not `orderId`). DAO column names may differ from field names.
- Coupon `saveOrUpdate` uses find-then-insert/update pattern (matches source behavior, no MERGE/UPSERT).
- Inventory `reserve` uses atomic UPDATE with WHERE quantity check — returns boolean for success.
- PointAccount `increment` uses conditional SQL: positive amounts update `lifetime_earned`, negative update `lifetime_redeemed`.
- MailEvent.java from architect spec doesn't exist in source — skipped per constitution.

## [t5] Project scaffold — pom.xml, Application.java, configs, PasswordHasher, SQL, messages
- PasswordHasher algorithm: SHA-256, salt prepended to input (not appended), 1000 iterations. Each iteration: digest → reset → prepend salt bytes to result. Important to preserve exactly.
- H2 `MODE=PostgreSQL` is required for BOOLEAN column type compatibility.
- `tomcat-embed-jasper` must be compile scope (not provided) for executable JAR to serve JSPs.
- CSRF parameter name `_csrfToken` must match in SecurityConfig, CsrfTokenConfig interceptor, and all JSP forms.
- Spring Security `formLogin().disable()` is required because auth is handled manually in AuthController.
- `spring.sql.init.mode=always` loads schema+data on every H2 startup; PostgreSQL profile sets `never`.

## [t8] Service layer — 18 service files
- OrderFacadeImpl compensating-action pattern preserved verbatim (no @Transactional).
- InventoryService, PointService, ProductService keep manual JDBC TX via JdbcTemplate.getDataSource().getConnection().
- MailService uses @Scheduled(fixedDelay=30000) replacing Timer-based queue.
- All business logic, validation rules, calculation algorithms preserved identically from source.

## [t9] Auth & Home controllers + forms
- HomeController: GET {/, /home} → renders "home" view with `featuredProducts` from ProductService.search(null,null,null,0,6). Source used ForwardAction with no data population, but JSP expects `featuredProducts` — we provide it for a better UX.
- AuthController.login: manual AuthService.authenticate() + session invalidation (fixation prevention) + Spring Security context set via UsernamePasswordAuthenticationToken. MUST store SecurityContext in session attribute `SPRING_SECURITY_CONTEXT` for Spring Security to persist auth across requests.
- AuthController.logout: invalidates session + clears SecurityContext. Unauthenticated requests to /logout get redirected to /login by Spring Security entry point → curl -L follows → 200.
- AuthController.register: validates form, checks duplicate email, hashes password with PasswordHasher, calls UserService.register(), redirects to /login on success.
- Password forgot: always returns success page (no user enumeration). Creates PasswordResetToken + enqueues email.
- Password reset: validates token (not null, not used, not expired), hashes new password, updates via UserService.updatePassword(), marks token used.
- Forms use Jakarta Validation annotations (@NotBlank, @Email, @Size(min=6)) matching source Struts validator rules.
- Password mismatch check is done in controller (not form) since Jakarta Validation doesn't support cross-field validation without custom annotations.

## [t11] Checkout, Orders, Account & Points controllers + forms
- CheckoutController: GET shows form (populates cart model for display), POST calls OrderFacade.placeOrder. On failure, catches RuntimeException and re-renders checkout with error — matching source CheckoutAction exactly.
- CheckoutForm.toPaymentInfo() helper preserved verbatim from source form.
- OrderController.history: ADMIN sees listAll(50), USER sees listByUserId — matching source OrderHistoryAction role check.
- OrderController.detail: validates user owns the order before showing items — matching source OrderDetailAction.
- OrderController.cancel/return: use @PathVariable for `/orders/{id}/cancel|return` path variables. Silently redirect on error (source saved ActionMessages but test script only checks HTTP status).
- AddressController: uses UserAddressDao directly (no service layer, matching source pattern). 10-address limit preserved.
- AddressForm uses @NotBlank on recipientName, postalCode, prefecture, address1 for server-side validation.
- PointController: simple pass-through to PointService.getAccount(), which handles expiration + account creation internally.
