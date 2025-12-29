/* Create table for search profile job titles */
CREATE TABLE IF NOT EXISTS search_profile_job_titles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title TEXT NOT NULL,
    search_profile_id UUID NOT NULL,
    is_active boolean NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_search_profile_job_titles_search_profile_id ON search_profile_job_titles (search_profile_id);

ALTER TABLE search_profile_job_titles
    ADD CONSTRAINT fk_search_profile_job_titles_search_profile FOREIGN KEY (search_profile_id) REFERENCES search_profiles(id);
