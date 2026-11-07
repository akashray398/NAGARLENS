"""Unit tests for Municipal Incident & Work Order Endpoints."""

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


def test_create_and_list_incidents():
    create_payload = {
        "id": "INC-TEST-99",
        "asset_id": "511a2c72-86f0-4c1a-9509-09c928410811",
        "asset_code": "NAG-CB-000001",
        "title": "Severe Waterlogging Over Flow",
        "description": "Drain channel overtopping during heavy rainfall",
        "priority": "CRITICAL",
        "defect_category": "DRAIN_OVERFLOW"
    }

    create_resp = client.post("/api/v1/incidents", json=create_payload, headers=get_auth_headers())
    assert create_resp.status_code == 201
    assert create_resp.json()["id"] == "INC-TEST-99"

    list_resp = client.get("/api/v1/incidents")
    assert list_resp.status_code == 200
    assert len(list_resp.json()) >= 1


def test_update_incident_status():
    status_payload = {"status": "RESOLVED", "comment": "Channel desilted and cleared."}
    patch_resp = client.patch("/api/v1/incidents/INC-10001/status", json=status_payload, headers=get_auth_headers())
    assert patch_resp.status_code == 200
    assert patch_resp.json()["status"] == "RESOLVED"
