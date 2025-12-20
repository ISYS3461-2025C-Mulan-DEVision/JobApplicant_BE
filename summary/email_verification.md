# Branch Accomplishments: Email Activation Flow

This branch implements and validates the end-to-end email activation flow for user accounts, including resend capability and activation token lifecycle management. It also adds necessary gateway routing, configuration, and a polished activation email template.

## What’s Implemented

### 1) Registration and Activation
- `POST /api/v1/auth/register`
  - Creates an inactive, unverified auth credential.
  - Generates a 24-hour activation token.
  - Sends an activation email to the registered address.

- `GET /api/v1/auth/activate?token=<token>`
  - Validates the token and checks expiry.
  - Activates the account (sets `isActive = true` and `emailVerified = true`).
  - Deletes the token after successful activation.
  - Returns access and refresh tokens for immediate login.
  - Emits a Kafka `UserRegisteredEvent` to trigger downstream user-profile creation.

- `POST /api/v1/auth/login`
  - Requires account to be active; blocks login until email is verified.
  - Returns JWT access and refresh tokens upon success.

### 2) Resend Activation Email
- `POST /api/v1/auth/resend-activation`
  - Accepts `email` as a form parameter.
  - If the account is inactive, issues a fresh 24-hour activation token and sends the email.
  - If the account is already active, it no-ops and returns success messaging.

### 3) Activation Token Management
- Tokens have a 24-hour lifetime (set on generation).
- Used tokens are deleted on successful activation.
- Scheduled cleanup job runs hourly to delete expired tokens:
  - Keeps the `verification_tokens` table lean.
  - Prevents accumulation of unusable tokens.

### 4) API Gateway Routing and Security
- Public endpoints via gateway:
  - `/api/v1/auth/register`
  - `/api/v1/auth/login`
  - `/api/v1/auth/refresh`
  - `/api/v1/auth/health`
  - `/api/v1/auth/activate`
  - `/api/v1/auth/resend-activation`
- Other service routes requiring authentication remain protected and use the JWT filter.

### 5) Email Template (Plain Text)
- Polished content with:
  - Friendly greeting.
  - Clear activation link.
  - Instruction to copy/paste link if clicking fails.
  - Explicit note that the link expires in 24 hours.
- Kept simple and reliable (plain text link instead of HTML button).

## Configuration Summary

- Auth Service (`application.properties`)
  - JWT settings:
    - `jwt.secret` (Base64-encoded ≥256-bit key)
    - `jwt.access-token-expiration`
    - `jwt.refresh-token-expiration`
  - Mail settings:
    - `spring.mail.host`, `spring.mail.port`, `spring.mail.username`, `spring.mail.password`
    - TLS/SMTP auth flags
  - Activation URL:
    - `app.url.activation` (e.g., `http://localhost:8080` for local dev via gateway)
  - Scheduling:
    - `spring.task.scheduling.enabled=true` (enables scheduled token cleanup)
  - DB, Flyway, Kafka, Eureka, Swagger documented and parameterized.

- API Gateway (`application.properties`)
  - JWT secret (same as auth-service).
  - Discovery-based routing for downstream services.
  - CORS configuration for common local frontend origins.
  - Actuator endpoints enabled for health and gateway info.

- Docker Compose
  - Environment variables are wired for both services:
    - `JWT_SECRET`, `APP_URL_ACTIVATION`, SMTP variables.
  - Supports local dev out of the box with Postgres, Eureka, Kafka, and Gateway.

## Testing Performed

- Registration via gateway:
  - Success response returned.
  - Auth-service logs show token creation and activation email sent.

- Activation via gateway:
  - Activation link returns 200 with tokens.
  - Credential marked active and emailVerified; token deleted.

- Login via gateway:
  - Blocked before activation; allowed after activation.

- Resend activation via gateway:
  - Returns 200.
  - Skips sending if account already active; otherwise generates a new token and sends email.

- Token cleanup:
  - Scheduled job runs hourly; logs confirm execution and completion.

## Notes

- The earlier gateway 500 error was due to transient container startup timing (connect-refused). With services healthy, routing works correctly. If desired, gateway resilience (retry/circuit breaker) can be added as a follow-up.

## Next Optional Enhancements

- Add a circuit breaker/retry policy to gateway for auth-service routes (smoother startup resilience).
- Provide an HTML email version with a styled activation button.
- Add a “Resend activation” endpoint rate limit to prevent abuse.
- Expand the cleanup job to also remove stale tokens by credential if multiple resends are frequently issued.

## Conclusion

This branch completes:
- End-to-end email activation flow.
- Resend activation capability.
- Scheduled activation token cleanup.
- Public routing for necessary auth endpoints through the gateway.
- Robust configurations and a user-friendly email template.

It’s ready to merge. 
