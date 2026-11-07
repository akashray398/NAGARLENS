# NAGARLENS — Spatial Risk Intelligence & Catchment Analysis

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. Catchment Spatial Buffer Analysis

- Evaluates risk score propagation across adjacent infrastructure assets within a 100-meter PostGIS spatial buffer (`ST_DWithin`).
- High-risk clusters trigger area-wide municipal warning notices for drainage catchment zones.
