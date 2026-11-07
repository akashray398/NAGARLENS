# NAGARLENS — Data & Evidence Protection Specification

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. Evidence Immutability & Integrity

- **Cryptographic Digest**: Client computes SHA-256 binary hash prior to upload.
- **S3 Storage Validation**: `S3MediaStorageService` verifies upload binary size (max 50 MB) and SHA-256 match.
