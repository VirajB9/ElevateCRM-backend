# 🛠️ Development Guidelines & Engineering Standards

**Project**: Digital Marketing Agency ERP Backend (ElevateCRM)  
**Version**: 2.5 (Fully Integrated Invoicing, RBAC, Rate Limiting & Test Suite Baseline)  
**Tech Stack**: Java 17 • Spring Boot 3.5.15 • MongoDB 7.0+ • Gradle 9.x • JJWT 0.12.6 • Bucket4j 8.10.1  
**Base Package**: `com.viraj.dmabackend`  

---

## 1. Development Philosophy
The objective is **NOT** just to complete the ERP. The objective is to build a robust, production-grade, portfolio-quality backend system following Domain-Driven Design (DDD) principles and defensive software engineering.

Every implementation must be:
- **Readable & Maintainable**: Explicit naming, clear separation of concerns, and minimal cognitive load.
- **Feature-Packaged & Scalable**: Modular structure organized by domain boundaries rather than horizontal layers.
- **Production Ready**: Robust input validation, optimistic locking, idempotent operations, and secure perimeter defense.
- **Architecturally Sound**: Strict unidirectional data flow (`Controller → Service → Repository → Database`), zero entity leakage via REST, and defensive error propagation.

> ⚠️ **Golden Rule**: Understanding always comes before coding. Never write or copy code without understanding the "why" behind every annotation, method, query, and security filter.

---

## 2. General Architecture Rules
1. **Feature-Based Packaging**: Code is organized into dedicated domain modules (`auth`, `lead`, `client`, `project`, `invoice`, `menu`, `permission`), not generic horizontal layers.
2. **Thin Controllers**: Controllers handle only HTTP routing, OpenAPI documentation, `@PreAuthorize` security checks, `@Valid` request body validation, and immediate delegation to service interfaces.
3. **Services Own Business Logic**: All business invariants, validation rules, state transitions, calculation logic, and entity mutations reside strictly in service implementation classes (`*ServiceImpl`).
4. **Repositories Access Persistence**: Spring Data MongoDB repository interfaces and custom implementations (`*RepositoryCustomImpl`) only execute database queries and aggregations. Zero business logic belongs in repositories.
5. **Strict DTO Encapsulation**: Never return `@Document` entity objects directly through REST APIs. Always communicate via typed Request/Response DTOs using dedicated mappers.
6. **Unified API Envelopes**: Every API endpoint returns a standardized `ApiResponse<T>` wrapper (`success`, `message`, `data`).
7. **Optimistic Concurrency Control (OCC)**: All mutable entities inherit `@Version private Long version;` from `BaseEntity` to prevent concurrent write conflicts and lost updates.
8. **SPI Deletion & Status Guards**: Cross-module entity deletions and state transitions must be guarded by Service Provider Interface (SPI) beans (e.g., `ProjectClientDeletionGuard`, `InvoiceClientDeletionGuard`, `InvoiceProjectCancellationGuard`).
9. **Universal Pagination Clamping**: All paginated endpoints clamp page size (`max-page-size: 100`) and sanitize sort parameters against explicit whitelists via `PaginationUtils.clampPageable()`.

---

## 3. Package Structure (Feature-Based Architecture)

