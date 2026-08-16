# Production-Ready Future Roadmap & Engineering Blueprint
**Digital Marketing Agency (DMA) ERP & CRM Backend**  
*Target Scale: 10,000+ Active Monthly Users (MAU) | Architecture: Scalable, Multi-Tenant Ready Spring Boot SaaS*

---

## 1. Executive Overview & Production Readiness Goals
This roadmap details the engineering blueprint to transition the DMA CRM Backend from a functional modular monolith into a hardened, high-performance, production-grade SaaS application designed to seamlessly handle **10k+ MAU**, high concurrent analytics queries, batch billing cycles, and automated lead processing.

### Key Production Targets:
- **P99 API Latency**: < 150ms for OLTP operations; < 300ms for heavy reporting queries.
- **Availability / SLA**: 99.9% uptime with automated health checks, container restarts, and zero-downtime rolling deployments.
- **Security & Multi-Tenancy Hardening**: Zero-trust RBAC with fine-grained `module:action` permissions, strict rate-limiting, and auditable data mutations.
- **Data Integrity & Scalability**: Caching hot queries (Redis), connection pooling optimizations, read/write segregation, and MongoDB indexing strategies.

---

## 2. Global Architecture Refactoring & Cross-Cutting Upgrades

### 2.1 MongoDB Indexing & Query Tuning
To sustain 10k+ MAU querying leads, clients, invoices, and analytics simultaneously:
- **Compound Indexes**:
  - `leads`: `{ "status": 1, "assignedUserId": 1, "createdAt": -1 }`, `{ "email": 1 }` (unique sparse).
  - `clients`: `{ "status": 1, "createdAt": -1 }`, `{ "companyName": "text", "name": "text" }` (text search).
  - `invoices`: `{ "clientId": 1, "status": 1, "dueDate": 1 }`, `{ "invoiceNumber": 1 }` (unique).
  - `projects`: `{ "clientId": 1, "status": 1 }`.
- **Soft Deletion (`isDeleted`)**:
  - Ensure all queries default to `isDeleted: false` via custom repository base or `@Query` filter to prevent scan bloat.

### 2.2 Redis Caching Strategy
- **Session & Token Revocation**: Store JWT blacklist or active session tokens in Redis with TTL matching JWT expiration.
- **Metadata Caching**: Cache Roles, Permissions, and Lookup Data (`@Cacheable(value = "roles", key = "#roleName")`) with invalidation triggers on mutation.
- **Dashboard Stats**: Cache heavy dashboard summaries for 5-15 minutes using Cache-Aside pattern.

### 2.3 Distributed Rate Limiting & Security
- **Bucket4j + Redis**: Enforce endpoint-level rate limiting (e.g., Auth endpoints: 5 req/min per IP; General CRUD: 120 req/min per User).
- **Audit Logging**: Implement `@EntityListeners` / MongoDB lifecycle hooks to track `createdBy`, `lastModifiedBy`, `createdAt`, `updatedAt`, and mutation diffs in an `audit_logs` collection.

---

## 3. Module-by-Module Code-Stories, Refactors, and Test Cases

---

### Module 1: Auth & User Management (Hardening & Scale)

#### 📖 Code-Story: Enterprise Session & Identity Management
As the system scales to 10k+ users across multiple agency roles (Owner, Manager, Employee), authentication must shift from simple stateless JWTs to a hardened identity system supporting refresh token rotation, password reset flows, account lockout on brute-force attempts, and sub-millisecond RBAC permission checks via Redis caching.

#### 🔧 Codebase Modifications Required:
1. **Refresh Token Flow**:
   - Create `RefreshToken` entity (UUID/Secure Hash, `userId`, `expiryDate`, `revoked`).
   - Implement `POST /api/v1/auth/refresh-token` and `POST /api/v1/auth/logout` (revoking tokens).
2. **Brute Force Protection**:
   - Add Redis-backed login attempt counter per email/IP; lock accounts for 15 mins after 5 consecutive failures.
