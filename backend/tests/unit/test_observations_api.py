"""Unit tests for Evidence Ingestion & Observation API Endpoints."""

import time
import jwt
from fastapi.testclient import TestClient

from app.core.config import settings
from app.main import app

client = TestClient(app)


def get_auth_headers(role: str = "field_worker") -> dict:
    now = int(time.time())
    payload = {
        "sub": "usr_test_001",
        "username": "test_user",
        "role": role,
        "iss": settings.JWT_ISSUER,
        "aud": settings.JWT_AUDIENCE,
        "iat": now,
        "exp": now + 3600,
        "type": "access"
    }
    token = jwt.encode(payload, settings.SECRET_KEY, algorithm=settings.JWT_ALGORITHM)
    return {"Authorization": f"Bearer {token}"}


def test_create_and_get_observation():
    payload = {
        "id": "obs_unit_test_001",
        "asset_id": "511a2c72-86f0-4c1a-9509-09c928410811",
        "observation_type": "ROUTINE_INSPECTION",
        "defect_category": "SOLID_WASTE",
        "condition_severity": "HIGH",
        "description": "Plastic trash choking catch basin invert",
        "latitude": 12.9716,
        "longitude": 77.5946,
        "accuracy_meters": 3.5,
        "sha256_hash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    }

    create_resp = client.post("/api/v1/observations", json=payload, headers=get_auth_headers())
    assert create_resp.status_code == 201
    assert create_resp.json()["id"] == "obs_unit_test_001"

    get_resp = client.get("/api/v1/observations/obs_unit_test_001")
    assert get_resp.status_code == 200
    assert get_resp.json()["sha256_hash"] == "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"


def test_request_presigned_upload_url():
    resp = client.post(
        "/api/v1/observations/upload-url?evidence_id=evid_test_999&original_filename=drain_photo.jpg",
        headers=get_auth_headers()
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["evidence_id"] == "evid_test_999"
    assert "presigned_upload_url" in data
    assert data["s3_bucket_name"] == "nagarlens-evidence-media"
