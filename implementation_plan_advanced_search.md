# Implementation Plan: Advanced User Search

This document outlines the plan to implement advanced search and filtering capabilities for the `user-service`, based on the requirements from the Job Manager (JM) SRS (sections 5.1 and 5.2).

## Phase 1: API and Service Layer Foundation

This phase focuses on creating the search endpoint and service logic for basic filtering, without implementing advanced database features like Full-Text Search yet.

### Task 1.1: Define the Search Endpoint

- **File:** `services/user-service/src/main/java/com/team/ja/user/api/UserController.java`
- **Action:** Create a new `GET /api/v1/users/search` endpoint.
- **Parameters:** The endpoint will accept optional query parameters:
  - `skills`: A comma-separated list of skill names (e.g., "java,kafka").
  - `country`: A country abbreviation (e.g., "VN", "SG").
  - `keyword`: A general keyword for a simple `LIKE` search (as a placeholder for FTS).

### Task 1.2: Create a Search Specification Class

- **File:** `services/user-service/src/main/java/com/team/ja/user/repository/specification/UserSpecification.java` (new file)
- **Action:** Create a new class that uses the `jakarta.persistence.criteria` API (JPA Specifications) to build a dynamic query.
- **Details:** This class will have static methods to create `Specification<User>` objects based on the provided search criteria (skills, country, keyword). This approach allows us to easily chain multiple `AND` conditions.

### Task 1.3: Update the Service Layer

- **File:** `services/user-service/src/main/java/com/team/ja/user/service/UserService.java` (and its implementation)
- **Action:** Add a new `searchUsers` method that takes the search criteria DTO.
- **Details:** This method will use the `UserSpecification` class to build the final query specification and pass it to the repository.

### Task 1.4: Update the Repository

- **File:** `services/user-service/src/main/java/com/team/ja/user/repository/UserRepository.java`
- **Action:** Modify the `UserRepository` interface to extend `JpaSpecificationExecutor<User>`.
- **Details:** This interface from Spring Data JPA provides the `findAll(Specification<T> spec)` method, which is needed to execute the dynamic queries we build.

---

## Phase 2: Database Enhancements for Full-Text Search (FTS)

This phase will replace the simple keyword search with a powerful, index-based Full-Text Search using PostgreSQL's capabilities.

### Task 2.1: Create FTS Database Migration

- **File:** New Flyway migration script in `services/user-service/src/main/resources/db/migration/`
- **Action:**
  1. Add a new column of type `tsvector` (e.g., `fts_document`) to the `users` table (or the table containing the profile text).
  2. Create a GIN index on this new `tsvector` column to ensure searches are fast.

### Task 2.2: Implement Automatic `tsvector` Updates

- **File:** New Flyway migration script.
- **Action:** Create a database trigger function that concatenates all text fields to be searched (`objective_summary`, `work_experience` descriptions, etc.) and populates the `fts_document` column whenever a user's profile is created or updated.

### Task 2.3: Integrate FTS into the Specification

- **File:** `services/user-service/src/main/java/com/team/ja/user/repository/specification/UserSpecification.java`
- **Action:** Update the specification class to incorporate a native PostgreSQL FTS query.
- **Details:** When the `keyword` parameter is present, the specification will use a custom criteria that calls `to_tsquery` and matches it against the `fts_document` column. This replaces the simple `LIKE` search from Phase 1.

---

## Phase 3: Testing and Verification

### Task 3.1: Unit & Integration Testing

- **Action:**
  - Write unit tests for the `UserSpecification` class to verify that the correct query criteria are generated for different combinations of inputs.
  - Write integration tests for the `UserController`'s search endpoint to confirm the end-to-end flow.

### Task 3.2: Manual Verification

- **Action:** Use `curl` or a similar tool to perform a series of manual tests against a running instance to validate:
  - Filtering by country.
  - Filtering by one or more skills.
  - Searching by a keyword using FTS.
  - Combining multiple filters in a single request.
