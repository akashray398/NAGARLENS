# System Architecture Specification

> **System:** NAGARLENS Infrastructure Intelligence Network  
> **Architecture Pattern:** Offline-First Mobile Client + Modular Monolith Backend + Async Vision AI Engine  
> **Document Version:** 1.0.0  
> **Status:** Architecture Baseline  

---

## 1. High-Level Architecture Topology

NAGARLENS is structured into three primary physical tiers designed for maximum reliability in high-friction field environments:

```text
  +-----------------------------------------------------------------------+
  |                     ANDROID EDGE CLIENT ENGINE                        |
  |  +-----------------------------------------------------------------+  |
  |  | Jetpack Compose UI -> ViewModels -> Clean Domain UseCases       |  |
  |  | CameraX Media Capture (EXIF + GPS + SHA-256 Immutability)       |  |
  |  | Room Encrypted Local Store -> Background Sync Engine (WorkManager)|  |
  |  +-----------------------------------------------------------------+  |
  +-----------------------------------+-----------------------------------+
                                      |
                           HTTPS / TLS 1.3 REST API
                                      |
  +-----------------------------------v-----------------------------------+
  |                 FASTAPI MODULAR MONOLITH BACKEND                      |
  |  +------------------------+ +-------------------+ +----------------+  |
  |  | Ingestion & Auth       | | Asset Registry    | | Spatial Engine |  |
  |  | app.modules.observations| | app.modules.assets| | (PostGIS Join) |  |
  |  +-----------+------------+ +---------+---------+ +-------+--------+  |
  +--------------|------------------------|-----------------|-------------+
                 |                        |                 |
     +-----------v----------+   +---------v---------+   +---v-------------+
     | MinIO / AWS S3       |   | PostgreSQL 16     |   | Redis Broker    |
     | Object Media Storage |   | + PostGIS 3.4 DB  |   | & Cache         |
     +----------------------+   +-------------------+   +-------+---------+
                                                                |
                                                       Async Task Queue
                                                                |
  +-------------------------------------------------------------v---------+
  |                        AI VISION ENGINE                               |
  |  +-----------------------------------------------------------------+  |
  |  | PyTorch / ONNX Model Pipeline                                  |  |
  |  | Privacy Blurring (Faces/Plates) + Defect Classification        |  |
  |  | Severity Index Scoring -> Writeback to Observation Records     |  |
  |  +-----------------------------------------------------------------+  |
  +-----------------------------------------------------------------------+
```

---

## 2. Mobile Architecture (Android)

The mobile client is built strictly following **Android Clean Architecture** guidelines, employing modern Kotlin idioms and Jetpack libraries.

```text
+-----------------------------------------------------------------------+
| UI Layer (Jetpack Compose, Material 3, Unidirectional Data Flow)      |
+-----------------------------------+-----------------------------------+
                                    | State / Events
+-----------------------------------v-----------------------------------+
| Presentation Layer (ViewModels, UI State Flow, Navigation)            |
+-----------------------------------+-----------------------------------+
                                    | Domain Models
+-----------------------------------v-----------------------------------+
| Domain Layer (UseCases, Domain Entities, Business Rules)             |
+-----------------------------------+-----------------------------------+
                                    | Interfaces / Contracts
+-----------------------------------v-----------------------------------+
| Data Layer (Repository Implementations, Data Sources)                 |
|  +------------------------------+     +----------------------------+  |
|  | Local DataSource (Room DB)   |     | Remote DataSource          |  |
|  | Encrypted SQLite Queue       |     | (Retrofit + REST API)      |  |
|  +------------------------------+     +----------------------------+  |
+-----------------------------------------------------------------------+
```

### Key Android Architectural Components
- **Language:** Kotlin 2.0+ leveraging `Coroutines` and `StateFlow` for reactive, asynchronous state streaming.
- **UI Framework:** Jetpack Compose with Material 3 design tokens customized for high-visibility outdoor environments.
- **Dependency Injection:** Hilt (`@HiltViewModel`, `@InstallIn(SingletonComponent::class)`) for clean component scoping.
- **Local Persistence:** Room Database managing offline observation queues, local spatial caches, and media upload tasks.
- **Offline Sync Engine:** Android `WorkManager` managing retry backoffs, network constraints (`NetworkType.CONNECTED`), and chunked multi-part payload streaming.

