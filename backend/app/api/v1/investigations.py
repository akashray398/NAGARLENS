"""NAGARLENS — Investigation Workflows & Evidence Collection Router."""

import time
import uuid
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status

from app.core.security import AuthenticatedUser, get_current_user
from app.schemas.investigation import (
    EvidenceRequestCreateSchema,
    EvidenceRequestResponseSchema,
    InvestigationCreateSchema,
    InvestigationPriority,
    InvestigationResponseSchema,
    InvestigationStatus,
    InvestigationStatusUpdateSchema,
    TimelineEventSchema,
    TimelineEventType,
)

router = APIRouter(prefix="/investigations", tags=["Investigation Workflows & Evidence Collection"])

# State Transition Rules Matrix
VALID_TRANSITIONS: dict[InvestigationStatus, list[InvestigationStatus]] = {
    InvestigationStatus.DRAFT: [InvestigationStatus.OPEN, InvestigationStatus.REJECTED],
    InvestigationStatus.OPEN: [InvestigationStatus.TRIAGED, InvestigationStatus.REJECTED],
    InvestigationStatus.TRIAGED: [InvestigationStatus.INVESTIGATING, InvestigationStatus.REJECTED],
    InvestigationStatus.INVESTIGATING: [InvestigationStatus.AWAITING_EVIDENCE, InvestigationStatus.AWAITING_REVIEW, InvestigationStatus.ACTION_PROPOSED],
    InvestigationStatus.AWAITING_EVIDENCE: [InvestigationStatus.INVESTIGATING, InvestigationStatus.AWAITING_REVIEW],
    InvestigationStatus.AWAITING_REVIEW: [InvestigationStatus.ACTION_PROPOSED, InvestigationStatus.INVESTIGATING],
    InvestigationStatus.ACTION_PROPOSED: [InvestigationStatus.AWAITING_APPROVAL, InvestigationStatus.REJECTED],
    InvestigationStatus.AWAITING_APPROVAL: [InvestigationStatus.ACTION_IN_PROGRESS, InvestigationStatus.REJECTED, InvestigationStatus.INVESTIGATING],
    InvestigationStatus.ACTION_IN_PROGRESS: [InvestigationStatus.RESOLUTION_REVIEW, InvestigationStatus.INVESTIGATING],
    InvestigationStatus.RESOLUTION_REVIEW: [InvestigationStatus.RESOLVED, InvestigationStatus.INVESTIGATING],
    InvestigationStatus.RESOLVED: [InvestigationStatus.CLOSED, InvestigationStatus.REOPENED],
    InvestigationStatus.CLOSED: [InvestigationStatus.REOPENED],
    InvestigationStatus.REJECTED: [InvestigationStatus.REOPENED],
    InvestigationStatus.REOPENED: [InvestigationStatus.OPEN, InvestigationStatus.INVESTIGATING],
}

_INVESTIGATION_DB: dict[str, dict] = {
    "INV-10001": {
        "id": "INV-10001",
        "title": "Investigation: Severe Silt Blockage at Ward 12 Drain",
        "description": "Evidence-driven root cause investigation into recurring silt accumulation.",
        "category": "DRAINAGE_BLOCKAGE_INVESTIGATION",
        "asset_id": "511a2c72-86f0-4c1a-9509-09c928410811",
        "asset_code": "NAG-CB-000001",
        "incident_id": "INC-10001",
        "status": InvestigationStatus.INVESTIGATING,
        "priority": InvestigationPriority.HIGH,
        "assigned_investigator": "usr_dev_auditor_001",
        "created_timestamp_utc": int(time.time() * 1000) - 7200000,
        "updated_timestamp_utc": int(time.time() * 1000) - 3600000,
        "timeline_events": [
            {
                "event_id": "EVT-001",
                "investigation_id": "INV-10001",
                "event_type": TimelineEventType.INVESTIGATION_CREATED,
                "actor_id": "usr_dev_auditor_001",
                "timestamp_utc": int(time.time() * 1000) - 7200000,
                "title": "Investigation Case Created",
                "description": "Investigation created from Incident INC-10001."
            }
        ]
    }
}

_EVIDENCE_REQUEST_DB: dict[str, dict] = {}


