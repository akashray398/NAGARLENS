"""NAGARLENS — Controlled Automation Rules & Human Approval Schemas."""

from enum import Enum
from typing import List, Optional
from pydantic import BaseModel, Field


class TriggerEvent(str, Enum):
    HIGH_RISK_THRESHOLD_EXCEEDED = "HIGH_RISK_THRESHOLD_EXCEEDED"
    NEW_CRITICAL_INCIDENT = "NEW_CRITICAL_INCIDENT"
    EVIDENCE_REQUEST_SUBMITTED = "EVIDENCE_REQUEST_SUBMITTED"
    APPROVAL_GRANTED = "APPROVAL_GRANTED"


class ApprovalStatus(str, Enum):
    PENDING = "PENDING"
    APPROVED = "APPROVED"
    REJECTED = "REJECTED"


class ActionExecutionStatus(str, Enum):
    PENDING = "PENDING"
    AWAITING_APPROVAL = "AWAITING_APPROVAL"
    RUNNING = "RUNNING"
    SUCCEEDED = "SUCCEEDED"
    FAILED = "FAILED"


class AutomationRuleSchema(BaseModel):
    rule_id: str
    name: str
    description: str
    trigger_event: TriggerEvent
    is_enabled: bool = True
    requires_approval: bool = True
    version: str = "v1.0.0"


class ApprovalRequestSchema(BaseModel):
    approval_id: str
    investigation_id: str
    proposed_action: str
    reason: str
    requested_by: str
    status: ApprovalStatus = ApprovalStatus.PENDING
    reviewer_id: Optional[str] = None
    reviewer_notes: Optional[str] = None
    created_timestamp_utc: int


class ApprovalSubmissionSchema(BaseModel):
    status: ApprovalStatus  # APPROVED or REJECTED
    reviewer_notes: Optional[str] = None


class ActionExecutionResponseSchema(BaseModel):
    execution_id: str
    rule_id: str
    investigation_id: str
    action_type: str
    status: ActionExecutionStatus
    attempts: int = 1
    result_summary: str
    executed_timestamp_utc: int
