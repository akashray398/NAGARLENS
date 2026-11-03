import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_health_live_endpoint() -> None:
    response = client.get("/api/v1/health/live")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "alive"
    assert "environment" in data
    assert "version" in data


def test_root_health_redirect() -> None:
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["service"] == "NAGARLENS"
    assert data["status"] == "alive"
    assert data["readiness_endpoint"] == "/api/v1/health/ready"


@pytest.mark.asyncio
async def test_health_ready_unhealthy_when_db_down() -> None:
    # When database connection fails (no active DB in unit test mode), /ready returns HTTP 503
    response = client.get("/api/v1/health/ready")
    assert response.status_code == 503
    data = response.json()
    assert data["status"] == "unhealthy"
    assert data["components"]["api"]["status"] == "healthy"
    assert data["components"]["database"]["status"] == "unhealthy"
    assert data["components"]["database"]["connected"] is False
