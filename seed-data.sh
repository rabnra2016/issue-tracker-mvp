#!/bin/bash
echo "=== Seeding Issue Tracker Database ==="

# Wait for backend to be ready
echo "Waiting for backend to be ready..."
sleep 5

# Create admin user
echo "Creating admin user..."
ADMIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@issuetracker.com",
    "password": "admin123",
    "name": "Admin User"
  }')

ADMIN_TOKEN=$(echo $ADMIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$ADMIN_TOKEN" ]; then
  echo "Failed to create admin user"
  exit 1
fi

echo "✓ Admin user created"

# Create demo project
echo "Creating demo project..."
PROJECT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"name": "Demo Project"}')

PROJECT_ID=$(echo $PROJECT_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)

if [ -z "$PROJECT_ID" ]; then
  echo "Failed to create project"
  exit 1
fi

echo "✓ Demo project created (ID: $PROJECT_ID)"

# Create sample issues
echo "Creating sample issues..."

curl -s -X POST http://localhost:8080/api/issues \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"projectId\": $PROJECT_ID,
    \"title\": \"Setup CI/CD pipeline\",
    \"description\": \"Configure automated testing and deployment with GitHub Actions\",
    \"priority\": \"HIGH\"
  }" > /dev/null

curl -s -X POST http://localhost:8080/api/issues \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"projectId\": $PROJECT_ID,
    \"title\": \"Implement user authentication\",
    \"description\": \"JWT-based authentication with password hashing - COMPLETED\",
    \"priority\": \"CRITICAL\",
    \"status\": \"CLOSED\"
  }" > /dev/null

curl -s -X POST http://localhost:8080/api/issues \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"projectId\": $PROJECT_ID,
    \"title\": \"Add real-time updates\",
    \"description\": \"Implement WebSocket for live issue updates - IN PROGRESS\",
    \"priority\": \"HIGH\",
    \"status\": \"IN_PROGRESS\"
  }" > /dev/null

curl -s -X POST http://localhost:8080/api/issues \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"projectId\": $PROJECT_ID,
    \"title\": \"Update API documentation\",
    \"description\": \"Add OpenAPI/Swagger documentation for all endpoints\",
    \"priority\": \"MEDIUM\"
  }" > /dev/null

curl -s -X POST http://localhost:8080/api/issues \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"projectId\": $PROJECT_ID,
    \"title\": \"Fix mobile responsiveness\",
    \"description\": \"Ensure UI works well on mobile devices\",
    \"priority\": \"LOW\"
  }" > /dev/null

echo "✓ 5 sample issues created"
echo ""
echo "=== Seed Data Complete! ==="
echo ""
echo "Login credentials:"
echo "  Email: admin@issuetracker.com"
echo "  Password: admin123"
echo ""
echo "Access the app at: http://localhost:4200"
