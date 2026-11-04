# NAGARLENS — Living Map Architecture Specification

> **Architecture Status:** Accepted Baseline  
> **Milestone:** M0.8 — Living Map Foundation  

---

## 1. Living Map Principles

The NAGARLENS Living Map provides a real-time, spatial view of physical drainage infrastructure assets, their condition, and verification statuses across urban catchments:
- **Viewport-Driven Queries:** Spatial bounding box queries (`min_lat`, `min_lon`, `max_lat`, `max_lon`) prevent memory exhaustion by fetching only visible assets within the current viewport.
- **Marker Color Coding:** Assets are styled by verification status (`VERIFIED` = Safety Green, `UNVERIFIED` / `PENDING_REVIEW` = Signal Amber, `REJECTED` = Alert Red).
- **Offline Map Cache:** Local Room database mirrors spatial records, enabling map rendering and field registration when network connectivity is lost.
