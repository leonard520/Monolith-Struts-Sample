## [t18] Security audit: authentication/authorization, CSRF, Spring Security, session handling
- CSRF token expression inconsistency: admin JSPs use `${_csrfToken}` (interceptor model attr) while all others use `${_csrf.token}` (standard Spring Security). Both work at runtime but `${_csrfToken}` depends on interceptor postHandle firing.
- All DAO queries are properly parameterized — no SQL injection risk.
- `<c:out>` is consistently used for XSS prevention across all JSPs.
- SHA-256 password hashing is intentional (source parity) — not a bug to fix.
- GET /logout is by design per api-test.sh contract — not something to change.
- No blocking security issues found for the migration.
