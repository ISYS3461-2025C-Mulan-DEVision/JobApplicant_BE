-- =============================================
-- Auth Service - V2: Create Verification Tokens Table
-- =============================================

-- ---------------------------------------------
-- Table: verification_tokens
-- Stores tokens for email verification
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(255) NOT NULL UNIQUE,
    credential_id UUID NOT NULL REFERENCES credentials(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_verification_tokens_token ON verification_tokens(token);
CREATE INDEX IF NOT EXISTS idx_verification_tokens_credential_id ON verification_tokens(credential_id);

-- Comments
COMMENT ON TABLE verification_tokens IS 'Tokens for user email verification';
COMMENT ON COLUMN verification_tokens.token IS 'Unique verification token string';
COMMENT ON COLUMN verification_tokens.credential_id IS 'Foreign key to auth_credentials table';
COMMENT ON COLUMN verification_tokens.expiry_date IS 'Timestamp when the token expires';
