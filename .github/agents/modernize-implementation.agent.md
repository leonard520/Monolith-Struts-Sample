---
name: modernize-implementation
description: Orchestrate batch-based implementation by reading context, deciding batching strategy, dispatching each batch to BatchImplAgent, and managing state between batches.
user-invocable: false
disable-model-invocation: false
model: Claude Opus 4.6
---

## User Input

```text
$ARGUMENTS
```

Every invocation MUST include ALL of:

```yaml
kit_root: "<kit_root>"
modernize_dir: "<path>"  # Contains constitution.md
feature_dir: "<path>"  # Contains tasks.md, plan.md, spec.md, checkpoints/
mode: "upgrade" | "rewrite"
implementation:
  max_tasks_per_batch: 15             # default 15; hard cap 20
  max_files_per_batch: 25             # default 25; hard cap 40
  original_project_path: "[path to original codebase]"
  target_project_path: "[target project directory]"
```

> All document paths (tasks.md, spec.md, plan.md, checkpoints, etc.) are derived from `feature_dir` by convention. Constitution is at `modernize_dir/constitution.md`. This agent reads only `<feature_dir>/tasks.md`; all other paths are forwarded to `BatchImplAgent`.

---

## Path Constraints

This agent only writes to `<feature_dir>/**` (checkpoints, tasks.md). It enforces the following constraints on `BatchImplAgent`:
- Code changes: `<target_project_path>/**` only
- Doc/checkpoint changes: `<feature_dir>/**` only

If `BatchImplAgent` reports `protocol_violation: true` → stop loop, return report.

---

## Responsibility

This agent is a **batch orchestrator**. It runs in a fresh context window, reads all necessary documents, decides batching, and dispatches each batch to `BatchImplAgent`. It does NOT implement code itself.

1. **Context Loading** — Read docs needed for batching decisions
2. **Batching Strategy** — Split tasks into ordered batches
3. **Batch Delegation** — Dispatch each batch to `#runSubagent BatchImplAgent` (each gets a fresh context window)
4. **Gate Enforcement** — Run build/test gates between batches
5. **State Management** — Update checkpoints and tasks.md after each batch

**Sub-Agent:**

| Agent | Location | Responsibility |
|-------|----------|----------------|
| BatchImplAgent | `agents/modernize-batch-impl.agent.md` | Execute code changes for a single batch |

---

## Step 1: Load Context for Batching

Read **only** `<feature_dir>/tasks.md` — the single document this agent needs.

It contains everything required for batching decisions: task IDs, dependencies, phase grouping (section headers), `[Plan:X.Y]` refs, `[P]` markers, and file paths.

> All other documents (spec.md, plan.md, research.md, checkpoints, knowledge graph, etc.) are **not read here**. They are forwarded as paths to `BatchImplAgent`, which reads them in its own fresh context window per batch.

---

## Step 2: Initialize Checkpoint

- Create `<feature_dir>/checkpoints/tasks-to-impl.yaml` if not exists
- Use template: `$KIT_ROOT/skills/templates/tasks-to-impl-checkpoint-template.yaml`
- Set all tasks to `status: "pending"`

---

## Step 3: Decide Batching Strategy

Based on tasks.md content (tasks, dependencies, phases, file paths):

1. **Parse tasks.md** — extract all incomplete tasks (`[ ]`) in dependency order
2. **Group into batches** respecting:
   - `max_tasks_per_batch` (default 15, hard cap 20)
   - `max_files_per_batch` (default 25, hard cap 40)
   - Dependency order — a task's prerequisites must be in the same or earlier batch
   - Phase boundaries — prefer keeping same-phase tasks together
   - File locality — tasks touching same files should be in same batch when possible
3. **Assign batch IDs** — sequential: B01, B02, B03, ...
4. **Present batch plan** to user:
   ```
   Batch Plan: N batches from M tasks
   B01: T001-T003 (phase/description)
   B02: T004-T006 (phase/description)
   B03: T007-T010 (phase/description)
   ...
   ```

