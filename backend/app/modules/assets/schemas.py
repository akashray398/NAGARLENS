"""NAGARLENS — Pydantic Asset Domain Schemas."""

from typing import List, Optional
from pydantic import BaseModel, Field

from app.modules.assets.enums import (
    AssetCategory,
    AssetLifecycleStatus,
    AssetRelationshipType,
    AssetVerificationStatus,
    IdentifierType,
    VerificationDecision,
)


class AssetCreateSchema(BaseModel):
    id: Optional[str] = None  # Client permanent offline UUID or server-generated
    asset_code: Optional[str] = None  # Client operational code or server-generated
    asset_category: AssetCategory
    latitude: float = Field(ge=-90.0, le=90.0)
    longitude: float = Field(ge=-180.0, le=180.0)
    altitude_meters: Optional[float] = None
    description: Optional[str] = None
    created_by: str = "field_worker"
    evidence_id: Optional[str] = None  # Optional initial linked evidence ID


class AssetUpdateSchema(BaseModel):
    asset_category: Optional[AssetCategory] = None
    lifecycle_status: Optional[AssetLifecycleStatus] = None
    latitude: Optional[float] = Field(default=None, ge=-90.0, le=90.0)
    longitude: Optional[float] = Field(default=None, ge=-180.0, le=180.0)
    altitude_meters: Optional[float] = None
    description: Optional[str] = None
    correction_reason: Optional[str] = None
    actor_id: str = "field_worker"
    version: int  # Optimistic concurrency lock version


class AssetIdentifierSchema(BaseModel):
    id: str
    asset_id: str
    identifier_type: IdentifierType
    identifier_value: str
    is_active: bool
    created_timestamp_utc: int
    revoked_timestamp_utc: Optional[int] = None


class AssetEvidenceLinkSchema(BaseModel):
    id: str
    asset_id: str
    evidence_id: str
    relationship_type: AssetRelationshipType
    created_by: str
    created_timestamp_utc: int


class AssetVerificationSchema(BaseModel):
    id: str
    asset_id: str
    decision: VerificationDecision
    reason: Optional[str] = None
    reviewer_id: str
    decision_timestamp_utc: int


class AssetLocationHistorySchema(BaseModel):
    id: str
    asset_id: str
    previous_latitude: float
    previous_longitude: float
    new_latitude: float
    new_longitude: float
    reason: Optional[str] = None
    actor_id: str
    recorded_timestamp_utc: int


class AssetResponseSchema(BaseModel):
    id: str
    asset_code: str
    asset_category: AssetCategory
    lifecycle_status: AssetLifecycleStatus
    verification_status: AssetVerificationStatus
    latitude: float
    longitude: float
    altitude_meters: Optional[float] = None
    description: Optional[str] = None
    version: int
    created_by: str
    created_timestamp_utc: int
    updated_timestamp_utc: int


class DuplicateCandidateSchema(BaseModel):
    candidate_asset_id: str
    candidate_asset_code: str
    asset_category: AssetCategory
    distance_meters: float
    verification_status: AssetVerificationStatus
    advisory_message: str


class ViewportQuerySchema(BaseModel):
    min_latitude: float = Field(ge=-90.0, le=90.0)
    min_longitude: float = Field(ge=-180.0, le=180.0)
    max_latitude: float = Field(ge=-90.0, le=90.0)
    max_longitude: float = Field(ge=-180.0, le=180.0)
    category: Optional[AssetCategory] = None
    verification_status: Optional[AssetVerificationStatus] = None