@router.post("", response_model=InvestigationResponseSchema, status_code=status.HTTP_201_CREATED, summary="Create a new infrastructure investigation case")
async def create_investigation(
    data: InvestigationCreateSchema,
    current_user: AuthenticatedUser = Depends(get_current_user)
):
    inv_id = data.id or f"INV-{(10000 + len(_INVESTIGATION_DB) + 1)}"
    now = int(time.time() * 1000)

    creation_event = {
        "event_id": f"EVT-{now % 100000}",
        "investigation_id": inv_id,
        "event_type": TimelineEventType.INVESTIGATION_CREATED,
        "actor_id": current_user.user_id,
        "timestamp_utc": now,
        "title": "Investigation Case Created",
        "description": f"Created investigation '{data.title}' for asset {data.asset_code}."
    }

    record = {
        "id": inv_id,
        "title": data.title,
        "description": data.description,
        "category": data.category,
        "asset_id": data.asset_id,
        "asset_code": data.asset_code,
        "incident_id": data.incident_id,
        "status": InvestigationStatus.OPEN,
        "priority": data.priority,
        "assigned_investigator": data.assigned_investigator or current_user.user_id,
        "created_timestamp_utc": now,
        "updated_timestamp_utc": now,
        "timeline_events": [creation_event]
    }
    _INVESTIGATION_DB[inv_id] = record
    return record


@router.get("/{investigation_id}", response_model=InvestigationResponseSchema, summary="Retrieve investigation case details and timeline history")
async def get_investigation(investigation_id: str):
    record = _INVESTIGATION_DB.get(investigation_id)
    if not record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Investigation with ID '{investigation_id}' not found."
        )
    return record


@router.get("", response_model=List[InvestigationResponseSchema], summary="List all investigation cases with optional status filter")
async def list_investigations(status_filter: Optional[InvestigationStatus] = Query(None, alias="status")):
    investigations = list(_INVESTIGATION_DB.values())
    if status_filter:
        investigations = [i for i in investigations if i["status"] == status_filter]
    return investigations


@router.patch("/{investigation_id}/status", response_model=InvestigationResponseSchema, summary="Advance investigation lifecycle status state machine")
async def update_investigation_status(
    investigation_id: str,
    data: InvestigationStatusUpdateSchema,
    current_user: AuthenticatedUser = Depends(get_current_user)
):
    record = _INVESTIGATION_DB.get(investigation_id)
    if not record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Investigation with ID '{investigation_id}' not found."
        )

    current_status = record["status"]
    allowed = VALID_TRANSITIONS.get(current_status, [])

    if data.target_status not in allowed:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=f"Invalid state transition: Cannot transition investigation from '{current_status.value}' to '{data.target_status.value}'. Allowed target states: {[s.value for s in allowed]}."
        )

    now = int(time.time() * 1000)
    record["status"] = data.target_status
    record["updated_timestamp_utc"] = now

    event = {
        "event_id": f"EVT-{now % 100000}",
        "investigation_id": investigation_id,
        "event_type": TimelineEventType.STATUS_CHANGED,
        "actor_id": current_user.user_id,
        "timestamp_utc": now,
        "title": f"Status Advanced to {data.target_status.value}",
        "description": data.comment or f"Transitioned from {current_status.value} to {data.target_status.value}."
    }
    record["timeline_events"].append(event)
    return record


@router.post("/{investigation_id}/evidence-requests", response_model=EvidenceRequestResponseSchema, status_code=status.HTTP_201_CREATED, summary="Issue a field evidence collection request for an investigation")
async def create_evidence_request(
    investigation_id: str,
    data: EvidenceRequestCreateSchema,
    current_user: AuthenticatedUser = Depends(get_current_user)
):
    inv = _INVESTIGATION_DB.get(investigation_id)
    if not inv:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Investigation '{investigation_id}' not found.")

    req_id = f"REQ-{(1000 + len(_EVIDENCE_REQUEST_DB) + 1)}"
    now = int(time.time() * 1000)

    req_record = {
        "request_id": req_id,
        "investigation_id": investigation_id,
        "asset_id": data.asset_id,
        "requested_evidence_type": data.requested_evidence_type,
        "instructions": data.instructions,
        "requested_by": current_user.user_id,
        "assigned_to": data.assigned_to,
        "status": "PENDING",
        "created_timestamp_utc": now
    }
    _EVIDENCE_REQUEST_DB[req_id] = req_record

    # Append timeline event
    evt = {
        "event_id": f"EVT-{now % 100000}",
        "investigation_id": investigation_id,
        "event_type": TimelineEventType.EVIDENCE_REQUESTED,
        "actor_id": current_user.user_id,
        "timestamp_utc": now,
        "title": f"Evidence Requested: {data.requested_evidence_type}",
        "description": f"Instructions: {data.instructions}"
    }
    inv["timeline_events"].append(evt)

    # Automatically move status to AWAITING_EVIDENCE if currently INVESTIGATING
    if inv["status"] == InvestigationStatus.INVESTIGATING:
        inv["status"] = InvestigationStatus.AWAITING_EVIDENCE

    return req_record