```
com.viraj.dmabackend
├── auth/                          # Authentication, User Management, Roles & Token Lifecycle
│   ├── bootstrap/                 # PermissionBootstrap (1), RoleBootstrap (2), OwnerBootstrap (3)
│   ├── controller/                # AuthController (/api/v1/auth), UserController (/api/v1/users), RoleController (/api/v1/roles)
│   ├── dto/                       # LoginRequest, RefreshTokenRequest, LogoutRequest, CreateUserRequest, UserResponse, RoleResponse, etc.
│   ├── entity/                    # User, Role, Permission, RefreshToken (@Document)
│   ├── enums/                     # PermissionType (27 values), UserStatus (ACTIVE, INACTIVE, SUSPENDED, LOCKED, DELETED)
│   ├── exception/                 # UserNotFoundException, DuplicateEmailException, AccountLockedException, etc.
│   ├── mapper/                    # RoleMapper, UserMapper
│   ├── repository/                # UserRepository, UserRepositoryCustom, RoleRepository, PermissionRepository, RefreshTokenRepository
│   ├── security/                  # JwtFilter, JwtUtil, CustomUserDetails, CustomUserDetailsService
│   ├── service/                   # AuthService, UserService, RoleService, PermissionService
│   │   └── impl/                  # AuthServiceImpl, UserServiceImpl, RoleServiceImpl, PermissionServiceImpl
│   └── validator/                 # UserValidator, RoleValidator
│
├── lead/                          # Lead CRM & Sales Pipeline Module
│   ├── controller/                # LeadController (/api/v1/leads)
│   ├── dto/                       # CreateLeadRequest, UpdateLeadRequest, LeadResponse
│   ├── entity/                    # Lead (@Document)
│   ├── enums/                     # LeadStatus (9 stages), LeadSource (11 channels)
│   ├── event/                     # LeadConvertedEvent
│   ├── exception/                 # LeadNotFoundException, DuplicateLeadException, InvalidLeadStatusException, etc.
│   ├── listener/                  # LeadConversionEventListener
│   ├── mapper/                    # LeadMapper
│   ├── repository/                # LeadRepository, LeadRepositoryCustom, LeadRepositoryCustomImpl
│   ├── service/                   # LeadService & LeadServiceImpl
│   └── validator/                 # LeadValidator, LeadUserDeletionGuard
│
├── client/                        # Client Registry & Account Management Module
│   ├── controller/                # ClientController (/api/v1/clients)
│   ├── dto/                       # CreateClientRequest, UpdateClientRequest, ClientResponse
│   ├── entity/                    # Client (@Document)
│   ├── enums/                     # ClientStatus (ACTIVE, INACTIVE, ARCHIVED)
│   ├── exception/                 # ClientNotFoundException, DuplicateClientEmailException, DuplicateClientGstException, etc.
│   ├── mapper/                    # ClientMapper
│   ├── repository/                # ClientRepository, ClientRepositoryCustom, ClientRepositoryCustomImpl
│   ├── service/                   # ClientService & ClientServiceImpl
│   └── validator/                 # ClientValidator, ClientDeletionGuard
│
├── project/                       # Project Delivery & Campaign Management Module
│   ├── controller/                # ProjectController (/api/v1/projects)
│   ├── dto/                       # CreateProjectRequest, UpdateProjectRequest, ProjectResponse
│   ├── entity/                    # Project (@Document)
│   ├── enums/                     # ProjectStatus (6 states), ProjectPriority (LOW, MEDIUM, HIGH, URGENT)
│   ├── exception/                 # ProjectNotFoundException, DuplicateProjectException, InvalidProjectDateException
│   ├── mapper/                    # ProjectMapper
│   ├── repository/                # ProjectRepository, ProjectRepositoryCustom, ProjectRepositoryCustomImpl
│   ├── service/                   # ProjectService & ProjectServiceImpl
│   └── validator/                 # ProjectValidator, ProjectClientDeletionGuard
│
├── invoice/                       # Financial Invoicing & Billing Module
│   ├── controller/                # InvoiceController (/api/v1/invoices)
│   ├── dto/                       # CreateInvoiceRequest, UpdateInvoiceRequest, InvoiceResponse, InvoiceItemRequest, InvoiceHistoryResponse
│   ├── entity/                    # Invoice, InvoiceHistory (@Document)
│   ├── enums/                     # InvoiceStatus (DRAFT, SENT, PAID, OVERDUE, CANCELLED)
│   ├── exception/                 # InvoiceNotFoundException, DuplicateInvoiceNumberException, InvalidInvoiceStatusException
│   ├── mapper/                    # InvoiceMapper
│   ├── repository/                # InvoiceRepository, InvoiceHistoryRepository
│   ├── service/                   # InvoiceService, InvoiceServiceImpl, InvoiceNumberGenerator
│   └── validator/                 # InvoiceValidator, InvoiceClientDeletionGuard, InvoiceProjectDeletionGuard, InvoiceProjectCancellationGuard
│
├── menu/                          # Dynamic Navigation Menu Module
│   ├── bootstrap/                 # MenuBootstrap (4)
│   ├── controller/                # MenuController (/api/v1/menus)
│   ├── dto/                       # CreateMenuRequest, UpdateMenuRequest, MenuResponse, MenuTreeResponse
│   ├── entity/                    # Menu (@Document)
│   ├── exception/                 # MenuNotFoundException, ParentMenuNotFoundException, DuplicateMenuPathException, etc.
│   ├── mapper/                    # MenuMapper
│   ├── repository/                # MenuRepository
│   ├── service/                   # MenuService & MenuServiceImpl
│   └── validator/                 # MenuValidator
│
├── permission/                    # Permission Query Module
│   └── controller/                # PermissionController (/api/v1/permissions)
│
├── common/                        # Shared Enterprise Infrastructure
│   ├── counter/                   # Counter, CounterRepository, CounterRepositoryImpl (Atomic Sequence Generator)
│   ├── dto/                       # LeadConversionData
│   ├── entity/                    # BaseEntity (@Id, createdAt, updatedAt, createdBy, updatedBy, @Version version, active, deleted)
│   ├── pagination/                # PaginationUtils (Pageable clamping, sorting whitelists)
│   ├── response/                  # ApiResponse<T>
│   └── util/                      # PasswordGenerator
│
├── config/                        # Spring Framework & Infrastructure Configuration
│   ├── AuditorAwareConfig.java    # Spring Data AuditorAware (SecurityContext principal injection)
│   ├── CorsProperties.java        # Typed configuration properties (`cors.allowed-origins`)
│   ├── MongoConfig.java           # @EnableMongoAuditing, MongoTransactionManager (Replica Set transactions)
│   ├── OpenApiConfig.java         # Swagger OpenAPI 3.0 specification & security schemes
│   ├── RateLimitFilter.java       # Bucket4j token bucket rate limiting on /api/v1/auth/**
│   └── SecurityConfig.java        # SecurityFilterChain, BCrypt, Stateless Session, Route Rules
│
├── exception/                     # Global Exception Infrastructure
│   ├── GlobalExceptionHandler.java  # @RestControllerAdvice with RFC 7807 problem details
│   ├── BadRequestException.java     # Base class for 400 Bad Request
│   ├── ResourceNotFoundException.java # Base class for 404 Not Found
│   └── UnauthorizedException.java   # Base class for 401 Unauthorized
│
└── DmaBackendApplication.java     # Application Main Entry Point
```

