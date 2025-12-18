# Sentinel AI Surveillance System - Deployment & Quick Start Guide

## 🚀 Quick Start - Run Locally

### Prerequisites
- **Java:** JDK 17+ installed
- **Maven:** 3.8.1+ installed
- **PostgreSQL:** 15.x running (or use Docker)
- **Git:** For version control

### Installation Steps

#### 1. Clone and Navigate
```bash
cd d:\Docs\current_projects\springtools_workspace\SentinelApplication
```

#### 2. Build the Application
```bash
mvn clean install
```
**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 45s
```

#### 3. Run All Tests
```bash
mvn test
```
**Expected Output:**
```
[INFO] Tests run: 42+, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

#### 4. Start the Application
```bash
java -jar target/sentinel-surveillance-1.0.0-SNAPSHOT.jar
```

**Or with Maven:**
```bash
mvn spring-boot:run
```

**Expected Console Output:**
```
🧠 Initializing AI Engine (ONNX)...
✅ YOLOv8 AI Core Ready!
📽️  Video Player Initialized
🚀 Sentinel Application Started on http://localhost:8080
```

---

## 📊 Test Execution

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=AlertEngineTest
```

### Run Phase 3 Tests Only
```bash
mvn test -Dtest=Alert*Test,AnalyticsServiceTest,SecurityAlertRepositoryIntegrationTest
```

### Run E2E Integration Tests
```bash
mvn test -Dtest=AlertPipelineIntegrationTest
```

### Generate Test Report
```bash
mvn clean test site:site
# View: target/site/surefire-report.html
```

---

## 🔧 Configuration

### Database Setup

#### Option 1: PostgreSQL Local
```bash
# On Windows PowerShell
docker run --name sentinel-db -e POSTGRES_DB=sentinel -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:15
```

#### Option 2: Application Configuration
Edit `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sentinel
    username: postgres
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
```

### Encryption Key Setup

#### Development (Local)
Add to `application.yml`:
```yaml
sentinel:
  security:
    secret-key: 'x/A?D(G+KbPeShVmYq3t6w9z$B&E)H@M'
```

#### Production (Vault)
```bash
# Set environment variable
export SENTINEL_SECRET_KEY='your-256-bit-secure-key-here'

# Application will automatically use this instead of property file
```

#### Using HashiCorp Vault
```bash
# Vault setup
vault kv put secret/sentinel secret-key='your-key'

# Application fetches at startup
```

---

## 📡 API Endpoints

### Base URL
```
http://localhost:8080/api/v1/alerts
```

### Alert Endpoints

#### Get Unacknowledged Alerts
```bash
curl http://localhost:8080/api/v1/alerts/unacknowledged?page=0&size=20
```

#### Get Alerts by Zone
```bash
curl http://localhost:8080/api/v1/alerts/zone/{zoneId}
```

#### Get Alerts by Severity
```bash
curl http://localhost:8080/api/v1/alerts/severity/CRITICAL
```

#### Acknowledge Alert
```bash
curl -X POST http://localhost:8080/api/v1/alerts/{alertId}/acknowledge?username=admin
```

#### Get Time Range Alerts
```bash
curl "http://localhost:8080/api/v1/alerts/timerange?start=2025-12-18T00:00:00&end=2025-12-18T23:59:59"
```

### Analytics Endpoints

#### Generate Heatmap
```bash
curl "http://localhost:8080/api/v1/alerts/analytics/heatmap?objectClass=person&start=2025-12-18T00:00:00&end=2025-12-18T23:59:59&gridSize=10"
```

#### Calculate Dwell Time
```bash
curl "http://localhost:8080/api/v1/alerts/analytics/dwell-time?objectClass=person&zoneCoordinates=[0,0,1000,1000]&start=2025-12-18T00:00:00&end=2025-12-18T23:59:59"
```

#### PPE Compliance Analysis
```bash
curl "http://localhost:8080/api/v1/alerts/analytics/ppe-compliance?start=2025-12-18T00:00:00&end=2025-12-18T23:59:59"
```

#### Detection Frequency
```bash
curl "http://localhost:8080/api/v1/alerts/analytics/frequency?start=2025-12-18T00:00:00&end=2025-12-18T23:59:59"
```

---

## 🎯 Feature Validation Checklist

### Phase 3 Alert System
- [ ] Application starts without errors
- [ ] Database connection established
- [ ] AI model (YOLOv8) loaded successfully
- [ ] Video playback functional
- [ ] Detection events generated
- [ ] Alerts created for high-confidence detections
- [ ] Real-time alert queue populated
- [ ] REST API endpoints responding
- [ ] Dashboard accessible

### Real-Time Monitoring
```bash
# Monitor alerts in real-time
while true; do
  curl -s http://localhost:8080/api/v1/alerts/unacknowledged | jq '.content | length'
  sleep 5
done
```

### Database Verification
```bash
# Connect to PostgreSQL
psql -h localhost -U postgres -d sentinel

# Verify tables created
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';

# Check alerts
SELECT id, severity, alert_message, acknowledged FROM security_alerts LIMIT 10;

# Check detections
SELECT id, detected_class, confidence FROM detection_events LIMIT 10;
```

---

## 🏗️ Architecture Components

### Core Services
```
VideoProcessor
  ↓ (persists detections)
