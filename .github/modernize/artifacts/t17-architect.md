# t17 — Architecture Review: Implementation vs Target Architecture

## Summary
Comprehensive architecture review of the Spring MVC rewrite against the t2 target architecture. Verified all layers: controllers (14), services (15), DAOs (18), domain POJOs (24+), configuration (5 classes), JSPs (32), and security. **64/65 API tests pass.** One bug found (addresses.jsp), plus advisory findings on transaction management and naming patterns.

## Deliverables
- [t17-architect-review.md](./t17-architect-review.md) — Full architecture conformance report with per-layer verdicts
- [t17-architect-findings.md](./t17-architect-findings.md) — Detailed findings table with severity, location, and recommended fix
