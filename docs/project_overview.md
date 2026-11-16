# Project Overview: Digital Marketing Agency (DMA) ERP Backend (ElevateCRM)

This document provides a comprehensive, high-level technical overview of the **ElevateCRM Backend**, generated from architectural analysis and validated using the `codegraph-dma` and `graphify` MCP knowledge graph servers.

---

## 1. 📊 System Architecture & Tech Stack

- **Language & Runtime**: **Java 17** • **Spring Boot 3.5.15** • **Gradle 9.x**
- **Persistence**: **Spring Data MongoDB** (MongoDB 7.0+ with replica set multi-document transactions)
- **Security & Perimeter**: Spring Security 6.x with stateless JWT (JJWT 0.12.6), BCrypt hashing, SHA-256 refresh token rotation, and Bucket4j in-memory token-bucket rate limiting.
- **Architecture Pattern**: Feature-based Packaging (Domain-Driven Design influenced) across 7 cohesive modules.
- **Control Flow**: `REST Controller ➔ Domain Service ➔ Spring Data Repository ➔ MongoDB Document`.
- **Encapsulation Invariant**: Controllers never expose `@Document` entity models; all external communication is strictly handled via strongly typed Request and Response DTOs.
- **Test Infrastructure**: JUnit 5, Mockito, Spring Boot Test, and Testcontainers 1.20.4 running live `mongo:7.0.5` replica sets (**82 test files, 662 tests passing**).

---

## 2. 🧩 Core Modules & Domain Model

The application is structured into 7 core feature modules forming the end-to-end operational backbone of the agency:

### 1. **Auth, Identity & User Module (`com.viraj.dmabackend.auth`)**
- **Central Entities**: `User`, `Role`, `Permission`, `RefreshToken`.
- **Responsibilities**: Enforces a strict 4-tier RBAC system (`OWNER` > `MANAGER` > `EMPLOYEE` > `INTERN`). Implements stateless JWT authentication, refresh token rotation with SHA-256 hashing, account lockout protection, and granular `module:action` authorities.
- **Key Endpoints**: `/api/v1/auth/**`, `/api/v1/users/**`, `/api/v1/roles/**`.

### 2. **Lead CRM & Pipeline Module (`com.viraj.dmabackend.lead`)**
- **Central Entity**: `Lead`.
- **Responsibilities**: Manages the top of the sales funnel across 9 pipeline stages (`NEW` → `CONTACTED` → `QUALIFIED` → `PROPOSAL_SENT` → `NEGOTIATION` → `WON` → `CONVERTED` / `LOST` / `DELETED`) and 11 acquisition sources.
- **Key Workflows**: Strict email/phone uniqueness deduplication, assignment to sales representatives, and atomic `convertLeadToClient` workflow.
- **Key Endpoints**: `/api/v1/leads/**`.

### 3. **Client Registry Module (`com.viraj.dmabackend.client`)**
- **Central Entity**: `Client`.
- **Responsibilities**: Represents active and onboarded agency clients converted from leads or registered directly. Manages corporate profiles, statutory GSTINs, primary contact persons, and client lifecycles (`ACTIVE`, `INACTIVE`, `ARCHIVED`).
- **Key Workflows**: Guarded soft-deletion ensuring clients with active projects or unpaid invoices cannot be prematurely archived.
- **Key Endpoints**: `/api/v1/clients/**`.

### 4. **Project Delivery Module (`com.viraj.dmabackend.project`)**
- **Central Entity**: `Project`.
- **Responsibilities**: Represents deliverable client contracts, marketing campaigns, and ongoing retainer retainers tied to specific clients.
- **Key Workflows**: Validates forward delivery dates, tracks budget allocations, priority tiers (`LOW`, `MEDIUM`, `HIGH`, `URGENT`), and lifecycle status (`PLANNING`, `ACTIVE`, `ON_HOLD`, `COMPLETED`, `CANCELLED`, `ARCHIVED`).
- **Key Endpoints**: `/api/v1/projects/**`.

