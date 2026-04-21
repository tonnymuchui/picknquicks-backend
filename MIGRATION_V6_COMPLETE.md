# PostgreSQL Product Tables Migration - COMPLETE ✅

## Problem Resolved

**Original Error:** `[42501] ERROR: permission denied for schema public`

This error occurred when running migration `V6__create_products.sql` which creates product-related tables.

## Root Cause
1. User role (`tonnymuchui`) lacked `USAGE` and `CREATE` privileges on the `public` schema
2. Self-grant blocks in migrations cannot elevate privileges (app users aren't superusers)
3. Docker init scripts only run on fresh volumes; existing volumes skip them

## Solutions Applied

### 1. **Fixed Schema Permissions** ✅
- Updated `docker/postgres-init/01_public_schema_permissions.sql` to grant hardcoded privileges to `tonnymuchui`
- Removed ineffective self-grant `DO $$` block from `V6__create_products.sql`
- Created `docker/postgres-init/00_fix_schema_privileges.sql` for one-time remediation

### 2. **Fixed Environment Variable Loading** ✅
- Copied `.env` to `docker/` directory so `docker compose` can find it
- Updated `docker/compose.yaml` to reference `.env` directly

### 3. **Added Missing `version` Column** ✅
- `BaseEntity` extends `@Version` annotation for optimistic locking
- Added `version BIGINT DEFAULT 0` to:
  - `products`
  - `product_images`
  - `product_variants`
- Migration now passes schema validation

## Verification

### Migration Status
```sql
SELECT * FROM flyway_schema_history WHERE version = '6';
-- Result: V6 migration SUCCESSFUL (installed_rank=6, success=t)
```

### Tables Created
```
 tablename     
------------------
 product_images
 product_variants
 products
```

### Schema Validation
✅ Hibernate schema validation **PASSED** - all columns including `version` are present

## Files Modified

1. **src/main/resources/db/migration/V6__create_products.sql**
   - Removed self-grant DO block (lines 10-17)
   - Added `version BIGINT DEFAULT 0` to products, product_images, product_variants tables

2. **docker/postgres-init/01_public_schema_permissions.sql**
   - Replaced dynamic `current_user` with hardcoded role name `tonnymuchui`

3. **docker/compose.yaml**
   - Changed env_file from `../.env` to `.env`
   - `.env` file copied to docker/ directory

4. **src/main/resources/application.properties**
   - Added `spring.data.elasticsearch.cluster-nodes=localhost:9200`

5. **NEW: docker/postgres-init/00_fix_schema_privileges.sql**
   - One-time remediation script for existing databases

## Current Application Status

**Database & Migration:** ✅ SUCCESSFUL
- Flyway V6 migration completed
- All product tables created with correct schema
- Hibernate validation passed

**Application Startup:** ⚠️ Elasticsearch Configuration Issue
- This is a separate issue unrelated to the database migration
- Elasticsearch service not configured/running
- Can be resolved by:
  - Running Elasticsearch container
  - Disabling Elasticsearch integration
  - Providing proper configuration

## How to Run

```bash
cd docker
docker compose -f compose.yaml up -d
```

The database migration will run automatically via Flyway on application startup.

## For Future Migrations

**Best Practices Applied:**
1. ✅ All permission setup in bootstrap scripts (runs as superuser)
2. ✅ No self-grant blocks in migrations
3. ✅ Explicit role names (not `current_user`)
4. ✅ All tables include `version` column for BaseEntity
5. ✅ Environment variables properly configured


