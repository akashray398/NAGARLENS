# NAGARLENS — Relational & PostGIS Database Architecture

> **Document Version:** 1.0.0  
> **Status:** Production Database Architecture  
> **Database Engine:** PostgreSQL 16 + PostGIS 3.4  
> **ORM Layer:** Async SQLAlchemy 2.0 + GeoAlchemy2  

---

## 1. Cloud Database Schema Design

The cloud datastore consists of 8 core tables with PostGIS geometry types (`GEOMETRY(Point, 4326)`):

```text
  ast_assets (Asset ID, Code, Category, WGS84 Point Geometry)
      ├── asset_location_history (Location correction audit log)
      ├── asset_identifier_registry (Physical QR / NFC tag tokens)
      ├── asset_evidence_links (Evidence-to-Asset relationships)
      └── asset_verification_records (Reviewer decision log)

  obs_observations (Observation ID, Capture timestamp, SHA-256 hash, Location)
      └── obs_evidence_media (S3 media object key, MIME type)

  ai_predictions (Assessment ID, Defect Category, Confidence, Severity Index)

  inc_incidents (Incident ID, Priority, Status, Reported By)
```

---

## 2. Alembic Migration Strategy

- Migrations are versioned in `backend/migrations/versions/`.
- `0001_init_postgis.py`: Enables PostGIS extension (`CREATE EXTENSION IF NOT EXISTS postgis`).
- `0002_add_asset_tables.py`: Creates `assets`, `asset_location_history`, `asset_identifier_registry`, `asset_evidence_links`.
- `0003_add_temporal_tables.py`: Creates `observations`, `interventions`, `outcome_verifications`.
