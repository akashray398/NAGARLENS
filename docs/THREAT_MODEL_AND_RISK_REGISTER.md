# NAGARLENS — STRIDE Threat Model & Technical Risk Register

> **Document Version:** 1.0.0  
> **Status:** Active Risk Inventory  

---

## 1. STRIDE Threat Analysis Matrix

| Threat Category | Threat Scenario | Affected Component | Mitigation Control | Risk Level |
| :--- | :--- | :--- | :--- | :--- |
| **Spoofing** | Forged JWT access token or spoofed role header. | Backend API Router | Cryptographic JWT verification in `security.py`; role derived strictly from token payload. | **MITIGATED** |
| **Tampering** | ADB backup extraction of local Room database. | Android Client | Set `android:allowBackup="false"` in `AndroidManifest.xml`. | **MITIGATED** |
| **Repudiation** | Denying evidence submission or inspection action. | Ingestion Pipeline | Client-side SHA-256 binary hash digest calculation and server validation. | **MITIGATED** |
| **Information Disclosure** | PII (faces/license plates) visible in public evidence photos. | AI Vision Pipeline | `PrivacyBlurProcessor` redacts sensitive regions before display. | **MITIGATED** |
| **Denial of Service** | Decompression bomb image upload (>25 MP). | Preprocessing Pipeline | `Image.MAX_IMAGE_PIXELS = 25_000_000` cap in `pipeline.py`. | **MITIGATED** |
| **Elevation of Privilege** | Bypassing reviewer approval gate on action execution. | Automation Router | Backend `get_current_reviewer` dependency checks approval status before action dispatch. | **MITIGATED** |
