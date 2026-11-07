# NAGARLENS — Production Containerization & Deployment Guide

> **Deployment Stack:** Docker Compose / Kubernetes  
> **Containers:** `nagarlens_db` (PostGIS 3.4), `nagarlens_backend` (FastAPI)  

---

## 1. Quickstart Container Launch

Copy environment configuration:

```cmd
cd infrastructure/docker
copy .env.example .env
```

Start container orchestration:

```cmd
docker compose up -d
```

Endpoints available:
- **Backend API**: `http://localhost:8000/api/v1`
- **Swagger Docs**: `http://localhost:8000/docs`
- **Health Check**: `http://localhost:8000/health`
