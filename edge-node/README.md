# Image Inverter Project

A Spring Boot REST API application for image inversion with comprehensive testing and documentation.

## Quick Navigation

- **[Setup Guide](docs/setup/SETUP_GUIDE.md)** - Installation and environment setup
- **[REST API Reference](docs/api/REST_API.md)** - Complete API endpoint documentation
- **[Architecture Guide](docs/architecture/)** - System design and architecture
- **[Full Documentation](docs/README.md)** - Documentation hub

## Project Overview

The **Image Inverter** is a Java REST API application that provides endpoints to invert images. It features:
- **REST API endpoints** for image inversion in multiple formats (PNG binary, JSON)
- **Spring Boot 3.2.0** for lightweight and flexible deployment
- **Multiple response formats** following REST best practices
- **CORS support** for cross-origin requests
- **Comprehensive error handling** with consistent error responses
- **Health check and info endpoints** for API monitoring
- **Full test coverage** with JUnit 5

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+

### Running the Application

```bash
# Clone/navigate to project directory
cd Image-Inverter_Project

# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

The server will start on **http://localhost:8080**

### Testing the REST API

#### Using cURL (Linux/Mac):
```bash
# Health check
curl http://localhost:8080/api/images/health

# Invert image (returns PNG)
curl -X POST -F "file=@myimage.png" http://localhost:8080/api/images/invert --output inverted.png

# Invert image (returns JSON)
curl -X POST -F "file=@myimage.png" http://localhost:8080/api/images/invert/json
```

#### Using PowerShell (Windows):
```powershell
# Health check
Invoke-RestMethod -Uri "http://localhost:8080/api/images/health"

# Invert image
$form = @{file=@'myimage.png'}
Invoke-RestMethod -Uri "http://localhost:8080/api/images/invert/json" -Method Post -Form $form
```

#### Using the test script:
```bash
# Linux/Mac
bash test-api.sh

# Windows
test-api.bat
```

## REST API Endpoints

### Core Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/images/invert` | POST | Invert image, returns PNG binary |
| `/api/images/invert/json` | POST | Invert image, returns JSON with base64 image |
| `/api/images/invert/xml` | POST | Invert image, returns XML format |
| `/api/images/health` | GET | Health check endpoint |
| `/api/images/info` | GET | API information and available endpoints |

### Request/Response Format

**Request:**
```
POST /api/images/invert/json
Content-Type: multipart/form-data

Parameter: file (the image file to invert)
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Image processed successfully",
  "data": {
    "status": "success",
    "message": "Image inverted successfully",
    "image_base64": "iVBORw0KGgoAAAANSUhEUgAAAAEA...",
    "format": "PNG",
    "size_bytes": 12345,
    "processing_time_ms": 45
  },
  "timestamp": 1703001234567
}
```

## Project Structure

```
Image-Inverter_Project/
├── src/
│   ├── main/java/backend/
│   │   ├── ImageInverterApplication.java       # Spring Boot entry point
│   │   ├── controllers/
│   │   │   └── ImageController.java             # REST endpoints
│   │   ├── services/
│   │   │   └── ImageService.java                # Business logic
│   │   ├── processor/
│   │   │   └── ImageProcessor.java              # Image processing
│   │   ├── dto/
│   │   │   ├── ApiResponse.java                 # Generic response wrapper
│   │   │   ├── ImageInvertResponse.java         # Image response DTO
│   │   │   └── ImageInfoResponse.java           # Image info DTO
│   │   ├── config/
│   │   │   ├── RestApiConfiguration.java        # REST configuration
│   │   │   └── GlobalExceptionHandler.java      # Exception handling
│   │   ├── entities/
│   │   ├── models/
│   │   └── repositories/
│   └── test/
├── pom.xml                                      # Maven dependencies
├── REST_API.md                                  # Detailed API documentation
├── test-api.sh                                  # Linux/Mac test script
├── test-api.bat                                 # Windows test script
└── README.md                                    # This file
```

## Architecture

The application follows a layered architecture:

```
HTTP Request
    ↓
ImageController (REST endpoints)
    ↓
ImageService (Business logic & orchestration)
    ↓
ImageProcessor (Image operations)
    ↓
Response (JSON/XML/Binary)
```

