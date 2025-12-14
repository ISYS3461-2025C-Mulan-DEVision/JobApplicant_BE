-- =============================================
-- Auth Service - V1: Create Credentials Table
-- =============================================

-- ---------------------------------------------
-- Table: credentials
-- Stores authentication data for users
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    user_id UUID,
    role VARCHAR(50) NOT NULL DEFAULT 'FREE',
    auth_provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_credentials_email ON credentials(email);
CREATE INDEX IF NOT EXISTS idx_credentials_user_id ON credentials(user_id);
CREATE INDEX IF NOT EXISTS idx_credentials_is_active ON credentials(is_active);

-- Comments
COMMENT ON TABLE credentials IS 'User authentication credentials';
COMMENT ON COLUMN credentials.user_id IS 'Reference to user profile in user-service';
COMMENT ON COLUMN credentials.role IS 'User role: FREE, PREMIUM, ADMIN';
COMMENT ON COLUMN credentials.auth_provider IS 'Authentication provider: LOCAL, GOOGLE, LINKEDIN';
