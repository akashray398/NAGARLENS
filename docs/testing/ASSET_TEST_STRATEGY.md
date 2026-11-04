# NAGARLENS — Asset Test Strategy & Coverage Matrix

> **Testing Scope:** Backend Python Pytest & Android JUnit Test Suites  

---

## 1. Test Coverage Goals

- **Asset Identity & Code Uniqueness:** Test UUID persistence, asset code generation, and duplicate prevention.
- **Geospatial & Spatial Queries:** Test PostGIS point queries, viewport filtering, and GeoJSON coordinate ordering (`longitude, latitude`).
- **Verification & Lifecycle:** Test allowed state transitions and decision auditing.
- **Offline Sync & Room Migration:** Test Room database migration `1 -> 2`, local draft creation, and WorkManager sync.
