# Application Service - Implementation Plan

## 📋 Overview

This document outlines the implementation plan for the **Application Service** based on the project architecture design and established patterns. The plan is divided into phases, with each phase building upon the previous one.

**Service Responsibility:**
- Manage job applications from users to job postings
- Handle file uploads (resume, cover letter, additional documents) to MinIO/S3
- Coordinate with User Service and Job Manager service for data validation
- Publish application events to Kafka for notifications
- Provide APIs for both applicants and recruiters to track application status

---

## 🎯 Phase 0: Project Structure & Configuration

### Goal
Set up the complete project structure following the established patterns from User Service.

### Tasks

#### 0.1 Create Directory Structure
```
services/application-service/src/main/java/com/team/ja/application/
├── api/
│   └── ApplicationController.java          (REST endpoints)
├── dto/
│   ├── request/
│   │   ├── CreateApplicationRequest.java
│   │   ├── UpdateApplicationStatusRequest.java
│   │   └── UploadApplicationFilesRequest.java
│   └── response/
│       ├── ApplicationResponse.java
│       ├── ApplicationDetailResponse.java
│       └── FileUploadResponse.java
├── mapper/
│   └── ApplicationMapper.java              (MapStruct)
├── model/
│   ├── JobApplication.java                 (JPA Entity)
│   └── ApplicationStatus.java              (Enum)
├── repository/
│   └── JobApplicationRepository.java       (Spring Data JPA)
├── service/
│   ├── ApplicationService.java
│   ├── FileStorageService.java
│   ├── KafkaPublisherService.java
│   └── ExternalServiceClient.java
├── config/
│   ├── MinIOConfig.java
│   └── FeignClientConfig.java
├── exception/
│   ├── ApplicationNotFoundException.java
│   └── FileUploadException.java
└── ApplicationServiceApplication.java
```

#### 0.2 Update Configuration Files

**pom.xml additions:**
- Add MinIO dependency
- Add Kafka dependencies (spring-cloud-stream)
- Add OpenAPI/Swagger
- Add MapStruct
- Add Feign Client (for service-to-service communication)

**application.yml updates:**
- MinIO configuration (endpoint, access key, secret key, bucket name)
- Kafka configuration (bootstrap servers, topics)
- Feign client configuration for User Service & Job Manager
- Database schema configuration (application_schema)
- Server port (8082)

---

## 🗄️ Phase 1: Database Layer

### Goal
Create database schema, entities, and repositories following Flyway migrations.

### Tasks

#### 1.1 Create Flyway Migrations

**File: `V1__create_application_schema.sql`**
- Create `application_schema` schema
- Create `job_applications` table with columns:
  - `id` (UUID, PRIMARY KEY)
  - `user_id` (UUID, FK to user_schema.users)
  - `job_post_id` (UUID, external reference to Job Manager)
  - `status` (VARCHAR - APPLIED, REVIEWED, INTERVIEW, REJECTED, OFFERED)
  - `resume_url` (TEXT - MinIO S3 URL)
  - `cover_letter_url` (TEXT - MinIO S3 URL)
  - `additional_files` (JSON - array of file URLs)
  - `applied_at` (TIMESTAMP)
  - `application_status_updated_at` (TIMESTAMP)
  - `is_active` (BOOLEAN - soft delete)
  - `deactivated_at` (TIMESTAMP - soft delete)
  - `created_at` (TIMESTAMP)
  - `updated_at` (TIMESTAMP)
- Create indexes:
  - `idx_job_applications_user_id`
  - `idx_job_applications_job_post_id`
  - `idx_job_applications_status`
  - `idx_job_applications_user_job_post` (composite unique constraint)

#### 1.2 Create JPA Entities

**JobApplication.java**
- Extend `BaseEntity` from common module
- Map all columns from migration
- Add relationships (lazy loading where appropriate)
- Use Lombok annotations (`@Entity`, `@Getter`, `@Setter`, `@SuperBuilder`)
- Add validation annotations (`@NotNull`, `@NotBlank`)

**ApplicationStatus.java** (Enum)
```java
APPLIED("Applied"),
REVIEWED("Under Review"),
INTERVIEW("Interview Scheduled"),
REJECTED("Rejected"),
OFFERED("Offer Extended")
```

#### 1.3 Create Spring Data JPA Repository

**JobApplicationRepository.java**
- Extend `JpaRepository<JobApplication, UUID>`
- Custom query methods:
  - `findByUserId(UUID userId)` - Get all applications for a user
  - `findByJobPostId(UUID jobPostId)` - Get all applications for a job post
  - `findByUserIdAndJobPostId(UUID userId, UUID jobPostId)` - Check if user already applied
  - `findByUserIdAndStatus(UUID userId, ApplicationStatus status)` - Filter by status
  - `findByJobPostIdAndStatus(UUID jobPostId, ApplicationStatus status)`

