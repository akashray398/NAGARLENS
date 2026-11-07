"""Unit tests for Authentication & Identity Endpoints."""

from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_login_success():
    response = client.post(
        "/api/v1/auth/token",
        json={"username": "auditor_central", "password": "password"}
    )
    assert response.status_code == 200
    data = response.json()
    assert "access_token" in data
    assert "refresh_token" in data
    assert data["role"] == "field_worker"


def test_login_invalid_password():
    response = client.post(
        "/api/v1/auth/token",
        json={"username": "auditor_central", "password": "wrongpassword"}
    )
    assert response.status_code == 401


def test_user_registration():
    response = client.post(
        "/api/v1/auth/register",
        json={
            "username": "new_inspector_99",
            "email": "inspector99@bbmp.gov.in",
            "password": "securepassword123",
            "full_name": "Anita Rao",
            "role": "field_worker",
            "assigned_ward": "Ward 14"
        }
    )
    assert response.status_code == 201
    data = response.json()
    assert data["username"] == "new_inspector_99"
    assert data["assigned_ward"] == "Ward 14"


def test_refresh_token_rotation():
    login_resp = client.post(
        "/api/v1/auth/token",
        json={"username": "auditor_central", "password": "password"}
    )
    refresh_token = login_resp.json()["refresh_token"]

    refresh_resp = client.post(
        "/api/v1/auth/refresh",
        json={"refresh_token": refresh_token}
    )
    assert refresh_resp.status_code == 200
    assert "access_token" in refresh_resp.json()
