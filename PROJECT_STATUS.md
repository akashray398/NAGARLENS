# NAGARLENS — Project Status & Roadmap

> **Current Status:** Offline Evidence Persistence, Synchronization & Reliability Hardening Complete
> **Active Milestone:** `M0.6 — Offline Evidence Persistence & Synchronization Foundation`
> **Last Updated:** Milestone M0.6 Complete & Hardened

---

## 1. Milestone Roadmap Overview

| Milestone | Target Scope | Status | Target Date |
| :--- | :--- | :--- | :--- |
| **M0.1** | Repository Foundation & Architectural Baseline | **COMPLETED** | Current |
| **M0.2** | Core Android App Shell & Architecture Skeleton | **COMPLETED** | Current |
| **M0.3** | Modular Monolith Backend Foundation & PostGIS Setup | **IMPLEMENTED — RUNTIME VALIDATION PENDING** | Current |
| **M0.4** | Design System & Navigation Implementation | **COMPLETED** | Current |
| **M0.5** | Reality Capture & Evidence Integrity Pipeline | **COMPLETED** | Current |
| **M0.6** | Offline Evidence Persistence & Synchronization Foundation | **COMPLETED** | Current |
| **M0.7** | AI Computer Vision Drainage Defect Model Pipeline | PLANNED | Pending Approval |

---

## 2. Module Completion Matrix

### Documentation & Architecture
- [x] Repository Root & Contributing Guidelines (`README.md`, `CONTRIBUTING.md`, `.gitignore`)
- [x] Product Requirements Document (`docs/product/PRD.md`)
- [x] System Architecture Specification (`docs/architecture/SYSTEM_ARCHITECTURE.md`)
- [x] Module Boundaries & Dependency Rules (`docs/architecture/MODULE_BOUNDARIES.md`)
- [x] Architecture Decision Record ADR-001 (`docs/architecture/adr/ADR-001-MODULAR-MONOLITH.md`)
- [x] Field Design System & Accessibility Guidelines (`docs/design/DESIGN_SYSTEM.md`)
- [x] Database & PostGIS Strategy (`docs/database/DATABASE_STRATEGY.md`)
- [x] API Standards & Ingestion Protocols (`docs/api/API_STANDARDS.md`)
- [x] Testing Strategy & Verification Guidelines (`docs/testing/TEST_STRATEGY.md`)
- [x] Release Strategy & Deployment Pipelines (`docs/releases/RELEASE_STRATEGY.md`)

### Android Application (`/android`)
- [x] `:app` Application Shell setup (Gradle DSL, Version Catalog, Hilt, Compose, Navigation)
- [ ] `:core:network` (Retrofit + OkHttp)
- [x] `:core:database` (Room database `NagarlensDatabase`, `EvidenceDao`, Room schema export enabled)
- [x] `:core:model` (`Evidence`, `MediaType`, `EvidenceStatus`, `SyncStatus` domain entities)
- [x] `:core:designsystem` (Material 3 Theme, Field Colors, Typography, Shapes & Components)
- [x] `:core:location` (Fused Location Provider & GNSS telemetry)
- [x] `:feature:capture` (CameraX photo/video acquisition, app-private evidence store, SHA-256 integrity pipeline)
- [x] `:feature:sync` (Offline queue, WorkManager background manager, stale UPLOADING recovery)
- [x] `:feature:asset-memory` (Map screen, Activity history, and Profile foundations)

### Backend Services (`/backend` & `/infrastructure`)
- [x] Modular FastAPI Core (`app.core`, `app.main`)
- [x] Async SQLAlchemy 2.0 Engine & Session Factory (`app.infrastructure.db`)
- [x] Alembic Async Migration Environment & PostGIS Init (`migrations/versions/0001_init_postgis.py`)
- [x] Diagnostic Health Endpoints (`GET /api/v1/health/live`, `GET /api/v1/health/ready`)
- [x] Container Orchestration Blueprint (`infrastructure/docker/docker-compose.yml`)
- [x] Dependency Management (`backend/pyproject.toml` targeting Python 3.12)
- [ ] Asset Management Module (`app.modules.assets`)
- [ ] Observation Ingestion Module (`app.modules.observations`)
- [ ] AI Inference Pipeline Gateway (`app.modules.ai_engine`)
- [ ] PostGIS Spatial Analytics Engine (`app.modules.spatial`)
- [ ] Object Storage S3 Media Manager (`app.core.storage`)

---

## 3. Key Architectural Decisions Log

1. **Modular Monolith for Backend:** Selected to eliminate microservice operational overhead while enforcing clean domain separation via package boundaries. (See [ADR-001](docs/architecture/adr/ADR-001-MODULAR-MONOLITH.md)).
2. **PostgreSQL 16 + PostGIS 3.4:** Primary datastore for spatial drainage line networks, catch basins, and spatial queries (`ST_DWithin`, `ST_Contains`).
3. **Offline-First Android App:** SQLite/Room encrypted queue storing observations locally until network connectivity is confirmed, ensuring zero field data loss.
4. **SHA-256 Media Hashing:** Cryptographic hashing performed client-side upon capture to guarantee evidence immutability and anti-tampering.
5. **Gradle Version Catalog & Single-Module App Shell:** Established single `:app` module structure with Kotlin 2.0.0, Compose, Material 3, and Hilt DI.
6. **Backend Foundation Architecture:** Implemented FastAPI modular monolith foundation with Pydantic Settings v2, async SQLAlchemy 2.0, Alembic PostGIS migrations, and health routes (`/health/live`, `/health/ready`).
7. **Design System & Navigation Shell (M0.4):** Implemented Material 3 field design tokens, custom buttons/cards/badges/app bars, and 5-destination bottom navigation architecture (Home, Map, Capture, Activity, Profile) with prominent central Capture FAB action button.
8. **Reality Capture & Evidence Preservation (M0.5):** Implemented CameraX lifecycle-aware controller for photo & video recordings, app-private evidence storage (`context.filesDir/evidence/`), SHA-256 content digest computation on original completed binaries, and Fused Location Provider GNSS lock telemetry with explicit runtime permission handling.
9. **Offline Evidence Persistence & WorkManager Synchronization (M0.6):** Implemented Room database (`NagarlensDatabase` with schema export enabled, `EvidenceDao`, `EvidenceEntity`), explicit state machine (`LOCAL_ONLY`, `QUEUED`, `UPLOADING`, `SYNCED`, `RETRYABLE_FAILURE`, `PERMANENT_FAILURE`), cancellation-safe WorkManager background sync scheduler (`EvidenceSyncScheduler`, `EvidenceSyncWorker`), stale `UPLOADING` recovery engine (`recoverStaleUploadingRecords`), orphan file auto-recovery audit engine, and `DeferredEvidenceUploadClient`.

---

## 4. Known Issues & Technical Risks

- **Risk 1 — Field GPS Accuracy:** GPS drift under urban canopy or near tall structures may mislocate drainage assets. *Mitigation:* Require manual map-pin verification and capture heading/bearing metadata.
- **Risk 2 — Heavy Media Sync Overhead:** High-resolution videos/photos captured in low-connectivity areas may stall sync queues. *Mitigation:* Background chunked uploading, adaptive compression, and prioritization of metadata JSON before media binaries.
- **Risk 3 — Docker Runtime Pending:** Host environment currently lacks Docker CLI; container integration tests are pending Docker runtime environment availability.

---

## 5. Next Task

- **Task M0.7:** AI Computer Vision Drainage Defect Model Pipeline (awaiting user approval).
