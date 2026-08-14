
# AlgoWorkspace Backend Workflow Documentation

## Overview

AlgoWorkspace follows a **manual dry-run** approach where users visually trace algorithm execution step-by-step. The backend stores workspace metadata and step snapshots without executing any code.

**Core Principle:** *"Edit instead of redraw, and snapshot instead of overwrite."*

---

## Workflow Patterns

### 1. Create Workspace Workflow

**Use Case:** Start a new algorithm visualization session

**Steps:**
1. User wants to trace an algorithm (e.g., "Two Sum Problem")
2. Frontend sends `POST /api/workspaces` with workspace name
3. Backend creates a new workspace record
4. Backend returns workspace with auto-generated ID
5. Frontend stores workspace ID for subsequent operations

**Sequence:**
```
Frontend                    Backend                     Database
   |                           |                            |
   |-- POST /workspaces ------>|                            |
   |    { name: "..." }        |                            |
   |                           |-- INSERT workspace ------->|
   |                           |<-- workspace.id = 1 -------|
   |<-- 201 Created ----------|                            |
   |    { id: 1, ... }         |                            |
```

---

### 2. Step Commit Workflow

**Use Case:** Save current canvas state as a step/snapshot

**Steps:**
1. User places/edits components on canvas (e.g., Array [2,7,11,15], Variable i=0)
2. User clicks "Commit Step" button
3. Frontend serializes current canvas state to JSON
4. Frontend sends `POST /api/workspaces/{id}/steps` with snapshot JSON
5. Backend validates workspace exists
6. Backend queries existing steps for this workspace
7. Backend calculates next step number (max stepNumber + 1, or 1 if no steps)
8. Backend creates and persists step with auto-incremented step number
9. Backend returns committed step with assigned step number
10. Frontend updates local step list and shows confirmation

**Sequence:**
```
Frontend                    Backend                     Database
   |                           |                            |
   | User edits canvas         |                            |
   | Clicks "Commit"           |                            |
   |                           |                            |
   |-- POST /workspaces/1/steps|                            |
   |    { snapshotJson }       |                            |
   |                           |-- SELECT steps ----------->|
   |                           |    WHERE workspace_id=1    |
   |                           |<-- steps: [1, 2] ---------|
   |                           |    (calculate next: 3)     |
   |                           |-- INSERT step ------------->|
   |                           |    stepNumber = 3          |
   |                           |<-- step.id = 10 ----------|
   |<-- 201 Created ----------|                            |
   |    { stepNumber: 3 }      |                            |
```

**Step Number Logic:**
```java
List<Step> existingSteps = getStepsByWorkspace(workspaceId);
int nextStepNumber = existingSteps.isEmpty() 
    ? 1 
    : existingSteps.get(existingSteps.size() - 1).getStepNumber() + 1;
```

---

### 3. Load Steps for Timeline Workflow

**Use Case:** Display all committed steps in the timeline UI

**Steps:**
1. Frontend loads workspace (on mount or after commit)
2. Frontend sends `GET /api/workspaces/{id}/steps`
3. Backend retrieves all steps for workspace, ordered by stepNumber ASC
4. Backend returns step list
5. Frontend renders timeline markers (S1, S2, S3...)

**Sequence:**
```
Frontend                    Backend                     Database
   |                           |                            |
   | Load workspace            |                            |
   |                           |                            |
   |-- GET /workspaces/1/steps-|                            |
   |                           |-- SELECT * FROM steps ---->|
   |                           |    WHERE workspace_id=1    |
   |                           |    ORDER BY stepNumber ASC |
   |                           |<-- [step1, step2, step3] --|
   |<-- 200 OK ---------------|                            |
   |    [ {...}, {...}, ... ]  |                            |
   |                           |                            |
   | Render timeline: S1 S2 S3 |                            |
```

---

### 4. Navigate to Step (Prev/Next) Workflow

**Use Case:** Restore canvas state from a specific step

**Steps:**
1. User clicks Prev or Next button
2. Frontend calculates target step number (currentStep ± 1)
3. Frontend sends `GET /api/workspaces/{id}/steps/{stepNumber}`
4. Backend retrieves step with matching workspace ID and step number
5. Backend returns step with snapshot JSON
6. Frontend **replaces entire canvas state** with snapshot data (not merge)
7. Frontend updates current step indicator

**Sequence:**
```
Frontend                    Backend                     Database
   |                           |                            |
   | Current: Step 2           |                            |
   | User clicks "Next"        |                            |
   | Target: Step 3            |                            |
   |                           |                            |
   |-- GET /workspaces/1/steps/3|                           |
   |                           |-- SELECT * FROM steps ---->|
   |                           |    WHERE workspace_id=1    |
   |                           |    AND stepNumber=3        |
   |                           |<-- step -------------------|
   |<-- 200 OK ---------------|                            |
   |    { snapshotJson }       |                            |
   |                           |                            |
   | Parse snapshotJson        |                            |
   | REPLACE canvas state      |                            |
   | (not merge!)              |                            |
```

**Important:** Full state replacement, not incremental updates.

---

## Data Flow Architecture

### Component State → Snapshot JSON

**Frontend Responsibility:**
```
Canvas Components (in-memory)
  ↓
Serialize to Snapshot JSON
  ↓
POST to /api/workspaces/{id}/steps
```

