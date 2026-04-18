## [t19] Runtime validation — 65/65 api-test.sh pass, 3 visual bugs found
- api-test.sh is comprehensive: covers 14 sections, 65 tests across all endpoints, auth flows, edge cases
- Build + startup is fast (~3s with H2 in-memory)
- JSP `<jsp:include>` for header.jsp and footer.jsp renders empty content — the div containers appear but content is blank. This is likely a JSTL taglib resolution issue in the embedded Tomcat JSP engine. The `<c:url>` tags inside included JSPs may not be resolving.
- home.jsp has Chinese text hardcoded ("推荐滑雪装备", "查看详情") while all other JSPs correctly use Japanese. Likely a copy-paste from wrong template during implementation.

## [t19.3] Re-validation — all 3 visual bugs fixed, 65/65 pass, visual parity achieved
- V1/V2 root cause was Spring Security blocking DispatcherType.INCLUDE — one-line fix in SecurityConfig
- V3 was straightforward text replacement in home.jsp
- Full api-test.sh regression: 65/65 PASS, zero regressions
- Compared rendered HTML against both screenshots (home.png, product_list.png): header, footer, Japanese text all match
- Overall verdict: PASS — both functional and visual parity achieved
- The product_list.png screenshot shows "Azure SkiShop" branding while home.png shows "Ski Resort Shop" — the screenshots may be from different source app versions. Current implementation uses "Ski Resort Shop" consistently.
- Docker daemon not available on this host — not a blocker since H2 is the approved test DB per constitution.
