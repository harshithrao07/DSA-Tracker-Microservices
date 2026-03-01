 # DSA Tracker Microservices
 
 A microservice system built with Spring Boot 3/Java 17. It uses service discovery, asynchronous messaging, and containerized deployment to power a modular DSA practice application.
 
 ## Contents
 
 - Architecture Overview
- Technologies
 - Services
 - Communication
 - Data Management
- Service Discovery
 - Fault Tolerance
 - Deployment
 - Security
 - Examples
 - Setup & Run
 - Troubleshooting
 
 ---
 
 ## Architecture Overview
 
 ```mermaid
 graph TD
     UserClient[User/Client] -->|HTTP| Backend[DSA-Tracker Backend]
     Backend -->|REST| Autofill[Autofill Service]
     Backend -->|produce events| Kafka[(Kafka)]
     Kafka -->|consume events| Notification[Notification Service]
     Backend -->|MongoDB Driver| MongoDB[(MongoDB)]
 
     Backend -. register .-> Eureka[Eureka Server]
     Autofill -. register .-> Eureka
     Notification -. register .-> Eureka
 
    KafkaUI[kafka-ui] --> Kafka
 ```
 
 - Domain decomposition: user/question management is isolated in Backend; autofill logic in a separate service; notifications in a dedicated consumer.
 - Integration styles: synchronous REST for request/response; asynchronous Kafka for decoupled events.
 - Cross-cutting: discovery via Eureka; resilience via Resilience4j; containerized with Docker Compose.
 
 ---
 
## Technologies

- Language & Build
  - Java 17 (Eclipse Temurin), Maven 3.9+, Lombok, SLF4J
- Frameworks
  - Spring Boot (Web, Validation, Mail)
  - Spring Data MongoDB
  - Spring Security OAuth2 Client
  - Spring for Apache Kafka
  - Spring Cloud Netflix Eureka (Server & Client)
  - Resilience4j (rate limiter, bulkhead; circuit breaker ready)
- Messaging & Storage
  - Apache Kafka 3.7, Kafka UI
  - MongoDB 7
- Containerization & Orchestration
  - Docker, Docker Compose
- Auth & Tokens
  - OAuth2 (Google, GitHub), JWT (JJWT)
- HTTP Client & JSON
  - Spring 6 RestClient, Jackson

