# NAGARLENS — Production Backup & Disaster Recovery Guide

> **Document Version:** 1.0.0  
> **Status:** Operational Runbook  

---

## 1. Backup & Recovery Operations

- **Database RPO**: 1 Hour (Automated PostgreSQL WAL archiving).
- **Database RTO**: 4 Hours (Docker Compose PostGIS restore procedure).
