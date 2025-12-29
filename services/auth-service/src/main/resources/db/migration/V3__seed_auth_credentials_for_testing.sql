-- V3__seed_auth_credentials_for_testing.sql
-- This script seeds the credentials table with test users.
-- It uses hardcoded UUIDs to ensure consistency with the user-service seed data.

INSERT INTO credentials (id, user_id, email, password_hash, role, auth_provider, is_active, email_verified, created_at, updated_at) VALUES
(
    'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', -- Consistent UUID for phan.nguyen@example.com
    'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1',
    'phan.nguyen@example.com',
    '$2a$04$4gaE9mrolh0DpHgFkjEzIeO8kfvu5WZcaGqVqR5XTY0EcQw7gjmDu', -- Correct Bcrypt hash for "Password123!" as per previous successful login
    'FREE',
    'LOCAL',
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', -- Consistent UUID for polar.bear@example.com
    'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2',
    'polar.bear@example.com',
    '$2a$04$4gaE9mrolh0DpHgFkjEzIeO8kfvu5WZcaGqVqR5XTY0EcQw7gjmDu', -- Correct Bcrypt hash for "Password123!"
    'FREE',
    'LOCAL',
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO UPDATE 
SET 
    id = EXCLUDED.id,
    user_id = EXCLUDED.user_id,
    password_hash = EXCLUDED.password_hash,
    updated_at = CURRENT_TIMESTAMP;