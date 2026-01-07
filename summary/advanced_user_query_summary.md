# Advanced User Querying Implementation Summary

This document summarizes the implementation of advanced search and filtering capabilities for the `user-service`, addressing requirements from the Job Manager (JM) SRS related to applicant search.

## Implemented Features (Phase 1: API and Service Layer Foundation)

The initial phase focused on establishing the core API endpoint and service logic to support filtering users based on various criteria.

### Endpoint: `GET /api/v1/users/search`

This endpoint allows authenticated clients to search for users by providing optional query parameters.

| Query Parameter | Description                                                                | Example Value                  |
| :-------------- | :------------------------------------------------------------------------- | :----------------------------- |
| `skills`        | A comma-separated list of skill names to filter users by. Users matching ANY of the provided skills will be returned. | `java,spring-boot,kafka`       |
| `country`       | The two-letter abbreviation of a country to filter users by.               | `VN` (for Vietnam), `US` (for United States) |
| `keyword`       | A general keyword for Full-Text Search across user profiles. | `engineer`, `developer`          |

**Example Request:**
`GET /api/v1/users/search?skills=java,spring&country=VN&keyword=backend`

### Technical Details

The search functionality is implemented using Spring Data JPA.

*   **`UserController`:** Exposes the `GET /api/v1/users/search` endpoint, taking query parameters and delegating to the `UserService`.
*   **`UserServiceImpl`:** Processes the incoming parameters, performs necessary lookups (e.g., resolving `country` abbreviation to a `UUID`), and constructs the query.
*   **`UserSpecification`:** A utility class containing static methods that return `Specification<User>` instances for filter criteria (`hasSkills`, `hasCountry`). These specifications are chained together using `AND` operators.
*   **`UserRepository`:** Extends `JpaSpecificationExecutor<User>`, allowing `UserServiceImpl` to execute queries built with `Specification` objects. It also includes a native `@Query` method for Full-Text Search.
*   **Entity Mappings:** Correct `@OneToMany` and `@ManyToOne` mappings were added between `User`, `UserSkill`, and `Skill` entities to facilitate accurate joins within the `hasSkills` specification.

## Full-Text Search (FTS) Implementation (Phase 2)

Full-Text Search capabilities were integrated using PostgreSQL's native `tsvector` type and related functions.

*   **Database Migrations:**
    *   `V4__add_fts_to_users.sql`: Added an `fts_document` column of type `TSVECTOR` to the `users` table and created a GIN index on it.
    *   `V5__create_fts_trigger.sql`: Created a database function and trigger to automatically populate and update the `fts_document` column whenever `first_name`, `last_name`, or `objective_summary` fields in the `users` table are inserted or updated.
*   **`User` Entity:** The `ftsDocument` field was added to the `User` entity, mapped to the `fts_document` column with `columnDefinition = "TSVECTOR"`.
*   **`UserRepository`:** A native `@Query` method (`findByFts`) was added to execute PostgreSQL's `to_tsquery` function against the `fts_document` column, handling the keyword search.
*   **`UserServiceImpl`:** Modified to prioritize the native `findByFts` query when a `keyword` is provided, falling back to `Specification`-based filtering for other criteria.

## Testing and Verification (Phase 3)

All implemented features have been thoroughly tested and verified through manual `curl` requests.

*   **JWE Tokens:** Successfully implemented and verified end-to-end.
*   **Basic Search (No Filters):** Confirmed the endpoint is accessible and returns all users.
*   **Country Filter:** Verified correct filtering of users by country abbreviation.
*   **Skills Filter:** Verified correct filtering of users by a list of skills.
*   **Full-Text Search (FTS):** Verified that searching by keywords (e.g., "engineer", "developer") accurately returns users with matching content in their profile summary or name, utilizing the PostgreSQL FTS capabilities.

## Seed Data

*   **`V6__seed_users_for_testing.sql` (user-service):** A new migration script was added to seed two test user profiles with diverse data, including skills, objective summaries, and countries.
*   **`V3__seed_auth_credentials_for_testing.sql` (auth-service):** A new migration script was added to seed corresponding authentication credentials for the test users, allowing seamless login and testing.