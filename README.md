# Medicaps Launchpad - Backend Foundation

Clean, production-grade Spring Boot 3.x backend foundation for the Medicaps Launchpad platform, established using Java 21 and Maven.

---

## 1. Project Overview

The Medicaps Launchpad project is designed to evolve into an interactive coding and assessment platform following the architectural workflow:
```
React Frontend -> Spring Boot API -> PostgreSQL -> Execution Worker -> Judge0 API -> Docker Sandbox
```

This backend represents the foundational stage of the project:
- Clean modular package structure and conventions.
- Demonstration `Task` REST APIs with in-memory persistence.
- Strict request validation and standardized response envelopes (`ApiResponse<T>` and `ErrorResponse`).
- Robust profile-based configuration (`dev`, `test`).
- Automated tests using JUnit 5 and MockMvc with complete state isolation.
- Continuous Integration with GitHub Actions using `./mvnw clean verify`.

---

## 2. Tech Stack

- **Language:** Java 21 (LTS)
- **Framework:** Spring Boot 3.4.2
- **Build Tool:** Apache Maven (with Maven Wrapper `mvnw` / `mvnw.cmd`)
- **Key Modules:**
  - `spring-boot-starter-web` (REST APIs, Jackson JSON serialization, embedded Tomcat)
  - `spring-boot-starter-validation` (Jakarta Bean Validation / Hibernate Validator)
  - `spring-boot-starter-actuator` (Production health endpoints and monitoring)
  - `spring-boot-starter-test` (JUnit Jupiter 5, MockMvc, AssertJ, Mockito)

---

## 3. Project Structure

```
Medicaps Launchpad/
├── .github/
│   └── workflows/
│       └── ci.yml                 # GitHub Actions CI workflow
├── .mvn/
│   └── wrapper/                   # Maven wrapper configuration and binaries
├── mvnw                           # Linux/macOS Maven wrapper script
├── mvnw.cmd                       # Windows Maven wrapper batch script
├── pom.xml                        # Maven dependency and build definitions
├── README.md                      # Project documentation
├── src/
│   ├── main/
│   │   ├── java/com/project/backend/
│   │   │   ├── BackendApplication.java       # Application entry point
│   │   │   ├── controller/
│   │   │   │   └── TaskController.java       # REST controller for /api/v1/tasks
│   │   │   ├── service/
│   │   │   │   └── TaskService.java          # Business logic and in-memory storage
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   └── TaskRequest.java      # Task creation payload with validation
│   │   │   │   └── response/
│   │   │   │       ├── ApiResponse.java       # Standard generic success envelope
│   │   │   │       ├── ErrorResponse.java     # Standard error envelope
│   │   │   │       └── TaskResponse.java      # Task response presentation DTO
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java # @RestControllerAdvice error handler
│   │   │   │   └── ResourceNotFoundException.java # 404 custom exception
│   │   │   └── model/
│   │   │       ├── Task.java                  # Domain entity
│   │   │       └── TaskStatus.java            # Status enum (PENDING, IN_PROGRESS, COMPLETED)
│   │   └── resources/
│   │       ├── application.properties         # Common baseline configuration
│   │       ├── application-dev.properties     # Development profile configuration
│   │       └── application-test.properties    # Test profile configuration
│   └── test/
│       └── java/com/project/backend/
│           ├── BackendApplicationTests.java   # Context loading smoke test
│           └── TaskControllerTest.java        # Comprehensive MockMvc integration tests
```

---

## 4. How to Run Locally

### Prerequisites
- **JDK 21** installed and configured.

### Using the Maven Wrapper (No pre-installed Maven required)

#### On Windows (PowerShell / Command Prompt):
```powershell
.\mvnw.cmd spring-boot:run
```

#### On Linux / macOS:
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Once started, the backend runs on `http://localhost:8080`.

### Health & Monitoring
Check Actuator health status:
```bash
curl http://localhost:8080/actuator/health
```

Expected output:
```json
{"status":"UP"}
```

---

## 5. Profile Management

Spring Boot profiles allow environment-specific properties to override defaults cleanly.

| Profile | Configuration File | Purpose |
|---|---|---|
| **dev** (Default) | `application-dev.properties` | Local development (`server.port=8080`, `DEBUG` logging) |
| **test** | `application-test.properties` | Automated test suite (`server.port=0`, `WARN` logging) |
| *(Common)* | `application.properties` | Common baseline configuration and fallback environment variables |