---

 ## Services
 
 ### DSA-Tracker Backend
 - Responsibilities
   - Core domain APIs: users, questions, notes, stats.
   - OAuth2 login (Google/GitHub) and JWT issuance.
   - Produces domain events to Kafka for downstream consumers.
   - Uses MongoDB for persistence.
 - Boundaries
   - Owns the dsa_tracker MongoDB schema.
   - Calls Autofill Service for auxiliary data (difficulty/topics heuristics).
 - Key code
   - Configuration: [application.properties](file:///d:/DSA%20Tracker/DSA-Tracker%20Microservices/DSA-Tracker-Backend/src/main/resources/application.properties)
   - Mongo template: [MongoConfig.java](file:///d:/DSA%20Tracker/DSA-Tracker%20Microservices/DSA-Tracker-Backend/src/main/java/com/harshith/dsa_question_picker/config/MongoConfig.java)
   - Kafka settings: same application.properties (bootstrap-servers via KAFKA_URL).
 
 ### Autofill Service
 - Responsibilities
   - Fetches and aggregates problem metadata from external platforms (e.g., Codeforces, LeetCode).
   - Provides REST APIs consumed by Backend.
 - Boundaries
   - Stateless; no database by default.
   - Registered with Eureka for discovery.
 - Key code
   - REST client config: [AppConfig.java](file:///d:/DSA%20Tracker/DSA-Tracker%20Microservices/autofill-service/src/main/java/com/harshith/autofill_service/config/AppConfig.java)
   - Fetchers: [CodeforcesFetcher.java](file:///d:/DSA%20Tracker/DSA-Tracker%20Microservices/autofill-service/src/main/java/com/harshith/autofill_service/service/CodeforcesFetcher.java), [LeetCodeFetcher.java](file:///d:/DSA%20Tracker/DSA-Tracker%20Microservices/autofill-service/src/main/java/com/harshith/autofill_service/service/LeetCodeFetcher.java)
 
 ### Notification Service
 - Responsibilities
   - Consumes events from Kafka and delivers email notifications.
 - Boundaries
   - Stateless; leverages SMTP for email.
   - Registered with Eureka; subscribes to Kafka topics.
 - Key code
   - Kafka listener + mailer: [NotificationService.java](file:///d:/DSA%20Tracker/DSA-Tracker%20Microservices/notification-service/src/main/java/com/harshith/notification_service/service/NotificationService.java)
 
 ### Eureka Server
 - Responsibilities
   - Service registry for discovery.
 - Boundaries
   - Central infrastructure component; no business logic.
 - Key code
   - Spring Cloud Netflix Eureka server app at eureka-server module.
 
 ### Infrastructure
 - Kafka: single-broker setup, plus optional Kafka UI for local inspection.
 - MongoDB: single-node for development.
 
 ---
 
 ## Communication
 
 ### Synchronous (HTTP/REST)
 - Client → Backend: public APIs.
 - Backend → Autofill: REST calls for enrichment.
 - Service discovery: logical hostnames resolved via Eureka; fall back to environment URLs in local/dev as needed.
 
 ### Asynchronous (Kafka)
 - Backend publishes domain events (e.g., user inactivity, notification-worthy actions).
 - Notification Service consumes topics and sends emails.
 - Bootstrap servers provided by KAFKA_URL.
 
 Patterns
 - Request/Response for immediate user interactions.
 - Event-driven for decoupled side effects and retries.
 
 ---
 
 ## Data Management
 
 - Database-per-service pattern.
 - Backend stores data in MongoDB database dsa_tracker.
 - Other services are stateless in current design.
 - Mongo connection provided via MONGODB_URL (see environment section).
 
 ---
 
 ## Service Discovery
 
 - Eureka server runs at http://localhost:8080 by default.
 - Clients set `eureka.client.service-url.defaultZone` to the EUREKA_URL from env.properties.
 - In containers, internal URL is `http://eureka-server:8080/eureka` (Compose service name + port).
 
 ---
 
 ## Fault Tolerance
 
 Resilience4j is configured in Backend for rate limiting and bulkheads. Add circuit breakers similarly.
 
 From Backend’s application.properties:
 
 ```properties
 resilience4j.ratelimiter.instances.autofillRateLimiter.limitForPeriod=2
 resilience4j.ratelimiter.instances.autofillRateLimiter.limitRefreshPeriod=10s
 resilience4j.ratelimiter.instances.autofillRateLimiter.timeoutDuration=1s
 
 resilience4j.bulkhead.instances.autofillBulkhead.maxConcurrentCalls=2
 resilience4j.bulkhead.instances.autofillBulkhead.maxWaitDuration=0
 ```
 
 ---
 
 ## Deployment
 
 Docker Compose orchestrates all services and infrastructure for local development.
 
 - Compose file: [compose.yaml](file:///d:/DSA%20Tracker/DSA-Tracker%20Microservices/compose.yaml)
 - Services
   - mongodb (27017)
   - kafka (9092) and kafka-ui (9090)
   - eureka-server (8080)
   - backend (8081)
   - autofill-service (8082)
   - notification-service (8083)
 - Each service has a Dockerfile that expects a built JAR in `target/`.
 
 Start
 - Build JARs first:
   - `cd "d:\DSA Tracker\DSA-Tracker Microservices\eureka-server" && mvn -q -DskipTests clean package`
   - `cd "d:\DSA Tracker\DSA-Tracker Microservices\DSA-Tracker-Backend" && mvn -q -DskipTests clean package`
   - `cd "d:\DSA Tracker\DSA-Tracker Microservices\autofill-service" && mvn -q -DskipTests clean package`
   - `cd "d:\DSA Tracker\DSA-Tracker Microservices\notification-service" && mvn -q -DskipTests clean package`
 - Bring up the stack:
   - `cd "d:\DSA Tracker\DSA-Tracker Microservices"`
   - `docker compose up -d`
 - Logs and status:
   - `docker compose logs -f eureka-server backend autofill-service notification-service kafka mongodb`
   - `docker compose ps`
 
 Networking
 - Intra-service hostnames: `mongodb`, `kafka`, `eureka-server`, `backend`, etc.
 - Kafka advertised listeners are configured for in-network access (`kafka:9092`). For host tools, consider adding a second external listener.
 
 ---
 
 ## Security
 
 - OAuth2 Login
   - Google and GitHub clients configured in Backend: see application.properties.
   - After successful OAuth2 login, Backend issues JWTs; validity controlled by `jwt.expiration.ms` and signing key `JWT_SECRET`.
 - Inter-service security
  - Internal network-only access in local Compose reduces exposure.
 - Secrets management
   - For local dev, credentials are provided via `env.properties`. For higher environments, use a secrets manager and container runtime env vars.
 
 ---
 
 ## Examples
 
### Backend → Autofill via OpenFeign (existing)
 
 ```java
@FeignClient(name = "autofill-service")
public interface AutofillClient {
    @PostMapping("/autofill")
    ResponseEntity<ApiResponseDTO<QuestionAutofillDTO>>
    autofillQuestion(@Valid @RequestBody AutofillRequestDTO req);
 }
 ```
 
Usage with Resilience4j in Backend service layer:

```java
@RateLimiter(name = "autofillRateLimiter")
@Bulkhead(name = "autofillBulkhead", type = Bulkhead.Type.SEMAPHORE)
public ResponseEntity<ApiResponseDTO<QuestionAutofillDTO>> autofillQuestion(AutofillRequestDTO req) {
    return autofillClient.autofillQuestion(req);
}
```

 ### Kafka consumer in Notification Service (existing)
 
 See [NotificationService.java](file:///d:/DSA%20Tracker/DSA-Tracker%20Microservices/notification-service/src/main/java/com/harshith/notification_service/service/NotificationService.java) with `@KafkaListener` methods and JavaMailSender integration.
 
 ### MongoDB aggregation in Backend (existing)
 
 See services using `MongoTemplate`, e.g. [DashboardService.java](file:///d:/DSA%20Tracker/DSA-Tracker%20Microservices/DSA-Tracker-Backend/src/main/java/com/harshith/dsa_question_picker/service/DashboardService.java) and [QuestionsService.java](file:///d:/DSA%20Tracker/DSA-Tracker%20Microservices/DSA-Tracker-Backend/src/main/java/com/harshith/dsa_question_picker/service/QuestionsService.java).
 
 ---
 
 ## Setup & Run
 
 Prerequisites
 - Windows/macOS/Linux with Docker Desktop (or Docker Engine) and Docker Compose.
 - JDK 17, Maven 3.9+ (for building JARs).
 - SMTP credentials for notifications (Gmail requires app password).
 
 Environment
 - Backend env: [env.properties](file:///d:/DSA%20Tracker/DSA-Tracker%20Microservices/DSA-Tracker-Backend/env.properties)
   - `MONGODB_URL=mongodb://mongodb:27017/dsa_tracker?directConnection=true`
   - `KAFKA_URL=kafka:9092`
   - `EUREKA_URL=http://eureka-server:8080/eureka`
   - OAuth2 client IDs and secrets (Google/GitHub)
   - `JWT_SECRET` (base64-encoded key recommended)
 - Notification env: [env.properties](file:///d:/DSA%20Tracker/DSA-Tracker%20Microservices/notification-service/env.properties)
   - `MAIL_USERNAME`, `MAIL_PASSWORD` (or app password)
   - `KAFKA_URL`, `EUREKA_URL`
 - Autofill env: [env.properties](file:///d:/DSA%20Tracker/DSA-Tracker%20Microservices/autofill-service/env.properties)
   - `EUREKA_URL`
 
 Build & Run
 - Build all JARs, then `docker compose up -d` as shown above.
 - Access points:
   - Backend APIs: http://localhost:8081
   - Eureka dashboard: http://localhost:8080
   - Kafka UI: http://localhost:9090
 
 ---
 
 ## Troubleshooting
 
 - Container name conflicts
   - Error indicates existing container (e.g., `/kafka` already in use). Remove or rename:
     - `docker rm -f kafka`
     - Or run with a project prefix: `docker compose -p dsa-tracker up -d`
 - Kafka listener issues from host tools
   - Broker advertises `kafka:9092` for internal clients. To connect from the host, consider a dual-listener setup:
     - Internal: `PLAINTEXT://kafka:9092`
     - External: `PLAINTEXT_HOST://localhost:29092`
 - Missing JARs during image build
   - Ensure `mvn clean package -DskipTests` ran in each service so `target/*.jar` exists.
 - MongoDB connection
   - Use `mongodb://mongodb:27017/dsa_tracker?directConnection=true` inside Compose; `localhost` works from your host.
 - Ports already in use
   - Change host ports in compose or stop conflicting services.
 - Email failures
   - For Gmail, enable “App Password” and use it as `MAIL_PASSWORD`.
 
 ---
 
 ## License
 
 For internal development and learning purposes.
 
