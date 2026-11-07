# NAGARLENS — Infrastructure Digital Twin Memory Graph

> **Document Version:** 1.0.0  
> **Status:** Active Digital Twin Specification  

---

## 1. Digital Twin Concept & Structure

In NAGARLENS, an **Infrastructure Digital Twin** is a dynamic, memory-graph representation of a physical asset.

```text
                  ┌───────────────────────────────┐
                  │    Canonical Asset UUID       │
                  │   NAG-CB-000104 (WGS84 Point) │
                  └──────────────┬────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         ▼                       ▼                       ▼
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│  Spatial Twin    │    │ Physical Identity│    │  Temporal Graph  │
│ - WGS84 Coords   │    │ - QR Code Token  │    │ - Observations   │
│ - Distance to Usr│    │ - NFC Tag Token  │    │ - Interventions  │
│ - Ward Catchment │    │ - Municipal ID   │    │ - AI Defect Logs │
└──────────────────┘    └──────────────────┘    └──────────────────┘
```

### Key Capabilities:
- **Spatial Positioning**: Live calculation of distance (meters) and bearing angle (degrees) from field inspector location.
- **Physical Tag Binding**: Dual QR code and NFC tag resolution to canonical Asset UUID.
- **Location History Audit**: Immutable audit logging of any coordinate corrections (`asset_location_history`).