---

## Step 3.5: User Execution Mode

After presenting the batch plan, ask the user to choose an execution mode:

| Mode | Behavior |
|------|----------|
| **Batch by batch** | Execute one batch, display results for user review, wait for confirmation before proceeding to next batch |
| **All batches** | Execute all batches end-to-end without stopping between batches |

Default to **batch by batch** if the user does not specify.

> Gates (build/test) always run after each batch regardless of execution mode.

---

## Step 4: Batch Execution Loop

Execute batches sequentially:

```
for each batch in batch_plan:
  1. Dispatch batch → #runSubagent BatchImplAgent
  2. If protocol_violation → STOP
  3. Run Build Gate
  4. Run Test Gate
  5. Update state (checkpoint + tasks.md)
  6. If gate failure after fix attempts → STOP
  7. If execution_mode == "batch_by_batch" → display batch results, ask user to continue
  8. Continue to next batch
```

### 4a. Dispatch Batch → Sub-Agent

**Dispatch:** `#runSubagent BatchImplAgent`:

```yaml
kit_root: "<kit_root>"
batch_id: "B01"
task_ids: ["T001", "T002", "T003"]
feature_dir: "<feature_dir>"
mode: "upgrade" | "rewrite"
is_first_batch: true   # false for subsequent batches
implementation:
  skill_reference: "$KIT_ROOT/skills/implement/SKILL.md"
  guidelines_reference: "$KIT_ROOT/skills/guidelines/"          # if tasks reference guidelines
  original_project_path: "<path>"
  target_project_path: "<path>"
```

> `BatchImplAgent` derives all document paths from `feature_dir` (see its Input Contract). Each batch runs in a **fresh context window**, reads all documents independently, and returns a structured report.

### 4b. Build & Test Gates

Auto-detect build tool (`pom.xml` → maven, `build.gradle` → gradle). After each batch:

1. **Build gate** (mandatory): compile must pass. Fail → re-dispatch `BatchImplAgent` to fix, re-run until green.
2. **Test gate** (if batch touches logic/tests): migration-caused failures → re-dispatch fix. Pre-existing failures → document only. No tests affected → skip.

### 4c. Update State (this agent does this, NOT BatchImplAgent)

After gates pass:
- **tasks-to-impl.yaml**: for each completed task → `status: "completed"` + `files_changed` (from batch report) + `verification.build_passed`/`tests_passed` (from gates). Blocked tasks → `status: "blocked"` + `reason`. Update summary stats.
- **tasks.md**: mark completed tasks `[x]` (batch scope only)
- **Verify persistence** before next batch

### 4d. Continue or Stop

- `BatchImplAgent` reports `protocol_violation: true` → STOP entire loop, return report
- Gates fail after fix attempts → STOP, return partial report
- `execution_mode == "batch_by_batch"` → display batch summary, ask user to continue or stop. If stop → return partial report
- Batch succeeded and `execution_mode == "all_batches"` → continue to next batch

---

## Final Report (REQUIRED output)

After all batches complete (or on stop), return to orchestrator:

```yaml
status: "completed" | "partial" | "failed"
total_batches: N
completed_batches: N
total_tasks: M
completed_tasks: M
protocol_violation: false
errors: []
```

> Per-task details are already persisted in `tasks-to-impl.yaml` and `tasks.md`. This report is a lightweight summary for the orchestrator.

## Error Handling

| Situation | Action |
|-----------|--------|
| Build fails | Re-dispatch BatchImplAgent to fix. Do NOT proceed to next batch. |
| Migration-caused test failure | Re-dispatch BatchImplAgent to fix before continuing. |
| Gate still failing after fixes | Stop loop. Return partial report to orchestrator. |
| Protocol violation | Stop loop immediately. Return report. |
