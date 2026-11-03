# Database & Spatial Data Strategy

> **System:** NAGARLENS Infrastructure Intelligence Network  
> **Backend Database:** PostgreSQL 16 + PostGIS 3.4  
> **Mobile Database:** Android Room 2.6 (SQLite + SQLCipher)  
> **Spatial Reference System:** WGS84 (EPSG:4326) / Web Mercator (EPSG:3857)  
> **Document Version:** 1.0.0  
> **Status:** Strategy Baseline  

---

## 1. Overview & Data Architecture

NAGARLENS manages two distinct data storage domains:
1. **Server-Side Geospatial Database (PostgreSQL + PostGIS):** Handles the authoritative spatial asset network (drainage lines, catch basins, culverts), historical observation records, AI defect analysis predictions, and user verification records.
2. **Mobile Client Database (Android Room):** Manages local spatial caches, offline observation upload queues, user sessions, and local evidence media metadata.

---

## 2. Server Schema Design (PostgreSQL + PostGIS)

All spatial geometries are stored using PostGIS geometry types (`GEOMETRY(Point, 4326)` or `GEOMETRY(LineString, 4326)`).

```text
               +-----------------------+
               |      ast_assets       |
               +-----------------------+
               | id (UUID, PK)         |
               | asset_code (VARCHAR)  |
               | asset_type (ENUM)     |
               | geom (GEOMETRY Point) |
               +-----------+-----------+
                           | 1
                           |
                           | N
               +-----------v-----------+
               |   obs_observations    |
               +-----------------------+
               | id (UUID, PK)         |
               | asset_id (UUID, FK)   |
               | client_timestamp (TZ) |
               | location (GEOMETRY)   |
               | sha256_hash (VARCHAR) |
               +-----------+-----------+
                           | 1
                           |
                           | 1..N
               +-----------v-----------+
               |  obs_evidence_media   |
               +-----------------------+
               | id (UUID, PK)         |
               | observation_id (FK)   |
               | s3_bucket_key (TEXT)  |
               | media_type (ENUM)     |
               +-----------+-----------+
                           | 1
                           |
                           | 0..N
               +-----------v-----------+
               |   ai_analysis_results |
               +-----------------------+
               | id (UUID, PK)         |
               | observation_id (FK)   |
               | defect_score (NUMERIC)|
               | classification (ENUM) |
               | blurred_s3_key (TEXT) |
               +-----------------------+
```

### 2.1 Core Tables & Spatial Columns

#### Asset Entity Table (`ast_assets`)
- `id`: `UUID` (Primary Key, Default `gen_random_uuid()`)
- `asset_code`: `VARCHAR(64)` (Unique natural identifier, e.g., `DRAIN-WARD12-0401`)
- `asset_type`: `VARCHAR(32)` (`CATCH_BASIN`, `STORM_DRAIN_LINE`, `MANHOLE`, `CULVERT`, `OUTFALL`)
- `location`: `GEOMETRY(Point, 4326)` (Spatial point centroid)
- `line_network`: `GEOMETRY(LineString, 4326)` (Optional spatial line vector for channels)
- `created_at`: `TIMESTAMPTZ` (Record creation time)

#### Observation Log Table (`obs_observations`)
- `id`: `UUID` (Primary Key, generated client-side)
- `asset_id`: `UUID` (Foreign Key referencing `ast_assets.id`, nullable if unlinked)
- `observer_user_id`: `UUID` (Foreign Key referencing observer)
- `client_timestamp`: `TIMESTAMPTZ` (Exact time captured on mobile device)
- `ingest_timestamp`: `TIMESTAMPTZ` (Time received at backend server)
- `location`: `GEOMETRY(Point, 4326)` (Recorded GNSS location)
- `altitude`: `DOUBLE PRECISION`
- `heading_degrees`: `REAL` (Compass orientation angle)
- `horizontal_accuracy_m`: `REAL` (GNSS precision radius in meters)
- `sha256_hash`: `VARCHAR(64)` (Cryptographic media/payload digest)
- `sync_status`: `VARCHAR(32)` (`PENDING_AI`, `PROCESSED`, `FLAGGED`, `VERIFIED`)

#### AI Analysis Results Table (`ai_analysis_results`)
- `id`: `UUID` (Primary Key)
- `observation_id`: `UUID` (Foreign Key referencing `obs_observations.id`)
- `model_version`: `VARCHAR(32)` (e.g., `yolo-drain-v2.1`)
- `defect_type`: `VARCHAR(32)` (`CLEAN`, `SILTATION`, `TRASH_ACCUMULATION`, `STRUCTURAL_CRACK`)
- `severity_index`: `NUMERIC(3,2)` (0.00 to 1.00 score)
- `confidence_score`: `NUMERIC(3,2)`
- `privacy_blurred_s3_key`: `TEXT` (Path to processed media with obscured PII)

---

## 3. Spatial Indexing & Query Optimization

To achieve fast spatial queries across tens of thousands of urban asset nodes:

1. **GiST Spatial Indexes:**
   ```sql
   CREATE INDEX idx_ast_assets_location ON ast_assets USING GIST (location);
   CREATE INDEX idx_obs_observations_location ON obs_observations USING GIST (location);
   ```

2. **Proximity Nearest-Asset Join Query:**
   Find closest drainage asset within 10 meters of an observation point:
   ```sql
   SELECT a.id, a.asset_code, ST_Distance(a.location::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography) AS distance_meters
   FROM ast_assets a
   WHERE ST_DWithin(a.location::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, 10.0)
   ORDER BY distance_meters ASC
   LIMIT 1;
   ```

---

## 4. Mobile SQLite / Room Schema Strategy

The Android client maintains an offline-first Room database (`nagarlens_offline.db`).

### Room Entities
- `LocalObservationEntity`: Stores captured observations, client UUID, location, metadata, sync status (`PENDING_SYNC`, `SYNCING`, `SYNCED`, `FAILED`).
- `LocalMediaQueueEntity`: Manages photo/video file references, binary upload progress, chunk offset, and SHA-256 local hash.
- `LocalAssetCacheEntity`: Caches spatial assets within the inspector's active ward for offline display on map tiles.

---

## 5. Schema Migration & Versioning

- **Backend Migrations:** Managed using **Alembic**. Auto-generated migrations MUST be inspected and tested against a PostGIS-enabled test container before merging.
- **Android Migrations:** Managed via Room explicit migration scripts (`Migration(1, 2)`). Destructive fallback (`fallbackToDestructiveMigration`) is strictly disabled in production builds to prevent field data loss.
