# NAGARLENS — Asset Identity & Lifecycle Architecture Specification

> **Architecture Status:** Accepted Baseline  
> **Milestone:** M0.8 — Asset Identity & Living Map Foundation  

---

## 1. Domain Decoupling Principles

In NAGARLENS, infrastructure assets have a persistent digital identity that is strictly decoupled from observations, physical media evidence, and AI assessments:
- **Asset:** Physical infrastructure entity (e.g., Catch Basin `NAG-DRN-000001` at `12.9716°N, 77.5946°E`). Has a permanent UUIDv4.
- **Observation:** A time-stamped inspection event recording physical conditions.
- **Evidence:** Immutable media binary file (JPEG/MP4) with SHA-256 hash digest.
- **AI Assessment:** Machine learning prediction schema analyzing an evidence item.

---

## 2. Hybrid Asset Identity Strategy

Every asset possesses a dual identifier:
1. **Canonical Asset UUID (`id`):** Primary Key in database. Immutable UUIDv4 generated offline or online.
2. **Operational Asset Code (`asset_code`):** Human-readable string formatted as `NAG-{TYPE_PREFIX}-{SEQ_NUM}` (e.g. `NAG-CB-000104`). Unique per organization/namespace.

### Identity Invariants:
- An asset UUID **never changes** once created.
- Correcting asset coordinates updates `asset_location_history` without mutating asset UUID.
- QR codes and NFC tags are physical tokens linked to asset UUID in `asset_identifier_registry`. Physical tags can be revoked or replaced without invalidating the asset UUID.
- Duplicate detection is advisory and returns candidates for human audit. Assets are **never automatically merged**.
