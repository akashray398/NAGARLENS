# NAGARLENS

> **Category:** Infrastructure Intelligence Network  
> **Tagline:** Giving the physical world a living memory.  
> **Initial Focus Domain:** Urban drainage failures and localized waterlogging.

---

## Overview

**NAGARLENS** is an infrastructure intelligence platform designed to build a persistent, evidence-backed digital memory of physical urban infrastructure. By combining edge-capable mobile data collection, spatial analysis, and computer vision, NAGARLENS captures the life cycle of civil assets—tracking their physical condition, localized changes, maintenance interventions, and long-term outcomes over time.

Urban drainage networks are frequently subject to silent degradation, unrecorded blockages, and recurring waterlogging. NAGARLENS transforms reactive municipal maintenance into a verifiable, audit-ready asset memory system.

---

## Key Capabilities

- **Field Evidence Capture:** High-integrity mobile photo/video observation capture with embedded spatial context (GPS/altitude/bearing) and offline sync resilience.
- **AI-Powered Visual Intelligence:** Custom computer vision pipeline for automated defect detection (e.g., drain clogging, structural cracking, sediment accumulation, trash accumulation) and severity scoring.
- **Spatial Asset Registry:** PostGIS-powered geospatial modeling of drainage lines, manholes, catch basins, and flood-prone zones.
- **Timeline & Memory Graph:** Immutably tracked historical state of assets over time, providing verifiable proof of condition before and after maintenance interventions.
- **Human-in-the-Loop Verification:** Workflow tools for municipal engineers to review, confirm, or override AI-generated assessments before work order generation.

---

## Architecture Stack

### Mobile Application (Android)
- **Language:** Kotlin 2.0+
- **UI Framework:** Jetpack Compose (Material 3 design system)
- **Architecture:** Clean Architecture + MVVM
- **Dependency Injection:** Hilt
- **Local Database:** Room (SQLite) with encrypted offline queue
- **Networking:** Retrofit + OkHttp with resilience & sync adapters
- **Concurrency:** Kotlin Coroutines & Flow

### Backend Services
- **Framework:** FastAPI (Python 3.12+)
- **Architecture:** Modular Monolith with strictly enforced domain boundaries
- **Primary Database:** PostgreSQL 16 + PostGIS 3.4
- **Cache & Message Broker:** Redis
- **Object Storage:** S3-compatible storage (MinIO / AWS S3) for media evidence

### AI Vision Engine
- **Language/Framework:** Python, PyTorch / ONNX Runtime
- **Capabilities:** Semantic segmentation, object detection, image hashing, EXIF validation, and privacy masking (blurring faces/license plates).

---

## Repository Structure

```text
NAGARLENS/
├── docs/
│   ├── product/
│   │   └── PRD.md                       # Product Requirements Document
│   ├── architecture/
│   │   ├── SYSTEM_ARCHITECTURE.md       # End-to-end system topology & data flows
│   │   └── MODULE_BOUNDARIES.md         # Component boundaries & dependency rules
│   ├── architecture/adr/
│   │   └── ADR-001-MODULAR-MONOLITH.md  # Architectural decision records
│   ├── design/
│   │   └── DESIGN_SYSTEM.md             # Outdoor & field UI guidelines
│   ├── database/
│   │   └── DATABASE_STRATEGY.md         # PostGIS schema, indexing & Room sync
│   ├── api/
│   │   └── API_STANDARDS.md             # REST standards, GeoJSON, media upload
│   ├── testing/
│   │   └── TEST_STRATEGY.md             # Mobile, backend & AI test frameworks
│   └── releases/
│       └── RELEASE_STRATEGY.md          # Versioning, CI/CD, deployment strategy
├── README.md
├── CONTRIBUTING.md
├── PROJECT_STATUS.md
└── .gitignore
```

---

## Documentation Quick Links

- [Product Requirements Document (PRD)](docs/product/PRD.md)
- [System Architecture Specification](docs/architecture/SYSTEM_ARCHITECTURE.md)
- [Module Boundaries & Dependency Rules](docs/architecture/MODULE_BOUNDARIES.md)
- [ADR-001: Modular Monolith Architecture](docs/architecture/adr/ADR-001-MODULAR-MONOLITH.md)
- [Database & PostGIS Strategy](docs/database/DATABASE_STRATEGY.md)
- [API Design Standards](docs/api/API_STANDARDS.md)
- [Testing Strategy](docs/testing/TEST_STRATEGY.md)
- [Release Strategy](docs/releases/RELEASE_STRATEGY.md)
- [Project Status & Roadmap](PROJECT_STATUS.md)

---

## Status & Milestone

Currently at **Milestone M0.1: Repository Foundation**.  
See [PROJECT_STATUS.md](PROJECT_STATUS.md) for current implementation progress, module readiness, and upcoming milestones.

---

## License

Copyright © NAGARLENS Project. All rights reserved. Proprietary and confidential infrastructure intelligence system.
