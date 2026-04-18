## [t1] Set up migration constitution
- Source app is Struts **1.3** (not Struts 2) — the guidelines reference Struts 2, but the actual codebase uses Struts 1.x ActionForm/ActionMapping pattern. Important distinction for downstream agents.
- `api-test.sh` expects the new project at `monolith-new/` relative to the repo root, artifact name `skishop-app-2.0.0.jar`, Spring profile `h2`.
- CSRF field name is `_csrfToken` — hardcoded in the test script's `extract_csrf` function. Spring Security default is `_csrf`, so this needs explicit config.
- The `/coupons/apply` endpoint in the test script differs from the struts-config `/coupon/apply` path — test script uses `/coupons/apply` (plural). Constitution follows the test script as source of truth for URL paths.
- Tiles layout (header/body/footer) must be replicated — simplest approach is JSP includes.
- Source uses commons-dbutils with raw JDBC, not JPA. Constitution mandates JdbcTemplate to keep the migration straightforward.

## [t3] Implementation plan, testing strategy, task breakdown
- 38 tasks across 11 phases. Bottom-up then vertical module decomposition.
- DAO layer has 7 parallelizable tasks (T005-T011), service layer has 6 (T012-T017). This enables significant speedup in phases 3-4.
- Key risk: tasks bundle controller+form+JSP per module. If a single JSP tag conversion is wrong, the whole module fails api-test.sh. Considered splitting JSPs out but vertical bundling keeps context tight for each agent.
- Testing strategy deliberately simple: api-test.sh IS the test suite. No MockMvc/integration test tasks — would be over-engineering given the existing 55-test shell script.
- OrderFacade (T015) is the most complex single task — 18-step workflow with compensating actions. Cannot be split smaller because the compensation logic cross-cuts all sub-steps.
- The `FormValidationUtils.java` from source is a utility used by Struts ValidatorForms. In Spring, Jakarta Validation annotations replace it, but implementers should check if any custom validation logic needs porting.

## [t4] Quality gate — plan quality and traceability
- Both gates (spec-to-plan + plan-to-tasks) PASS with 100% coverage.
- Found 2 LOW issues: (1) t2 architecture oversimplifies PointDao — plan correctly has PointAccountDao + PointTransactionDao; (2) plan includes /orders/detail and /admin/orders/detail URLs not listed in constitution's URL table, but they exist in source Actions and are needed for functional equivalence.
- The self-generated checkpoints from t3 (spec-to-plan.yaml, plan-to-tasks.yaml) were accurate — my independent audit confirmed their claims.
- Constitution compliance verified against all 7 principles + all 5 architectural decisions from decisions.md.

## [t20] Conformance & completeness gate — final sign-off
- All 7 constitution principles verified independently: zero violations.
- api-test.sh independently re-run by teamlead: 65/65 PASS (not relying on tester evidence alone).
- git diff confirmed original src/ untouched — C-III satisfied.
- grep-based constitution compliance: zero Lombok/JPA/Thymeleaf/Swagger, JdbcTemplate used everywhere, _csrfToken in all POST forms, jsp:include for layout.
- Testing strategy was simple (api-test.sh + visual spot-check) — executed exactly as planned, no deviations.
- The test count grew from 55 (original estimate) to 65 (final script) — this was a script evolution, not a deviation.
- All 5 bugs found during reviews (t17 architecture, t18 security, t19 visual) were fixed and re-verified before this gate.
- Traceability: 21 requirements (7 constitution + 14 test sections) all traced through to implementation evidence.
