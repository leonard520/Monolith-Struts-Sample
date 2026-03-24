---
name: modernize-foundation
description: Orchestrate foundation phase - constitution generation and knowledge graph building.
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
- `modernize_dir`: Path to modernize directory (e.g., `.github/modernize`)

## Responsibility

This agent **orchestrates** the **Foundation Phase** of the re-architecture workflow:

1. **Constitution Generation** - Generate project constitution from principles
2. **Knowledge Graph Generation** - Build knowledge graph of the application module

## Prerequisites

- User has confirmed migration mode (Upgrade or Rewrite)
- Access to source codebase

---

## Execution Flow

> **PARALLEL EXECUTION**: Dispatch Step 1 and Step 2 as **two parallel sub-agents simultaneously**. Constitution generation and Knowledge Graph generation are independent — do NOT wait for Step 1 to finish before starting Step 2. Wait for both to complete before returning results.

### Step 1: Constitution Generation (parallel)

**Delegate to:** `$KIT_ROOT/skills/constitution/SKILL.md`

Execute the constitution skill with user-provided principles. The skill handles:
- Template loading and placeholder filling
- Dependent template propagation
- Sync Impact Report generation

**Expected Output:** `.github/modernize/constitution.md`

### Step 2: Knowledge Graph Generation (parallel)

**Delegate to:** `$KIT_ROOT/skills/java-knowledge-graph/SKILL.md`

Execute the knowledge graph skill to analyze the codebase. The skill handles:
- Component identification and dependency mapping
- Architecture pattern detection
- Technology stack inventory
- Migration order determination

**Expected Output:** Knowledge graph saved at `.github/modernize/knowledge-graph/`

### Step 3: Merge Results

Wait for both Step 1 and Step 2 to complete, then validate both outputs exist before returning.

---

## Completion Criteria

- [ ] Constitution file exists and passes validation
- [ ] Knowledge graph is generated with dependency mapping
- [ ] No critical errors encountered

## Output Format

```yaml
foundation_complete: true/false
setup_artifacts:
  constitution: "$MODERNIZE_DIR/constitution.md"
  knowledge_graph_dir: "[path to knowledge graph, or null if Python unavailable]"
errors: []
warnings: []
```

> If the `java-knowledge-graph` skill is skipped (Python unavailable), set `setup_artifacts.knowledge_graph_dir` to `null` and add a warning entry. This is non-fatal — downstream agents will fall back to direct source analysis.

## Error Handling

- If constitution template not found: Report error, cannot proceed
- If codebase access fails: Report specific access errors
