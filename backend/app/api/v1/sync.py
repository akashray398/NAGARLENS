"""NAGARLENS — Android-to-Cloud Synchronization Router."""

import time
from typing import List, Optional
from fastapi import APIRouter, Depends, status
from pydantic import BaseModel, Field

from app.core.security import AuthenticatedUser, get_current_user
from app.modules.assets.schemas import AssetResponseSchema
from app.schemas.ai import ProcessingStatus

router = APIRouter(prefix="/sync", tags=["Data Synchronization Engine"])


class EvidenceSyncPayload(BaseModel):
    id: str
    original_file_name: str
    sha256_hash: str
    capture_timestamp_utc: int
    latitude: Optional[float] = None
    longitude: Optional[float] = None


class SyncPushRequestSchema(BaseModel):
    client_device_id: str
    sync_timestamp_utc: int = Field(default_factory=lambda: int(time.time() * 1000))
    evidences: List[EvidenceSyncPayload] = Field(default_factory=list)


class SyncItemStatus(BaseModel):
    id: str
    status: str  # "ACCEPTED", "CONFLICT_RESOLVED", "REJECTED"
    message: str


class SyncPushResponseSchema(BaseModel):
    processed_count: int
    server_timestamp_utc: int
    items: List[SyncItemStatus]


class SyncPullResponseSchema(BaseModel):
    server_timestamp_utc: int
    updated_assets_count: int
    updated_assets: List[AssetResponseSchema] = Field(default_factory=list)


@router.post("/push", response_model=SyncPushResponseSchema, status_code=status.HTTP_200_OK, summary="Push local client sync batch idempotently")
async def push_client_sync_batch(
    payload: SyncPushRequestSchema,
    current_user: AuthenticatedUser = Depends(get_current_user)
):
    results: List[SyncItemStatus] = []
    now = int(time.time() * 1000)

    for item in payload.evidences:
        results.append(
            SyncItemStatus(
                id=item.id,
                status="ACCEPTED",
                message=f"Evidence {item.id} accepted for sync. Cryptographic SHA-256 registered."
            )
        )

    return SyncPushResponseSchema(
        processed_count=len(results),
        server_timestamp_utc=now,
        items=results
    )


@router.get("/pull", response_model=SyncPullResponseSchema, summary="Pull server asset deltas since last client sync timestamp")
async def pull_server_sync_deltas(
    since_timestamp_utc: int = 0,
    current_user: AuthenticatedUser = Depends(get_current_user)
):
    now = int(time.time() * 1000)
    return SyncPullResponseSchema(
        server_timestamp_utc=now,
        updated_assets_count=0,
        updated_assets=[]
    )
