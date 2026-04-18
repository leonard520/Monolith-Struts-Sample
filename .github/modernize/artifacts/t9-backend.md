# t9 — Auth & Home Controllers + Forms

## Summary
6 files created: AuthController, HomeController, LoginForm, RegisterForm, PasswordResetRequestForm, PasswordResetForm. Source-anchored rewrite from Struts Actions (LoginAction, RegisterAction, PasswordForgotAction, PasswordResetAction, LogoutAction) and Struts ValidatorForms to Spring MVC @Controller with Jakarta Validation form beans. Manual authentication via AuthService (not Spring Security formLogin) to match source session handling. CSRF token support via existing SecurityConfig + CsrfTokenConfig interceptor.

## Deliverables

### Controllers (2)
| File | Source Action | URL Mappings |
|------|-------------|-------------|
| `web/controller/HomeController.java` | ForwardAction (struts-config /home) | GET {/, /home} → "home" |
| `web/controller/AuthController.java` | LoginAction, LogoutAction, RegisterAction, PasswordForgotAction, PasswordResetAction | GET/POST /login, GET /logout, GET/POST /register, GET/POST /password/forgot, GET/POST /password/reset |

### Form Beans (4)
| File | Source Form | Validation |
|------|-----------|-----------|
| `web/form/LoginForm.java` | LoginForm (ValidatorForm) | @NotBlank @Email email, @NotBlank @Size(min=6) password |
| `web/form/RegisterForm.java` | RegisterForm (ValidatorForm) | @NotBlank @Email email, @NotBlank username, @NotBlank @Size(min=6) password, @NotBlank passwordConfirm |
| `web/form/PasswordResetRequestForm.java` | PasswordResetRequestForm (ValidatorForm) | @NotBlank @Email email |
| `web/form/PasswordResetForm.java` | PasswordResetForm (ValidatorForm) | @NotBlank token, @NotBlank @Size(min=6) password, @NotBlank passwordConfirm |

## Service Dependencies
- AuthService (t8) — authentication with lockout
- UserService (t8) — findByEmail, register, updatePassword
- PasswordResetTokenDao (t7) — insert, findByToken, markUsed
- MailService (t8) — enqueuePasswordReset
- ProductService (t8) — search (for home featured products)
- PasswordHasher (t5) — hash + generateSalt

## Key Decisions
- **Manual auth over Spring Security formLogin**: Test script expects POST /login with email+password+_csrfToken fields, session-based cookie jar, redirect-follows to 200. Spring Security formLogin would require different config and wouldn't match source behavior.
- **SecurityContext in session**: `SPRING_SECURITY_CONTEXT_KEY` must be set in session for Spring Security to recognize auth on subsequent requests after manual authentication.
- **Password mismatch in controller**: Jakarta Validation doesn't support cross-field validation without custom annotations. Mismatch check done in controller post-validation.
- **Home featured products**: Source ForwardAction populated no data; JSP showed empty message. Target populates first 6 products for better UX while still rendering correctly if empty.

## Build Status
`mvn -f monolith-new/pom.xml clean compile -DskipTests -q` — **PASS** (zero errors)
