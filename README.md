# Ecommerce API Platform - Deployment Guide

## Overview

This is a Spring Boot-based Ecommerce API platform with a robust revision system, JWT authentication, and an admin dashboard. The application is built using Java 21 and Spring Boot 3.1.4 with support for MySQL database.

## System Requirements

- Java Development Kit (JDK) 21
- Maven 3.8+ (or use the included Maven wrapper)
- MySQL 8.0+
- Docker and Docker Compose (for containerized deployment)
- Git

## Local Development Setup

### 1. Clone the Repository

```bash
git clone [repository-url]
cd ecommerce-api
```

### 2. Configure Database

Update the database configuration in `src/main/resources/application.yml` or use environment variables.

### 3. Build the Application

```bash
# Using Maven
mvn clean package -DskipTests

# Using Maven Wrapper
./mvnw clean package -DskipTests
```

### 4. Run the Application

```bash
# Using Java
java -jar target/*.jar

# Using Maven
mvn spring-boot:run

# Using Maven Wrapper
./mvnw spring-boot:run
```

The application will be available at: http://localhost:8080/api

## Production Deployment

### Option 1: Standard Deployment

1. Configure production properties in `src/main/resources/application-prod.yml`
2. Build the application with the production profile:

```bash
./mvnw clean package -DskipTests
```

3. Run the application in production mode:

```bash
java -jar target/*.jar --spring.profiles.active=prod
```

### Option 2: Docker Deployment (Recommended)

#### Prerequisites

- Docker and Docker Compose installed
- Access to a MySQL server (or use the included Docker Compose configuration)

#### Steps

1. Set up environment variables (create a `.env` file in the project root):

```
SMTP_USERNAME=your-smtp-username
SMTP_PASSWORD=your-smtp-password
```

2. Run the deployment script:

```bash
# Make the script executable
chmod +x deploy.sh

# Run the deployment
./deploy.sh
```

The script will:
- Build the application with Maven
- Create a Docker image
- Start all necessary containers with Docker Compose
- Verify the deployment

Alternatively, you can run Docker Compose manually:

```bash
docker-compose up -d
```

The application will be available at: http://localhost:8080/api

### Environment Variables

The following environment variables can be set to configure the application:

| Variable | Description | Default |
|----------|-------------|---------|
| `JDBC_DATABASE_URL` | JDBC URL for database connection | - |
| `JDBC_DATABASE_USERNAME` | Database username | - |
| `JDBC_DATABASE_PASSWORD` | Database password | - |
| `JWT_SECRET` | Secret key for JWT token signing | - |
| `JWT_EXPIRATION` | JWT token expiration in milliseconds | 86400000 (24h) |
| `PORT` | Server port | 8080 |
| `CORS_ALLOWED_ORIGINS` | Allowed origins for CORS | * |
| `SMTP_HOST` | SMTP server host | smtp.gmail.com |
| `SMTP_PORT` | SMTP server port | 587 |
| `SMTP_USERNAME` | SMTP server username | - |
| `SMTP_PASSWORD` | SMTP server password | - |
| `SMTP_FROM` | From email address | noreply@your-production-domain.com |

## Monitoring and Maintenance

### Health and Metrics

The application includes Spring Boot Actuator for health checks and metrics:

- Health Check: http://localhost:8080/api/actuator/health
- Metrics: http://localhost:8080/api/actuator/metrics

### Logs

In production mode, logs are written to `/var/log/ecommerce-api/application.log` by default.

For Docker deployment, logs can be viewed with:

```bash
docker logs ecommerce-api
```

### Database

Database migrations are managed automatically through Hibernate's `ddl-auto` setting (set to `none` in production).

## Rollback Procedure

If a deployment fails or needs to be rolled back:

1. Stop the current deployment:

```bash
docker-compose down
```

2. Re-deploy the previous version:

```bash
# Checkout the previous version tag
git checkout [previous-version-tag]

# Re-run the deployment script
./deploy.sh
```

## Security Considerations

- JWT secret should be a strong, unique value in production
- Database credentials should be strong and unique
- CORS settings should be restricted to specific domains in production
- Regular security updates should be applied

## Troubleshooting

### Application Won't Start

- Check database connection
- Verify environment variables
- Review application logs

### Connection Issues

- Verify network configuration
- Check firewall rules
- Ensure required ports are open

### Authentication Problems

- Verify JWT configuration
- Check user credentials
- Review security logs

## License

[License details]
