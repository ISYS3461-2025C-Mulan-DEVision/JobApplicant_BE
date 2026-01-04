# Application Service API Endpoints

**Base URL:** `/api/v1/applications` | **Port:** 8083

---

## PUBLIC ENDPOINTS (Applicant)

### 1. Create Job Application
| Property | Value |
|----------|-------|
| **Endpoint** | `POST /api/v1/applications` |
| **Description** | Submit a new job application with resume and cover letter files |
| **Path Parameters** | None |
| **Query Parameters** | None |
| **Request Body** | `jobPostId` (UUID), `resumeFile` (File), `coverLetterFile` (File) |

### 2. Get User's Applications
| Property | Value |
|----------|-------|
| **Endpoint** | `GET /api/v1/applications` |
| **Description** | Retrieve all applications submitted by authenticated user |
| **Path Parameters** | None |
| **Query Parameters** | `page` (int, default 0), `size` (int, default 10), `status` (String), `sortBy` (String), `sortOrder` (String) |
| **Request Body** | None |

### 3. Get Application by ID
| Property | Value |
|----------|-------|
| **Endpoint** | `GET /api/v1/applications/{applicationId}` |
| **Description** | Get detailed information about a specific application |
| **Path Parameters** | `applicationId` (UUID) - The application ID |
| **Query Parameters** | None |
| **Request Body** | None |

### 4. Update Application Status
| Property | Value |
|----------|-------|
| **Endpoint** | `PATCH /api/v1/applications/{applicationId}/status` |
| **Description** | Update application status (SUBMITTED, REVIEW, INTERVIEW, OFFERED, REJECTED, WITHDRAWN) |
| **Path Parameters** | `applicationId` (UUID) - The application ID |
| **Query Parameters** | None |
| **Request Body** | `status` (String), `notes` (String optional) |

### 5. Withdraw Application
| Property | Value |
|----------|-------|
| **Endpoint** | `DELETE /api/v1/applications/{applicationId}` |
| **Description** | Soft delete (withdraw) a job application |
| **Path Parameters** | `applicationId` (UUID) - The application ID |
| **Query Parameters** | None |
| **Request Body** | None |

### 6. Download Application Files
| Property | Value |
|----------|-------|
| **Endpoint** | `GET /api/v1/applications/{applicationId}/files/{docType}` |
| **Description** | Download resume or cover letter files as binary stream (inline preview or download) |
| **Path Parameters** | `applicationId` (UUID), `docType` (DocType Enum: RESUME, COVER_LETTER) |
| **Query Parameters** | None |
| **Request Body** | None |
| **Response** | Binary file stream (PDF/DOC), inline display or attachment download |
| **Authentication** | JWT Token Required, User must own the application |

---

## INTERNAL ENDPOINTS (Service-to-Service Communication)

### 1. Get Applications for Job Post
| Property | Value |
|----------|-------|
| **Endpoint** | `GET /api/v1/internal/job-posts/{jobPostId}/applications` |
| **Description** | Get all applications for a specific job post (called by job-posting-service) |
| **Path Parameters** | `jobPostId` (UUID) - The job post ID |
| **Query Parameters** | `page` (int), `size` (int), `status` (String), `sortBy` (String), `sortOrder` (String) |
| **Request Body** | None |

---

## ADMIN ENDPOINTS

### 1. Get All Applications
| Property | Value |
|----------|-------|
| **Endpoint** | `GET /api/v1/admin/applications` |
| **Description** | Admin: Retrieve all applications in the system with pagination |
| **Path Parameters** | None |
| **Query Parameters** | `page` (int, default 0), `size` (int, default 20), `status` (String), `userId` (UUID), `jobPostId` (UUID), `sortBy` (String), `sortOrder` (String) |
| **Request Body** | None |

### 2. Get Application by ID (Admin)
| Property | Value |
|----------|-------|
| **Endpoint** | `GET /api/v1/admin/applications/{applicationId}` |
| **Description** | Admin: Get detailed information about any application |
| **Path Parameters** | `applicationId` (UUID) - The application ID |
| **Query Parameters** | None |
| **Request Body** | None |

### 3. Update Application Status (Admin)
| Property | Value |
|----------|-------|
| **Endpoint** | `PATCH /api/v1/admin/applications/{applicationId}/status` |
| **Description** | Admin: Update application status with admin notes |
| **Path Parameters** | `applicationId` (UUID) - The application ID |
| **Query Parameters** | None |
| **Request Body** | `status` (String), `adminNotes` (String) |

### 4. Delete Application (Hard Delete)
| Property | Value |
|----------|-------|
| **Endpoint** | `DELETE /api/v1/admin/applications/{applicationId}` |
| **Description** | Admin: Permanently delete an application from the system |
| **Path Parameters** | `applicationId` (UUID) - The application ID |
| **Query Parameters** | None |
| **Request Body** | None |

### 5. Get Application Statistics
| Property | Value |
|----------|-------|
| **Endpoint** | `GET /api/v1/admin/applications/statistics` |
| **Description** | Admin: Get application statistics (total, by status, by date range) |
| **Path Parameters** | None |
| **Query Parameters** | `startDate` (DateTime), `endDate` (DateTime) |
| **Request Body** | None |

### 6. Bulk Update Application Status
| Property | Value |
|----------|-------|
| **Endpoint** | `PATCH /api/v1/admin/applications/bulk/status` |
| **Description** | Admin: Update status for multiple applications at once |
| **Path Parameters** | None |
| **Query Parameters** | None |
| **Request Body** | `applicationIds` (UUID[]), `status` (String), `adminNotes` (String) |

### 7. Restore Deleted Application
| Property | Value |
|----------|-------|
| **Endpoint** | `POST /api/v1/admin/applications/{applicationId}/restore` |
| **Description** | Admin: Restore a soft-deleted application |
| **Path Parameters** | `applicationId` (UUID) - The application ID |
| **Query Parameters** | None |
| **Request Body** | None |

---

## Application Status Values
- `SUBMITTED` - Initial submission
- `REVIEW` - Under company review
- `INTERVIEW` - Interview scheduled
- `OFFERED` - Job offered
- `REJECTED` - Application rejected
- `WITHDRAWN` - Applicant withdrew

## Authentication
All endpoints require JWT token in header: `Authorization: Bearer <JWT_TOKEN>`