3. **Optimized UserDetails & RBAC**:
   - Cache user permissions in Redis on login to avoid DB hits on every request filter chain evaluation.

#### 🧪 Test Cases:
| Test ID | Scenario | Given | When | Then |
| :--- | :--- | :--- | :--- | :--- |
| **AUTH-01** | Standard Login | Valid credentials | `POST /api/v1/auth/login` | Return 200 OK + Access JWT (15m) + Refresh Token (7d). |
| **AUTH-02** | Refresh Token Rotation | Valid Refresh Token | `POST /api/v1/auth/refresh-token` | Return new Access Token + new Refresh Token; invalidate previous token. |
| **AUTH-03** | Token Reuse Detection | Already consumed Refresh Token | `POST /api/v1/auth/refresh-token` | Invalidate all sessions for user (Security Alert), return 401. |
| **AUTH-04** | Rate Limiting / Lockout | 5 incorrect passwords | 6th `POST /api/v1/auth/login` | Return 429 Too Many Requests / 423 Locked with retry-after header. |
| **AUTH-05** | Unauthorized Action | Employee role token | `DELETE /api/v1/users/{id}` | Return 403 Forbidden (`user:delete` permission required). |

---

### Module 2: Lead Management & Conversion Pipeline

#### 📖 Code-Story: High-Velocity Lead Intake & Automated Attribution
Digital marketing agencies capture thousands of inbound leads via webhooks, landing pages, and manual entry. The lead service must support idempotent batch ingestion, duplicate detection, automatic team assignment algorithms (Round-Robin), and atomic transactional conversion from Lead to Client.

#### 🔧 Codebase Modifications Required:
1. **Deduplication Engine**:
   - Query verification on `email` and normalized `phone` before insert.
2. **Assignment Engine**:
   - Add `AssignmentService` using Redis atomic counter (`INCR`) for round-robin assignment among available Employees.
3. **Atomic Lead-to-Client Conversion**:
   - Wrap `convertLeadToClient(leadId)` in `@Transactional` (MongoDB replica set transaction) to ensure `Lead` status updates to `CONVERTED` and new `Client` document is created atomically.

#### 🧪 Test Cases:
| Test ID | Scenario | Given | When | Then |
| :--- | :--- | :--- | :--- | :--- |
| **LEAD-01** | Lead Intake Deduplication | Existing lead with `lead@agency.com` | `POST /api/v1/leads` with same email | Return 409 Conflict with duplicate lead reference. |
| **LEAD-02** | Round-Robin Assignment | 3 active employees | Ingesting 3 consecutive leads | Each employee is assigned exactly 1 lead in sequence. |
| **LEAD-03** | Atomic Lead Conversion | Valid lead in `QUALIFIED` status | `POST /api/v1/leads/{id}/convert` | Create `Client` record, update `Lead.status = CONVERTED`, return 201 with `clientId`. |
| **LEAD-04** | Conversion Rollback Failure | Failure during Client document save | `POST /api/v1/leads/{id}/convert` | Transaction rolls back; Lead remains `QUALIFIED` without orphaned Client record. |

---

### Module 3: Client & Account Management

#### 📖 Code-Story: 360-Degree Client Relationship & Health Monitoring
Clients form the operational core of the ERP. Beyond basic CRUD, the production Client module must maintain a live aggregated view of client health (Total Revenue, Active Projects, Overdue Invoices, Communication History) without executing expensive joins on every read.

#### 🔧 Codebase Modifications Required:
1. **Aggregated Client Summary / Projections**:
   - Use MongoDB Aggregation Pipeline (`$lookup`, `$group`) to build high-performance summary responses (`ClientSummaryDTO`).
2. **Status State-Machine**:
   - Enforce transitions: `ONBOARDING` ➔ `ACTIVE` ➔ `PAUSED` ➔ `CHURNED` via validator pattern.
3. **Multi-Contact Association**:
   - Support embedded sub-documents for multiple points of contact per client company.

