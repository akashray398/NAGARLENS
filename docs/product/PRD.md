# Product Requirements Document (PRD)

> **System:** NAGARLENS Infrastructure Intelligence Network  
> **Tagline:** Giving the physical world a living memory.  
> **Domain:** Urban Drainage Failures & Localized Waterlogging  
> **Document Version:** 1.0.0  
> **Status:** Approved Baseline  

---

## 1. Executive Summary & Vision

Physical urban infrastructure—specifically subterranean and surface drainage networks—suffers from severe visibility gaps. Municipalities traditionally operate in a reactive mode, responding to catastrophic waterlogging and urban flooding only after citizen complaints or storm events occur. Existing municipal asset management systems track assets statically (e.g., "Catch Basin #402 exists") without maintaining a dynamic, evidence-backed timeline of conditions, maintenance interventions, structural decay, or operational failures.

**NAGARLENS** addresses this fundamental gap by building a **living memory** of physical infrastructure. Starting with urban drainage networks, NAGARLENS creates a verifiable audit trail of infrastructure observations, AI-assisted defect scoring, maintenance events, and post-intervention outcomes.

---

## 2. Problem Statement & Initial Deployment Domain

### 2.1 The Core Problem
1. **Silent Degradation & Lack of Evidence:** Drains get clogged gradually by silt, construction debris, or municipal solid waste. Without recorded evidence, maintenance teams cannot prove prior condition or identify chronic failure hotspots.
2. **Ghost Interventions:** Municipalities spend significant funds on drain desilting contracts, but lack photographic, spatial, and temporal proof that work was actually executed effectively.
3. **Disconnected Spatial Context:** Isolated photos sent via messaging apps or basic portals lack precise geographic coordinates, direction, asset binding, and temporal continuity.

### 2.2 Initial Domain Focus
- **Urban Drainage & Catchment Networks:** Stormwater drains, catch basins, culverts, manholes, outfalls, and recognized waterlogging vulnerability zones.

---

## 3. Target User Personas & Workflows

### 3.1 Personas

| Persona | Role | Key Objectives | Primary Interface |
| :--- | :--- | :--- | :--- |
| **Field Inspector** | Municipal worker or ground field auditor | Capture high-integrity ground evidence of drain conditions in harsh field environments | Android Mobile App |
| **Municipal Engineer** | Ward/City Civil Engineer | Review verified evidence, validate AI defect detections, approve maintenance work orders | Web Dashboard |
| **Field Technician** | Maintenance crew / Contractor worker | Receive targeted work orders, execute clearing/repairs, capture post-intervention evidence | Android Mobile App |
| **Executive / Analyst** | City Administrator / Data Scientist | Analyze recurring flooding hotspots, track contract delivery compliance, measure asset lifespan | Web Analytics Portal |

---

### 3.2 Primary User Workflows

#### Workflow A: Ground Truth Observation Capture (Field Inspector)
1. Inspector approaches an urban drainage asset (e.g., storm drain inlet).
2. Mobile app resolves high-precision spatial location (GPS/GNSS, orientation, accuracy radius).
3. Inspector captures multi-angle photos/videos via NAGARLENS app camera.
4. App automatically attaches EXIF metadata, spatial coordinates, device telemetry, and computes a cryptographic SHA-256 hash.
5. Observation is saved to local offline encrypted storage and queued for background synchronization.

#### Workflow B: Evidence Verification & AI Defect Analysis (System & Engineer)
1. Uploaded media is processed by backend AI Vision Engine.
2. AI performs face/license plate privacy blurring, validates image integrity, and runs defect classification models (e.g., `HEAVY_SILTATION`, `STRUCTURAL_CRACK`, `TRASH_BLOCKAGE`, `CLEAR_FLOW`).
3. Municipal Engineer receives flagged high-severity items on the spatial dashboard.
4. Engineer inspects side-by-side evidence, verifies AI classification, and initiates a formal Maintenance Intervention.

