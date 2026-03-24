---
name: modernize-task-merge
description: Merge parallel TaskAgent outputs into a single tasks.md and generate the combined checkpoint.
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
- `feature_dir`: Path to feature directory (contains tasks-phase-*.md files)
- `total_phases`: Number of phases to merge

## Responsibility

After all parallel TaskAgents complete, this agent:
1. Merges `tasks-phase-{1..N}.md` into a single `tasks.md`
2. Merges checkpoint fragments into `plan-to-tasks.yaml`
3. Validates full coverage

## Execution Flow

### Step 1: Collect Phase Files

```bash
ls -1 $FEATURE_DIR/tasks-phase-*.md | sort -t- -k3 -n
ls -1 $FEATURE_DIR/checkpoints/plan-to-tasks-phase-*.yaml | sort -t- -k5 -n
```

### Step 2: Merge tasks.md

Concatenate all phase files in order, adding a header:

```markdown
# Tasks: {Feature Name}

## Format: `[ID] [P?] [Story?] [Plan:X.Y] Description`

{contents of tasks-phase-1.md}

{contents of tasks-phase-2.md}

...

## Dependencies
{Generate dependency graph showing phase completion order}

## Parallel Execution
{Identify which phases can run concurrently}
```

Write to `$FEATURE_DIR/tasks.md`.

### Step 3: Merge Checkpoints

Combine all `plan-to-tasks-phase-{N}.yaml` fragments into `$FEATURE_DIR/checkpoints/plan-to-tasks.yaml`:
- Aggregate plan_items_covered from all phases
- Calculate total coverage percentage
- Identify any missing plan items
- Include upstream traceability from spec-to-plan checkpoint

### Step 4: Validate

- Every plan item from plan.md must appear in at least one phase checkpoint
- Coverage must be 100%
- No duplicate task IDs across phases

### Step 5: Cleanup (Optional)

After successful merge, the individual `tasks-phase-*.md` files can be kept for reference.

## Output Format

```yaml
merge_complete: true
artifacts:
  tasks: "FEATURE_DIR/tasks.md"
  checkpoint: "FEATURE_DIR/checkpoints/plan-to-tasks.yaml"
total_tasks: NN
coverage_percentage: 100.0
errors: []
```
