"""NAGARLENS — Pydantic Schemas for Temporal Reality Graph."""

from typing import List, Optional
from pydantic import BaseModel, Field

from app.ml.taxonomy import DefectCategory
from app.modules.temporal.enums import (
    ConditionSeverity,
    InterventionEvidenceType,
    InterventionStatus,
    InterventionType,
    ObservationType,
    VerificationOutcome,
)


class ObservationCreateSchema(BaseModel):
    id: Optional[str] = None  # Client UUID or server generated
    observation_type: ObservationType
    condition_category: DefectCategory
    condition_severity: ConditionSeverity = ConditionSeverity.UNKNOWN
    description: Optional[str] = None
    observed_at_utc: int  # Field capture timestamp
    latitude: float = Field(ge=-90.0, le=90.0)
    longitude: float = Field(ge=-180.0, le=180.0)
    location_accuracy_meters: Optional[float] = None
    evidence_id: Optional[str] = None  # Optional initial linked evidence ID


class ObservationResponseSchema(BaseModel):
    id: str
    asset_id: str
    observation_type: ObservationType
    condition_category: DefectCategory
    condition_severity: ConditionSeverity
    description: Optional[str] = None
    observed_at_utc: int
    recorded_at_utc: int
    observed_by: str
    latitude: float
    longitude: float
    location_accuracy_meters: Optional[float] = None


class InterventionCreateSchema(BaseModel):
    id: Optional[str] = None
    intervention_type: InterventionType
    title: str
    description: Optional[str] = None
    planned_at_utc: Optional[int] = None
    before_evidence_id: Optional[str] = None


class InterventionStatusUpdateSchema(BaseModel):
    new_status: InterventionStatus
    reason: Optional[str] = None


class InterventionStatusHistorySchema(BaseModel):
    id: str
    intervention_id: str
    previous_status: InterventionStatus
    new_status: InterventionStatus
    reason: Optional[str] = None
    actor_id: str
    recorded_at_utc: int


class InterventionResponseSchema(BaseModel):
    id: str
    asset_id: str
    intervention_type: InterventionType
    title: str
    description: Optional[str] = None
    status: InterventionStatus
    planned_at_utc: Optional[int] = None
    started_at_utc: Optional[int] = None
    completed_at_utc: Optional[int] = None
    recorded_by: str
    created_at_utc: int
    updated_at_utc: int


class OutcomeVerificationCreateSchema(BaseModel):
    id: Optional[str] = None
    verification_observation_id: str
    outcome: VerificationOutcome
    description: Optional[str] = None


class OutcomeVerificationResponseSchema(BaseModel):
    id: str
    intervention_id: str
    asset_id: str
    verification_observation_id: str
    outcome: VerificationOutcome
    description: Optional[str] = None
    verified_at_utc: int
    verified_by: str


class TimelineEventSchema(BaseModel):
    event_id: str
    event_type: str  # REGISTRATION, OBSERVATION, INTERVENTION, VERIFICATION
    timestamp_utc: int
    title: str
    description: Optional[str] = None
    actor_id: str
    severity: Optional[str] = None
    outcome: Optional[str] = None
    linked_evidence_id: Optional[str] = None


class AssetTimelineResponseSchema(BaseModel):
    asset_id: str
    asset_code: str
    total_events: int
    events: List[TimelineEventSchema]
