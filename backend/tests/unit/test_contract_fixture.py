"""NAGARLENS — Multi-Platform Contract Compatibility Tests for Shared Fixture."""

import json
from pathlib import Path
from app.schemas.ai import AiAssessmentSchema, DefectCategory, ProcessingStatus, ImageQualityStatus


def test_shared_fixture_deserializes_into_pydantic_schema():
    fixture_path = Path(__file__).parents[3] / "shared_fixtures" / "ai_assessment_fixture.json"
    assert fixture_path.exists(), f"Shared fixture not found at {fixture_path}"

    with open(fixture_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    assessment = AiAssessmentSchema(**data)

    assert assessment.assessment_id == "8f3b2d10-4e5a-4b2c-9a1b-3c4d5e6f7a8b"
    assert assessment.evidence_id == "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d"
    assert assessment.predicted_category == DefectCategory.SOLID_WASTE
    assert assessment.confidence_score == 0.942
    assert assessment.image_quality_status == ImageQualityStatus.PASS
    assert assessment.processing_status == ProcessingStatus.COMPLETED
    assert len(assessment.bounding_boxes) == 1
    assert assessment.bounding_boxes[0].category == DefectCategory.SOLID_WASTE
    assert assessment.bounding_boxes[0].x_min == 0.25
    assert assessment.bounding_boxes[0].y_min == 0.30
    assert assessment.bounding_boxes[0].x_max == 0.65
    assert assessment.bounding_boxes[0].y_max == 0.75
