"""NAGARLENS — Evidence Ingestion & Observation Router."""

import time
import uuid
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, File, UploadFile, status
from pydantic import BaseModel, Field

from app.core.security import AuthenticatedUser, get_current_user
from app.core.storage import UploadPresignedUrlResponse, storage_service
from app.ml.taxonomy import DefectCategory

router = APIRouter(prefix="/observations", tags=["Evidence & Observation Ingestion"])


class ObservationCreateSchema(BaseModel):
    id: Optional[str] = Field(default=None, description="Client-generated UUIDv4")
    asset_id: str
    observation_type: str = "ROUTINE_INSPECTION"
    defect_category: DefectCategory = DefectCategory.DRAIN_BLOCKAGE
    condition_severity: str = "MODERATE"
    description: Optional[str] = None
    latitude: float
    longitude: float
    accuracy_meters: Optional[float] = None
    sha256_hash: str


class ObservationResponseSchema(BaseModel):
    id: str
    asset_id: str
    observation_type: str
    defect_category: DefectCategory
    condition_severity: str
    description: Optional[str]
    latitude: float
    longitude: float
    accuracy_meters: Optional[float]
    sha256_hash: str
    observer_id: str
    created_timestamp_utc: int


_OBSERVATION_DB: dict[str, dict] = {}


@router.post("", response_model=ObservationResponseSchema, status_code=status.HTTP_201_CREATED, summary="Create a new infrastructure observation record")
async def create_observation(
    data: ObservationCreateSchema,
    current_user: AuthenticatedUser = Depends(get_current_user)
):
    obs_id = data.id or str(uuid.uuid4())
    now = int(time.time() * 1000)

    record = {
        "id": obs_id,
        "asset_id": data.asset_id,
        "observation_type": data.observation_type,
        "defect_category": data.defect_category,
        "condition_severity": data.condition_severity,
        "description": data.description,
        "latitude": data.latitude,
        "longitude": data.longitude,
        "accuracy_meters": data.accuracy_meters,
        "sha256_hash": data.sha256_hash,
        "observer_id": current_user.user_id,
        "created_timestamp_utc": now
    }
    _OBSERVATION_DB[obs_id] = record
    return record


@router.get("/{observation_id}", response_model=ObservationResponseSchema, summary="Retrieve observation record by ID")
async def get_observation(observation_id: str):
    record = _OBSERVATION_DB.get(observation_id)
    if not record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Observation with ID '{observation_id}' not found."
        )
    return record


@router.post("/upload-url", response_model=UploadPresignedUrlResponse, summary="Request presigned S3 upload URL for evidence media binary")
async def request_upload_url(
    evidence_id: str,
    original_filename: str = "field_photo.jpg",
    mime_type: str = "image/jpeg",
    current_user: AuthenticatedUser = Depends(get_current_user)
):
    return storage_service.generate_presigned_upload_url(
        evidence_id=evidence_id,
        original_filename=original_filename,
        mime_type=mime_type
    )
