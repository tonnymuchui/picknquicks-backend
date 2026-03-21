CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    slug VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    image_url VARCHAR(255),
    icon_url VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true,
    display_order INTEGER NOT NULL DEFAULT 0,
    meta_title VARCHAR(128),
    meta_description VARCHAR(255),
    meta_keywords VARCHAR(255),
    parent_id UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    version BIGINT,
    CONSTRAINT uk_category_slug UNIQUE (slug),
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_category_slug ON categories(slug);
CREATE INDEX IF NOT EXISTS idx_category_parent ON categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_category_active ON categories(active);
CREATE INDEX IF NOT EXISTS idx_category_display_order ON categories(display_order);

