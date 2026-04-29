-- =====================================================
-- PRODUCT TABLES MIGRATION
-- Version: 4
-- Description: Create products, product_images, product_variants, idempotency_keys tables
-- =====================================================

-- =====================================================
-- PRODUCTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS products (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        name VARCHAR(255) NOT NULL,
                                        slug VARCHAR(150) NOT NULL UNIQUE,
                                        sku VARCHAR(100) NOT NULL UNIQUE,
                                        description TEXT,
                                        short_description VARCHAR(500),
                                        price DECIMAL(10, 2) NOT NULL,
                                        sale_price DECIMAL(10, 2),
                                        cost_price DECIMAL(10, 2),
                                        tax_rate DECIMAL(5, 2) DEFAULT 0.00,
                                        category_id UUID NOT NULL,
                                        brand_id UUID,
                                        stock_quantity INTEGER NOT NULL DEFAULT 0,
                                        low_stock_threshold INTEGER DEFAULT 10,
                                        weight_grams INTEGER,
                                        dimensions VARCHAR(50),
                                        active BOOLEAN NOT NULL DEFAULT TRUE,
                                        featured BOOLEAN NOT NULL DEFAULT FALSE,
                                        is_digital BOOLEAN NOT NULL DEFAULT FALSE,
                                        requires_shipping BOOLEAN NOT NULL DEFAULT TRUE,
                                        display_order INTEGER DEFAULT 0,
                                        view_count BIGINT NOT NULL DEFAULT 0,
                                        sale_count BIGINT NOT NULL DEFAULT 0,
                                        average_rating DECIMAL(3, 2) DEFAULT 0.00,
                                        review_count BIGINT NOT NULL DEFAULT 0,
                                        meta_title VARCHAR(128),
                                        meta_description VARCHAR(255),
                                        meta_keywords VARCHAR(255),
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
                                        CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brands(id) ON DELETE SET NULL,
                                        CONSTRAINT chk_product_price_positive CHECK (price > 0),
                                        CONSTRAINT chk_product_sale_price_positive CHECK (sale_price IS NULL OR sale_price > 0),
                                        CONSTRAINT chk_product_cost_price_positive CHECK (cost_price IS NULL OR cost_price > 0),
                                        CONSTRAINT chk_product_tax_rate_range CHECK (tax_rate >= 0 AND tax_rate <= 100),
                                        CONSTRAINT chk_product_stock_non_negative CHECK (stock_quantity >= 0),
                                        CONSTRAINT chk_product_weight_non_negative CHECK (weight_grams IS NULL OR weight_grams >= 0)
);

-- =====================================================
-- PRODUCTS INDEXES
-- =====================================================
CREATE INDEX idx_product_slug ON products(slug);
CREATE INDEX idx_product_sku ON products(sku);
CREATE INDEX idx_product_active ON products(active);
CREATE INDEX idx_product_featured ON products(featured);
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_product_brand ON products(brand_id);
CREATE INDEX idx_product_price ON products(price);
CREATE INDEX idx_product_stock ON products(stock_quantity);
CREATE INDEX idx_product_created ON products(created_at);
CREATE INDEX idx_product_sale_count ON products(sale_count);
CREATE INDEX idx_product_rating ON products(average_rating);
CREATE INDEX idx_product_active_featured ON products(active, featured);
CREATE INDEX idx_product_active_category ON products(active, category_id);
CREATE INDEX idx_product_active_brand ON products(active, brand_id);

