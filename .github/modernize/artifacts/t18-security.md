# t18 — Security Audit

## Summary
Audit of the `monolith-new/` Spring Boot 3.2 / Spring MVC rewrite covering authentication/authorization flows, CSRF `_csrfToken`, Spring Security config, session handling, default-deny enforcement, XSS/SQLi patterns, and dependency CVEs.

**Overall verdict: PASS with LOW-severity findings.**

5 findings total: 1 MEDIUM, 3 LOW, 1 INFORMATIONAL.

## Deliverables
- [t18-security-findings.md](./t18-security-findings.md) — Full findings with severity, evidence, and remediation owner
