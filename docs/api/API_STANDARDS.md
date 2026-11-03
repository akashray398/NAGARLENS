# API Standards & Ingestion Protocols

> **System:** NAGARLENS Infrastructure Intelligence Network  
> **Protocol:** HTTP/2 over TLS 1.3  
> **Format:** JSON / GeoJSON / Multipart Upload / S3 Presigned URLs  
> **Document Version:** 1.0.0  
> **Status:** API Baseline  

---

## 1. RESTful URL Naming Conventions

All REST endpoints follow strict resource-oriented naming rules using lower-case kebab-case paths:

- Base URL: `https://api.nagarlens.org/api/v1`

### Standard Resource Endpoints

| Resource | HTTP Method | Endpoint Path | Description |
| :--- | :--- | :--- | :--- |
| **Assets** | `GET` | `/api/v1/assets` | Query spatial assets within geographic bounding box |
| | `GET` | `/api/v1/assets/{id}` | Retrieve asset details & historical condition timeline |
| **Observations** | `POST` | `/api/v1/observations` | Ingest new field observation metadata & upload request |
| | `GET` | `/api/v1/observations/{id}` | Fetch observation state, media links & AI analysis |
| **Media Upload**| `POST` | `/api/v1/media/upload-url` | Generate S3 presigned upload URL for binary media |
| **Verification**| `POST` | `/api/v1/verifications` | Submit human engineer sign-off or AI override |

---

## 2. Standard Request / Response Payloads

### 2.1 Standard Success Response Wrapper
```json
{
  "success": true,
  "data": {
    "observation_id": "a4b8c9d0-1234-5678-9abc-def012345678",
    "status": "PENDING_AI",
    "sha256_verified": true
  },
  "timestamp": "2025-02-17T12:30:00Z"
}
```

### 2.2 Standard Error Response Payload
```json
{
  "success": false,
  "error": {
    "code": "MEDIA_HASH_MISMATCH",
    "message": "The calculated payload SHA-256 digest does not match the client manifest digest.",
    "details": [
      {
        "field": "sha256_hash",
        "issue": "Client claimed 'e3b0c442...', server received bytes hashing to '8f434346...'"
      }
    ]
  },
  "timestamp": "2025-02-17T12:30:01Z"
}
```

---

## 3. GeoJSON Format Integration

Spatial queries return standard GeoJSON FeatureCollections for native integration with MapLibre / Mapbox mobile SDKs:

```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "id": "c1f7a8b2-5555-4444-3333-222211110000",
      "geometry": {
        "type": "Point",
        "coordinates": [77.5946, 12.9716, 920.5]
      },
      "properties": {
        "asset_code": "DRAIN-CB-104",
        "asset_type": "CATCH_BASIN",
        "condition_score": 0.85,
        "last_inspected_at": "2025-02-15T09:14:22Z"
      }
    }
  ]
}
```

---

## 4. Media Ingestion & Presigned S3 Upload Protocol

To avoid bottlenecks on the main API server when inspectors upload high-resolution video and photo evidence:

```text
[ Android App ] --(1) POST /api/v1/media/upload-url (Metadata + SHA256) --> [ Ingest API ]
[ Android App ] <-- (2) Returns S3 Presigned Upload URL + File ID --------- [ Ingest API ]
[ Android App ] --(3) PUT Binary Media directly to S3 Bucket ---------------> [ S3 Storage ]
[ Android App ] --(4) POST /api/v1/observations (Confirm Complete) -------> [ Ingest API ]
```

---

## 5. Idempotency & Authentication Headers

### Authentication
All requests must include standard OAuth2 Bearer Tokens:
`Authorization: Bearer <JWT_ACCESS_TOKEN>`

### Idempotency Control
To prevent duplicate observations caused by network re-transmits in rural/fringe connectivity areas, offline upload requests MUST include an idempotency header containing the client-generated UUID:
`X-Idempotency-Key: a4b8c9d0-1234-5678-9abc-def012345678`
