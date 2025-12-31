-- V8__seed_100_users.sql
-- Seed ~100 active users with varied skills and countries.
-- Idempotent:
--  - Users are keyed by unique email -> ON CONFLICT(email) DO NOTHING
--  - User skills are keyed by (user_id, skill_id) -> ON CONFLICT DO NOTHING
--
-- Deterministic user IDs:
--  - We derive user.id from email using uuid_generate_v5 with a fixed namespace.
--  - This allows the auth-service to seed matching credentials using the same rule.
--
-- NOTE for auth-service:
--  - Create a matching migration that inserts credentials for these emails.
--  - Use the same user_id = uuid_generate_v5('6ba7b810-9dad-11d1-80b4-00c04fd430c8', email)
--  - Reuse the known test hash from V3__seed_auth_credentials_for_testing.sql:
--      $2a$04$4gaE9mrolh0DpHgFkjEzIeO8kfvu5WZcaGqVqR5XTY0EcQw7gjmDu
--    to avoid login issues (password: "Password123!")
--
-- Safe to run multiple times.

-- Ensure required extensions are available
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Ensure a baseline set of skills exist (no duplicates)
INSERT INTO skills (id, name, normalized_name, usage_count, is_active, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'AWS', 'aws', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'Docker', 'docker', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'Kubernetes', 'kubernetes', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'PostgreSQL', 'postgresql', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'TypeScript', 'typescript', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'Python', 'python', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'Go', 'go', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'Node.js', 'node.js', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'Angular', 'angular', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'Vue', 'vue', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'C#', 'c#', 0, TRUE, now(), now()),
    (gen_random_uuid(), '.NET', '.net', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'Ruby', 'ruby', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'Rails', 'rails', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'PHP', 'php', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'Laravel', 'laravel', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'Swift', 'swift', 0, TRUE, now(), now()),
    (gen_random_uuid(), 'Kotlin', 'kotlin', 0, TRUE, now(), now())
ON CONFLICT (name) DO NOTHING;

DO $$
DECLARE
    -- Fixed UUID namespace for v5 generation (DNS namespace)
    v_ns_uuid CONSTANT UUID := '6ba7b810-9dad-11d1-80b4-00c04fd430c8';

    v_skill_ids UUID[];
    v_country_ids UUID[];

    v_i INT;
    v_uid UUID;
    v_email TEXT;

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

    v_fn TEXT;
    v_ln TEXT;

    v_country_count INT;
    v_skill_count INT;

    v_objective_samples TEXT[] := ARRAY[
        'Software engineer with experience in microservices and cloud platforms.',
        'Backend developer passionate about distributed systems and data pipelines.',
        'Frontend engineer focused on UX, accessibility, and performance.',
        'Full-stack developer experienced in modern web frameworks and CI/CD.',
        'Platform engineer with a background in Kubernetes and observability.',
        'Data-oriented developer with interests in ETL and analytics.',
        'API engineer delivering robust, secure RESTful services.',
        'DevOps-minded engineer automating deployments and infrastructure.'
    ];
    v_obj TEXT;

    v_s_count INT;
    v_s_idx INT;
    v_s_id UUID;

    v_chosen_country UUID;
    v_has_address BOOLEAN;
    v_has_city    BOOLEAN;
