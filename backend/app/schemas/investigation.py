"""NAGARLENS — Investigation Domain Schemas & Lifecycle State Machine."""

import time
from enum import Enum
from typing import List, Optional
from pydantic import BaseModel, Field

from app.ml.taxonomy import DefectCategory


class InvestigationStatus(str, Enum):
    DRAFT = "DRAFT"
    OPEN = "OPEN"
    TRIAGED = "TRIAGED"
    INVESTIGATING = "INVESTIGATING"
    AWAITING_EVIDENCE = "AWAITING_EVIDENCE"
    AWAITING_REVIEW = "AWAITING_REVIEW"
    ACTION_PROPOSED = "ACTION_PROPOSED"
    AWAITING_APPROVAL = "AWAITING_APPROVAL"
    ACTION_IN_PROGRESS = "ACTION_IN_PROGRESS"
    RESOLUTION_REVIEW = "RESOLUTION_REVIEW"
    RESOLVED = "RESOLVED"
    CLOSED = "CLOSED"
    REJECTED = "REJECTED"
    REOPENED = "REOPENED"


class InvestigationPriority(str, Enum):
    CRITICAL = "CRITICAL"
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"


class TimelineEventType(str, Enum):
    INVESTIGATION_CREATED = "INVESTIGATION_CREATED"
    EVIDENCE_ATTACHED = "EVIDENCE_ATTACHED"
    AI_ANALYSIS_RECEIVED = "AI_ANALYSIS_RECEIVED"
    RISK_ASSESSMENT_UPDATED = "RISK_ASSESSMENT_UPDATED"
    STATUS_CHANGED = "STATUS_CHANGED"
    EVIDENCE_REQUESTED = "EVIDENCE_REQUESTED"
    EVIDENCE_SUBMITTED = "EVIDENCE_SUBMITTED"
    APPROVAL_REQUESTED = "APPROVAL_REQUESTED"
    APPROVAL_GRANTED = "APPROVAL_GRANTED"
    APPROVAL_DENIED = "APPROVAL_DENIED"
    ACTION_STARTED = "ACTION_STARTED"
    ACTION_COMPLETED = "ACTION_COMPLETED"
    RESOLUTION_VERIFIED = "RESOLUTION_VERIFIED"


class TimelineEventSchema(BaseModel):
    event_id: str
    investigation_id: str
    event_type: TimelineEventType
    actor_id: str
    timestamp_utc: int
    title: str
    description: Optional[str] = None


class InvestigationCreateSchema(BaseModel):
    id: Optional[str] = None
    title: str
    description: Optional[str] = None
    asset_id: str
    asset_code: str
    incident_id: Optional[str] = None
    category: str = "DRAINAGE_BLOCKAGE_INVESTIGATION"
    priority: InvestigationPriority = InvestigationPriority.HIGH
    assigned_investigator: Optional[str] = "usr_dev_auditor_001"


class InvestigationStatusUpdateSchema(BaseModel):
    target_status: InvestigationStatus
    comment: Optional[str] = None


class EvidenceRequestCreateSchema(BaseModel):
    investigation_id: str
    asset_id: str
    requested_evidence_type: str = "PHOTO_CLOSEUP_INVERT"
    instructions: str
    assigned_to: str = "usr_field_inspector"


class EvidenceRequestResponseSchema(BaseModel):
    request_id: str
    investigation_id: str
    asset_id: str
    requested_evidence_type: str
    instructions: str
    requested_by: str
    assigned_to: str
    status: str = "PENDING"  # PENDING, SUBMITTED, OVERDUE
    created_timestamp_utc: int


class InvestigationResponseSchema(BaseModel):
    id: str
    title: str
    description: Optional[str]
    category: str
    asset_id: str
    asset_code: str
    incident_id: Optional[str]
    status: InvestigationStatus
    priority: InvestigationPriority
    assigned_investigator: str
    created_timestamp_utc: int
    updated_timestamp_utc: int
    timeline_events: List[TimelineEventSchema] = Field(default_factory=list)
