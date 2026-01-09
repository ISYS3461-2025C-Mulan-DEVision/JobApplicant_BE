-- Migration V13: create search profile and related join tables
-- Tables: users_search_profiles, user_search_profile_skills,
--         user_search_profile_employment_statuses, user_search_profile_job_titles

CREATE TABLE IF NOT EXISTS users_search_profiles (
	id UUID NOT NULL PRIMARY KEY,
	user_id UUID NOT NULL UNIQUE,
	salary_min NUMERIC(19,2),
	salary_max NUMERIC(19,2),
	country_abbreviation VARCHAR(16),
	education_level VARCHAR(64),
	created_at TIMESTAMP WITHOUT TIME ZONE,
	updated_at TIMESTAMP WITHOUT TIME ZONE,
	is_active BOOLEAN DEFAULT true,
	deactivated_at TIMESTAMP WITHOUT TIME ZONE,
	CONSTRAINT fk_users_search_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_users_search_profiles_user_id ON users_search_profiles(user_id);

CREATE TABLE IF NOT EXISTS user_search_profile_skills (
	id UUID NOT NULL PRIMARY KEY,
	user_search_profile_id UUID NOT NULL,
	skill_id UUID NOT NULL,
	created_at TIMESTAMP WITHOUT TIME ZONE,
	updated_at TIMESTAMP WITHOUT TIME ZONE,
	is_active BOOLEAN DEFAULT true,
	deactivated_at TIMESTAMP WITHOUT TIME ZONE,
	CONSTRAINT sp_user_skill UNIQUE (user_search_profile_id, skill_id),
	CONSTRAINT fk_usp_skill_profile FOREIGN KEY (user_search_profile_id) REFERENCES users_search_profiles(id) ON DELETE CASCADE,
	CONSTRAINT fk_usp_skill_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_usp_skills_profile_id ON user_search_profile_skills(user_search_profile_id);
CREATE INDEX IF NOT EXISTS idx_usp_skills_skill_id ON user_search_profile_skills(skill_id);

CREATE TABLE IF NOT EXISTS user_search_profile_employment_statuses (
	id UUID NOT NULL PRIMARY KEY,
	user_search_profile_id UUID NOT NULL,
	employment_type VARCHAR(64) NOT NULL,
	created_at TIMESTAMP WITHOUT TIME ZONE,
	updated_at TIMESTAMP WITHOUT TIME ZONE,
	is_active BOOLEAN DEFAULT true,
	deactivated_at TIMESTAMP WITHOUT TIME ZONE,
	CONSTRAINT sp_user_employment_status UNIQUE (user_search_profile_id, employment_type),
	CONSTRAINT fk_usp_employment_profile FOREIGN KEY (user_search_profile_id) REFERENCES users_search_profiles(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_usp_employment_profile_id ON user_search_profile_employment_statuses(user_search_profile_id);

CREATE TABLE IF NOT EXISTS user_search_profile_job_titles (
	id UUID NOT NULL PRIMARY KEY,
	user_search_profile_id UUID NOT NULL,
	job_title VARCHAR(255) NOT NULL,
	created_at TIMESTAMP WITHOUT TIME ZONE,
	updated_at TIMESTAMP WITHOUT TIME ZONE,
	is_active BOOLEAN DEFAULT true,
	deactivated_at TIMESTAMP WITHOUT TIME ZONE,
	CONSTRAINT sp_user_job_title UNIQUE (user_search_profile_id, job_title),
	CONSTRAINT fk_usp_job_profile FOREIGN KEY (user_search_profile_id) REFERENCES users_search_profiles(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_usp_job_profile_id ON user_search_profile_job_titles(user_search_profile_id);

