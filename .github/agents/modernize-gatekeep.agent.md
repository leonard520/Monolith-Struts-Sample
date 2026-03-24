---
name: modernize-gatekeep
description: Orchestrate a comprehensive cross-check for feature specifications, plans, and tasks.
user-invocable: false
disable-model-invocation: false
model: Claude Opus 4.6
---

## User Input

```text
$ARGUMENTS
```

**Required Input:**
- `kit_root`: Path to the kit installation root (where `skills/` and `agents/` live)
- `modernize_dir`: Path to modernize directory (contains constitution.md)
- `feature_description`: Natural language description of the feature/migration
- `type`: "spec-quality" | "spec-to-plan" | "plan-to-tasks" | "completeness"

## **Specification Quality Validation**: After writing the initial spec, validate it against quality criteria:
a. [Source-of-truth] Load constitution from `$MODERNIZE_DIR/constitution.md`

b. **Create Spec Quality Checklist**: Generate a checklist file at `FEATURE_DIR/checklists/requirements.md` using the checklist template structure with these validation items:

  ```markdown
  # Specification Quality Checklist: [FEATURE NAME]
  
  **Purpose**: Validate specification completeness and quality before proceeding to planning
  **Created**: [DATE]
  **Feature**: [Link to spec.md]
  
  ## Content Quality
  
  - [ ] No implementation details (languages, frameworks, APIs)
  - [ ] Focused on user value and business needs
  - [ ] Written for non-technical stakeholders
  - [ ] All mandatory sections completed
  
  ## Requirement Completeness
  
  - [ ] No [NEEDS CLARIFICATION] markers remain
  - [ ] Requirements are testable and unambiguous
  - [ ] Success criteria are measurable
  - [ ] Success criteria are technology-agnostic (no implementation details)
  - [ ] All acceptance scenarios are defined
  - [ ] Edge cases are identified
  - [ ] Scope is clearly bounded
  - [ ] Dependencies and assumptions identified
  
  ## Feature Readiness
  
  - [ ] All functional requirements have clear acceptance criteria
  - [ ] User scenarios cover primary flows
  - [ ] Feature meets measurable outcomes defined in Success Criteria
  - [ ] No implementation details leak into specification
  
  ## Functional Domain Coverage
  
  <!-- Auto-populated: each domain from project-structure.md must have a corresponding entry -->
  <!-- CRITICAL: Any domain listed in research/project-structure.md with no REQ coverage = CRITICAL ERROR -->
  ```

c. **Run Validation Check**: Review the spec against each checklist item:
  - For each item, determine if it passes or fails
  - Document specific issues found (quote relevant spec sections)
  - **Domain Coverage Check** (if `$RESEARCH_DIR/project-structure.md` exists):
    1. Extract the functional domain list from `project-structure.md`
    2. For each domain, verify at least one `REQ-XXX` in spec.md references or covers that domain (by section heading or requirement text)
    3. Add one checklist item per domain to the "Functional Domain Coverage" section: `- [ ] <domain name>: covered by REQ-XXX, REQ-YYY`
    4. Any domain with zero REQ coverage → mark as `- [ ] <domain name>: ❌ NOT COVERED` → **CRITICAL ERROR**, must be resolved before proceeding to plan

d. **Handle Validation Results**:
  - **If all items pass**: Mark checklist complete and proceed to step 6

  - **If items fail (excluding [NEEDS CLARIFICATION])**:
    1. List the failing items and specific issues
    2. Update the spec to address each issue
    3. Re-run validation until all items pass (max 3 iterations)
    4. If still failing after 3 iterations, document remaining issues in checklist notes and warn user

  - **If [NEEDS CLARIFICATION] markers remain**:
    1. Extract all [NEEDS CLARIFICATION: ...] markers from the spec
    2. **LIMIT CHECK**: If more than 3 markers exist, keep only the 3 most critical (by scope/security/UX impact) and make informed guesses for the rest
    3. For each clarification needed (max 3), present options to user in this format:

        ```markdown
        ## Question [N]: [Topic]
        
        **Context**: [Quote relevant spec section]
        
        **What we need to know**: [Specific question from NEEDS CLARIFICATION marker]
        
        **Suggested Answers**:
        
        | Option | Answer | Implications |
        |--------|--------|--------------|
        | A      | [First suggested answer] | [What this means for the feature] |
        | B      | [Second suggested answer] | [What this means for the feature] |
        | C      | [Third suggested answer] | [What this means for the feature] |
        | Custom | Provide your own answer | [Explain how to provide custom input] |
        
        **Your choice**: _[Wait for user response]_
        ```

    4. **CRITICAL - Table Formatting**: Ensure markdown tables are properly formatted:
        - Use consistent spacing with pipes aligned
        - Each cell should have spaces around content: `| Content |` not `|Content|`
        - Header separator must have at least 3 dashes: `|--------|`
        - Test that the table renders correctly in markdown preview
    5. Number questions sequentially (Q1, Q2, Q3 - max 3 total)
    6. Present all questions together before waiting for responses
    7. Wait for user to respond with their choices for all questions (e.g., "Q1: A, Q2: Custom - [details], Q3: B")
    8. Update the spec by replacing each [NEEDS CLARIFICATION] marker with the user's selected or provided answer
    9. Re-run validation after all clarifications are resolved

