# NAGARLENS — Map Integration & Spatial Viewport Guide

> **Document Version:** 1.0.0  
> **Status:** Production Field Guide  

---

## 1. Living Map Viewport Principles

1. **Viewport Bounding Box Queries**: Requests assets bounded within the current map envelope (`min_lat`, `min_lon`, `max_lat`, `max_lon`) to keep Android memory footprint minimal.
2. **Status Color System**:
   - `VERIFIED`: Safety Green (`#00C853`)
   - `UNVERIFIED` / `PENDING_REVIEW`: Signal Amber (`#FFAB00`)
   - `REJECTED`: Alert Red (`#FF3D00`)
3. **Offline Mirror**: Local Room SQLite database mirrors cached spatial asset nodes for seamless offline field mapping.
