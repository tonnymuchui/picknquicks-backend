-- =====================================================
-- CART TABLES MIGRATION
-- Version: 5
-- Description: Create carts, cart_items tables
-- =====================================================

-- =====================================================
-- CARTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS carts (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     user_id UUID,
                                     guest_token VARCHAR(36),
                                     status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                                     expires_at TIMESTAMP,
                                     last_activity_at TIMESTAMP,
                                     version BIGINT DEFAULT 0,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                     CONSTRAINT chk_cart_user_or_guest CHECK (
                                         (user_id IS NOT NULL AND guest_token IS NULL) OR
                                         (user_id IS NULL AND guest_token IS NOT NULL)
                                         )
);

-- =====================================================
-- CARTS INDEXES
-- =====================================================
CREATE UNIQUE INDEX uk_cart_user ON carts(user_id) WHERE user_id IS NOT NULL AND status = 'ACTIVE';
CREATE UNIQUE INDEX uk_cart_guest_token ON carts(guest_token) WHERE guest_token IS NOT NULL AND status = 'ACTIVE';
CREATE INDEX idx_cart_user ON carts(user_id);
CREATE INDEX idx_cart_guest_token ON carts(guest_token);
CREATE INDEX idx_cart_status ON carts(status);
CREATE INDEX idx_cart_expires ON carts(expires_at);
CREATE INDEX idx_cart_updated ON carts(updated_at);

-- =====================================================
-- CART ITEMS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS cart_items (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                          cart_id UUID NOT NULL,
                                          product_id UUID NOT NULL,
                                          quantity INTEGER NOT NULL DEFAULT 1,
                                          price DECIMAL(10, 2) NOT NULL,
                                          tax_rate DECIMAL(5, 2) DEFAULT 0.00,
                                          version BIGINT DEFAULT 0,
                                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
                                          CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                                          CONSTRAINT chk_cart_item_quantity CHECK (quantity > 0),
                                          CONSTRAINT chk_cart_item_price CHECK (price >= 0)
);

-- =====================================================
-- CART ITEMS INDEXES
-- =====================================================
CREATE UNIQUE INDEX uk_cart_item_cart_product ON cart_items(cart_id, product_id);
CREATE INDEX idx_cart_item_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_item_product ON cart_items(product_id);

-- =====================================================
-- UPDATE TRIGGERS
-- =====================================================
CREATE OR REPLACE FUNCTION update_carts_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER carts_updated_at_trigger
    BEFORE UPDATE ON carts
    FOR EACH ROW
EXECUTE FUNCTION update_carts_updated_at();

CREATE OR REPLACE FUNCTION update_cart_items_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER cart_items_updated_at_trigger
    BEFORE UPDATE ON cart_items
    FOR EACH ROW
EXECUTE FUNCTION update_cart_items_updated_at();

-- =====================================================
-- TRIGGER: Update cart last_activity_at on item changes
-- =====================================================
CREATE OR REPLACE FUNCTION update_cart_last_activity()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE carts
    SET last_activity_at = CURRENT_TIMESTAMP
    WHERE id = COALESCE(NEW.cart_id, OLD.cart_id);
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER cart_items_activity_trigger
    AFTER INSERT OR UPDATE OR DELETE ON cart_items
    FOR EACH ROW
EXECUTE FUNCTION update_cart_last_activity();

-- =====================================================
-- COMMENTS
-- =====================================================
COMMENT ON TABLE carts IS 'Shopping carts for both guest and authenticated users';
COMMENT ON TABLE cart_items IS 'Items in shopping carts';

COMMENT ON COLUMN carts.user_id IS 'User ID for authenticated carts (NULL for guest)';
COMMENT ON COLUMN carts.guest_token IS 'Guest token for anonymous carts (NULL for authenticated)';
COMMENT ON COLUMN carts.status IS 'Cart status: ACTIVE, ABANDONED, CONVERTED, EXPIRED, MERGED';
COMMENT ON COLUMN carts.expires_at IS 'Expiration timestamp for guest carts (30 days)';
COMMENT ON COLUMN carts.last_activity_at IS 'Last time cart was modified';
COMMENT ON COLUMN carts.version IS 'Optimistic locking version';

COMMENT ON COLUMN cart_items.price IS 'Price at time of adding to cart (may differ from current product price)';
COMMENT ON COLUMN cart_items.tax_rate IS 'Tax rate percentage applied to this item';