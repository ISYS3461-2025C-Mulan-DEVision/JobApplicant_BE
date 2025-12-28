-- Create a function to update the fts_document column
CREATE OR REPLACE FUNCTION update_users_fts_document() RETURNS TRIGGER AS $$
BEGIN
    NEW.fts_document :=
        setweight(to_tsvector('english', coalesce(NEW.first_name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(NEW.last_name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(NEW.objective_summary, '')), 'B');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create a trigger that calls the function before insert or update on the users table
CREATE TRIGGER trg_users_fts_update
BEFORE INSERT OR UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_users_fts_document();

-- Update existing rows to populate the new fts_document column
UPDATE users SET fts_document = 
    setweight(to_tsvector('english', coalesce(first_name, '')), 'A') ||
    setweight(to_tsvector('english', coalesce(last_name, '')), 'A') ||
    setweight(to_tsvector('english', coalesce(objective_summary, '')), 'B');
