"""NAGARLENS — Cryptographic JWT Authentication & Security Unit Tests."""

import time
import jwt
import pytest
from fastapi.testclient import TestClient

from app.core.config import settings
from app.main import app
from app.modules.assets.enums import AssetCategory, AssetLifecycleStatus, AssetVerificationStatus
from app.modules.assets.schemas import AssetCreateSchema, DuplicateCandidateSchema

client = TestClient(app)


def create_test_jwt(
    user_id: str,
    role: str,
    expires_in: int = 3600,
    issuer: str = None,
    audience: str = None,
    secret_key: str = None,
) -> str:
    """Helper function to generate cryptographically signed test JWT tokens."""
    now = int(time.time())
    payload = {
        "sub": user_id,
        "user_id": user_id,
        "role": role,
        "username": f"user_{user_id}",
        "iss": issuer or settings.JWT_ISSUER,
        "aud": audience or settings.JWT_AUDIENCE,
        "iat": now,
        "exp": now + expires_in,
    }
    key = secret_key or settings.SECRET_KEY
    return jwt.encode(payload, key, algorithm=settings.JWT_ALGORITHM)


def test_asset_category_prefix_mapping():
    assert AssetCategory.get_prefix(AssetCategory.CATCH_BASIN) == "CB"
    assert AssetCategory.get_prefix(AssetCategory.STORM_WATER_DRAIN) == "SWD"
    assert AssetCategory.get_prefix(AssetCategory.MANHOLE) == "MH"
    assert AssetCategory.get_prefix(AssetCategory.CULVERT) == "CVT"


def test_asset_create_schema_validates_coordinate_bounds():
    valid = AssetCreateSchema(
        asset_category=AssetCategory.CATCH_BASIN,
        latitude=12.9716,
        longitude=77.5946,
        description="Drain Catch Basin at 4th Cross",
    )
    assert valid.latitude == 12.9716
    assert valid.longitude == 77.5946
    assert valid.asset_category == AssetCategory.CATCH_BASIN

    with pytest.raises(Exception):
        AssetCreateSchema(
            asset_category=AssetCategory.CATCH_BASIN,
            latitude=120.0,  # Invalid latitude (>90)
            longitude=77.5946,
        )


def test_duplicate_candidate_schema_formatting():
    candidate = DuplicateCandidateSchema(
        candidate_asset_id="AST-101",
        candidate_asset_code="NAG-CB-000101-A1B2",
        asset_category=AssetCategory.CATCH_BASIN,
        distance_meters=4.2,
        verification_status=AssetVerificationStatus.VERIFIED,
        advisory_message="Matching category asset NAG-CB-000101-A1B2 found within 4.2m.",
    )
    assert candidate.distance_meters == 4.2
    assert candidate.verification_status == AssetVerificationStatus.VERIFIED
    assert "NAG-CB-000101" in candidate.advisory_message


def test_verification_missing_bearer_token_rejected_401():
    response = client.post("/api/v1/assets/dummy_asset_id/verification?decision=APPROVED")
    assert response.status_code == 401
    assert "Authentication required" in response.json()["detail"]


def test_verification_invalid_jwt_signature_rejected_401():
    invalid_token = create_test_jwt("reviewer_01", "reviewer", secret_key="WRONG_INVALID_SECRET_KEY_12345678")
    headers = {"Authorization": f"Bearer {invalid_token}"}
    response = client.post("/api/v1/assets/dummy_asset_id/verification?decision=APPROVED", headers=headers)
    assert response.status_code == 401
    assert "Invalid or corrupt JWT signature" in response.json()["detail"]


def test_verification_expired_jwt_rejected_401():
    expired_token = create_test_jwt("reviewer_01", "reviewer", expires_in=-3600)  # Expired 1 hour ago
    headers = {"Authorization": f"Bearer {expired_token}"}
    response = client.post("/api/v1/assets/dummy_asset_id/verification?decision=APPROVED", headers=headers)
    assert response.status_code == 401
    assert "token has expired" in response.json()["detail"]


def test_verification_incorrect_jwt_issuer_rejected_401():
    bad_issuer_token = create_test_jwt("reviewer_01", "reviewer", issuer="untrusted-malicious-issuer")
    headers = {"Authorization": f"Bearer {bad_issuer_token}"}
    response = client.post("/api/v1/assets/dummy_asset_id/verification?decision=APPROVED", headers=headers)
    assert response.status_code == 401
    assert "Invalid token issuer" in response.json()["detail"]


def test_verification_field_worker_role_rejected_403():
    worker_token = create_test_jwt("worker_101", "field_worker")
    headers = {"Authorization": f"Bearer {worker_token}"}
    response = client.post("/api/v1/assets/dummy_asset_id/verification?decision=APPROVED", headers=headers)
    assert response.status_code == 403
    assert "Forbidden" in response.json()["detail"]


def test_client_supplied_header_cannot_override_jwt_claims():
    # Valid field_worker token + spoofed client header X-User-Role: administrator
    worker_token = create_test_jwt("worker_101", "field_worker")
    headers = {
        "Authorization": f"Bearer {worker_token}",
        "X-User-Role": "administrator",  # Attempt spoofing
        "X-User-Id": "admin_spoofed",
    }
    response = client.post("/api/v1/assets/dummy_asset_id/verification?decision=APPROVED", headers=headers)
    # Must STILL be rejected with 403 because role is derived ONLY from verified JWT claims!
    assert response.status_code == 403
    assert "Forbidden" in response.json()["detail"]
