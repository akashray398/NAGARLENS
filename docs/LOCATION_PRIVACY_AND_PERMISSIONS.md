# NAGARLENS — Location Privacy & Permission Governance

> **Document Version:** 1.0.0  
> **Status:** Active Privacy Policy  

---

## 1. Location Access Principles

1. **Contextual Permission Requests**: `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` are requested contextually only when the inspector enables location tagging or opens the asset registration workflow.
2. **No Background Tracking**: The app never tracks user location in the background. Location acquisition is foreground-only and tied to active field audits.
3. **Traceability & Source Metadata**: Every captured location records precision accuracy radius in meters (`accuracyMeters`) and acquisition source (`GPS_SENSOR`, `USER_PIN_CORRECTION`, `IMPORT`).
