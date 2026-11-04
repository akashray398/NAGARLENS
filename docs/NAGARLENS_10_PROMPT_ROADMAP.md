# NAGARLENS — 10-Master-Prompt Execution Roadmap

> **Product:** NAGARLENS Infrastructure Intelligence Network  
> **Document Version:** 1.0.0  
> **Status:** Final Roadmap Specification  

---

## Master Development Sequence

```text
PROMPT 01 ──► Audit & Reconstruction Blueprint [COMPLETED]
PROMPT 02 ──► Complete Android Application
PROMPT 03 ──► Custom AI & Computer Vision Engine
PROMPT 04 ──► Geospatial & Infrastructure Digital Twin
PROMPT 05 ──► Backend & Cloud Platform
PROMPT 06 ──► Predictive Infrastructure Intelligence
PROMPT 07 ──► Investigation & Intelligent Automation
PROMPT 08 ──► Security, Privacy & Reliability
PROMPT 09 ──► Complete Testing & Performance Engineering
PROMPT 10 ──► Release Engineering & Production Launch
```

---

## Detailed Prompt Breakdown

### PROMPT 01 — Repository Audit & Architecture Reconstruction
- **Objective**: Conduct exhaustive code, database, AI, and architecture audit; build target blueprint and 10-prompt master roadmap.
- **Status**: **COMPLETED**
- **Deliverables**: Audit Report, Module Inventory, Architecture Blueprint, Technical Risk Register, 10-Prompt Roadmap.

---

### PROMPT 02 — Complete Android Application
- **Objective**: Reconstruct and finalize the production-grade native Android client application.
- **Modules Covered**: Application Foundation, UI Shell, Design System, Feature Capture, Feature Sync, Feature Asset Memory, Feature Profile.
- **Dependencies**: Android SDK 35, Compose, Hilt, Room v4, CameraX, WorkManager.
- **Key Changes**:
  - Implement full 5-destination Compose navigation shell (Home, Map, Capture, Activity, Profile).
  - Apply dark navy / emerald green Material 3 field theme.
  - Implement Retrofit HTTP network client for REST communication with backend.
  - Integrate CameraX with GNSS telemetry binding and real-time reticle UI.
  - Enable offline-first Room persistence and background sync scheduling.
- **Definition of Done**: 100% Android screens functional, 0 crashes on device, all unit tests passing.

---

### PROMPT 03 — Custom AI & Computer Vision Engine
- **Objective**: Implement the end-to-end computer vision model pipeline, ONNX Runtime integration, and defect scoring engine.
- **Modules Covered**: AI Image Processing, Defect Detection & Classification, Model Registry.
- **Dependencies**: PyTorch, ONNX Runtime, PIL, NumPy, OpenCV.
- **Key Changes**:
  - Export trained MobileNetV3-Small & RT-DETR model weights to `.onnx` format.
  - Integrate ONNX Runtime on Android edge (`:core:ml`) for real-time local inference.
  - Connect FastAPI `/api/v1/ai/assessments` route to live server PyTorch/ONNX inference engine.
  - Implement 9-category defect severity index calculator (0.0 - 1.0 scale).
- **Definition of Done**: Real-time defect inference returning bounding boxes, class labels, and confidence scores (>85% mAP).

---

### PROMPT 04 — Geospatial & Infrastructure Digital Twin
- **Objective**: Build PostGIS geospatial spatial engine, vector tile mapping, and asset digital twin memory graph.
- **Modules Covered**: Infrastructure Mapping, Geospatial Intelligence, Infrastructure Asset Registry, Asset Identity.
- **Dependencies**: PostGIS 3.4, GeoAlchemy2, MapLibre / Compose Canvas.
- **Key Changes**:
  - Implement PostGIS spatial joins (`ST_DWithin`, `ST_Contains`, `ST_MakeEnvelope`).
  - Add Vector Tile rendering engine to Android Compose Map screen.
  - Build physical QR code / NFC tag registration and scanning module.
  - Build asset spatial duplicate detection engine.
- **Definition of Done**: Interactive geospatial map displaying assets, spatial queries responding in <50ms.

---