#### 🧪 Test Cases:
| Test ID | Scenario | Given | When | Then |
| :--- | :--- | :--- | :--- | :--- |
| **CLI-01** | Client Creation Validation | Payload missing required company name/tax ID | `POST /api/v1/clients` | Return 400 Bad Request with field-level validation errors. |
| **CLI-02** | Status Transition Validation | Client currently `CHURNED` | `PATCH /api/v1/clients/{id}/status` to `ONBOARDING` | Return 422 Unprocessable Entity (Invalid lifecycle transition). |
| **CLI-03** | High-Load Client Search | 10,000 Client records | `GET /api/v1/clients?search=tech&page=0&size=20` | Query executes via Text Index in < 50ms returning paginated results. |
| **CLI-04** | Soft Delete Cascade Check | Client with 2 active projects | `DELETE /api/v1/clients/{id}` | Mark `Client.isDeleted = true` and archive/notify assigned managers. |

---

### Module 4: Project & Deliverable Management

#### 📖 Code-Story: Campaign Execution & Budget Tracking
Marketing campaigns operate against strict deadlines and ad budgets. The Project module tracks project phases (SEO, SEM, Social Media), budget consumption, and task completion, automatically alerting account managers when milestones approach risk thresholds.

#### 🔧 Codebase Modifications Required:
1. **Budget vs. Actuals Tracker**:
   - Add calculation methods in `ProjectService` for consumed budget vs allocated budget.
2. **Project Milestones / Tasks**:
   - Embed or reference `Task` sub-entities with completion states and due dates.
3. **Dynamic Filtering**:
   - Implement `MongoTemplate` dynamic criteria queries for filtering by date ranges, budget thresholds, and assigned teams.

#### 🧪 Test Cases:
| Test ID | Scenario | Given | When | Then |
| :--- | :--- | :--- | :--- | :--- |
| **PROJ-01** | Create Project with Milestones | Valid client ID & milestone list | `POST /api/v1/projects` | Return 201 Created; verify project is linked to client. |
| **PROJ-02** | Non-existent Client Linking | Invalid client ID | `POST /api/v1/projects` | Return 404 Resource Not Found with clear error message. |
| **PROJ-03** | Budget Overrun Alert | Project with budget $5,000 and expenses $5,200 | `PUT /api/v1/projects/{id}/expenses` | Update expenses and set `budgetStatus = OVER_BUDGET`. |
| **PROJ-04** | Pagination & Sorting | 100 projects in DB | `GET /api/v1/projects?sortBy=deadline&order=asc` | Return page 1 sorted chronologically by nearest deadline. |

---

### Module 5: Billing, Invoicing & PDF Generation

#### 📖 Code-Story: Automated Invoicing & PDF Rendering Engine
For 10k users, billing must be automated and resilient. Invoices must calculate tax, discounts, line items, and generate secure downloadable PDF files stored on S3/Cloud Storage, complete with automated webhook handlers for payment gateways (Stripe/Razorpay).

#### 🔧 Codebase Modifications Required:
1. **Itemized Calculation Engine**:
   - Strict decimal arithmetic using `BigDecimal` for line items, tax percentage, and discounts to prevent floating-point rounding errors.
2. **PDF Generation Service**:
   - Integrate OpenPDF / iText / Thymeleaf HTML-to-PDF engine.
   - Offload heavy PDF rendering to an asynchronous thread pool (`@Async`) or background queue to prevent blocking Tomcat request threads.
3. **Storage Abstraction Layer**:
   - Create `FileStorageService` interface with `LocalStorageService` (dev) and `S3StorageService` (prod) implementations.

