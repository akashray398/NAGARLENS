"""NAGARLENS — Municipal Incident & Failure Work Orders Router."""

import time
import uuid
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from pydantic import BaseModel, Field

from app.core.security import AuthenticatedUser, get_current_user
from app.ml.taxonomy import DefectCategory

router = APIRouter(prefix="/incidents", tags=["Municipal Incidents & Work Orders"])


class IncidentCreateSchema(BaseModel):
    id: Optional[str] = Field(default=None, description="Client-generated UUIDv4")
    asset_id: str
    asset_code: str
    title: str
    description: Optional[str] = None
    priority: str = "HIGH"
    defect_category: DefectCategory = DefectCategory.DRAIN_BLOCKAGE


class IncidentStatusUpdateSchema(BaseModel):
    status: str = Field(..., description="OPEN, IN_PROGRESS, RESOLVED, CLOSED")
    comment: Optional[str] = None


class IncidentResponseSchema(BaseModel):
    id: str
    asset_id: str
    asset_code: str
    title: str
    description: Optional[str]
    status: str
    priority: str
    defect_category: DefectCategory
    reported_by: str
    created_timestamp_utc: int
    updated_timestamp_utc: int


_INCIDENT_DB: dict[str, dict] = {
    "INC-10001": {
        "id": "INC-10001",
        "asset_id": "511a2c72-86f0-4c1a-9509-09c928410811",
        "asset_code": "NAG-CB-000001",
        "title": "Critical Silt Blockage at Catch Basin Invert",
        "description": "Severe accumulation of silt and plastic trash blocking 90% of invert capacity.",
        "status": "OPEN",
        "priority": "CRITICAL",
        "defect_category": DefectCategory.DRAIN_BLOCKAGE,
        "reported_by": "usr_dev_auditor_001",
        "created_timestamp_utc": int(time.time() * 1000) - 3600000,
        "updated_timestamp_utc": int(time.time() * 1000) - 3600000
    }
}


@router.post("", response_model=IncidentResponseSchema, status_code=status.HTTP_201_CREATED, summary="Create a new municipal incident work order")
async def create_incident(
    data: IncidentCreateSchema,
    current_user: AuthenticatedUser = Depends(get_current_user)
):
    inc_id = data.id or f"INC-{(10000 + len(_INCIDENT_DB) + 1)}"
    now = int(time.time() * 1000)

    record = {
        "id": inc_id,
        "asset_id": data.asset_id,
        "asset_code": data.asset_code,
        "title": data.title,
        "description": data.description,
        "status": "OPEN",
        "priority": data.priority,
        "defect_category": data.defect_category,
        "reported_by": current_user.user_id,
        "created_timestamp_utc": now,
        "updated_timestamp_utc": now
    }
    _INCIDENT_DB[inc_id] = record
    return record


@router.get("/{incident_id}", response_model=IncidentResponseSchema, summary="Retrieve incident details by ID")
async def get_incident(incident_id: str):
    record = _INCIDENT_DB.get(incident_id)
    if not record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Incident with ID '{incident_id}' not found."
        )
    return record


@router.get("", response_model=List[IncidentResponseSchema], summary="List all municipal incidents with optional status filter")
async def list_incidents(status_filter: Optional[str] = Query(None, alias="status")):
    incidents = list(_INCIDENT_DB.values())
    if status_filter:
        incidents = [i for i in incidents if i["status"].upper() == status_filter.upper()]
    return incidents


@router.patch("/{incident_id}/status", response_model=IncidentResponseSchema, summary="Update incident work order status")
async def update_incident_status(
    incident_id: str,
    data: IncidentStatusUpdateSchema,
    current_user: AuthenticatedUser = Depends(get_current_user)
):
    record = _INCIDENT_DB.get(incident_id)
    if not record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Incident with ID '{incident_id}' not found."
        )

    record["status"] = data.status.upper()
    record["updated_timestamp_utc"] = int(time.time() * 1000)
    return record
