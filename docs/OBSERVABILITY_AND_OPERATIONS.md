# NAGARLENS — Observability & Operations Specification

> **Document Version:** 1.0.0  
> **Status:** Active Operational Guide  

---

## 1. Operational Telemetry & Health Checks

- **Liveness Probe**: `GET /api/v1/health/live` (Returns HTTP 200 OK if service is alive).
- **Readiness Probe**: `GET /api/v1/health/ready` (Verifies PostgreSQL + PostGIS database connectivity).
- **Structured Logging**: JSON-formatted Python logger (`app/core/logging.py`) capturing request correlation IDs, response status codes, and execution duration.
