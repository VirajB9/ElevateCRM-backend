# PROJECT CONTEXT — Digital Marketing Agency ERP Backend (ElevateCRM)

> This document describes the current architectural state of ElevateCRM based on the actual codebase. It serves as the primary technical context for software engineers and AI assistants working on this repository.

---

## 1. What Is This Project?

ElevateCRM is a comprehensive enterprise Spring Boot backend for Digital Marketing Agencies. It orchestrates the complete end-to-end agency operational lifecycle:
$$\mathbf{Leads} \longrightarrow \mathbf{Clients} \longrightarrow \mathbf{Projects} \longrightarrow \mathbf{Invoices}$$

Key Enterprise Subsystems:
- **Authentication & Perimeter Defense**: Stateless JWT, Refresh Token Rotation with SHA-256 hashing, and Bucket4j token-bucket rate limiting.
- **Role-Based Access Control (RBAC)**: Fine-grained 4-tier hierarchy (`OWNER` > `MANAGER` > `EMPLOYEE` > `INTERN`) enforcing `module:action` authorities.
- **Dynamic Navigation Engine**: Permission-filtered hierarchical menu tree built with SpEL caching.
- **Financial Invoicing**: Statutory sequential invoice generation, line-item tax and discount math, multi-state lifecycle, and immutable audit history snapshots.
- **Defensive Persistence**: Optimistic Concurrency Control (`@Version`), SPI cross-module deletion guards, and multi-document transactions on MongoDB replica sets.

---

## 2. Tech Stack

- **Runtime & Framework**: **Java 17** • **Spring Boot 3.5.15** • **Gradle 9.x**
- **Persistence**: **Spring Data MongoDB** (MongoDB 7.0+ with replica set transactions via `MongoTransactionManager`)
- **Security**: **Spring Security 6.x** + **JJWT 0.12.6** (stateless sessions, HMAC-SHA256)
- **Rate Limiting**: **Bucket4j Core 8.10.1** (in-memory token bucket on authentication endpoints)
- **Testing**: **JUnit 5**, **Mockito**, **Spring Boot Test**, **Testcontainers 1.20.4** (`mongo:7.0.5` replica set)
- **Documentation**: **SpringDoc OpenAPI 2.8.9** (Swagger UI at `/swagger-ui.html`)
- **Observability**: **Spring Boot Actuator** (`/actuator/health`, `/actuator/info`, `/actuator/metrics`)

---

## 3. Package Structure

