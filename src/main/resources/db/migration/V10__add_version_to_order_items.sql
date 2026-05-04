-- =====================================================
-- ADD VERSION COLUMN TO ORDER_ITEMS TABLE
-- Version: 10
-- Description: Add version column for optimistic locking
-- =====================================================

ALTER TABLE order_items ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

