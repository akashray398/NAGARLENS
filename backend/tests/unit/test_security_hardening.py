"""Unit tests for Enterprise Security Hardening, Middleware & Privacy Blurring."""

from fastapi.testclient import TestClient
from app.main import app
from app.ml.preprocessing.privacy_blur import PrivacyBlurProcessor
from PIL import Image
import io

client = TestClient(app)


def test_security_headers_middleware_present():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.headers.get("X-Content-Type-Options") == "nosniff"
    assert response.headers.get("X-Frame-Options") == "DENY"
    assert "Strict-Transport-Security" in response.headers


def test_privacy_blur_processor_redacts_sensitive_regions():
    processor = PrivacyBlurProcessor()
    img = Image.new("RGB", (100, 100), color=(255, 0, 0))
    buf = io.BytesIO()
    img.save(buf, format="JPEG")
    raw_bytes = buf.getvalue()

    boxes = [
        {"category": "PERSON_FACE", "x_min": 0.1, "y_min": 0.1, "x_max": 0.5, "y_max": 0.5}
    ]

    blurred_bytes = processor.apply_privacy_blur(raw_bytes, bounding_boxes=boxes)
    assert blurred_bytes is not None
    assert len(blurred_bytes) > 0


def test_unauthenticated_request_rejected_401():
    resp = client.post("/api/v1/observations", json={})
    assert resp.status_code == 401
