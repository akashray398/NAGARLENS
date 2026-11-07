# NAGARLENS — Backend & Cloud Platform Architecture Specification

> **Product:** NAGARLENS Infrastructure Intelligence Network  
> **Document Version:** 1.0.0  
> **Status:** Active Production Specification  
> **Framework:** FastAPI Python 3.12+ (Modular Monolith)  

---

## 1. High-Level Modular Monolith Topology

The NAGARLENS Backend Platform is structured as a **Modular Monolith** (`app.modules.*`), combining REST API v1 routers, async SQLAlchemy 2.0 ORM datastores, S3-compatible media object storage, and AI inference gateways.

```mermaid
flowchart TD
    subgraph Clients ["Mobile & Web Clients"]
        AND["Android Edge Client (Retrofit / OkHttp)"]
        WEB["Web Admin Portal / Dashboard"]
    end

    subgraph API_Gateway ["FastAPI Modular Monolith API Gateway (/api/v1)"]
        AUTH_ROUTER["/api/v1/auth\n(JWT Authentication & Password Rotation)"]
        ASSET_ROUTER["/api/v1/assets\n(PostGIS Spatial Asset Registry)"]
        OBS_ROUTER["/api/v1/observations\n(Evidence Ingestion & Presigned S3 URLs)"]
        INC_ROUTER["/api/v1/incidents\n(Municipal Incident Work Orders)"]
        AI_ROUTER["/api/v1/ai\n(Defect Taxonomy & Inference Engine)"]
        SYNC_ROUTER["/api/v1/sync\n(Idempotent Push & Delta Pull Sync Engine)"]
    end

    subgraph Core_Services ["Core Shared Services"]
        SEC["app.core.security\n(Bcrypt / PyJWT / Role Authorization)"]
        STORAGE["app.core.storage\n(S3MediaStorageService / Presigned S3 URLs)"]
        ML_ENGINE["app.ml.inference\n(VisionInferenceEngine / NMS / Severity)"]
    end

    subgraph Data_Tier ["Persistence & Cloud Storage"]
        POSTGIS[("PostgreSQL 16 +\nPostGIS 3.4 DB (EPSG:4326)")]
        MINIO[("MinIO / AWS S3\nObject Media Store")]
    end

    AND --> AUTH_ROUTER
    AND --> ASSET_ROUTER
    AND --> OBS_ROUTER
    AND --> INC_ROUTER
    AND --> AI_ROUTER
    AND --> SYNC_ROUTER
    WEB --> AUTH_ROUTER

    AUTH_ROUTER --> SEC
    OBS_ROUTER --> STORAGE
    AI_ROUTER --> ML_ENGINE

    ASSET_ROUTER --> POSTGIS
    OBS_ROUTER --> POSTGIS
    INC_ROUTER --> POSTGIS
    STORAGE --> MINIO
```

---

## 2. API Router Index (`/api/v1`)

| Router Path | Module | Endpoints | Security Requirement |
| :--- | :--- | :--- | :--- |
| `/api/v1/health` | Health & Diagnostics | `GET /live`, `GET /ready` | Public |
| `/api/v1/auth` | Authentication | `POST /register`, `POST /token`, `POST /refresh`, `GET /me` | Bearer Token for `/me` |
| `/api/v1/assets` | Asset Registry | `POST /`, `GET /{id}`, `GET /`, `GET /nearby`, `PATCH /{id}`, `POST /{id}/verification` | Bearer Token / Reviewer Role |
| `/api/v1/observations` | Evidence Ingestion | `POST /`, `GET /{id}`, `POST /upload-url` | Bearer Token |
| `/api/v1/incidents` | Incident Work Orders | `POST /`, `GET /{id}`, `GET /`, `PATCH /{id}/status` | Bearer Token |
| `/api/v1/sync` | Data Sync Engine | `POST /push`, `GET /pull` | Bearer Token |
| `/api/v1/ai` | AI Vision Engine | `GET /taxonomy`, `GET /models`, `POST /assessments` | Public / Bearer Token |
| `/api/v1/temporal` | Reality Graph | `POST /assets/{id}/observations`, `GET /assets/{id}/timeline` | Bearer Token |
