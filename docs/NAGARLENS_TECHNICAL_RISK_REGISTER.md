# NAGARLENS — Technical Risk Register & Security Audit

> **Product:** NAGARLENS Infrastructure Intelligence Network  
> **Document Version:** 1.0.0  
> **Status:** Active Risk Inventory  

---

## 1. Risk Classification Matrix

| Risk ID | Category | Severity | Description | Impact | Mitigation Strategy | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TR-01** | Security | **CRITICAL** | Hardcoded JWT Secret (`SECRET_KEY = "dev_secret_key_change_in_production"`) in `app/core/config.py`. | Unauthorized token forgery in production if unchanged. | Load `SECRET_KEY` strictly from environment variables via Pydantic Settings; fail startup if default value used in production. | **MITIGATED IN BLUEPRINT** |
| **TR-02** | Security / Privacy | **HIGH** | PII Exposure (Bystander faces and vehicle license plates visible in captured evidence imagery). | Regulatory privacy non-compliance (GDPR / DPDP). | Implement AI Privacy Blurring pipeline (OpenCV/PyTorch) prior to persistent S3 storage. | **SCHEDULED FOR PROMPT 08** |
| **TR-03** | Reliability | **HIGH** | Low-connectivity high-resolution video upload stall in field environments. | Sync queue blocking, heavy battery drain on mobile devices. | Implement chunked resumable media uploading, adaptive compression, and prioritize JSON metadata payload sync first. | **SCHEDULED FOR PROMPT 02/05** |
| **TR-04** | Data Integrity | **MEDIUM** | Field GPS drift under dense urban canopy or tall buildings mislocating catch basins and drains. | Inaccurate asset coordinate placement on map. | Require manual map-pin alignment confirmation in UI, store GNSS accuracy radius (meters) & bearing. | **SCHEDULED FOR PROMPT 02/04** |
| **TR-05** | Security | **MEDIUM** | Room SQLite Database unencrypted on local device storage. | Local data tampering on rooted physical Android devices. | Evaluate SQLCipher for Room database encryption at rest. | **SCHEDULED FOR PROMPT 08** |
| **TR-06** | Reliability | **LOW** | Android `SimpleDateFormat` pattern character exception on localized device locales. | App crash on navigation to Map screen. | Fixed by escaping `'UTC'` string in `MapScreen.kt`. Verified resolved. | **CLOSED / RESOLVED** |
| **TR-07** | Build / Infra | **LOW** | `ANDROID_PREFS_ROOT` and `ANDROID_USER_HOME` environment conflict in local Windows shell builds. | Gradle build failure during plugin application. | Suppress or clear `ANDROID_PREFS_ROOT` prior to calling `gradlew.bat`. | **CLOSED / RESOLVED** |

---

## 2. Security & Compliance Checklist

- [x] Client-side SHA-256 cryptographic media digest calculation prior to upload.
- [ ] Server-side SHA-256 validation against uploaded binary stream (Target: Prompt 05).
- [ ] Automated face & license plate blurring pipeline (Target: Prompt 08).
- [ ] Production JWT secret key enforcement & token expiration (Target: Prompt 05).
- [ ] Multi-tenant organization data partitioning (Target: Prompt 05).
- [ ] SQL injection prevention via SQLAlchemy parameterized queries (Verified: SQLAlchemy 2.0 ORM active).