---

## 🔄 Phase 2: DTO Layer & MapStruct Mappers

### Goal
Define DTOs and create MapStruct mappers for entity ↔ DTO conversions.

### Tasks

#### 2.1 Create Request DTOs

**CreateApplicationRequest.java**
```java
- jobPostId (UUID, @NotNull)
- resumeFile (MultipartFile, @NotNull)
- coverLetterFile (MultipartFile, optional)
- additionalFiles (List<MultipartFile>, optional)
```

**UpdateApplicationStatusRequest.java**
```java
- status (ApplicationStatus, @NotNull)
- notes (String, optional)
```

#### 2.2 Create Response DTOs

**ApplicationResponse.java** (List/Summary view)
```java
- id (UUID)
- userId (UUID)
- jobPostId (UUID)
- status (ApplicationStatus)
- appliedAt (LocalDateTime)
- createdAt (LocalDateTime)
```

**ApplicationDetailResponse.java** (Detailed view)
```java
- id (UUID)
- userId (UUID)
- jobPostId (UUID)
- status (ApplicationStatus)
- resumeUrl (String)
- coverLetterUrl (String)
- additionalFiles (List<String>)
- appliedAt (LocalDateTime)
- applicationStatusUpdatedAt (LocalDateTime)
- userDetails (nested: UserResponse from User Service)
- jobPostDetails (nested: JobPostResponse from Job Manager)
```

**FileUploadResponse.java**
```java
- fileName (String)
- fileUrl (String)
- fileSize (Long)
- uploadedAt (LocalDateTime)
```

#### 2.3 Create MapStruct Mappers

**ApplicationMapper.java**
```java
@Mapper(componentModel = "spring")
public interface ApplicationMapper {
    JobApplication toEntity(CreateApplicationRequest request);
    ApplicationResponse toResponse(JobApplication entity);
    ApplicationDetailResponse toDetailResponse(JobApplication entity);
    void updateEntity(@MappingTarget JobApplication entity, UpdateApplicationStatusRequest request);
}
```

---

## 📡 Phase 3: Service Layer (Business Logic)

### Goal
Implement core business logic for applications, file handling, and service integrations.

### Tasks

#### 3.1 ApplicationService.java

**Core Responsibilities:**
```
createApplication(userId, jobPostId, files)
  → Validate user exists (call User Service)
  → Validate job post exists (call Job Manager)
  → Check duplicate application
  → Upload files to MinIO
  → Save application to DB
  → Publish "application.created" event to Kafka
  → Return ApplicationDetailResponse

updateApplicationStatus(applicationId, newStatus, userId)
  → Check authorization (is user owner or recruiter?)
  → Validate status transition
  → Update status in DB
  → Publish "application.status_changed" event to Kafka
  → Return updated ApplicationDetailResponse

getApplicationById(applicationId, userId)
  → Check authorization
  → Retrieve from DB
  → Enrich with user & job post details from external services
  → Return ApplicationDetailResponse

getUserApplications(userId, filters)
  → Retrieve applications for user from DB
  → Apply filters (status, date range, etc.)
  → Add pagination
  → Return List<ApplicationResponse>

getJobPostApplications(jobPostId, recruiterId)
  → Check authorization (is recruiter owner of job post?)
  → Retrieve applications for job post
  → Add pagination & sorting
  → Return List<ApplicationResponse>

deleteApplication(applicationId, userId)
  → Check authorization
  → Soft delete from DB
  → Delete files from MinIO
  → Publish "application.deleted" event
  → Return success

rejectApplication(applicationId, reason)
  → Update status to REJECTED
  → Publish event with reason
  → Return updated response
```

**Methods to implement:**
- `public ApplicationDetailResponse createApplication(UUID userId, CreateApplicationRequest request)`
- `public ApplicationDetailResponse updateApplicationStatus(UUID applicationId, UUID userId, UpdateApplicationStatusRequest request)`
- `public ApplicationDetailResponse getApplicationById(UUID applicationId, UUID requestingUserId)`
- `public Page<ApplicationResponse> getUserApplications(UUID userId, Pageable pageable)`
- `public Page<ApplicationResponse> getJobPostApplications(UUID jobPostId, UUID recruiterId, Pageable pageable)`
- `public void deleteApplication(UUID applicationId, UUID userId)`
- `public List<ApplicationResponse> getApplicationsByStatus(ApplicationStatus status)`
- `private boolean canAccess(UUID applicationId, UUID userId)` - Authorization helper

