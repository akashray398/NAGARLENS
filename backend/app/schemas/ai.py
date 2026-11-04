"""NAGARLENS — AI Assessment Schemas and Contracts."""

from enum import Enum
from typing import List, Optional
from pydantic import BaseModel, Field

from app.ml.taxonomy import DefectCategory


class ProcessingStatus(str, Enum):
    COMPLETED = "COMPLETED"
    MODEL_UNAVAILABLE = "MODEL_UNAVAILABLE"
    INSUFFICIENT_EVIDENCE = "INSUFFICIENT_EVIDENCE"
    FAILED = "FAILED"


class ImageQualityStatus(str, Enum):
    PASS = "PASS"
    FAIL_CORRUPTED = "FAIL_CORRUPTED"
    FAIL_LOW_RESOLUTION = "FAIL_LOW_RESOLUTION"
    FAIL_DARK = "FAIL_DARK"
    FAIL_BLURRY = "FAIL_BLURRY"


class BoundingBoxSchema(BaseModel):
    category: DefectCategory
    confidence: float = Field(ge=0.0, le=1.0)
    x_min: float = Field(ge=0.0, le=1.0)
    y_min: float = Field(ge=0.0, le=1.0)
    x_max: float = Field(ge=0.0, le=1.0)
    y_max: float = Field(ge=0.0, le=1.0)


class AiAssessmentSchema(BaseModel):
    assessment_id: str
    evidence_id: str
    model_name: str
    model_version: str
    taxonomy_version: str
    predicted_category: DefectCategory
    confidence_score: float = Field(ge=0.0, le=1.0)
    bounding_boxes: List[BoundingBoxSchema] = Field(default_factory=list)
    image_quality_status: ImageQualityStatus
    processing_status: ProcessingStatus
    message: str
    inference_timestamp_utc: int


class ModelRegistryItemSchema(BaseModel):
    model_name: str
    model_version: str
    taxonomy_version: str
    architecture: str
    is_active: bool
    status_message: str
