# 🛠️ Development Guidelines & Engineering Standards

**Project**: Digital Marketing Agency ERP Backend  
**Version**: 2.0 (Refactored & Aligned with Codebase)  
**Tech Stack**: Java 17 • Spring Boot 3.5.15 • MongoDB • Gradle  
**Base Package**: `com.viraj.dmabackend`  

---

## 1. Development Philosophy
The objective is **NOT** just to complete the ERP. The objective is to learn how professional, portfolio-quality backend software is engineered.

Every implementation must be:
- **Readable & Maintainable**
- **Feature-Packaged & Scalable**
- **Production Ready**
- **Architecturally Sound**

> ⚠️ **Golden Rule**: Understanding always comes before coding. Never write or copy code without understanding the "why" behind every annotation, method, and package.

---

## 2. General Architecture Rules
1. **Feature-Based Packaging**: Code is organized by domain modules (`auth`, `lead`, `client`, `project`, `menu`, `invoice`), NOT by generic layers.
2. **Thin Controllers**: Controllers handle only HTTP routing, `@PreAuthorize` security checks, `@Valid` body annotations, and delegation to services.
3. **Services Own Logic**: All business logic, permission filtering, validation rules, and entity conversions reside strictly in the service layer.
4. **Repositories Only Access DB**: Repositories only contain Spring Data Mongo query methods. Zero business logic.
5. **Entities Are Encapsulated**: Never return `@Document` entity objects directly through REST APIs. Always communicate via DTOs.
6. **Unified API Responses**: Every API returns a standardized `ApiResponse<T>` wrapper (`success`, `message`, `data`).

---

## 3. Package Structure (Feature-Based Architecture)

```
com.viraj.dmabackend
├── auth/                          # Authentication, User Management, Roles & Permissions
│   ├── bootstrap/                 # PermissionBootstrap (1), RoleBootstrap (2), OwnerBootstrap (3)
│   ├── controller/                # AuthController, UserController, RoleController
│   ├── dto/                       # LoginRequest, CreateUserRequest, UserResponse, RoleResponse, etc.
│   ├── entity/                    # User, Role, Permission (@Document)
│   ├── enums/                     # PermissionType, UserStatus
│   ├── exception/                 # UserNotFoundException, DuplicateEmailException, etc.
│   ├── mapper/                    # RoleMapper, UserMapper
│   ├── repository/                # UserRepository, RoleRepository, PermissionRepository
│   ├── security/                  # JwtFilter, JwtUtil, CustomUserDetails, CustomUserDetailsService
│   ├── service/                   # AuthService, UserService, RoleService, PermissionService
│   └── validator/                 # RoleValidator
│
├── lead/                          # Lead CRM Module
│   ├── controller/                # LeadController
│   ├── dto/                       # CreateLeadRequest, UpdateLeadRequest, LeadResponse
│   ├── entity/                    # Lead
│   ├── enums/                     # LeadStatus, LeadSource
│   ├── exception/                 # LeadNotFoundException, DuplicateLeadException, etc.
│   ├── repository/                # LeadRepository
│   └── service/                   # LeadService
│
├── client/                        # Client Registry Module
│   ├── controller/                # ClientController
│   ├── dto/                       # CreateClientRequest, UpdateClientRequest, ClientResponse
│   ├── entity/                    # Client
│   ├── enums/                     # ClientStatus
│   ├── exception/                 # ClientNotFoundException, DuplicateClientEmailException, etc.
│   ├── repository/                # ClientRepository
│   └── service/                   # ClientService
│
├── project/                       # Project Management Module
│   ├── controller/                # ProjectController
│   ├── dto/                       # CreateProjectRequest, UpdateProjectRequest, ProjectResponse
│   ├── entity/                    # Project
│   ├── enums/                     # ProjectStatus, ProjectPriority
│   ├── exception/                 # ProjectNotFoundException, DuplicateProjectException, etc.
│   ├── repository/                # ProjectRepository
│   └── service/                   # ProjectService & ProjectServiceImpl
│
├── menu/                          # Dynamic Navigation Menu Module
│   ├── bootstrap/                 # MenuBootstrap (4)
│   ├── controller/                # MenuController
│   ├── dto/                       # CreateMenuRequest, UpdateMenuRequest, MenuResponse, MenuTreeResponse
│   ├── entity/                    # Menu
│   ├── exception/                 # MenuNotFoundException, DuplicateMenuPathException, etc.
│   ├── mapper/                    # MenuMapper
│   ├── repository/                # MenuRepository
│   ├── service/                   # MenuService & MenuServiceImpl
│   └── validator/                 # MenuValidator
│
├── common/                        # Cross-Cutting Shared Components
│   ├── entity/                    # BaseEntity (createdAt, updatedAt, createdBy, updatedBy)
│   ├── response/                  # ApiResponse<T>
│   └── util/                      # PasswordGenerator
│
├── config/                        # Spring Configuration
│   ├── SecurityConfig.java        # SecurityFilterChain, CORS, PasswordEncoder, Method Security
│   ├── OpenApiConfig.java         # Swagger OpenAPI docs setup
│   └── MongoConfig.java           # @EnableMongoAuditing
│
├── exception/                     # Global Exception Infrastructure
│   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│   ├── BadRequestException.java     # Base class for 400 Bad Request
│   ├── ResourceNotFoundException.java # Base class for 404 Not Found
│   └── UnauthorizedException.java   # Base class for 401 Unauthorized
│
└── DmaBackendApplication.java     # Application Main Entry Point
```

