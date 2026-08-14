# AlgoWorkspace Backend API Documentation

## Base URL
```
http://localhost:8081/api
```

## Overview
AlgoWorkspace backend provides REST APIs for managing algorithm visualization workspaces and their step-by-step execution snapshots. Users manually trace algorithm execution by creating visual components (Array, Variable) and committing snapshots at each step.

---

## API Endpoints

### 1. Create Workspace

**Endpoint:** `POST /api/workspaces`

**Description:** Creates a new workspace for algorithm visualization.

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Two Sum Problem"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "Two Sum Problem",
  "createdAt": "2026-08-13T08:26:56.8009048"
}
```

**Example:**
```bash
curl -X POST http://localhost:8081/api/workspaces \
  -H "Content-Type: application/json" \
  -d '{"name":"Two Sum Problem"}'
```

---

### 2. Create/Commit Step

**Endpoint:** `POST /api/workspaces/{id}/steps`

**Description:** Commits a new step (snapshot) to a workspace. The step number is auto-incremented based on existing steps.

**Path Parameters:**
- `id` (Long) - Workspace ID

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "snapshotJson": "{\"step\":1,\"components\":[{\"id\":\"array-1\",\"type\":\"ARRAY\",\"values\":[2,7,11,15]},{\"id\":\"variable-1\",\"type\":\"VARIABLE\",\"name\":\"i\",\"value\":0}]}"
}
```

**Snapshot JSON Structure (stringified):**
```json
{
  "step": 1,
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
      "value": 0
    }
  ]
}
```

**Component Types:**
- **ARRAY**: `{ "id": "string", "type": "ARRAY", "values": [numbers] }`
- **VARIABLE**: `{ "id": "string", "type": "VARIABLE", "name": "string", "value": number }`

**Response:** `201 Created`
```json
{
  "id": 1,
  "workspaceId": 1,
  "stepNumber": 1,
  "snapshotJson": "{\"step\":1,\"components\":[...]}",
  "committedAt": "2026-08-13T08:27:09.7433571"
}
```

**Error Responses:**
- `404 Not Found` - Workspace does not exist
- `500 Internal Server Error` - Invalid snapshot JSON format

**Example:**
```bash
curl -X POST http://localhost:8081/api/workspaces/1/steps \
  -H "Content-Type: application/json" \
  -d '{"snapshotJson":"{\"step\":1,\"components\":[{\"id\":\"array-1\",\"type\":\"ARRAY\",\"values\":[2,7,11,15]}]}"}'
```

---

### 3. Get All Steps

**Endpoint:** `GET /api/workspaces/{id}/steps`

**Description:** Retrieves all steps for a workspace, ordered by step number ascending.

**Path Parameters:**
- `id` (Long) - Workspace ID

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "workspaceId": 1,
    "stepNumber": 1,
    "snapshotJson": "{\"step\":1,\"components\":[...]}",
    "committedAt": "2026-08-13T08:27:09.7433570"
  },
  {
    "id": 2,
    "workspaceId": 1,
    "stepNumber": 2,
    "snapshotJson": "{\"step\":2,\"components\":[...]}",
    "committedAt": "2026-08-13T08:27:19.9238520"
  }
]
```

**Error Responses:**
- `404 Not Found` - Workspace does not exist
- `200 OK` with empty array `[]` - Workspace exists but has no steps

**Example:**
```bash
curl http://localhost:8081/api/workspaces/1/steps
```

---

### 4. Get Step by Number

**Endpoint:** `GET /api/workspaces/{id}/steps/{stepNumber}`

**Description:** Retrieves a specific step by its step number within a workspace.

**Path Parameters:**
- `id` (Long) - Workspace ID
- `stepNumber` (Integer) - Step number (1-indexed)

**Response:** `200 OK`
```json
{
  "id": 1,
  "workspaceId": 1,
  "stepNumber": 1,
  "snapshotJson": "{\"step\":1,\"components\":[...]}",
  "committedAt": "2026-08-13T08:27:09.7433570"
}
```

**Error Responses:**
- `404 Not Found` - Workspace does not exist
- `500 Internal Server Error` - Step number not found for workspace

**Example:**
```bash
curl http://localhost:8081/api/workspaces/1/steps/1
```

---

## Data Models

### Workspace
```java
{
  "id": Long,              // Auto-generated unique identifier
  "name": String,          // Workspace name (e.g., "Two Sum Problem")
  "createdAt": LocalDateTime  // Timestamp of creation
}
```

### Step
```java
{
  "id": Long,              // Auto-generated unique identifier
  "workspaceId": Long,     // Foreign key to Workspace
  "stepNumber": Integer,   // Auto-incremented step number (1, 2, 3...)
  "snapshotJson": String,  // JSON string containing snapshot data
  "committedAt": LocalDateTime  // Timestamp of commit
}
```

---

## Error Handling

All endpoints follow standard HTTP status codes:
- `200 OK` - Successful GET request
- `201 Created` - Successful POST request
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server-side error

Error responses include a timestamp and error message:
```json
{
  "timestamp": "2026-08-13T03:00:11.694Z",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/workspaces/1/steps"
}
```

---

## CORS Configuration

CORS is enabled for all origins (`*`) to support frontend development.

---

## Database Schema

### Table: `workspaces`
```sql
CREATE TABLE workspaces (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

### Table: `steps`
```sql
CREATE TABLE steps (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    step_number INTEGER NOT NULL,
    snapshot_json TEXT NOT NULL,
    committed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_workspace FOREIGN KEY (workspace_id) 
        REFERENCES workspaces(id)
);
```

---

## Technical Stack

- **Framework:** Spring Boot 4.1.0
- **Java Version:** 21
- **Database:** PostgreSQL 17.6
- **ORM:** Hibernate 7.4.1
- **Server Port:** 8081

---

## Notes

1. **Step Numbering:** Step numbers are auto-incremented per workspace. The service automatically calculates the next step number based on existing steps.

2. **Snapshot JSON Format:** The snapshot JSON is stored as a TEXT column. It must be a valid JSON string containing a `step` field and a `components` array.

3. **Timestamp Handling:** Both `createdAt` and `committedAt` timestamps are automatically set on entity creation using `@PrePersist`.

4. **Transactions:** Step creation is wrapped in a `@Transactional` to ensure atomic operations.

5. **DDL Auto-Update:** Database schema is automatically managed by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).
