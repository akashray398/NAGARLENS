# NAGARLENS — Temporal Reality Graph API Contract Specification

> **Base Route:** `/api/v1`  

---

## 1. Endpoints Overview

- `POST /api/v1/assets/{asset_id}/observations` — Record a new inspection observation.
- `GET /api/v1/assets/{asset_id}/timeline` — Retrieve full chronological asset memory timeline.
- `GET /api/v1/observations/{observation_id}` — Retrieve specific observation details.
- `POST /api/v1/observations/{observation_id}/evidence` — Link immutable evidence to an observation.
- `POST /api/v1/assets/{asset_id}/interventions` — Record a new physical intervention.
- `GET /api/v1/assets/{asset_id}/interventions` — Retrieve asset interventions.
- `POST /api/v1/interventions/{intervention_id}/outcome-verifications` — Submit outcome verification.
- `GET /api/v1/interventions/{intervention_id}/outcome-verifications` — Retrieve outcome verifications.
