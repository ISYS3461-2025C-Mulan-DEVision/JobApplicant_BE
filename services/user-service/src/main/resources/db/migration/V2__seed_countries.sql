-- =============================================
-- User Service - V2: Seed Countries
-- =============================================
-- ISO 3166-1 country data (common countries)
-- Uses deterministic UUIDs (v5) to ensure consistent IDs across all shards.
-- =============================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
DECLARE
    -- Fixed UUID namespace for v5 generation (DNS namespace)
    v_ns_uuid CONSTANT UUID := '6ba7b810-9dad-11d1-80b4-00c04fd430c8';
BEGIN
    INSERT INTO countries (id, name, abbreviation, is_active, created_at, updated_at) VALUES
    -- Asia
    (uuid_generate_v5(v_ns_uuid, 'VN'), 'Vietnam', 'VN', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'JP'), 'Japan', 'JP', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'KR'), 'South Korea', 'KR', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'CN'), 'China', 'CN', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'TW'), 'Taiwan', 'TW', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'HK'), 'Hong Kong', 'HK', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'SG'), 'Singapore', 'SG', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'MY'), 'Malaysia', 'MY', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'TH'), 'Thailand', 'TH', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'ID'), 'Indonesia', 'ID', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'PH'), 'Philippines', 'PH', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'IN'), 'India', 'IN', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'PK'), 'Pakistan', 'PK', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'BD'), 'Bangladesh', 'BD', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'LK'), 'Sri Lanka', 'LK', TRUE, now(), now()),

    -- Middle East
    (uuid_generate_v5(v_ns_uuid, 'AE'), 'United Arab Emirates', 'AE', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'SA'), 'Saudi Arabia', 'SA', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'IL'), 'Israel', 'IL', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'TR'), 'Turkey', 'TR', TRUE, now(), now()),

    -- North America
    (uuid_generate_v5(v_ns_uuid, 'US'), 'United States', 'US', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'CA'), 'Canada', 'CA', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'MX'), 'Mexico', 'MX', TRUE, now(), now()),

    -- South America
    (uuid_generate_v5(v_ns_uuid, 'BR'), 'Brazil', 'BR', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'AR'), 'Argentina', 'AR', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'CL'), 'Chile', 'CL', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'CO'), 'Colombia', 'CO', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'PE'), 'Peru', 'PE', TRUE, now(), now()),

    -- Europe
    (uuid_generate_v5(v_ns_uuid, 'GB'), 'United Kingdom', 'GB', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'DE'), 'Germany', 'DE', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'FR'), 'France', 'FR', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'IT'), 'Italy', 'IT', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'ES'), 'Spain', 'ES', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'PT'), 'Portugal', 'PT', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'NL'), 'Netherlands', 'NL', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'BE'), 'Belgium', 'BE', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'CH'), 'Switzerland', 'CH', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'AT'), 'Austria', 'AT', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'SE'), 'Sweden', 'SE', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'NO'), 'Norway', 'NO', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'DK'), 'Denmark', 'DK', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'FI'), 'Finland', 'FI', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'IE'), 'Ireland', 'IE', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'PL'), 'Poland', 'PL', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'CZ'), 'Czech Republic', 'CZ', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'HU'), 'Hungary', 'HU', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'RO'), 'Romania', 'RO', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'GR'), 'Greece', 'GR', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'UA'), 'Ukraine', 'UA', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'RU'), 'Russia', 'RU', TRUE, now(), now()),

    -- Oceania
    (uuid_generate_v5(v_ns_uuid, 'AU'), 'Australia', 'AU', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'NZ'), 'New Zealand', 'NZ', TRUE, now(), now()),

    -- Africa
    (uuid_generate_v5(v_ns_uuid, 'ZA'), 'South Africa', 'ZA', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'EG'), 'Egypt', 'EG', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'NG'), 'Nigeria', 'NG', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'KE'), 'Kenya', 'KE', TRUE, now(), now()),
    (uuid_generate_v5(v_ns_uuid, 'MA'), 'Morocco', 'MA', TRUE, now(), now())
    ON CONFLICT (name) DO NOTHING;
END $$;