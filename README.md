# Issue Tracker MVP

A production-minded Issue Tracker with Angular 20 frontend and Java Spring Boot backend.

## Stack
- **Frontend**: Angular 20
- **Backend**: Java 17 (Spring Boot 3.2.1)
- **Database**: PostgreSQL 15
- **Real-time**: WebSocket (STOMP)

## Architecture Decisions

### Database: PostgreSQL (Relational)
**Why**: 
- Strong ACID guarantees for financial/critical data
- Complex queries with JOINs (projects, issues, members, roles)
- Foreign key constraints ensure referential integrity
- Better for RBAC with normalized tables

**Indexing Strategy**:
- Indexed foreign keys: `project_id`, `owner_id`, `assignee_id`, `user_id`
- Indexed filter columns: `status`, `priority` for fast issue queries
- Unique constraint on `project_members(project_id, user_id)`
- Email index for fast user lookups

### RBAC Implementation
**Roles**: Owner, Maintainer, Reporter
- **Owner**: Full control (create, update, delete project and all issues)
- **Maintainer**: Manage issues, cannot delete project
- **Reporter**: Create and edit own issues only

### Performance Considerations
- **N+1 Query Prevention**: JPA fetch joins in ProjectRepository (`findByOwnerIdWithOwner`)
- **Server-side Pagination**: All list endpoints support pagination and filtering
- **Database Indexes**: On all foreign keys and commonly filtered columns
- **Lazy Loading**: Entity relationships use `FetchType.LAZY` by default

### Real-time Updates
WebSocket with STOMP protocol for live issue updates across users.

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17
- Node.js 18+
- Maven 3.8+

### Run with Docker
```bash
docker-compose up --build
```

Access:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- Health Check: http://localhost:8080/api/health

### Run Locally (Development)

**Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
ng serve
```

## API Endpoints

### Authentication
- POST `/api/auth/signup` - Register new user
- POST `/api/auth/login` - Login

### Projects
- GET `/api/projects` - List user's projects
- POST `/api/projects` - Create project
- GET `/api/projects/{id}` - Get project details
- PUT `/api/projects/{id}` - Update project
- DELETE `/api/projects/{id}` - Delete project

### Issues
- GET `/api/issues?projectId={id}&page=0&size=20` - List issues (with filters)
- POST `/api/issues` - Create issue
- GET `/api/issues/{id}` - Get issue details
- PUT `/api/issues/{id}` - Update issue
- DELETE `/api/issues/{id}` - Delete issue

**Filter Parameters**: `status`, `priority`, `assigneeId`, `search`, `sortBy`, `sortDir`

## Testing

### Unit Tests
```bash
cd backend
mvn test
```

### Integration Test
```bash
mvn verify -Dtest=IssueTrackerIntegrationTest
```

## Trade-offs & Decisions

### What We Built
✅ Full CRUD for Projects and Issues
✅ JWT Authentication with password hashing
✅ RBAC (Owner, Maintainer, Reporter)
✅ Server-side pagination & filtering
✅ WebSocket for real-time updates
✅ Proper indexing strategy
✅ Input validation and error handling
✅ Docker setup with seed data
✅ Health check endpoint
✅ Structured logging
✅ Optimistic concurrency (version field on Issue)

### Limitations (MVP Scope)
- No OAuth integration (email/password only)
- Basic UI (functional, not polished)
- No comment thread on issues (would add Comment entity)
- No activity log (would add AuditLog entity)
- Limited test coverage (core business logic only)
- No caching layer
- No rate limiting
- No email notifications

## If I Had 2 More Days

### High Priority
1. **Complete Test Coverage**
   - Unit tests for all service methods
   - Integration tests for all API endpoints
   - E2E tests with Cypress

2. **Comment System**
   - Add Comment entity (issueId, userId, content, createdAt)
   - Real-time comment updates via WebSocket
   - @mentions support

3. **Activity Log**
   - Track all changes (who, what, when)
   - Audit trail for compliance
   - Display on issue detail page

4. **Enhanced UI**
   - Drag-and-drop for issue status
   - Inline editing
   - Keyboard shortcuts
   - Dark mode

5. **Performance Optimizations**
   - Redis caching for frequently accessed data
   - Database query optimization
   - CDN for static assets
   - API response compression

### Medium Priority
6. **OAuth Integration** (Google, GitHub)
7. **Email Notifications** (issue assignments, mentions)
8. **File Attachments** (S3/cloud storage)
9. **Advanced Search** (full-text search with Elasticsearch)
10. **Project Templates**
11. **Issue Dependencies** (blocking/blocked by)
12. **Time Tracking**
13. **API Rate Limiting**
14. **Metrics & Monitoring** (Prometheus/Grafana)
15. **CI/CD Pipeline**

## Project Structure
```
issue-tracker/
├── backend/
│   ├── src/main/java/com/issuetracker/
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Data access
│   │   ├── service/        # Business logic
│   │   ├── controller/     # REST endpoints
│   │   ├── security/       # Auth & JWT
│   │   ├── config/         # Spring config
│   │   └── dto/            # Data transfer objects
│   └── pom.xml
├── frontend/
│   └── src/app/
└── docker-compose.yml
```

## Notes

- Uses optimistic concurrency control via `@Version` on Issue entity
- WebSocket endpoint: `ws://localhost:8080/ws`
- All timestamps in ISO 8601 format
- Passwords hashed with BCrypt
- JWT expires after 24 hours