#### 🧪 Test Cases:
| Test ID | Scenario | Given | When | Then |
| :--- | :--- | :--- | :--- | :--- |
| **INV-01** | Precise Tax Calculation | 3 line items ($100.50, $200.25, $50.00) @ 18% Tax | `POST /api/v1/invoices` | Verify subtotal = $350.75, Tax = $63.14, Total = $413.89. |
| **INV-02** | Asynchronous PDF Generation | Valid approved invoice | `GET /api/v1/invoices/{id}/pdf` | Return 200 with `application/pdf` stream or pre-signed S3 URL. |
| **INV-03** | Payment Status Transition | Unpaid invoice | `POST /api/v1/invoices/{id}/payments` (Full amount) | Mark invoice as `PAID`, record payment transaction, trigger client receipt email. |
| **INV-04** | Immutability of Paid Invoices | Invoice marked as `PAID` | `PUT /api/v1/invoices/{id}` (Attempt to modify line items) | Return 400 Bad Request ("Paid invoices cannot be modified"). |

---

### Module 6: Background Jobs, Automation & Notifications (New)

#### 📖 Code-Story: Automated SLA Monitoring & Overdue Notice Triggers
An enterprise CRM requires automated scheduled jobs to scan for unpaid invoices, trigger payment reminder emails, flag abandoned leads, and generate weekly performance digests without manual intervention.

#### 🔧 Codebase Modifications Required:
1. **Spring `@EnableScheduling` & ShedLock**:
   - Implement `ShedLock` with MongoDB to ensure scheduled jobs run on only one instance in a multi-replica cluster.
2. **Automated Reminders**:
   - Daily cron job: Check `invoices` where `dueDate < NOW` and `status == UNPAID` ➔ change status to `OVERDUE` and dispatch email event.
3. **Async Event Bus (`ApplicationEventMulticaster`)**:
   - Decouple business actions (e.g., `LeadConvertedEvent`, `InvoicePaidEvent`) from notification and email dispatchers.

#### 🧪 Test Cases:
| Test ID | Scenario | Given | When | Then |
| :--- | :--- | :--- | :--- | :--- |
| **SCHED-01** | Distributed Cron Locking | 3 running backend replicas | Scheduled invoice reminder triggers | ShedLock acquires lock; job executes exactly once across all 3 nodes. |
| **SCHED-02** | Automatic Overdue Marking | Invoice past due date | Daily cron executes | Status transitions to `OVERDUE` and email dispatch event is queued. |
| **SCHED-03** | Asynchronous Event Firing | Lead converted | Event `LeadConvertedEvent` fired | Audit log recorded and notification dispatched without increasing response latency of conversion API. |

---

## 4. Comprehensive Testing Strategy

```
               / \
              /   \
             / E2E \       <-- Postman / Newman Automated Suites (CI/CD)
            /-------\
           /  Integ  \     <-- @SpringBootTest + Testcontainers (MongoDB & Redis)
          /-----------\
         /    Unit     \   <-- JUnit 5 + Mockito (Services, Mappers, Validators)
        /---------------\
```

### 4.1 Unit Testing (Target: > 85% Code Coverage)
- **Frameworks**: JUnit 5, Mockito, AssertJ.
- **Scope**:
  - Service layer business logic and calculations (especially Invoice `BigDecimal` math and Lead assignment algorithms).
  - Custom validation annotations and DTO validators.
  - Exception handling and error response mapping in `@RestControllerAdvice`.

### 4.2 Integration Testing with Testcontainers
- **Testcontainers**: Spin up real, ephemeral Docker containers for MongoDB and Redis during integration test runs.
- **Scope**:
  - Repository layer queries, Mongo text searches, and compound index validation.
  - End-to-end Controller-to-Repository flow verifying Spring Security filters and JWT validation.

### 4.3 Load & Performance Testing (10k MAU Target)
- **Tool**: Gatling / k6.
- **Scenarios**:
  - **Peak Concurrent Users**: Simulate 500 concurrent active users browsing dashboard, filtering leads, and creating projects.
  - **Stress Test**: Ingest 1,000 inbound leads/minute to identify connection pool exhaustion and memory pressure.
  - **Threshold Criteria**: 0% error rate, P95 < 200ms, CPU usage < 70%.

---

