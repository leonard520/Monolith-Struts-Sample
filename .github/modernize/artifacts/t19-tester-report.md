# Runtime Validation Report

**Generated**: 2026-04-18T06:55:49Z
**Target**: monolith-new/ (Spring Boot 3.2 / Spring MVC rewrite of SkiShop)

## Environment

```
environment:
  docker: UNAVAILABLE — docker daemon not responding (`docker info` failed)
  node: AVAILABLE — v22.19.0
  playwright: NOT ATTEMPTED — api-test.sh is the approved acceptance oracle per constitution; Playwright deferred (see gaps)
  infra-tier: EMBEDDED (H2 in-memory) — per constitution §III and §VI, H2 is the test profile
  browser-tier: MANUAL SPOT-CHECK — visual comparison of live HTML against screenshots
  java: OpenJDK 21.0.5 (Temurin)
  maven: Apache Maven 3.9.10
```

## Step 1: Build & Startup

| Check | Result | Detail |
|-------|--------|--------|
| `mvn -f monolith-new/pom.xml clean package -DskipTests -q` | **PASS** | Build successful, produces `skishop-app-2.0.0.jar` |
| `java -jar monolith-new/target/skishop-app-2.0.0.jar --spring.profiles.active=h2` | **PASS** | Started in ~3s |
| Readiness: `GET /` → HTTP 200 | **PASS** | Responded within 3s of startup |

## Step 2: api-test.sh — Full Test Suite

**Result: 65/65 PASS (0 FAIL)**

| Section | Tests | Status |
|---------|-------|--------|
| 1. Home Page | 2 | ✅ ALL PASS |
| 2. Product Pages | 6 | ✅ ALL PASS |
| 3. Authentication | 5 | ✅ ALL PASS |
| 4. Registration | 5 | ✅ ALL PASS |
| 5. Login with new user | 1 | ✅ ALL PASS |
| 6. Password Reset | 6 | ✅ ALL PASS |
| 7. Cart | 2 | ✅ ALL PASS |
| 8. Coupons | 3 | ✅ ALL PASS |
| 9. Checkout & Orders | 3 | ✅ ALL PASS |
| 10. Protected Pages (Unauthenticated) | 6 | ✅ ALL PASS |
| 11. Authenticated User Flows | 9 | ✅ ALL PASS |
| 12. Order Operations | 2 | ✅ ALL PASS |
| 13. Product Search Variations | 11 | ✅ ALL PASS |
| 14. Edge Cases | 4 | ✅ ALL PASS |
| **TOTAL** | **65** | **65 PASS / 0 FAIL** |

Key flows verified:
- Public browsing: home, products (pagination, search, category filter), product detail
- Authentication: login (valid, invalid, wrong password, validation error), logout
- Registration: form validation (empty, mismatch, duplicate), successful registration
- Login with newly registered user → access protected pages
- Password reset flow (forgot, reset form, invalid token)
- Cart (session-based, no auth required)
- Coupons (list, apply valid/invalid)
- Checkout (form, place order)
- Protected page access control (USER, ADMIN roles redirect to login)
- Order operations (cancel/return unauthenticated → 403)
- Edge cases (404 page, empty ID, huge page number, zero page size)

## Step 3: Visual Spot-Check Against Screenshots

Compared `screenshot/home.png` and `screenshot/product_list.png` against live rendered HTML.

**Findings: 3 BUGS found — see [t19-tester-visual-findings.md](./t19-tester-visual-findings.md)**

| # | Severity | Finding | Location |
|---|----------|---------|----------|
| V1 | **BUG** | Header navigation bar renders empty on all pages | `header.jsp` include produces no visible output |
| V2 | **BUG** | Footer renders empty on all pages | `footer.jsp` include produces no visible output |
| V3 | **BUG** | Chinese text on home page instead of Japanese | `home.jsp` lines 28, 46, 52 |

**Note:** These bugs do NOT cause api-test.sh failures because the test script validates HTTP status codes and specific body keywords, not nav bar or footer content. However, the rendered pages do not match the screenshots.

## Step 4: Verdict

```
startup: PASS — java -jar skishop-app-2.0.0.jar --spring.profiles.active=h2, GET / → 200, 3s
integration: PASS — 65/65 api-test.sh cases pass (full session-based flows with CSRF, auth, registration, CRUD)
e2e: PARTIAL — api-test.sh covers all URL endpoints; visual spot-check found 3 rendering bugs vs screenshots
overall: NEEDS_SIGNOFF — all functional tests pass but visual parity with screenshots is incomplete
```

## Issues Found

| # | Severity | Description | Escalated To |
|---|----------|-------------|--------------|
| V1 | BUG | Header `<jsp:include>` renders empty — no nav bar visible | backend (JSP/include mechanism) |
| V2 | BUG | Footer `<jsp:include>` renders empty — no footer visible | backend (JSP/include mechanism) |
| V3 | BUG | `home.jsp` uses Chinese text ("推荐滑雪装备", "查看详情") instead of Japanese ("おすすめスキー用品", "詳細を見る") per screenshots | frontend (JSP text) |

## Known Gaps

- **Docker unavailable**: Docker daemon not responding. H2 is the approved test DB per constitution — no gap for this project.
- **Playwright not attempted**: The constitution defines `api-test.sh` as the acceptance oracle. Writing additional Playwright E2E tests would provide browser-rendering validation beyond what api-test.sh covers, but the approved testing contract is api-test.sh. The visual spot-check was done via HTML inspection + screenshot comparison instead.
- **Admin authenticated flows**: api-test.sh tests admin endpoints only for access control (unauthenticated → redirect/403). Full admin CRUD with actual admin login is not covered because the seed data password hash doesn't match the hashing algorithm for plain-text login.
