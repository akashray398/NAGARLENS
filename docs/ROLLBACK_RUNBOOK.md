# NAGARLENS — Production Deployment Rollback Runbook

> **Document Version:** 1.0.0  
> **Status:** Operational Runbook  

---

## 1. Rollback Execution Procedure

To rollback backend container deployment:

```cmd
cd infrastructure/docker
docker compose down
git checkout v0.9.0
docker compose up -d --build
```
