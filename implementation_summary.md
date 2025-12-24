# Implementation Summary

This document summarizes the recent features and enhancements implemented across the services.

## 1. User Avatar Upload

A new feature allowing users to upload a profile picture has been implemented in the `user-service`.

- **Endpoint:** `POST /api/v1/users/{id}/avatar`
- **Functionality:**
    - Accepts image file uploads (`image/jpeg`, `image/png`, `image/gif`).
    - Automatically resizes uploaded images to a standard 256x256 pixel format for consistency and performance.
    - Stores the processed image in the configured S3-compatible object storage (SeaweedFS).
    - Updates the user's profile in the database with the new `avatarUrl`.
    - The user's `avatarUrl` is now included in API responses for user profiles.

## 2. Enhanced Exception Handling

The error handling framework in the `common` module has been improved to provide more detailed diagnostics.

- **Global Error Response:** All API error responses now include a new field, `exceptionType`.
- **Details:** This field contains the name of the specific exception class that was triggered on the backend (e.g., `ForbiddenException`, `MethodArgumentNotValidException`), making it easier to identify the root cause of an error during development and testing.

## 3. Stricter Validation Rules

Input validation has been strengthened in both the `auth-service` and `user-service` to improve data integrity and security, as per the SRS requirements.

- **Password Complexity (`auth-service`):**
    - New user registrations now enforce a strong password policy.
    - **Rule:** Passwords must be at least 8 characters long and contain at least one uppercase letter, one number, and one special character (from the set: `$#@!`).

- **Phone Number Format (`user-service`):**
    - The `phone` field for user profiles is now validated.
    - **Rule:** Phone numbers must follow an international format, starting with a `+` followed by up to 12 digits (e.g., `+84123456789`).

## 4. Strict Authorization Checks

Security has been significantly hardened across the `user-service` by implementing strict resource ownership checks.

- **Protected Endpoints:** All endpoints related to viewing or modifying a specific user's personal data are now secured. This includes:
    - `GET /api/v1/users/{id}/profile`
    - `PUT /api/v1/users/{id}`
    - `POST /api/v1/users/{id}/avatar`
    - `DELETE /api/v1/users/{id}`
    - All endpoints under `/api/v1/users/{userId}/education/`
    - All endpoints under `/api/v1/users/{userId}/work-experience/`
- **Mechanism:** These endpoints now require the `X-User-Id` header (sent by the API Gateway). The service verifies that this ID matches the user ID in the URL.
- **Outcome:** If a user attempts to access or modify another user's data, the API will return a `403 Forbidden` error, preventing unauthorized actions.

## 5. Event-Driven Profile Updates (Kafka)

In alignment with a strictly event-driven architecture and SRS requirement `3.3.1`, the `user-service` now publishes events to a Kafka topic when significant user profile data changes.

- **New Kafka Topic:** A new topic, `user-profile-updated`, has been established for these events.
- **New Event DTO:** A `UserProfileUpdatedEvent` object is now defined in the `common` module. This event contains the `userId`, the type of update (`SKILLS` or `COUNTRY`), and the relevant updated data (list of skill IDs or the new country ID).
- **Producer Implementation:** A Kafka producer has been configured and implemented within the `user-service` to send these events.
- **Event Triggers:** An event is now published whenever:
    1.  A user's skills are modified (added or removed) via the `SkillController`.
    2.  A user's country is changed via the `UserController`.
- **Architectural Benefit:** This enhancement decouples the `user-service` from other services. Downstream consumers (like a notification or job-matching service) can now react to profile changes in real-time without needing to poll the `user-service` for updates, leading to a more scalable and responsive system.
