-- V6__seed_users_for_testing.sql

-- Insert Skills if they don't exist
INSERT INTO skills (id, name, normalized_name, usage_count, is_active, created_at, updated_at) VALUES
(gen_random_uuid(), 'Java', 'java', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Spring Boot', 'spring boot', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Kafka', 'kafka', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'React', 'react', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'JavaScript', 'javascript', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'HTML', 'html', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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

    -- Consistent UUIDs for testing. These must be globally unique.
    phan_nguyen_user_id UUID := 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1';
    polar_bear_user_id UUID := 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2';
BEGIN
    SELECT id INTO vn_country_id FROM countries WHERE abbreviation = 'VN';
    SELECT id INTO us_country_id FROM countries WHERE abbreviation = 'US';

    SELECT id INTO java_skill_id FROM skills WHERE normalized_name = 'java';
    SELECT id INTO springboot_skill_id FROM skills WHERE normalized_name = 'spring boot';
    SELECT id INTO kafka_skill_id FROM skills WHERE normalized_name = 'kafka';
    SELECT id INTO react_skill_id FROM skills WHERE normalized_name = 'react';
    SELECT id INTO javascript_skill_id FROM skills WHERE normalized_name = 'javascript';
    SELECT id INTO html_skill_id FROM skills WHERE normalized_name = 'html';

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

END $$;
