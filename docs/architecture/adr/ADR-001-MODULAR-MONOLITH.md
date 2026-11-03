# ADR-001: Selection of Modular Monolith Architecture for NAGARLENS Backend

> **Status:** Proposed  
> **Date:** Initial Repository Foundation  
> **Author:** Senior Staff Software Engineer  
> **Deciders:** Engineering Architecture Lead, Core Infrastructure Team  

---

## 1. Context & Problem Statement

NAGARLENS is an infrastructure intelligence platform that processes spatial asset registries, high-integrity observation uploads, AI visual defect inference, and human verification workflows.

During initial repository establishment, we must select a backend architecture topology. The team evaluated two primary options:
1. **Distributed Microservices Topology:** Splitting Asset Management, Observation Ingestion, Spatial Querying, and AI Processing into separate HTTP/gRPC microservices.
2. **Modular Monolith Topology:** Building a single backend application deployment artifact containing strictly isolated Python packages with enforced module boundaries.

---

## 2. Decision Drivers

- **System Complexity:** Early-stage infrastructure platforms require rapid iteration on domain boundaries, shared spatial data models, and database schemas.
- **Operational Overhead:** Managing multi-service deployments, distributed transactions, inter-service API versioning, and complex Kubernetes setups introduces significant friction for small engineering teams.
- **Data Integrity & Spatial Joins:** Physical asset observation processing relies heavily on spatial joins between observation points and drainage line networks in PostGIS. Distributed databases across microservices severely complicate spatial queries.
- **Code Organization & Future Extraction:** The team needs clean domain separation so that high-compute sub-domains (such as AI Vision inference) can be isolated if needed later.

---

## 3. Considered Options

### Option A: Microservices Architecture
- **Pros:** Independent scaling of ingestion, spatial joins, and AI inference. Independent deployment pipelines.
- **Cons:** High operational complexity, distributed transaction handling, inability to perform native SQL spatial joins across service boundaries, API network overhead, delayed time-to-market.

### Option B: Unstructured Single-Package Monolith ("Big Ball of Mud")
- **Pros:** Fast initial setup.
- **Cons:** High risk of circular dependencies, spaghetti database calls, impossible to refactor or extract modules as team scales.

### Option C: Modular Monolith Architecture (Chosen Option)
- **Pros:** Single deployment pipeline, native PostGIS spatial query capability across tables, zero inter-service network latency, strict module boundary enforcement via Python package constraints. Allows future microservice extraction if operational data proves necessary.
- **Cons:** Shared deployment cycle (all modules deploy together); requires strict discipline to prevent cross-module code leakage.

---

## 4. Decision Outcome

We decided to adopt **Option C: Modular Monolith Architecture** using Python (FastAPI), PostgreSQL 16 + PostGIS 3.4, and Redis.

### Architectural Rules Implemented
1. Backend application logic resides in a single repository (`backend/app`).
2. Domain logic is compartmentalized inside `app/modules/<domain_name>` (`assets`, `observations`, `spatial`, `ai_engine`, `audit`).
3. Inter-module communication MUST occur through explicit service interfaces.
4. Database tables are strictly owned by specific modules via table name prefixes (`ast_`, `obs_`, `ai_`, `aud_`).

---

## 5. Consequences & Future Extraction Path

### Positive Consequences
- Simplified local development setup (single `docker-compose up` with PostgreSQL/PostGIS and Redis).
- Direct, high-performance spatial SQL queries combining asset geometries (`ast_assets`) and observation points (`obs_observations`).
- Low operational overhead and deterministic debugging.

### Negative / Risk Consequences
- High-compute workloads (e.g., synchronous AI inference) could consume backend HTTP worker threads if run inline.
- *Mitigation:* Heavy AI vision inference is offloaded to asynchronous background workers via Redis queue queues (`app.modules.ai_engine`), completely isolating CPU/GPU workloads from user-facing HTTP request threads.

### Extraction Path (If Required)
If observation ingestion volume scales exponentially beyond single-instance capacity in future milestones, `app.modules.observations` can be extracted into an independent microservice with minimal code refactoring due to established package boundaries.
