-- =====================================================
-- ADD VERSION COLUMN TO SHIPPING_LOCATIONS TABLE
-- Version: 11
-- Description: Add version column for optimistic locking
-- =====================================================

ALTER TABLE shipping_locations ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

