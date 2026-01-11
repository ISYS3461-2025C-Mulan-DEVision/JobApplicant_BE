<!-- File: job-manager-jobpost/API_DOCUMENTATION_COMPANIES.md -->
# GET /api/companies ✅

## Overview
**Purpose:** Retrieve all companies (list or paginated list).
**Method:** `GET`
**Path:** `/api/companies`
**Produces:** `application/json`
**Auth:** Optional; include `Authorization: Bearer <token>` if protected.

---

## Query parameters
- `page` (integer, optional) — page index (0-based)
- `size` (integer, optional) — items per page
- `sort` (string, optional) — e.g., `name,asc`
- `q` (string, optional) — search / filter term (e.g., by name)

Example:
```
GET /api/companies?page=0&size=20&sort=name,asc
```

---

## Success responses
### 200 OK — Simple list
```json
[
  {
    "id": 1,
    "name": "Acme Corp",
    "country": "US",
    "website": "https://acme.example",
    "createdAt": "2024-08-01T12:34:56Z"
  }
]
```

### 200 OK — Paginated response
```json
{
  "content": [ /* Company objects */ ],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 123,
  "totalPages": 7,
  "last": false
}
```

---

## Error responses
- `400 Bad Request` — invalid query params
- `401 Unauthorized` — missing or invalid auth
- `500 Internal Server Error` — server failure

---

## cURL examples
Simple:
```bash
curl -sS "https://api.example.com/api/companies"
```
With pagination and auth:
```bash
curl -sS -H "Authorization: Bearer $TOKEN" \
  "https://api.example.com/api/companies?page=0&size=25&sort=name,asc"
```

---

## OpenAPI snippet
```yaml
paths:
  /api/companies:
    get:
      summary: Get companies
      parameters:
        - in: query
          name: page
          schema:
            type: integer
        - in: query
          name: size
          schema:
            type: integer
        - in: query
          name: sort
          schema:
            type: string
      responses:
        '200':
          description: A list of companies (or paged response)
          content:
            application/json:
              schema:
                oneOf:
                  - type: array
                    items:
                      $ref: '#/components/schemas/Company'
                  - $ref: '#/components/schemas/PagedCompanies'
        '401':
          description: Unauthorized
        '500':
          description: Internal server error

components:
  schemas:
    Company:
      type: object
      properties:
        id: { type: integer }
        name: { type: string }
        country: { type: string }
        website: { type: string, format: uri }
        createdAt: { type: string, format: date-time }
    PagedCompanies:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/Company'
        totalElements: { type: integer }
        totalPages: { type: integer }
        pageable:
          type: object
```

---

## Implementation notes 🔧
- Prefer pagination for large datasets to avoid memory issues.
- Support filters (country, name) to make queries efficient.
- Add caching headers or ETag if the list changes infrequently.

> Tip: If you'd like, I can add a small Spring `@GetMapping("/api/companies")` controller sample and a unit test to this file.
