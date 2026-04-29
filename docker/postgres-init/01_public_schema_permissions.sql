-- =====================================================
-- Grant schema privileges to app role (runs as superuser during init)
-- =====================================================
-- Grant USAGE and CREATE on public schema to allow Flyway migrations
GRANT USAGE, CREATE ON SCHEMA public TO tonnymuchui;

-- Grant existing tables/sequences to app role
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO tonnymuchui;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO tonnymuchui;

-- Set default privileges for future tables/sequences created by this role
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO tonnymuchui;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO tonnymuchui;

-- Grant database connect and temp permissions
GRANT CONNECT, TEMP ON DATABASE pnq TO tonnymuchui;