```
com.viraj.dmabackend
├── auth/
│   ├── bootstrap/          PermissionBootstrap (1), RoleBootstrap (2), OwnerBootstrap (3)
│   ├── controller/         AuthController, UserController, RoleController
│   ├── dto/                LoginRequest, RefreshTokenRequest, LogoutRequest, CreateUserRequest,
│   │                       CreateUserResponse, UpdateUserRequest, UpdateUserStatusRequest,
│   │                       UserResponse, RoleResponse, UpdateRoleRequest, AssignPermissionsRequest,
│   │                       RemovePermissionsRequest, PermissionResponse
│   ├── entity/             User, Role, Permission, RefreshToken
│   ├── enums/              PermissionType, UserStatus
│   ├── exception/          UserNotFoundException, RoleNotFoundException, PermissionNotFoundException,
│   │                       DuplicateEmailException, DuplicatePhoneException, DuplicatePermissionException,
│   │                       InvalidUserStatusException, SystemRoleModificationException, AccountLockedException
│   ├── mapper/             RoleMapper, UserMapper
│   ├── repository/         UserRepository, UserRepositoryCustom, RoleRepository, PermissionRepository, RefreshTokenRepository
│   ├── security/           JwtFilter, JwtUtil, CustomUserDetails, CustomUserDetailsService
│   ├── service/            AuthService, UserService, RoleService, PermissionService
│   │   └── impl/           AuthServiceImpl, UserServiceImpl, RoleServiceImpl, PermissionServiceImpl
│   └── validator/          UserValidator, RoleValidator
│
├── lead/
│   ├── controller/         LeadController
│   ├── dto/                CreateLeadRequest, UpdateLeadRequest, LeadResponse
│   ├── entity/             Lead
│   ├── enums/              LeadStatus, LeadSource
│   ├── event/              LeadConvertedEvent
│   ├── exception/          LeadNotFoundException, DuplicateLeadException, InvalidLeadStatusException,
│   │                       LeadAlreadyConvertedException
│   ├── listener/           LeadConversionEventListener
│   ├── mapper/             LeadMapper
│   ├── repository/         LeadRepository, LeadRepositoryCustom, LeadRepositoryCustomImpl
│   ├── service/            LeadService & LeadServiceImpl
│   └── validator/          LeadValidator, LeadUserDeletionGuard
│
├── client/
│   ├── controller/         ClientController
│   ├── dto/                CreateClientRequest, UpdateClientRequest, ClientResponse
│   ├── entity/             Client
│   ├── enums/              ClientStatus
│   ├── exception/          ClientNotFoundException, DuplicateClientEmailException,
│   │                       DuplicateClientPhoneException, DuplicateClientGstException
│   ├── mapper/             ClientMapper
│   ├── repository/         ClientRepository, ClientRepositoryCustom, ClientRepositoryCustomImpl
│   ├── service/            ClientService & ClientServiceImpl
│   └── validator/          ClientValidator, ClientDeletionGuard
│
├── project/
│   ├── controller/         ProjectController
│   ├── dto/                CreateProjectRequest, UpdateProjectRequest, ProjectResponse
│   ├── entity/             Project
│   ├── enums/              ProjectStatus, ProjectPriority
│   ├── exception/          ProjectNotFoundException, DuplicateProjectException, InvalidProjectDateException
│   ├── mapper/             ProjectMapper
│   ├── repository/         ProjectRepository, ProjectRepositoryCustom, ProjectRepositoryCustomImpl
│   ├── service/            ProjectService & ProjectServiceImpl
│   └── validator/          ProjectValidator, ProjectClientDeletionGuard
│
├── invoice/
│   ├── controller/         InvoiceController
│   ├── dto/                CreateInvoiceRequest, UpdateInvoiceRequest, InvoiceResponse,
│   │                       InvoiceItemRequest, InvoiceHistoryResponse
│   ├── entity/             Invoice, InvoiceHistory
│   ├── enums/              InvoiceStatus
│   ├── exception/          InvoiceNotFoundException, DuplicateInvoiceNumberException, InvalidInvoiceStatusException
│   ├── mapper/             InvoiceMapper
│   ├── repository/         InvoiceRepository, InvoiceHistoryRepository
│   ├── service/            InvoiceService, InvoiceServiceImpl, InvoiceNumberGenerator
│   └── validator/          InvoiceValidator, InvoiceClientDeletionGuard, InvoiceProjectDeletionGuard,
│                           InvoiceProjectCancellationGuard, InvoiceProjectCompletionGuard
│
├── menu/
│   ├── bootstrap/          MenuBootstrap (4)
│   ├── controller/         MenuController
│   ├── dto/                CreateMenuRequest, UpdateMenuRequest, MenuResponse, MenuTreeResponse
│   ├── entity/             Menu
│   ├── exception/          MenuNotFoundException, ParentMenuNotFoundException, DuplicateMenuTitleException,
│   │                       DuplicateMenuPathException, InvalidMenuPathException, SelfParentMenuException
│   ├── mapper/             MenuMapper
│   ├── repository/         MenuRepository
│   ├── service/            MenuService & MenuServiceImpl
│   └── validator/          MenuValidator
│
├── permission/
│   └── controller/         PermissionController
│
├── common/
│   ├── counter/            Counter, CounterRepository, CounterRepositoryImpl
│   ├── dto/                LeadConversionData
│   ├── entity/             BaseEntity (@Id, createdAt, updatedAt, createdBy, updatedBy, @Version version, active, deleted)
│   ├── pagination/         PaginationUtils
│   ├── response/           ApiResponse<T>
│   └── util/               PasswordGenerator
│
├── config/
│   ├── AuditorAwareConfig.java
│   ├── CorsProperties.java
│   ├── MongoConfig.java
│   ├── OpenApiConfig.java
│   ├── RateLimitFilter.java
│   └── SecurityConfig.java
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── BadRequestException.java
│   ├── ResourceNotFoundException.java
│   └── UnauthorizedException.java
│
└── DmaBackendApplication.java
```