Each layer has clear responsibilities:
- **Controller:** Handles HTTP requests, validates input, returns responses
- **Service:** Orchestrates business logic, calls processors
- **Processor:** Performs image inversion and manipulation
- **DTO:** Data transfer objects for serialization/deserialization

## Key Features

### 1. Multiple Response Formats
- **Binary PNG:** Direct image data for simple display
- **JSON:** Structured response with base64-encoded image and metadata
- **XML:** Alternative structured format for enterprise integration

### 2. Content Negotiation
Specify desired format using:
- Path extension: `/api/images/invert.json`
- HTTP Accept header: `Accept: application/json`

### 3. Response Wrapping
All responses wrapped in `ApiResponse<T>` for consistency:
```json
{
  "success": boolean,
  "message": "Human readable message",
  "data": {...},
  "timestamp": unix_timestamp
}
```

### 4. Error Handling
Consistent error responses with appropriate HTTP status codes:
- 200: Success
- 400: Bad Request
- 413: Payload Too Large
- 500: Server Error

### 5. CORS Support
API supports cross-origin requests from web applications

### 6. Monitoring
- Health check endpoint for uptime monitoring
- Info endpoint for API discovery
- Processing time tracking

## Dependencies

Key Maven dependencies:
- `spring-boot-starter-web` - REST API support
- `spring-boot-starter-data-jpa` - Database access
- `spring-boot-starter-actuator` - Monitoring
- `jackson-dataformat-xml` - XML serialization
- `h2` - In-memory database

See [pom.xml](pom.xml) for complete dependency list.

## Configuration

### Application Properties
Default configuration in `application.properties`:
```properties
spring.application.name=image-inverter-project
server.port=8080
```

### CORS Configuration
Enabled globally in `RestApiConfiguration.java` for all origins

## Extending the API

### Adding a New Endpoint

1. Add method to `ImageController`:
```java
@GetMapping("/endpoint")
public ResponseEntity<ApiResponse<Data>> getEndpoint() {
    return ResponseEntity.ok(new ApiResponse<>(true, "Success", data));
}
```

2. Create corresponding DTO if needed:
```java
public class NewResponseDto {
    // fields and getters/setters
}
```

3. Use `ApiResponse<T>` wrapper for consistency

### Adding New Image Operations

1. Extend `ImageProcessor` with new processing method
2. Add corresponding service method in `ImageService`
3. Create controller endpoint calling the service

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
# Start server
mvn spring-boot:run

# In another terminal, run tests
bash test-api.sh    # Linux/Mac
test-api.bat        # Windows
```

## Comparison with helloworld-rs

This project mirrors the helloworld-rs quickstart pattern but uses **Spring Boot** instead of JBoss EAP:

| Aspect | helloworld-rs | Image Inverter |
|--------|---------------|----------------|
| Framework | JBoss EAP/WildFly | Spring Boot |
| REST API | JAX-RS | Spring Web MVC |
| Response Formats | XML, JSON | PNG, JSON, XML |
| Deployment | WAR to JBoss | Standalone JAR |
| Configuration | beans.xml | application.properties |

Both follow the same REST principles:
- Multiple response format support
- Content negotiation
- Structured response formats
- Clean layered architecture

## Documentation

- [REST_API.md](REST_API.md) - Detailed API endpoint documentation with examples
- [test-api.sh](test-api.sh) - Linux/Mac testing script
- [test-api.bat](test-api.bat) - Windows testing script

## Hierarchy Tree Class Overview

A **Hierarchy Tree Class** represents a structured tree of nodes for organizing and managing inverted images in
the Image Inverter project. This is particularly relevant for Java backend data modeling and React
frontend UI representation.

- The core class should be `Tree` (managing root node, children, etc.), with child classes like `Node` handling
individual image/inversion data.
- Relationships are imposed via UML constructs: e.g., parent-child associations using composition or containment
to ensure logical integrity in both backend and frontend implementations.

#### ## Core Classes
Here's a minimal Java class definition for the hierarchy tree. Assume nodes store basic image metadata; more
details (e.g., inversion parameters) can be added if specified:
