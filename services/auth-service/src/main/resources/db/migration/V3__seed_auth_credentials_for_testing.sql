-- V3__seed_auth_credentials_for_testing.sql
-- This script seeds the credentials table with test users.
-- It uses hardcoded UUIDs to ensure consistency with the user-service seed data.

-- Seed credentials for all users created in user-service
-- All users will have password: Password123!
-- Bcrypt hash: $2a$04$4gaE9mrolh0DpHgFkjEzIeO8kfvu5WZcaGqVqR5XTY0EcQw7gjmDu

-- Programmatically seed credentials for all users (1-50) to match user-service
DO $$
DECLARE
    uuids UUID[] := ARRAY[
        'd4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4',
        'e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5',
        'f6f6f6f6-f6f6-f6f6-f6f6-f6f6f6f6f6f6',
        'a7a7a7a7-a7a7-a7a7-a7a7-a7a7a7a7a7a7',
        'b8b8b8b8-b8b8-b8b8-b8b8-b8b8b8b8b8b8',
        'c9c9c9c9-c9c9-c9c9-c9c9-c9c9c9c9c9c9',
        'd0d0d0d0-d0d0-d0d0-d0d0-d0d0d0d0d0d0',
        'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1',
        'f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2',
        'a3a3a3a3-a3a3-a3a3-a3a3-a3a3a3a3a3a3',
        'b4b4b4b4-b4b4-b4b4-b4b4-b4b4b4b4b4b4',
        'c5c5c5c5-c5c5-c5c5-c5c5-c5c5c5c5c5c5',
        'd6d6d6d6-d6d6-d6d6-d6d6-d6d6d6d6d6d6',
        'e7e7e7e7-e7e7-e7e7-e7e7-e7e7e7e7e7e7',
        'f8f8f8f8-f8f8-f8f8-f8f8-f8f8f8f8f8f8',
        'a9a9a9a9-a9a9-a9a9-a9a9-a9a9a9a9a9a9',
        'b0b0b0b0-b0b0-b0b0-b0b0-b0b0b0b0b0b0',
        'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1',
        'd2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2',
        'e3e3e3e3-e3e3-e3e3-e3e3-e3e3e3e3e3e3',
        'f4f4f4f4-f4f4-f4f4-f4f4-f4f4f4f4f4f4',
        'a5a5a5a5-a5a5-a5a5-a5a5-a5a5a5a5a5a5',
        'b6b6b6b6-b6b6-b6b6-b6b6-b6b6b6b6b6b6',
        'c7c7c7c7-c7c7-c7c7-c7c7-c7c7c7c7c7c7',
        'd8d8d8d8-d8d8-d8d8-d8d8-d8d8d8d8d8d8',
        'e9e9e9e9-e9e9-e9e9-e9e9-e9e9e9e9e9e9',
        'f0f0f0f0-f0f0-f0f0-f0f0-f0f0f0f0f0f0',
        'a1b2c3d4-e5f6-7890-abcd-ef0123456789',
        'b2c3d4e5-f6a1-8901-bcde-f0123456789a',
        'c3d4e5f6-a1b2-9012-cdef-0123456789ab',
        'd4e5f6a1-b2c3-0123-def0-123456789abc',
        'e5f6a1b2-c3d4-1234-ef01-23456789abcd',
        'f6a1b2c3-d4e5-2345-f012-3456789abcde',
        'a1b2c3d4-e5f6-3456-0123-456789abcdef',
        'b2c3d4e5-f6a1-4567-1234-56789abcdef0',
        'c3d4e5f6-a1b2-5678-2345-6789abcdef01',
        'd4e5f6a1-b2c3-6789-3456-789abcdef012',
        'e5f6a1b2-c3d4-789a-4567-89abcdef0123',
        'f6a1b2c3-d4e5-89ab-5678-9abcdef01234',
        'a1b2c3d4-e5f6-9abc-6789-abcdef012345',
        'b2c3d4e5-f6a1-abcd-789a-bcdef0123456',
        'c3d4e5f6-a1b2-bcde-89ab-cdef01234567',
        'd4e5f6a1-b2c3-cdef-9abc-def012345678',
        'e5f6a1b2-c3d4-def0-abcd-ef0123456789',
        'f6a1b2c3-d4e5-ef01-bcde-f0123456789a',
        'a1b2c3d4-e5f6-f012-cdef-0123456789ab',
        'b2c3d4e5-f6a1-0123-def0-123456789abc',
        'c3d4e5f6-a1b2-1234-ef01-23456789abcd',
        'd4e5f6a1-b2c3-2345-f012-3456789abcde',
        'e5f6a1b2-c3d4-3456-0123-456789abcdef',
        'f6a1b2c3-d4e5-4567-1234-56789abcdef0',
        'a1b2c3d4-e5f6-5678-2345-6789abcdef01',
        'b2c3d4e5-f6a1-6789-3456-789abcdef012'
    ];
    hash TEXT := '$2a$04$4gaE9mrolh0DpHgFkjEzIeO8kfvu5WZcaGqVqR5XTY0EcQw7gjmDu'; -- Bcrypt hash for Password123!
BEGIN
    -- First three users
    INSERT INTO credentials (id, user_id, email, password_hash, role, auth_provider, is_active, email_verified, created_at, updated_at)
    VALUES
        ('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'phan.nguyen@example.com', hash, 'FREE', 'LOCAL', TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        ('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'polar.bear@example.com', hash, 'FREE', 'LOCAL', TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        ('c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', 'alex.tiger@example.com', hash, 'FREE', 'LOCAL', TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (email) DO UPDATE SET id = EXCLUDED.id, user_id = EXCLUDED.user_id, password_hash = EXCLUDED.password_hash, updated_at = CURRENT_TIMESTAMP;

    -- Bulk users user1@example.com ... user50@example.com
    FOR i IN 1..50 LOOP
        INSERT INTO credentials (id, user_id, email, password_hash, role, auth_provider, is_active, email_verified, created_at, updated_at)
        VALUES (
            uuids[i], uuids[i], 'user' || i || '@example.com', hash, 'FREE', 'LOCAL', TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        )
        ON CONFLICT (email) DO UPDATE SET id = EXCLUDED.id, user_id = EXCLUDED.user_id, password_hash = EXCLUDED.password_hash, updated_at = CURRENT_TIMESTAMP;
    END LOOP;
END;
$$;