#!/bin/bash

set -e  # Exit immediately if a command exits with a non-zero status

# Display ASCII banner
echo "=============================================="
echo "  Ecommerce API Deployment"
echo "=============================================="

# Environment variables
ENV_FILE=".env"
if [ -f "$ENV_FILE" ]; then
    echo "Loading environment variables from $ENV_FILE"
    export $(grep -v '^#' $ENV_FILE | xargs)
fi

# Function to handle errors
handle_error() {
    echo "ERROR: Deployment failed at stage: $1"
    echo "Reason: $2"
    exit 1
}

# Step 1: Maven Build
echo "Step 1/4: Building application with Maven..."
./mvnw clean package -DskipTests || handle_error "Maven Build" "Maven build failed"
echo "Maven build completed successfully."

# Step 2: Build Docker image
echo "Step 2/4: Building Docker image..."
docker build -t ecommerce-api:latest . || handle_error "Docker Build" "Failed to build Docker image"
echo "Docker image built successfully."

# Step 3: Deploy with Docker Compose
echo "Step 3/4: Deploying with Docker Compose..."
docker-compose down || true  # Don't fail if containers are not running
docker-compose up -d || handle_error "Docker Compose" "Failed to start containers"
echo "Containers deployed successfully."

# Step 4: Verify deployment
echo "Step 4/4: Verifying deployment..."
sleep 10  # Wait for containers to initialize

# Check if the application is running
CONTAINER_STATUS=$(docker ps -a | grep ecommerce-api | grep -c "Up" || echo "0")
if [ "$CONTAINER_STATUS" -eq "1" ]; then
    echo "Application is running."
else
    handle_error "Verification" "Application container is not running"
fi

# Print logs
echo "Recent logs from the application:"
docker logs ecommerce-api --tail 20

echo "=============================================="
echo "  Deployment Completed Successfully!"
echo "  Application URL: http://localhost:8080/api"
echo "=============================================="
