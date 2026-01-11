# Application Service - Internal API Documentation

## Overview
Internal service-to-service communication endpoints for Job Manager (JM) system to query and manage job applications.

**Base URL**: `http://application-service:8083` (via Eureka) or `http://localhost:8083` (direct)

**Authentication**: None required (internal service calls only)

---

## Endpoints

### 1. Get Applications by Job Post ID

Retrieve all applications submitted for a specific job post.

**Endpoint**: `GET /api/v1/internal/job-posts/{jobPostId}/applications`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `jobPostId` | UUID | Yes | The job post ID |

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | Integer | No | 0 | Page number (0-indexed) |
| `size` | Integer | No | 20 | Number of items per page |
| `status` | String | No | - | Filter by application status (PENDING, REVIEWING, SHORTLISTED, INTERVIEWED, OFFERED, ACCEPTED, REJECTED, WITHDRAWN) |

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Applications retrieved successfully",
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
        "jobPostId": "a3bb189e-8bf9-3888-9912-ace4e6543002",
        "resumeUrl": "https://s3.amazonaws.com/bucket/applications/resumes/file.pdf",
        "coverLetterUrl": "https://s3.amazonaws.com/bucket/applications/cover-letters/file.pdf",
        "status": "PENDING",
        "userNotes": "Very interested in this position",
        "adminNotes": null,
        "appliedAt": "2026-01-08T10:30:00",
        "updatedAt": "2026-01-08T10:30:00",
        "deletedAt": null
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 45,
    "totalPages": 3,
    "first": true,
    "last": false
  },
  "timestamp": "2026-01-09T12:00:00"
}
```

**Example Requests**:
```bash
# Get first page of all applications
GET http://application-service:8083/api/v1/internal/job-posts/a3bb189e-8bf9-3888-9912-ace4e6543002/applications

# Get second page with 10 items
GET http://application-service:8083/api/v1/internal/job-posts/a3bb189e-8bf9-3888-9912-ace4e6543002/applications?page=1&size=10

# Filter by status
GET http://application-service:8083/api/v1/internal/job-posts/a3bb189e-8bf9-3888-9912-ace4e6543002/applications?status=SHORTLISTED
```

---

### 2. Download Application File

Download resume or cover letter for a specific application.

**Endpoint**: `GET /api/v1/internal/job-posts/applications/{applicationId}/files/{docType}`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `applicationId` | UUID | Yes | The application ID |
| `docType` | Enum | Yes | Document type: `RESUME` or `COVER_LETTER` |

**Response**: `200 OK`
- **Content-Type**: `application/pdf`
- **Content-Disposition**: `inline; filename="resume.pdf"` or `inline; filename="cover-letter.pdf"`
- **Body**: Binary PDF file content

**Error Responses**:
- `404 Not Found` - Application not found or file does not exist

**Example Requests**:
```bash
# Download resume
GET http://application-service:8083/api/v1/internal/job-posts/applications/550e8400-e29b-41d4-a716-446655440000/files/RESUME

# Download cover letter
GET http://application-service:8083/api/v1/internal/job-posts/applications/550e8400-e29b-41d4-a716-446655440000/files/COVER_LETTER
```

**cURL Examples**:
```bash
# Download resume and save to file
curl -o resume.pdf http://application-service:8083/api/v1/internal/job-posts/applications/550e8400-e29b-41d4-a716-446655440000/files/RESUME

# Download cover letter and save to file
curl -o cover-letter.pdf http://application-service:8083/api/v1/internal/job-posts/applications/550e8400-e29b-41d4-a716-446655440000/files/COVER_LETTER
```

---

## Data Models

### ApplicationResponse

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Application unique identifier |
| `userId` | UUID | Applicant's user ID |
| `jobPostId` | UUID | Job post ID this application is for |
| `resumeUrl` | String | S3 URL to resume file |
| `coverLetterUrl` | String (nullable) | S3 URL to cover letter file |
| `status` | String | Application status |
| `userNotes` | String (nullable) | Notes from applicant |
| `adminNotes` | String (nullable) | Notes from admin/recruiter |
| `appliedAt` | DateTime | When the application was submitted |
| `updatedAt` | DateTime | Last update timestamp |
| `deletedAt` | DateTime (nullable) | Soft delete timestamp (null if active) |

### Application Status Values

| Status | Description |
|--------|-------------|
| `PENDING` | Application submitted, awaiting review |
| `REVIEWING` | Application is being reviewed |
| `SHORTLISTED` | Applicant has been shortlisted |
| `INTERVIEWED` | Applicant has been interviewed |
| `OFFERED` | Job offer has been made |
| `ACCEPTED` | Applicant accepted the offer |
| `REJECTED` | Application was rejected |
| `WITHDRAWN` | Applicant withdrew the application |

---

## Usage Notes

1. **Service Discovery**: Use Eureka service name `application-service` for service-to-service calls
2. **No Authentication**: These endpoints are internal-only and do not require user authentication
3. **Soft Deletes**: Applications with `deletedAt != null` are withdrawn and excluded from results
4. **File Access**: Files are stored in S3/SeaweedFS and streamed through the API
5. **Pagination**: Default page size is 20, maximum recommended is 100

---

## Integration Example (Java - OpenFeign)

```java
@FeignClient(name = "application-service")
public interface ApplicationServiceClient {
    
    @GetMapping("/api/v1/internal/job-posts/{jobPostId}/applications")
    ApiResponse<Page<ApplicationResponse>> getApplicationsByJobPost(
        @PathVariable("jobPostId") UUID jobPostId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size,
        @RequestParam(value = "status", required = false) String status
    );
    
    @GetMapping("/api/v1/internal/job-posts/applications/{applicationId}/files/{docType}")
    ResponseEntity<byte[]> downloadApplicationFile(
        @PathVariable("applicationId") UUID applicationId,
        @PathVariable("docType") String docType
    );
}
```

---

## Error Handling

All endpoints follow standard error response format:

```json
{
  "success": false,
  "message": "Error description",
  "errorCode": "ERROR_CODE",
  "timestamp": "2026-01-09T12:00:00"
}
```

Common error codes:
- `NOT_FOUND` - Resource not found
- `INTERNAL_ERROR` - Server error
- `INVALID_REQUEST` - Invalid request parameters