---

## 4. Naming Conventions

### Packages
- Lowercase only (e.g., `com.viraj.dmabackend.auth.controller`).
- Never use camelCase or uppercase in package names.

### Classes & Interfaces
- Use **PascalCase** (e.g., `UserServiceImpl`, `ClientRepository`, `PermissionType`).
- Domain entities live in `entity/` (e.g., `Lead`, `Client`, `Menu`).

### Enums
- Enum Class: **PascalCase** (`UserStatus`, `LeadStatus`, `ProjectPriority`).
- Enum Constants: **UPPER_CASE** (`ACTIVE`, `PROPOSAL_SENT`, `LEAD_CONVERT`, `HIGH`).

### Methods & Variables
- Use **camelCase** (e.g., `convertLeadToClient()`, `estimatedBudget`, `assignedUserId`).

---

## 5. Exception Handling Standards

### Rule: Inherit from Base Exceptions
**NEVER inherit directly from `java.lang.RuntimeException` for domain exceptions.**

All domain exceptions must inherit from one of the core base classes in `com.viraj.dmabackend.exception`:

1. **`ResourceNotFoundException`** (returns HTTP `404 Not Found` JSON)
   - Examples: `UserNotFoundException`, `ClientNotFoundException`, `LeadNotFoundException`, `ProjectNotFoundException`, `MenuNotFoundException`, `ParentMenuNotFoundException`.
2. **`BadRequestException`** (returns HTTP `400 Bad Request` JSON)
   - Examples: `DuplicateEmailException`, `DuplicateClientGstException`, `InvalidLeadStatusException`, `LeadAlreadyConvertedException`, `InvalidProjectDateException`, `SelfParentMenuException`.
3. **`UnauthorizedException`** (returns HTTP `401 Unauthorized` JSON)

### Global Exception Handler
`GlobalExceptionHandler` (`@RestControllerAdvice`) intercepts all base exceptions and formats clean responses:

```json
{
  "success": false,
  "message": "Client not found with id: 60d5ecf9b...",
  "data": null
}
```

---

## 6. RBAC & Security Architecture

