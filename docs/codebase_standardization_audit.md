# 🔍 ERP Codebase Standardization Audit

> **Date**: August 16, 2026
> **Scope**: Complete source code analysis of all 100+ Java files across 10 modules
> **Objective**: Identify every inconsistency in architecture, patterns, naming, and implementation

---

## 🚨 Critical Bug Found

> [!CAUTION]
> ### Lead Entity: `@Document(collation)` instead of `@Document(collection)`
> **File**: [`Lead.java`](file:///home/sayless/DMA/DMA-backend/src/main/java/com/viraj/dmabackend/lead/entity/Lead.java)
> ```java
> @Document(collation = "leads")  // ❌ BUG — "collation" is for text sorting rules
> @Document(collection = "leads") // ✅ CORRECT
> ```
> This means leads are **not being stored in the "leads" collection**. MongoDB is using the default class-derived collection name instead. This is a **data integrity issue** that needs immediate fixing.

---

## 1. Module Architecture Matrix

### 1.1 Service Layer Pattern

| Module | Interface | Impl Class | Pattern |
|--------|:---------:|:----------:|---------|
| Auth (User) | ✅ `UserService` | ✅ `UserServiceImpl` | Interface + Impl |
| Auth (Role) | ✅ `RoleService` | ✅ `RoleServiceImpl` | Interface + Impl |
| Auth (Permission) | ✅ `PermissionService` | ✅ `PermissionServiceImpl` | Interface + Impl |
| Auth (Auth) | ✅ `AuthService` | ✅ `AuthServiceImpl` | Interface + Impl |
| **Lead** | ❌ | `LeadService` (concrete) | **No interface** |
| **Client** | ❌ | `ClientService` (concrete) | **No interface** |
| Project | ✅ `ProjectService` | ✅ `ProjectServiceImpl` | Interface + Impl |
| Menu | ✅ `MenuService` | ✅ `MenuServiceImpl` | Interface + Impl |
| Invoice | ✅ `InvoiceService` | ✅ `InvoiceServiceImpl` | Interface + Impl |

> [!IMPORTANT]
> **Lead** and **Client** modules use concrete service classes — no interface. Every other module uses the Interface + Impl pattern.

---

### 1.2 Mapper Pattern

| Module | Has Mapper? | Approach | Location |
|--------|:-----------:|----------|----------|
| Auth (User) | ✅ `UserMapper` | `@Component` manual | `auth/mapper/` |
| Auth (Role) | ✅ `RoleMapper` | `@Component` manual | `auth/mapper/` |
| **Auth (Permission)** | ❌ | **Inline `mapPermission()` in service** | — |
| **Lead** | ❌ | **Inline `mapToLeadResponse()` in service** | — |
| **Client** | ❌ | **Inline `mapClient()` in service** | — |
| **Project** | ❌ | **Inline `mapToProjectResponse()` in service** | — |
| Menu | ✅ `MenuMapper` | `@Component` manual | `menu/mapper/` |
| Invoice | ✅ `InvoiceMapper` | `@Component` manual | `invoice/mapper/` |

> [!WARNING]
> **4 modules** (Permission, Lead, Client, Project) have inline mapping buried inside services. The project standard (established by Menu & Invoice) is a dedicated `@Component` Mapper class.

---

### 1.3 Validator Pattern

| Module | Has Validator? | Approach |
|--------|:-------------:|----------|
| Auth (Role) | ✅ `RoleValidator` | `@Component` |
| **Auth (User)** | ❌ | **Inline in `UserServiceImpl`** |
| **Lead** | ❌ | **Inline in `LeadService`** |
| **Client** | ❌ | **Inline in `ClientService`** |
| **Project** | ❌ | **Inline in `ProjectServiceImpl`** |
| Menu | ✅ `MenuValidator` | `@Component` |
| Invoice | ✅ `InvoiceValidator` | `@Component` |

> [!WARNING]
> **4 modules** (User, Lead, Client, Project) have inline validation. The project standard (Menu, Invoice, Auth/Role) is a dedicated `@Component` Validator class.

---

### 1.4 Complete Module Comparison

| Feature | Auth | Lead | Client | Project | Menu | Invoice |
|---------|:----:|:----:|:------:|:-------:|:----:|:-------:|
| Service Interface + Impl | ✅ | ❌ | ❌ | ✅ | ✅ | ✅ |
| Dedicated Mapper | ✅* | ❌ | ❌ | ❌ | ✅ | ✅ |
| Dedicated Validator | ✅* | ❌ | ❌ | ❌ | ✅ | ✅ |
| `@TypeAlias` on Entity | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Custom Exceptions | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Feature-based Packaging | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Swagger Tags | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `@PreAuthorize` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

*Auth has mappers for User and Role but NOT for Permission. Auth has validator for Role but NOT for User.

**🏆 Gold Standard Modules** (follow all patterns): **Menu** and **Invoice**

---

## 2. Controller Inconsistencies

### 2.1 Dependency Injection: Interface vs Concrete

| Controller | Injects | Type | Correct? |
|-----------|---------|------|:--------:|
| `AuthController` | `AuthServiceImpl` | ❌ Concrete class | ❌ |
| `UserController` | `UserServiceImpl` | ❌ Concrete class | ❌ |
| `RoleController` | `RoleService` | ✅ Interface | ✅ |
| `LeadController` | `LeadService` | N/A (no interface) | ⚠️ |
| `ClientController` | `ClientService` | N/A (no interface) | ⚠️ |
| `ProjectController` | `ProjectService` | ✅ Interface | ✅ |
| `MenuController` | `MenuService` | ✅ Interface | ✅ |
| `InvoiceController` | `InvoiceService` | ✅ Interface | ✅ |

> [!IMPORTANT]
> `AuthController` and `UserController` inject **concrete implementations** instead of interfaces. This violates Dependency Inversion Principle.

### 2.2 `hasAuthority` vs `hasAnyAuthority` Misuse

| Controller | Pattern | Issue |
|-----------|---------|-------|
| `UserController` | `hasAuthority('...')` | ✅ Correct |
| `RoleController` | `hasAuthority('...')` | ✅ Correct |
| `LeadController` | `hasAuthority('...')` | ✅ Correct |
| `ClientController` | `hasAuthority('...')` | ✅ Correct |
| `ProjectController` | `hasAuthority('...')` | ✅ Correct |
| **`MenuController`** | **Mixed** | ⚠️ Uses `hasAnyAuthority` for create & getAll, `hasAuthority` for others |
| **`InvoiceController`** | **`hasAnyAuthority` everywhere** | ❌ Uses `hasAnyAuthority('invoice:create')` for single permissions |

> [!NOTE]
> `hasAnyAuthority` is for checking **multiple** permissions (OR logic). Using it with a single permission string works but is semantically wrong and inconsistent. Exception: `updateInvoiceStatus` correctly uses `hasAnyAuthority('invoice:update', 'invoice:pay')` for dual-permission check.

### 2.3 API Response Wrapping

| Component | Uses `ApiResponse<T>`? |
|-----------|:---------------------:|
| `HealthController` | ✅ |
| `GlobalExceptionHandler` | ✅ (for errors) |
| All other controllers | ❌ (return raw DTOs) |

`ApiResponse` exists and is used for errors, but **no business controller wraps success responses**. This is technically consistent across business controllers (all raw), but inconsistent with the error handling pattern.

---

## 3. DTO Inconsistencies

### 3.1 Lombok Annotation Pattern

| Module | Request DTOs | Response DTOs |
|--------|-------------|---------------|
| Auth | `@Getter @Setter` | `@Getter @Builder` (some have `@Setter`) |
| Lead | `@Getter @Setter` | `@Getter @Setter @Builder` |
| Client | `@Getter @Setter` | `@Getter @Builder` (no `@Setter`) |
| Project | `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor` | Same full set |
| Menu | `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor` | Same full set |
| Invoice | `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor` | Same full set |

> [!WARNING]
> **No consistent DTO annotation standard.**
> - Auth & Lead Request DTOs: minimal (`@Getter @Setter`)
> - Project/Menu/Invoice DTOs: full annotations (`@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`)
> - Response DTOs: mixed — some have `@Setter`, some don't

### 3.2 Audit Fields in Response DTOs

| Response DTO | `createdAt` | `updatedAt` | `createdBy` | `updatedBy` |
|-------------|:-----------:|:-----------:|:-----------:|:-----------:|
| `UserResponse` | ❌ | ❌ | ❌ | ❌ |
| `LeadResponse` | ✅ | ✅ | ✅ | ✅ |
| `ClientResponse` | ✅ | ✅ | ❌ | ❌ |
| `ProjectResponse` | ✅ | ✅ | ✅ | ✅ |
| `MenuResponse` | ❌ | ❌ | ❌ | ❌ |
| `InvoiceResponse` | ✅ | ✅ | ❌ | ❌ |

> [!WARNING]
> Completely inconsistent audit field exposure. Should be standardized — either all responses include audit fields or none do.

---

## 4. Exception Inconsistencies

### 4.1 Error Message Casing

| Exception | Message Style |
|----------|--------------|
| `UserNotFoundException` | `"User not found with id: "` (lowercase `id`) |
| `LeadNotFoundException` | `"Lead not found with ID: "` (uppercase `ID`) |
| `ClientNotFoundException` | `"Client not found with id: "` (lowercase `id`) |
| `ProjectNotFoundException` | `"Project not found with ID: "` (uppercase `ID`) |
| `MenuNotFoundException` | `"Menu not found with id: "` (lowercase `id`) |
| `InvoiceNotFoundException` | `"Invoice not found with id: "` (lowercase `id`) |

> **Lead** and **Project** use uppercase "ID" while all others use lowercase "id".

### 4.2 `UnauthorizedRoleAssignmentException` Wrong Parent

```java
// CURRENT — extends BadRequestException (400)
public class UnauthorizedRoleAssignmentException extends BadRequestException { ... }

// SHOULD BE — extends UnauthorizedException (401/403)
// An unauthorized action is NOT a bad request
```

### 4.3 `InvalidInvoiceStatusException` Constructor Style

```java
// Current — accepts generic String message
public InvalidInvoiceStatusException(String message) {
    super(message);
}

// Other modules' "InvalidStatus" exceptions use typed parameters:
public InvalidLeadStatusException(LeadStatus current, LeadStatus target) { ... }
public InvalidUserStatusException(UserStatus status) { ... }
```

### 4.4 AuthServiceImpl Uses Generic Exceptions

[`AuthServiceImpl.java`](file:///home/sayless/DMA/DMA-backend/src/main/java/com/viraj/dmabackend/auth/service/impl/AuthServiceImpl.java):
```java
// ❌ Uses generic ResourceNotFoundException
throw new ResourceNotFoundException("Role not found");

// ✅ Should use module-specific exception
throw new RoleNotFoundException(user.getRoleId());
```

---

## 5. Service Implementation Inconsistencies

### 5.1 `UserServiceImpl` Doesn't Use Its Own Mapper

[`UserServiceImpl.java`](file:///home/sayless/DMA/DMA-backend/src/main/java/com/viraj/dmabackend/auth/service/impl/UserServiceImpl.java):
- Has its own private `mapToUserResponse()`, `mapUser()` methods
- `UserMapper` exists as a `@Component` but is **never injected** in `UserServiceImpl`
- Only `RoleServiceImpl` actually uses `UserMapper` and `RoleMapper`

### 5.2 `AuthServiceImpl` Builds Response Manually

[`AuthServiceImpl.java`](file:///home/sayless/DMA/DMA-backend/src/main/java/com/viraj/dmabackend/auth/service/impl/AuthServiceImpl.java):
- Manually builds `UserResponse` via builder instead of using `UserMapper`
- This duplicates the mapping logic

### 5.3 `UserMapper` Is Incomplete

[`UserMapper.java`](file:///home/sayless/DMA/DMA-backend/src/main/java/com/viraj/dmabackend/auth/mapper/UserMapper.java):
- Does **NOT** set `roleName` field in `UserResponse`
- But `UserServiceImpl.mapToUserResponse()` DOES set `roleName`
- So the dedicated mapper produces an **incomplete** response object

### 5.4 Delete Strategy Inconsistency

| Module | Delete Strategy | Method |
|--------|----------------|--------|
| Auth (User) | Soft delete | Set `status = DELETED` |
| **Lead** | **Hard delete** | `repository.delete(lead)` |
| Client | Soft delete | Set `status = ARCHIVED` |
| **Project** | **Hard delete** | `repository.delete(project)` |
| Menu | Soft delete | Set `active = false` |
| Invoice | Soft delete | Set `active = false` |

> [!IMPORTANT]
> Lead and Project use **hard delete** while all other modules use **soft delete**. This is an architectural inconsistency — hard deletes permanently destroy data with no recovery option.

---

## 6. Entity Inconsistencies

### 6.1 Validation on Entity vs DTO

| Entity | Has Jakarta Validation? |
|--------|:----------------------:|
| `User` | ❌ |
| `Lead` | ❌ |
| `Client` | ❌ |
| **`Project`** | ✅ `@NotBlank`, `@DecimalMin` on entity fields |
| `Menu` | ❌ |
| `Invoice` | ❌ |

> [!WARNING]
> **Project entity** has `@NotBlank` and `@DecimalMin` directly on entity fields. Validation belongs on DTOs, not entities. All other entities correctly keep validation-free.

### 6.2 `Counter` Entity Inconsistencies

[`Counter.java`](file:///home/sayless/DMA/DMA-backend/src/main/java/com/viraj/dmabackend/common/counter/Counter.java):
- Does **NOT** extend `BaseEntity` (no audit fields)
- Does **NOT** have `@TypeAlias`
- Minor — since it's infrastructure, not a business entity

---

## 7. Naming Inconsistencies

### 7.1 Helper Method Naming

| Module | Build Method | Map Method | Find Method |
|--------|-------------|-----------|-------------|
| Auth (User) | `buildUser()` | `mapToUserResponse()`, `mapUser()` | `findUserById()` |
| Lead | `buildLead()` | `mapToLeadResponse()` | `findLeadById()` |
| Client | `buildClient()` | `mapClient()` | `findClientById()` |
| Project | `buildProject()` | `mapToProjectResponse()` | `findProjectById()` |

**Mapping method names are inconsistent**: `mapToXResponse()` vs `mapX()` vs using a Mapper class.

### 7.2 Security Naming

| Class | Name |
|-------|------|
| JWT Filter | `JwtFilter` |
| JWT Utility | `JwtUtil` |

These are fine and consistent with each other. ✅

---

## 8. Standardization Rules (Recommended)

Based on the audit, here are the **gold standard patterns** that each module should follow:

### Standard Module Structure
```
module/
├── controller/       → Thin, delegates to service interface
├── dto/              → @Getter @Setter (Requests) | @Getter @Builder (Responses)  
├── entity/           → @Document @TypeAlias, extends BaseEntity, NO validation
├── enums/            → Simple enums
├── exception/        → Extends BadRequestException or ResourceNotFoundException
├── mapper/           → @Component, handles all DTO ↔ Entity conversion
├── repository/       → MongoRepository interface
├── service/          → Interface
├── service/impl/     → @Service implementation
└── validator/        → @Component, all business validation rules
```

### Rules Summary
1. **Service**: Always Interface + Impl in `service/impl/`
2. **Mapper**: Always dedicated `@Component` in `mapper/` — never inline
3. **Validator**: Always dedicated `@Component` in `validator/` — never inline
4. **Controller**: Inject **interface**, not concrete implementation
5. **Delete**: Soft delete everywhere (set status/active flag)
6. **Exceptions**: Module-specific, typed constructors, lowercase "id" in messages
7. **DTOs**: Consistent Lombok annotations across modules
8. **Entity**: NO Jakarta validation annotations — validation goes on DTOs only
9. **`@PreAuthorize`**: Use `hasAuthority()` for single permission, `hasAnyAuthority()` for multiple
10. **Audit fields**: Standardize which fields appear in responses

---

## 9. Priority Fix List

### 🔴 Priority 1 — Critical (Fix Immediately)
| # | Issue | File | Impact |
|---|-------|------|--------|
| 1 | `@Document(collation)` typo — should be `collection` | `Lead.java` | Data stored in wrong collection |

### 🟠 Priority 2 — Architecture (Fix Before New Features)
| # | Issue | Modules Affected |
|---|-------|-----------------|
| 2 | Missing Service Interface | Lead, Client |
| 3 | Missing Mapper class | Permission, Lead, Client, Project |
| 4 | Missing Validator class | User, Lead, Client, Project |
| 5 | Controller injects concrete class | AuthController, UserController |
| 6 | UserServiceImpl doesn't use UserMapper | Auth |
| 7 | AuthServiceImpl uses generic exceptions | Auth |
| 8 | Entity has validation annotations | Project |

### 🟡 Priority 3 — Consistency (Fix During Cleanup)
| # | Issue | Modules Affected |
|---|-------|-----------------|
| 9 | `hasAnyAuthority` for single permissions | Invoice, Menu |
| 10 | Delete strategy inconsistency (hard vs soft) | Lead, Project |
| 11 | Exception message casing (ID vs id) | Lead, Project |
| 12 | DTO Lombok annotation inconsistency | Auth, Lead, Client |
| 13 | Audit field inconsistency in responses | All modules |
| 14 | `UnauthorizedRoleAssignmentException` wrong parent | Auth |
| 15 | `InvalidInvoiceStatusException` generic constructor | Invoice |

---

## 10. Module-by-Module Fix Checklist

### Auth Module
- [x] `AuthController` → inject `AuthService` interface, not `AuthServiceImpl`
- [x] `UserController` → inject `UserService` interface, not `UserServiceImpl`
- [x] `UserServiceImpl` → inject and use `UserMapper` instead of inline mapping
- [x] `AuthServiceImpl` → use `UserMapper` and `RoleNotFoundException` instead of generic exceptions
- [x] Create `PermissionMapper` @Component
- [x] Create `UserValidator` @Component (extract validation from UserServiceImpl)
- [x] `UnauthorizedRoleAssignmentException` → extend `UnauthorizedException` instead of `BadRequestException`
- [x] `UserResponse` → add `createdAt`, `updatedAt` fields
- [x] `UserMapper.toUserResponse()` → include `roleName` field

### Lead Module
- [x] **FIX BUG**: `@Document(collation = "leads")` → `@Document(collection = "leads")`
- [x] Extract `LeadService` interface, rename current class to `LeadServiceImpl`
- [x] Create `LeadMapper` @Component
- [x] Create `LeadValidator` @Component
- [x] `LeadNotFoundException` → lowercase "id" in message
- [x] Consider soft delete instead of hard delete

### Client Module
- [x] Extract `ClientService` interface, rename current class to `ClientServiceImpl`
- [x] Create `ClientMapper` @Component
- [x] Create `ClientValidator` @Component
- [x] `ClientResponse` → add `createdBy`, `updatedBy` fields

### Project Module
- [x] Create `ProjectMapper` @Component
- [x] Create `ProjectValidator` @Component
- [x] Remove `@NotBlank` and `@DecimalMin` from `Project.java` entity
- [x] `ProjectNotFoundException` → lowercase "id" in message
- [x] Consider soft delete instead of hard delete

### Menu Module ✅ (Gold Standard — minor fixes only)
- [x] `MenuController` → fix `hasAnyAuthority` to `hasAuthority` for single permissions

### Invoice Module ✅ (Gold Standard — minor fixes only)
- [x] `InvoiceController` → fix `hasAnyAuthority` to `hasAuthority` for single permissions
- [x] `InvalidInvoiceStatusException` → consider typed constructor like `InvalidLeadStatusException`
