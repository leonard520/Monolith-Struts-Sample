---
name: planning
description: Create an implementation plan from a feature specification, with traceability checkpoint generation.
---
## User Input

```text
$ARGUMENTS
```

You **MUST** consider the user input before proceeding (if not empty).

## Outline

1. **Setup**: Run script from repo root and parse JSON for FEATURE_SPEC, IMPL_PLAN, SPECS_DIR, BRANCH. For single quotes in args like "I'm Groot", use escape syntax: e.g 'I'\''m Groot' (or double-quote if possible: "I'm Groot").

   > `$KIT_ROOT` is provided by the calling agent.

   - Bash example: `$KIT_ROOT/skills/scripts/bash/setup-plan.sh --json` 
   - PowerShell example: `$KIT_ROOT/skills/scripts/powershell/setup-plan.ps1 -Json`

2. **Load context**: 
   1. Read FEATURE_SPEC starting at line 1 and continuing as needed to include all `REQ-XXX` requirement IDs (for example, using a helper like `view_range` if available), focusing first on the user stories section; read `$MODERNIZE_DIR/constitution.md` in full (short). Load IMPL_PLAN template (already copied).
   2. Read KNOWLEDGE_GRAPH

3. **Extract all requirements for tracking**:
   - Parse spec for all `REQ-XXX` IDs
   - Create requirements inventory with: ID, description, priority
   - This inventory is the source of truth for checkpoint generation

4. **Generate artifacts sequentially**:
   - Phase 0: Research → research.md (resolve all NEEDS CLARIFICATION)
   - Phase 1.1: Data Model → data-model.md
   - Phase 1.2: API Contracts → contracts/
   - [REWRITE ONLY]: Business Logic Extraction → business-logic-inventory.md
   - See Phase 0 and Phase 1 sections below for detailed instructions

5. **Plan Assembly** (after artifact generation):
   - Load all subagent outputs
   - Fill Technical Context from research.md
   - Fill Constitution Check section from constitution
   - Evaluate gates (ERROR if violations unjustified)
   - Generate quickstart.md from contracts and data model
   - Update agent context by running the agent script
   - Re-evaluate Constitution Check post-design
   - **CRITICAL**: For each plan item, document which REQ-XXX it addresses

6. **Generate Traceability Checkpoint** (after plan complete):
   - Create `FEATURE_DIR/checkpoints/spec-to-plan.yaml`
   - Use template: `$KIT_ROOT/skills/templates/spec-to-plan-checkpoint-template.yaml`
   - Map each requirement to plan phases/items
   - Identify orphan plan items (no requirement trace)
   - Calculate coverage percentage

7. **Validate Checkpoint**:
   ```
   CRITICAL ERRORS (must fix before proceeding):
   - Any requirement with status: "missing"
   - Coverage percentage < 100%
   
   WARNINGS (document but can proceed):
   - Orphan plan items (may be valid infrastructure)
   ```

8. **Stop and report**: Command ends after plan assembly. Report:
   - Branch, IMPL_PLAN path, and generated artifacts
   - Coverage summary table

## Phases

### Phase 0: Research

> Execute this phase sequentially as part of the planning workflow.

1. **Extract unknowns from Technical Context**:
   - For each NEEDS CLARIFICATION → research task
   - For each dependency → best practices task
   - For each integration → patterns task

2. **Generate and dispatch research agents**:

   ```text
   For each unknown in Technical Context:
     Task: "Research {unknown} for {feature context}"
   For each technology choice:
     Task: "Find best practices for {tech} in {domain}"
   ```

3. **Consolidate findings** in `research.md` using format:
   - Decision: [what was chosen]
   - Rationale: [why chosen]
   - Alternatives considered: [what else evaluated]

4. **[REWRITE MODE ONLY] Business Logic Extraction**:
   - **Delegate to:** `$KIT_ROOT/skills/rewrite/SKILL-business-logic-extraction.md`
   - Use the knowledge graph to identify source code components
   - Extract and catalog all business logic units (BL-XXX) with inputs, outputs, business rules, and edge cases
   - Generate `FEATURE_DIR/business-logic-inventory.md`
   - This inventory feeds into task breakdown and functional equivalence verification

**Output**: research.md with all NEEDS CLARIFICATION resolved. In rewrite mode, also `business-logic-inventory.md`.

### Phase 1: Design & Contracts

> Execute these tasks sequentially as part of the planning workflow.

#### Data Model

> ⚠️ **Data Model Preservation Rule**: In `upgrade` and `rewrite` modes, the target data model MUST preserve the existing database schema (table names, column names, data types, relationships). Do NOT redesign, rename, or restructure entities unless the constitution explicitly permits schema migration. The purpose of data-model.md is to **document the existing model**, not to improve it.

1. **Extract entities from feature spec** → `data-model.md`:
   - Entity name, fields, relationships
   - Validation rules from requirements
   - State transitions if applicable
   - **[upgrade/rewrite]** Mirror existing schema exactly — flag any discrepancies as `[SCHEMA CONFLICT: ...]` for human review, do not silently resolve them

#### API Contracts

2. **Generate API contracts** from functional requirements:
   - For each user action → endpoint
   - Use standard REST/GraphQL patterns
   - Output OpenAPI/GraphQL schema to `/contracts/`

**Output**: contracts/ directory with API specifications

### Plan Assembly

> This section runs after all artifact generation phases complete.