**Snapshot JSON Format:**
```json
{
  "step": 2,
  "components": [
    {
      "id": "array-1",
      "type": "ARRAY", 
      "values": [2, 7, 11, 15]
    },
    {
      "id": "variable-1",
      "type": "VARIABLE",
      "name": "i",
      "value": 1
    }
  ]
}
```

### Snapshot JSON → Component State

**Frontend Responsibility:**
```
GET /api/workspaces/{id}/steps/{stepNumber}
  ↓
Parse snapshotJson string
  ↓
Deserialize to Canvas Components
  ↓
Replace current canvas state
```

---

## Business Rules

### Step Numbering
1. Step numbers are **workspace-scoped** (each workspace has its own sequence)
2. Step numbers start at **1** (not 0)
3. Step numbers are **sequential** and **auto-incremented**
4. Step numbers are **immutable** once committed
5. No gaps in step numbers (no delete operation in MVP)

### Workspace-Step Relationship
- One Workspace → Many Steps (1:N)
- Steps cannot exist without a Workspace
- Steps are ordered by stepNumber within a workspace

### Snapshot Integrity
- Snapshots are **immutable** once committed
- Old snapshots are **never modified** or deleted
- Each commit creates a **new** step record
- Snapshot JSON is stored as-is (no backend parsing/validation in Phase 1)

---

## Common Scenarios

### Scenario 1: Empty Workspace
```
1. POST /workspaces → { id: 1, name: "Binary Search" }
2. GET /workspaces/1/steps → [] (empty array)
3. Frontend shows "Empty Workspace" placeholder
```

### Scenario 2: First Step Commit
```
1. User adds Array [1,2,3] and Variable mid=1
2. POST /workspaces/1/steps → { stepNumber: 1 }
3. GET /workspaces/1/steps → [{ stepNumber: 1 }]
4. Timeline shows: S1
```

### Scenario 3: Multiple Steps
```
1. User commits 5 steps
2. GET /workspaces/1/steps → [S1, S2, S3, S4, S5]
3. User navigates: S1 → S2 → S3
4. Each navigation: GET /workspaces/1/steps/{stepNumber}
5. Canvas state replaces completely each time
```

### Scenario 4: Commit While Viewing Past Step
```
Current: User is viewing Step 2 (via Prev button)
Action: User modifies canvas and clicks "Commit"

Option A (Current Implementation - Phase 1):
- POST /workspaces/1/steps creates Step 6 (appends to end)
- Does not overwrite Steps 3-5
- No branching support yet

Option B (Future - Post-MVP):
- Could implement branching
- Or prompt user to return to latest step before committing
```

---

## State Management

### Backend State
- **Persistent:** All workspaces and steps in PostgreSQL
- **Immutable:** Steps never modified after creation
- **Stateless:** No session state, each request is independent

### Frontend State (Expected)
- **Current Workspace ID:** Stored in component state
- **Current Step Number:** Tracked for Prev/Next navigation
- **Canvas Components:** In-memory, uncommitted state
- **Committed Steps List:** Cached from GET /steps

---

## Transaction Boundaries

### Create Step Transaction
```java
@Transactional
public Step createStep(Long workspaceId, String snapshotJson) {
    // 1. Query existing steps (READ)
    List<Step> existingSteps = stepRepository.findByWorkspaceId...();
    
    // 2. Calculate next step number (COMPUTE)
    int nextStepNumber = ...;
    
    // 3. Create and save step (WRITE)
    Step step = new Step(...);
    return stepRepository.save(step);
}
```

**Isolation:** Prevents race conditions when multiple clients commit simultaneously.

---

## Performance Considerations

### Query Optimization
- Steps are indexed by `workspace_id` and `step_number`
- `ORDER BY stepNumber ASC` is efficient (leverages index)
- Workspace validation uses primary key lookup (fast)

### Scalability Notes
- Each workspace is independent (no cross-workspace queries)
- Step commits are O(n) where n = number of existing steps (for max calculation)
- Future optimization: Maintain `last_step_number` in workspace table

---

## Error Scenarios

### Workspace Not Found
```
Request: POST /workspaces/999/steps
Response: 500 Internal Server Error
Message: "Workspace not found with id: 999"
```

### Step Not Found
```
Request: GET /workspaces/1/steps/99
Response: 500 Internal Server Error
Message: "Step not found with number: 99 for workspace: 1"
```

### Invalid Snapshot JSON (Phase 2+)
```
Request: POST /workspaces/1/steps
Body: { "snapshotJson": "not-valid-json" }
Response: 400 Bad Request
Message: "Invalid snapshot format"
```

---

## Phase Roadmap

### ✅ Phase 1 (Complete)
- Basic CRUD endpoints
- Auto-incrementing step numbers
- Snapshot storage (no validation)

### 🔄 Phase 2 (In Progress)
- Snapshot JSON validation
- Prev/Next service logic
- Integration tests

### 📋 Phase 3+ (Future)
- Frontend Angular skeleton
- Drag & drop canvas
- Timeline visualization
- Full integration

---

## Development Guidelines

### Adding New Component Types
1. Update snapshot JSON schema (frontend)
2. Add validation rules (backend Phase 2+)
3. Update this documentation

### Testing Workflows
1. Use Postman or curl for manual testing
2. Create workspaces before creating steps
3. Test step number auto-increment with multiple commits
4. Verify step ordering with GET /steps

### Debugging Tips
- Enable SQL logging: `spring.jpa.show-sql=true` (already enabled)
- Check step numbers are sequential
- Verify snapshot JSON is stored as TEXT (not CLOB/OID)
- Use `@Transactional` for write operations
