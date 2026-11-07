# NAGARLENS — Security & Threat Model

> **Document Version:** 1.0.0  
> **Status:** Active Threat Matrix  

---

## 1. Threat Mitigation Matrix

- **TR-01 (Hardcoded Secrets)**: `SECRET_KEY` validated at startup; fails in staging/production if <32 characters or default value used.
- **TR-02 (PII Exposure)**: AI privacy blurring pipeline obscures faces & license plates prior to public display.
- **TR-05 (Local Database Security)**: Room SQLite v5 encrypted queue.
- **SQL Injection Defense**: Async SQLAlchemy 2.0 parameterized query compilation.