---

## 4. Security & Access Control

### Authentication & Token Lifecycle
1. **Login** (`POST /api/v1/auth/login`): Verifies credentials via BCrypt, issues short-lived JWT access token (15m) and secure refresh token (7d).
2. **Refresh** (`POST /api/v1/auth/refresh`): Rotates refresh token, hashes new token with SHA-256 in MongoDB `refresh_tokens`, revokes old token, issues new JWT.
3. **Logout** (`POST /api/v1/auth/logout`): Revokes active refresh token and invalidates active session.
4. **Rate Limiting**: `RateLimitFilter` throttles auth endpoints to 5 requests/minute per client IP using Bucket4j.

### Authorization & RBAC
- Users hold one `roleId` mapping to a system or custom `Role`.
- Roles contain a list of `permissionIds` referencing `PermissionType` enums.
- Authorities are mapped canonically: `PermissionType.LEAD_CREATE` → `lead:create`.
- Controllers use `@PreAuthorize("hasAuthority('invoice:create')")` for declarative security.

### Public Endpoints (Permitted Without Auth)
```
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
GET  /v3/api-docs/**
GET  /swagger-ui/**
GET  /swagger-ui.html
GET  /actuator/health/**
```

---

## 5. Domain Enums

### `PermissionType` (27 values)
- **User**: `USER_CREATE`, `USER_READ`, `USER_UPDATE`, `USER_DELETE`
- **Client**: `CLIENT_CREATE`, `CLIENT_READ`, `CLIENT_UPDATE`, `CLIENT_DELETE`
- **Lead**: `LEAD_CREATE`, `LEAD_READ`, `LEAD_UPDATE`, `LEAD_DELETE`, `LEAD_CONVERT`
- **Project**: `PROJECT_CREATE`, `PROJECT_READ`, `PROJECT_UPDATE`, `PROJECT_DELETE`
- **Invoice**: `INVOICE_CREATE`, `INVOICE_READ`, `INVOICE_UPDATE`, `INVOICE_DELETE`
- **Role**: `ROLE_READ`, `ROLE_UPDATE`
- **Menu**: `MENU_CREATE`, `MENU_READ`, `MENU_UPDATE`, `MENU_DELETE`

### `UserStatus`
`ACTIVE, INACTIVE, SUSPENDED, LOCKED, DELETED`

### `ClientStatus`
`ACTIVE, INACTIVE, ARCHIVED`

### `LeadStatus`
`NEW, CONTACTED, QUALIFIED, PROPOSAL_SENT, NEGOTIATION, WON, CONVERTED, LOST, DELETED`

### `LeadSource`
`WEBSITE, GOOGLE, FACEBOOK, INSTAGRAM, LINKEDIN, REFERRAL, EMAIL, PHONE, WHATSAPP, MANUAL, OTHER`

### `ProjectStatus`
`PLANNING, ACTIVE, ON_HOLD, COMPLETED, CANCELLED, ARCHIVED`

### `ProjectPriority`
`LOW, MEDIUM, HIGH, URGENT`

### `InvoiceStatus`
`DRAFT, SENT, PAID, OVERDUE, CANCELLED`

---

## 6. MongoDB Entities & Schemas

All entities inherit from `BaseEntity`:
- `id` (`String` / `@Id`)
- `createdAt` (`Instant` / `@CreatedDate`)
- `updatedAt` (`Instant` / `@LastModifiedDate`)
- `createdBy` (`String` / `@CreatedBy`)
- `updatedBy` (`String` / `@LastModifiedBy`)
- `version` (`Long` / `@Version`) — Optimistic Concurrency Control
- `active` (`boolean`) & `deleted` (`boolean`) — Soft-deletion indicators