BEGIN
    -- Collect available active countries and skills
    SELECT array_agg(id) INTO v_country_ids FROM countries WHERE is_active = TRUE;
    SELECT array_agg(id) INTO v_skill_ids FROM skills WHERE is_active = TRUE;

    v_country_count := COALESCE(array_length(v_country_ids, 1), 0);
    v_skill_count := COALESCE(array_length(v_skill_ids, 1), 0);

    -- Check column existence once to avoid nested DO blocks later
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'address'
    ) INTO v_has_address;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'city'
    ) INTO v_has_city;

    -- Safety: ensure we have at least some skills to assign
    IF v_skill_count = 0 THEN
        RAISE NOTICE 'No active skills found; seeding aborted.';
        RETURN;
    END IF;

    FOR v_i IN 1..100 LOOP
        v_fn := v_fnames[(v_i % array_length(v_fnames,1)) + 1];
        v_ln := v_lnames[(v_i % array_length(v_lnames,1)) + 1];

        v_email := lower(v_fn || '.' || v_ln || '.' || v_i || '@example.com');

        -- Deterministic user id from email (stays consistent across runs and usable by auth-service)
        v_uid := uuid_generate_v5(v_ns_uuid, v_email);

        -- Choose a country if available
        IF v_country_count > 0 THEN
            v_chosen_country := v_country_ids[(v_i % v_country_count) + 1];
        ELSE
            v_chosen_country := NULL;
        END IF;

        -- Pick an objective summary sample and enrich it with a random skill name for FTS diversity
        v_obj := v_objective_samples[(v_i % array_length(v_objective_samples,1)) + 1] || ' Skilled in ' ||
               (SELECT name FROM skills WHERE is_active = TRUE ORDER BY random() LIMIT 1) || '.';

        -- Insert user (idempotent via email)
        -- Address/City inserted only if columns exist to avoid migration order issues
        IF v_has_address AND v_has_city THEN
            INSERT INTO users (
                id, email, first_name, last_name, country_id, address, city, objective_summary,
                is_premium, is_active, created_at, updated_at
            )
            VALUES (
                v_uid,
                v_email,
                v_fn,
                v_ln,
                v_chosen_country,
                'Address ' || v_i || ' ' || v_ln,
                (SELECT name FROM countries WHERE id = v_chosen_country),
                v_obj,
                CASE WHEN v_i % 10 = 0 THEN TRUE ELSE FALSE END,
                TRUE,
                now(),
                now()
            )
            ON CONFLICT (email) DO NOTHING;
        ELSIF v_has_address AND NOT v_has_city THEN
            INSERT INTO users (
                id, email, first_name, last_name, country_id, address, objective_summary,
                is_premium, is_active, created_at, updated_at
            )
            VALUES (
                v_uid,
                v_email,
                v_fn,
                v_ln,
                v_chosen_country,
                'Address ' || v_i || ' ' || v_ln,
                v_obj,
                CASE WHEN v_i % 10 = 0 THEN TRUE ELSE FALSE END,
                TRUE,
                now(),
                now()
            )
            ON CONFLICT (email) DO NOTHING;
        ELSIF NOT v_has_address AND v_has_city THEN
            INSERT INTO users (
                id, email, first_name, last_name, country_id, city, objective_summary,
                is_premium, is_active, created_at, updated_at
            )
            VALUES (
                v_uid,
                v_email,
                v_fn,
                v_ln,
                v_chosen_country,
                (SELECT name FROM countries WHERE id = v_chosen_country),
                v_obj,
                CASE WHEN v_i % 10 = 0 THEN TRUE ELSE FALSE END,
                TRUE,
                now(),
                now()
            )
            ON CONFLICT (email) DO NOTHING;
        ELSE
            INSERT INTO users (
                id, email, first_name, last_name, country_id, objective_summary,
                is_premium, is_active, created_at, updated_at
            )
            VALUES (
                v_uid,
                v_email,
                v_fn,
                v_ln,
                v_chosen_country,
                v_obj,
                CASE WHEN v_i % 10 = 0 THEN TRUE ELSE FALSE END,
                TRUE,
                now(),
                now()
            )
            ON CONFLICT (email) DO NOTHING;
        END IF;

        -- Attach 2..4 skills per user (idempotent via (user_id, skill_id))
        v_s_count := 2 + (v_i % 3); -- 2..4
        FOR v_s_idx IN 1..v_s_count LOOP
            v_s_id := v_skill_ids[((v_i * v_s_idx) % v_skill_count) + 1];

            INSERT INTO user_skills (id, user_id, skill_id, is_active, created_at, updated_at)
            VALUES (gen_random_uuid(), v_uid, v_s_id, TRUE, now(), now())
            ON CONFLICT (user_id, skill_id) DO NOTHING;
        END LOOP;
    END LOOP;
END $$;
