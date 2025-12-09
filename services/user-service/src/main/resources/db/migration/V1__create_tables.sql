-- =============================================
-- User Service - V1: Create Tables
-- =============================================

-- ---------------------------------------------
-- Table: countries (Reference Data)
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS user_schema.countries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    abbreviation VARCHAR(10) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for searching countries
CREATE INDEX IF NOT EXISTS idx_countries_name ON user_schema.countries(name);
CREATE INDEX IF NOT EXISTS idx_countries_abbreviation ON user_schema.countries(abbreviation);

-- ---------------------------------------------
-- Table: skills (Reference Data)
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS user_schema.skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    normalized_name VARCHAR(255) NOT NULL,
    usage_count INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for searching skills
CREATE INDEX IF NOT EXISTS idx_skills_name ON user_schema.skills(name);
CREATE INDEX IF NOT EXISTS idx_skills_normalized_name ON user_schema.skills(normalized_name);

-- ---------------------------------------------
-- Table: users
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS user_schema.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    country_id UUID REFERENCES user_schema.countries(id),
    objective_summary TEXT,
    is_premium BOOLEAN NOT NULL DEFAULT FALSE,
    profile_updated_at TIMESTAMP,
    search_vector TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for users
CREATE INDEX IF NOT EXISTS idx_users_email ON user_schema.users(email);
CREATE INDEX IF NOT EXISTS idx_users_country_id ON user_schema.users(country_id);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON user_schema.users(is_active);

-- ---------------------------------------------
-- Table: user_education
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS user_schema.user_education (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_schema.users(id) ON DELETE CASCADE,
    institution VARCHAR(255) NOT NULL,
    education_level VARCHAR(50),
    field_of_study VARCHAR(255) NOT NULL,
    start_at DATE,
    end_at DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for user education lookup
CREATE INDEX IF NOT EXISTS idx_user_education_user_id ON user_schema.user_education(user_id);

-- ---------------------------------------------
-- Table: user_work_experience
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS user_schema.user_work_experience (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_schema.users(id) ON DELETE CASCADE,
    job_title VARCHAR(255) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    employment_type VARCHAR(50),
    country_id UUID REFERENCES user_schema.countries(id),
    start_at DATE,
    end_at DATE,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for user work experience
CREATE INDEX IF NOT EXISTS idx_user_work_experience_user_id ON user_schema.user_work_experience(user_id);
CREATE INDEX IF NOT EXISTS idx_user_work_experience_country_id ON user_schema.user_work_experience(country_id);

-- ---------------------------------------------
-- Table: user_skills (Junction Table)
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS user_schema.user_skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_schema.users(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES user_schema.skills(id) ON DELETE CASCADE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_skill UNIQUE (user_id, skill_id)
);

-- Indexes for user skills
CREATE INDEX IF NOT EXISTS idx_user_skills_user_id ON user_schema.user_skills(user_id);
CREATE INDEX IF NOT EXISTS idx_user_skills_skill_id ON user_schema.user_skills(skill_id);

