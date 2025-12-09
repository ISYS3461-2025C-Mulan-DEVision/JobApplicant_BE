-- =============================================
-- User Service - V2: Seed Countries
-- =============================================
-- ISO 3166-1 country data (common countries)
-- =============================================

INSERT INTO user_schema.countries (id, name, abbreviation, is_active, created_at, updated_at) VALUES
-- Asia
(gen_random_uuid(), 'Vietnam', 'VN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Japan', 'JP', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'South Korea', 'KR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'China', 'CN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Taiwan', 'TW', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Hong Kong', 'HK', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Singapore', 'SG', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Malaysia', 'MY', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Thailand', 'TH', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Indonesia', 'ID', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Philippines', 'PH', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'India', 'IN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pakistan', 'PK', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Bangladesh', 'BD', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Sri Lanka', 'LK', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Middle East
(gen_random_uuid(), 'United Arab Emirates', 'AE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Saudi Arabia', 'SA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Israel', 'IL', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Turkey', 'TR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- North America
(gen_random_uuid(), 'United States', 'US', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Canada', 'CA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Mexico', 'MX', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- South America
(gen_random_uuid(), 'Brazil', 'BR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Argentina', 'AR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Chile', 'CL', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Colombia', 'CO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Peru', 'PE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Europe
(gen_random_uuid(), 'United Kingdom', 'GB', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Germany', 'DE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'France', 'FR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Italy', 'IT', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Spain', 'ES', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Portugal', 'PT', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Netherlands', 'NL', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Belgium', 'BE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Switzerland', 'CH', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Austria', 'AT', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Sweden', 'SE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Norway', 'NO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Denmark', 'DK', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Finland', 'FI', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Ireland', 'IE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Poland', 'PL', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Czech Republic', 'CZ', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Hungary', 'HU', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Romania', 'RO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Greece', 'GR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Ukraine', 'UA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Russia', 'RU', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Oceania
(gen_random_uuid(), 'Australia', 'AU', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'New Zealand', 'NZ', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Africa
(gen_random_uuid(), 'South Africa', 'ZA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Egypt', 'EG', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Nigeria', 'NG', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Kenya', 'KE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Morocco', 'MA', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

