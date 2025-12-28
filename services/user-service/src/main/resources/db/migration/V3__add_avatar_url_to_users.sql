-- =============================================
-- User Service - V3: Add Avatar URL to Users
-- =============================================

ALTER TABLE users
ADD COLUMN avatar_url VARCHAR(1024);

COMMENT ON COLUMN users.avatar_url IS 'URL for the user profile picture';
