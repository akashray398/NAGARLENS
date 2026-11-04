"""NAGARLENS — Temporal Service Engine."""

import time
import uuid
from typing import List, Optional
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.assets.models import AssetModel
from app.modules.temporal.enums import InterventionEvidenceType, InterventionStatus
from app.modules.temporal.models import (
    InterventionEvidenceLinkModel,
    InterventionModel,
    InterventionStatusHistoryModel,
    ObservationEvidenceLinkModel,
    ObservationModel,
    OutcomeVerificationModel,
)
from app.modules.temporal.schemas import (
    AssetTimelineResponseSchema,
    InterventionCreateSchema,
    InterventionStatusUpdateSchema,
    ObservationCreateSchema,
    OutcomeVerificationCreateSchema,
    TimelineEventSchema,
)


class TemporalService:

    @staticmethod
    async def create_observation(
        session: AsyncSession,
        asset_id: str,
        data: ObservationCreateSchema,
        actor_id: str = "field_worker",
    ) -> ObservationModel:
        asset_query = select(AssetModel).where(AssetModel.id == asset_id)
        asset_res = await session.execute(asset_query)
        if not asset_res.scalar_one_or_none():
            raise ValueError(f"Asset with ID '{asset_id}' not found.")

        obs_id = data.id or str(uuid.uuid4())

        existing_query = select(ObservationModel).where(ObservationModel.id == obs_id)
        existing_res = await session.execute(existing_query)
        existing = existing_res.scalar_one_or_none()
        if existing:
            return existing

        now = int(time.time() * 1000)

        observation = ObservationModel(
            id=obs_id,
            asset_id=asset_id,
            observation_type=data.observation_type.value,
            condition_category=data.condition_category.value,
            condition_severity=data.condition_severity.value,
            description=data.description,
            observed_at_utc=data.observed_at_utc,
            recorded_at_utc=now,
            observed_by=actor_id,
            latitude=data.latitude,
            longitude=data.longitude,
            location_accuracy_meters=data.location_accuracy_meters,
            source="FIELD_INSPECTION",
        )
        session.add(observation)

        if data.evidence_id:
            link = ObservationEvidenceLinkModel(
                id=str(uuid.uuid4()),
                observation_id=obs_id,
                evidence_id=data.evidence_id,
                relationship_type="CONDITION_PROOF",
                linked_at_utc=now,
            )
            session.add(link)

        await session.flush()
        return observation

    @staticmethod
    async def get_observation_by_id(session: AsyncSession, observation_id: str) -> Optional[ObservationModel]:
        query = select(ObservationModel).where(ObservationModel.id == observation_id)
        res = await session.execute(query)
        return res.scalar_one_or_none()

    @staticmethod
    async def create_intervention(
        session: AsyncSession,
        asset_id: str,
        data: InterventionCreateSchema,
        actor_id: str = "field_worker",
    ) -> InterventionModel:
        asset_query = select(AssetModel).where(AssetModel.id == asset_id)
        asset_res = await session.execute(asset_query)
        if not asset_res.scalar_one_or_none():
            raise ValueError(f"Asset with ID '{asset_id}' not found.")

        intervention_id = data.id or str(uuid.uuid4())

        existing_query = select(InterventionModel).where(InterventionModel.id == intervention_id)
        existing_res = await session.execute(existing_query)
        existing = existing_res.scalar_one_or_none()
        if existing:
            return existing

        now = int(time.time() * 1000)

        intervention = InterventionModel(
            id=intervention_id,
            asset_id=asset_id,
            intervention_type=data.intervention_type.value,
            title=data.title,
            description=data.description,
            status=InterventionStatus.PROPOSED.value,
            planned_at_utc=data.planned_at_utc,
            recorded_by=actor_id,
            created_at_utc=now,
            updated_at_utc=now,
        )
        session.add(intervention)

        # Initial status history record
        status_hist = InterventionStatusHistoryModel(
            id=str(uuid.uuid4()),
            intervention_id=intervention_id,
            previous_status="NONE",
            new_status=InterventionStatus.PROPOSED.value,
            reason="Initial intervention creation",
            actor_id=actor_id,
            recorded_at_utc=now,
        )
        session.add(status_hist)

        if data.before_evidence_id:
            link = InterventionEvidenceLinkModel(
                id=str(uuid.uuid4()),
                intervention_id=intervention_id,
                evidence_id=data.before_evidence_id,
                relationship_type=InterventionEvidenceType.BEFORE_ACTION.value,
                linked_at_utc=now,
            )
            session.add(link)

        await session.flush()
        return intervention

    @staticmethod
    async def update_intervention_status(
        session: AsyncSession,
        intervention_id: str,
        data: InterventionStatusUpdateSchema,
        actor_id: str,
    ) -> InterventionModel:
        int_query = select(InterventionModel).where(InterventionModel.id == intervention_id)
        int_res = await session.execute(int_query)
        intervention = int_res.scalar_one_or_none()
        if not intervention:
            raise ValueError(f"Intervention with ID '{intervention_id}' not found.")

        old_status = intervention.status
        now = int(time.time() * 1000)

        # Immutable status history transition event record
        status_hist = InterventionStatusHistoryModel(
            id=str(uuid.uuid4()),
            intervention_id=intervention_id,
            previous_status=old_status,
            new_status=data.new_status.value,
            reason=data.reason or f"Status changed from {old_status} to {data.new_status.value}",
            actor_id=actor_id,
            recorded_at_utc=now,
        )
        session.add(status_hist)

        intervention.status = data.new_status.value
        if data.new_status == InterventionStatus.IN_PROGRESS and not intervention.started_at_utc:
            intervention.started_at_utc = now
        elif data.new_status in (InterventionStatus.COMPLETED, InterventionStatus.FAILED) and not intervention.completed_at_utc:
            intervention.completed_at_utc = now

        intervention.updated_at_utc = now
        await session.flush()
        return intervention

    @staticmethod
    async def submit_outcome_verification(
        session: AsyncSession,
        intervention_id: str,
        data: OutcomeVerificationCreateSchema,
        reviewer_id: str,
    ) -> OutcomeVerificationModel:
        int_query = select(InterventionModel).where(InterventionModel.id == intervention_id)
        int_res = await session.execute(int_query)
        intervention = int_res.scalar_one_or_none()
        if not intervention:
            raise ValueError(f"Intervention with ID '{intervention_id}' not found.")

        obs_query = select(ObservationModel).where(ObservationModel.id == data.verification_observation_id)
        obs_res = await session.execute(obs_query)
        if not obs_res.scalar_one_or_none():
            raise ValueError(f"Verification Observation with ID '{data.verification_observation_id}' not found.")

        ver_id = data.id or str(uuid.uuid4())
        now = int(time.time() * 1000)

        verification = OutcomeVerificationModel(
            id=ver_id,
            intervention_id=intervention_id,
            asset_id=intervention.asset_id,
            verification_observation_id=data.verification_observation_id,
            outcome=data.outcome.value,
            description=data.description,
            verified_at_utc=now,
            verified_by=reviewer_id,
            created_at_utc=now,
        )
        session.add(verification)

        # Update intervention status to COMPLETED if outcome achieved and record status transition event
        if intervention.status != InterventionStatus.COMPLETED.value:
            status_hist = InterventionStatusHistoryModel(
                id=str(uuid.uuid4()),
                intervention_id=intervention_id,
                previous_status=intervention.status,
                new_status=InterventionStatus.COMPLETED.value,
                reason=f"Status set to COMPLETED upon outcome verification ({data.outcome.value})",
                actor_id=reviewer_id,
                recorded_at_utc=now,
            )
            session.add(status_hist)
            intervention.status = InterventionStatus.COMPLETED.value
            intervention.completed_at_utc = now
            intervention.updated_at_utc = now

        await session.flush()
        return verification

    @staticmethod
    async def get_asset_timeline(session: AsyncSession, asset_id: str) -> AssetTimelineResponseSchema:
        asset_query = select(AssetModel).where(AssetModel.id == asset_id)
        asset_res = await session.execute(asset_query)
        asset = asset_res.scalar_one_or_none()
        if not asset:
            raise ValueError(f"Asset with ID '{asset_id}' not found.")

        events: List[TimelineEventSchema] = []

        # 1. Registration Event
        events.append(
            TimelineEventSchema(
                event_id=f"REG_{asset.id}",
                event_type="REGISTRATION",
                timestamp_utc=asset.created_timestamp_utc,
                title=f"Asset Registration ({asset.asset_code})",
                description=asset.description or f"Initial registration of {asset.asset_category}",
                actor_id=asset.created_by,
            )
        )

        # 2. Observations
        obs_query = select(ObservationModel).where(ObservationModel.asset_id == asset_id)
        obs_res = await session.execute(obs_query)
        for obs in obs_res.scalars().all():
            events.append(
                TimelineEventSchema(
                    event_id=obs.id,
                    event_type="OBSERVATION",
                    timestamp_utc=obs.observed_at_utc,
                    title=f"Observation: {obs.observation_type}",
                    description=obs.description or f"Category: {obs.condition_category}",
                    actor_id=obs.observed_by,
                    severity=obs.condition_severity,
                )
            )

        # 3. Interventions
        int_query = select(InterventionModel).where(InterventionModel.asset_id == asset_id)
        int_res = await session.execute(int_query)
        for interv in int_res.scalars().all():
            events.append(
                TimelineEventSchema(
                    event_id=interv.id,
                    event_type="INTERVENTION",
                    timestamp_utc=interv.created_at_utc,
                    title=f"Intervention: {interv.title}",
                    description=interv.description or f"Type: {interv.intervention_type} • Status: {interv.status}",
                    actor_id=interv.recorded_by,
                )
            )

        # 4. Outcome Verifications
        ver_query = select(OutcomeVerificationModel).where(OutcomeVerificationModel.asset_id == asset_id)
        ver_res = await session.execute(ver_query)
        for ver in ver_res.scalars().all():
            events.append(
                TimelineEventSchema(
                    event_id=ver.id,
                    event_type="VERIFICATION",
                    timestamp_utc=ver.verified_at_utc,
                    title=f"Outcome Verification: {ver.outcome}",
                    description=ver.description or f"Verified outcome for intervention {ver.intervention_id}",
                    actor_id=ver.verified_by,
                    outcome=ver.outcome,
                )
            )

        # Deterministic sorting tie-breaker: (timestamp_utc, event_type_priority, event_id)
        event_priority = {"REGISTRATION": 0, "OBSERVATION": 1, "INTERVENTION": 2, "VERIFICATION": 3}
        events.sort(key=lambda x: (x.timestamp_utc, event_priority.get(x.event_type, 9), x.event_id))

        return AssetTimelineResponseSchema(
            asset_id=asset.id,
            asset_code=asset.asset_code,
            total_events=len(events),
            events=events,
        )