1. **Load all outputs**: research.md, data-model.md, contracts/, [rewrite] business-logic-inventory.md
2. **Load design-phase research** (migration/rewrite mode — required; new feature mode — skip): Read all files from `$MODERNIZE_DIR/research/`. Use to fill the migration-specific Technical Context fields in plan.md:
   - `Source Stack` ← `research/tech-stack.md`
   - `Target Stack` ← decide here based on tech-stack.md + constitution + applicable guidelines
   - `Schema Migration` ← `research/data-model.md` + `research/infrastructure.md`
   - `Test Strategy` ← `research/infrastructure.md` + `research/test-coverage.md`
   - `Deployment` ← `research/deployment.md`
   - `Migration Risks` ← `research/migration-risks.md`
3. **Fill plan template** using IMPL_PLAN template structure:
   - Technical Context: derive HOW decisions (target stack, infra choices, test strategy, deployment approach) from the design research + constitution principles + applicable guidelines. This is where implementation decisions are made.
   - Constitution Check from constitution principles
4. **Generate quickstart.md** from contracts and data model (integration scenarios)
5. **Agent context update**:
   - Run script:
      - Bash example: `$KIT_ROOT/skills/scripts/bash/update-agent-context.sh copilot`
      - PowerShell example: `$KIT_ROOT/skills/scripts/powershell/update-agent-context.ps1 -Agent copilot`
   - Add only new technology from current plan
6. **Integrate guidelines** into plan phases (see Guideline Check section)
7. **Map every plan item** to at least one REQ-XXX
8. **Write** `FEATURE_DIR/plan.md` **section by section** following the plan template structure. Write each section to file before moving to the next:

   a. Write header + Summary → append to plan.md
   b. Write Technical Context → append to plan.md
   c. Write Constitution Check → append to plan.md
   d. Write Project Structure → append to plan.md
   e. Write Complexity Tracking + remaining → append to plan.md

   **CRITICAL**: Write each section to file before moving to the next. Do NOT generate all sections first and write at the end.

**Output**: plan.md, quickstart.md, agent-specific file

## Guideline Check

Before proceeding with planning, check for applicable guidelines:

1. **Identify technologies** from feature spec and existing codebase:
   - Source frameworks (Struts, EJB, JSF, etc.)
   - Target frameworks (Spring Boot, React, etc.)
   - Migration patterns mentioned

2. **Search guidelines directory**: `skills/guidelines/`
   - List all subdirectories (e.g., `struts-to-spring/`, `spring-boot-scaffolding/`)
   - Read each subdirectory's `SKILL.md` to check applicability based on technology patterns

3. **For each matching guideline**:
   - Load the SKILL.md content
   - Extract planning-relevant skills (dependency changes, architecture patterns)
   - Integrate migration checklist into plan phases
   - Document in plan.md under "Applied Guidelines" section

4. **Add guideline tasks to research.md**:
   - Technology-specific best practices
   - Migration path decisions
   - Compatibility considerations

## Checkpoint Generation

### Step-by-Step Checkpoint Creation

After plan.md is complete, generate the traceability checkpoint:

1. **Create checkpoint directory**: `FEATURE_DIR/checkpoints/` (if not exists)

2. **Extract requirement inventory from spec**:
   ```yaml
   # Scan spec for REQ-XXX patterns
   requirements_found:
     - id: "REQ-001"
       description: "User can login with email"
       priority: "P1"
       source_line: 45
   ```

3. **Extract plan item inventory from plan.md**:
   ```yaml
   # Scan plan for phase items
   plan_items_found:
     - phase: "Phase-2"
       item_id: "2.1"
       description: "Implement authentication controller"
       requirements_mentioned: ["REQ-001"]  # explicit or inferred
   ```

4. **Build coverage mapping**:
   - For each requirement: find all plan items that address it
   - For each plan item: verify it traces to at least one requirement
   - Mark requirements as: covered | partial | missing
   - Mark plan items as: traced | orphan

5. **Generate checkpoint YAML**:
   ```yaml
   # checkpoints/spec-to-plan.yaml
   metadata:
     generated: "2026-01-30"
     source_spec: "spec.md"
     target_plan: "plan.md"
   
   summary:
     total_requirements: 10
     covered_requirements: 9
     missing_requirements: 1
     coverage_percentage: 90.0
   
   requirements:
     - req_id: "REQ-001"
       req_description: "User can login with email"
       priority: "P1"
       plan_mapping:
         phase: "Phase-2"
         items:
           - item_id: "2.1"
             description: "Implement authentication controller"
       status: "covered"
   
   missing_requirements:
     - req_id: "REQ-005"
       req_description: "User can reset password"
       reason: "Not addressed in current plan"
   
   validation:
     passed: false
     errors: ["REQ-005 has no plan coverage"]
   ```

6. **Validation Gate**:
   - If `validation.passed == false`: 
     - Display missing requirements
     - Ask user to either:
       a) Update plan to cover missing requirements
       b) Explicitly mark requirement as deferred (with justification)
     - Do NOT proceed to tasks until resolved

### Checkpoint File Naming

- Checkpoint file: `checkpoints/spec-to-plan.yaml`

## Key rules

- Use absolute paths
- ERROR on gate failures or unresolved clarifications
- Apply guideline skills when technology patterns match
- **NEVER proceed to tasks with missing requirement coverage**
