"""NAGARLENS — Unit Tests for AI API Endpoints."""

from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_get_taxonomy_endpoint():
    response = client.get("/api/v1/ai/taxonomy")
    assert response.status_code == 200
    data = response.json()
    assert data["taxonomy_version"] == "v1.0.0"
    assert len(data["categories"]) == 9


def test_list_models_endpoint():
    response = client.get("/api/v1/ai/models")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 2
    assert data[0]["model_name"] == "MobileNetV3-Small-DrainDefect"


def test_create_assessment_returns_model_unavailable():
    response = client.post("/api/v1/ai/assessments?evidence_id=123456")
    assert response.status_code == 200
    data = response.json()
    assert data["evidence_id"] == "123456"
    assert data["processing_status"] == "MODEL_UNAVAILABLE"
    assert data["confidence_score"] == 0.0
