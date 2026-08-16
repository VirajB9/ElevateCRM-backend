# Graph Report - DMA-backend  (2026-08-16)

## Corpus Check
- 157 files · ~66,945 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 888 nodes · 2725 edges · 45 communities (24 shown, 21 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 143 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Java Src Main
- Java Src Main
- Java Src Main
- Java Lead Src
- Java Menu Src
- Java Src Main
- Java Client Src
- Java Invoice Src
- Java Src Main
- Java Src Main
- Permissiontype Src Main
- Src Main Java
- Project Java Src
- Java Src Main
- Projectserviceimpl Java Src
- Src Main Java
- Src Main Java
- Java Src Main
- Java Src Main
- Passwordgenerator Java Src
- Test Dmabackendapplicationtests Src
- Gradlew Entry Die
- Dmabackendapplication Main Src
- Duplicatepermissionexception Src Main
- Invalidinvoicedateexception Src Main
- Feature Based Packaging
- Rbac Docs Development
- Duplicateemailexception Src Main
- Duplicatephoneexception Src Main
- Invaliduserstatusexception Src Main
- Systemrolemodificationexception Src Main
- Unauthorizedroleassignmentexception Src Main
- Duplicateclientemailexception Src Main
- Duplicateleadexception Src Main
- Docs Codebase Audit
- Docs Bootstrap Chain
- Src Main Resources
- Agent Rules Graperoot
- Docs Codebase Standardization
- Project Context Docs
- Docs Spring Boot
- Gemini Instructor Mode
- Src Main Resources

## God Nodes (most connected - your core abstractions)
1. `BadRequestException` - 49 edges
2. `UserResponse` - 37 edges
3. `PermissionType` - 37 edges
4. `InvoiceResponse` - 36 edges
5. `User` - 35 edges
6. `ProjectResponse` - 33 edges
7. `UserServiceImpl` - 30 edges
8. `Role` - 29 edges
9. `Lead` - 28 edges
10. `ClientService` - 27 edges

## Surprising Connections (you probably didn't know these)
- `Feature-Based Packaging` --semantically_similar_to--> `Feature-Based Packaging`  [INFERRED] [semantically similar]
  GEMINI.md → docs/development_guidelines.md
- `RBAC Design` --semantically_similar_to--> `RBAC Security Architecture`  [INFERRED] [semantically similar]
  GEMINI.md → docs/development_guidelines.md
- `Feature-based Packaging` --semantically_similar_to--> `Feature-Based Packaging`  [INFERRED] [semantically similar]
  docs/project_context.md → docs/development_guidelines.md
- `RBAC` --semantically_similar_to--> `RBAC Security Architecture`  [INFERRED] [semantically similar]
  docs/project_context.md → docs/development_guidelines.md
- `Bootstrap Chain` --semantically_similar_to--> `Order-Based Bootstrap Chain`  [INFERRED] [semantically similar]
  docs/project_context.md → docs/development_guidelines.md

## Import Cycles
- None detected.

## Communities (45 total, 21 thin omitted)

### Community 0 - "Java Src Main"
Cohesion: 0.06
Nodes (32): io.swagger.v3.oas.annotations.Operation, org.springframework.data.domain.Page, org.springframework.data.domain.Pageable, org.springframework.security.access.prepost.PreAuthorize, DeleteMapping, GetMapping, PatchMapping, PostMapping (+24 more)

### Community 1 - "Java Src Main"
Cohesion: 0.07
Nodes (37): lombok.AllArgsConstructor, lombok.Builder, lombok.Getter, lombok.NoArgsConstructor, lombok.Setter, PostMapping, AssignPermissionsRequest, AuthenticationResponse (+29 more)

### Community 2 - "Java Src Main"
Cohesion: 0.07
Nodes (35): lombok.RequiredArgsConstructor, org.springframework.boot.CommandLineRunner, org.springframework.core.annotation.Order, org.springframework.data.annotation.TypeAlias, org.springframework.data.mongodb.core.mapping.Document, org.springframework.data.mongodb.repository.MongoRepository, org.springframework.security.core.GrantedAuthority, org.springframework.security.core.userdetails.UserDetails (+27 more)

### Community 3 - "Java Lead Src"
Cohesion: 0.06
Nodes (39): DeleteMapping, PostMapping, PutMapping, RequestMapping, ResponseStatus, RestController, LeadController, CreateLeadRequest (+31 more)

### Community 4 - "Java Menu Src"
Cohesion: 0.08
Nodes (19): GetMapping, PatchMapping, PostMapping, PutMapping, RequestMapping, ResponseStatus, RestController, MenuController (+11 more)

### Community 5 - "Java Src Main"
Cohesion: 0.07
Nodes (22): org.springframework.data.mongodb.core.MongoTemplate, org.springframework.stereotype.Component, org.springframework.stereotype.Repository, DeleteMapping, GetMapping, PatchMapping, PutMapping, RequestMapping (+14 more)

### Community 6 - "Java Client Src"
Cohesion: 0.09
Nodes (17): ClientController, DeleteMapping, GetMapping, PostMapping, PutMapping, RequestMapping, ResponseStatus, RestController (+9 more)

### Community 7 - "Java Invoice Src"
Cohesion: 0.10
Nodes (16): Invoice, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, InvoiceItem, AllArgsConstructor (+8 more)

### Community 8 - "Java Src Main"
Cohesion: 0.10
Nodes (22): io.jsonwebtoken.Claims, io.swagger.v3.oas.models.OpenAPI, jakarta.annotation.PostConstruct, jakarta.servlet.FilterChain, jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.security.Key, OpenAPI (+14 more)

### Community 9 - "Java Src Main"
Cohesion: 0.10
Nodes (20): io.swagger.v3.oas.annotations.security.SecurityRequirement, io.swagger.v3.oas.annotations.tags.Tag, lombok.Data, org.springframework.http.ResponseEntity, org.springframework.web.bind.annotation.ExceptionHandler, org.springframework.web.bind.annotation.GetMapping, org.springframework.web.bind.annotation.RestController, org.springframework.web.bind.annotation.RestControllerAdvice (+12 more)

### Community 10 - "Permissiontype Src Main"
Cohesion: 0.07
Nodes (30): PermissionType, CLIENT_CREATE, CLIENT_DELETE, CLIENT_READ, CLIENT_UPDATE, INVOICE_CREATE, INVOICE_DELETE, INVOICE_READ (+22 more)

### Community 11 - "Src Main Java"
Cohesion: 0.10
Nodes (9): PermissionNotFoundException, RoleNotFoundException, UserNotFoundException, ClientNotFoundException, ResourceNotFoundException, InvoiceNotFoundException, LeadNotFoundException, MenuNotFoundException (+1 more)

### Community 12 - "Project Java Src"
Cohesion: 0.12
Nodes (14): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, Project, ProjectStatus, ACTIVE (+6 more)

### Community 13 - "Java Src Main"
Cohesion: 0.15
Nodes (12): DeleteMapping, PutMapping, RequestMapping, RestController, ProjectController, AllArgsConstructor, Builder, Getter (+4 more)

### Community 15 - "Src Main Java"
Cohesion: 0.17
Nodes (5): DuplicateMenuPathException, DuplicateMenuTitleException, InvalidMenuPathException, ParentMenuNotFoundException, SelfParentMenuException

### Community 16 - "Src Main Java"
Cohesion: 0.17
Nodes (5): DuplicateClientGstException, DuplicateClientPhoneException, BadRequestException, LeadAlreadyConvertedException, InvalidProjectDateException

### Community 17 - "Java Src Main"
Cohesion: 0.15
Nodes (11): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UpdateProjectRequest, ProjectPriority, HIGH (+3 more)

### Community 18 - "Java Src Main"
Cohesion: 0.22
Nodes (7): PostMapping, CreateProjectRequest, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 20 - "Test Dmabackendapplicationtests Src"
Cohesion: 0.60
Nodes (3): org.junit.jupiter.api.Test, org.springframework.boot.test.context.SpringBootTest, DmaBackendApplicationTests

### Community 21 - "Gradlew Entry Die"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 25 - "Feature Based Packaging"
Cohesion: 0.67
Nodes (3): Feature-Based Packaging, Feature-based Packaging, Feature-Based Packaging

### Community 26 - "Rbac Docs Development"
Cohesion: 0.67
Nodes (3): RBAC Security Architecture, RBAC, RBAC Design

## Knowledge Gaps
- **79 isolated node(s):** `USER_CREATE`, `USER_READ`, `USER_UPDATE`, `USER_DELETE`, `CLIENT_CREATE` (+74 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **21 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `PermissionType` connect `Permissiontype Src Main` to `Java Src Main`?**
  _High betweenness centrality (0.063) - this node is a cross-community bridge._
- **Why does `BadRequestException` connect `Src Main Java` to `Duplicateclientemailexception Src Main`, `Java Src Main`, `Duplicateleadexception Src Main`, `Java Lead Src`, `Java Src Main`, `Java Invoice Src`, `Java Src Main`, `Project Java Src`, `Src Main Java`, `Duplicatepermissionexception Src Main`, `Invalidinvoicedateexception Src Main`, `Duplicateemailexception Src Main`, `Duplicatephoneexception Src Main`, `Invaliduserstatusexception Src Main`, `Systemrolemodificationexception Src Main`, `Unauthorizedroleassignmentexception Src Main`?**
  _High betweenness centrality (0.059) - this node is a cross-community bridge._
- **Why does `LeadSource` connect `Java Lead Src` to `Java Src Main`, `Java Src Main`?**
  _High betweenness centrality (0.028) - this node is a cross-community bridge._
- **What connects `USER_CREATE`, `USER_READ`, `USER_UPDATE` to the rest of the system?**
  _79 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Java Src Main` be split into smaller, more focused modules?**
  _Cohesion score 0.055823680823680825 - nodes in this community are weakly interconnected._
- **Should `Java Src Main` be split into smaller, more focused modules?**
  _Cohesion score 0.06765935214211076 - nodes in this community are weakly interconnected._
- **Should `Java Src Main` be split into smaller, more focused modules?**
  _Cohesion score 0.0667578659370725 - nodes in this community are weakly interconnected._