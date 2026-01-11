# Job Post Service API Documentation

## Table of Contents
- [Search Job Posts](#search-job-posts)
- [Delete Job Post](#delete-job-post)

---

## Search Job Posts

### Endpoint
```
POST /api/external/job-posts/search
```

### Description
Search for published job posts with various filter criteria. This endpoint is designed for the Job Applicant (JA) microservice to search through available job postings. The search supports case-insensitive title matching, multiple employment types, location filtering, salary range filtering, and fresher status.

### Authentication
Required: Yes (Service-to-service authentication)

### Request Headers
| Header | Type | Required | Description |
|--------|------|----------|-------------|
| Content-Type | string | Yes | `application/json` |

### Request Body
```json
{
  "title": "string (optional)",
  "employmentTypes": ["FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP"] (optional),
  "locationCity": "string (optional)",
  "countryCode": "string (optional)",
  "minSalary": "decimal (optional)",
  "maxSalary": "decimal (optional)",
  "fresher": "boolean (optional)",
  "page": "integer (optional, default: 0)",
  "size": "integer (optional, default: 10, min: 1)"
}
```

#### Request Body Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| title | string | No | Case-insensitive search for job title |
| employmentTypes | array[enum] | No | Filter by employment types: `FULL_TIME`, `PART_TIME`, `CONTRACT`, `INTERNSHIP` |
| locationCity | string | No | Filter by city location |
| countryCode | string | No | Filter by country code (ISO 3166-1 alpha-2) |
| minSalary | decimal | No | Minimum salary filter |
| maxSalary | decimal | No | Maximum salary filter |
| fresher | boolean | No | Filter for fresher-friendly positions. If `false` or `null`, shows all job posts |
| page | integer | No | Page number (0-indexed). Default: `0`. Min: `0` |
| size | integer | No | Page size. Default: `10`. Min: `1` |

### Response

#### Success Response (200 OK)
```json
{
  "content": [
    {
      "id": "uuid",
      "companyId": "uuid",
      "title": "string",
      "description": "string",
      "locationCity": "string",
      "countryCode": "string",
      "salaryType": "HOURLY | MONTHLY | ANNUAL | NEGOTIABLE",
      "salaryMin": "decimal",
      "salaryMax": "decimal",
      "salaryNote": "string",
      "employmentTypes": ["FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP"],
      "isFresher": "boolean",
      "skillIds": ["uuid"],
      "postedAt": "datetime (ISO 8601)",
      "expiryAt": "datetime (ISO 8601)",
      "isActive": "boolean"
    }
  ],
  "pageable": {
    "sort": { "sorted": false, "unsorted": true, "empty": true },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 1,
  "totalElements": 5,
  "last": true,
  "first": true,
  "size": 10,
  "number": 0,
  "sort": { "sorted": false, "unsorted": true, "empty": true },
  "numberOfElements": 5,
  "empty": false
}
```

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Job post unique identifier |
| companyId | UUID | Company that posted the job |
| title | string | Job title |
| description | string | Detailed job description |
| locationCity | string | City where job is located |
| countryCode | string | Country code (ISO 3166-1 alpha-2) |
| salaryType | enum | Type of salary: `HOURLY`, `MONTHLY`, `ANNUAL`, `NEGOTIABLE` |
| salaryMin | decimal | Minimum salary |
| salaryMax | decimal | Maximum salary |
| salaryNote | string | Additional salary information |
| employmentTypes | array[enum] | List of employment types for this position |
| isFresher | boolean | Whether the position is suitable for freshers |
| skillIds | array[UUID] | Required skills for the job |
| postedAt | datetime | When the job was posted (ISO 8601 format) |
| expiryAt | datetime | When the job posting expires (ISO 8601 format) |
| isActive | boolean | Whether the job is currently active |

### Example Request

```bash
curl -X POST http://localhost:8083/api/external/job-posts/search \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Software Engineer",
    "employmentTypes": ["FULL_TIME", "CONTRACT"],
    "locationCity": "New York",
    "countryCode": "US",
    "minSalary": 60000,
    "maxSalary": 120000,
    "fresher": false,
    "page": 0,
    "size": 20
  }'
```

### Example Response

```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "companyId": "987e6543-e21b-12d3-a456-426614174000",
      "title": "Senior Software Engineer",
      "description": "We are looking for an experienced software engineer...",
      "locationCity": "New York",
      "countryCode": "US",
      "salaryType": "ANNUAL",
      "salaryMin": 80000,
      "salaryMax": 110000,
      "salaryNote": "Plus benefits and bonus",
      "employmentTypes": ["FULL_TIME"],
      "isFresher": false,
      "skillIds": ["abc12345-e89b-12d3-a456-426614174000"],
      "postedAt": "2026-01-10T10:30:00Z",
      "expiryAt": "2026-02-10T10:30:00Z",
      "isActive": true
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "offset": 0
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "first": true,
  "size": 20,
  "number": 0,
  "numberOfElements": 1,
  "empty": false
}
```

### Notes
- All filters are optional and can be combined
- Title search is case-insensitive
- Only **published** job posts are returned
- The `published` field will always be `true` in search results
- Pagination is 0-indexed (first page is `0`)

---

## Delete Job Post

### Endpoint
```
DELETE /api/job-posts/{id}
```

### Description
Permanently delete a job post by its unique identifier. This action cannot be undone.

### Authentication
Required: Yes (Company authentication)

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | The unique identifier of the job post to delete |

### Request Headers
| Header | Type | Required | Description |
|--------|------|----------|-------------|
| Authorization | string | Yes | Bearer token for authentication |

### Response

#### Success Response (200 OK)
```json
{
  "success": true,
  "message": "Job post deleted successfully",
  "data": null
}
```

#### Error Response (400 Bad Request)
```json
{
  "success": false,
  "message": "Failed to delete job post: Job post not found with ID: {id}",
  "data": null
}
```

#### Error Response (404 Not Found)
When job post doesn't exist.

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| success | boolean | Indicates if the operation was successful |
| message | string | Human-readable message describing the result |
| data | null | Always null for delete operations |

### Example Request

```bash
curl -X DELETE http://localhost:8083/api/job-posts/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Example Success Response

```json
{
  "success": true,
  "message": "Job post deleted successfully",
  "data": null
}
```

### Example Error Response

```json
{
  "success": false,
  "message": "Failed to delete job post: Job post not found with ID: 123e4567-e89b-12d3-a456-426614174000",
  "data": null
}
```

### Error Codes

| HTTP Status | Description |
|-------------|-------------|
| 200 | Job post deleted successfully |
| 400 | Invalid request or job post not found |
| 401 | Unauthorized - Missing or invalid authentication token |
| 403 | Forbidden - User doesn't have permission to delete this job post |
| 404 | Job post not found |

### Notes
- Only the company that created the job post can delete it
- Deletion is permanent and cannot be reversed
- Any associated data (skills, employment types, etc.) will also be removed
- Active applications linked to this job post may be affected (check application service documentation)
- Logging statement: `"Deleting job post with ID: {id}"`

---

## Common Data Types

### EmploymentType Enum
- `FULL_TIME`
- `PART_TIME`
- `CONTRACT`
- `INTERNSHIP`

### SalaryType Enum
- `HOURLY` - Hourly wage
- `MONTHLY` - Monthly salary
- `ANNUAL` - Annual salary
- `NEGOTIABLE` - Salary is negotiable

---

## Error Handling

All endpoints return consistent error responses in the following format:

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

Common HTTP status codes:
- `200 OK` - Request successful
- `400 Bad Request` - Invalid request parameters
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## Version Information
- **Service**: job-manager-jobpost
- **Base URL**: `http://localhost:8083`
- **API Version**: v1
- **Last Updated**: January 11, 2026
