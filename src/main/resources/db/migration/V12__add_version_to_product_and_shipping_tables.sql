-- =====================================================
-- ADD VERSION COLUMNS TO PRODUCT AND SHIPPING TABLES
-- Version: 12
-- Description: Add version columns for optimistic locking
-- =====================================================

ALTER TABLE products ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE product_images ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE shipping_zones ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE shipping_rates ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