#### 3.2 FileStorageService.java

**Responsibilities:**
```
uploadFile(file, folderPath)
  → Generate unique file name
  → Upload to MinIO bucket
  → Return MinIO URL
  → Handle errors gracefully

uploadMultipleFiles(files, folderPath)
  → Upload each file
  → Return list of URLs

deleteFile(fileUrl)
  → Extract bucket & object name from URL
  → Delete from MinIO
  → Handle errors gracefully

generatePresignedUrl(fileUrl, expirationMinutes)
  → Generate temporary download URL for file access
  → Return presigned URL

validateFile(file)
  → Check file size (max 50MB)
  → Check file type (PDF, Word, etc.)
  → Return true/false
```

**Methods to implement:**
- `public String uploadFile(MultipartFile file, String folderPath) throws FileUploadException`
- `public List<String> uploadMultipleFiles(List<MultipartFile> files, String folderPath)`
- `public void deleteFile(String fileUrl)`
- `public String getPresignedUrl(String fileUrl, int expirationMinutes)`
- `private void validateFile(MultipartFile file)`

#### 3.3 KafkaPublisherService.java

**Responsibilities:**
```
publishApplicationCreated(applicationId, userId, jobPostId)
publishApplicationStatusChanged(applicationId, oldStatus, newStatus, reason)
publishApplicationDeleted(applicationId)
```

**Topics to publish to:**
- `job-applications.created`
- `job-applications.status-changed`
- `job-applications.deleted`

**Event payload structure:**
```json
{
  "eventId": "uuid",
  "timestamp": "ISO-8601",
  "eventType": "APPLICATION_CREATED",
  "applicationId": "uuid",
  "userId": "uuid",
  "jobPostId": "uuid",
  "status": "APPLIED",
  "details": {}
}
```

#### 3.4 ExternalServiceClient.java (Feign Client)

**Responsibilities:**
```
validateUserExists(userId) → call User Service GET /api/v1/users/{id}
validateJobPostExists(jobPostId) → call Job Manager GET /api/v1/job-posts/{id}
getUserDetails(userId) → get full user profile
getJobPostDetails(jobPostId) → get full job post details
```

**Feign interfaces to create:**
- `UserServiceClient` - calls User Service
- `JobManagerServiceClient` - calls Job Manager service

---

## 🔌 Phase 4: API Layer (REST Controllers)

### Goal
Create REST endpoints following established patterns.

### Tasks

#### 4.1 ApplicationController.java

**Endpoints:**

1. **Health Check**
   - `GET /api/v1/applications/health`
   - Response: `ApiResponse<String>`

2. **Create Application** ⭐
   - `POST /api/v1/applications`
   - Request: `multipart/form-data` with files
   - Response: `ApiResponse<ApplicationDetailResponse>`
   - Auth: Required (current user)

3. **Get Application by ID**
   - `GET /api/v1/applications/{id}`
   - Response: `ApiResponse<ApplicationDetailResponse>`
   - Auth: Required (owner or recruiter)

4. **Get User's Applications**
   - `GET /api/v1/applications/user/{userId}`
   - Params: `page`, `size`, `status`, `sortBy`
   - Response: `ApiResponse<Page<ApplicationResponse>>`
   - Auth: Required (self or admin)

5. **Get Job Post Applications** ⭐
   - `GET /api/v1/applications/job-post/{jobPostId}`
   - Params: `page`, `size`, `status`, `sortBy`
   - Response: `ApiResponse<Page<ApplicationResponse>>`
   - Auth: Required (recruiter owner)

6. **Update Application Status** ⭐
   - `PUT /api/v1/applications/{id}/status`
   - Request: `UpdateApplicationStatusRequest`
   - Response: `ApiResponse<ApplicationDetailResponse>`
   - Auth: Required (recruiter)

7. **Reject Application**
   - `PUT /api/v1/applications/{id}/reject`
   - Request: `{ reason: String }`
   - Response: `ApiResponse<ApplicationDetailResponse>`
   - Auth: Required (recruiter)

8. **Download Resume**
   - `GET /api/v1/applications/{id}/resume/download`
   - Response: File with presigned URL redirect
   - Auth: Required (applicant or recruiter)

9. **Delete Application**
   - `DELETE /api/v1/applications/{id}`
   - Response: `ApiResponse<Void>`
   - Auth: Required (owner only)

10. **Get Applicant Statistics** (Optional - Phase 5)
    - `GET /api/v1/applications/stats/user/{userId}`
    - Response: `{ applied: 5, reviewing: 2, interviews: 1, rejected: 1, offered: 0 }`

