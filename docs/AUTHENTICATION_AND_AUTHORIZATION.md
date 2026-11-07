# NAGARLENS — Authentication & Authorization Governance

> **Document Version:** 1.0.0  
> **Status:** Active Security Specification  

---

## 1. Cryptographic Authentication & RBAC

- **Password Hashing**: Bcrypt with salt (`bcrypt.hashpw(password, salt)`).
- **JWT Signature**: Cryptographically signed HS256 JWT tokens containing `sub`, `role`, `org_id`, `iss`, `aud`, `exp`, `iat`.
- **Role-Based Access Control (RBAC)**:
  - `field_worker`: Permitted to capture evidence, register assets, record observations.
  - `reviewer`: Permitted to submit asset verification decisions (`APPROVED`, `REJECTED`, `CORRECTION_REQUESTED`).
  - `administrator`: Full administrative system access.
- **Tenant Isolation**: Multi-organization data isolation via `org_id` claims.