---

## 3. Backend Architecture (Python FastAPI Modular Monolith)

To prevent microservice sprawl and complex operational overhead during early phases while preserving architectural boundaries, the backend is organized as a **Modular Monolith**.

### Package Structure Overview
```text
backend/app/
├── core/                  # Cross-cutting concerns (Auth, DB session, S3 client, config)
│   ├── config.py
│   ├── database.py
│   ├── security.py
│   └── storage.py
├── modules/               # Isolated business domains
│   ├── assets/            # Asset registry & PostGIS spatial entity management
│   ├── observations/      # Evidence ingestion, metadata verification, sync endpoints
│   ├── spatial/           # Geospatial catchment analysis & boundary joins
│   ├── ai_engine/         # Async worker trigger & model prediction handling
│   └── audit/             # Human verification & immutable event log
├── main.py                # FastAPI app initialization & router composition
```

### Key Backend Architectural Rules
1. **Module Autonomy:** Each module under `app/modules/` owns its own service layer and schema definitions.
2. **Inter-Module Communication:** Modules may ONLY communicate via explicit python interfaces or internal event buses. Direct cross-module internal database writes are strictly prohibited.
3. **Database Architecture:** Single PostgreSQL database instance, but tables are strictly namespaced by module prefix (e.g., `ast_assets`, `obs_observations`, `ai_predictions`).

---

## 4. AI Vision Engine & Ingestion Pipeline

The AI Engine operates asynchronously to avoid blocking user ingestion APIs.

```text
[Observation Ingest API] 
       |
       +---> Write Observation & Media Metadata to DB (Status: PENDING_AI)
       +---> Save Raw Photo/Video to S3 Object Storage
       +---> Publish Event to Redis Queue `obs.uploaded`
                                   |
                                   v
                      [AI Worker Queue Listener]
                                   |
       +---------------------------+---------------------------+
       |                                                       |
[Step 1: Integrity & Privacy]                [Step 2: Defect Classification]
- Validate SHA-256 Media Hash                - Detect Drain Clogging / Trash
- Detect & Blur Faces / License Plates       - Calculate Severity Score (0.0-1.0)
       |                                                       |
       +---------------------------+---------------------------+
                                   |
                                   v
                     [Persist AI Analysis Result]
- Update Observation Status -> `AI_PROCESSED`
- Store bounding boxes, defect tags, and updated media S3 key
```

---

## 5. Cross-Cutting Engineering Concerns

### 5.1 Evidence Integrity & Anti-Tamper Protocol
- **Client-Side Hashing:** As soon as an image or video frame is captured on the Android device, the app calculates its SHA-256 digest before any disk operation or network payload assembly.
- **Metadata Binding:** GPS location, device sensor bearing, time, altitude, and device ID are cryptographically bound to the hash payload.
- **Server Verification:** Backend recalculates media binary hashes upon receipt. Mismatched hashes immediately reject the upload payload and log a security audit warning.

### 5.2 Offline Synchronization & Resilience
- Observations are assigned a client-side generated UUIDv4 primary key upon capture.
- If network is absent, state is stored as `PENDING_SYNC` in local Room DB.
- `WorkManager` triggers background sync upon network reconnection.
- Conflict Resolution Strategy: **Client-side immutable append + Server-side timestamp ordering**. Observations are immutable event logs; updates create new observation versions rather than overwriting existing raw evidence.

### 5.3 Privacy & Data Governance
- Personal Identifiable Information (PII) including bystanders' faces, civilian vehicle license plates, or readable private door signs within field imagery are automatically obscured by the AI Privacy Blurring pipeline prior to public or cross-departmental display.
- Raw un-blurred imagery is stored in a restricted, access-audited S3 bucket accessible strictly by certified administrators for evidentiary compliance.
