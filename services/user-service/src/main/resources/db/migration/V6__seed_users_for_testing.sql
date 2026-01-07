-- 1. Insert Skills (Global - Runs on all shards)
INSERT INTO skills (id, name, normalized_name, usage_count, is_active) VALUES
('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'Java', 'java', 0, TRUE),
('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'Spring Boot', 'spring boot', 0, TRUE),
('c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', 'Kafka', 'kafka', 0, TRUE),
('d4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4', 'React', 'react', 0, TRUE),
('e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5', 'JavaScript', 'javascript', 0, TRUE),
('f6f6f6f6-f6f6-f6f6-f6f6-f6f6f6f6f6f6', 'HTML', 'html', 0, TRUE)
ON CONFLICT (name) DO NOTHING;

-- 2. Shard-Specific User Seeding
DO $$
DECLARE
    vn_country_id UUID;
    us_country_id UUID;
    
    -- Consistent Skill IDs from Step 1
    java_id       UUID := 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1';
    springboot_id UUID := 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2';
    kafka_id      UUID := 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3';
    react_id      UUID := 'd4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4';
    js_id         UUID := 'e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5';
    html_id       UUID := 'f6f6f6f6-f6f6-f6f6-f6f6-f6f6f6f6f6f6';
    phan_user_id  UUID := 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1';
    polar_user_id UUID := 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2';
BEGIN
    -- Look up IDs from the current database
    SELECT id INTO vn_country_id FROM countries WHERE abbreviation = 'VN';
    SELECT id INTO us_country_id FROM countries WHERE abbreviation = 'US';

    -- =============================================
    -- VIETNAM SHARD
    -- =============================================
    IF current_database() = 'user_shard_vn_db' THEN
        
        INSERT INTO users (id, email, first_name, last_name, country_id, objective_summary)
        VALUES (phan_user_id, 'phan.nguyen@example.com', 'Phan', 'Nguyen', vn_country_id, 'Expert in Java and Kafka.')
        ON CONFLICT (email) DO NOTHING;

        INSERT INTO user_skills (id, user_id, skill_id) VALUES
        (gen_random_uuid(), phan_user_id, java_id),
        (gen_random_uuid(), phan_user_id, springboot_id),
        (gen_random_uuid(), phan_user_id, kafka_id)
        ON CONFLICT DO NOTHING;

        RAISE NOTICE 'Seeded Phan Nguyen into VN Shard';

    -- =============================================
    -- NORTH AMERICA SHARD
    -- =============================================
    ELSIF current_database() = 'user_shard_north_america_db' THEN
        
        INSERT INTO users (id, email, first_name, last_name, country_id, objective_summary)
        VALUES (polar_user_id, 'polar.bear@example.com', 'Polar', 'Bear', us_country_id, 'Frontend Developer.')
        ON CONFLICT (email) DO NOTHING;

        INSERT INTO user_skills (id, user_id, skill_id) VALUES
        (gen_random_uuid(), polar_user_id, react_id),
        (gen_random_uuid(), polar_user_id, js_id),
        (gen_random_uuid(), polar_user_id, html_id)
        ON CONFLICT DO NOTHING;

        RAISE NOTICE 'Seeded Polar Bear into NA Shard';

    END IF;
    -- One END IF closes both IF and ELSIF
END $$;