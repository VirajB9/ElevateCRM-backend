# ElevateCRM Backend

ElevateCRM is a production-ready SaaS backend built to manage operations for a Digital Marketing Agency. This system handles everything from Lead generation and Client relationships to Project tracking, Invoicing, and multi-tiered Role-Based Access Control (RBAC).

## 🚀 Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.5.15
- **Database**: MongoDB
- **Security**: Spring Security + JWT (Stateless) + BCrypt
- **Architecture**: Feature-based Packaging (Domain-Driven Design influenced)

## 🧩 Core Modules

1. **Authentication & RBAC**
   - Multi-tiered hierarchy: `Owner` > `Manager` > `Employee`.
   - Granular permission system (e.g., `lead:create`, `client:view`).
   - Stateless JWT sessions.

2. **Lead CRM**
   - Track leads by status (e.g., NEW, CONTACTED) and source.
   - Strict duplication validation (email/phone).
   - Atomic workflow to convert a winning Lead directly into a Client.

3. **Client Management**
   - 360-degree view of active, paused, and churned clients.
   - Links with multiple points of contact and specific agency projects.

4. **Project Tracking**
   - Deliverables and campaigns tied to specific Clients.
   - Track deadlines, budget utilization, priorities, and statuses.

5. **Invoicing & Billing**
   - Financial engine for generating and tracking invoices.
   - Line-item support, tax calculation, and state-machine logic for status (Draft, Unpaid, Paid, Overdue).

## 📐 Architecture Highlights
- **Thin Controllers**: Controllers handle HTTP routing only. All business logic lives in the Service layer.
- **Strict Data Transfer**: Database Entities are *never* exposed to the API. Everything is serialized through dedicated DTOs (Data Transfer Objects) and Mappers.
- **Centralized Exception Handling**: Global exception handler mapping all custom domain exceptions (extending `ResourceNotFoundException` and `BadRequestException`) to standardized HTTP responses.
- **Soft Deletions**: Records are archived rather than permanently deleted to maintain historical integrity.

## 🛠️ Getting Started

### Prerequisites
- Java 17
- MongoDB (Running on `localhost:27017` or configured via `SPRING_DATA_MONGODB_URI`)
- Gradle

### Running Locally
1. Clone the repository.
2. Ensure MongoDB is running locally.
3. Build and run the application:
   ```bash
   ./gradlew bootRun
   ```
4. Access the API at `http://localhost:8080`.
5. View the Swagger UI Documentation at `http://localhost:8080/swagger-ui.html`.

### Default Login
Upon the first startup, an Owner account is automatically bootstrapped:
- **Email**: `owner@agency.com`
- **Password**: *(Refer to internal documentation or environment variables)*

## 📚 Documentation
Detailed architectural decisions and coding guidelines can be found in the `/docs` directory.
