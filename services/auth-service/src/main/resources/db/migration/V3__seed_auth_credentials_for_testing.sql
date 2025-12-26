-- V3__seed_auth_credentials_for_testing.sql
-- This script seeds the credentials table with test users.
-- It is designed to be run after the user-service has seeded the corresponding user profiles.

INSERT INTO credentials (id, user_id, email, password_hash, role, auth_provider, is_active, email_verified, created_at, updated_at) VALUES
(
    gen_random_uuid(),
    'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', -- Corresponds to phan.nguyen@example.com in user-service
    'phan.nguyen@example.com',
    '$2a$04$4gaE9mrolh0DpHgFkjEzIeO8kfvu5WZcaGqVqR5XTY0EcQw7gjmDu', -- Bcrypt for "Password123!"
    'FREE',
    'LOCAL',
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    gen_random_uuid(),
    'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', -- Corresponds to polar.bear@example.com in user-service
    'polar.bear@example.com',
    '$2a$04$4gaE9mrolh0DpHgFkjEzIeO8kfvu5WZcaGqVqR5XTY0EcQw7gjmDu', -- Bcrypt for "Password123!"
    'FREE',
    'LOCAL',
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;
