"""NAGARLENS — Backend Performance & Throughput Benchmark Unit Tests."""

import time
import io
from PIL import Image
from fastapi.testclient import TestClient
from app.main import app
from app.ml.preprocessing.pipeline import ImagePreprocessor
from app.ml.predictive.engine import PredictiveRiskEngine
from app.ml.inference import NmsProcessor
from app.schemas.ai import BoundingBoxSchema, DefectCategory

client = TestClient(app)


def test_api_health_latency_benchmark():
    start = time.perf_counter()
    for _ in range(50):
        response = client.get("/health")
        assert response.status_code == 200
    elapsed_ms = (time.perf_counter() - start) * 1000
    avg_latency_ms = elapsed_ms / 50.0
    # Health endpoint latency must be < 15ms per request
    assert avg_latency_ms < 15.0


def test_image_preprocessing_throughput_benchmark():
    preprocessor = ImagePreprocessor()
    img = Image.new("RGB", (640, 480), color=(100, 150, 200))
    buf = io.BytesIO()
    img.save(buf, format="JPEG")
    raw_bytes = buf.getvalue()

    start = time.perf_counter()
    iterations = 20
    for _ in range(iterations):
        res = preprocessor.process_image_bytes(raw_bytes)
        assert res.is_valid is True

    elapsed_s = time.perf_counter() - start
    fps = iterations / elapsed_s
    # Preprocessing throughput must exceed 20 frames/sec on CPU
    assert fps > 20.0


def test_predictive_engine_evaluation_latency_benchmark():
    engine = PredictiveRiskEngine()
    observations = [
        {"id": f"obs_{i}", "defect_category": "DRAIN_BLOCKAGE", "severity_score": 0.60 + (i * 0.05)}
        for i in range(10)
    ]

    start = time.perf_counter()
    for _ in range(100):
        alert = engine.evaluate_asset_risk(
            asset_id="AST-BENCHMARK-01",
            asset_code="NAG-CB-000104",
            observations=observations,
            incidents=[{"id": "INC-1"}]
        )
        assert alert.risk_score >= 0.50

    elapsed_ms = (time.perf_counter() - start) * 1000
    avg_ms = elapsed_ms / 100.0
    # Predictive risk evaluation latency must be < 5ms
    assert avg_ms < 5.0
