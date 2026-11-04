# NAGARLENS — Temporal Reality Graph Architecture Specification

> **Architecture Status:** Accepted Baseline  
> **Milestone:** M0.9 — Temporal Reality Graph Foundation  

---

## 1. Domain Decoupling & Temporal Memory Principles

NAGARLENS builds an immutable, time-series infrastructure memory graph across physical assets:
- **Asset:** Persistent physical entity (e.g. Catch Basin `NAG-CB-000104`).
- **Observation:** A time-stamped inspection event recording physical conditions at `observedAtUtc`.
- **Evidence:** Immutable binary media (JPEG/MP4) with SHA-256 hash digest.
- **Intervention:** A real-world physical action (e.g., `SILT_REMOVAL`, `COVER_REPLACEMENT`).
- **Outcome Verification:** Empirical evaluation checking whether an intervention achieved condition improvement (`IMPROVED`, `DETERIORATED`, etc.).

---

## 2. Temporal Invariants

1. **Append-Only History:** Observations and interventions are immutable history entries. Corrections append new observation records rather than overwriting past entries.
2. **Field vs. Ingestion Timestamp:** `observedAtUtc` (field event time) is strictly preserved separately from `recordedAtUtc` (server ingestion time) to handle late-arriving offline field observations.
3. **Graph Relationship Traversal:**
   `Asset` $\rightarrow$ `Observation` $\rightarrow$ `Evidence`  
   `Asset` $\rightarrow$ `Intervention` $\rightarrow$ `Intervention Evidence`  
   `Intervention` $\rightarrow$ `Outcome Verification` $\rightarrow$ `Verification Observation`
