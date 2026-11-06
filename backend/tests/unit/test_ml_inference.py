"""Unit tests for Machine Learning Vision Inference Engine, NMS Processor, and Severity Assessment."""

import io
from PIL import Image
from app.ml.inference import NmsProcessor, SeverityAssessmentEngine, SeverityLevel, VisionInferenceEngine
from app.ml.taxonomy import DefectCategory
from app.schemas.ai import BoundingBoxSchema, ProcessingStatus


def test_severity_assessment_calculation():
    boxes = [
        BoundingBoxSchema(
            category=DefectCategory.COVER_DAMAGE_MISSING,
            confidence=0.92,
            x_min=0.10,
            y_min=0.10,
            x_max=0.60,
            y_max=0.60
        )
    ]

    severity = SeverityAssessmentEngine.calculate_severity(
        category=DefectCategory.COVER_DAMAGE_MISSING,
        confidence=0.92,
        bounding_boxes=boxes
    )

    assert severity.severity_score >= 0.60
    assert severity.severity_level in (SeverityLevel.MEDIUM, SeverityLevel.HIGH, SeverityLevel.CRITICAL)
    assert severity.is_photographic_estimate is True


def test_nms_bounding_box_filtering():
    box1 = BoundingBoxSchema(category=DefectCategory.SOLID_WASTE, confidence=0.95, x_min=0.10, y_min=0.10, x_max=0.50, y_max=0.50)
    box2 = BoundingBoxSchema(category=DefectCategory.SOLID_WASTE, confidence=0.80, x_min=0.12, y_min=0.12, x_max=0.52, y_max=0.52)  # High IoU with box1
    box3 = BoundingBoxSchema(category=DefectCategory.DRAIN_BLOCKAGE, confidence=0.90, x_min=0.70, y_min=0.70, x_max=0.90, y_max=0.90)  # Separate box

    filtered = NmsProcessor.apply_nms([box1, box2, box3], iou_threshold=0.40)

    assert len(filtered) == 2
    assert filtered[0].confidence == 0.95
    assert filtered[1].confidence == 0.90


def test_vision_inference_engine_with_sample_image():
    engine = VisionInferenceEngine()

    # Create a valid test image
    img = Image.new("RGB", (300, 300), color="blue")
    buffer = io.BytesIO()
    img.save(buffer, format="JPEG")
    raw_bytes = buffer.getvalue()

    assessment = engine.analyze_image_bytes(
        evidence_id="EVID-TEST-100",
        image_bytes=raw_bytes
    )

    assert assessment.evidence_id == "EVID-TEST-100"
    assert assessment.processing_status == ProcessingStatus.MODEL_UNAVAILABLE
