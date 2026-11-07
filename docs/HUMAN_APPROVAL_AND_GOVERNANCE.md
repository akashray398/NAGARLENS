# NAGARLENS — Human Approval & Governance Specification

> **Document Version:** 1.0.0  
> **Status:** Production Governance Specification  

---

## 1. Approval Gate Enforcement

- Reviewer or administrator privileges (`get_current_reviewer`) are required on backend to approve or reject requests (`POST /api/v1/automation/approvals/{id}`).
- Unapproved attempts to execute gated actions return `HTTP 403 Forbidden`.
