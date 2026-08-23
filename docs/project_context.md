# PROJECT CONTEXT — Digital Marketing Agency ERP Backend

> This document describes the current state of this project based on the actual codebase. It is intended for any AI assistant that needs to work on this project.

---

## What Is This Project?

A Spring Boot backend for a Digital Marketing Agency ERP system. It manages the full business lifecycle: **Leads → Clients → Projects → (Invoices planned)**. It has a complete RBAC security system and a dynamic navigation menu module.

---

## Tech Stack

- **Java 17** + **Spring Boot 3.5.15** + **Gradle**
- **MongoDB** (localhost:27017, database: `digital_marketing_agency`)
- **Spring Security** + **JWT** (jjwt 0.11.5, HS256, stateless sessions)
- **Lombok** (annotations: `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, etc.)
- **Jakarta Bean Validation** (`@Valid`, `@NotBlank`, `@Email`, etc.)
- **SpringDoc OpenAPI 2.8.9** (Swagger UI at `/swagger-ui.html`)
- **Spring Boot Actuator** (`/actuator/health`)

---

## Architecture

**Feature-based packaging** — each module is a self-contained package, NOT organized by layers.

**Data flow**: `Controller → Service → Repository → MongoDB`

**Rules**:
- Controllers are thin — only HTTP mapping, `@PreAuthorize`, `@Valid`, and delegation
- Services contain all business logic
- Entities are MongoDB `@Document` classes, never exposed via API
- DTOs are used for all API communication
- Repositories only do database operations

---

## Package Structure

```
com.viraj.dmabackend
├── auth/
│   ├── bootstrap/          PermissionBootstrap, RoleBootstrap, OwnerBootstrap
│   ├── controller/         AuthController, UserController, RoleController
│   ├── dto/                LoginRequest, AuthenticationResponse, CreateUserRequest, CreateUserResponse,
│   │                       UpdateUserRequest, UpdateUserStatusRequest, UserResponse,
│   │                       RoleResponse, UpdateRoleRequest, AssignPermissionsRequest,
│   │                       RemovePermissionsRequest, PermissionResponse
│   ├── entity/             User, Role, Permission
│   ├── enums/              PermissionType, UserStatus
│   ├── exception/          UserNotFoundException, RoleNotFoundException, PermissionNotFoundException,
│   │                       DuplicateEmailException, DuplicatePhoneException, DuplicatePermissionException,
│   │                       InvalidUserStatusException, SystemRoleModificationException,
│   │                       UnauthorizedRoleAssignmentException
│   ├── mapper/             RoleMapper, UserMapper
│   ├── repository/         UserRepository, RoleRepository, PermissionRepository
│   ├── security/           JwtFilter, JwtUtil, CustomUserDetails, CustomUserDetailsService
│   ├── service/            AuthService (interface), UserService (interface), RoleService (interface),
│   │                       PermissionService (interface)
│   │   └── impl/           AuthServiceImpl, UserServiceImpl, RoleServiceImpl, PermissionServiceImpl
│   └── validator/          RoleValidator
│
├── lead/
│   ├── controller/         LeadController
│   ├── dto/                CreateLeadRequest, UpdateLeadRequest, LeadResponse
│   ├── entity/             Lead
│   ├── enums/              LeadStatus, LeadSource
│   ├── exception/          LeadNotFoundException, DuplicateLeadException, InvalidLeadStatusException,
│   │                       LeadAlreadyConvertedException
│   ├── repository/         LeadRepository
│   └── service/            LeadService
│
├── client/
│   ├── controller/         ClientController
│   ├── dto/                CreateClientRequest, UpdateClientRequest, ClientResponse
│   ├── entity/             Client
│   ├── enums/              ClientStatus
│   ├── exception/          ClientNotFoundException, DuplicateClientEmailException,
│   │                       DuplicateClientPhoneException, DuplicateClientGstException
│   ├── repository/         ClientRepository
│   └── service/            ClientService
│
├── project/
│   ├── controller/         ProjectController
│   ├── dto/                CreateProjectRequest, UpdateProjectRequest, ProjectResponse
│   ├── entity/             Project
│   ├── enums/              ProjectStatus, ProjectPriority
│   ├── exception/          ProjectNotFoundException, DuplicateProjectException, InvalidProjectDateException
│   ├── repository/         ProjectRepository
│   └── service/            ProjectService (interface) + impl/ProjectServiceImpl
│
├── menu/
│   ├── bootstrap/          MenuBootstrap
│   ├── controller/         MenuController
│   ├── dto/                CreateMenuRequest, UpdateMenuRequest, MenuResponse, MenuTreeResponse
│   ├── entity/             Menu
│   ├── exception/          MenuNotFoundException, ParentMenuNotFoundException, DuplicateMenuTitleException,
│   │                       DuplicateMenuPathException, InvalidMenuPathException, SelfParentMenuException
│   ├── mapper/             MenuMapper
│   ├── repository/         MenuRepository
│   ├── service/            MenuService (interface) + impl/MenuServiceImpl
│   └── validator/          MenuValidator
│
├── common/
│   ├── entity/             BaseEntity (createdAt, updatedAt, createdBy, updatedBy)
│   ├── response/           ApiResponse<T> (success, message, data)
│   └── util/               PasswordGenerator
│
├── config/
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
│   └── MongoConfig.java    (@EnableMongoAuditing)
│
├── controller/
│   └── HealthController.java
│
├── exception/
│   ├── GlobalExceptionHandler.java   (@RestControllerAdvice)
│   ├── BadRequestException.java      (base for 400s)
│   ├── ResourceNotFoundException.java (base for 404s)
│   └── UnauthorizedException.java    (base for 401s)
│
└── DmaBackendApplication.java
```

---

## Security

### Authentication
- `POST /api/auth/login` → validates email/password → returns JWT token
- All other endpoints require `Authorization: Bearer <token>` header
- `JwtFilter` (extends `OncePerRequestFilter`) intercepts requests, extracts token, validates via `JwtUtil`, loads user via `CustomUserDetailsService`, and sets `SecurityContextHolder`

### Authorization (RBAC)
- Each user has one `roleId` → role has a list of `permissionIds` → each permission maps to a `PermissionType` enum
- `CustomUserDetailsService` converts enum names to authority strings: `LEAD_CREATE` → `lead:create`
- Controllers use `@PreAuthorize("hasAuthority('lead:create')")` for method-level security
- `@EnableMethodSecurity` is on `SecurityConfig`

### JWT Configuration (`application-dev.yml`)
```yaml
app:
  jwt:
    secret: <YOUR_JWT_SECRET_HERE>
    expiration: 86400000  # 24 hours
```
`JwtUtil` reads these via `@Value("${app.jwt.secret}")` and `@Value("${app.jwt.expiration}")`.

### Public Endpoints (no auth required)
```
POST /api/auth/login
GET  /v3/api-docs/**
GET  /swagger-ui/**
GET  /swagger-ui.html
GET  /actuator/health
```

### CORS
Allowed origins: `localhost:5173`, `localhost:5174`, `localhost:3000`, `*.up.railway.app`

---

## Bootstrap Chain (Application Startup)

Four `CommandLineRunner` beans run in order via `@Order`:

| Order | Class | What It Does |
|-------|-------|-------------|
| 1 | `PermissionBootstrap` | Iterates `PermissionType.values()`, inserts any missing permissions into `permissions` collection |
| 2 | `RoleBootstrap` | Creates 4 system roles: OWNER (all perms), MANAGER (subset), EMPLOYEE (subset), INTERN (read-only) |
| 3 | `OwnerBootstrap` | Creates default owner: `owner@agency.com` / `Owner@123` with OWNER role |
| 4 | `MenuBootstrap` | Seeds hierarchical navigation menu tree with parent-child references |

All bootstraps are idempotent (skip if data already exists).

### System Roles & Their Permissions

| Role | Permissions |
|------|------------|
| **OWNER** | ALL permissions |
| **MANAGER** | user:create/read/update, client:create/read/update, lead:create/read/update/convert, project:create/read/update, invoice:read, role:read, menu:read |
| **EMPLOYEE** | client:read, lead:read, project:read/update, invoice:read, menu:read |
| **INTERN** | client:read, lead:read, project:read, menu:read |

System roles have `systemRole = true` — cannot be modified or deleted via API.

---

## All Enums

### `PermissionType` (29 values)
```
USER_CREATE, USER_READ, USER_UPDATE, USER_DELETE
CLIENT_CREATE, CLIENT_READ, CLIENT_UPDATE, CLIENT_DELETE
LEAD_CREATE, LEAD_READ, LEAD_UPDATE, LEAD_DELETE, LEAD_CONVERT
PROJECT_CREATE, PROJECT_READ, PROJECT_UPDATE, PROJECT_DELETE
INVOICE_CREATE, INVOICE_READ, INVOICE_UPDATE, INVOICE_DELETE
ROLE_CREATE, ROLE_READ, ROLE_UPDATE, ROLE_DELETE
MENU_CREATE, MENU_READ, MENU_UPDATE, MENU_DELETE
```

### `UserStatus`
`ACTIVE, INACTIVE, SUSPENDED, LOCKED, DELETED`

### `ClientStatus`
`ACTIVE, INACTIVE, ARCHIVED`

### `LeadStatus`
`NEW, CONTACTED, QUALIFIED, PROPOSAL_SENT, NEGOTIATION, WON, LOST`

### `LeadSource`
`WEBSITE, GOOGLE, FACEBOOK, INSTAGRAM, LINKEDIN, REFERRAL, EMAIL, PHONE, WHATSAPP, MANUAL, OTHER`

### `ProjectStatus`
`PLANNING, ACTIVE, ON_HOLD, COMPLETED, CANCELLED`

### `ProjectPriority`
`LOW, MEDIUM, HIGH, URGENT`

---

## MongoDB Collections & Entities

All entities extend `BaseEntity` which has `createdAt` (`@CreatedDate`), `updatedAt` (`@LastModifiedDate`), `createdBy`, `updatedBy`.

### `users`
| Field | Type | Notes |
|-------|------|-------|
| id | String | `@Id` |
| firstName | String | |
| lastName | String | |
| email | String | `@Indexed(unique = true)` |
| phoneNumber | String | `@Indexed(unique = true)` |
| password | String | BCrypt hashed |
| roleId | String | References `roles.id` |
| status | UserStatus | Default: `ACTIVE` |

### `roles`
| Field | Type | Notes |
|-------|------|-------|
| id | String | `@Id` |
| name | String | `@Indexed(unique = true)` |
| description | String | |
| permissionIds | List\<String\> | References `permissions.id` |
| systemRole | boolean | Default: `false`. `true` = immutable |

### `permissions`
| Field | Type | Notes |
|-------|------|-------|
| id | String | `@Id` |
| permissionType | PermissionType | Enum |
| module | String | |
| description | String | |

### `leads`
| Field | Type | Notes |
|-------|------|-------|
| id | String | `@Id` |
| firstName, lastName | String | |
| companyName | String | |
| email | String | `@Indexed(unique = true)` |
| phoneNumber | String | `@Indexed(unique = true)` |
| website, industry | String | |
| source | LeadSource | Enum |
| status | LeadStatus | Default: `NEW` |
| assignedUserId | String | References `users.id` |
| estimatedBudget | BigDecimal | |
| requirements, notes | String | |
| convertedClientId | String | Set when lead is converted to client |
| convertedAt | LocalDateTime | Set when lead is converted |

### `clients`
| Field | Type | Notes |
|-------|------|-------|
| id | String | `@Id` |
| companyName, contactPerson | String | |
| email | String | `@Indexed(unique = true)` |
| phoneNumber | String | `@Indexed(unique = true)` |
| website, industry | String | |
| gstNumber | String | `@Indexed(unique = true, sparse = true)` |
| address, city, state, country, postalCode | String | |
| notes | String | |
| status | ClientStatus | Default: `ACTIVE`. Soft-delete sets `ARCHIVED` |

### `projects`
| Field | Type | Notes |
|-------|------|-------|
| id | String | `@Id` |
| clientId | String | `@NotBlank`, references `clients.id` |
| projectName | String | `@NotBlank` |
| description | String | |
| status | ProjectStatus | Default: `PLANNING` |
| priority | ProjectPriority | Default: `MEDIUM` |
| startDate, endDate | LocalDate | End date cannot be before start date |
| budget | BigDecimal | `@DecimalMin("0.0")` |
| notes | String | |

### `menus`
| Field | Type | Notes |
|-------|------|-------|
| id | String | `@Id` |
| title | String | `@Indexed(unique = true)` |
| path | String | `@Indexed(unique = true)` |
| icon | String | |
| parentId | String | `@Indexed`, self-reference for tree hierarchy |
| orderIndex | Integer | Numeric sort order |
| requiredPermission | String | `@Indexed`, e.g. `"lead:read"` |
| active | Boolean | Default: `true` |

### Entity Relationships (All by String ID — no DBRef)
```
User.roleId            → Role.id
Role.permissionIds     → [Permission.id]
Lead.assignedUserId    → User.id
Lead.convertedClientId → Client.id
Project.clientId       → Client.id
Menu.parentId          → Menu.id  (self-referencing tree)
```

---

## All API Endpoints

### Auth (`/api/auth`)
| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| POST | `/api/auth/login` | Public | Login, returns JWT |

### Users (`/api/users`)
| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| POST | `/api/users` | `user:create` | Create user |
| GET | `/api/users` | `user:read` | Get all users (paginated) |
| GET | `/api/users/{userId}` | `user:read` | Get user by ID |
| PUT | `/api/users/{userId}` | `user:update` | Update user |
| PATCH | `/api/users/{userId}/status` | `user:update` | Update user status |
| DELETE | `/api/users/{userId}` | `user:delete` | Soft delete user |
| GET | `/api/users/search?keyword=` | `user:read` | Search users |
| GET | `/api/users/filter?status=` | `user:read` | Filter by status |

### Roles (`/api/roles`)
| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| GET | `/api/roles` | `role:read` | Get all roles |
| GET | `/api/roles/{roleId}` | `role:read` | Get role by ID |
| PUT | `/api/roles/{roleId}` | `role:update` | Update role |
| PATCH | `/api/roles/{roleId}/permissions` | `role:update` | Assign permissions |
| DELETE | `/api/roles/{roleId}/permissions` | `role:update` | Remove permissions |
| GET | `/api/roles/{roleId}/users` | `role:read` | Get users by role (paginated) |

### Leads (`/api/leads`)
| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| POST | `/api/leads` | `lead:create` | Create lead |
| GET | `/api/leads` | `lead:read` | Get all leads (paginated) |
| GET | `/api/leads/{leadId}` | `lead:read` | Get lead by ID |
| PUT | `/api/leads/{leadId}` | `lead:update` | Update lead |
| DELETE | `/api/leads/{leadId}` | `lead:delete` | Delete lead |
| POST | `/api/leads/{leadId}/convert` | `lead:convert` | Convert lead to client |
| GET | `/api/leads/search?keyword=` | `lead:read` | Search leads |
| GET | `/api/leads/source?source=` | `lead:read` | Filter by source |
| GET | `/api/leads/assigned-user/{userId}` | `lead:read` | Filter by assigned user |

### Clients (`/api/clients`)
| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| POST | `/api/clients` | `client:create` | Create client |
| GET | `/api/clients` | `client:read` | Get all clients (excludes ARCHIVED) |
| GET | `/api/clients/{clientId}` | `client:read` | Get client by ID |
| PUT | `/api/clients/{clientId}` | `client:update` | Update client |
| DELETE | `/api/clients/{clientId}` | `client:delete` | Soft delete (→ ARCHIVED) |
| GET | `/api/clients/search?keyword=` | `client:read` | Search by company name |
| GET | `/api/clients/status?status=` | `client:read` | Filter by status |

### Projects (`/api/projects`)
| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| POST | `/api/projects` | `project:create` | Create project |
| GET | `/api/projects` | `project:read` | Get all projects (paginated) |
| GET | `/api/projects/{projectId}` | `project:read` | Get project by ID |
| PUT | `/api/projects/{projectId}` | `project:update` | Update project |
| DELETE | `/api/projects/{projectId}` | `project:delete` | Delete project |
| GET | `/api/projects/search?keyword=` | `project:read` | Search projects |
| GET | `/api/projects/status?status=` | `project:read` | Filter by status |
| GET | `/api/projects/client/{clientId}` | `project:read` | Get projects by client |

### Menus (`/api/menus`)
| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| POST | `/api/menus` | `menu:create` | Create menu item |
| GET | `/api/menus` | `menu:read` | Get all menus (flat list) |
| GET | `/api/menus/{menuId}` | `menu:read` | Get menu by ID |
| PUT | `/api/menus/{menuId}` | `menu:update` | Update menu |
| PATCH | `/api/menus/{menuId}/deactivate` | `menu:delete` | Deactivate menu |
| GET | `/api/menus/tree` | `menu:read` | Get permission-filtered menu tree for current user |

---

## Exception Handling

### Base Classes (in `exception/` package)
| Class | HTTP Status |
|-------|-------------|
| `ResourceNotFoundException` | 404 Not Found |
| `BadRequestException` | 400 Bad Request |
| `UnauthorizedException` | 401 Unauthorized |

### All Domain Exceptions Extend Base Classes

**Extends `ResourceNotFoundException` (→ 404)**:
`UserNotFoundException`, `RoleNotFoundException`, `PermissionNotFoundException`, `LeadNotFoundException`, `ClientNotFoundException`, `ProjectNotFoundException`, `MenuNotFoundException`, `ParentMenuNotFoundException`

**Extends `BadRequestException` (→ 400)**:
`DuplicateEmailException`, `DuplicatePhoneException`, `DuplicatePermissionException`, `InvalidUserStatusException`, `SystemRoleModificationException`, `UnauthorizedRoleAssignmentException`, `DuplicateLeadException`, `InvalidLeadStatusException`, `LeadAlreadyConvertedException`, `DuplicateClientEmailException`, `DuplicateClientPhoneException`, `DuplicateClientGstException`, `DuplicateProjectException`, `InvalidProjectDateException`, `DuplicateMenuTitleException`, `DuplicateMenuPathException`, `InvalidMenuPathException`, `SelfParentMenuException`

### GlobalExceptionHandler (`@RestControllerAdvice`)
```
ResourceNotFoundException → 404 with ApiResponse
BadRequestException       → 400 with ApiResponse
UnauthorizedException     → 401 with ApiResponse
Exception (fallback)      → 500 with ApiResponse
```

### Standard Response Wrapper
```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
```

---

## Key Business Logic

### Lead → Client Conversion
When `POST /api/leads/{id}/convert` is called:
1. Validates lead exists and hasn't been converted already
2. Creates a new `Client` document from lead data (name, email, phone, company, etc.)
3. Sets `lead.convertedClientId = client.id` and `lead.convertedAt = now()`

### Client Soft Delete
`DELETE /api/clients/{id}` sets `status = ARCHIVED`. All GET queries exclude `ARCHIVED` clients.

### Menu Tree Building (`GET /api/menus/tree`)
1. Fetch all active menus sorted by `orderIndex`
2. Filter menus by checking `requiredPermission` against current user's authorities from `SecurityContextHolder`
3. Build parent-child tree using `parentId` references
4. Return nested `MenuTreeResponse` (each node has `List<MenuTreeResponse> children`)

### Seeded Menu Hierarchy
```
Dashboard (/dashboard)                    [no permission required]
Leads CRM (/leads)                        [lead:read]
├── All Leads (/leads/all)                [lead:read]
└── Create Lead (/leads/new)              [lead:create]
Client Management (/clients)              [client:read]
├── All Clients (/clients/all)            [client:read]
└── Add Client (/clients/new)             [client:create]
Project Management (/projects)            [project:read]
├── All Projects (/projects/all)          [project:read]
└── New Project (/projects/new)           [project:create]
Invoice & Billing (/invoices)             [invoice:read]
├── All Invoices (/invoices/all)          [invoice:read]
└── Create Invoice (/invoices/new)        [invoice:create]
System Administration (/settings)         [role:read]
├── User Accounts (/settings/users)       [user:read]
└── Roles & Permissions (/settings/roles) [role:read]
```

---

## Configuration

### Profile System
- `application.yml` → `spring.profiles.active: ${SPRING_PROFILES_ACTIVE:dev}`
- `application-dev.yml` → local dev config
- `application-prod.yml` → production config (exists but incomplete)

### Dev Config (`application-dev.yml`)
```yaml
spring:
  application:
    name: digital-marketing-agency-backend
  data:
    mongodb:
      host: localhost
      port: 27017
      database: digital_marketing_agency
server:
  port: 8080
app:
  jwt:
    secret: <YOUR_JWT_SECRET_HERE>
    expiration: 86400000
```

### Default Login
```
Email:    owner@agency.com
Password: <YOUR_SECURE_PASSWORD>
```

---

## What Has NOT Been Built Yet

- **Invoice module** — no code exists for it yet
- **Unit/Integration tests** — no test classes written
- **MapStruct** — not added to `build.gradle`, mappers are manual
- **Docker** — no Dockerfile or docker-compose
- **CI/CD** — no pipeline configured
- **Refresh tokens** — not implemented
- **Audit fields** — `createdBy`/`updatedBy` exist in `BaseEntity` but are not auto-populated
- **`application-prod.yml`** — incomplete
