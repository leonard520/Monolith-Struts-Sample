# t3 — Implementation Plan, Testing Strategy, and Task Breakdown

## Summary
38 tasks across 11 phases. Brownfield rewrite of SkiShop from Struts 1.3 to Spring Boot 3.2. Bottom-up layered build (scaffold → domain → DAO → service) then vertical user story modules (auth, catalog, cart, checkout, account, admin). Testing via api-test.sh (55 HTTP tests). Tasks split by vertical business module per charter.

## Deliverables
- [t3-teamlead-plan.md](./t3-teamlead-plan.md) — full implementation plan with testing strategy, task breakdown, and requirement mapping
- [checkpoints/spec-to-plan.yaml](./checkpoints/spec-to-plan.yaml) — REQ → plan item traceability (22 requirements, 100% coverage)
- [checkpoints/plan-to-tasks.yaml](./checkpoints/plan-to-tasks.yaml) — plan item → task traceability (30 items → 38 tasks, 100% coverage)

## Key Decisions
- **Task granularity**: Foundational layers (phases 1-4) split by domain/concern. User stories (phases 5-10) split by vertical module with controller+form+JSP bundled per task.
- **Parallelism**: DAO tasks (T005-T011) are all [P] parallelizable. Service tasks (T012-T017) are all [P] parallelizable. Phase 5-10 tasks have implicit dependencies on their layer predecessors.
- **Testing strategy**: api-test.sh is the sole acceptance oracle. No unit tests mandated (implementer's discretion). No browser-tier testing needed (curl-based tests).
- **Phase 11**: Single integration task (T038) for iterative fix cycles against api-test.sh + visual verification.
