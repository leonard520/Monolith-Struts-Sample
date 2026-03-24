---
name: modernize-design
description: Run the design phase — dynamically dispatch research sub-agents based on project type, then generate the specification document.
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
- `mode`: "upgrade" | "rewrite"
- `setup_artifacts`: FoundationAgent artifacts object — includes a `knowledge_graph` entry (e.g., at `artifacts.knowledge_graph`) pointing to the knowledge graph directory (may be `null` if Python was unavailable; treat as optional, skip silently if null)

## ⚠️ Critical Constraint: NO Direct Codebase Exploration

**This agent MUST NOT read source code files directly.**

- **Phase 1**: Dispatch research sub-agents to do all codebase analysis. Wait for them to complete.
- **Phase 2**: Dispatch SpecWriterAgent to generate spec. It reads the research files.
- **Your role**: Orchestrate only. Dispatch sub-agents. Collect results. Move to next phase.

## Responsibility

This agent runs the **Design Phase** directly:

1. **Research** - Dynamically dispatch research sub-agents (2 core always + 0–N optional based on project context) to gather structured analysis into research files. The total number of agents is not fixed — it depends on the tech stack, project type, and migration mode.
2. **Specification** - Dispatch SpecWriterAgent to read research files and generate spec.md

> **⚠️ DO NOT dispatch another DesignAgent.** You ARE the DesignAgent. Execute steps 1 and 2 yourself.

## Prerequisites

- FoundationAgent completed successfully
- Constitution and knowledge graph available
- Migration mode confirmed by user

---

## Execution Flow

> **ℹ️ Research agent count is dynamic, not fixed.** Phase 1a always dispatches 2 core agents. Phase 1b dispatches 0–N optional agents based on project type, tech stack, and migration mode. Total research agents dispatched per run varies.

### Phase 1a: Core Research (Always Run)

Read the Research skill at `$KIT_ROOT/skills/research/SKILL.md`.

Dispatch the **2 Core agents** in parallel:
- **project-structure** → `$MODERNIZE_DIR/research/project-structure.md`
- **tech-stack** → `$MODERNIZE_DIR/research/tech-stack.md`

Use the prompts defined in the skill's "Core Agents" section.

> ⚡ Issue both `#runSubagent` calls in a **single response** (parallel). Wait for both to complete.

After both complete: use `read_agent` to retrieve each agent's output, then **write the results yourself** to the corresponding research files.

---

### Phase 1b: Dynamic Research (Select from Pool)

After core research files are written:

1. **Read** `$MODERNIZE_DIR/research/project-structure.md` and `$MODERNIZE_DIR/research/tech-stack.md`
2. **Read** the "Optional Agent Pool" section in `$KIT_ROOT/skills/research/SKILL.md`
3. **Evaluate** each optional agent's "When relevant" condition against:
   - The project structure and tech stack findings
   - The `feature_description` and `mode` from user input
   - The `setup_artifacts` available (e.g., knowledge graph existence)
4. **Select** all agents whose conditions are met
5. **Dispatch** all selected agents in **one parallel batch**

> ⚡ Issue all selected `#runSubagent` calls in a **single response**. Do NOT wait for any agent to finish before spawning the next.

After all complete: use `read_agent` to retrieve each agent's output, then **write the results yourself** to the corresponding research files under `$MODERNIZE_DIR/research/`.

---

### Phase 2: Specification Writing (SpecWriterAgent)

After all research files exist, delegate to `$KIT_ROOT/agents/modernize-spec-writer.agent.md`:

```yaml
subagent: SpecWriterAgent
agent_md: $KIT_ROOT/agents/modernize-spec-writer.agent.md
input:
  kit_root: $KIT_ROOT
  modernize_dir: $MODERNIZE_DIR
  feature_description: "[feature_description from input]"
  mode: "[mode from input]"
  constitution_file: $MODERNIZE_DIR/constitution.md
  research_dir: $MODERNIZE_DIR/research
```

The SpecWriterAgent handles:
- Reading pre-built research files (no re-exploration needed)
- Branch short name generation
- Requirement ID assignment (REQ-XXX format)
- Section-by-section spec writing
- Specification quality validation
- [NEEDS CLARIFICATION] marker handling (max 3)

**Expected Outputs:**
- `FEATURE_DIR/spec.md` - Main specification
- `FEATURE_DIR/checklists/requirements.md` - Quality checklist

---

## Completion Criteria

- [ ] Core research complete (project-structure + tech-stack)
- [ ] Dynamic research complete (selected optional agents finished or timed out)
- [ ] Specification complete with unique REQ-XXX IDs
- [ ] Specification passes quality validation

## Output Format

```yaml
design_complete: true/false
mode: "upgrade" | "rewrite"

artifacts:
  research_dir: "$MODERNIZE_DIR/research"
  specification: "FEATURE_DIR/spec.md"

research_agents:
  core: ["project-structure", "tech-stack"]
  selected: ["data-model", "migration-risks", "api-surface", ...]  # dynamically chosen
  skipped: ["ui-components", "build-bundle", ...]                   # not relevant

summary:
  total_requirements: N

errors: []
warnings: []
```

## Error Handling

- If research skill not found at `$KIT_ROOT/skills/research/SKILL.md`: Report error, cannot proceed
- If spec template not found: Report error, cannot proceed
- If knowledge graph missing: architecture-summary agent will produce partial output — proceed normally, log warning
- If research files missing after agents complete: Proceed with partial data, log warning
