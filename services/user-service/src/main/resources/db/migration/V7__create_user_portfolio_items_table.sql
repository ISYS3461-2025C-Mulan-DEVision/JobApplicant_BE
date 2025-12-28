-- V7__create_user_portfolio_items_table.sql

CREATE TABLE user_portfolio_items (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    file_url VARCHAR(2048) NOT NULL,
    description TEXT,
    media_type VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deactivated_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT fk_user
        FOREIGN KEY(user_id) 
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_user_portfolio_items_user_id ON user_portfolio_items(user_id);
