---
name: modernize-task
description: Generate tasks for a SINGLE phase/slice from the implementation plan. Designed to run in parallel — one instance per phase.
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
- `feature_dir`: Path to feature directory (contains plan.md)
- `mode`: "upgrade" | "rewrite"
- `phase_id`: Which phase to generate tasks for (e.g. "Phase-1", "Phase-2", "Phase-3")
- `phase_title`: Human-readable phase title (e.g. "Project Foundation & Tooling")
- `plan_items`: Comma-separated plan item IDs for this phase (e.g. "1.1,1.2,1.3,1.4,1.5,1.6,1.7")

**Optional Input:**
- `plan_artifacts`: Paths to PlanAgent outputs (plan.md, checkpoint, contracts)
- `task_id_offset`: Starting task ID number (default: 1). Use to avoid ID collisions across parallel instances.

## Responsibility

This agent generates tasks for **ONE phase only**. Multiple instances run in parallel (one per phase) to speed up task generation.

**Output file:** `$FEATURE_DIR/tasks-phase-{N}.md` where N is the phase number extracted from `phase_id`.

## Prerequisites

- PlanAgent completed successfully
- Spec-to-plan checkpoint exists and passes validation
- Plan.md and spec.md available

---

## Execution Flow

### Step 1: Load Context (Minimal)

Read only what's needed for YOUR phase:

```bash
# Derive numeric phase index from the phase_id input (e.g., "Phase-3" -> "3")
PHASE_NUMBER="${PHASE_ID#Phase-}"

# Read only your phase section from plan.md
grep -nE "### Phase.*${PHASE_NUMBER}|## Phase.*${PHASE_NUMBER}" "$FEATURE_DIR/plan.md"
# Then use view_range to read only those lines

# Read spec.md — only the user story related to your phase (if applicable)
# Read knowledge graph — only modules relevant to your phase
# Read checkpoint — for upstream traceability
```

**⚠️ Do NOT read the entire plan.md.** Use grep to find your phase section line numbers, then read only those lines.

### Step 2: Generate Tasks for This Phase

Follow the task checklist format:

```text
- [ ] [TaskID] [P?] [Story?] [Plan:X.Y] Description with file path
```

**Task ID format:** Use offset-based IDs. If `task_id_offset=50`, start at T050.

**Organization:**
- Phase 1 (Foundation): Setup tasks, no [Story] label
- Phase 2+ (Slices): Include [US-N] story label, organized as: Models → Services → Controllers → Templates → Tests
- Mark parallelizable tasks with [P]
- Every task MUST have [Plan:X.Y] traceability reference

### Step 3: Write Output File Incrementally

Write tasks to `$FEATURE_DIR/tasks-phase-{N}.md`:

```markdown
## Phase {N}: {Phase Title}

### {Item X.Y Title}

- [ ] T{offset} [Plan:X.Y] Description...
- [ ] T{offset+1} [P] [Plan:X.Y] Description...
```

### Step 4: Generate Phase Checkpoint Fragment

Write `$FEATURE_DIR/checkpoints/plan-to-tasks-phase-{N}.yaml`:

```yaml
phase_id: "Phase-{N}"
phase_title: "{Phase Title}"
plan_items_covered:
  - item_id: "X.Y"
    tasks: ["T0XX", "T0XY"]
    status: "covered"
total_tasks: NN
```

---

## Completion Criteria

- [ ] `tasks-phase-{N}.md` generated with all tasks for this phase
- [ ] Every task has [Plan:X.Y] reference
- [ ] Phase checkpoint fragment generated
- [ ] All plan items for this phase are covered

## Output Format

```yaml
phase_tasks_complete: true
phase_id: "Phase-{N}"
artifacts:
  tasks: "FEATURE_DIR/tasks-phase-{N}.md"
  checkpoint: "FEATURE_DIR/checkpoints/plan-to-tasks-phase-{N}.yaml"
task_count: NN
errors: []
next_step: "Return to orchestrator — wait for TaskMergeAgent dispatch"
```

## ⚡ Incremental Write Rule

**Write tasks to disk in batches of 5-10.** Do NOT buffer all tasks in memory before writing.
