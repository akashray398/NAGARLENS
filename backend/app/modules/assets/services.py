"""NAGARLENS — Asset Domain Services & PostGIS Integration Engine."""

import time
import uuid
from typing import List, Optional, Tuple
from geoalchemy2 import Geography
from geoalchemy2.functions import ST_DWithin, ST_MakeEnvelope, ST_MakePoint, ST_SetSRID
from sqlalchemy import cast, func, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.assets.enums import (
    AssetCategory,
    AssetLifecycleStatus,
    AssetRelationshipType,
    AssetVerificationStatus,
    IdentifierType,
    VerificationDecision,
)
from app.modules.assets.models import (
    AssetEvidenceLinkModel,
    AssetIdentifierRegistryModel,
    AssetLocationHistoryModel,
    AssetModel,
    AssetVerificationRecordModel,
)
from app.modules.assets.schemas import (
    AssetCreateSchema,
    AssetUpdateSchema,
    DuplicateCandidateSchema,
)


class AssetService:

    @staticmethod
    async def generate_asset_code(session: AsyncSession, category: AssetCategory) -> str:
        prefix = AssetCategory.get_prefix(category)
        query = select(func.count(AssetModel.id)).where(AssetModel.asset_category == category.value)
        result = await session.execute(query)
        count = result.scalar() or 0
        seq_num = count + 1
        entropy = uuid.uuid4().hex[:4].upper()
        return f"NAG-{prefix}-{seq_num:06d}-{entropy}"

    @staticmethod
    async def create_asset(session: AsyncSession, data: AssetCreateSchema, max_retries: int = 5) -> AssetModel:
        asset_id = data.id or str(uuid.uuid4())

        # 1. Idempotency Check: Validate existing asset with same UUID
        existing = await AssetService.get_asset_by_id(session, asset_id)
        if existing:
            is_cat_match = existing.asset_category == data.asset_category.value
            is_lat_match = abs(existing.latitude - data.latitude) < 1e-4
            is_lon_match = abs(existing.longitude - data.longitude) < 1e-4

            if is_cat_match and is_lat_match and is_lon_match:
                # Identical retry: Return existing asset idempotently
                return existing
            else:
                # Material Conflict: Same UUID reuse with conflicting immutable attributes
                raise ValueError(
                    f"Idempotency Conflict: Asset with UUID {asset_id} already exists with different attributes "
                    f"(existing category={existing.asset_category}, lat={existing.latitude}, lon={existing.longitude})."
                )

        now = int(time.time() * 1000)
        point_geom = ST_SetSRID(ST_MakePoint(data.longitude, data.latitude), 4326)

        # 2. Concurrency-Safe Bounded Retry Loop for Unique Constraint on asset_code
        for attempt in range(max_retries):
            code_candidate = (
                data.asset_code
                if (attempt == 0 and data.asset_code)
                else await AssetService.generate_asset_code(session, data.asset_category)
            )

            try:
                async with session.begin_nested():  # DB savepoint
                    asset = AssetModel(
                        id=asset_id,
                        asset_code=code_candidate,
                        asset_category=data.asset_category.value,
                        lifecycle_status=AssetLifecycleStatus.ACTIVE.value,
                        verification_status=AssetVerificationStatus.UNVERIFIED.value,
                        location=point_geom,
                        latitude=data.latitude,
                        longitude=data.longitude,
                        altitude_meters=data.altitude_meters,
                        description=data.description,
                        version=1,
                        created_by=data.created_by,
                        created_timestamp_utc=now,
                        updated_timestamp_utc=now,
                    )
                    session.add(asset)

                    if data.evidence_id:
                        link = AssetEvidenceLinkModel(
                            id=str(uuid.uuid4()),
                            asset_id=asset_id,
                            evidence_id=data.evidence_id,
                            relationship_type=AssetRelationshipType.INITIAL_REGISTRATION.value,
                            created_by=data.created_by,
                            created_timestamp_utc=now,
                        )
                        session.add(link)

                    await session.flush()
                    return asset
            except IntegrityError:
                if attempt == max_retries - 1:
                    raise

        raise RuntimeError("Failed to generate unique asset code after maximum retries.")

    @staticmethod
    async def get_asset_by_id(session: AsyncSession, asset_id: str) -> Optional[AssetModel]:
        query = select(AssetModel).where(AssetModel.id == asset_id)
        result = await session.execute(query)
        return result.scalar_one_or_none()

    @staticmethod
    async def get_assets_in_viewport(
        session: AsyncSession,
        min_lat: float,
        min_lon: float,
        max_lat: float,
        max_lon: float,
        category: Optional[AssetCategory] = None,
        verification_status: Optional[AssetVerificationStatus] = None,
        limit: int = 100,
    ) -> List[AssetModel]:
        envelope = ST_MakeEnvelope(min_lon, min_lat, max_lon, max_lat, 4326)
        query = select(AssetModel).where(AssetModel.location.intersects(envelope)).limit(limit)

        if category:
            query = query.where(AssetModel.asset_category == category.value)
        if verification_status:
            query = query.where(AssetModel.verification_status == verification_status.value)

        result = await session.execute(query)
        return list(result.scalars().all())

    @staticmethod
    async def get_assets_nearby(
        session: AsyncSession,
        lat: float,
        lon: float,
        radius_meters: float = 50.0,
        limit: int = 50,
    ) -> List[AssetModel]:
        target_point = ST_SetSRID(ST_MakePoint(lon, lat), 4326)
        query = (
            select(AssetModel)
            .where(ST_DWithin(cast(AssetModel.location, Geography), cast(target_point, Geography), radius_meters))
            .limit(limit)
        )

        result = await session.execute(query)
        return list(result.scalars().all())

    @staticmethod
    async def update_asset(session: AsyncSession, asset_id: str, data: AssetUpdateSchema) -> AssetModel:
        asset = await AssetService.get_asset_by_id(session, asset_id)
        if not asset:
            raise ValueError(f"Asset with ID {asset_id} not found.")

        if asset.version != data.version:
            raise ValueError(f"Concurrency Conflict: Asset version mismatch (expected {asset.version}, got {data.version}).")

        now = int(time.time() * 1000)

        if data.latitude is not None and data.longitude is not None:
            if abs(data.latitude - asset.latitude) > 1e-6 or abs(data.longitude - asset.longitude) > 1e-6:
                loc_history = AssetLocationHistoryModel(
                    id=str(uuid.uuid4()),
                    asset_id=asset_id,
                    previous_latitude=asset.latitude,
                    previous_longitude=asset.longitude,
                    new_latitude=data.latitude,
                    new_longitude=data.longitude,
                    reason=data.correction_reason or "Location coordinate update",
                    actor_id=data.actor_id,
                    recorded_timestamp_utc=now,
                )
                session.add(loc_history)

                asset.latitude = data.latitude
                asset.longitude = data.longitude
                asset.location = ST_SetSRID(ST_MakePoint(data.longitude, data.latitude), 4326)

        if data.asset_category:
            asset.asset_category = data.asset_category.value
        if data.lifecycle_status:
            asset.lifecycle_status = data.lifecycle_status.value
        if data.description is not None:
            asset.description = data.description

        asset.version += 1
        asset.updated_timestamp_utc = now

        await session.flush()
        return asset

    @staticmethod
    async def submit_verification(
        session: AsyncSession,
        asset_id: str,
        decision: VerificationDecision,
        reviewer_id: str,
        reason: Optional[str] = None,
    ) -> AssetModel:
        asset = await AssetService.get_asset_by_id(session, asset_id)
        if not asset:
            raise ValueError(f"Asset with ID {asset_id} not found.")

        now = int(time.time() * 1000)

        record = AssetVerificationRecordModel(
            id=str(uuid.uuid4()),
            asset_id=asset_id,
            decision=decision.value,
            reason=reason,
            reviewer_id=reviewer_id,
            decision_timestamp_utc=now,
        )
        session.add(record)

        if decision == VerificationDecision.APPROVED:
            asset.verification_status = AssetVerificationStatus.VERIFIED.value
        elif decision == VerificationDecision.REJECTED:
            asset.verification_status = AssetVerificationStatus.REJECTED.value
        elif decision == VerificationDecision.CORRECTION_REQUESTED:
            asset.verification_status = AssetVerificationStatus.CORRECTION_REQUIRED.value

        asset.updated_timestamp_utc = now
        await session.flush()
        return asset

    @staticmethod
    async def find_duplicate_candidates(
        session: AsyncSession,
        lat: float,
        lon: float,
        category: AssetCategory,
        radius_meters: float = 15.0,
    ) -> List[DuplicateCandidateSchema]:
        nearby_assets = await AssetService.get_assets_nearby(session, lat, lon, radius_meters=radius_meters)
        candidates: List[DuplicateCandidateSchema] = []

        for candidate in nearby_assets:
            lat_diff = (candidate.latitude - lat) * 111_000.0
            lon_diff = (candidate.longitude - lon) * 111_000.0
            dist_m = round((lat_diff**2 + lon_diff**2) ** 0.5, 2)

            is_same_type = candidate.asset_category == category.value
            advisory = (
                f"Matching category asset {candidate.asset_code} found within {dist_m}m."
                if is_same_type
                else f"Nearby asset {candidate.asset_code} ({candidate.asset_category}) found within {dist_m}m."
            )

            candidates.append(
                DuplicateCandidateSchema(
                    candidate_asset_id=candidate.id,
                    candidate_asset_code=candidate.asset_code,
                    asset_category=AssetCategory(candidate.asset_category),
                    distance_meters=dist_m,
                    verification_status=AssetVerificationStatus(candidate.verification_status),
                    advisory_message=advisory,
                )
            )

        return candidates
