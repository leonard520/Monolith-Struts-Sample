# t19 — Runtime Validation: Startup, API Tests, Visual Spot-Check

## Summary
**65/65 api-test.sh cases pass.** Application builds, starts in ~3s on H2, and all endpoints return correct HTTP status codes and expected body content. Visual spot-check against screenshots found **3 bugs** (empty header/footer from broken JSP include rendering, Chinese text on home page). No source code was modified.

## Deliverables
- [t19-tester-report.md](./t19-tester-report.md) — Full runtime validation report with environment, test evidence, visual findings, and verdict
- [t19-tester-visual-findings.md](./t19-tester-visual-findings.md) — Detailed visual spot-check findings with expected vs actual
