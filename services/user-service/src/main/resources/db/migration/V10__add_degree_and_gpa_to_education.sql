-- V10__add_degree_and_gpa_to_education.sql
-- Add degree (plaintext) and GPA (0-100) to user_education

ALTER TABLE user_education
    ADD COLUMN degree VARCHAR(255),
    ADD COLUMN gpa NUMERIC(5, 2);

COMMENT ON COLUMN user_education.degree IS 'Plaintext degree name (e.g., Bachelor of Software Engineering (Hons))';
COMMENT ON COLUMN user_education.gpa IS 'Grade Point Average (0 to 100)';
