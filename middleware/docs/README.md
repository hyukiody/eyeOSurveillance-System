# TeraAPI - Distributed Microservices Architecture

**Copyright (c) 2026 YiStudIo Software Inc. All rights reserved.**

## Project Overview

TeraAPI is a production-ready, distributed microservices architecture implementing JWT-based authentication and high-performance stream processing. The system separates concerns into two independent services: an Identity Provider (IdentityService) and a Resource Server (StreamProcessingService).

## Architecture

### Components

#### 1. IdentityService (Port 8081)
- **Purpose**: JWT Token Provider and Identity Management
- **Stack**: Spring Boot 3.2+, Spring Security 6, MySQL
- **Responsibilities**:
  - User registration and authentication
  - JWT token generation and validation
  - Role-based access control (RBAC)
  - User database management

#### 2. StreamProcessingService (Port 8080)
- **Purpose**: High-performance encryption and stream processing
- **Stack**: Java 17, Standard Library HTTP Server
- **Responsibilities**:
  - JWT token validation (signature-based, no database dependency)
  - High-performance data stream processing
  - AES-256 encryption/decryption
  - License tier validation

### Interaction Flow

```
Client
  ↓
[1] POST /api/auth/login → IdentityService
  ↓
[2] IdentityService → MySQL (validate credentials)
  ↓
[3] ← JWT Token (signed with secret key)
  ↓
[4] POST /api/stream/process → StreamProcessingService
    Authorization: Bearer {JWT}
  ↓
[5] StreamProcessingService validates token signature (no DB)
  ↓
[6] Process request → Response
```

## Directory Structure

```
teraApi/
├── identity-service/                           # IdentityService module
│   ├── src/main/java/com/teraapi/identity/
│   │   ├── controller/                         # REST controllers
│   │   │   └── AuthController.java
│   │   ├── service/                            # Business logic
│   │   │   ├── AuthenticationService.java
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── MyUserDetailsService.java
│   │   ├── entity/                             # JPA entities
│   │   │   ├── User.java
│   │   │   └── Role.java
│   │   ├── repository/                         # Data access layer
│   │   │   ├── UserRepository.java
│   │   │   └── RoleRepository.java
│   │   ├── config/                             # Configuration classes
│   │   │   └── SecurityConfig.java
│   │   ├── dto/                                # Data transfer objects
│   │   │   ├── AuthenticationRequest.java
│   │   │   └── AuthenticationResponse.java
│   │   └── IdentityServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml                     # Configuration
│   ├── src/test/
│   └── pom.xml                                 # Maven configuration
│
├── stream-processing-service/                  # StreamProcessingService module
│   ├── src/main/java/com/teraapi/stream/
│   │   ├── JwtValidationUtil.java             # JWT validation (no DB)
│   │   ├── LicenseValidationService.java      # Tier-based access control
│   │   ├── EncryptionService.java             # AES-256 encryption
│   │   ├── StreamRequestHandler.java          # HTTP request handler
│   │   └── StreamProcessingService.java       # Main HTTP server
│   ├── src/test/
│   └── pom.xml
│
├── Dockerfile.mysql                            # MySQL container
├── Dockerfile.identity                         # IdentityService container
├── Dockerfile.stream                           # StreamProcessingService container
├── docker-compose.yml                          # Orchestration
├── init-db.sql                                 # Database initialization
├── .gitignore                                  # Git ignore rules
├── README.md                                   # This file
└── pom.xml                                     # Parent POM (optional)
```

## Technology Stack

- **Java 17+**: Base language
- **Spring Boot 3.2+**: Application framework
- **Spring Security 6**: Authentication and authorization
- **Spring Data JPA**: Database access
- **MySQL 8.0**: Primary database
- **JWT (JJWT 0.12.3)**: Token-based authentication
- **GSON**: JSON processing
- **Lombok**: Boilerplate reduction
- **Docker & Docker Compose**: Containerization

## API Endpoints

### IdentityService (/api/auth)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/login` | Authenticate user, return JWT | ❌ |
| POST | `/api/auth/register` | Register new user | ❌ |
| GET | `/api/auth/health` | Health check | ❌ |

### StreamProcessingService (/api/stream)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/stream/process` | Process data stream | ✅ JWT |
| POST | `/api/stream/encrypt` | Encrypt data with AES-256 | ✅ JWT |
| POST | `/api/stream/decrypt` | Decrypt data with AES-256 | ✅ JWT |
| GET | `/health` | Health check | ❌ |

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Git
- Java 17+ (for local development)
- Maven (for local development)

### Local Development with Docker

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd teraApi
   ```

2. **Build Maven projects**
   ```bash
   # Build IdentityService
   cd identity-service
   mvn clean package -DskipTests
   
   # Build StreamProcessingService
   cd ../stream-processing-service
   mvn clean package
   ```

3. **Start services with Docker Compose**
   ```bash
   cd ..
   docker-compose up -d
   ```

4. **Verify services are running**
   ```bash
   curl http://localhost:8081/api/auth/health
   curl http://localhost:8080/health
   ```

### Local Development Without Docker

1. **Start MySQL**
   ```bash
   # Ensure MySQL 8.0 is running on localhost:3306
   # Create database: teraapi_identity
   ```

2. **Start IdentityService**
   ```bash
   cd identity-service
   mvn spring-boot:run
   ```

3. **Start StreamProcessingService**
   ```bash
   cd ../stream-processing-service
   mvn clean package
   java -jar target/stream-processing-service-1.0.0.jar
   ```

## Authentication Flow

### 1. Registration
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "securePassword123"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "username": "testuser",
  "role": "USER"
}
```

