# NAGARLENS — Production Backend Deployment Guide

> **Runtime:** Python 3.12+ FastAPI (Modular Monolith)  
> **Database Engine:** PostgreSQL 16 + PostGIS 3.4  

---

## 1. Production Docker Orchestration

Launch backend application and PostGIS spatial database:

```cmd
cd infrastructure/docker
copy .env.example .env
docker compose up -d --build
```

Health Check Endpoints:
- **Liveness Probe**: `GET /api/v1/health/live`
- **Readiness Probe**: `GET /api/v1/health/ready`
