# c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\document\summary\Service.md

## JA Subsystem Microservice Container Diagram (Lean Version)

```mermaid
flowchart TD
    subgraph JA_Subsystem[Job Applicant Subsystem]
        JA_API[API Gateway<br/>Port: 8080]
        AuthService[Auth Service<br/>Port: 8081]
        UserService[User Service<br/>Port: 8085]
        ApplicationService["✅ Application Service<br/>Port: 8083<br/>Status: RUNNING"]
        NotificationService[Notification Service<br/>Port: 8086]
        PaymentService[Subscription & Payment Service<br/>Port: 8087]
        AdminService[Admin Service<br/>Port: 8088]
        AuthDB[(Auth DB<br/>:5434)]
        UserDB[(User DB<br/>:5433)]
        ApplicationDB[(Application DB<br/>:5435)]
        NotificationDB[(Notification DB<br/>:5436)]
        PaymentDB[(Payment DB<br/>:5437)]
        AdminDB[(Admin DB<br/>:5438)]
        Redis[(Redis<br/>:6379)]
        Kafka[(Kafka<br/>:9092)]
        SeaweedFS["✅ SeaweedFS<br/>Port: 8333<br/>Status: RUNNING"]
        EurekaServer["✅ Eureka Server<br/>Port: 8761<br/>Status: RUNNING"]
    end

    JA_API --> AuthService
    JA_API --> UserService
    JA_API --> ApplicationService
    JA_API --> NotificationService
    JA_API --> PaymentService
    JA_API --> AdminService
    
    JA_API --> EurekaServer

    AuthService --> AuthDB
    UserService --> UserDB
    ApplicationService --> ApplicationDB
    NotificationService --> NotificationDB
    PaymentService --> PaymentDB
    AdminService --> AdminDB

    ApplicationService --> SeaweedFS
    NotificationService --> Redis
    NotificationService --> Kafka
    ApplicationService --> Kafka
    PaymentService --> Kafka

    %% Service responsibilities
    AuthService -.->|Handles: UserCredential, UserRefreshToken| AuthDB
    UserService -.->|Handles: User, UserEducation, UserWorkExperience, UserSkill, Country, Skill| UserDB
    ApplicationService -.->|✅ Handles: JobApplication, resume/coverLetter files<br/>Endpoints: 14 total (6 Public + 7 Admin + 1 Internal)<br/>S3 Upload: SeaweedFS| ApplicationDB
    NotificationService -.->|Handles: UserNotificationSubscription| NotificationDB
    PaymentService -.->|Handles: UserSubscription, UserPaymentTransaction| PaymentDB
    AdminService -.->|Handles: Administrator| AdminDB
```

---

## Application Service Implementation Status ✅

### Completed Features:
- **Entity Model**: JobApplication with BaseEntity inheritance
- **DTOs**: Request/Response DTOs for all operations
- **Repository**: Custom queries with filtering, pagination, soft delete support
- **Service Layer**: Full business logic with transactions
- **Controllers**: 3 separate controllers (Public, Admin, Internal)
- **File Management**: S3 uploads via SeaweedFS with unique filename generation
- **Enum Integration**: ApplicationStatus from common module
- **Note System**: User, Admin, and Company User notes

### API Endpoints (14 Total):
#### Public Endpoints (6):
1. `POST /api/v1/applications` - Create application with file uploads
2. `GET /api/v1/applications` - Get user's applications (paginated)
3. `GET /api/v1/applications/{applicationId}` - Get specific application
4. `PATCH /api/v1/applications/{applicationId}/status` - Update status
5. `DELETE /api/v1/applications/{applicationId}` - Withdraw application
6. `GET /api/v1/applications/{applicationId}/files/{fileType}` - Download files

#### Admin Endpoints (7):
1. `GET /api/v1/admin/applications` - Get all with filters
2. `GET /api/v1/admin/applications/{applicationId}` - View any application
3. `PATCH /api/v1/admin/applications/{applicationId}/status` - Update with admin notes
4. `DELETE /api/v1/admin/applications/{applicationId}` - Hard delete
5. `GET /api/v1/admin/applications/statistics` - Get statistics
6. `PATCH /api/v1/admin/applications/bulk/status` - Bulk update
7. `POST /api/v1/admin/applications/{applicationId}/restore` - Restore deleted

#### Internal Endpoints (1):
1. `GET /api/v1/internal/job-posts/{jobPostId}/applications` - Service-to-service

### Infrastructure:
- **Database**: PostgreSQL 16-alpine on port 5435
- **Object Storage**: SeaweedFS S3-compatible API on port 8333
- **Service Discovery**: Eureka Server on port 8761
- **Status**: ✅ All services running and healthy
