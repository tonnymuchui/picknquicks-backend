-- =====================================================
-- ORDERS AND SHIPPING TABLES MIGRATION
-- Version: 6
-- Description: Create orders, order_items, order_addresses, payments, shipping tables
-- =====================================================

-- =====================================================
-- ORDERS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS orders (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      order_number VARCHAR(20) NOT NULL UNIQUE,
                                      user_id UUID,
                                      email VARCHAR(255) NOT NULL,
                                      phone_number VARCHAR(20) NOT NULL,
                                      customer_name VARCHAR(255) NOT NULL,
                                      status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                      payment_method VARCHAR(20) NOT NULL,
                                      payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                      subtotal DECIMAL(10, 2) NOT NULL,
                                      tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                                      shipping_cost DECIMAL(10, 2) NOT NULL,
                                      total_amount DECIMAL(10, 2) NOT NULL,
                                      notes TEXT,
                                      admin_notes TEXT,
                                      tracking_number VARCHAR(100),
                                      estimated_delivery_date TIMESTAMP,
                                      delivered_at TIMESTAMP,
                                      cancelled_at TIMESTAMP,
                                      cancellation_reason VARCHAR(500),
                                      version BIGINT DEFAULT 0,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                                      CONSTRAINT chk_order_user_or_email CHECK (
                                          user_id IS NOT NULL OR (user_id IS NULL AND email IS NOT NULL)
                                          ),
                                      CONSTRAINT chk_order_amounts CHECK (
                                          subtotal >= 0 AND tax_amount >= 0 AND shipping_cost >= 0 AND total_amount >= 0
                                          )
);

-- =====================================================
-- ORDERS INDEXES
-- =====================================================
CREATE UNIQUE INDEX uk_order_number ON orders(order_number);
CREATE INDEX idx_order_user ON orders(user_id);
CREATE INDEX idx_order_email ON orders(email);
CREATE INDEX idx_order_phone ON orders(phone_number);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_payment_status ON orders(payment_status);
CREATE INDEX idx_order_created ON orders(created_at DESC);
CREATE INDEX idx_order_tracking ON orders(tracking_number);

-- =====================================================
-- ORDER ITEMS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS order_items (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           order_id UUID NOT NULL,
                                           product_id UUID NOT NULL,
                                           product_name VARCHAR(255) NOT NULL,
                                           product_sku VARCHAR(100) NOT NULL,
                                           product_image_url VARCHAR(255),
                                           quantity INTEGER NOT NULL,
                                           unit_price DECIMAL(10, 2) NOT NULL,
                                           tax_rate DECIMAL(5, 2) DEFAULT 0.00,
                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                                           CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
                                           CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
                                           CONSTRAINT chk_order_item_price CHECK (unit_price >= 0)
);

-- =====================================================
-- ORDER ITEMS INDEXES
-- =====================================================
CREATE INDEX idx_order_item_order ON order_items(order_id);
CREATE INDEX idx_order_item_product ON order_items(product_id);