#### 4.2 Swagger/OpenAPI Annotations

- Add `@Tag`, `@Operation`, `@Parameter` annotations
- Document request/response models
- Add example values
- Document authorization requirements

---

## 🔐 Phase 5: Configuration & Security

### Goal
Set up external service integrations, configuration, and error handling.

### Tasks

#### 5.1 MinIO Configuration

**MinIOConfig.java**
```java
@Configuration
public class MinIOConfig {
    @Bean
    public MinioClient minioClient(
        @Value("${minio.endpoint}") String endpoint,
        @Value("${minio.access-key}") String accessKey,
        @Value("${minio.secret-key}") String secretKey
    ) {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();
    }
}
```

**application.yml additions:**
```yaml
minio:
  endpoint: http://minio:9000
  access-key: ${MINIO_ACCESS_KEY}
  secret-key: ${MINIO_SECRET_KEY}
  bucket-name: job-applications
  file-max-size: 52428800  # 50MB
```

#### 5.2 Kafka Configuration

**application.yml additions:**
```yaml
spring:
  cloud:
    stream:
      kafka:
        binder:
          brokers: kafka:9092
      bindings:
        applicationCreated-out-0:
          destination: job-applications.created
        applicationStatusChanged-out-0:
          destination: job-applications.status-changed
        applicationDeleted-out-0:
          destination: job-applications.deleted
```

#### 5.3 Feign Client Configuration

**FeignClientConfig.java**
```java
@Configuration
public class FeignClientConfig {
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
```

**Feign interfaces:**
```java
@FeignClient(name = "user-service", url = "${services.user-service.url}")
public interface UserServiceClient {
    @GetMapping("/api/v1/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable UUID id);
}

@FeignClient(name = "job-manager", url = "${services.job-manager.url}")
public interface JobManagerServiceClient {
    @GetMapping("/api/v1/job-posts/{id}")
    ApiResponse<JobPostResponse> getJobPostById(@PathVariable UUID id);
}
```

#### 5.4 Error Handling & Exceptions

**Custom exceptions to create:**
- `ApplicationNotFoundException` - 404
- `DuplicateApplicationException` - 400
- `FileUploadException` - 400
- `UnauthorizedException` - 403
- `JobPostNotFoundException` - 404 (from external service)
- `UserNotFoundException` - 404 (from external service)

**Global exception handler (in common module or here):**
- Create `@RestControllerAdvice` for error responses
- Map exceptions to appropriate HTTP status codes

---

## 🧪 Phase 6: Testing

### Goal
Create comprehensive unit and integration tests.

### Tasks

#### 6.1 Unit Tests

**ApplicationServiceTest.java**
- Test `createApplication()` - success, duplicate, invalid user, invalid job post
- Test `updateApplicationStatus()` - success, unauthorized, invalid status transition
- Test `getApplicationById()` - success, not found, unauthorized
- Test `getUserApplications()` - success, pagination, filtering
- Test `deleteApplication()` - success, unauthorized, not found

**FileStorageServiceTest.java**
- Test `uploadFile()` - success, invalid file type, file too large
- Test `uploadMultipleFiles()` - success, partial failure
- Test `deleteFile()` - success, not found
- Test `validateFile()` - various file types and sizes

**ApplicationControllerTest.java**
- Test endpoint responses with mock service
- Test request validation
- Test response status codes

#### 6.2 Integration Tests

**ApplicationServiceIntegrationTest.java**
- Test with real database (H2 in-memory for tests)
- Test repository queries
- Test transaction handling
- Test cascading deletes (soft delete)

**KafkaPublisherTest.java**
- Test event publishing to Kafka
- Verify event payload structure

---

## 📦 Phase 7: Docker & Deployment

### Goal
Containerize the service and configure for deployment.

### Tasks

#### 7.1 Update Dockerfile
```dockerfile
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/application-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 7.2 Update docker-compose.yml

Add Application Service configuration:
```yaml
application-service:
  build:
    context: ./services/application-service
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/jobapplicant
    SPRING_DATASOURCE_USERNAME: postgres
    SPRING_DATASOURCE_PASSWORD: postgres
    MINIO_ENDPOINT: http://minio:9000
    MINIO_ACCESS_KEY: minioadmin
    MINIO_SECRET_KEY: minioadmin
  ports:
    - "8082:8082"
  depends_on:
    - postgres
    - kafka
    - minio
