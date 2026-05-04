-- =====================================================
-- ADD VERSION COLUMN TO ORDER_ADDRESSES TABLE
-- Version: 9
-- Description: Add version column for optimistic locking
-- =====================================================

ALTER TABLE order_addresses ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

