ALTER TABLE users ADD COLUMN fts_document TSVECTOR;

CREATE INDEX idx_users_fts_document ON users USING GIN (fts_document);
