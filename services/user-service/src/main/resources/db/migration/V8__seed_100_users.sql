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
-- SHARD AWARE:
--  - This script checks the current database name and the user's assigned country.
--  - It only inserts the user if the country belongs to the current shard.
--  - This prevents duplicate users across shards and ensures data consistency.

-- Ensure required extensions are available
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

DO $$
DECLARE
    -- Fixed UUID namespace for v5 generation (DNS namespace)
    v_ns_uuid CONSTANT UUID := '6ba7b810-9dad-11d1-80b4-00c04fd430c8';
BEGIN
    -- Ensure a baseline set of skills exist (no duplicates)
    -- Using deterministic IDs for skills to ensure consistency across shards
    INSERT INTO skills (id, name, normalized_name, usage_count, is_active, created_at, updated_at)
    VALUES
        (uuid_generate_v5(v_ns_uuid, 'aws'), 'AWS', 'aws', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'docker'), 'Docker', 'docker', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'kubernetes'), 'Kubernetes', 'kubernetes', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'postgresql'), 'PostgreSQL', 'postgresql', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'typescript'), 'TypeScript', 'typescript', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'python'), 'Python', 'python', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'go'), 'Go', 'go', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'node.js'), 'Node.js', 'node.js', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'angular'), 'Angular', 'angular', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'vue'), 'Vue', 'vue', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'c#'), 'C#', 'c#', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, '.net'), '.NET', '.net', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'ruby'), 'Ruby', 'ruby', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'rails'), 'Rails', 'rails', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'php'), 'PHP', 'php', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'laravel'), 'Laravel', 'laravel', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'swift'), 'Swift', 'swift', 0, TRUE, now(), now()),
        (uuid_generate_v5(v_ns_uuid, 'kotlin'), 'Kotlin', 'kotlin', 0, TRUE, now(), now())
    ON CONFLICT (name) DO NOTHING;
END $$;

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
    v_chosen_abbr TEXT;
    v_target_db TEXT;
    v_current_db TEXT;
    
    v_has_address BOOLEAN;
    v_has_city    BOOLEAN;
BEGIN
    -- Collect available active countries and skills
    -- ORDER BY abbreviation ensures deterministic ordering for modulo selection
    SELECT array_agg(id ORDER BY abbreviation) INTO v_country_ids FROM countries WHERE is_active = TRUE;
    SELECT array_agg(id) INTO v_skill_ids FROM skills WHERE is_active = TRUE;

    v_country_count := COALESCE(array_length(v_country_ids, 1), 0);
    v_skill_count := COALESCE(array_length(v_skill_ids, 1), 0);
    v_current_db := current_database();

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

        -- Deterministic user id from email
        v_uid := uuid_generate_v5(v_ns_uuid, v_email);

        -- Choose a country if available
        v_chosen_country := NULL;
        v_target_db := 'user_shard_others_db'; -- Default

        IF v_country_count > 0 THEN
            v_chosen_country := v_country_ids[(v_i % v_country_count) + 1];
            
            -- Get abbreviation to determine target shard
            SELECT abbreviation INTO v_chosen_abbr FROM countries WHERE id = v_chosen_country;
            
            -- Shard Mapping Logic (Matches ShardingProperties.java)
            CASE 
                WHEN v_chosen_abbr = 'VN' THEN v_target_db := 'user_shard_vn_db';
                WHEN v_chosen_abbr = 'SG' THEN v_target_db := 'user_shard_sg_db';
                WHEN v_chosen_abbr IN ('AU', 'NZ') THEN v_target_db := 'user_shard_oceania_db';
                WHEN v_chosen_abbr IN ('JP', 'KR', 'CN') THEN v_target_db := 'user_shard_east_asia_db';
                WHEN v_chosen_abbr IN ('US', 'CA') THEN v_target_db := 'user_shard_north_america_db';
                WHEN v_chosen_abbr IN ('GB', 'FR', 'DE', 'NL') THEN v_target_db := 'user_shard_europe_db';
                ELSE v_target_db := 'user_shard_others_db';
            END CASE;
        END IF;

        -- ONLY INSERT if the current database matches the target database for this user
        IF v_current_db = v_target_db THEN
            
            RAISE NOTICE 'Seeding user % into % (Country: %)', v_email, v_current_db, v_chosen_abbr;

            -- Pick an objective summary sample
            v_obj := v_objective_samples[(v_i % array_length(v_objective_samples,1)) + 1] || ' Skilled in ' ||
                   (SELECT name FROM skills WHERE is_active = TRUE ORDER BY random() LIMIT 1) || '.';

            -- Insert user (idempotent via email)
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
            
        END IF; -- End DB check

    END LOOP;
END $$;
