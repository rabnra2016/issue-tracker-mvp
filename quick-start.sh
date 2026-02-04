#!/bin/bash
echo "===================================="
echo "Issue Tracker MVP - Quick Start"
echo "===================================="
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
  echo "❌ Docker is not running. Please start Docker Desktop."
  exit 1
fi

echo "✓ Docker is running"
echo ""

# Start backend
echo "Starting backend and database..."
docker-compose up -d

echo "Waiting for backend to be ready (15 seconds)..."
sleep 15

# Seed data
echo ""
echo "Seeding demo data..."
./seed-data.sh

echo ""
echo "===================================="
echo "✅ Backend Ready!"
echo "===================================="
echo ""
echo "Backend API: http://localhost:8080"
echo "Health Check: http://localhost:8080/api/health"
echo ""
echo "Demo Login Credentials:"
echo "  Email: admin@issuetracker.com"
echo "  Password: admin123"
echo ""
echo "To start the frontend:"
echo "  cd frontend"
echo "  ng serve"
echo "  Open http://localhost:4200"
echo ""
echo "To view backend logs:"
echo "  docker-compose logs -f backend"
echo ""
echo "To stop everything:"
echo "  docker-compose down"
echo ""
