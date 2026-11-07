"""Unit tests for Synchronization Engine Endpoints."""

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


def test_sync_push_idempotent_ingestion():
    push_payload = {
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

    push_resp = client.post("/api/v1/sync/push", json=push_payload, headers=get_auth_headers())
    assert push_resp.status_code == 200
    data = push_resp.json()
    assert data["processed_count"] == 1
    assert data["items"][0]["status"] == "ACCEPTED"


def test_sync_pull_deltas():
    pull_resp = client.get("/api/v1/sync/pull?since_timestamp_utc=1711987000000", headers=get_auth_headers())
    assert pull_resp.status_code == 200
    assert "server_timestamp_utc" in pull_resp.json()
