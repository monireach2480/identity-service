# Epic 1: Identity Service

Digital Identity Setup and Restore - Backend API

## Prerequisites

- Java 21
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15
- Redis 7

## Quick Start

### 1. Start Infrastructure

```bash
docker-compose up -d
```

### 2. Build Application

```bash
mvn clean install
```

### 3. Run Application

```bash
mvn spring-boot:run
```

Or with custom profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## API Documentation

Once running, access Swagger UI at:
```
http://localhost:8081/swagger-ui.html
```

## API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new identity
- `POST /api/v1/auth/verify-otp` - Verify OTP
- `POST /api/v1/auth/resend-otp` - Resend OTP

### Identity Management
- `POST /api/v1/identity/check` - Check identity exists
- `POST /api/v1/identity/register` - Register DID
- `POST /api/v1/identity/backup` - Create backup
- `POST /api/v1/identity/restore` - Restore identity

### Sync
- `GET /api/v1/sync/health` - Check sync availability
- `POST /api/v1/sync/batch` - Batch sync operations

## Configuration

Edit `src/main/resources/application.yml` or use environment variables:

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export TWILIO_ACCOUNT_SID=your_sid
export TWILIO_AUTH_TOKEN=your_token
```

## Testing

Run all tests:
```bash
mvn test
```

Run specific test:
```bash
mvn test -Dtest=OTPServiceTest
```

## Database Migration

Flyway migrations are in `src/main/resources/db/migration/`

To manually run migrations:
```bash
mvn flyway:migrate
```

## Building for Production

```bash
mvn clean package -DskipTests
docker build -t identity-service:latest .
docker run -p 8081:8081 identity-service:latest
```

## Troubleshooting

### Database Connection Issues
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check connection
psql -h localhost -U postgres -d identity_db
```

### Redis Connection Issues
```bash
# Check Redis is running
docker ps | grep redis

# Test connection
redis-cli ping
```

## License

MIT License