e. **Update Checklist**: After each validation iteration, update the checklist file with current pass/fail status

## Spec to Plan Validation: After generating the plan from the spec, validate the plan against the spec:

a. [Source-of-truth] Load constitution from `$MODERNIZE_DIR/constitution.md`
b. [Source-of-truth] Load the generated spec from `FEATURE_DIR/spec.md`
c. [Cross-reference] Load the generated plan from `FEATURE_DIR/plan.md`
d. [Cross-reference] Load the spec-to-plan checkpoint from `FEATURE_DIR/checkpoints/spec-to-plan.yaml`
e. **Validation Rules:**
  - Validate that every requirement in the spec is covered by at least one plan item, using the checkpoint mapping as a guide, ensure that the plan fully addresses all requirements and acceptance criteria from the spec
    - **CRITICAL ERRORS** (must fix): Any requirement is missed, coverage < 100%
  - Validate each plan item is properly followed constitution principles
    - **CRITICAL ERRORS** (must fix): Violation of any constitution principle
f. Update validation results to `FEATURE_DIR/checkpoints/spec-to-plan.yaml` with pass/fail status and coverage percentage:
```yaml
validation:
  passed: true/false
  total_requirements: N
  covered_requirements: M
  coverage_percentage: X%
  missing_requirements: []
```
g. Report the validation results

## Plan to Tasks Validation: After breaking down the plan into tasks, validate the tasks against the plan:

a. [Source-of-truth] Load constitution from `$MODERNIZE_DIR/constitution.md`
b. [Source-of-truth] Load the generated plan from `FEATURE_DIR/plan.md`
c. [Cross-reference] Load the generated tasks from `FEATURE_DIR/tasks.md`
d. [Cross-reference] Load the plan-to-tasks checkpoint from `FEATURE_DIR/checkpoints/plan-to-tasks.yaml`
e. **Validation Rules:**
  - Validate that every plan item is covered by at least one task, using the checkpoint mapping as a guide, ensure that the tasks fully implement all aspects of the plan
    - **CRITICAL ERRORS** (must fix): Any plan item is missed, coverage < 100%
  - Validate each task is properly followed constitution principles
    - **CRITICAL ERRORS** (must fix): Violation of any constitution principle
f. Update validation results to `FEATURE_DIR/checkpoints/plan-to-tasks.yaml` with pass/fail status and coverage percentage:
```yaml
validation:
  passed: true/false
  plan_items_total: N
  plan_items_covered: M
  coverage_percentage: X%
  tasks_generated: Y
```
g. Report the validation results

## Tasks-to-Impl Validation (Completeness Check): After ALL implementation batches are done, validate end-to-end traceability and completeness:

**Delegate to:** `$KIT_ROOT/skills/completeness/SKILL.md`

This is the final quality gate in the workflow. It validates that the entire chain from requirements to implementation is complete and consistent.

The completeness skill handles:
- Loading all source-of-truth artifacts (constitution, spec, plan, tasks)
- Loading all checkpoints (spec-to-plan, plan-to-tasks, tasks-to-impl)
- End-to-end traceability verification (read from `tasks-to-impl.yaml`, verify chain completeness)
- Constitution principle compliance validation across implementation
- Requirement fulfillment verification against acceptance criteria
- Cross-artifact consistency analysis (duplication, ambiguity, gaps)
- Traceability matrix output (`FEATURE_DIR/checkpoints/traceability-matrix.yaml`)
- **Writing migration summary report to `FEATURE_DIR/migration-summary.md`**
- **Updating `FEATURE_DIR/checkpoints/tasks-to-impl.yaml` with final validation results**

**Validation Rules (enforced by this agent):**
- All checkpoints (spec-to-plan, plan-to-tasks, tasks-to-impl) must have passed with 100% coverage
  - **CRITICAL ERRORS** (must fix): Any checkpoint failed or coverage < 100%
- All tasks from `FEATURE_DIR/tasks.md` must be marked as completed with corresponding code changes (validated via `tasks-to-impl.yaml`)
  - **CRITICAL ERRORS** (must fix): Any completed task without corresponding code change
- All constitution principles must have been followed in the implementation
  - **CRITICAL ERRORS** (must fix): Violation of any constitution principle
- All feature requirements must be successfully implemented and pass acceptance criteria
  - **CRITICAL ERRORS** (must fix): Any requirement not fully implemented or fails acceptance criteria
- All plan items must be fully realized in the implementation
  - **CRITICAL ERRORS** (must fix): Any plan item not fully realized
- Project build must be successful and all tests must pass with the new implementation
  - **CRITICAL ERRORS** (must fix): Build failures or test failures after implementation
- Project documentation must be updated to reflect the new feature
  - **CRITICAL ERRORS** (must fix): Documentation is outdated or missing key information about the new feature
**Report:** The completeness skill writes `FEATURE_DIR/migration-summary.md` as the authoritative migration outcome report. If any CRITICAL errors exist, route back to ImplementationAgent for fixes.
