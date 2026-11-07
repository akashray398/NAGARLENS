# NAGARLENS — Backend REST API Security Specification

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. REST API Security Controls

- **Security Headers**: `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Strict-Transport-Security`.
- **RBAC Dependencies**: `get_current_user`, `get_current_reviewer`, `get_current_admin`.
- **Tenant Partitioning**: Enforces `org_id` authorization across asset and evidence queries.