### Primary Collections
| Collection | Entity Class | Primary Responsibility |
| :--- | :--- | :--- |
| `users` | `User` | User profiles, BCrypt passwords, role assignments, lockouts |
| `roles` | `Role` | System roles (`OWNER`, `MANAGER`, `EMPLOYEE`, `INTERN`) and custom roles |
| `permissions` | `Permission` | Atomic permissions keyed by `PermissionType` |
| `refresh_tokens` | `RefreshToken` | SHA-256 hashed refresh tokens with TTL expiration |
| `leads` | `Lead` | Sales pipeline leads, estimated budgets, and conversion links |
| `clients` | `Client` | Converted clients, unique GSTINs, contact persons, and company profiles |
| `projects` | `Project` | Client campaigns, deliverables, start/end dates, budgets, and priority |
| `invoices` | `Invoice` | Statutory invoices, line items, tax/discount math, issue/due dates |
| `invoice_history` | `InvoiceHistory` | Immutable audit trail snapshots of invoice status modifications |
| `counters` | `Counter` | Atomic sequence counters for sequential invoice numbering (`INV-YYYY-XXXX`) |
| `menus` | `Menu` | Hierarchical navigation routes, display order, and permission gates |

---

## 7. Complete REST API Surface (`/api/v1/...`)

### Auth (`/api/v1/auth`)
| Verb | Path | Authority | Purpose |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Public | Authenticate user, issue JWT and refresh token |
| `POST` | `/api/v1/auth/refresh` | Public | Rotate refresh token, issue new JWT |
| `POST` | `/api/v1/auth/logout` | Public | Revoke refresh token and terminate session |

### Users (`/api/v1/users`)
| Verb | Path | Authority | Purpose |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/users` | `user:create` | Create new staff user |
| `GET` | `/api/v1/users` | `user:read` | List all users (paginated) |
| `GET` | `/api/v1/users/{userId}` | `user:read` | Get user by ID |
| `PUT` | `/api/v1/users/{userId}` | `user:update` | Update user details |
| `PATCH`| `/api/v1/users/{userId}/status` | `user:update` | Update user status (ACTIVE, SUSPENDED, etc.) |
| `DELETE`| `/api/v1/users/{userId}` | `user:delete` | Soft delete user |
| `GET` | `/api/v1/users/search` | `user:read` | Search users by keyword |

### Roles & Permissions (`/api/v1/roles`, `/api/v1/permissions`)
| Verb | Path | Authority | Purpose |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/roles` | `role:read` | List all roles |
| `GET` | `/api/v1/roles/{roleId}` | `role:read` | Get role by ID |
| `PUT` | `/api/v1/roles/{roleId}` | `role:update` | Update role permissions (non-system roles) |
| `DELETE`| `/api/v1/roles/{roleId}/permissions` | `role:update` | Remove specific permissions |
| `GET` | `/api/v1/roles/{roleId}/users` | `role:read` | List users assigned to a role |
| `GET` | `/api/v1/permissions` | `role:read` | List all system permissions |

### Leads (`/api/v1/leads`)
| Verb | Path | Authority | Purpose |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/leads` | `lead:create` | Create incoming sales lead |
| `GET` | `/api/v1/leads` | `lead:read` | List leads (paginated) |
| `GET` | `/api/v1/leads/{leadId}` | `lead:read` | Get lead by ID |
| `PUT` | `/api/v1/leads/{leadId}` | `lead:update` | Update lead status/details |
| `POST` | `/api/v1/leads/{leadId}/convert` | `lead:convert` | Convert WON lead to Client entity |
| `DELETE`| `/api/v1/leads/{leadId}` | `lead:delete` | Soft delete lead |
| `GET` | `/api/v1/leads/search` | `lead:read` | Search leads by keyword |
| `GET` | `/api/v1/leads/source` | `lead:read` | Filter leads by source |
| `GET` | `/api/v1/leads/status` | `lead:read` | Filter leads by pipeline status |
| `GET` | `/api/v1/leads/assigned-user/{userId}` | `lead:read` | Filter leads by sales representative |

### Clients (`/api/v1/clients`)
| Verb | Path | Authority | Purpose |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/clients` | `client:create` | Register new client |
| `GET` | `/api/v1/clients` | `client:read` | List all active clients |
| `GET` | `/api/v1/clients/{clientId}` | `client:read` | Get client by ID |
| `PUT` | `/api/v1/clients/{clientId}` | `client:update` | Update client profile/GSTIN |
| `DELETE`| `/api/v1/clients/{clientId}` | `client:delete` | Soft delete client (ARCHIVED) |
| `GET` | `/api/v1/clients/search` | `client:read` | Full-text search by company name |
| `GET` | `/api/v1/clients/status` | `client:read` | Filter clients by status |

