-- =============================================
-- Auth Service - V4: Seed ~100 Credentials (Fixed variable ambiguity)
-- =============================================
-- This migration seeds ~100 credentials to match the users seeded in
-- user-service V8__seed_100_users.sql.
--
-- Fixes:
-- - Avoid ambiguous references by using PL/pgSQL variables prefixed with v_*
-- - Qualify columns in ON CONFLICT DO UPDATE using EXCLUDED.*
--
-- Key points:
-- - user_id is deterministically derived from the same email using uuid_generate_v5
--   with the DNS namespace UUID, ensuring it matches the user-service seed.
-- - Password hash reused from existing test accounts (Password123!):
--     $2a$04$4gaE9mrolh0DpHgFkjEzIeO8kfvu5WZcaGqVqR5XTY0EcQw7gjmDu
-- - Mark email_verified = TRUE to bypass verification for seeded accounts.
-- - Idempotent via ON CONFLICT (email).
--
-- Prerequisites:
-- - V1 (credentials table) applied
-- - uuid-ossp extension available (for uuid_generate_v5)

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
DECLARE
    -- DNS namespace UUID for uuid_generate_v5 (deterministic)
    v_ns_uuid CONSTANT UUID := '6ba7b810-9dad-11d1-80b4-00c04fd430c8';

    v_i INT;
    v_email TEXT;
    v_uid UUID;

    -- First/Last names arrays must match user-service V8 migration to generate the same emails
    v_fnames TEXT[] := ARRAY[
        'Alice','Bob','Carol','David','Eve','Frank','Grace','Heidi','Ivan','Judy',
        'Ken','Laura','Mallory','Niaj','Olivia','Peggy','Quentin','Ruth','Sybil','Trent',
        'Uma','Victor','Wendy','Xavier','Yvonne','Zack','Noah','Mia','Liam','Emma',
        'Ava','Sophia','Isabella','Mason','Logan','Lucas','Ethan','James','Amelia','Harper'
    ];
    v_lnames TEXT[] := ARRAY[
        'Smith','Johnson','Williams','Brown','Jones','Miller','Davis','Garcia','Rodriguez','Wilson',
        'Martinez','Anderson','Taylor','Thomas','Hernandez','Moore','Martin','Jackson','Thompson','White',
        'Lopez','Lee','Gonzalez','Harris','Clark','Lewis','Walker','Hall','Allen','Young',
        'King','Wright','Scott','Green','Baker','Adams','Nelson','Hill','Ramirez','Campbell'
    ];

    v_passhash CONSTANT TEXT := '$2a$04$4gaE9mrolh0DpHgFkjEzIeO8kfvu5WZcaGqVqR5XTY0EcQw7gjmDu';
BEGIN
    -- Generate 100 credentials with deterministic user_id from email
    FOR v_i IN 1..100 LOOP
        -- Construct the same email format used in user-service V8
        v_email := lower(
            v_fnames[(v_i % array_length(v_fnames,1)) + 1]
            || '.' ||
            v_lnames[(v_i % array_length(v_lnames,1)) + 1]
            || '.' || v_i || '@example.com'
        );

        -- Deterministic user_id derived from email
        v_uid := uuid_generate_v5(v_ns_uuid, v_email);

        -- Insert credentials (idempotent on unique email)
        INSERT INTO credentials (
            id,
            user_id,
            email,
            password_hash,
            role,
            auth_provider,
            email_verified,
            is_active,
            created_at,
            updated_at
        )
        VALUES (
            v_uid,              -- Set credential id equal to user_id for consistency
            v_uid,
            v_email,
            v_passhash,
            'FREE',
            'LOCAL',
            TRUE,               -- Verified to allow immediate login
            TRUE,               -- Active credential
            now(),
            now()
        )
        ON CONFLICT (email) DO UPDATE
        SET
            user_id        = EXCLUDED.user_id,
            password_hash  = EXCLUDED.password_hash,
            role           = EXCLUDED.role,
            auth_provider  = EXCLUDED.auth_provider,
            email_verified = EXCLUDED.email_verified,
            is_active      = EXCLUDED.is_active,
            updated_at     = now();
    END LOOP;
END $$;