---

## 4. Naming Conventions

### Packages
- Lowercase only (e.g., `com.viraj.dmabackend.invoice.controller`).
- Never use camelCase, underscores, or uppercase in package names.

### Classes & Interfaces
- Use **PascalCase** (e.g., `InvoiceServiceImpl`, `ClientRepository`, `PermissionType`).
- Service interfaces omit prefixes; implementations carry the `Impl` suffix (e.g., `InvoiceService` → `InvoiceServiceImpl`).
- Domain entities live in `entity/` (e.g., `Lead`, `Client`, `Invoice`, `InvoiceHistory`).

### Enums
- Enum Class: **PascalCase** (`UserStatus`, `LeadStatus`, `ProjectStatus`, `InvoiceStatus`).
- Enum Constants: **UPPER_SNAKE_CASE** (`ACTIVE`, `PROPOSAL_SENT`, `INVOICE_CREATE`, `OVERDUE`).

### REST API Endpoints
- Plural nouns in lowercase kebab-case: `/api/v1/invoices`, `/api/v1/leads`, `/api/v1/projects`.
- All endpoints must include the `/api/v1/` version prefix.

---

## 5. Exception Handling Standards

### Rule: Inherit from Base Exceptions
**NEVER inherit directly from `java.lang.RuntimeException` for domain exceptions.**

All domain exceptions must inherit from one of the core base classes in `com.viraj.dmabackend.exception`:

1. **`ResourceNotFoundException`** (HTTP `404 Not Found`)
   - `UserNotFoundException`, `ClientNotFoundException`, `LeadNotFoundException`, `ProjectNotFoundException`, `InvoiceNotFoundException`, `MenuNotFoundException`, `RoleNotFoundException`.
2. **`BadRequestException`** (HTTP `400 Bad Request`)
   - `DuplicateEmailException`, `DuplicateClientGstException`, `DuplicateInvoiceNumberException`, `InvalidInvoiceStatusException`, `InvalidLeadStatusException`, `InvalidProjectDateException`, `LeadAlreadyConvertedException`.
3. **`UnauthorizedException`** (HTTP `401 Unauthorized`)
   - `InvalidCredentialsException`, `TokenExpiredException`, `AccountLockedException`.

### Standardized Error Envelope
`GlobalExceptionHandler` (`@RestControllerAdvice`) catches all domain exceptions and formats clean responses:

