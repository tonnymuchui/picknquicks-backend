-- =====================================================
-- PostgreSQL Schema Privilege Fix (Superuser Only)
-- =====================================================
-- This script must be run by a superuser (e.g., 'postgres' role)
-- to fix permission issues on existing databases.
--
-- Run once on existing database to grant proper privileges to app role:
--   psql -U postgres -d pnq -f 00_fix_schema_privileges.sql
--
-- After running this, migrations will succeed.
-- =====================================================

-- 1. Verify current user is superuser or has sufficient privileges
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_roles
            WHERE rolname = current_user AND rolsuper = true
        ) THEN
            RAISE WARNING 'Warning: Current user % is not a superuser. Script may fail.', current_user;
        END IF;
    END $$;

-- 2. Grant schema privileges to app role
GRANT USAGE, CREATE ON SCHEMA public TO tonnymuchui;

-- 3. Grant existing tables and sequences
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO tonnymuchui;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO tonnymuchui;

-- 4. Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO tonnymuchui;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO tonnymuchui;

-- 5. Grant database-level permissions
GRANT CONNECT, TEMP ON DATABASE pnq TO tonnymuchui;

-- 6. Verify the grants took effect
DO $$
    DECLARE
        has_usage BOOLEAN;
        has_create BOOLEAN;
    BEGIN
        SELECT (aclexplode(nspacl)).privilege_type = 'USAGE' INTO has_usage
        FROM pg_namespace
        WHERE nspname = 'public'
        LIMIT 1;

        SELECT (aclexplode(nspacl)).privilege_type = 'CREATE' INTO has_create
        FROM pg_namespace
        WHERE nspname = 'public'
        LIMIT 1;

        RAISE NOTICE 'Schema public privileges for tonnymuchui:';
        RAISE NOTICE '  - USAGE: %', COALESCE(has_usage, FALSE);
        RAISE NOTICE '  - CREATE: %', COALESCE(has_create, FALSE);
    END $$;

RAISE NOTICE 'Schema privileges fix completed. Flyway migrations should now succeed.';

