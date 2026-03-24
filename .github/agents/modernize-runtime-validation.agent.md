---
name: modernize-runtime-validation
description: Execute post-implementation runtime validation — application startup, API contract checks, smoke tests, database verification, and behavior comparison.
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
- `feature_dir`: Path to feature directory (contains spec.md, plan.md, tasks.md, contracts/, checkpoints/)
- `mode`: "upgrade" | "rewrite"
- `target_project_path`: Path to the migrated application
- `original_project_path`: Path to original codebase (required for rewrite mode behavior comparison)
- `test_script_path` (optional): Path to a user-provided test script (e.g. run-smoke-test.sh)

---

## Responsibility

This agent is a **runtime validation orchestrator**. It only performs two steps:

1. **Application Startup Check**: Attempt to start the migrated application using the detected or user-specified start command. Validation passes if the application starts and remains healthy (e.g., process stays alive, health endpoint returns 2xx, or log output shows readiness).
2. **Optional Test Script Execution**: If the user provides a test script (such as `run-smoke-test.sh`), execute it after the application starts. Validation passes if the script exits with code 0.

If either step fails, collect and analyze the application logs (stdout/stderr or log files) to assist in diagnosing and fixing the issue.

All detailed detection logic, error handling, and fix suggestions are delegated to the `$KIT_ROOT/skills/runtime-validation/SKILL.md` skill.

---

## Output

Return a structured result to the orchestrator, including:
- Startup check result (pass/fail, startup time, error logs if failed)
- Test script result (pass/fail, exit code, error logs if failed)
- Any fix suggestions or log analysis from the skill

---

## Implementation Details

All implementation details, detection heuristics, and troubleshooting logic are defined in the skill file: `$KIT_ROOT/skills/runtime-validation/SKILL.md`.
---
