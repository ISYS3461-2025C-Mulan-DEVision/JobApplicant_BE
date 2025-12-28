# Implementation Plan: User Portfolio Upload

This document outlines the plan to implement the user portfolio feature (SRS `3.2.3`), allowing job applicants to upload multiple images and videos to showcase their work. The implementation will include robust, enterprise-level exception handling.

---

## Phase 1: Database and Entity Layer

This phase sets up the database schema and JPA entities required to store portfolio information.

1.  **Create `UserPortfolioItem` Entity:**
    *   Create a new entity class `UserPortfolioItem.java`.
    *   It will include fields for `id` (UUID), `fileUrl` (String), `description` (String, optional), `mediaType` (String), and a `@ManyToOne` relationship to the `User` entity.

2.  **Update `User` Entity:**
    *   Add a `@OneToMany` relationship in the `User.java` entity to create a collection of `UserPortfolioItem` entities (e.g., `private Set<UserPortfolioItem> portfolioItems`).

3.  **Create Flyway Migration:**
    *   Create a new migration script (`V7__create_user_portfolio_items_table.sql`) to add the `user_portfolio_items` table to the `user-db` schema. This table will include a foreign key constraint linking to the `users` table.

---

## Phase 2: API and Service Layer

This phase builds the API endpoints and business logic for managing portfolio items.

1.  **Create DTOs:**
    *   `UserPortfolioItemResponse`: A DTO to represent portfolio items in API responses.
    *   `UploadPortfolioItemRequest`: A request object that may be used with `@RequestPart` for structured metadata.

2.  **Create `UserPortfolioController`:**
    *   A new controller to handle all portfolio-related endpoints.
    *   **`POST /api/v1/users/{userId}/portfolio`**: Uploads a new portfolio item. Takes `MultipartFile` and an optional `description`.
    *   **`DELETE /api/v1/users/{userId}/portfolio/{itemId}`**: Deletes a specific portfolio item.
    *   **`GET /api/v1/users/{userId}/portfolio`**: Retrieves a list of all portfolio items for a user.
    *   All endpoints will validate that the authenticated user (`X-User-Id` header) matches the `{userId}` in the path.

3.  **Implement `UserPortfolioService`:**
    *   **`uploadItem(...)`**:
        *   Validate the file (type, size).
        *   Upload the file to the S3-compatible storage using the existing `S3FileService`.
        *   Create and save a `UserPortfolioItem` record in the database.
    *   **`deleteItem(...)`**:
        *   Verify the item belongs to the user.
        *   Delete the file from S3 storage.
        *   Delete the `UserPortfolioItem` record from the database.
    *   **`getItemsForUser(...)`**:
        *   Retrieve all portfolio items for a given user.

---

## Phase 3: Enterprise-Level Exception Handling & Refinement

This phase focuses on making the feature robust and secure.

1.  **Custom Exceptions for File Handling:**
    *   Create specific, checked exceptions like `UnsupportedFileTypeException` and `FileSizeExceededException`.
    *   In `UserPortfolioServiceImpl`, throw these exceptions when validation fails.

2.  **Global Exception Handler:**
    *   Update `GlobalExceptionHandler.java` to include `@ExceptionHandler` methods for the new custom exceptions, mapping them to clear `400 Bad Request` API responses.

3.  **Storage Service Robustness:**
    *   In `S3FileService`, wrap SDK exceptions (e.g., `AmazonS3Exception`) in a custom `StorageException` (which could be a `RuntimeException`).
    *   This abstraction prevents leaking AWS SDK details to the service layer and provides a consistent error type for storage-related problems. The `GlobalExceptionHandler` can then catch `StorageException` and return a `500 Internal Server Error` with a generic "Could not process file storage operation" message, while logging the detailed cause.

4.  **Transactional Integrity:**
    *   The `deleteItem` service method will be marked `@Transactional`. If deleting the file from S3 succeeds but deleting the record from the database fails, the transaction will roll back, leaving the database record intact. This creates a recoverable state (an orphaned file in S3), which is preferable to an inconsistent database. A separate cleanup job could handle orphaned files later if necessary.