DetectionEventRepository
  ↓ (saves to DB)
PostgreSQL Database
  ↓ (queries by)
AlertEngine
  ↓ (evaluates rules)
GeofenceZoneRepository
  ↓ (looks up zones)
SecurityAlert
  ↓ (persists alert)
SecurityAlertRepository
  ↓ (publishes event)
SecurityAlertEvent
  ↓ (dispatches)
AlertNotificationService
  ↓ (adds to queue)
Real-Time Alert Queue
  ↓ (consumed by)
ReportsDashboardView
  ↓ (displays)
REST API Endpoints
```

---

## 📊 System Health Check

### Health Endpoint
```bash
curl http://localhost:8080/actuator/health
```

Expected Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### Metrics
```bash
# View available metrics
curl http://localhost:8080/actuator/metrics

# Check JVM memory
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

---

## 🔐 Security Configuration

### API Authentication (Optional)
Add to `application.yml`:
```yaml
spring:
  security:
    user:
      name: admin
      password: sentinel_secure_password
```

### HTTPS Setup
```yaml
server:
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: your_password
    key-store-type: PKCS12
  port: 8443
```

---

## 📈 Performance Optimization

### Database Connection Pool
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

### JPA/Hibernate Optimization
```yaml
spring:
  jpa:
    properties:
      hibernate:
        batch_size: 25
        fetch_size: 50
        jdbc:
          batch_versioned_data: true
```

### Frame Rate Limiter (AI Inference)
```yaml
app:
  ai:
    frame-rate-limit: 2  # Process 2 frames per second max
```

---

## 🐛 Troubleshooting

### Issue: Database Connection Failed
```bash
# Check PostgreSQL is running
docker ps | grep sentinel-db

# If not running, start it
docker run --name sentinel-db -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:15
```

### Issue: YOLOv8 Model Not Found
```bash
# Model will auto-download from HuggingFace
# Check models/ directory
ls -la models/

# If missing, manually download:
# wget https://huggingface.co/dosage/yolov8n-onnx/resolve/main/yolov8n.onnx -O models/yolov8n.onnx
```

### Issue: Tests Timeout
```bash
# Increase timeout in pom.xml
<properties>
  <test.timeout>120000</test.timeout> <!-- 2 minutes -->
</properties>
```

### Issue: Out of Memory
```bash
# Increase heap size
java -Xmx2g -jar target/sentinel-surveillance-1.0.0-SNAPSHOT.jar
```

---

## 📦 Build Artifacts

### Output Files After Build
```
target/
├── sentinel-surveillance-1.0.0-SNAPSHOT.jar  (Executable JAR)
├── classes/                                    (Compiled classes)
├── test-classes/                              (Test classes)
├── surefire-reports/                          (Test results)
└── site/
    └── surefire-report.html                   (Test report)
```

### Docker Build (Optional)
```bash
# Create Dockerfile
cat > Dockerfile << EOF
FROM openjdk:17-slim
COPY target/sentinel-surveillance-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EOF

# Build image
docker build -t sentinel-ai:1.0 .

# Run container
docker run -p 8080:8080 -e SENTINEL_SECRET_KEY='your-key' sentinel-ai:1.0
```

---

## 📚 Documentation References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Testcontainers Guide](https://www.testcontainers.org/)
- [DJL (Deep Java Library)](https://djl.ai/)

---

## 🎓 Development Workflow

### Local Development Loop
```bash
# 1. Make code changes
# 2. Compile
mvn clean compile

# 3. Run tests
mvn test

# 4. Review test failures (if any)
mvn test -Dtest=YourTestName -X

# 5. Build
mvn clean install

# 6. Run application
java -jar target/sentinel-surveillance-1.0.0-SNAPSHOT.jar

# 7. Test API
curl http://localhost:8080/api/v1/alerts/unacknowledged
```

### Git Workflow
```bash
# Check status
git status

# Review changes
git diff

# Stage changes
git add .

# Commit with descriptive message
git commit -m "Feature: Add new analytics endpoint"

# Push to remote
git push origin main
```

---

## 📋 Deployment Checklist

- [ ] Application compiles without errors
- [ ] All tests pass (42+ tests)
- [ ] Database backup created
- [ ] Environment variables configured
- [ ] Encryption keys rotated
- [ ] API endpoints tested
- [ ] Dashboard UI verified
- [ ] Logs configured
- [ ] Monitoring alerts setup
- [ ] Documentation updated

---

## ✨ Next Steps

1. **Deploy to Staging**
   ```bash
   # Tag release
   git tag -a v1.0.0 -m "Phase 3 Release"
   git push origin v1.0.0
   ```

2. **Monitor Metrics**
   - Alert creation rate
   - Average response time
   - Database query performance
   - Memory usage

3. **Continuous Improvement**
   - Collect user feedback
   - Analyze detection accuracy
   - Optimize alert thresholds
   - Enhance UI based on usage patterns

---

**Status:** ✅ READY FOR DEPLOYMENT  
**Last Updated:** December 18, 2025  
**Phase:** 3 - Complete  
**Project:** Sentinel AI Surveillance System
