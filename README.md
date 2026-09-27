# 🤖AI Content Web Service

An backend service designed to automate content research, trend analysis, and content generation for business owners and content creators.

![AI-image](image/AI-image.png)

## 🛠 Technical Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Java 17+ | Modern Java Records & Features |
| **Framework** | Spring Boot `3.2.0` | Core Application Framework |
| **Security** | Spring Security & JJWT `0.11.5` | Stateless JWT Authentication & RBAC |
| **Persistence** | Spring Data JPA / Hibernate | ORM & Data Access |
| **Database** | PostgreSQL / H2 (In-memory) | Production / Test Datasources |
| **Migration** | Flyway | Database version control & schema migration |
| **Build Tool** | Gradle | Multi-module build management |

## 📦 Package
This project adopts a **Modular Monolith** architecture based on **Clean Architecture** principles.

```text
📦 Content AI Backend
├── 🎯 core-domain/                      # Domain & Business Layer
│   ├── common/                          
│   │   └── error/                       # CustomException, ErrorCode definitions
│   └── feature/                         # Bounded Context
│       ├── domain/                      # Pure Domain Models
│       └── service/                     # Application Services & Port Interfaces
│           ├── FeatureService.java         # Domain logic & orchestration
│           └── FeatureRepository.java      # Output Port (Repository Interface)
│
├── 💾 db-core/                          # Persistence & Storage Layer
│   └── feature/                         # Persistence Implementation
│       ├── FeatureEntity.java              # JPA Entity
│       ├── FeatureJpaRepository.java       # Spring Data JPA Repository
│       └── FeatureCoreRepository.java      # Adapter implementing UserRepository (Port)
│
└── 🌐 core-api/                         # Presentation Layer
    ├── AIContentApplication.java        # Spring Boot main entrypoint
    ├── config/                          # Infrastructure & Framework Configurations
    └── controller/                      # REST API Endpoints
```
## 🌐Architecture System
![architecture](image/architecture.png)

##  🧱 ERD

