-- Create a function to update the fts_document column
-- Gate address/city via to_json(NEW) to avoid compile errors when columns are absent
CREATE OR REPLACE FUNCTION update_users_fts_document() RETURNS TRIGGER AS $$
DECLARE
    v_first TEXT := coalesce(NEW.first_name, '');
    v_last  TEXT := coalesce(NEW.last_name, '');
    v_addr  TEXT := coalesce((to_json(NEW)->>'address'), '');
    v_city  TEXT := coalesce((to_json(NEW)->>'city'), '');
    v_obj   TEXT := coalesce(NEW.objective_summary, '');
BEGIN
    NEW.fts_document :=
        setweight(to_tsvector('english', v_first), 'A') ||
        setweight(to_tsvector('english', v_last),  'A') ||
        setweight(to_tsvector('english', v_addr),  'B') ||
        setweight(to_tsvector('english', v_city),  'B') ||
        setweight(to_tsvector('english', v_obj),   'B');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create a trigger that calls the function before insert or update on the users table
CREATE TRIGGER trg_users_fts_update
BEFORE INSERT OR UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_users_fts_document();

-- Update existing rows to populate the new fts_document column
-- Use a DO block to conditionally include address/city based on column existence
DO $$
DECLARE
    has_address BOOLEAN;
    has_city    BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'address'
    ) INTO has_address;

    SELECT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'city'
    ) INTO has_city;

    IF has_address AND has_city THEN
        UPDATE users SET fts_document =
            setweight(to_tsvector('english', coalesce(first_name, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(last_name,  '')), 'A') ||
            setweight(to_tsvector('english', coalesce(address,    '')), 'B') ||
            setweight(to_tsvector('english', coalesce(city,       '')), 'B') ||
            setweight(to_tsvector('english', coalesce(objective_summary, '')), 'B');
    ELSIF has_address AND NOT has_city THEN
        UPDATE users SET fts_document =
            setweight(to_tsvector('english', coalesce(first_name, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(last_name,  '')), 'A') ||
            setweight(to_tsvector('english', coalesce(address,    '')), 'B') ||
            setweight(to_tsvector('english', coalesce(objective_summary, '')), 'B');
    ELSIF NOT has_address AND has_city THEN
        UPDATE users SET fts_document =
            setweight(to_tsvector('english', coalesce(first_name, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(last_name,  '')), 'A') ||
            setweight(to_tsvector('english', coalesce(city,       '')), 'B') ||
            setweight(to_tsvector('english', coalesce(objective_summary, '')), 'B');
    ELSE
        UPDATE users SET fts_document =
            setweight(to_tsvector('english', coalesce(first_name, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(last_name,  '')), 'A') ||
            setweight(to_tsvector('english', coalesce(objective_summary, '')), 'B');
    END IF;
END $$;
