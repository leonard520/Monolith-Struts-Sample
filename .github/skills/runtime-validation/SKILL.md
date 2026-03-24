---
name: runtime-validation
description: Post-implementation runtime validation — verify the migrated application starts successfully, and optionally run a user-provided test script. Use application logs to diagnose and fix failures.
---

## User Input

```text
$ARGUMENTS
```

You **MUST** consider the user input before proceeding (if not empty).

## Goal

After implementation and completeness check pass, verify the migrated application **actually runs** at runtime. All previous gates (build, unit test, traceability) are static/compile-time. This gate validates runtime behavior.

## Operating Constraints

- **Fail-fast**: If startup fails, skip the test script step.
- **Log-driven debugging**: On any failure, capture and analyze application logs to suggest fixes.

---

## Input Requirements

```yaml
kit_root: "<kit_root>"
modernize_dir: "<path>"
feature_dir: "<path>"
mode: "upgrade" | "rewrite"
target_project_path: "<path>"
test_script_path: "<path>"   # optional — user-provided test script
```

---

## Step 1: Application Startup Verification

**Objective**: Confirm the application starts and reaches a healthy state.

### 1a: Auto-Detect Start Command

Detect build tool and framework from `target_project_path`:

| Indicator | Tech Stack | Default Start Command |
|-----------|------------|-----------------------|
| `pom.xml` with spring-boot plugin | Spring Boot (Maven) | `mvn spring-boot:run` |
| `build.gradle` with spring-boot plugin | Spring Boot (Gradle) | `gradle bootRun` |
| `package.json` with `start` script | Node.js | `npm start` |
| `requirements.txt` or `pyproject.toml` | Python | `python manage.py runserver` or `flask run` |
| `*.csproj` | .NET | `dotnet run` |

If user provides `startup.command` in `runtime-validation-config.yaml`, use that instead.

### 1b: Detect Health/Readiness Signal

Determine how to know the app is ready (check in order, use first match):

1. **Health endpoint** — search config files (`application.yml`, `application.properties`, `.env`) and source code for health endpoints (`/actuator/health`, `/health`, `/healthz`, `/api/health`, `/status`). Poll with HTTP GET until 2xx response.
2. **Stdout pattern** — watch process stdout for common readiness patterns: `Started`, `Listening on port`, `Application is running`, etc.
3. **Process alive** — if no health endpoint and no stdout pattern detected, simply verify the process is still running after a grace period.

If user provides `startup.health_endpoint` or `startup.readiness_pattern` in config, use that instead.

### 1c: Execute Startup

1. Start the application in background from `target_project_path`
2. Wait for readiness signal (from 1b)
3. Timeout: use `startup.startup_timeout_seconds` from config, or default **60s**
4. **PASS**: Readiness signal received within timeout
5. **FAIL**: Process crashes, exits non-zero, or timeout exceeded

### 1d: On Failure — Log Capture & Analysis

If startup fails:
1. Capture the last **200 lines** of stdout/stderr
2. Search for common log files in `target_project_path` (e.g., `logs/`, `log/`, `*.log`)
3. Analyze logs to identify root cause (missing dependency, config error, port conflict, bean injection failure, etc.)
4. Generate actionable fix suggestions with specific file/line references

---

## Step 2: Test Script Execution (Optional)

**Prerequisite**: Step 1 passed (application is running).

**Skip condition**: If `test_script_path` is not provided, skip this step entirely and report overall result as PASS (startup-only validation).

### 2a: Execute Script

1. Verify the script file exists and is executable
2. Run the script: `bash <test_script_path>` (or `sh` if bash unavailable)
3. **PASS**: Script exits with code **0**
4. **FAIL**: Script exits with non-zero code

### 2b: On Failure — Log Capture & Analysis

If the test script fails:
1. Capture the script's stdout/stderr output
2. Capture the application's recent log output (last 200 lines) — the failure may be in the app, not the script
3. Correlate script errors with application logs to identify root cause
4. Generate actionable fix suggestions

---

## Cleanup

After all steps complete (pass or fail):
1. Stop the application process started in Step 1
2. Report results

---

## Fix Loop

When re-dispatched by the orchestrator after a failure:

1. Read the previous runtime validation report to understand what failed
2. Read application logs and any captured error output
3. Identify root cause and generate fix tasks:
   ```markdown
   ## Runtime Fix Tasks
   - [ ] RF-001: Fix missing bean — add @Service annotation to OrderService
   - [ ] RF-002: Fix port conflict — change server.port in application.yml
   ```
4. Execute fixes (modify code in `target_project_path`)
5. Re-run the failed step (startup or test script)
6. Update report

**Max fix iterations**: 3. If still failing → report remaining issues to orchestrator.

---

## Output

Write `FEATURE_DIR/runtime-validation-report.md`:

```markdown
# Runtime Validation Report

**Generated**: [ISO 8601 timestamp]
**Mode**: [upgrade|rewrite]
**Target**: [target_project_path]

## Summary

| Step | Status | Details |
|------|--------|---------|
| Application Startup | ✓ PASS | Started in 8.3s, health endpoint `/actuator/health` → 200 |
| Test Script | ✓ PASS | `run-smoke-test.sh` exited with code 0 |

**Overall**: PASS

## Startup Details
- Start command: `mvn spring-boot:run`
- Readiness signal: `/actuator/health` → 200 OK
- Startup time: 8.3s

## Test Script Details (if executed)
- Script: `run-smoke-test.sh`
- Exit code: 0
- Output: [captured stdout/stderr]

## Issues Found (if any)
| # | Step | Description | Fix Suggestion |
|---|------|-------------|----------------|
| 1 | Startup | Failed: BeanCreationException for OrderService | Add @Service annotation to OrderService.java |

## Application Logs (on failure)
[last 200 lines of captured logs]
```

Also append runtime validation results to `FEATURE_DIR/checkpoints/tasks-to-impl.yaml`:

```yaml
runtime_validation:
  passed: true|false
  timestamp: "[ISO 8601]"
  startup: { passed: true, startup_time_seconds: 8.3 }
  test_script: { passed: true, exit_code: 0, script: "run-smoke-test.sh" }  # or null if not provided
  fix_iterations: 0
  overall_passed: true
```
