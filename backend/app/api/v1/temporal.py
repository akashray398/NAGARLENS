"""NAGARLENS — Temporal Reality Graph FastAPI Router."""

from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import AuthenticatedUser, get_current_reviewer, get_current_user
from app.infrastructure.db.session import get_async_session
from app.modules.temporal.schemas import (
    AssetTimelineResponseSchema,
    InterventionCreateSchema,
    InterventionResponseSchema,
    InterventionStatusUpdateSchema,
    ObservationCreateSchema,
    ObservationResponseSchema,
    OutcomeVerificationCreateSchema,
    OutcomeVerificationResponseSchema,
)
from app.modules.temporal.services import TemporalService

router = APIRouter(prefix="", tags=["Temporal Reality Graph & Asset Memory Timeline"])


@router.post("/assets/{asset_id}/observations", response_model=ObservationResponseSchema, status_code=status.HTTP_201_CREATED, summary="Record a new asset observation")
async def create_observation(
    asset_id: str,
    data: ObservationCreateSchema,
    current_user: AuthenticatedUser = Depends(get_current_user),
    session: AsyncSession = Depends(get_async_session),
):
    try:
        obs = await TemporalService.create_observation(
            session=session,
            asset_id=asset_id,
            data=data,
            actor_id=current_user.user_id,
        )
        await session.commit()
        return obs
    except ValueError as exc:
        await session.rollback()
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(exc))
    except Exception as exc:
        await session.rollback()
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=f"Observation creation failed: {str(exc)}")


@router.get("/assets/{asset_id}/timeline", response_model=AssetTimelineResponseSchema, summary="Retrieve full chronological asset memory timeline")
async def get_asset_timeline(
    asset_id: str,
    session: AsyncSession = Depends(get_async_session),
):
    try:
        timeline = await TemporalService.get_asset_timeline(session, asset_id)
        return timeline
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(exc))


@router.get("/observations/{observation_id}", response_model=ObservationResponseSchema, summary="Retrieve observation details by ID")
async def get_observation(
    observation_id: str,
    session: AsyncSession = Depends(get_async_session),
):
    obs = await TemporalService.get_observation_by_id(session, observation_id)
    if not obs:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Observation with ID '{observation_id}' not found.")
    return obs


@router.post("/assets/{asset_id}/interventions", response_model=InterventionResponseSchema, status_code=status.HTTP_201_CREATED, summary="Record a new physical intervention")
async def create_intervention(
    asset_id: str,
    data: InterventionCreateSchema,
    current_user: AuthenticatedUser = Depends(get_current_user),
    session: AsyncSession = Depends(get_async_session),
):
    try:
        interv = await TemporalService.create_intervention(
            session=session,
            asset_id=asset_id,
            data=data,
            actor_id=current_user.user_id,
        )
        await session.commit()
        return interv
    except ValueError as exc:
        await session.rollback()
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(exc))
    except Exception as exc:
        await session.rollback()
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=f"Intervention creation failed: {str(exc)}")


@router.patch("/interventions/{intervention_id}/status", response_model=InterventionResponseSchema, summary="Update intervention status and record status transition event")
async def update_intervention_status(
    intervention_id: str,
    data: InterventionStatusUpdateSchema,
    current_user: AuthenticatedUser = Depends(get_current_user),
    session: AsyncSession = Depends(get_async_session),
):
    try:
        interv = await TemporalService.update_intervention_status(
            session=session,
            intervention_id=intervention_id,
            data=data,
            actor_id=current_user.user_id,
        )
        await session.commit()
        return interv
    except ValueError as exc:
        await session.rollback()
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(exc))
    except Exception as exc:
        await session.rollback()
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=f"Status update failed: {str(exc)}")


@router.post("/interventions/{intervention_id}/outcome-verifications", response_model=OutcomeVerificationResponseSchema, status_code=status.HTTP_201_CREATED, summary="Submit intervention outcome verification")
async def submit_outcome_verification(
    intervention_id: str,
    data: OutcomeVerificationCreateSchema,
    current_reviewer: AuthenticatedUser = Depends(get_current_reviewer),
    session: AsyncSession = Depends(get_async_session),
):
    try:
        ver = await TemporalService.submit_outcome_verification(
            session=session,
            intervention_id=intervention_id,
            data=data,
            reviewer_id=current_reviewer.user_id,
        )
        await session.commit()
        return ver
    except ValueError as exc:
        await session.rollback()
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(exc))
    except Exception as exc:
        await session.rollback()
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=f"Outcome verification failed: {str(exc)}")
