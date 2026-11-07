# NAGARLENS — Controlled Automation Security Audit

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. Approval Gate Enforcement

- High-impact maintenance dispatch actions require explicit reviewer approval (`ApprovalStatus.APPROVED`). Unapproved requests are rejected with `HTTP 403 Forbidden`.
