# NAGARLENS — Complete REST API Specification

> **Base URL:** `https://api.nagarlens.gov/api/v1`  
> **OpenAPI Interactive Documentation:** `https://api.nagarlens.gov/docs`  

---

## 1. Authentication Endpoints

### 1.1 `POST /api/v1/auth/token`
Authenticate credentials and issue JWT Access Token & Refresh Token.

**Request Body:**
```json
{
  "username": "auditor_central",
  "password": "password"
}
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer",
  "expires_in_seconds": 3600,
  "user_id": "usr_dev_auditor_001",
  "role": "field_worker"
}
```

---

## 2. Ingestion & Sync Endpoints

### 2.1 `POST /api/v1/sync/push`
Idempotently ingest client observation sync payloads.

**Request Body:**
```json
{
  "client_device_id": "pixel_7_inspector_001",
  "sync_timestamp_utc": 1711987200000,
  "evidences": [
    {
      "id": "EVID-SYNC-001",
      "original_file_name": "photo1.jpg",
      "sha256_hash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
      "capture_timestamp_utc": 1711987100000,
      "latitude": 12.9716,
      "longitude": 77.5946
    }
  ]
}
```

**Response (200 OK):**
```json
{
  "processed_count": 1,
  "server_timestamp_utc": 1711987205000,
  "items": [
    {
      "id": "EVID-SYNC-001",
      "status": "ACCEPTED",
      "message": "Evidence EVID-SYNC-001 accepted for sync."
    }
  ]
}
```
