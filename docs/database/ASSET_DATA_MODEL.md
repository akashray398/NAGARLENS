# NAGARLENS — Asset Database Data Model

> **Database Version:** PostgreSQL 16 + PostGIS 3.4 (Backend) / Room v2 (Android)  

---

## 1. Relational Schema & Tables

### 1. `assets` Table
- `id` (UUID, Primary Key)
- `asset_code` (VARCHAR(32), Unique)
- `asset_category` (VARCHAR(32))
- `lifecycle_status` (VARCHAR(32), Default: `ACTIVE`)
- `verification_status` (VARCHAR(32), Default: `UNVERIFIED`)
- `location` (GEOMETRY(Point, 4326), PostGIS Spatial Index)
- `latitude` (DOUBLE PRECISION)
- `longitude` (DOUBLE PRECISION)
- `altitude_meters` (FLOAT, Nullable)
- `description` (TEXT, Nullable)
- `version` (INTEGER, Default: 1 for optimistic locking)
- `created_by` (VARCHAR(64))
- `created_timestamp_utc` (BIGINT)
- `updated_timestamp_utc` (BIGINT)

### 2. `asset_location_history` Table
- `id` (UUID, Primary Key)
- `asset_id` (UUID, Foreign Key -> `assets.id`)
- `previous_latitude` (DOUBLE PRECISION)
- `previous_longitude` (DOUBLE PRECISION)
- `new_latitude` (DOUBLE PRECISION)
- `new_longitude` (DOUBLE PRECISION)
- `reason` (TEXT)
- `actor_id` (VARCHAR(64))
- `recorded_timestamp_utc` (BIGINT)

### 3. `asset_identifier_registry` Table
- `id` (UUID, Primary Key)
- `asset_id` (UUID, Foreign Key -> `assets.id`)
- `identifier_type` (VARCHAR(32): `QR_CODE`, `NFC_TAG`, `MUNICIPAL_ID`)
- `identifier_value` (VARCHAR(128))
- `is_active` (BOOLEAN, Default: TRUE)
- `created_timestamp_utc` (BIGINT)
- `revoked_timestamp_utc` (BIGINT, Nullable)

### 4. `asset_evidence_links` Table
- `id` (UUID, Primary Key)
- `asset_id` (UUID, Foreign Key -> `assets.id`)
- `evidence_id` (VARCHAR(64))
- `relationship_type` (VARCHAR(32): `INITIAL_REGISTRATION`, `CONDITION_OBSERVATION`, `DAMAGE_EVIDENCE`, `MAINTENANCE_EVIDENCE`)
- `created_by` (VARCHAR(64))
- `created_timestamp_utc` (BIGINT)

### 5. `asset_verification_records` Table
- `id` (UUID, Primary Key)
- `asset_id` (UUID, Foreign Key -> `assets.id`)
- `decision` (VARCHAR(32): `APPROVED`, `REJECTED`, `CORRECTION_REQUESTED`)
- `reason` (TEXT, Nullable)
- `reviewer_id` (VARCHAR(64))
- `decision_timestamp_utc` (BIGINT)
