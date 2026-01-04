-- V12__seed_portfolio_items.sql
-- Seed sample portfolio items for the 100 users.

DO $$
DECLARE
    v_ns_uuid CONSTANT UUID := '6ba7b810-9dad-11d1-80b4-00c04fd430c8';
    v_i INT;
    v_uid UUID;
    v_email TEXT;
    
    v_fnames TEXT[] := ARRAY['Alice','Bob','Carol','David','Eve','Frank','Grace','Heidi','Ivan','Judy','Ken','Laura','Mallory','Niaj','Olivia','Peggy','Quentin','Ruth','Sybil','Trent','Uma','Victor','Wendy','Xavier','Yvonne','Zack','Noah','Mia','Liam','Emma','Ava','Sophia','Isabella','Mason','Logan','Lucas','Ethan','James','Amelia','Harper'];
    v_lnames TEXT[] := ARRAY['Smith','Johnson','Williams','Brown','Jones','Miller','Davis','Garcia','Rodriguez','Wilson','Martinez','Anderson','Taylor','Thomas','Hernandez','Moore','Martin','Jackson','Thompson','White','Lopez','Lee','Gonzalez','Harris','Clark','Lewis','Walker','Hall','Allen','Young','King','Wright','Scott','Green','Baker','Adams','Nelson','Hill','Ramirez','Campbell'];
    v_fn TEXT;
    v_ln TEXT;

    v_descriptions TEXT[] := ARRAY[
        'Personal project: E-commerce platform built with React and Spring Boot.',
        'Open source contribution: Fixed critical bug in a popular Java library.',
        'Data visualization: Dashboard for real-time monitoring of IoT devices.',
        'Machine Learning model: Predicting stock prices using LSTM.',
        'Mobile App: Fitness tracker with social sharing features.',
        'Architecture diagram: Microservices ecosystem for a fintech startup.',
        'Cloud infrastructure: Terraform scripts for multi-region AWS deployment.'
    ];
BEGIN
    FOR v_i IN 1..100 LOOP
        v_fn := v_fnames[(v_i % array_length(v_fnames, 1)) + 1];
        v_ln := v_lnames[(v_i % array_length(v_lnames, 1)) + 1];
        v_email := lower(v_fn || '.' || v_ln || '.' || v_i || '@example.com');
        v_uid := uuid_generate_v5(v_ns_uuid, v_email);

        -- Check if user exists
        IF EXISTS (SELECT 1 FROM users WHERE id = v_uid) THEN
            -- Seed 1-2 portfolio items per user
            INSERT INTO user_portfolio_items (
                id, user_id, file_url, description, media_type, is_active, created_at, updated_at
            ) VALUES (
                gen_random_uuid(),
                v_uid,
                'https://picsum.photos/seed/' || v_i || '/800/600',
                v_descriptions[(v_i % array_length(v_descriptions, 1)) + 1],
                'image/jpeg',
                TRUE,
                now(),
                now()
            ) ON CONFLICT DO NOTHING;

            IF v_i % 3 = 0 THEN
                INSERT INTO user_portfolio_items (
                    id, user_id, file_url, description, media_type, is_active, created_at, updated_at
                ) VALUES (
                    gen_random_uuid(),
                    v_uid,
                    'https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4',
                    'Demo video of ' || v_fn || '''s latest project.',
                    'video/mp4',
                    TRUE,
                    now(),
                    now()
                ) ON CONFLICT DO NOTHING;
            END IF;
        END IF;
    END LOOP;
END $$;
