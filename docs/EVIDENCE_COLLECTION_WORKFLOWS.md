# NAGARLENS — Evidence Collection Workflows

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. Evidence Request Lifecycle

1. **Request Creation**: Investigator issues `EvidenceRequestCreateSchema` specifying target asset, required photograph type (e.g. `PHOTO_CLOSEUP_INVERT`), and instructions.
2. **Status Transition**: Investigation automatically transitions from `INVESTIGATING` to `AWAITING_EVIDENCE`.
3. **Field Submission**: Field inspector captures SHA-256 verified evidence using CameraX, linking submission back to `request_id`.
