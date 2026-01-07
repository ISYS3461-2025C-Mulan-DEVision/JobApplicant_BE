-- =============================================
-- Auth Service - V5: Extend verification_tokens with profile fields
-- =============================================
-- Adds optional fields captured during registration so activation flow
-- can build the complete user profile without relying on request scope.
--
-- New columns:
--  - country_abbreviation VARCHAR(3)
--  - phone                VARCHAR(20)
--  - address              VARCHAR(255)
--  - city                 VARCHAR(100)
--
-- Idempotent: Uses IF NOT EXISTS to avoid failures on re-apply.

ALTER TABLE verification_tokens
    ADD COLUMN IF NOT EXISTS country_abbreviation VARCHAR(3),
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20),
    ADD COLUMN IF NOT EXISTS address VARCHAR(255),
    ADD COLUMN IF NOT EXISTS city VARCHAR(100);

-- Optional: Add comments for documentation
COMMENT ON COLUMN verification_tokens.country_abbreviation IS '2-3 letter country code captured at registration';
COMMENT ON COLUMN verification_tokens.phone IS 'Phone number captured at registration';
COMMENT ON COLUMN verification_tokens.address IS 'Street address (name/number) captured at registration';
COMMENT ON COLUMN verification_tokens.city IS 'City captured at registration';