### Projects (`/api/v1/projects`)
| Verb | Path | Authority | Purpose |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/projects` | `project:create` | Create client project/campaign |
| `GET` | `/api/v1/projects` | `project:read` | List projects (paginated) |
| `GET` | `/api/v1/projects/{projectId}` | `project:read` | Get project by ID |
| `PUT` | `/api/v1/projects/{projectId}` | `project:update` | Update project dates/budget |
| `DELETE`| `/api/v1/projects/{projectId}` | `project:delete` | Soft delete project |
| `GET` | `/api/v1/projects/client/{clientId}` | `project:read` | Get projects for client |
| `GET` | `/api/v1/projects/search` | `project:read` | Search projects |
| `GET` | `/api/v1/projects/status` | `project:read` | Filter projects by status |

### Invoices (`/api/v1/invoices`)
| Verb | Path | Authority | Purpose |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/invoices` | `invoice:create` | Create invoice with auto-generated sequence |
| `GET` | `/api/v1/invoices` | `invoice:read` | List invoices (paginated) |
| `GET` | `/api/v1/invoices/{invoiceId}` | `invoice:read` | Get invoice by ID |
| `GET` | `/api/v1/invoices/{invoiceId}/history` | `invoice:read` | Get immutable audit timeline snapshots |
| `PUT` | `/api/v1/invoices/{invoiceId}` | `invoice:update` | Update draft invoice line items/dates |
| `PATCH`| `/api/v1/invoices/{invoiceId}/status` | `invoice:update` | Transition invoice state (SENT, PAID, etc.) |
| `DELETE`| `/api/v1/invoices/{invoiceId}` | `invoice:delete` | Soft delete draft invoice |
| `GET` | `/api/v1/invoices/client/{clientId}` | `invoice:read` | Get invoices for client |
| `GET` | `/api/v1/invoices/project/{projectId}` | `invoice:read` | Get invoices for project |
| `GET` | `/api/v1/invoices/status` | `invoice:read` | Filter invoices by status |

### Navigation Menus (`/api/v1/menus`)
| Verb | Path | Authority | Purpose |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/menus` | `menu:create` | Create menu route node |
| `GET` | `/api/v1/menus` | `menu:read` | Flat list of menus |
| `GET` | `/api/v1/menus/{menuId}` | `menu:read` | Get menu by ID |
| `PUT` | `/api/v1/menus/{menuId}` | `menu:update` | Update route title/path/permission |
| `PATCH`/`DELETE` | `/api/v1/menus/{menuId}/deactivate` | `menu:delete` | Deactivate menu node |
| `GET` | `/api/v1/menus/tree` | `menu:read` | Get user permission-filtered hierarchical tree |

---

## 8. Current Implementation Status & Production Roadmap

### What Has Been Fully Implemented:
- ✅ **7 Core Feature Modules**: Auth/Users, RBAC/Roles, Permissions, Leads, Clients, Projects, Invoices, Menus.
- ✅ **Statutory Invoicing Engine**: Sequential number generator (`INV-YYYY-XXXX`), line-item tax calculation, discount guards, and audit history snapshots.
- ✅ **Automated Test Suite**: **82 test suites with 662 tests passing** cleanly in CI/CD.
- ✅ **Testcontainers Integration Harness**: Real MongoDB 7.0.5 replica set testing multi-document transactions.
- ✅ **Rate Limiting & Perimeter Hardening**: Bucket4j token bucket rate limiting on auth endpoints.
- ✅ **Containerization**: `Dockerfile` and `docker-compose.yml` for multi-service orchestration.

### Ongoing Production Hardening Tasks:
- 🔄 **Database Migration Engine**: Integrating Mongock/Flyway to ensure secondary and unique indexes are created automatically in production (`application-prod.yml`).
- 🔄 **Distributed Caching & Token Store**: Migrating in-memory rate limiters and refresh tokens to Redis for multi-instance horizontal scaling.
- 🔄 **CI/CD Pipeline Automation**: Hardened GitHub Actions workflow executing `./gradlew test` with containerized MongoDB on pull requests.
- 🔄 **Issue Remediation**: 65 verified forensic audit issues tracked on GitHub ([#340 to #404](https://github.com/VirajB9/ElevateCRM-backend/issues)).