-- =====================================================
-- PRODUCT IMAGES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS product_images (
                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                              product_id UUID NOT NULL,
                                              image_url VARCHAR(255) NOT NULL,
                                              alt_text VARCHAR(255),
                                              is_primary BOOLEAN NOT NULL DEFAULT FALSE,
                                              display_order INTEGER NOT NULL DEFAULT 0,
                                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- =====================================================
-- PRODUCT IMAGES INDEXES
-- =====================================================
CREATE INDEX idx_product_image_product ON product_images(product_id);
CREATE INDEX idx_product_image_primary ON product_images(is_primary);
CREATE INDEX idx_product_image_display_order ON product_images(product_id, display_order);

-- =====================================================
-- PRODUCT VARIANTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS product_variants (
                                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                product_id UUID NOT NULL,
                                                sku VARCHAR(100) NOT NULL UNIQUE,
                                                name VARCHAR(100) NOT NULL,
                                                attribute_type VARCHAR(50) NOT NULL,
                                                attribute_value VARCHAR(100) NOT NULL,
                                                price_adjustment DECIMAL(10, 2) DEFAULT 0.00,
                                                stock_quantity INTEGER NOT NULL DEFAULT 0,
                                                image_url VARCHAR(255),
                                                active BOOLEAN NOT NULL DEFAULT TRUE,
                                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                CONSTRAINT fk_product_variant_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                                                CONSTRAINT chk_variant_stock_non_negative CHECK (stock_quantity >= 0)
);

-- =====================================================
-- PRODUCT VARIANTS INDEXES
-- =====================================================
CREATE INDEX idx_product_variant_product ON product_variants(product_id);
CREATE INDEX idx_product_variant_sku ON product_variants(sku);
CREATE INDEX idx_product_variant_active ON product_variants(active);

-- =====================================================
-- IDEMPOTENCY KEYS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS idempotency_keys (
                                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                idempotency_key VARCHAR(255) NOT NULL UNIQUE,
                                                request_hash VARCHAR(64) NOT NULL,
                                                response_body TEXT,
                                                response_status INTEGER,
                                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                expires_at TIMESTAMP NOT NULL
);

-- =====================================================
-- IDEMPOTENCY KEYS INDEXES
-- =====================================================
CREATE INDEX idx_idempotency_key ON idempotency_keys(idempotency_key);
CREATE INDEX idx_idempotency_expires ON idempotency_keys(expires_at);

-- =====================================================
-- UPDATE TRIGGER FOR products
-- =====================================================
CREATE OR REPLACE FUNCTION update_products_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER products_updated_at_trigger
    BEFORE UPDATE ON products
    FOR EACH ROW
EXECUTE FUNCTION update_products_updated_at();

-- =====================================================
-- UPDATE TRIGGER FOR product_images
-- =====================================================
CREATE OR REPLACE FUNCTION update_product_images_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER product_images_updated_at_trigger
    BEFORE UPDATE ON product_images
    FOR EACH ROW
EXECUTE FUNCTION update_product_images_updated_at();

-- =====================================================
-- UPDATE TRIGGER FOR product_variants
-- =====================================================
CREATE OR REPLACE FUNCTION update_product_variants_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER product_variants_updated_at_trigger
    BEFORE UPDATE ON product_variants
    FOR EACH ROW
EXECUTE FUNCTION update_product_variants_updated_at();

-- =====================================================
-- SEED DATA - Sample Products
-- =====================================================

-- Get category and brand IDs (assuming they exist from previous migrations)
DO $$
    DECLARE
        electronics_cat_id UUID;
        apple_brand_id UUID;
        samsung_brand_id UUID;
        lg_brand_id UUID;
    BEGIN
        -- Get category ID
        SELECT id INTO electronics_cat_id FROM categories WHERE slug = 'electronics' LIMIT 1;

        -- Get brand IDs
        SELECT id INTO apple_brand_id FROM brands WHERE slug = 'apple' LIMIT 1;
        SELECT id INTO samsung_brand_id FROM brands WHERE slug = 'samsung' LIMIT 1;
        SELECT id INTO lg_brand_id FROM brands WHERE slug = 'lg' LIMIT 1;

        -- Only insert if we have valid IDs
        IF electronics_cat_id IS NOT NULL THEN

            -- Apple MacBook Pro 16"
            IF apple_brand_id IS NOT NULL THEN
                INSERT INTO products (
                    name, slug, sku, description, short_description,
                    price, sale_price, cost_price, tax_rate,
                    category_id, brand_id,
                    stock_quantity, low_stock_threshold,
                    weight_grams, dimensions,
                    active, featured, is_digital, requires_shipping,
                    display_order,
                    meta_title, meta_description, meta_keywords
                ) VALUES (
                             'MacBook Pro 16" M3 Max',
                             'macbook-pro-16-m3-max',
                             'APPLE-MBP16-M3MAX-2024',
                             'The most powerful MacBook Pro ever. With the M3 Max chip, up to 128GB unified memory, and stunning Liquid Retina XDR display.',
                             'Premium laptop with M3 Max chip and 16-inch display',
                             2499.99, 2299.99, 1800.00, 16.00,
                             electronics_cat_id, apple_brand_id,
                             15, 5,
                             2100, '35.57 x 24.81 x 1.68 cm',
                             TRUE, TRUE, FALSE, TRUE,
                             1,
                             'MacBook Pro 16" M3 Max - Apple',
                             'Buy the latest MacBook Pro with M3 Max chip. Powerful performance for professionals.',
                             'macbook pro, apple, m3 max, laptop, professional'
                         ) ON CONFLICT (slug) DO NOTHING;
            END IF;

            -- Samsung Galaxy S24 Ultra
            IF samsung_brand_id IS NOT NULL THEN
                INSERT INTO products (
                    name, slug, sku, description, short_description,
                    price, sale_price, cost_price, tax_rate,
                    category_id, brand_id,
                    stock_quantity, low_stock_threshold,
                    weight_grams, dimensions,
                    active, featured, is_digital, requires_shipping,
                    display_order,
                    meta_title, meta_description, meta_keywords
                ) VALUES (
                             'Samsung Galaxy S24 Ultra',
                             'samsung-galaxy-s24-ultra',
                             'SAMSUNG-S24-ULTRA-256GB',
                             'The ultimate smartphone with AI-powered features, 200MP camera, and stunning 6.8" display. Built with titanium.',
                             'Flagship smartphone with AI and 200MP camera',
                             1199.99, 1099.99, 750.00, 16.00,
                             electronics_cat_id, samsung_brand_id,
                             50, 10,
                             232, '16.26 x 7.9 x 0.86 cm',
                             TRUE, TRUE, FALSE, TRUE,
                             2,
                             'Samsung Galaxy S24 Ultra - 256GB',
                             'Experience the future with Galaxy AI. S24 Ultra with 200MP camera and titanium design.',
                             'samsung, galaxy s24, smartphone, android, flagship'
                         ) ON CONFLICT (slug) DO NOTHING;
            END IF;

            -- LG 55" OLED TV
            IF lg_brand_id IS NOT NULL THEN
                INSERT INTO products (
                    name, slug, sku, description, short_description,
                    price, sale_price, cost_price, tax_rate,
                    category_id, brand_id,
                    stock_quantity, low_stock_threshold,
                    weight_grams, dimensions,
                    active, featured, is_digital, requires_shipping,
                    display_order,
                    meta_title, meta_description, meta_keywords
                ) VALUES (
                             'LG C3 55" OLED evo 4K Smart TV',
                             'lg-c3-55-oled-4k-tv',
                             'LG-OLED55C3-2024',
                             'Brilliant picture quality with self-lit OLED pixels. Powered by α9 AI Processor Gen6, Dolby Vision, and webOS.',
                             'Premium OLED TV with stunning 4K picture quality',
                             1499.99, 1299.99, 950.00, 16.00,
                             electronics_cat_id, lg_brand_id,
                             8, 3,
                             18500, '122.8 x 70.6 x 4.48 cm',
                             TRUE, TRUE, FALSE, TRUE,
                             3,
                             'LG C3 55" OLED evo 4K Smart TV',
                             'Experience cinema-quality picture with LG OLED. Self-lit pixels for perfect blacks.',
                             'lg, oled tv, 4k tv, smart tv, c3'
                         ) ON CONFLICT (slug) DO NOTHING;
            END IF;

            -- Generic Products (No Brand)
            INSERT INTO products (
                name, slug, sku, description, short_description,
                price, cost_price, tax_rate,
                category_id,
                stock_quantity, low_stock_threshold,
                weight_grams,
                active, featured, is_digital, requires_shipping,
                display_order
            ) VALUES
                  (
                      'USB-C to HDMI Cable 2m',
                      'usb-c-hdmi-cable-2m',
                      'CABLE-USBC-HDMI-2M',
                      'High-speed USB-C to HDMI cable supporting 4K@60Hz. Perfect for connecting laptops to monitors.',
                      'Premium USB-C to HDMI cable - 2 meters',
                      29.99, 8.00, 16.00,
                      electronics_cat_id,
                      200, 20,
                      150,
                      TRUE, FALSE, FALSE, TRUE,
                      10
                  ),
                  (
                      'Wireless Mouse - Ergonomic Design',
                      'wireless-mouse-ergonomic',
                      'MOUSE-WIRELESS-ERG-001',
                      'Comfortable ergonomic wireless mouse with 2.4GHz connection and long battery life.',
                      'Ergonomic wireless mouse for productivity',
                      39.99, 12.00, 16.00,
                      electronics_cat_id,
                      150, 20,
                      120,
                      TRUE, FALSE, FALSE, TRUE,
                      11
                  )
            ON CONFLICT (slug) DO NOTHING;

        END IF;
    END $$;

-- =====================================================
-- SAMPLE PRODUCT IMAGES
-- =====================================================
DO $$
    DECLARE
        macbook_id UUID;
        s24_id UUID;
        tv_id UUID;
    BEGIN
        -- Get product IDs
        SELECT id INTO macbook_id FROM products WHERE slug = 'macbook-pro-16-m3-max' LIMIT 1;
        SELECT id INTO s24_id FROM products WHERE slug = 'samsung-galaxy-s24-ultra' LIMIT 1;
        SELECT id INTO tv_id FROM products WHERE slug = 'lg-c3-55-oled-4k-tv' LIMIT 1;

        -- MacBook images
        IF macbook_id IS NOT NULL THEN
            INSERT INTO product_images (product_id, image_url, alt_text, is_primary, display_order)
            VALUES
                (macbook_id, '/uploads/products/macbook-pro-16-front.jpg', 'MacBook Pro 16 Front View', TRUE, 0),
                (macbook_id, '/uploads/products/macbook-pro-16-side.jpg', 'MacBook Pro 16 Side View', FALSE, 1),
                (macbook_id, '/uploads/products/macbook-pro-16-keyboard.jpg', 'MacBook Pro 16 Keyboard', FALSE, 2)
            ON CONFLICT DO NOTHING;
        END IF;

        -- Samsung Galaxy S24 images
        IF s24_id IS NOT NULL THEN
            INSERT INTO product_images (product_id, image_url, alt_text, is_primary, display_order)
            VALUES
                (s24_id, '/uploads/products/s24-ultra-titanium.jpg', 'Galaxy S24 Ultra Titanium', TRUE, 0),
                (s24_id, '/uploads/products/s24-ultra-camera.jpg', 'Galaxy S24 Ultra Camera', FALSE, 1),
                (s24_id, '/uploads/products/s24-ultra-display.jpg', 'Galaxy S24 Ultra Display', FALSE, 2)
            ON CONFLICT DO NOTHING;
        END IF;

        -- LG TV images
        IF tv_id IS NOT NULL THEN
            INSERT INTO product_images (product_id, image_url, alt_text, is_primary, display_order)
            VALUES
                (tv_id, '/uploads/products/lg-c3-oled-front.jpg', 'LG C3 OLED Front', TRUE, 0),
                (tv_id, '/uploads/products/lg-c3-oled-side.jpg', 'LG C3 OLED Side Profile', FALSE, 1)
            ON CONFLICT DO NOTHING;
        END IF;
    END $$;

-- =====================================================
-- SAMPLE PRODUCT VARIANTS (Galaxy S24 Storage Options)
-- =====================================================
DO $$
    DECLARE
        s24_id UUID;
    BEGIN
        SELECT id INTO s24_id FROM products WHERE slug = 'samsung-galaxy-s24-ultra' LIMIT 1;

        IF s24_id IS NOT NULL THEN
            INSERT INTO product_variants (
                product_id, sku, name, attribute_type, attribute_value,
                price_adjustment, stock_quantity, active
            ) VALUES
                  (s24_id, 'SAMSUNG-S24-ULTRA-512GB', '512GB Storage', 'Storage', '512GB', 200.00, 30, TRUE),
                  (s24_id, 'SAMSUNG-S24-ULTRA-1TB', '1TB Storage', 'Storage', '1TB', 400.00, 15, TRUE)
            ON CONFLICT (sku) DO NOTHING;
        END IF;
    END $$;

-- =====================================================
-- ANALYTICS VIEW - Product Performance
-- =====================================================
CREATE OR REPLACE VIEW product_analytics AS
SELECT
    p.id,
    p.name,
    p.sku,
    p.price,
    p.sale_price,
    p.stock_quantity,
    p.view_count,
    p.sale_count,
    p.average_rating,
    p.review_count,
    c.name as category_name,
    b.name as brand_name,
    CASE
        WHEN p.sale_price IS NOT NULL AND p.sale_price < p.price
            THEN ((p.price - p.sale_price) / p.price * 100)
        ELSE 0
        END as discount_percentage,
    CASE
        WHEN p.stock_quantity = 0 THEN 'Out of Stock'
        WHEN p.stock_quantity <= p.low_stock_threshold THEN 'Low Stock'
        ELSE 'In Stock'
        END as stock_status,
    p.created_at,
    p.updated_at
FROM products p
         LEFT JOIN categories c ON p.category_id = c.id
         LEFT JOIN brands b ON p.brand_id = b.id
WHERE p.active = TRUE;

-- =====================================================
-- COMMENT ON TABLES
-- =====================================================
COMMENT ON TABLE products IS 'Main products table storing all product information';
COMMENT ON TABLE product_images IS 'Product images with display order';
COMMENT ON TABLE product_variants IS 'Product variations (size, color, storage, etc.)';
COMMENT ON TABLE idempotency_keys IS 'Idempotency keys for preventing duplicate operations';

COMMENT ON COLUMN products.slug IS 'URL-friendly unique identifier';
COMMENT ON COLUMN products.sku IS 'Stock Keeping Unit - unique product identifier';
COMMENT ON COLUMN products.sale_price IS 'Discounted price (if null, no discount)';
COMMENT ON COLUMN products.stock_quantity IS 'Current available stock';
COMMENT ON COLUMN products.low_stock_threshold IS 'Alert when stock falls below this';
COMMENT ON COLUMN products.is_digital IS 'Digital products dont require shipping';
COMMENT ON COLUMN products.view_count IS 'Number of times product was viewed';
COMMENT ON COLUMN products.sale_count IS 'Number of times product was sold';

-- =====================================================
-- GRANT PERMISSIONS (if needed)
-- =====================================================
-- GRANT SELECT, INSERT, UPDATE, DELETE ON products TO app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON product_images TO app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON product_variants TO app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON idempotency_keys TO app_user;