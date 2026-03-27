CREATE TABLE IF NOT EXISTS brands (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        name VARCHAR(128) NOT NULL UNIQUE,
                        slug VARCHAR(150) NOT NULL UNIQUE,
                        description VARCHAR(1000),
                        logo_url VARCHAR(255),
                        banner_url VARCHAR(255),
                        website_url VARCHAR(255),
                        country_of_origin VARCHAR(64),
                        active BOOLEAN NOT NULL DEFAULT true,
                        featured BOOLEAN NOT NULL DEFAULT false,
                        display_order INTEGER NOT NULL DEFAULT 0,
                        product_count BIGINT NOT NULL DEFAULT 0,
                        meta_title VARCHAR(128),
                        meta_description VARCHAR(255),
                        meta_keywords VARCHAR(255),
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_brand_slug ON brands(slug);
CREATE INDEX IF NOT EXISTS idx_brand_active ON brands(active);
CREATE INDEX IF NOT EXISTS idx_brand_featured ON brands(featured);
CREATE INDEX IF NOT EXISTS idx_brand_display_order ON brands(display_order);

INSERT INTO brands (id, name, slug, description, country_of_origin, active, featured, display_order) VALUES
                                                                                                         ('a0000000-0000-0000-0000-000000000001', 'Apple', 'apple', 'Think Different - Premium consumer electronics and software', 'USA', true, true, 0),
                                                                                                         ('a0000000-0000-0000-0000-000000000002', 'Samsung', 'samsung', 'Global leader in smartphones, displays, and home appliances', 'South Korea', true, true, 1),
                                                                                                         ('a0000000-0000-0000-0000-000000000003', 'LG', 'lg', 'Life''s Good - Premium displays, home appliances, and electronics', 'South Korea', true, true, 2),
                                                                                                         ('a0000000-0000-0000-0000-000000000004', 'Dell', 'dell', 'Technology solutions for business and personal computing', 'USA', true, true, 3),
                                                                                                         ('a0000000-0000-0000-0000-000000000005', 'HP', 'hp', 'Computing and printing solutions for home and business', 'USA', true, false, 4),
                                                                                                         ('a0000000-0000-0000-0000-000000000006', 'Lenovo', 'lenovo', 'Innovative PCs, tablets, and smart devices', 'China', true, false, 5),
                                                                                                         ('a0000000-0000-0000-0000-000000000007', 'Sony', 'sony', 'Entertainment and electronics innovator', 'Japan', true, true, 6),
                                                                                                         ('a0000000-0000-0000-0000-000000000008', 'Microsoft', 'microsoft', 'Software, cloud computing, and hardware solutions', 'USA', true, false, 7),
                                                                                                         ('a0000000-0000-0000-0000-000000000009', 'ASUS', 'asus', 'Computer hardware and electronics manufacturer', 'Taiwan', true, false, 8),
                                                                                                         ('a0000000-0000-0000-0000-000000000010', 'Acer', 'acer', 'Affordable computing solutions', 'Taiwan', true, false, 9)
ON CONFLICT (slug) DO NOTHING;
