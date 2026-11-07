# NAGARLENS — Production Environment Configuration Guide

> **Document Version:** 1.0.0  
> **Status:** Production Environment Specification  

---

## 1. Environment Variable Configuration

- `ENVIRONMENT`: Set to `production`
- `SECRET_KEY`: Minimum 32-character random cryptographic secret key (no default dev keys allowed).
- `DATABASE_URL`: `postgresql+asyncpg://<prod_user>:<prod_password>@<db_host>:5432/nagarlens_prod`
- `CORS_ORIGINS`: `["https://nagarlens.gov", "https://api.nagarlens.gov"]`
