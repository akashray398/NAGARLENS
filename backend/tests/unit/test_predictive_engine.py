"""Unit tests for Predictive Risk Engine & Early Warning System."""

import time
import jwt
from fastapi.testclient import TestClient
from app.core.config import settings
from app.main import app
from app.ml.predictive.engine import PredictiveRiskEngine, RiskLevel
from app.ml.taxonomy import DefectCategory

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


def test_predictive_risk_evaluation_insufficient_data():
    engine = PredictiveRiskEngine()
    alert = engine.evaluate_asset_risk(
        asset_id="AST-EMPTY-001",
        asset_code="NAG-CB-000000",
        observations=[],
        incidents=[]
    )
    assert alert.risk_level == RiskLevel.INFORMATIONAL
    assert alert.confidence_score == 0.20
    assert alert.primary_defect_category == DefectCategory.UNKNOWN_INSUFFICIENT


def test_predictive_risk_evaluation_high_risk():
    engine = PredictiveRiskEngine()
    observations = [
        {"id": "o1", "defect_category": "DRAIN_BLOCKAGE", "severity_score": 0.40},
        {"id": "o2", "defect_category": "DRAIN_BLOCKAGE", "severity_score": 0.65},
        {"id": "o3", "defect_category": "DRAIN_BLOCKAGE", "severity_score": 0.85},
        {"id": "o4", "defect_category": "DRAIN_BLOCKAGE", "severity_score": 0.90}
    ]
    incidents = [{"id": "INC-1"}]
    nearby_risks = [0.80, 0.70]

    alert = engine.evaluate_asset_risk(
        asset_id="AST-HIGH-001",
        asset_code="NAG-CB-000104",
        observations=observations,
        incidents=incidents,
        nearby_asset_risks=nearby_risks
    )
    assert alert.risk_level in (RiskLevel.HIGH, RiskLevel.CRITICAL)
    assert alert.risk_score >= 0.65
    assert len(alert.contributing_factors) >= 3


def test_anomaly_detection_z_score_spike():
    engine = PredictiveRiskEngine()
    observations = [
        {"severity_score": 0.20},
        {"severity_score": 0.25},
        {"severity_score": 0.22},
        {"severity_score": 0.90}  # Sudden spike
    ]
    result = engine.detect_anomalies(observations)
    assert result.is_anomaly is True
    assert result.anomaly_type == "SUDDEN_SEVERITY_SPIKE"
    assert result.z_score > 2.0


def test_predictive_api_endpoints():
    headers = get_auth_headers()
    eval_resp = client.post(
        "/api/v1/predictive/evaluate",
        json={
            "asset_id": "AST-TEST-999",
            "asset_code": "NAG-CB-999999",
            "observations": [{"id": "o1", "defect_category": "DRAIN_BLOCKAGE", "severity_score": 0.70}]
        },
        headers=headers
    )
    assert eval_resp.status_code == 200
    assert eval_resp.json()["asset_id"] == "AST-TEST-999"

    get_resp = client.get("/api/v1/predictive/assets/AST-TEST-999/risk")
    assert get_resp.status_code == 200
    assert get_resp.json()["asset_id"] == "AST-TEST-999"

    feedback_resp = client.post(
        "/api/v1/predictive/feedback",
        json={
            "warning_id": "WARN-AST-TEST",
            "asset_id": "AST-TEST-999",
            "feedback": "CONFIRMED",
            "reviewer_notes": "Desilting team confirmed heavy silt accumulation."
        },
        headers=headers
    )
    assert feedback_resp.status_code == 200
    assert feedback_resp.json()["is_human_reviewed"] is True