## 5. Production Infrastructure, Containerization & CI/CD

### 5.1 Multi-Stage Dockerfile Optimization
```dockerfile
# Stage 1: Build & Package
FROM gradle:8.5-jdk17-alpine AS builder
WORKDIR /app
COPY build.gradle settings.gradle /app/
RUN gradle dependencies --no-daemon
COPY src /app/src
RUN gradle bootJar --no-daemon -x test

# Stage 2: Hardened Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=builder /app/build/libs/*.jar app.jar
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 5.2 Production Docker Compose Stack (`docker-compose.prod.yml`)
- **Services**:
  - `dma-backend-1`, `dma-backend-2` (Load-balanced Spring Boot instances).
  - `nginx` / `traefik` (Reverse proxy, SSL termination, rate limiting).
  - `mongodb-primary`, `mongodb-secondary` (Replica set for transactions and high availability).
  - `redis` (Cache and session store with AOF persistence).
  - `prometheus` & `grafana` (Application metrics and APM dashboard).

### 5.3 CI/CD Automation Pipeline (GitHub Actions)
```yaml
name: Production CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run Unit & Integration Tests (Testcontainers)
        run: ./gradlew check --no-daemon
      - name: Generate JaCoCo Code Coverage Report
        run: ./gradlew jacocoTestReport
      - name: Verify Quality Gate (SonarQube/Lint)
        run: echo "Quality Gate Passed"

  deploy-staging:
    needs: build-and-test
    if: github.ref == 'refs/heads/develop'
    runs-on: ubuntu-latest
    steps:
      - name: Build & Push Docker Image
        run: echo "Pushing image to Container Registry..."
      - name: Trigger Rolling Update on Staging
        run: echo "Deploying to Staging Environment..."

  deploy-production:
    needs: build-and-test
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - name: Zero-Downtime Rolling Deployment to Production
        run: echo "Deploying to Production Cluster..."
```

---

## 6. Observability, Monitoring & Production Operations

1. **Spring Boot Actuator & Prometheus**:
   - Expose `/actuator/prometheus` (secured, internal only).
   - Track JVM memory, GC pauses, active HTTP request threads, and MongoDB connection pool saturation (`HikariCP` / `MongoClientSettings`).
2. **Centralized Logging (ELK / Grafana Loki)**:
   - Structured JSON logging using Logback with `traceId` and `spanId` (Micrometer Tracing) injected into every request header.
3. **Alerting Rules**:
   - Alert on error rate > 1% over 5 minutes.
   - Alert on P99 response time > 500ms.
   - Alert on MongoDB connection pool wait time > 100ms.

---

## 7. Strategic Phase-by-Phase Execution Timeline

```
Q1 ─────────────────────────► Q2 ─────────────────────────► Q3
[ Phase 1: Hardening ]        [ Phase 2: Adv Features ]     [ Phase 3: Scale & Prod ]
• Refresh Tokens & Redis      • PDF Billing Engine          • Testcontainers & CI/CD
• Mongo Compound Indexes      • S3 Asset Storage            • k6 Load Testing (10k MAU)
• Rate Limiting (Bucket4j)    • Distributed ShedLock Jobs   • Zero-Downtime Deployment
```

1. **Phase 1: Architecture Hardening & Security (Weeks 1-3)**
   - Implement refresh tokens, Redis caching, Mongo indexes, and rate limiting.
2. **Phase 2: Advanced Feature Set & Automation (Weeks 4-6)**
   - Build S3 file storage, async PDF generation engine, and distributed scheduled jobs.
3. **Phase 3: Automated Testing & Containerization (Weeks 7-9)**
   - Complete JUnit5/Testcontainers test suite; write multi-stage Dockerfile and Docker Compose.
4. **Phase 4: Performance Validation & CI/CD Deployment (Weeks 10-12)**
   - Execute Gatling load tests for 10k MAU; establish GitHub Actions CI/CD pipeline and Prometheus/Grafana monitoring.
