---
name: modernize-spec-writer
description: Generate spec.md from pre-built research files. Called by DesignAgent after Explore phase.
user-invocable: false
disable-model-invocation: false
model: Claude Opus 4.6
---

## User Input

```text
$ARGUMENTS
```

**Required Input:**
- `kit_root`: Path to the kit installation root
- `modernize_dir`: Path to modernize directory (used for branch number lookup: `$MODERNIZE_DIR/[0-9]+-<short-name>`)
- `feature_description`: Natural language description of the migration
- `mode`: "upgrade" | "rewrite" (included in spec header)
- `constitution_file`: Path to constitution.md
- `research_dir`: Path to research/ directory (pre-built by Explore agents)

## ⚠️ Critical Constraint: NO Direct Codebase Exploration

**Do NOT read project source files directly** (source code, build files, config files, etc.).
All codebase analysis is already in the research files under `$RESEARCH_DIR/`.

## Role

Read pre-built research files and generate spec.md section by section.

---

## Execution Flow

### Step 1: Load Inputs

Set variables from inputs:
```
CONSTITUTION_FILE = $constitution_file
RESEARCH_DIR      = $research_dir
MODERNIZE_DIR        = $modernize_dir
```

Read the specification skill for **format and content requirements** (sections, REQ-XXX format, NEEDS CLARIFICATION rules, Success Criteria guidelines).

> **This agent overrides the following steps from the spec skill:**
> - **Step 4** (read knowledge-graph.json) → replaced by `$RESEARCH_DIR/architecture-summary.md` already pre-built by Explore agent
> - **Step 7** (write full spec at once) → replaced by section-by-section writing in Step 3 below

```bash
cat $KIT_ROOT/skills/specification/SKILL.md
```

Read all context files:

```bash
cat $CONSTITUTION_FILE          # = $constitution_file input
cat $KIT_ROOT/skills/templates/spec-template.md

# Core research files (required)
cat $RESEARCH_DIR/project-structure.md       # includes functional domain list; $RESEARCH_DIR = $research_dir input
cat $RESEARCH_DIR/tech-stack.md

# Additional structured research files (loaded if present; may be absent for some projects)
if [ -f "$RESEARCH_DIR/data-model.md" ]; then
  cat "$RESEARCH_DIR/data-model.md"          # includes key entities summary
fi
if [ -f "$RESEARCH_DIR/architecture-summary.md" ]; then
  cat "$RESEARCH_DIR/architecture-summary.md"  # distilled from knowledge-graph.json
fi
# All additional research files (optional, load all that exist)
for f in $(ls $RESEARCH_DIR/*.md 2>/dev/null | grep -v -E '(project-structure|tech-stack|data-model|architecture-summary)\.md$'); do
  echo "=== $f ===" && cat "$f"
done
# Examples of optional files that may exist:
# guidelines.md, migration-risks.md, api-surface.md, integration-points.md,
# infrastructure.md, ui-components.md, state-routing.md, build-bundle.md
```

### Step 2: Determine FEATURE_DIR and SPEC_FILE

Follow the branch creation steps from the specification skill:

```bash
# a. Fetch remote branches
git fetch --all --prune

# b. Find highest existing number across all 3 sources for the chosen short-name
git ls-remote --heads origin | grep -E 'refs/heads/[0-9]+-<short-name>$'
git branch | grep -E '^[* ]*[0-9]+-<short-name>$'
ls $MODERNIZE_DIR | grep -E '^[0-9]+-<short-name>$'

# c. Use N+1 and run the script (--json outputs BRANCH_NAME and SPEC_FILE paths)
bash $KIT_ROOT/skills/scripts/bash/create-new-feature.sh --json "$feature_description" --number N --short-name "short-name"
```

Set:
- `SPEC_FILE` and `FEATURE_DIR` from the script's JSON output
- `CHECKLISTS_DIR = $FEATURE_DIR/checklists`

### Step 3: Write Spec Section by Section

Write each section immediately after generating it — do NOT buffer the entire spec and write at once.

**a. Header + Scope Baseline**
1. Generate the spec header (title, date, governed-by, mode from `$mode` input) and `## Scope Baseline` section.
2. Write/create `$SPEC_FILE` with this content.

**b. User Scenarios & Testing**

Write the `## User Scenarios & Testing` section header first.

Use the functional domain list from `$RESEARCH_DIR/project-structure.md`. For each domain:
1. Generate 1–3 user stories (P1/P2/P3 priority, Given-When-Then format).
2. Immediately append to `$SPEC_FILE`.

After all domains:
1. Generate edge cases section.
2. Append to `$SPEC_FILE`.

**c. Requirements**

First write the section header: append `## Requirements\n\n### Functional Requirements\n` to `$SPEC_FILE`.

Then for each business domain from `$RESEARCH_DIR/project-structure.md` (the functional domain list identified during research):
1. Write a domain subsection header — **only the domain name, NO REQ ID ranges** (e.g., `#### Authentication`, not `#### Authentication (REQ-041 through REQ-055)`).
2. Generate 2–4 REQ-XXX items for that domain (referencing relevant constitution principles).
3. Immediately append those REQ items to `$SPEC_FILE`.

Then Non-Functional Requirements:
1. Generate 4–6 NFR-XXX items (performance, security, availability, etc.).
2. Append to `$SPEC_FILE`.

Then Key Entities — use the key entities summary from `$RESEARCH_DIR/data-model.md` directly:
1. Format and append the top 6–8 entities to `$SPEC_FILE`.

**d. Success Criteria**
1. Generate `## Success Criteria` section with measurable outcomes from constitution quality gates.
2. Append to `$SPEC_FILE`.

### Step 4: Generate Quality Checklist

```bash
mkdir -p $CHECKLISTS_DIR
cat > $CHECKLISTS_DIR/requirements.md << EOF
# Requirements Quality Checklist

## Requirement ID Coverage
- [ ] All requirements use REQ-XXX format
- [ ] IDs are unique and sequential

## Testability
- [ ] Every requirement is independently testable
- [ ] Acceptance criteria are concrete (Given-When-Then)

## Completeness
- [ ] Scope Baseline section complete
- [ ] User Scenarios prioritized (P1, P2, P3)
- [ ] Functional requirements cover all in-scope items
- [ ] Success criteria are measurable

## Constitution Alignment
- [ ] All constitution principles referenced
- [ ] Migration mode constraints respected
EOF
```

### Step 5: Validate

```bash
for section in "Scope Baseline" "User Scenarios" "Requirements" "Success Criteria"; do
    grep -q "$section" $SPEC_FILE && echo "✅ $section" || echo "❌ MISSING: $section"
done
echo "Requirements: $(grep -c 'REQ-[0-9]' $SPEC_FILE)"
echo "Clarifications: $(grep -c 'NEEDS CLARIFICATION' $SPEC_FILE) (max 3)"
```

---

## Completion Criteria

- [ ] `FEATURE_DIR/spec.md` exists with all 4 mandatory sections
- [ ] All requirements use REQ-XXX format with unique IDs
- [ ] Maximum 3 [NEEDS CLARIFICATION] markers
- [ ] `checklists/requirements.md` generated