### How Profiles Work
1. `application.properties` defines common settings and sets `spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}`.
2. When the application starts, Spring activates the `dev` profile by default.
3. Profile-specific files (`application-{profile}.properties`) override or supplement values in `application.properties`.

### Activating a Different Profile
- **Via Command-line Flag:**
  ```powershell
  .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
  ```
- **Via Environment Variable:**
  ```powershell
  $env:SPRING_PROFILES_ACTIVE="dev"
  .\mvnw.cmd spring-boot:run
  ```
- **Via JVM System Property:**
  ```bash
  java -Dspring.profiles.active=dev -jar target/backend-0.0.1-SNAPSHOT.jar
  ```

---

## 6. API Documentation

Base URL: `/api/v1/tasks`

### 1. Fetch All Tasks
- **Method:** `GET`
- **Path:** `/api/v1/tasks`
- **Status:** `200 OK`
- **Response Format:**
```json
{
  "data": [
    {
      "id": 1,
      "title": "Configure project",
      "description": "Initialize Spring Boot foundation",
      "status": "PENDING"
    }
  ]
}
```

### 2. Fetch Task by ID
- **Method:** `GET`
- **Path:** `/api/v1/tasks/{id}`
- **Status:** `200 OK` (or `404 Not Found` if missing)
- **Response Format:**
```json
{
  "data": {
    "id": 1,
    "title": "Configure project",
    "description": "Initialize Spring Boot foundation",
    "status": "PENDING"
  }
}
```

### 3. Create a Task
- **Method:** `POST`
- **Path:** `/api/v1/tasks`
- **Status:** `201 Created`
- **Allowed Status Values:** `PENDING`, `IN_PROGRESS`, `COMPLETED`
- **Default Status:** If `status` is omitted or `null`, defaults to `PENDING`.

#### Request Payload:
```json
{
  "title": "Configure project",
  "description": "Initialize Spring Boot foundation",
  "status": "IN_PROGRESS"
}
```

#### Response Payload (`201 Created`):
```json
{
  "data": {
    "id": 1,
    "title": "Configure project",
    "description": "Initialize Spring Boot foundation",
    "status": "IN_PROGRESS"
  }
}
```

---

## 7. Error Handling Convention

All error responses follow a consistent, predictable structure without exposing internal stack traces.

### Validation Error (`400 Bad Request`)
When required fields are missing or invalid (e.g. empty `title`):
```json
{
  "timestamp": "2026-11-05T00:20:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Title is required",
  "path": "/api/v1/tasks"
}
```

### Invalid Enum Status (`400 Bad Request`)
When an unrecognized status is supplied (e.g. `"status": "INVALID_STATE"`):
```json
{
  "timestamp": "2026-11-05T00:20:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid value provided for status. Allowed values are: [PENDING, IN_PROGRESS, COMPLETED]",
  "path": "/api/v1/tasks"
}
```

### Resource Not Found (`404 Not Found`)
When requesting a non-existent task ID:
```json
{
  "timestamp": "2026-11-05T00:20:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task with ID 999 not found",
  "path": "/api/v1/tasks/999"
}
```

---

## 8. Testing & Test Isolation

The test suite is built on JUnit 5 and Spring `MockMvc`.

To prevent state leakage between tests in the in-memory `TaskService`, each test run executes a clean isolation reset hook (`taskService.clear()`) inside `@BeforeEach`. This guarantees test independence without opening any public HTTP reset endpoints in production.

### Running Tests
```powershell
.\mvnw.cmd clean test
```

### Running Full Build & Verification
```powershell
.\mvnw.cmd clean verify
```

---

## 9. GitHub Actions CI Workflow

The continuous integration pipeline is defined in `.github/workflows/ci.yml`.

### How It Works:
1. **Trigger:** Runs automatically on pushes to `main` and on pull requests targeting `main`.
2. **Environment:** Uses `ubuntu-latest` and sets up `JDK 21` (Temurin distribution).
3. **Dependency Caching:** Utilizes GitHub Actions built-in Maven caching (`cache: 'maven'`) to accelerate build execution times across runs.
4. **Verification Step:** Runs `./mvnw clean verify`. This compiles all code, runs all unit/integration tests under the `test` profile, and packages the application JAR.
5. **Zero Secrets Required:** Operates entirely self-contained without requiring external databases, Docker containers, or cloud credentials.
