# Digital Agency ERP - Comprehensive Audit Report
**Date:** 2026-09-02

## PHASE 1 — PROJECT UNDERSTANDING

### Architecture Discovered
The project is a Spring Boot Modular Monolith utilizing a robust event-driven architecture, MongoDB (Spring Data), and stateless JWT security. 

### Modules Discovered
- **Auth/Security**: `Auth`, `User`, `Role`, `Permission` (RBAC)
- **Core ERP**: `Client`, `Lead`, `Project`, `Invoice`
- **UI/Layout**: `Menu` (Dynamic Sidebar)
- **Infrastructure**: `Common` (Counters), `Exception` (Global handlers), `Config` (Security, Swagger, Rate Limiter)

### Major Dependencies
- Spring Boot Starter Web, Data MongoDB, Security, Validation
- Bucket4j (Rate Limiting)
- JJWT (Authentication)
- Swagger/OpenAPI
- Docker / Docker Compose

### Data & Security Flow
1. **Security**: Stateless JWT via `JwtFilter`. RBAC enforced strictly via `@PreAuthorize("hasAuthority(...)")`. No DB hits happen on token verification (optimized).
2. **Data**: Controller → DTO Validation (`@Valid`) → Service → Business Validator (e.g., `UserValidator`) → Repository → MongoDB.
3. **Cross-Module Boundaries**: Excellent use of Spring `ApplicationEventPublisher`. For example, `LeadService` emits `LeadConvertedEvent`, and `ClientModule` listens silently to generate a new `Client` without tight service-to-service coupling.

---

## PHASE 2 — AUDIT FINDINGS

### 🚨 CRITICAL SEVERITY

**AUDIT-001: Deleted/Suspended users can still login**
* **Module:** Auth
* **File:** `AuthServiceImpl.java`
* **Method:** `login()`
* **Problem:** The `login` method verifies the email and password but never checks `user.getStatus()`.
* **Why it matters:** An `ARCHIVED`, `SUSPENDED`, or `DELETED` employee can successfully log in, receive a valid JWT, and access the ERP.
* **Recommended Fix:** Add `if(user.getStatus() != UserStatus.ACTIVE) throw new UnauthorizedException("User account is not active");`.

**AUDIT-002: Massive JWT Expiration Window (24 hours)**
* **Module:** Config
* **File:** `application-dev.yml` & `application-prod.yml`
* **Problem:** `app.jwt.expiration` is set to `86400000` (24 hours). 
* **Why it matters:** Because your JWTs are stateless (not checked against the DB to save performance), if an employee is fired and soft-deleted, their *existing* JWT will remain fully authorized to destroy/download company data for up to 24 hours. 
* **Recommended Fix:** Lower token expiration to `900000` (15 minutes). You already built a robust Refresh Token system—use it to seamlessly rotate the 15-minute tokens!

### 🔴 HIGH SEVERITY

**AUDIT-003: AccessDeniedException swallowed as HTTP 500**
* **Module:** Exception
* **File:** `GlobalExceptionHandler.java`
* **Problem:** Missing handler for Spring Security's `AccessDeniedException`. 
* **Why it matters:** If a user without `invoice:delete` tries to delete an invoice, `@PreAuthorize` throws an `AccessDeniedException`. Instead of returning a proper `403 Forbidden`, your generic `Exception.class` handler catches it, logs a massive stack trace, and returns a confusing `500 Internal Server Error`.
* **Recommended Fix:** Add `@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)` returning `403`.

**AUDIT-004: Missing Transactional Boundaries on Invoice History**
* **Module:** Invoice
* **File:** `InvoiceServiceImpl.java`
* **Method:** `updateInvoice()`, `updateInvoiceStatus()`, `deleteInvoice()`
* **Problem:** These methods save to `InvoiceHistoryRepository` and then `InvoiceRepository`, but lack `@Transactional`.
* **Why it matters:** If the application crashes or MongoDB throws a lock exception exactly between those two saves, the system will record an audit history for a financial change that actually failed to save.
* **Recommended Fix:** Add `@Transactional` to all state-mutating methods in `InvoiceServiceImpl`.

**AUDIT-005: Unbounded Rate Limiter Memory Leak (OOM Risk)**
* **Module:** Config
* **File:** `RateLimitFilter.java`
* **Problem:** Uses an unbounded `ConcurrentHashMap<String, Bucket> buckets`.
* **Why it matters:** A botnet hitting your login endpoint with random spoofed IPs will infinitely expand this map in memory until the JVM crashes with an `OutOfMemoryError`.
* **Recommended Fix:** Use a time-based eviction cache (like `Caffeine Cache`) instead of `ConcurrentHashMap`.

**AUDIT-006: Trivial Rate Limit Bypass via X-Forwarded-For**
* **Module:** Config
* **File:** `RateLimitFilter.java`
* **Method:** `getClientIp()`
* **Problem:** Blindly parses the raw `X-Forwarded-For` header. 
* **Why it matters:** An attacker can just send `X-Forwarded-For: random-ip-1`, `random-ip-2` on every request to instantly bypass bucket4j limiters.
* **Recommended Fix:** Rely on Spring's built-in trusted proxy resolution rather than manually parsing the header.

### 🟠 MEDIUM SEVERITY

**AUDIT-007: Soft Delete Leakage in `findAll` queries**
* **Module:** Auth / Lead
* **File:** `UserServiceImpl.java`, `LeadServiceImpl.java`
* **Method:** `getAllUsers()`, `getAllLeads()`
* **Problem:** They use `repository.findAll(pageable)`.
* **Why it matters:** Hitting the pagination endpoints will return `DELETED` users and leads.
* **Recommended Fix:** Switch to `findByStatusNot(Status.DELETED)`.

