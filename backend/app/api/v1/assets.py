"""NAGARLENS — Asset Registry & Living Map FastAPI Router."""

from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import AuthenticatedUser, get_current_reviewer
from app.infrastructure.db.session import get_async_session
from app.modules.assets.enums import AssetCategory, AssetVerificationStatus, VerificationDecision
from app.modules.assets.schemas import (
    AssetCreateSchema,
    AssetResponseSchema,
    AssetUpdateSchema,
    DuplicateCandidateSchema,
)
from app.modules.assets.services import AssetService

router = APIRouter(prefix="/assets", tags=["Infrastructure Asset Registry & Living Map"])


@router.post("", response_model=AssetResponseSchema, status_code=status.HTTP_201_CREATED, summary="Register a new infrastructure asset")
async def create_asset(
    data: AssetCreateSchema,
    session: AsyncSession = Depends(get_async_session),
):
    try:
        asset = await AssetService.create_asset(session, data)
        await session.commit()
        return asset
    except ValueError as exc:
        await session.rollback()
        if "Idempotency Conflict" in str(exc):
            raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail=str(exc))
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(exc))
    except Exception as exc:
        await session.rollback()
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Asset registration failed: {str(exc)}",
        )


@router.get("/{asset_id}", response_model=AssetResponseSchema, summary="Retrieve asset details by UUID")
async def get_asset(
    asset_id: str,
    session: AsyncSession = Depends(get_async_session),
):
    asset = await AssetService.get_asset_by_id(session, asset_id)
    if not asset:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Asset with ID '{asset_id}' not found.",
        )
    return asset


@router.get("", response_model=List[AssetResponseSchema], summary="Query assets within map viewport envelope")
async def query_assets_in_viewport(
    min_lat: float = Query(..., ge=-90.0, le=90.0),
    min_lon: float = Query(..., ge=-180.0, le=180.0),
    max_lat: float = Query(..., ge=-90.0, le=90.0),
    max_lon: float = Query(..., ge=-180.0, le=180.0),
    category: Optional[AssetCategory] = None,
    verification_status: Optional[AssetVerificationStatus] = None,
    session: AsyncSession = Depends(get_async_session),
):
    assets = await AssetService.get_assets_in_viewport(
        session=session,
        min_lat=min_lat,
        min_lon=min_lon,
        max_lat=max_lat,
        max_lon=max_lon,
        category=category,
        verification_status=verification_status,
    )
    return assets


@router.get("/nearby", response_model=List[AssetResponseSchema], summary="Query assets within geographic radius")
async def query_assets_nearby(
    lat: float = Query(..., ge=-90.0, le=90.0),
    lon: float = Query(..., ge=-180.0, le=180.0),
    radius_meters: float = Query(50.0, ge=1.0, le=5000.0),
    session: AsyncSession = Depends(get_async_session),
):
    assets = await AssetService.get_assets_nearby(
        session=session,
        lat=lat,
        lon=lon,
        radius_meters=radius_meters,
    )
    return assets


@router.patch("/{asset_id}", response_model=AssetResponseSchema, summary="Update asset metadata or coordinates")
async def update_asset(
    asset_id: str,
    data: AssetUpdateSchema,
    session: AsyncSession = Depends(get_async_session),
):
    try:
        asset = await AssetService.update_asset(session, asset_id, data)
        await session.commit()
        return asset
    except ValueError as exc:
        await session.rollback()
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(exc))
    except Exception as exc:
        await session.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Asset update failed: {str(exc)}",
        )


@router.post("/{asset_id}/verification", response_model=AssetResponseSchema, summary="Submit asset verification decision")
async def submit_verification(
    asset_id: str,
    decision: VerificationDecision,
    reason: Optional[str] = None,
    current_reviewer: AuthenticatedUser = Depends(get_current_reviewer),
    session: AsyncSession = Depends(get_async_session),
):
    try:
        asset = await AssetService.submit_verification(
            session=session,
            asset_id=asset_id,
            decision=decision,
            reviewer_id=current_reviewer.user_id,
            reason=reason,
        )
        await session.commit()
        return asset
    except ValueError as exc:
        await session.rollback()
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(exc))
    except Exception as exc:
        await session.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Verification submission failed: {str(exc)}",
        )


@router.get("/duplicate-candidates", response_model=List[DuplicateCandidateSchema], summary="Query duplicate asset candidates near coordinates")
async def query_duplicate_candidates(
    lat: float = Query(..., ge=-90.0, le=90.0),
    lon: float = Query(..., ge=-180.0, le=180.0),
    category: AssetCategory = Query(...),
    radius_meters: float = Query(15.0, ge=1.0, le=100.0),
    session: AsyncSession = Depends(get_async_session),
):
    candidates = await AssetService.find_duplicate_candidates(
        session=session,
        lat=lat,
        lon=lon,
        category=category,
        radius_meters=radius_meters,
    )
    return candidates
