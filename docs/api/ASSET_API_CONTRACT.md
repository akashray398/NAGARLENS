# NAGARLENS — Asset API Contract Specification

> **Base Route:** `/api/v1/assets`  

---

## 1. Endpoints Overview

- `POST /api/v1/assets` — Register new asset (returns asset UUID and generated code)
- `GET /api/v1/assets/{asset_id}` — Retrieve asset details by UUID
- `GET /api/v1/assets` — Query assets within map viewport (`min_lat`, `min_lon`, `max_lat`, `max_lon`)
- `GET /api/v1/assets/nearby` — Query assets within radius (`lat`, `lon`, `radius_meters`)
- `PATCH /api/v1/assets/{asset_id}` — Update asset metadata / coordinates (records location history)
- `GET /api/v1/assets/{asset_id}/history` — Retrieve asset location correction history
- `GET /api/v1/assets/{asset_id}/evidence` — Retrieve linked evidence records
- `POST /api/v1/assets/{asset_id}/identifiers` — Register physical QR/NFC tag
- `POST /api/v1/assets/{asset_id}/verification` — Submit verification decision (Approve/Reject/Correction)
- `GET /api/v1/assets/duplicate-candidates` — Query duplicate candidates near coordinates
