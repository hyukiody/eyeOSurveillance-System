# EyeO Platform - Database Migrations

Modular database migration system for the convergent platform architecture.

## Directory Structure

```
ops/migrations/
├── identity/          # Identity service migrations
│   ├── 001_initial_schema.sql
│   ├── 002_sessions_audit.sql
│   └── 003_seed_data.sql
└── stream/            # Stream processing migrations
    ├── 001_event_sources.sql
    ├── 002_pattern_detection.sql
    ├── 003_legacy_compatibility.sql
    └── 004_seed_data.sql
```

## Migration Naming Convention

Format: `{number}_{description}.sql`
- **Number**: 3-digit sequential number (001, 002, 003...)
- **Description**: Snake_case description of migration purpose

## Execution Order

### Identity Database (teraapi_identity)
1. **001_initial_schema.sql** - RBAC foundation (roles, users, users_roles)
2. **002_sessions_audit.sql** - JWT tracking and security logging
3. **003_seed_data.sql** - Default admin user and roles

### Stream Database (teraapi_stream)
1. **001_event_sources.sql** - Edge devices and detection events
2. **002_pattern_detection.sql** - Pattern matching tables
3. **003_legacy_compatibility.sql** - Backward compatibility
4. **004_seed_data.sql** - Default event patterns

## Running Migrations

### Docker (Automatic)
Migrations run automatically when containers start via docker-compose.yml

### Manual Execution
```bash
# Identity database
mysql -u identity_user -p teraapi_identity < ops/migrations/identity/001_initial_schema.sql
mysql -u identity_user -p teraapi_identity < ops/migrations/identity/002_sessions_audit.sql
mysql -u identity_user -p teraapi_identity < ops/migrations/identity/003_seed_data.sql

# Stream database
mysql -u stream_user -p teraapi_stream < ops/migrations/stream/001_event_sources.sql
mysql -u stream_user -p teraapi_stream < ops/migrations/stream/002_pattern_detection.sql
mysql -u stream_user -p teraapi_stream < ops/migrations/stream/003_legacy_compatibility.sql
mysql -u stream_user -p teraapi_stream < ops/migrations/stream/004_seed_data.sql
```

## Migration Guidelines

### DO
- ✅ Use sequential numbering
- ✅ Include rollback scripts when possible
- ✅ Test migrations on dev environment first
- ✅ Document schema changes in migration comments
- ✅ Use transactions for data migrations

### DON'T
- ❌ Modify existing migration files
- ❌ Skip migration numbers
- ❌ Include production passwords in migrations
- ❌ Mix DDL and DML in same file (when possible)

## Schema Design Principles

Based on CaCTUs architecture specification:

### Security
- **Fixed Role Enum**: Only 3 roles (ADMIN, USER, DEVICE)
- **Referential Integrity**: Foreign keys enforce relationships
- **Audit Trail**: Timestamps on all tables
- **Blind Storage**: Stream DB doesn't contain video content

### Performance
- **Proper Indexing**: All foreign keys and search columns indexed
- **InnoDB Engine**: ACID compliance and row-level locking
- **UTF8MB4**: Full Unicode support

### Compliance
- **GDPR/LGPD**: Audit logs for data access
- **Data Retention**: Configurable via status fields
- **Granular Deletion**: Foreign key cascades

## Rollback Strategy

To rollback a migration:
1. Create a new migration with reverse operations
2. Never delete or modify existing migrations
3. Document the rollback reason in comments

Example:
```sql
-- Migration: 005_rollback_feature_x.sql
-- Reason: Feature X caused performance issues
DROP TABLE IF EXISTS feature_x_table;
```

## Environment-Specific Migrations

- **Development**: All migrations run automatically
- **Staging**: Review required before execution
- **Production**: Change control process + backup mandatory

## Version Tracking

Track applied migrations in application metadata:

```sql
CREATE TABLE schema_migrations (
    version VARCHAR(20) PRIMARY KEY,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Related Documentation

- [Database Schema](../../docs/database-schema.md)
- [CaCTUs Architecture](../../ARCHITECTURE.md)
- [Security Model](../../SECURITY.md)