```

---

## 📝 Implementation Checklist

### Phase 0: Structure
- [ ] Create directory structure
- [ ] Update pom.xml with dependencies
- [ ] Update application.yml configuration
- [ ] Add environment variables to docker-compose.yml

### Phase 1: Database
- [ ] Create Flyway migration V1__create_application_schema.sql
- [ ] Create JobApplication entity
- [ ] Create ApplicationStatus enum
- [ ] Create JobApplicationRepository

### Phase 2: DTOs & Mappers
- [ ] Create request DTOs (CreateApplicationRequest, UpdateApplicationStatusRequest)
- [ ] Create response DTOs (ApplicationResponse, ApplicationDetailResponse, FileUploadResponse)
- [ ] Create ApplicationMapper

### Phase 3: Services
- [ ] Implement ApplicationService (all methods)
- [ ] Implement FileStorageService
- [ ] Implement KafkaPublisherService
- [ ] Create Feign clients (UserServiceClient, JobManagerServiceClient)

### Phase 4: Controllers
- [ ] Create ApplicationController with all endpoints
- [ ] Add Swagger/OpenAPI annotations
- [ ] Test endpoints with Postman/curl

### Phase 5: Configuration
- [ ] Create MinIOConfig
- [ ] Create FeignClientConfig
- [ ] Create custom exceptions
- [ ] Create global exception handler

### Phase 6: Testing
- [ ] Write unit tests for services
- [ ] Write integration tests
- [ ] Write controller tests
- [ ] Achieve 80%+ code coverage

### Phase 7: Deployment
- [ ] Update Dockerfile
- [ ] Update docker-compose.yml
- [ ] Test with Docker Compose
- [ ] Verify service communication

---

## 🔗 Dependencies & Integration Points

### Outbound Integrations
1. **User Service** - Validate user exists, get user details
   - GET `/api/v1/users/{id}`
2. **Job Manager Service** - Validate job post exists, get job post details
   - GET `/api/v1/job-posts/{id}`
3. **MinIO** - Store/retrieve application files
4. **Kafka** - Publish application events
5. **PostgreSQL** - Store application records

### Inbound Integrations
1. **API Gateway** - Route incoming requests
2. **Notification Service** - Consume application events
3. **Admin Service** - Query application statistics

---

## 🎯 Success Criteria

✅ **Functionality**
- All CRUD operations work correctly
- File uploads/downloads function properly
- Status transitions are valid
- Soft deletes work as expected

✅ **Integration**
- Service-to-service communication works
- Kafka events are published successfully
- MinIO stores files correctly

✅ **Quality**
- Code follows project patterns (LayeredArchitecture, DTOs, Mappers)
- All tests pass with 80%+ coverage
- No sonarqube critical issues

✅ **Documentation**
- Swagger/OpenAPI spec is accurate
- Code comments explain complex logic
- README documents local setup

---

## 📅 Estimated Timeline

| Phase | Duration | Notes |
|-------|----------|-------|
| Phase 0 | 1 day | Setup & configuration |
| Phase 1 | 1 day | Database layer |
| Phase 2 | 1 day | DTOs & mappers |
| Phase 3 | 2-3 days | Service layer (most complex) |
| Phase 4 | 1 day | Controller layer |
| Phase 5 | 1 day | Configuration & security |
| Phase 6 | 1-2 days | Testing |
| Phase 7 | 1 day | Docker & deployment |
| **Total** | **9-11 days** | Depends on complexity & issues |

---

## ❓ Questions to Confirm

Before starting implementation, please confirm:

1. ✅ **Scope Confirmation** - Are we only implementing the Application Service, or should we also create/update the Job Manager service?

2. ✅ **File Storage** - Should we use MinIO for local development and S3 for production? Any bucket naming conventions?

3. ✅ **Status Workflow** - Is the status flow linear (APPLIED → REVIEWED → INTERVIEW → OFFERED/REJECTED) or can statuses transition freely?

4. ✅ **Authorization** - Should only the recruiter who created the job post be able to see/manage applications for it?

5. ✅ **Notifications** - When application status changes, should we immediately notify the user (via email/push)?

6. ✅ **Job Post Details** - Should we cache job post details or fetch them each time for API requests?

7. ✅ **Pagination** - Any preferred default page size? (Current default: typically 20)

8. ✅ **Additional Files** - Should we limit the number of additional files? Any specific file type restrictions beyond PDFs/Word?

---

## 📞 Next Steps

1. **Review this plan** - Confirm all phases and requirements
2. **Answer confirmation questions** - Clarify any ambiguities
3. **Start Phase 0** - I'll begin with project structure and configuration
4. **Progress through phases** - Each phase builds on previous work
5. **Testing & deployment** - Final validation and Docker setup

**Ready to proceed? Please confirm the plan and answer the questions above! 🚀**
