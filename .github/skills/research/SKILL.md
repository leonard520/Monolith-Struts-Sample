# Research Skill

Provides a pool of research sub-agents for codebase analysis during the Design phase.
The DesignAgent uses this skill to dynamically select which research agents to dispatch based on project characteristics and user requirements.

Each agent's full prompt is in `$KIT_ROOT/skills/research/references/<agent-name>.md`. Load only the files for agents you select.

## Usage

1. **Always run Core agents first** (Phase 1a — project-structure + tech-stack)
2. Read Phase 1a results
3. Based on findings + user prompt, select relevant agents from the Optional Pool below
4. Load each selected agent's reference file, then dispatch in parallel

---

## Core Agents (Always Run)

| Agent | Reference | Output |
|-------|-----------|--------|
| `project-structure` | `references/project-structure.md` | Functional domains, layers, project type |
| `tech-stack` | `references/tech-stack.md` | Frameworks, deps, migration blockers |

---

## Optional Agent Pool

Select based on Phase 1a results and user prompt. Load reference file only for selected agents.

| Agent | When Relevant | Reference | Output |
|-------|---------------|-----------|--------|
| `data-model` | Project has ORM / entity classes / DB access | `references/data-model.md` | Entity inventory, schema |
| `migration-risks` | Any migration/upgrade task | `references/migration-risks.md` | Risk by module, patterns |
| `architecture-summary` | Knowledge graph exists, or complex inter-module deps | `references/architecture-summary.md` | Arch patterns, coupling |
| `api-surface` | Project exposes REST/GraphQL/gRPC endpoints | `references/api-surface.md` | Endpoint inventory, DTOs |
| `integration-points` | Project calls external services, MQ, cache, 3rd-party APIs | `references/integration-points.md` | External deps, service boundaries |
| `infrastructure` | **Always for migration/upgrade** — existing resource deps must be understood before planning | `references/infrastructure.md` | DB/MQ/cache deps, test infra |
| `test-coverage` | **Always for migration/upgrade** — existing test suite portability affects planning | `references/test-coverage.md` | Test inventory, portability, gaps |
| `deployment` | Project has Dockerfile/K8s/CI configs | `references/deployment.md` | Container, CI/CD, IaC |
| `ui-components` | Frontend with component-based framework (React/Vue/Angular) | `references/ui-components.md` | Component tree, design system |
| `state-routing` | Frontend SPA with state management / client-side routing | `references/state-routing.md` | State, routes, data fetching |
| `build-bundle` | Frontend or Node.js with custom build config | `references/build-bundle.md` | Build tool, bundling, optimization |

---

## Selection Guidelines

After reading Phase 1a results:

1. **Project type** (from project-structure.md): backend → skip ui/state/build; frontend → skip data-model/api-surface; fullstack → consider all
2. **Has ORM/DB** → select `data-model` + `infrastructure`
3. **Migration/upgrade task** → always select `migration-risks` + `infrastructure` + `test-coverage`
4. **Has external integrations** → select `integration-points`
5. **Has endpoints** → select `api-surface`
6. **Has Dockerfile/CI** → select `deployment`
7. **When in doubt, include** — extra research is cheap; missing research causes bad specs

Dispatch all selected agents in **one parallel batch**.
