-- Create roles table
CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name VARCHAR(50) NOT NULL UNIQUE,
                       description VARCHAR(255),
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       version BIGINT DEFAULT 0
);

-- Create users table
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(128) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       first_name VARCHAR(64) NOT NULL,
                       last_name VARCHAR(64) NOT NULL,
                       phone VARCHAR(20),
                       avatar_url TEXT,
                       enabled BOOLEAN NOT NULL DEFAULT false,
                       email_verified BOOLEAN NOT NULL DEFAULT false,
                       account_locked BOOLEAN NOT NULL DEFAULT false,
                       failed_login_attempts INTEGER NOT NULL DEFAULT 0,
                       last_login TIMESTAMP,
                       provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
                       provider_id VARCHAR(255),
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       version BIGINT DEFAULT 0
);

-- Create user_roles junction table
CREATE TABLE user_roles (
                            user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

-- Create verification_tokens table
CREATE TABLE verification_tokens (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     token VARCHAR(255) NOT NULL UNIQUE,
                                     user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                     expiry_date TIMESTAMP NOT NULL,
                                     verified BOOLEAN NOT NULL DEFAULT false,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     version BIGINT DEFAULT 0
);

-- Create password_reset_tokens table
CREATE TABLE password_reset_tokens (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       token VARCHAR(255) NOT NULL UNIQUE,
                                       user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       expiry_date TIMESTAMP NOT NULL,
                                       used BOOLEAN NOT NULL DEFAULT false,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       version BIGINT DEFAULT 0
);

-- Create addresses table
CREATE TABLE addresses (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                           address_line1 VARCHAR(255) NOT NULL,
                           address_line2 VARCHAR(255),
                           city VARCHAR(100) NOT NULL,
                           state_province VARCHAR(100),
                           postal_code VARCHAR(20) NOT NULL,
                           country VARCHAR(100) NOT NULL,
                           phone_number VARCHAR(20),
                           is_default BOOLEAN NOT NULL DEFAULT false,
                           address_type VARCHAR(20) NOT NULL DEFAULT 'SHIPPING',
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           version BIGINT DEFAULT 0
);

-- Create indexes
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_provider ON users(provider, provider_id);
CREATE INDEX idx_user_enabled ON users(enabled);

CREATE INDEX idx_token ON verification_tokens(token);
CREATE INDEX idx_user_id ON verification_tokens(user_id);

CREATE INDEX idx_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_reset_user_id ON password_reset_tokens(user_id);

CREATE INDEX idx_address_user ON addresses(user_id);

-- Insert default roles
INSERT INTO roles (id, name, description) VALUES
                                              (gen_random_uuid(), 'ADMIN', 'Administrator with full access'),
                                              (gen_random_uuid(), 'CUSTOMER', 'Regular customer user'),
                                              (gen_random_uuid(), 'STAFF', 'Staff member with limited access'),
                                              (gen_random_uuid(), 'MANAGER', 'Manager with elevated access');

-- Create admin user (password: Admin123!)
-- You should change this after first login
INSERT INTO users (id, email, password, first_name, last_name, enabled, email_verified, provider) VALUES
    (gen_random_uuid(),
     'admin@picknquicks.com',
     '$2a$10$XQz9YmjmVzJ8QBHxVHKxR.wX7qLxO5lNqJmP7hKNZ0qGFNLQ5xRya', -- Admin123!
     'Admin',
     'User',
     true,
     true,
     'LOCAL');

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@picknquicks.com' AND r.name = 'ADMIN';