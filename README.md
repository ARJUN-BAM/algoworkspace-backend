# AlgoWorkspace Backend

A Spring Boot REST API for managing algorithm visualization workspaces and step-by-step execution snapshots.

## Project Overview

AlgoWorkspace is a visual dry-run notebook for LeetCode/DSA practice. Users manually trace algorithm execution step-by-step using draggable visual components (Array, Variable) on a canvas. Each step's state can be committed as a snapshot, allowing users to navigate through their trace history.

**Core Principle:** *"Edit instead of redraw, and snapshot instead of overwrite."*

## Tech Stack

- **Java:** 21
- **Spring Boot:** 4.1.0
- **Database:** PostgreSQL 17.6
- **ORM:** Hibernate 7.4.1
- **Build Tool:** Maven
- **Server Port:** 8081

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- PostgreSQL 17+ running on localhost:5432
- Database: `algoworkspace` with user `postgres` / password `rit123`

### Run Application
```bash
mvn spring-boot:run
```

Application will start on `http://localhost:8081`

### Run Tests
```bash
mvn test
```

## API Endpoints

### 1. Create Workspace
```http
POST /api/workspaces
Content-Type: application/json

{
  "name": "Two Sum Problem"
}
```

### 2. Create/Commit Step
```http
POST /api/workspaces/{id}/steps
Content-Type: application/json

{
  "snapshotJson": "{\"components\":[{\"id\":\"array-1\",\"type\":\"ARRAY\",\"values\":[2,7,11,15]}]}"
}
```

### 3. Get All Steps
```http
GET /api/workspaces/{id}/steps
```

### 4. Get Step by Number
```http
GET /api/workspaces/{id}/steps/{stepNumber}
```

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for complete API reference.

## Project Structure

```
src/main/java/com/algoworkspace/algoworkspace_backend/
├── controller/
│   └── WorkspaceController.java          # REST endpoints
├── service/
│   ├── WorkspaceService.java             # Workspace business logic
│   └── StepService.java                  # Step business logic + Prev/Next
├── repository/
│   ├── WorkspaceRepository.java          # Workspace JPA repository
│   └── StepRepository.java               # Step JPA repository
├── entity/
│   ├── Workspace.java                    # Workspace entity
│   └── Step.java                         # Step entity with snapshot JSON
├── validator/
│   └── SnapshotValidator.java            # Snapshot JSON validation
├── dto/
│   ├── WorkspaceCreateRequest.java       # Request DTOs
│   └── StepCreateRequest.java
└── AlgoworkspaceBackendApplication.java  # Main application
```

## Database Schema

### Table: workspaces
| Column     | Type      | Description                |
|------------|-----------|----------------------------|
| id         | BIGSERIAL | Primary key (auto-generated) |
| name       | VARCHAR   | Workspace name             |
| created_at | TIMESTAMP | Creation timestamp         |

### Table: steps
| Column        | Type      | Description                     |
|---------------|-----------|---------------------------------|
| id            | BIGSERIAL | Primary key (auto-generated)    |
| workspace_id  | BIGINT    | Foreign key to workspaces       |
| step_number   | INTEGER   | Auto-incremented step number    |
| snapshot_json | TEXT      | JSON snapshot of component state |
| committed_at  | TIMESTAMP | Commit timestamp                |

## Component Types (MVP)

### ARRAY Component
```json
{
  "id": "array-1",
  "type": "ARRAY",
  "values": [2, 7, 11, 15]
}
```

### VARIABLE Component
```json
{
  "id": "variable-1",
  "type": "VARIABLE",
  "name": "i",
  "value": 0
}
```

## Features Implemented

### ✅ Phase 1 - Backend Skeleton
- [x] Workspace and Step entities
- [x] JPA repositories with custom queries
- [x] CRUD service layer
- [x] 4 REST endpoints
- [x] Auto-incrementing step numbers per workspace
- [x] PostgreSQL integration

### ✅ Phase 2 - Snapshot Validation & Navigation
- [x] SnapshotValidator service with comprehensive validation
- [x] Validation for ARRAY and VARIABLE components
- [x] Detailed error messages for invalid snapshots
- [x] getPreviousStep() and getNextStep() helper methods
- [x] 400 Bad Request responses for validation errors
- [x] 16 passing unit tests

### 📋 Phase 3 - Angular Frontend (Next)
- [ ] Angular project setup
- [ ] TypeScript models
- [ ] HTTP service layer
- [ ] Component structure
- [ ] Basic UI layout

## Documentation

- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Complete REST API reference
- **[WORKFLOW_DOCUMENTATION.md](WORKFLOW_DOCUMENTATION.md)** - Workflow patterns and business rules
- **[PHASE2_SUMMARY.md](PHASE2_SUMMARY.md)** - Phase 2 implementation details

## Configuration

Configuration is managed in `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/algoworkspace
spring.datasource.username=postgres
spring.datasource.password=rit123

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Server Configuration
server.port=8081
```

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SnapshotValidatorTest

# Run with coverage
mvn test jacoco:report
```

**Test Results:**
- ✅ 17/17 tests passing
- ✅ Snapshot validation coverage: 100%
- ✅ Integration with PostgreSQL: Working

## Development Guidelines

### Adding New Component Types

1. Update `SnapshotValidator.java` with new validation method
2. Add test cases in `SnapshotValidatorTest.java`
3. Update API documentation
4. Update WORKFLOW_DOCUMENTATION.md

### Code Style
- Use Lombok annotations (@Data, @RequiredArgsConstructor)
- Follow Spring Boot naming conventions
- Keep controllers thin, logic in services
- Write tests for all validation logic

## Error Handling

All endpoints return appropriate HTTP status codes:
- `200 OK` - Successful GET
- `201 Created` - Successful POST
- `400 Bad Request` - Validation error
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## CORS

CORS is enabled for all origins to support frontend development.

## License

This project is for educational purposes (LeetCode/DSA practice tool).

## Contact

For questions or issues, refer to the documentation files or raise an issue.
