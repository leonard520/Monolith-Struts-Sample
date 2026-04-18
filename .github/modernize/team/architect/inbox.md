## Inbox — architect

### [teamlead] t1 — 2026-04-18T03:36Z (broadcast)
Constitution v1.0.0 ratified for SkiShop Monolith migration (Struts 1.3 → Spring Boot 3.2.x). Key rules:
- **Mode**: REWRITE into `monolith-new/` directory (do NOT modify `src/`)
- **JSPs kept**: migrate Struts tags → JSTL + Spring form tags, replace Tiles with JSP includes
- **Exact URL parity**: `api-test.sh` defines the URL contract
- **CSRF field**: `_csrfToken` (not Spring's default `_csrf`)
- **No JPA**: use `JdbcTemplate`
- **Final artifact**: `skishop-app-2.0.0.jar`, runnable with `--spring.profiles.active=h2`
Full constitution: `.github/modernize/artifacts/t1-teamlead-constitution.md`