### 2. Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "securePassword123"
  }'
```

### 3. Use Token to Access StreamProcessingService
```bash
curl -X POST http://localhost:8080/api/stream/encrypt \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -d '{
    "data": "sensitive information"
  }'
```

## Environment Variables

### IdentityService
```env
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/teraapi_identity
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
JWT_SECRET=mySecretKeyForJWTTokenGenerationAndValidationPurposesOnly123456789!@#$%^&*
JWT_EXPIRATION=86400000
```

### StreamProcessingService
```env
JWT_SECRET=mySecretKeyForJWTTokenGenerationAndValidationPurposesOnly123456789!@#$%^&*
```

## Configuration Files

### application.yml (IdentityService)
Located at: `identity-service/src/main/resources/application.yml`

Key configurations:
- Database connection pooling (HikariCP)
- JPA/Hibernate settings
- JWT secret and expiration
- Logging configuration
- Server port (8081)

## Security Considerations

### Token Validation Strategy
- **IdentityService**: Full Spring Security with database lookups
- **StreamProcessingService**: Stateless validation using HMAC signature
- **No Inter-Service Database Calls**: StreamProcessingService validates tokens cryptographically

### Password Encryption
- BCrypt with strength 12
- Secure random salt generation
- Never store plain-text passwords

### CORS Configuration
- Configurable allowed origins
- Support for preflight requests
- Customizable max age for cached credentials

## License Tier System

| Tier | Max Requests | Max Stream Size |
|------|-------------|-----------------|
| FREE | 100 | 10 KB |
| STANDARD | 1,000 | 100 KB |
| PREMIUM | 10,000 | 1 MB |

## Monitoring and Logging

### IdentityService
- Structured logging with SLF4J + Logback
- Request/response logging
- Authentication event tracking
- Database pool metrics

### StreamProcessingService
- Console logging with timestamps
- Token validation audit trails
- Performance metrics

### Docker Logs
```bash
docker-compose logs -f identity-service
docker-compose logs -f stream-processing-service
docker-compose logs -f mysql
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(255) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  is_locked BOOLEAN DEFAULT FALSE,
  created_at BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### Roles Table
```sql
CREATE TABLE roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) UNIQUE NOT NULL,
  description VARCHAR(255),
  is_active BOOLEAN DEFAULT TRUE
);
```

## Performance Optimization

### Caching
- JWT signature validation uses in-memory key parsing
- No database queries in StreamProcessingService

### Connection Pooling
- HikariCP with 10 max connections
- 5 minimum idle connections
- 30-second connection timeout

### Stream Processing
- Non-blocking HTTP server
- Thread pool executor (10 threads)
- Efficient JSON parsing with GSON

## Testing

### Unit Tests
```bash
cd identity-service
mvn test

cd ../stream-processing-service
mvn test
```

### Integration Tests
```bash
# Services must be running
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## Troubleshooting

### Database Connection Issues
- Verify MySQL is running: `docker-compose logs mysql`
- Check credentials in `application.yml`
- Ensure port 3306 is not blocked

### JWT Token Validation Failures
- Verify JWT_SECRET matches between services
- Check token expiration time (default: 24 hours)
- Validate Authorization header format: `Bearer {token}`

### Port Conflicts
- Change ports in `docker-compose.yml`
- Update `application.yml` for local development

## Development Guidelines

### Code Structure
- Controllers: HTTP request handling
- Services: Business logic and transactions
- Repositories: Data access (Spring Data JPA)
- Entities: Domain models with JPA annotations
- DTOs: Client API contracts
- Config: Spring configuration classes

### Naming Conventions
- Classes: PascalCase (e.g., `AuthController`)
- Methods: camelCase (e.g., `authenticateUser`)
- Constants: UPPER_SNAKE_CASE (e.g., `MAX_STREAM_SIZE`)
- Packages: lowercase (e.g., `com.teraapi.identity`)

### Commit Message Format
```
[SERVICE] ACTION: Description

Example:
[Identity] Feature: Implement JWT token refresh mechanism
[Stream] Fix: Handle malformed encryption requests
[Core] Refactor: Optimize database connection pooling
```

## Future Enhancements

- [ ] Token refresh mechanism
- [ ] Rate limiting per user/tier
- [ ] Audit logging system
- [ ] Multi-factor authentication (MFA)
- [ ] OAuth 2.0 integration
- [ ] GraphQL API layer
- [ ] Kubernetes deployment manifests
- [ ] Distributed caching (Redis)
- [ ] API versioning strategy
- [ ] OpenAPI/Swagger documentation

## Support and Contact

For issues, questions, or contributions:
1. Create an issue in the repository
2. Contact: support@yistudio.com
3. Documentation: [Wiki](./wiki)

## Changelog

### v1.0.0 (2026-01-02)
- Initial release
- IdentityService with JWT authentication
- StreamProcessingService with encryption support
- Docker Compose orchestration
- MySQL database integration
- Tier-based license validation

---

**© 2026 YiStudIo Software Inc.** | All rights reserved | Licensed under proprietary license
