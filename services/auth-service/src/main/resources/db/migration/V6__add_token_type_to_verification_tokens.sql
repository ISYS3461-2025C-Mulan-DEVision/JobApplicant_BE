-- =============================================
-- Auth Service - V6: Add Token Type to Verification Tokens
-- =============================================
-- Adds a token_type column to distinguish between ACTIVATION and PASSWORD_RESET tokens

ALTER TABLE verification_tokens
ADD COLUMN IF NOT EXISTS token_type VARCHAR(50) NOT NULL DEFAULT 'ACTIVATION';

-- Add index for token type
CREATE INDEX IF NOT EXISTS idx_verification_tokens_token_type ON verification_tokens(token_type);

-- Comments
COMMENT ON COLUMN verification_tokens.token_type IS 'Type of token: ACTIVATION or PASSWORD_RESET';