### PROMPT 05 — Backend & Cloud Platform
- **Objective**: Harden the FastAPI modular monolith backend, PostgreSQL/PostGIS database, MinIO S3 media storage, and multi-tenant organization engine.
- **Modules Covered**: Backend & Cloud Services, Authentication & Identity, Data Synchronization, Organization Support.
- **Dependencies**: FastAPI, SQLAlchemy 2.0, MinIO/AWS S3 SDK, Redis, Celery/ARQ.
- **Key Changes**:
  - Implement OAuth2 JWT authentication with refresh tokens and RBAC.
  - Implement S3 presigned URL generation and chunked multipart media upload API.
  - Deploy Redis task queue for async background AI processing.
  - Multi-tenant organization schema isolation.
- **Definition of Done**: All REST endpoints fully functional, secure authorization enforced, Docker Compose running cleanly.

---

### PROMPT 06 — Predictive Infrastructure Intelligence
- **Objective**: Implement spatial-temporal predictive risk intelligence, asset degradation modeling, and historical change detection.
- **Modules Covered**: Infrastructure Condition Monitoring, Historical Change Detection, Predictive Intelligence.
- **Dependencies**: SciPy, PyTorch, PostGIS, Pandas.
- **Key Changes**:
  - Build asset condition degradation trend analyzer over time.
  - Build side-by-side historical before/after intervention visual comparison engine.
  - Implement predictive blockage & waterlogging risk ranking algorithm.
- **Definition of Done**: Predictive risk scores calculated for all assets, historical change timeline rendered visually.

---

### PROMPT 07 — Investigation & Intelligent Automation
- **Objective**: Build municipal incident management, automated work order dispatch, investigation engine, and PDF reporting.
- **Modules Covered**: Incident Management, Investigation Engine, Intelligent Notifications, Automated Reporting.
- **Dependencies**: WeasyPrint / ReportLab, FCM, WebSockets.
- **Key Changes**:
  - Build automated incident creation state machine upon severe defect detection.
  - Implement digital evidence chain-of-custody investigation builder.
  - Implement FCM push notification dispatch for critical alerts.
  - Build one-click municipal compliance PDF report generator.
- **Definition of Done**: Incidents auto-created on high-risk detections, PDF reports exported successfully.

---

### PROMPT 08 — Security, Privacy & Reliability
- **Objective**: Enforce end-to-end security, AI privacy blurring (faces/license plates), cryptographic auditability, and observability.
- **Modules Covered**: Security & Privacy, Analytics & Observability.
- **Dependencies**: OpenCV, PyTorch, OpenTelemetry, Prometheus.
- **Key Changes**:
  - Implement automated AI face & license plate blurring prior to public storage.
  - Enforce server-side SHA-256 media hash integrity verification on upload.
  - Implement OpenTelemetry distributed tracing across Android and FastAPI backend.
  - Conduct security vulnerability audit & secrets elimination.
- **Definition of Done**: PII automatically redacted from imagery, server hash checks active, 0 high/critical security vulnerabilities.

---

### PROMPT 09 — Complete Testing & Performance Engineering
- **Objective**: Build complete automated E2E test suites, load testing, performance profiling, and memory leak analysis.
- **Modules Covered**: Testing Infrastructure.
- **Dependencies**: JUnit, Robolectric, Pytest, Locust, Android Profiler, Espresso.
- **Key Changes**:
  - Expand Android unit and Compose UI integration tests to >85% code coverage.
  - Expand Backend pytest coverage to >90%.
  - Run Locust load test simulating 100+ concurrent field evidence uploads.
  - Eliminate memory leaks and optimize UI frame rendering to 60 FPS.
- **Definition of Done**: >85% total code coverage, green test runs, verified performance benchmarks under load.

---

### PROMPT 10 — Release Engineering & Production Launch
- **Objective**: Execute final release engineering, CI/CD pipeline deployment, containerization, and production launch setup.
- **Modules Covered**: Release Engineering.
- **Dependencies**: GitHub Actions, Docker, Android App Bundle (AAB), Play Console CLI.
- **Key Changes**:
  - Configure automated GitHub Actions CI/CD pipeline for Android build & Backend testing.
  - Build production Docker multi-stage containers for backend API and AI worker.
  - Generate release-signed Android App Bundle (AAB) with R8 Proguard rules optimized.
  - Finalize production deployment documentation and operator manuals.
- **Definition of Done**: Release APK/AAB compiled and signed, production Docker image built, system ready for commercial deployment.