### 5. **Financial Invoicing Module (`com.viraj.dmabackend.invoice`)**
- **Central Entities**: `Invoice`, `InvoiceHistory`, `Counter`.
- **Responsibilities**: Statutory financial engine. Generates sequential invoice identifiers (`INV-YYYY-XXXX`), manages multi-line items, calculates tax and discount arithmetic, and links back to both `Client` and `Project`.
- **Key Workflows**: Robust state machine (`DRAFT` → `SENT` → `PAID` / `OVERDUE` / `CANCELLED`), guarded status transitions, and immutable audit snapshot history logging.
- **Key Endpoints**: `/api/v1/invoices/**`.

### 6. **Dynamic Navigation Menu Module (`com.viraj.dmabackend.menu`)**
- **Central Entity**: `Menu`.
- **Responsibilities**: Dynamically builds user-facing navigation trees based on runtime user permissions, cached with Spring SpEL.
- **Key Workflows**: Self-referencing parent-child menu trees, display order sorting, and active status toggling.
- **Key Endpoints**: `/api/v1/menus/**`.

### 7. **Permissions & System Configuration (`com.viraj.dmabackend.permission`, `config`)**
- **Central Components**: `PermissionType` (27 atomic permissions), `SecurityConfig`, `RateLimitFilter`, `MongoConfig`.
- **Responsibilities**: Centralized system permissions catalog and infrastructure beans.
- **Key Endpoints**: `/api/v1/permissions/**`.

---

## 3. 🕸️ Codebase Topology (Graphify AST Insights)

Knowledge graph queries executed via `codegraph-dma` / `graphify` reveal the topological structure of the codebase:
- **Total Knowledge Graph Nodes**: 2,313
- **Total Directed Dependency Edges**: 7,445
- **Detected Community Clusters**: 89
- **Extraction Fidelity**: 88% Extracted, 12% Inferred

### Key God Nodes & Architectural Anchors:
1. **`Lead` & `LeadRepository`** (Rank #1 God Node): Highest degree of centrality, serving as the primary business intake interface.
2. **`ClientRepository` & `Client`** (Rank #4 God Node): Central aggregate anchor connecting converted leads, active projects, and billing invoices.
3. **`BadRequestException` & `GlobalExceptionHandler`**: The most densely referenced exception nodes, confirming strict defensive validation across all service methods.
4. **`BaseEntity`**: Common persistence ancestor declaring `@Id`, auditing timestamps, soft-deletion flags, and `@Version private Long version;` for Optimistic Concurrency Control.
5. **SPI Guards**: Decoupled interface guards (`ProjectClientDeletionGuard`, `InvoiceClientDeletionGuard`, `InvoiceProjectCancellationGuard`) preventing circular dependencies between domain modules.

---

## 4. 🚀 Current Development Status & Production Roadmap

### Milestone Achieved: Complete Feature Implementation & Audit Saturation
- **All 7 Core Modules Built & Operational**: 195 production Java source files fully implemented.
- **Automated Test Baseline**: **662 / 662 tests passing cleanly** across unit, controller, and Testcontainers integration suites.
- **Forensic Audit Completed**: 5 multi-agent audit rounds executed, resulting in **65 verified GitHub issues** ([#340 to #404](https://github.com/VirajB9/ElevateCRM-backend/issues)) covering security, concurrency, database indexing, arithmetic precision, and test quality.

### Next Production Readiness Priorities:
1. **Database Index Migrations**: Introduce an automated schema migration runner (e.g. Mongock) to guarantee secondary and unique indexes are created in production where `auto-index-creation: false` is active.
2. **Distributed Infrastructure**: Replace in-memory token bucket rate limiting and refresh token storage with Redis for horizontal multi-instance scaling.
3. **Continuous Integration Pipeline**: Harden GitHub Actions CI/CD to run Testcontainers replica sets on every pull request.
4. **Issue Remediation**: Systematically resolve filed forensic audit issues according to their priority (`P1` critical perimeter security, `P2` state machine/concurrency, `P3` code quality).