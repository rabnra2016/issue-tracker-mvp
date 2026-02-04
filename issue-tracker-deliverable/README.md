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
