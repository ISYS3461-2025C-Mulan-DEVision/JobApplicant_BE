-- V6__seed_users_for_testing.sql

-- Insert Skills if they don't exist
INSERT INTO skills (id, name, normalized_name, usage_count, is_active, created_at, updated_at) VALUES
(gen_random_uuid(), 'Java', 'java', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Spring Boot', 'spring boot', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Kafka', 'kafka', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'React', 'react', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'JavaScript', 'javascript', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'HTML', 'html', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'CSS', 'css', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'TypeScript', 'typescript', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Node.js', 'node.js', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Express', 'express', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Python', 'python', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Django', 'django', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Flask', 'flask', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'C++', 'c++', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'C#', 'c#', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Go', 'go', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Ruby', 'ruby', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Rails', 'rails', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'PHP', 'php', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Laravel', 'laravel', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'MySQL', 'mysql', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'PostgreSQL', 'postgresql', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'MongoDB', 'mongodb', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Redis', 'redis', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Ensure users are not inserted multiple times
DO $$
DECLARE
    vn_country_id UUID;
    us_country_id UUID;
    java_skill_id UUID;
    springboot_skill_id UUID;
    kafka_skill_id UUID;
    react_skill_id UUID;
    javascript_skill_id UUID;
    html_skill_id UUID;
    nodejs_skill_id UUID;
    mongodb_skill_id UUID;

    -- Consistent UUIDs for testing. These must be globally unique.
    phan_nguyen_user_id UUID := 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1';
    polar_bear_user_id UUID := 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2';
    alex_tiger_user_id UUID := 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3';

    -- Additional user UUIDs for bulk seeding
    user_uuids UUID[] := ARRAY[
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

BEGIN
    SELECT id INTO vn_country_id FROM countries WHERE abbreviation = 'VN';
    SELECT id INTO us_country_id FROM countries WHERE abbreviation = 'US';

    SELECT id INTO java_skill_id FROM skills WHERE normalized_name = 'java';
    SELECT id INTO springboot_skill_id FROM skills WHERE normalized_name = 'spring boot';
    SELECT id INTO kafka_skill_id FROM skills WHERE normalized_name = 'kafka';
    SELECT id INTO react_skill_id FROM skills WHERE normalized_name = 'react';
    SELECT id INTO javascript_skill_id FROM skills WHERE normalized_name = 'javascript';
    SELECT id INTO html_skill_id FROM skills WHERE normalized_name = 'html';
    SELECT id INTO nodejs_skill_id FROM skills WHERE normalized_name = 'node.js';
    SELECT id INTO mongodb_skill_id FROM skills WHERE normalized_name = 'mongodb';

    -- Insert User 1 (Phan Nguyen)
    INSERT INTO users (id, email, first_name, last_name, country_id, objective_summary, is_premium, is_active, created_at, updated_at) VALUES
    (phan_nguyen_user_id, 'phan.nguyen@example.com', 'Phan', 'Nguyen', vn_country_id, 'Experienced software engineer with expertise in Java, Spring Boot, and Kafka.', FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (email) DO NOTHING;

    -- Insert User 2 (Polar Bear)
    INSERT INTO users (id, email, first_name, last_name, country_id, objective_summary, is_premium, is_active, created_at, updated_at) VALUES
    (polar_bear_user_id, 'polar.bear@example.com', 'Polar', 'Bear', us_country_id, 'Junior developer passionate about web technologies including React and JavaScript.', FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (email) DO NOTHING;

    -- Insert UserSkills for Phan Nguyen
    INSERT INTO user_skills (id, user_id, skill_id, is_active, created_at, updated_at) VALUES
    (gen_random_uuid(), phan_nguyen_user_id, java_skill_id, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), phan_nguyen_user_id, springboot_skill_id, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), phan_nguyen_user_id, kafka_skill_id, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (user_id, skill_id) DO NOTHING;

    -- Insert UserSkills for Polar Bear
    INSERT INTO user_skills (id, user_id, skill_id, is_active, created_at, updated_at) VALUES
    (gen_random_uuid(), polar_bear_user_id, react_skill_id, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), polar_bear_user_id, javascript_skill_id, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), polar_bear_user_id, html_skill_id, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (user_id, skill_id) DO NOTHING;

    -- Insert User 3 (Alex Tiger)
    INSERT INTO users (id, email, first_name, last_name, country_id, objective_summary, is_premium, is_active, created_at, updated_at) VALUES
    (alex_tiger_user_id, 'alex.tiger@example.com', 'Alex', 'Tiger', us_country_id, 'Full stack developer with experience in Node.js, React, and MongoDB.', FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (email) DO NOTHING;

    -- Insert UserSkills for Alex Tiger
    INSERT INTO user_skills (id, user_id, skill_id, is_active, created_at, updated_at) VALUES
    (gen_random_uuid(), alex_tiger_user_id, nodejs_skill_id, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), alex_tiger_user_id, react_skill_id, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), alex_tiger_user_id, mongodb_skill_id, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (user_id, skill_id) DO NOTHING;

    -- Seed 50 users
    FOR i IN 1..50 LOOP
        -- Generate user details
        -- Use round-robin for country and skills
        INSERT INTO users (id, email, first_name, last_name, country_id, objective_summary, is_premium, is_active, created_at, updated_at) VALUES
        (user_uuids[i],
            'user' || i || '@example.com',
            'User' || i,
            'Test',
            CASE WHEN MOD(i, 2) = 0 THEN vn_country_id ELSE us_country_id END,
            'Test user ' || i || ' skilled in ' || (
                SELECT name FROM (
                     VALUES ('Java'), ('Spring Boot'), ('Kafka'), ('React'), ('JavaScript'), ('HTML'),
                              ('CSS'), ('TypeScript'), ('Node.js'), ('Express'), ('Python'), ('Django'),
                              ('Flask'), ('C++'), ('C#'), ('Go'), ('Ruby'), ('Rails'), ('PHP'), ('Laravel'),
                              ('MySQL'), ('PostgreSQL'), ('MongoDB'), ('Redis')
                ) AS skill_names(name) LIMIT 1 OFFSET MOD(i, 24)
            ) || '.',
            FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (email) DO NOTHING;

        -- Assign 3 skills per user, round-robin from the first 24 skills
        FOR j IN 0..2 LOOP
            INSERT INTO user_skills (id, user_id, skill_id, is_active, created_at, updated_at)
            SELECT gen_random_uuid(), user_uuids[i], id, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            FROM skills
            WHERE normalized_name = (
                SELECT normalized_name FROM (
                    VALUES ('java'), ('spring boot'), ('kafka'), ('react'), ('javascript'), ('html'),
                           ('css'), ('typescript'), ('node.js'), ('express'), ('python'), ('django'),
                           ('flask'), ('c++'), ('c#'), ('go'), ('ruby'), ('rails'), ('php'), ('laravel'),
                           ('mysql'), ('postgresql'), ('mongodb'), ('redis')
                ) AS skill_names(normalized_name)
                LIMIT 1 OFFSET MOD(i + j, 24)
            )
            ON CONFLICT (user_id, skill_id) DO NOTHING;
        END LOOP;
    END LOOP;
END;
$$;