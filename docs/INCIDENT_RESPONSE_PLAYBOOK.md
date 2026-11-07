# NAGARLENS — Security Incident Response Playbook

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. Incident Response Stages

1. **Triage & Containment**: Revoke compromised JWT tokens via secret rotation.
2. **Investigation**: Inspect structured JSON logs for correlation ID traces.
3. **Remediation & Recovery**: Restore PostgreSQL/PostGIS database snapshot if necessary.
