-- V11__seed_education_and_experience.sql
-- Seed education and work experience for the 100 users created in V8.
-- Uses the same deterministic UUID generation logic.

DO $$
DECLARE
    -- Fixed UUID namespace for v5 generation (DNS namespace)
    v_ns_uuid CONSTANT UUID := '6ba7b810-9dad-11d1-80b4-00c04fd430c8';

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

    v_institutions TEXT[] := ARRAY['RMIT University', 'MIT', 'Stanford University', 'Harvard University', 'Oxford University', 'National University of Singapore', 'University of Tokyo', 'Ho Chi Minh City University of Technology'];
    v_fields TEXT[] := ARRAY['Computer Science', 'Software Engineering', 'Information Technology', 'Data Science', 'Business Administration', 'Marketing', 'Electrical Engineering'];
    v_degrees TEXT[] := ARRAY['Bachelor of Science', 'Master of Science', 'Doctor of Philosophy', 'Bachelor of Engineering', 'Associate Degree'];
    v_levels TEXT[] := ARRAY['BACHELOR', 'MASTER', 'DOCTORATE', 'ASSOCIATE'];

    v_companies TEXT[] := ARRAY['Google', 'Microsoft', 'Amazon', 'Meta', 'Apple', 'Netflix', 'Tesla', 'FPT Software', 'VNG Corporation', 'Grab'];
    v_titles TEXT[] := ARRAY['Software Engineer', 'Senior Software Engineer', 'DevOps Engineer', 'Product Manager', 'Data Scientist', 'Frontend Developer', 'Backend Developer'];
    v_emp_types TEXT[] := ARRAY['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP'];

    v_country_count INT;
BEGIN
    SELECT array_agg(id) INTO v_country_ids FROM countries WHERE is_active = TRUE;
    v_country_count := COALESCE(array_length(v_country_ids, 1), 0);

    FOR v_i IN 1..100 LOOP
        v_fn := v_fnames[(v_i % array_length(v_fnames, 1)) + 1];
        v_ln := v_lnames[(v_i % array_length(v_lnames, 1)) + 1];
        v_email := lower(v_fn || '.' || v_ln || '.' || v_i || '@example.com');
        v_uid := uuid_generate_v5(v_ns_uuid, v_email);

        -- Check if user exists before seeding (safety)
        IF EXISTS (SELECT 1 FROM users WHERE id = v_uid) THEN
            
            -- Seed 1 Education record
            INSERT INTO user_education (
                id, user_id, institution, education_level, field_of_study, degree, gpa, start_at, end_at, is_active, created_at, updated_at
            ) VALUES (
                gen_random_uuid(),
                v_uid,
                v_institutions[(v_i % array_length(v_institutions, 1)) + 1],
                v_levels[(v_i % array_length(v_levels, 1)) + 1],
                v_fields[(v_i % array_length(v_fields, 1)) + 1],
                v_degrees[(v_i % array_length(v_degrees, 1)) + 1],
                (70 + (v_i % 30))::NUMERIC(5,2), -- GPA between 70 and 100
                '2018-09-01',
                '2022-06-01',
                TRUE,
                now(),
                now()
            ) ON CONFLICT DO NOTHING;

            -- Seed 1-2 Work Experience records
            -- Current job
            INSERT INTO user_work_experience (
                id, user_id, job_title, company_name, employment_type, country_id, start_at, end_at, is_current, description, is_active, created_at, updated_at
            ) VALUES (
                gen_random_uuid(),
                v_uid,
                v_titles[(v_i % array_length(v_titles, 1)) + 1],
                v_companies[(v_i % array_length(v_companies, 1)) + 1],
                v_emp_types[(v_i % array_length(v_emp_types, 1)) + 1],
                CASE WHEN v_country_count > 0 THEN v_country_ids[(v_i % v_country_count) + 1] ELSE NULL END,
                '2022-07-01',
                NULL,
                TRUE,
                'Developing high-scale applications and collaborating with cross-functional teams.',
                TRUE,
                now(),
                now()
            ) ON CONFLICT DO NOTHING;

            -- Previous job (50% chance)
            IF v_i % 2 = 0 THEN
                INSERT INTO user_work_experience (
                    id, user_id, job_title, company_name, employment_type, country_id, start_at, end_at, is_current, description, is_active, created_at, updated_at
                ) VALUES (
                    gen_random_uuid(),
                    v_uid,
                    'Junior ' || v_titles[((v_i+1) % array_length(v_titles, 1)) + 1],
                    v_companies[((v_i+1) % array_length(v_companies, 1)) + 1],
                    'INTERNSHIP',
                    CASE WHEN v_country_count > 0 THEN v_country_ids[((v_i+1) % v_country_count) + 1] ELSE NULL END,
                    '2021-06-01',
                    '2021-12-31',
                    FALSE,
                    'Gained initial experience in software development and agile methodologies.',
                    TRUE,
                    now(),
                    now()
                ) ON CONFLICT DO NOTHING;
            END IF;

        END IF;
    END LOOP;
END $$;
