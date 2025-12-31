-- =============================================
-- V9__add_address_city_to_users.sql
-- Migration: Add address and city columns to users table
-- =============================================

-- This migration adds two new optional fields required by SRS:
-- - address: street name/number (up to 255 chars)
-- - city   : city name (up to 100 chars)
--
-- Notes:
-- - Columns are added as nullable to avoid issues with existing data.
-- - Uses IF NOT EXISTS to be idempotent on repeated runs.
-- - No changes to existing FTS triggers or functions are made here.

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS address VARCHAR(255),
    ADD COLUMN IF NOT EXISTS city VARCHAR(100);

-- End of migration