```json
{
  "success": false,
  "message": "Invoice not found with id: 6733b8a1c...",
  "data": null
}
```

---

## 6. Security, Authentication & Rate Limiting

1. **Stateless JWT**: Access tokens (15-minute validity) signed using HMAC-SHA256 via JJWT 0.12.6.
2. **Refresh Token Rotation**: Refresh tokens (7-day validity) stored in `refresh_tokens` collection with SHA-256 `tokenHash`. Rotated on every use with revocation detection.
3. **Rate Limiting**: `RateLimitFilter` (Bucket4j) enforces a 5 requests/minute token bucket per IP on `/api/v1/auth/**`.
4. **4-Tier RBAC Hierarchy**: `OWNER` > `MANAGER` > `EMPLOYEE` > `INTERN`.
5. **Authority Format**: Canonical `module:action` string format (e.g., `invoice:create`, `lead:convert`, `client:read`).
6. **Method Security**: Enforced via `@PreAuthorize("hasAuthority('invoice:create')")` on all secured controller endpoints.
7. **System Role Immutability**: System roles (`systemRole = true`) cannot be renamed, modified, or deleted via REST API.

---

## 7. Order-Based Bootstrap Chain

Application seed data is initialized deterministically on startup via `CommandLineRunner` beans with strict `@Order` execution:

| Order | Class | Function |
|---|---|---|
| `@Order(1)` | `PermissionBootstrap` | Seeds all 27 `PermissionType` enum values into `permissions` collection |
| `@Order(2)` | `RoleBootstrap` | Seeds default system roles (`OWNER`, `MANAGER`, `EMPLOYEE`, `INTERN`) with permission IDs |
| `@Order(3)` | `OwnerBootstrap` | Seeds initial System Owner account (`owner@agency.com`) with BCrypt password |
| `@Order(4)` | `MenuBootstrap` | Seeds hierarchical navigation menu tree with parent-child references |

> 🔒 **Requirement**: All bootstraps must be idempotent (check existence before inserting).

---

## 8. Git Conventions & Workflow

### Commit Format (Conventional Commits)
```
<type>: <short descriptive summary>
```

Allowed types:
- `feat:` New feature or endpoint
- `fix:` Bug fix or security remediation
- `refactor:` Code refactoring without changing functionality
- `docs:` Documentation or guideline updates
- `test:` Unit or integration test additions
- `chore:` Dependency or build configuration updates

### Pre-Commit Checklist
- [ ] All tests pass cleanly (`./gradlew test`) — 662/662 baseline verified
- [ ] No application compiler warnings or broken imports
- [ ] Verified against OpenAPI Swagger UI (`/swagger-ui.html`)
- [ ] Zero hardcoded secrets, test credentials, or plaintext tokens

---

## 9. Standard 13-Step Module Development Order

Every module follows this **exact 13-step sequence**:

1. **Architecture Discussion**: Define entities, invariants, relationships, and security permissions.
2. **Database Design**: Collection schema, secondary indexes, compound indexes, and uniqueness constraints.
3. **Entity Creation**: Declare `@Document`, `@Id`, `@Indexed`, `@Version`, and extend `BaseEntity`.
4. **Repository**: Declare Spring Data interface and optional `*RepositoryCustom` for dynamic aggregations.
5. **DTOs**: Typed Request and Response DTOs with Jakarta validation constraints (`@NotBlank`, `@NotNull`, etc.).
6. **Mapper**: Dedicated mapper class transforming Entity ↔ DTO with null safety and string trimming.
7. **Service Interface & Implementation**: Service contract in `service/` and logic in `service/impl/`.
8. **SPI Guards & Validators**: Cross-domain deletion and status guards registered in Spring context.
9. **Controller**: Thin REST endpoints with `@RequestMapping("/api/v1/...")`, `@PreAuthorize`, and `@Valid`.
10. **Exception Handling**: Domain-specific exceptions inheriting from base classes with clean error messages.
11. **Unit & WebMvc Testing**: Mockito service unit tests and MockMvc controller verification tests.
12. **Integration Testing**: Testcontainers MongoDB (`mongo:7.0.5`) replica set integration tests.
13. **Git Commit & Push**: Conventional commit referencing verified issue or task.

---

**Last Updated**: November 2026  
**Status**: Active, Verified & Enforced across ElevateCRM Codebase