-- =====================================================
-- ORDER ADDRESSES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS order_addresses (
                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                               order_id UUID NOT NULL UNIQUE,
                                               recipient_name VARCHAR(255) NOT NULL,
                                               phone_number VARCHAR(20) NOT NULL,
                                               address_line1 VARCHAR(255) NOT NULL,
                                               address_line2 VARCHAR(255),
                                               city VARCHAR(100) NOT NULL,
                                               county VARCHAR(100),
                                               postal_code VARCHAR(20),
                                               country VARCHAR(100) NOT NULL DEFAULT 'Kenya',
                                               notes TEXT,
                                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               CONSTRAINT fk_order_address_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- =====================================================
-- ORDER ADDRESSES INDEXES
-- =====================================================
CREATE UNIQUE INDEX uk_order_address_order ON order_addresses(order_id);
CREATE INDEX idx_order_address_city ON order_addresses(city);

-- =====================================================
-- PAYMENTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS payments (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        order_id UUID NOT NULL UNIQUE,
                                        payment_method VARCHAR(20) NOT NULL,
                                        status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                        amount DECIMAL(10, 2) NOT NULL,
                                        transaction_id VARCHAR(100) UNIQUE,
                                        mpesa_checkout_request_id VARCHAR(100),
                                        mpesa_merchant_request_id VARCHAR(100),
                                        mpesa_receipt_number VARCHAR(100),
                                        phone_number VARCHAR(20),
                                        paid_at TIMESTAMP,
                                        failure_reason VARCHAR(500),
                                        callback_data TEXT,
                                        version BIGINT DEFAULT 0,
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                                        CONSTRAINT chk_payment_amount CHECK (amount >= 0)
);

-- =====================================================
-- PAYMENTS INDEXES
-- =====================================================
CREATE UNIQUE INDEX uk_payment_order ON payments(order_id);
CREATE UNIQUE INDEX uk_payment_transaction_id ON payments(transaction_id) WHERE transaction_id IS NOT NULL;
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_method ON payments(payment_method);
CREATE INDEX idx_payment_mpesa_checkout ON payments(mpesa_checkout_request_id);
CREATE INDEX idx_payment_mpesa_merchant ON payments(mpesa_merchant_request_id);

-- =====================================================
-- SHIPPING ZONES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS shipping_zones (
                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                              name VARCHAR(100) NOT NULL,
                                              description TEXT,
                                              active BOOLEAN NOT NULL DEFAULT TRUE,
                                              display_order INTEGER DEFAULT 0,
                                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- SHIPPING ZONES INDEXES
-- =====================================================
CREATE INDEX idx_shipping_zone_name ON shipping_zones(name);
CREATE INDEX idx_shipping_zone_active ON shipping_zones(active);
CREATE INDEX idx_shipping_zone_display_order ON shipping_zones(display_order);

-- =====================================================
-- SHIPPING LOCATIONS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS shipping_locations (
                                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                  zone_id UUID NOT NULL,
                                                  city VARCHAR(100) NOT NULL,
                                                  county VARCHAR(100),
                                                  postal_code VARCHAR(20),
                                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                  CONSTRAINT fk_shipping_location_zone FOREIGN KEY (zone_id) REFERENCES shipping_zones(id) ON DELETE CASCADE
);

-- =====================================================
-- SHIPPING LOCATIONS INDEXES
-- =====================================================
CREATE INDEX idx_shipping_location_zone ON shipping_locations(zone_id);
CREATE INDEX idx_shipping_location_city ON shipping_locations(LOWER(city));
CREATE INDEX idx_shipping_location_county ON shipping_locations(LOWER(county));

-- =====================================================
-- SHIPPING RATES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS shipping_rates (
                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                              zone_id UUID NOT NULL,
                                              name VARCHAR(100) NOT NULL,
                                              description TEXT,
                                              base_cost DECIMAL(10, 2) NOT NULL,
                                              min_order_amount DECIMAL(10, 2),
                                              max_order_amount DECIMAL(10, 2),
                                              free_shipping_threshold DECIMAL(10, 2),
                                              estimated_days_min INTEGER,
                                              estimated_days_max INTEGER,
                                              active BOOLEAN NOT NULL DEFAULT TRUE,
                                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              CONSTRAINT fk_shipping_rate_zone FOREIGN KEY (zone_id) REFERENCES shipping_zones(id) ON DELETE CASCADE,
                                              CONSTRAINT chk_shipping_rate_cost CHECK (base_cost >= 0)
);

-- =====================================================
-- SHIPPING RATES INDEXES
-- =====================================================
CREATE INDEX idx_shipping_rate_zone ON shipping_rates(zone_id);
CREATE INDEX idx_shipping_rate_active ON shipping_rates(active);

-- =====================================================
-- UPDATE TRIGGERS
-- =====================================================
CREATE OR REPLACE FUNCTION update_orders_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER orders_updated_at_trigger
    BEFORE UPDATE ON orders
    FOR EACH ROW
EXECUTE FUNCTION update_orders_updated_at();

CREATE OR REPLACE FUNCTION update_order_items_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER order_items_updated_at_trigger
    BEFORE UPDATE ON order_items
    FOR EACH ROW
EXECUTE FUNCTION update_order_items_updated_at();

CREATE OR REPLACE FUNCTION update_order_addresses_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER order_addresses_updated_at_trigger
    BEFORE UPDATE ON order_addresses
    FOR EACH ROW
EXECUTE FUNCTION update_order_addresses_updated_at();

CREATE OR REPLACE FUNCTION update_payments_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER payments_updated_at_trigger
    BEFORE UPDATE ON payments
    FOR EACH ROW
EXECUTE FUNCTION update_payments_updated_at();

CREATE OR REPLACE FUNCTION update_shipping_zones_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER shipping_zones_updated_at_trigger
    BEFORE UPDATE ON shipping_zones
    FOR EACH ROW
EXECUTE FUNCTION update_shipping_zones_updated_at();

CREATE OR REPLACE FUNCTION update_shipping_locations_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER shipping_locations_updated_at_trigger
    BEFORE UPDATE ON shipping_locations
    FOR EACH ROW
EXECUTE FUNCTION update_shipping_locations_updated_at();

CREATE OR REPLACE FUNCTION update_shipping_rates_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER shipping_rates_updated_at_trigger
    BEFORE UPDATE ON shipping_rates
    FOR EACH ROW
EXECUTE FUNCTION update_shipping_rates_updated_at();

-- =====================================================
-- SEED DATA: DEFAULT SHIPPING ZONES
-- =====================================================
INSERT INTO shipping_zones (id, name, description, active, display_order) VALUES
                                                                              (gen_random_uuid(), 'Nairobi Metro', 'Nairobi and surrounding areas', TRUE, 1),
                                                                              (gen_random_uuid(), 'Coastal Region', 'Mombasa and coastal areas', TRUE, 2),
                                                                              (gen_random_uuid(), 'Central Kenya', 'Central Kenya region', TRUE, 3),
                                                                              (gen_random_uuid(), 'Western Kenya', 'Western Kenya region', TRUE, 4),
                                                                              (gen_random_uuid(), 'Rest of Kenya', 'All other areas', TRUE, 5)
ON CONFLICT DO NOTHING;

-- =====================================================
-- SEED DATA: SHIPPING LOCATIONS
-- =====================================================
DO $$
    DECLARE
        nairobi_zone_id UUID;
        coastal_zone_id UUID;
        central_zone_id UUID;
        western_zone_id UUID;
        rest_zone_id UUID;
    BEGIN
        SELECT id INTO nairobi_zone_id FROM shipping_zones WHERE name = 'Nairobi Metro';
        SELECT id INTO coastal_zone_id FROM shipping_zones WHERE name = 'Coastal Region';
        SELECT id INTO central_zone_id FROM shipping_zones WHERE name = 'Central Kenya';
        SELECT id INTO western_zone_id FROM shipping_zones WHERE name = 'Western Kenya';
        SELECT id INTO rest_zone_id FROM shipping_zones WHERE name = 'Rest of Kenya';

        -- Nairobi Metro
        INSERT INTO shipping_locations (zone_id, city, county) VALUES
                                                                   (nairobi_zone_id, 'Nairobi', 'Nairobi County'),
                                                                   (nairobi_zone_id, 'Kiambu', 'Kiambu County'),
                                                                   (nairobi_zone_id, 'Machakos', 'Machakos County'),
                                                                   (nairobi_zone_id, 'Ruiru', 'Kiambu County'),
                                                                   (nairobi_zone_id, 'Thika', 'Kiambu County');

        -- Coastal Region
        INSERT INTO shipping_locations (zone_id, city, county) VALUES
                                                                   (coastal_zone_id, 'Mombasa', 'Mombasa County'),
                                                                   (coastal_zone_id, 'Kilifi', 'Kilifi County'),
                                                                   (coastal_zone_id, 'Malindi', 'Kilifi County'),
                                                                   (coastal_zone_id, 'Lamu', 'Lamu County');

        -- Central Kenya
        INSERT INTO shipping_locations (zone_id, city, county) VALUES
                                                                   (central_zone_id, 'Nyeri', 'Nyeri County'),
                                                                   (central_zone_id, 'Nakuru', 'Nakuru County'),
                                                                   (central_zone_id, 'Eldoret', 'Uasin Gishu County'),
                                                                   (central_zone_id, 'Naivasha', 'Nakuru County');

        -- Western Kenya
        INSERT INTO shipping_locations (zone_id, city, county) VALUES
                                                                   (western_zone_id, 'Kisumu', 'Kisumu County'),
                                                                   (western_zone_id, 'Kakamega', 'Kakamega County'),
                                                                   (western_zone_id, 'Bungoma', 'Bungoma County');
    END $$;

-- =====================================================
-- SEED DATA: SHIPPING RATES
-- =====================================================
DO $$
    DECLARE
        nairobi_zone_id UUID;
        coastal_zone_id UUID;
        central_zone_id UUID;
        western_zone_id UUID;
        rest_zone_id UUID;
    BEGIN
        SELECT id INTO nairobi_zone_id FROM shipping_zones WHERE name = 'Nairobi Metro';
        SELECT id INTO coastal_zone_id FROM shipping_zones WHERE name = 'Coastal Region';
        SELECT id INTO central_zone_id FROM shipping_zones WHERE name = 'Central Kenya';
        SELECT id INTO western_zone_id FROM shipping_zones WHERE name = 'Western Kenya';
        SELECT id INTO rest_zone_id FROM shipping_zones WHERE name = 'Rest of Kenya';

        -- Nairobi Metro Rates
        INSERT INTO shipping_rates (zone_id, name, description, base_cost, free_shipping_threshold, estimated_days_min, estimated_days_max, active) VALUES
                                                                                                                                                        (nairobi_zone_id, 'Standard Delivery', 'Standard delivery within Nairobi', 200.00, 5000.00, 1, 2, TRUE),
                                                                                                                                                        (nairobi_zone_id, 'Express Delivery', 'Same-day delivery within Nairobi', 500.00, NULL, 0, 1, TRUE);

        -- Coastal Region Rates
        INSERT INTO shipping_rates (zone_id, name, description, base_cost, free_shipping_threshold, estimated_days_min, estimated_days_max, active) VALUES
            (coastal_zone_id, 'Standard Delivery', 'Standard delivery to coast', 400.00, 10000.00, 2, 4, TRUE);

        -- Central Kenya Rates
        INSERT INTO shipping_rates (zone_id, name, description, base_cost, free_shipping_threshold, estimated_days_min, estimated_days_max, active) VALUES
            (central_zone_id, 'Standard Delivery', 'Standard delivery to central Kenya', 350.00, 7500.00, 2, 3, TRUE);

        -- Western Kenya Rates
        INSERT INTO shipping_rates (zone_id, name, description, base_cost, free_shipping_threshold, estimated_days_min, estimated_days_max, active) VALUES
            (western_zone_id, 'Standard Delivery', 'Standard delivery to western Kenya', 450.00, 10000.00, 3, 5, TRUE);

        -- Rest of Kenya Rates
        INSERT INTO shipping_rates (zone_id, name, description, base_cost, free_shipping_threshold, estimated_days_min, estimated_days_max, active) VALUES
            (rest_zone_id, 'Standard Delivery', 'Standard delivery nationwide', 500.00, 15000.00, 3, 7, TRUE);
    END $$;

-- =====================================================
-- COMMENTS
-- =====================================================
COMMENT ON TABLE orders IS 'Customer orders';
COMMENT ON TABLE order_items IS 'Items in orders';
COMMENT ON TABLE order_addresses IS 'Shipping addresses for orders';
COMMENT ON TABLE payments IS 'Payment records for orders';
COMMENT ON TABLE shipping_zones IS 'Shipping zones for different regions';
COMMENT ON TABLE shipping_locations IS 'Cities/counties in shipping zones';
COMMENT ON TABLE shipping_rates IS 'Shipping rates per zone';

COMMENT ON COLUMN orders.order_number IS 'Unique order identifier for customers';
COMMENT ON COLUMN orders.payment_method IS 'MPESA or CASH_ON_DELIVERY';
COMMENT ON COLUMN orders.payment_status IS 'Payment processing status';
COMMENT ON COLUMN payments.mpesa_checkout_request_id IS 'M-Pesa STK Push checkout request ID';
COMMENT ON COLUMN payments.callback_data IS 'Full M-Pesa callback JSON for audit';
COMMENT ON COLUMN shipping_rates.free_shipping_threshold IS 'Order amount for free shipping (NULL = no free shipping)';