**AUDIT-008: Soft Delete Leakage in Text Searches**
* **Module:** Auth / Lead
* **File:** `UserRepositoryCustomImpl.java`, `LeadRepositoryCustomImpl.java`
* **Problem:** Text search queries do not filter by status (unlike `ClientRepositoryCustomImpl` which correctly appends `.ne(ARCHIVED)`).
* **Recommended Fix:** Add `query.addCriteria(Criteria.where("status").ne("DELETED"))`.

**AUDIT-009: Unique Indexes Block Soft Deletion Re-registration**
* **Module:** Database / Entities
* **File:** `User.java`, `Client.java`, `Lead.java`
* **Problem:** Fields like `email` have `@Indexed(unique = true)`.
* **Why it matters:** If a Lead with `bob@email.com` is `DELETED`, that email stays in the DB. If Bob returns a year later, creating a new Lead for `bob@email.com` will crash with a MongoDB Duplicate Key Exception.
* **Recommended Fix:** Convert them to partial unique indexes (ignoring deleted records).

**AUDIT-010: Invoices/Projects can be billed to ARCHIVED Clients**
* **Module:** Invoice / Project
* **File:** `InvoiceValidator.java`, `ProjectValidator.java`
* **Problem:** `validateClientExists()` uses `clientRepository.existsById()`.
* **Why it matters:** `existsById` ignores the `status` flag. You can accidentally create an invoice for a soft-deleted agency client.
* **Recommended Fix:** Change to `existsByIdAndStatusNot(clientId, ClientStatus.ARCHIVED)`.

**AUDIT-011: Cross-Module Database Injection**
* **Module:** Client / Lead
* **File:** `ClientLeadEventListener.java`
* **Problem:** The `Client` module directly injects `LeadRepository`.
* **Why it matters:** Violates strict modular boundaries. A module should never directly access another module's repository.
* **Recommended Fix:** `ClientModule` should publish a `ClientCreatedEvent`, and `LeadModule` should listen to it to update its own DB.

### 🟡 LOW SEVERITY
* **AUDIT-012 (Performance):** Pagination parameters are completely unbounded. Hitting `/api/v1/users?size=1000000` will crash the server. Ensure `spring.data.web.pageable.max-page-size=100` is set in properties.
* **AUDIT-013 (Docker):** `docker-compose.yml` runs MongoDB as `STANDALONE`. This breaks `@Transactional` methods locally (like Lead conversion), as Mongo requires Replica Sets for ACID transactions. 

---

## PHASE 3 — TEST RESULTS
*   **Build/Compile:** PASS (`./gradlew clean build` succeeded)
*   **Unit/Integration Tests:** NOT TESTED (Zero test classes exist in `src/test/java`).
*   **Database Syntax:** PASS
*   **Module Event Publishing:** PASS
*   **Docker Config:** PASS (Multi-stage layer caching implemented beautifully).

---

## PHASE 4 — PREVIOUS ISSUE REGRESSION
*   API versioning (`/api/v1`): **PASS**
*   Lead/Client tight coupling: **PASS** (ApplicationEventPublisher fixed this)
*   Invoice NPE safe calculation: **PASS** (`Optional.ofNullable` is solid)
*   Refresh Token rotation/revocation: **PASS**
*   Missing MongoDB indexes: **PASS**
*   Soft-delete leakage: **PARTIAL FAIL** (Fixed in `findById`, but `getAll` and `search` were missed—see AUDIT-007, 008).

---

## PHASE 5 — FINAL RISK ASSESSMENT
*   **Architecture:** 8.5/10 (Excellent decoupling, just one listener boundary leak).
*   **Security:** 5/10 (Failed to check DELETED status on login, and stateless JWT persistence is a huge vulnerability).
*   **Database:** 7/10 (Missing partial filters for soft-delete uniqueness).
*   **Performance:** 8.5/10 (RateLimiter memory leak is the main risk).
*   **Code Quality:** 9/10 (Exceptionally clean, consistent DTO/Mapper pattern).
*   **Testing:** 0/10 (No automated tests).

**Overall Project Readiness: NEEDS WORK**

---

## PHASE 6 — RECOMMENDED FIX ORDER
1. **AUDIT-001 (Auth Fix):** Add active status checks to `login()` immediately so deleted users cannot access the system.
2. **AUDIT-002 (Token Lifecycle):** Lower JWT expiration from 24h to 15m to permanently neutralize the risk of stateless tokens remaining active after employee termination.
3. **AUDIT-003 (Access Denied):** Add `AccessDeniedException` to the `GlobalExceptionHandler` so the frontend actually receives a `403` instead of crashing with a `500`.
4. **AUDIT-005 & 006 (Rate Limiter):** Replace `ConcurrentHashMap` with an expiring Cache and fix IP spoofing to prevent trivial DDOS/OOM attacks.
5. **AUDIT-004 (Transactions):** Add `@Transactional` to `InvoiceServiceImpl` to guarantee financial audit trail consistency.
6. **AUDIT-007, 008, 010 (Data Leaks):** Patch the remaining `.findAll()` and Text Search endpoints that forgot to exclude `DELETED`/`ARCHIVED` statuses.
7. **AUDIT-009 (Indexes):** Convert standard `@Indexed(unique=true)` to Partial Indexes to solve the soft-delete re-registration bug.
