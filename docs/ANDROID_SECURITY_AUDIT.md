# NAGARLENS — Native Android Application Security Audit

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. Android Client Security Configuration

- **Backup Security**: `android:allowBackup="false"` in `AndroidManifest.xml`.
- **Exported Components**: `MainActivity` is the sole exported activity, protected by standard LAUNCHER intent filters.
- **Session Cleanup**: `SecurityHardeningManager` zeros session tokens upon logout.