#### Workflow C: Remediation & Living Memory Binding (Field Tech & System)
1. Field Maintenance Technician is assigned a work order bound to the specific spatial asset node.
2. Technician clears blockage and captures post-intervention photo evidence at the exact spatial coordinate.
3. System binds Before-and-After evidence to the asset's permanent timeline graph, updating asset health status.

---

## 4. Functional Requirements

### 4.1 Field Evidence Capture (Mobile)
- **FR-CAP-01:** App MUST capture photo and video media with mandatory device spatial metadata (Latitude, Longitude, Altitude, Bearing, Horizontal Accuracy).
- **FR-CAP-02:** App MUST compute a client-side SHA-256 cryptographic hash of every media file immediately upon capture to guarantee anti-tampering.
- **FR-CAP-03:** App MUST support complete offline capture, storing observations locally in encrypted SQLite/Room storage until connection is restored.
- **FR-CAP-04:** App MUST provide a field-optimised camera view with framing guides for drainage assets.

### 4.2 Asset Memory & Spatial Registry (Backend)
- **FR-AST-01:** System MUST store infrastructure assets as PostGIS spatial entities (Points, LineStrings, Polygons) with standard EPSG:4326 geometry.
- **FR-AST-02:** System MUST maintain a temporal sequence (timeline graph) of all observations, interventions, and condition scores for every registered asset.
- **FR-AST-03:** System MUST allow linking new observations to existing spatial assets within a configurable proximity threshold (e.g., 5 meters) or creating new asset records.

### 4.3 AI Vision & Defect Scoring (AI Engine)
- **FR-AI-01:** AI Engine MUST classify drainage blockage types into defined taxonomy (`CLEAN`, `PARTIAL_BLOCKAGE`, `TOTAL_BLOCKAGE`, `STRUCTURAL_DAMAGE`, `DEBRIS_ACCUMULATION`).
- **FR-AI-02:** AI Engine MUST generate a normalized Defect Severity Index (0.0 to 1.0) and confidence score for each observation.
- **FR-AI-03:** AI Engine MUST automatically apply Gaussian privacy blurring to detected human faces and vehicle license plates before public display/storage.

### 4.4 Verification & Auditability
- **FR-AUD-01:** System MUST maintain an immutable audit log of all human reviews, AI model predictions, and state transitions.
- **FR-AUD-02:** System MUST highlight any discrepancy between AI predictions and engineer overrides for model retraining feedback loops.

---

## 5. Non-Functional Requirements (NFRs)

### 5.1 Performance & Scale
- **NFR-PERF-01:** Mobile app cold start time MUST be under 1.8 seconds on standard Android mid-range hardware.
- **NFR-PERF-02:** Spatial query responses (e.g., assets within 1km radius) MUST complete in under 200ms for up to 50,000 spatial nodes.
- **NFR-PERF-03:** AI Engine inference time per image MUST NOT exceed 2.5 seconds on backend workers.

### 5.2 Reliability & Offline Resilience
- **NFR-REL-01:** Mobile app MUST withstand arbitrary network dropouts without data loss or UI freezing.
- **NFR-REL-02:** Sync engine MUST use exponential backoff and retry policies with chunked upload support for low-bandwidth cellular environments (2G/3G/4G).

### 5.3 Security & Evidence Integrity
- **NFR-SEC-01:** All network traffic MUST be encrypted in transit using TLS 1.3.
- **NFR-SEC-02:** Media binaries in object storage MUST be encrypted at rest (AES-256).
- **NFR-SEC-03:** Mobile local database MUST be encrypted using SQLCipher.
- **NFR-SEC-04:** System MUST reject any media upload whose SHA-256 digest does not match client-side calculated value.

---

## 6. Acceptance & Quality Principles

1. **Zero Data Loss in Field:** An observation captured by an inspector must never be lost, even if the device crashes, loses battery, or suffers sudden network termination.
2. **Verifiable Spatial Truth:** No observation may exist without valid, authenticated spatial coordinates and temporal timestamps.
3. **Human-in-the-Loop Authority:** AI predictions serve as advisory intelligence; final maintenance work orders and formal condition scores require human engineering sign-off.
