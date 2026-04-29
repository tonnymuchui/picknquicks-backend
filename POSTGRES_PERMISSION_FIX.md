# PostgreSQL Schema Permission Fix

## Problem
The migration `V6__create_products.sql` failed with:
```
[42501] ERROR: permission denied for schema public
```

This happens because the app role (`tonnymuchui`) lacked `USAGE` and `CREATE` privileges on the `public` schema.

## Root Cause
- Docker init scripts (`/docker-entrypoint-initdb.d`) only run on **first container init** with a fresh volume
- Existing volumes skip init scripts on restart
- Self-grant blocks in migrations cannot escalate privileges (migration users are not superusers)

## Solution Applied

### 1. **Removed Ineffective Block** ✓
   - Deleted the `DO $$` self-grant block from `V6__create_products.sql`
   - Migrations should never attempt privilege escalation

### 2. **Updated Bootstrap Script** ✓
   - Modified `01_public_schema_permissions.sql` to use hardcoded role name `tonnymuchui`
   - Runs as **superuser** during container init (only on fresh volumes)
   - Properly grants schema permissions

### 3. **Created One-Time Remediation Script** ✓
   - New file: `00_fix_schema_privileges.sql` (runs before other migrations alphabetically)
   - Must be executed as superuser on **existing database only**
   - Applies missing privileges to enable Flyway migrations

## How to Fix Your Existing Database

### Option A: Reset Database (Cleanest)
```bash
# Stop containers
docker-compose -f docker/compose.yaml down

# Remove stale postgres volume
docker volume rm picknquicks_postgres_data

# Start fresh (init scripts will run automatically)
docker-compose -f docker/compose.yaml up -d

# Application should start and migrations will succeed
```

### Option B: One-Time Privilege Fix (Keep Data)
Connect to your existing database **as superuser** and run:

```bash
# From your host machine (if Docker postgres exposed on port 5433)
psql -h localhost -p 5433 -U postgres -d pnq -f docker/postgres-init/00_fix_schema_privileges.sql

# If using Docker exec (postgres running in container):
docker exec picknquicks-postgres psql -U postgres -d pnq -f /docker-entrypoint-initdb.d/00_fix_schema_privileges.sql

# Then restart app to trigger Flyway
docker-compose -f docker/compose.yaml restart backend
```

## Verification

After applying fix, verify privileges are granted:

```sql
-- Connect as superuser
psql -h localhost -p 5433 -U postgres -d pnq

-- Check schema privileges
SELECT nspname, nspacl FROM pg_namespace WHERE nspname = 'public';

-- Check role exists and has needed privileges
SELECT rolname, usecanlogin, usecreatedb FROM pg_roles WHERE rolname = 'tonnymuchui';
```

Expected output should show `tonnymuchui` has create privileges on `public` schema.

## Files Modified
- `src/main/resources/db/migration/V6__create_products.sql` - removed self-grant block
- `docker/postgres-init/01_public_schema_permissions.sql` - hardcoded role name
- `docker/postgres-init/00_fix_schema_privileges.sql` - **NEW** remediation script

## Best Practices Going Forward
1. ✅ Never use `DO $$...END $$` self-grant blocks in migrations
2. ✅ Keep all privilege setup in bootstrap scripts that run as superuser
3. ✅ Use explicit role names in grant statements (not `current_user` dynamic grants)
4. ✅ Document which scripts must run as superuser vs app role
5. ✅ For team projects, document: "On existing databases, run `00_fix_schema_privileges.sql` as superuser"

## Next Steps
1. **Choose Option A or B above** to fix your database
2. **Restart the application** - Flyway will attempt V6 migration
3. **Verify** the products table and related objects are created

