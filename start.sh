#!/bin/bash

echo "Starting Digital Identity Service..."

# Start infrastructure
echo "Starting Docker containers..."
docker-compose up -d

# Wait for services to be ready
echo "Waiting for services to start..."
sleep 10

# Check PostgreSQL
until docker exec identity-postgres pg_isready -U postgres; do
  echo "Waiting for PostgreSQL..."
  sleep 2
done

# Check Redis
until docker exec identity-redis redis-cli ping; do
  echo "Waiting for Redis..."
  sleep 2
done

echo "Infrastructure ready!"

# Build application
echo "Building application..."
mvn clean package -DskipTests

# Run application
echo "Starting Spring Boot application..."
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Make it executable:
```bash
chmod +x start.sh