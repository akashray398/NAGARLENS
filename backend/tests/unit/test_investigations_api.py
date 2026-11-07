"""Unit tests for Investigation Workflows, Evidence Requests, Approvals & Automation."""

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


def test_create_and_get_investigation():
    headers = get_auth_headers()
    payload = {
        "id": "INV-TEST-99",
        "title": "Investigation into Invert Silt Accumulation",
        "description": "Checking drain line blockage",
        "asset_id": "511a2c72-86f0-4c1a-9509-09c928410811",
        "asset_code": "NAG-CB-000001",
        "priority": "HIGH"
    }

    create_resp = client.post("/api/v1/investigations", json=payload, headers=headers)
    assert create_resp.status_code == 201
    data = create_resp.json()
    assert data["id"] == "INV-TEST-99"
    assert data["status"] == "OPEN"
    assert len(data["timeline_events"]) == 1

    get_resp = client.get("/api/v1/investigations/INV-TEST-99")
    assert get_resp.status_code == 200
    assert get_resp.json()["title"] == "Investigation into Invert Silt Accumulation"


def test_investigation_state_machine_transitions():
    headers = get_auth_headers()
    # Valid transition: OPEN -> TRIAGED
    patch_resp = client.patch(
        "/api/v1/investigations/INV-TEST-99/status",
        json={"target_status": "TRIAGED", "comment": "Triage complete."},
        headers=headers
    )
    assert patch_resp.status_code == 200
    assert patch_resp.json()["status"] == "TRIAGED"

    # Invalid transition: TRIAGED -> RESOLVED (must go through INVESTIGATING/REVIEW first)
    invalid_resp = client.patch(
        "/api/v1/investigations/INV-TEST-99/status",
        json={"target_status": "RESOLVED", "comment": "Bypassing workflow!"},
        headers=headers
    )
    assert invalid_resp.status_code == 422
    assert "Invalid state transition" in invalid_resp.json()["detail"]


def test_create_evidence_request():
    headers = get_auth_headers()
    payload = {
        "investigation_id": "INV-10001",
        "asset_id": "511a2c72-86f0-4c1a-9509-09c928410811",
        "requested_evidence_type": "PHOTO_CLOSEUP_INVERT",
        "instructions": "Take high-angle photo of grate invert channel.",
        "assigned_to": "usr_field_inspector"
    }

    resp = client.post("/api/v1/investigations/INV-10001/evidence-requests", json=payload, headers=headers)
    assert resp.status_code == 201
    data = resp.json()
    assert data["requested_evidence_type"] == "PHOTO_CLOSEUP_INVERT"
    assert data["status"] == "PENDING"


def test_approval_gate_and_action_execution():
    headers_reviewer = get_auth_headers(role="reviewer")

    # Approve pending request
    app_resp = client.post(
        "/api/v1/automation/approvals/APP-1001",
        json={"status": "APPROVED", "reviewer_notes": "Emergency desilting approved."},
        headers=headers_reviewer
    )
    assert app_resp.status_code == 200
    assert app_resp.json()["status"] == "APPROVED"

    # Execute action with valid approval
    exec_resp = client.post(
        "/api/v1/automation/execute?rule_id=RULE-001&investigation_id=INV-10001&approval_id=APP-1001",
        headers=headers_reviewer
    )
    assert exec_resp.status_code == 200
    assert exec_resp.json()["status"] == "SUCCEEDED"
