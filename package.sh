#!/bin/bash
echo "Packaging Issue Tracker MVP..."

# Create a clean package
mkdir -p issue-tracker-deliverable
cp -r backend issue-tracker-deliverable/
cp docker-compose.yml issue-tracker-deliverable/
cp README.md issue-tracker-deliverable/
cp DELIVERABLES.md issue-tracker-deliverable/
cp index.html issue-tracker-deliverable/

# Clean up build artifacts
rm -rf issue-tracker-deliverable/backend/target
rm -rf issue-tracker-deliverable/backend/.mvn

echo "Package created in: issue-tracker-deliverable/"
echo "Ready to submit!"
