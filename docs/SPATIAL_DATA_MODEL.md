# NAGARLENS — Spatial Data Model & Schema Specification

> **Document Version:** 1.0.0  
> **Status:** Final Data Architecture  
> **Coordinate Reference System:** WGS 84 (EPSG:4326)  

---

## 1. Core Spatial Entities & Attributes

### 1.1 Infrastructure Asset (`ast_assets` / `AssetEntity`)
- `id`: `UUID` (Canonical Primary Key, immutable)
- `asset_code`: `VARCHAR(64)` (Operational code, e.g. `NAG-CB-000104`)
- `asset_category`: `VARCHAR(32)` (`CATCH_BASIN`, `STORM_WATER_DRAIN`, `MANHOLE`, `CULVERT`)
- `latitude`: `DOUBLE PRECISION` (-90.0 to +90.0°)
- `longitude`: `DOUBLE PRECISION` (-180.0 to +180.0°)
- `altitude_meters`: `REAL` (Optional altitude above sea level)
- `location`: `GEOMETRY(Point, 4326)` (PostGIS spatial centroid)

### 1.2 Asset Location History (`asset_location_history`)
- `id`: `UUID` (Primary Key)
- `asset_id`: `UUID` (Foreign Key)
- `previous_latitude`, `previous_longitude`: `DOUBLE PRECISION`
- `new_latitude`, `new_longitude`: `DOUBLE PRECISION`
- `reason`: `TEXT` (Audit reason for coordinate correction)
- `actor_id`: `VARCHAR(64)` (Inspector ID)
- `recorded_timestamp_utc`: `BIGINT`

---

## 2. PostGIS Spatial Queries & Indexing

```sql
-- Spatial GiST Index on PostGIS Point Geometry
CREATE INDEX idx_ast_assets_location ON assets USING GIST (location);

-- PostGIS Nearest Asset Radius Query (ST_DWithin)
SELECT id, asset_code, latitude, longitude,
       ST_Distance(location::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography) AS distance_meters
FROM assets
WHERE ST_DWithin(location::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radius_meters)
ORDER BY distance_meters ASC;
```
