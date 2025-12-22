# c:\Users\dorem\Documents\ArchSys\SpringBootKafka\docs\Service-Explanation.md

## Lean Microservice Container Diagram Explanation: Job Applicant (JA) Subsystem

This version reduces the number of services and ensures each service owns its database, following microservice best practices.

### 1. API Gateway
- **Purpose:** Central entry for all requests, routing, authentication, and aggregation.

### 2. Auth Service
- **Purpose:** Handles user authentication, registration, and token management.
- **Database:** Auth DB (UserCredential, tokens)

### 3. User Service
- **Purpose:** Manages user profile, education, experience, and skills.
- **Database:** User DB

### 4. Application Service
- **Purpose:** Handles job applications and file uploads.
- **Database:** Application DB
- **Integrates:** MinIO/S3 for file storage

### 5. Notification Service
- **Purpose:** Manages notification subscriptions and delivers notifications.
- **Database:** Notification DB
- **Integrates:** Redis/Kafka for real-time delivery

### 6. Subscription & Payment Service
- **Purpose:** Manages user subscriptions and payment transactions.
- **Database:** Payment DB
- **Integrates:** Kafka for event streaming

### 7. Admin Service
- **Purpose:** Provides admin panel and moderation features.
- **Database:** Admin DB

---

**Design Rationale:**
- Each service is focused and owns its data, supporting true microservice independence.
- Event streaming and caching are used only where needed (notifications, payments, applications).
- This design is easier to maintain, scale, and secure.
- Fewer services reduce operational overhead and complexity.
