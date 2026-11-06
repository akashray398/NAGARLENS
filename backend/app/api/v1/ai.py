"""NAGARLENS — AI Inference & Taxonomy API Endpoints."""

import time
import uuid
from typing import List, Optional
from fastapi import APIRouter, File, HTTPException, UploadFile, status

from app.ml.inference import VisionInferenceEngine
from app.ml.taxonomy import TAXONOMY_REGISTRY, TAXONOMY_VERSION, DefectCategory
from app.schemas.ai import (
    AiAssessmentSchema,
    ImageQualityStatus,
    ModelRegistryItemSchema,
    ProcessingStatus,
)

router = APIRouter(prefix="/ai", tags=["AI & Computer Vision"])
inference_engine = VisionInferenceEngine()


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
            is_active=inference_engine.is_weights_loaded,
            status_message="Model architecture foundation ready. Integrated with PIL ImagePreprocessor & NMS.",
        ),
        ModelRegistryItemSchema(
            model_name="RT-DETR-Nano-DrainLocalization",
            model_version="v1.0.0-foundation",
            taxonomy_version=TAXONOMY_VERSION,
            architecture="RT-DETR-Nano (Apache 2.0 / Commercial Safe)",
            is_active=inference_engine.is_weights_loaded,
            status_message="Object detection model architecture selected (Licensing: Apache 2.0).",
        ),
    ]


@router.post(
    "/assessments",
    response_model=AiAssessmentSchema,
    status_code=status.HTTP_200_OK,
    summary="Generate AI assessment for an evidence item (Validates image input and returns explicit contract)",
)
async def create_assessment(
    evidence_id: str,
    file: Optional[UploadFile] = File(None)
):
    if not evidence_id or len(evidence_id) < 3:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="A valid evidence_id is required.",
        )

    if file:
        raw_bytes = await file.read()
        return inference_engine.analyze_image_bytes(evidence_id, raw_bytes)

    # Fallback response when image binary upload is handled asynchronously via S3 bucket
    return AiAssessmentSchema(
        assessment_id=str(uuid.uuid4()),
        evidence_id=evidence_id,
        model_name=inference_engine.model_name,
        model_version=inference_engine.model_version,
        taxonomy_version=TAXONOMY_VERSION,
        predicted_category=DefectCategory.UNKNOWN_INSUFFICIENT,
        confidence_score=0.0,
        bounding_boxes=[],
        image_quality_status=ImageQualityStatus.PASS,
        processing_status=ProcessingStatus.MODEL_UNAVAILABLE,
        message="No trained computer vision model binary is currently active on the server. Evidence remains safely preserved.",
        inference_timestamp_utc=int(time.time() * 1000),
    )