1. **Single Role Per User**: Each user is assigned one `roleId` (`OWNER`, `MANAGER`, `EMPLOYEE`, `INTERN`).
2. **Role Hierarchy**: `OWNER` > `MANAGER` > `EMPLOYEE` > `INTERN`.
3. **Permission Format**: `module:action` string format (e.g., `lead:create`, `client:read`, `role:update`).
4. **Authority Mapping**: `CustomUserDetailsService` automatically maps `PermissionType.LEAD_CREATE` → `lead:create` authority string.
5. **Method Security**: Controllers enforce permissions via `@PreAuthorize("hasAuthority('lead:create')")`.
6. **System Role Protection**: Roles with `systemRole = true` (`OWNER`, `MANAGER`, `EMPLOYEE`, `INTERN`) cannot be modified or deleted via API.

---

## 7. Order-Based Bootstrap Chain

Application data is seeded automatically on startup using `CommandLineRunner` beans with strict `@Order` execution:

| Order | Class | Function |
|-------|-------|----------|
| `@Order(1)` | `PermissionBootstrap` | Seeds all `PermissionType` enum values into `permissions` collection |
| `@Order(2)` | `RoleBootstrap` | Seeds system roles (`OWNER`, `MANAGER`, `EMPLOYEE`, `INTERN`) with default permission IDs |
| `@Order(3)` | `OwnerBootstrap` | Seeds default System Owner user (`owner@agency.com` / `Owner@123`) |
| `@Order(4)` | `MenuBootstrap` | Seeds default ERP navigation hierarchy with parent-child relationships |

> 🔒 **Requirement**: All bootstraps must be idempotent (check if data exists before inserting).

---

## 8. Git Conventions & Workflow

### Commit Format (Conventional Commits)
```
<type>: <short descriptive summary>
```

Types allowed:
- `feat:` New feature or endpoint
- `fix:` Bug fix or security correction
- `refactor:` Code refactoring without changing functionality
- `docs:` Documentation or guideline updates
- `test:` Unit or integration test additions
- `chore:` Dependency or build configuration updates

❌ **Prohibited commit messages**: `done`, `updated`, `working`, `changes`, `final`.

### Golden Git Rule
> **One Logical Change = One Commit**

Commit checklist before every commit:
- [ ] Project compiles with zero errors (`./gradlew test`)
- [ ] Feature tested & verified via Swagger UI (`localhost:8080/swagger-ui.html`)
- [ ] No debug statements or unused imports
- [ ] Proper error handling implemented

---

## 9. Standard 13-Step Module Development Order

Every module follows this **exact 13-step sequence** without skipping steps:

1. **Architecture Discussion** (Fields, business rules, security context)
2. **Database Design** (Schema, indexes, references)
3. **Entity Creation** (`entity/` package, `@Document`, `@Id`, `@Indexed`)
4. **Repository** (`repository/` package, custom queries)
5. **DTOs** (`dto/` package, Request & Response objects)
6. **Mapper** (`mapper/` package, Entity ↔ DTO transformation)
7. **Service** (`service/` package, interface + `impl/` implementation)
8. **Controller** (`controller/` package, REST endpoints, `@PreAuthorize`, `@Valid`)
9. **Validation** (`validator/` package, business validation rules)
10. **Exception Handling** (`exception/` package, extending base exceptions)
11. **Swagger / Postman Testing** (End-to-end endpoint verification)
12. **Git Commit** (`git commit -m "feat: add <module> management"`)
13. **Git Push** (`git push origin <branch>`)

---

## 10. Instructor Mode & Learning Rules

When pair programming in **Instructor Mode**:
1. **Theory First**: Theory and architectural rationale are explained before any code is written.
2. **Collaborative Coding**: Code is written on the student's behalf only when explicitly requested as a learning aid.
3. **Partial Code Only**: Modifications focus strictly on the necessary changes, avoiding full copy-paste dumps.
4. **Explicit Location Reporting**: Every change explicitly highlights **EXACTLY WHERE** lines were added or modified.
5. **Progress Tracking**: `.gemini/progress.md` and `.gemini/concepts-learned.md` are updated to track learning milestones.

---

**Last Updated**: 2026-08-01  
**Status**: Active & Enforced Across Workspace
