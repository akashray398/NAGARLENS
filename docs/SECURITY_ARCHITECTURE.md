# NAGARLENS — Enterprise Security & Defense-in-Depth Architecture

> **Product:** NAGARLENS Infrastructure Intelligence Network  
> **Document Version:** 1.0.0  
> **Status:** Active Production Specification  

---

## 1. High-Level Defense-in-Depth Topology

NAGARLENS enforces security controls across client edge devices, API gateways, storage engines, and background automation frameworks.

```mermaid
flowchart TD
    subgraph Mobile_Edge ["Android Mobile Client Edge"]
        MANIFEST["AndroidManifest.xml\n(allowBackup=false / HTTPS Enforced)"]
        STORE["SecurityHardeningManager\n(Keystore Token Invalidation / SHA-256 Check)"]
        ROOM_SEC[("Encrypted Room v5 DB\nApp-Private Files Storage")]
    end

    subgraph API_Gateway ["FastAPI Backend Gateway (/api/v1)"]
        SEC_HDRS["SecurityHeadersMiddleware\n(nosniff / DENY / HSTS / Cache-Control)"]
        JWT_AUTH["app.core.security\n(Bcrypt / JWT Token Rotation / RBAC)"]
        TENANT["Multi-Tenant Isolation\n(org_id Query Scoping)"]
    end

    subgraph Data_Privacy ["Data & AI Privacy Engine"]
        S3_STORAGE["S3MediaStorageService\n(50MB Limit / Presigned URLs / SHA-256 Validation)"]
        PRIVACY["PrivacyBlurProcessor\n(AI Face & License Plate Redaction)"]
        PIL_SEC["PIL ImageInputValidator\n(Max 25 MP Decompression Bomb Cap)"]
    end

    MANIFEST --> STORE
    STORE --> ROOM_SEC

    STORE -- "TLS 1.3 / HTTPS REST" --> SEC_HDRS
    SEC_HDRS --> JWT_AUTH
    JWT_AUTH --> TENANT

    TENANT --> S3_STORAGE
    S3_STORAGE --> PRIVACY
    PRIVACY --> PIL_SEC
```

---

## 2. Core Security Control Matrix

- **Client Storage**: `allowBackup="false"` in `AndroidManifest.xml` prevents ADB backups of private app storage.
- **Crypto & Secrets**: Production Pydantic Settings (`config.py`) rejects default development JWT keys in production environments.
- **Headers & CORS**: `SecurityHeadersMiddleware` sets `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Strict-Transport-Security`.
- **Media Uploads**: S3 presigned URLs validate SHA-256 media digests before committing database metadata.
- **Privacy Redaction**: `PrivacyBlurProcessor` redacts faces and vehicle license plates prior to public display.
