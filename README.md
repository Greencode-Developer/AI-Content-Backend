# AI Content Web Service (content_web)

An backend service designed to automate content research, trend analysis, and content generation for business owners and content creators.

---

## 🏗️ System Architecture

### 1. Architecture Overview
This project adopts a **Modular Monolith** architecture based on **Clean Architecture** and **Hexagonal (Ports & Adapters)** principles. It provides high maintainability, clear domain boundaries, and strong decoupling between business logic and infrastructure.

#### Key Design Principles:
* **Domain-Centric Design**: The business domain is the core of the system and does not depend on external frameworks or databases.
* **Dependency Inversion Principle (DIP)**: High-level business logic defines repository interfaces (Ports), while low-level infrastructure implements them (Adapters).
* **Multi-Module Separation**: Strict boundary separation across Gradle subprojects (`core-api`, `core-domain`, `db-core`).
* **Stateless Security**: JWT-based authentication integrated with Spring Security and custom resolver annotations (`@CurrentUser`).

---

### 2. Multi-Module Project Structure

```text
📦 content_web (Root)
├── 🎯 core-domain/                     # Domain & Business Layer (Core Logic)
│   ├── common/                          # Cross-domain error handling & common models
│   │   └── error/                       # CustomException, ErrorCode definitions
│   └── user/                            # User Bounded Context
│       ├── domain/                      # Pure Domain Models (Java Records: User, UserRole, UserStatus)
│       └── service/                     # Application Services & Port Interfaces
│           ├── UserService.java         # Domain logic & orchestration
│           └── UserRepository.java      # Output Port (Repository Interface)
│
├── 💾 db-core/                          # Persistence & Storage Layer (Adapters)
│   ├── BaseEntity.java                  # MappedSuperclass with auditing timestamps
│   └── user/                            # User Persistence Implementation
│       ├── UserEntity.java              # JPA Entity & Domain mapper (toDomain)
│       ├── UserJpaRepository.java       # Spring Data JPA Repository
│       └── UserCoreRepository.java      # Adapter implementing UserRepository (Port)
│
└── 🌐 core-api/                         # Presentation Layer (HTTP & Infrastructure)
    ├── AIContentApplication.java        # Spring Boot main entrypoint
    ├── config/                          # Infrastructure & Framework Configurations
    │   ├── jwt/                         # JWT token provider, properties & payload
    │   ├── security/                    # Spring Security & JwtAuthenticationFilter
    │   └── web/                         # WebMvcConfigurer & CurrentUserArgumentResolver
    └── controller/                      # REST API Endpoints
        ├── HealthController.java        # Health check endpoint
        ├── user/                        # User REST Controllers
        └── advice/                      # Global exception handling & standard API responses
```

---

#### Layer Responsibilities:
1. **`core-domain` (Core)**:
   - Holds pure domain objects (immutable Java `record`), business logic, and error definitions.
   - Declares repository interfaces (**Ports**) without any knowledge of JPA or SQL.
   - Zero dependency on `core-api` and `db-core`.

2. **`db-core` (Infrastructure - Persistence)**:
   - Implements domain repository interfaces (**Adapters**).
   - Manages JPA entities, lifecycle hooks, and database migrations via Flyway.
   - Converts `UserEntity` (JPA) $\leftrightarrow$ `User` (Domain Record).

3. **`core-api` (Infrastructure - Presentation)**:
   - Handles HTTP requests, input validation, serialization, and API response standardization (`GlobalApiResponse`).
   - Intercepts requests via `JwtAuthenticationFilter` and extracts authenticated users via `@CurrentUser`.
   - Aggregates `core-domain` and `db-core` into the executable application artifact (`bootJar`).

---

### 4. Technical Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Java 17+ | Modern Java Records & Features |
| **Framework** | Spring Boot `3.2.0` | Core Application Framework |
| **Security** | Spring Security & JJWT `0.11.5` | Stateless JWT Authentication & RBAC |
| **Persistence** | Spring Data JPA / Hibernate | ORM & Data Access |
| **Database** | PostgreSQL / H2 (In-memory) | Production / Test Datasources |
| **Migration** | Flyway | Database version control & schema migration |
| **Build Tool** | Gradle | Multi-module build management |

---

### 5. Getting Started

#### Prerequisites
- JDK 17 or higher
- Gradle (or use the provided `./gradlew` wrapper)

#### Build & Run
```bash
# Clone the repository
git clone <repository-url>
cd content_web

# Build all modules
./gradlew build

# Run the API application
./gradlew :core-api:bootRun
```