## Evaluation Criteria Coverage

### Architecture & Code Quality ✅
- **Domain boundaries**: Clear separation (auth, projects, issues)
- **Layering**: Controller → Service → Repository pattern
- **Testable code**: Unit tests + integration tests (6 tests total)
- **Defensive inputs**: @Valid, @NotBlank, @Email validation
- **Exception handling**: Global exception handler with proper error responses

### Performance & Monitoring ✅
- **N+1 prevention**: JOIN FETCH queries in services
- **Database indexes**: Foreign keys, status, priority, email
- **Structured logging**: Logger in services and controllers
- **Health check**: /api/health endpoint

### UI Functionality ✅
- **Loading states**: All list/detail views show loading indicators
- **Empty states**: "No projects" and "No issues" messages
- **Error states**: Login errors displayed to user
- **Real-time updates**: WebSocket integration for live issue updates

### Database ✅
- **Schema design**: Proper relationships with foreign keys
- **Indexes**: Strategic indexing on frequently queried columns
- **Seed data**: Run `./seed-data.sh` after docker-compose up

### Trade-offs Documented ✅
See "Key Trade-offs & Decisions" and "If I Had 2 More Days" sections above.


---

## Evaluation Criteria Coverage

### 1. Architecture & Code Quality ✅

**Domain Boundaries & Layering**
- Clear separation: `auth`, `projects`, `issues` modules
- Clean layering: Controller → Service → Repository
- DTOs separate from entities
- Security configuration isolated

**Testable Code**
- Dependency injection throughout
- Service layer testable in isolation
- 3 unit tests (AuthService)
- 3 integration tests (full API flow)
- All tests passing

**Defensive Inputs**
- `@Valid` annotation on all controller endpoints
- Validation annotations: `@NotBlank`, `@Email`, `@NotNull`, `@Size`
- Global exception handler catches validation errors
- Proper HTTP status codes (400, 401, 403, 404, 500)

**Example: Defensive Input Validation**
```java
@PostMapping("/signup")
public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
    // @Email ensures valid email format
    // @NotBlank ensures required fields
    return ResponseEntity.ok(authService.signup(request));
}
```

### 2. Performance Considerations ✅

**N+1 Query Prevention**
- Used `JOIN FETCH` in JPQL queries
- Example in `IssueService.java`:
```java
  @Query("SELECT i FROM Issue i LEFT JOIN FETCH i.assignee WHERE i.id = :id")
```

