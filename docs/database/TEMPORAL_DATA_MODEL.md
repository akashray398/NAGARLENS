# NAGARLENS — Temporal Database Data Model

> **Database Version:** PostgreSQL 16 + PostGIS 3.4 / Room v3 (Android)  

---

## 1. Relational Tables

### 1. `observations` Table
- `id` (UUID, Primary Key)
- `asset_id` (UUID, Foreign Key -> `assets.id`)
- `observation_type` (VARCHAR(32))
- `condition_category` (VARCHAR(32))
- `condition_severity` (VARCHAR(32): `UNKNOWN`, `LOW`, `MODERATE`, `HIGH`, `CRITICAL`)
- `description` (TEXT, Nullable)
- `observed_at_utc` (BIGINT, UTC timestamp when recorded in field)
- `recorded_at_utc` (BIGINT, UTC timestamp when ingested)
- `observed_by` (VARCHAR(64))
- `latitude` (DOUBLE PRECISION)
- `longitude` (DOUBLE PRECISION)
- `location_accuracy_meters` (FLOAT, Nullable)
- `source` (VARCHAR(32), Default: `FIELD_INSPECTION`)

### 2. `observation_evidence_links` Table
- `id` (UUID, Primary Key)
- `observation_id` (UUID, Foreign Key -> `observations.id`)
- `evidence_id` (VARCHAR(64))
- `relationship_type` (VARCHAR(32))
- `linked_at_utc` (BIGINT)

### 3. `interventions` Table
- `id` (UUID, Primary Key)
- `asset_id` (UUID, Foreign Key -> `assets.id`)
- `intervention_type` (VARCHAR(32): `DRAIN_CLEANING`, `SILT_REMOVAL`, `STRUCTURAL_REPAIR`, etc.)
- `title` (VARCHAR(128))
- `description` (TEXT, Nullable)
- `status` (VARCHAR(32): `PROPOSED`, `APPROVED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `FAILED`)
- `planned_at_utc` (BIGINT, Nullable)
- `started_at_utc` (BIGINT, Nullable)
- `completed_at_utc` (BIGINT, Nullable)
- `recorded_by` (VARCHAR(64))
- `created_at_utc` (BIGINT)
- `updated_at_utc` (BIGINT)

### 4. `intervention_evidence_links` Table
- `id` (UUID, Primary Key)
- `intervention_id` (UUID, Foreign Key -> `interventions.id`)
- `evidence_id` (VARCHAR(64))
- `relationship_type` (VARCHAR(32): `BEFORE_ACTION`, `IN_PROGRESS`, `AFTER_ACTION`)
- `linked_at_utc` (BIGINT)

### 5. `outcome_verifications` Table
- `id` (UUID, Primary Key)
- `intervention_id` (UUID, Foreign Key -> `interventions.id`)
- `asset_id` (UUID, Foreign Key -> `assets.id`)
- `verification_observation_id` (UUID, Foreign Key -> `observations.id`)
- `outcome` (VARCHAR(32): `IMPROVED`, `PARTIALLY_IMPROVED`, `UNCHANGED`, `DETERIORATED`, `INCONCLUSIVE`)
- `description` (TEXT, Nullable)
- `verified_at_utc` (BIGINT)
- `verified_by` (VARCHAR(64))
- `created_at_utc` (BIGINT)
