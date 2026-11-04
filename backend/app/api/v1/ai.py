"""NAGARLENS — AI Inference & Taxonomy API Endpoints."""

import time
import uuid
from typing import List
from fastapi import APIRouter, HTTPException, status

from app.ml.taxonomy import TAXONOMY_REGISTRY, TAXONOMY_VERSION, DefectCategory
from app.schemas.ai import (
    AiAssessmentSchema,
    ImageQualityStatus,
    ModelRegistryItemSchema,
    ProcessingStatus,
)

router = APIRouter(prefix="/ai", tags=["AI & Computer Vision"])


@router.get("/taxonomy", summary="Retrieve versioned drainage defect taxonomy")
async def get_taxonomy():
    return {
        "taxonomy_version": TAXONOMY_VERSION,
        "categories": [
            {
                "category_id": spec.category_id.name,
                "name": spec.name,
                "description": spec.description,
                "inclusion_criteria": spec.inclusion_criteria,
                "exclusion_criteria": spec.exclusion_criteria,
            }
            for spec in TAXONOMY_REGISTRY.values()
        ],
    }


@router.get("/models", response_model=List[ModelRegistryItemSchema], summary="Retrieve active computer vision model registry")
async def list_models():
    return [
        ModelRegistryItemSchema(
            model_name="MobileNetV3-Small-DrainDefect",
            model_version="v1.0.0-foundation",
            taxonomy_version=TAXONOMY_VERSION,
            architecture="MobileNetV3-Small",
            is_active=False,
            status_message="Model architecture foundation ready. Awaiting validated training dataset artifacts.",
        ),
        ModelRegistryItemSchema(
            model_name="YOLOv8-Nano-DrainLocalization",
            model_version="v1.0.0-foundation",
            taxonomy_version=TAXONOMY_VERSION,
            architecture="YOLOv8-Nano",
            is_active=False,
            status_message="Object detection model foundation ready. Awaiting annotated bounding box artifacts.",
        ),
    ]


@router.post(
    "/assessments",
    response_model=AiAssessmentSchema,
    status_code=status.HTTP_200_OK,
    summary="Generate AI assessment for an evidence item (Returns explicit MODEL_UNAVAILABLE when weights pending)",
)
async def create_assessment(evidence_id: str):
    if not evidence_id or len(evidence_id) < 5:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="A valid evidence_id is required.",
        )

    # Return explicit MODEL_UNAVAILABLE contract when model weights are pending training
    return AiAssessmentSchema(
        assessment_id=str(uuid.uuid4()),
        evidence_id=evidence_id,
        model_name="MobileNetV3-Small-DrainDefect",
        model_version="v1.0.0-foundation",
        taxonomy_version=TAXONOMY_VERSION,
        predicted_category=DefectCategory.UNKNOWN_INSUFFICIENT,
        confidence_score=0.0,
        bounding_boxes=[],
        image_quality_status=ImageQualityStatus.PASS,
        processing_status=ProcessingStatus.MODEL_UNAVAILABLE,
        message="No trained computer vision model binary is currently active on the server. Evidence remains safely preserved.",
        inference_timestamp_utc=int(time.time() * 1000),
    )