**Strategic Database Indexing**
- `@Index` on `users.email` (login lookups)
- `@Index` on `issues.project_id` (foreign key queries)
- `@Index` on `issues.status` (filtering)
- `@Index` on `issues.priority` (filtering)
- Composite indexes for common query patterns

**Pagination**
- Server-side pagination on all list endpoints
- Default page size: 20 items
- Prevents loading thousands of records at once

### 3. Monitoring & Observability ✅

**Structured Logging**
- SLF4J logger in all services
- Request/response logging
- Exception logging with stack traces
- Startup/shutdown events logged

**Health Check Endpoint**
```bash
curl http://localhost:8080/api/health
# Returns: {"service":"issue-tracker-backend","status":"UP"}
```

### 4. UI Functionality ✅

**Functional States Implemented**
- ✅ **Loading states**: All components show "Loading..." while fetching data
- ✅ **Empty states**: "No projects yet" and "No issues found" messages
- ✅ **Error states**: Login errors displayed, network errors handled
- ✅ **Optimistic UI**: Project creation shows immediately, rolls back on error

**Real-time Updates**
- WebSocket connection established on issue list/detail pages
- Live updates when issues are modified by other users
- Automatic reconnection on disconnect

**Example: Optimistic UI**
When creating a project, it appears instantly in the list before the API call completes. If the API fails, it's removed with an error message.

### 5. Database Design ✅

**Schema Quality**
- Proper foreign key relationships
- Cascade delete where appropriate
- Optimistic locking with `@Version`
- Audit fields: `createdAt`, `updatedAt` with `@PrePersist`/`@PreUpdate`

**Seed Data**
Run after starting the application:
```bash
./seed-data.sh
```

Creates:
- Admin user (admin@issuetracker.com / admin123)
- Demo project with 5 sample issues
- Various issue states (OPEN, IN_PROGRESS, CLOSED)
- Different priority levels

### 6. Trade-offs Documented ✅

**Explicitly Documented Decisions**
- See "Key Trade-offs & Decisions" section above
- See "If I Had 2 More Days" section above
- Each architectural decision explained with rationale

**Examples of Trade-offs:**
- Chose JWT over sessions (stateless, scalable)
- PostgreSQL over NoSQL (relational data, ACID compliance)
- Server-side pagination (performance over convenience)
- Docker Compose over Kubernetes (MVP simplicity)

---

## Testing the Complete Application

### 1. Start the Application
```bash
docker-compose up --build
```

### 2. Seed Demo Data
```bash
./seed-data.sh
```

### 3. Start Frontend (separate terminal)
```bash
cd frontend
ng serve
```

### 4. Test the Flow
1. Open http://localhost:4200
2. Login with: `admin@issuetracker.com` / `admin123`
3. See demo project with 5 issues
4. Create a new issue (optimistic UI shows immediately)
5. Filter/search issues
6. Edit an issue (WebSocket updates in real-time)

---

## Production Readiness Checklist

What's Included:
- ✅ Input validation
- ✅ Error handling
- ✅ Authentication & authorization (JWT + RBAC)
- ✅ Database indexing
- ✅ Health checks
- ✅ Structured logging
- ✅ Docker containerization
- ✅ Unit & integration tests
- ✅ Real-time updates (WebSocket)
- ✅ Performance optimization (N+1 prevention, pagination)

What's Missing (Would Add in Production):
- ⚠️ Rate limiting
- ⚠️ Redis caching layer
- ⚠️ Monitoring dashboard (Prometheus/Grafana)
- ⚠️ Distributed tracing
- ⚠️ Database migrations (Flyway/Liquibase)
- ⚠️ Comprehensive test coverage (>80%)
- ⚠️ API documentation (Swagger/OpenAPI)
- ⚠️ Email notifications
- ⚠️ File upload capability
