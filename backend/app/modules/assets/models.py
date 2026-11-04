"""NAGARLENS — SQLAlchemy PostGIS Asset Data Models."""

import time
import uuid
from geoalchemy2 import Geometry
from sqlalchemy import BigInteger, Boolean, Column, Double, Float, ForeignKey, Integer, String, Text
from sqlalchemy.orm import relationship

from app.infrastructure.db.base import Base


class AssetModel(Base):
    __tablename__ = "assets"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    asset_code = Column(String(32), unique=True, nullable=False, index=True)
    asset_category = Column(String(32), nullable=False, index=True)
    lifecycle_status = Column(String(32), nullable=False, default="ACTIVE")
    verification_status = Column(String(32), nullable=False, default="UNVERIFIED", index=True)

    # PostGIS Point Geometry in SRID 4326 (WGS 84)
    location = Column(Geometry("POINT", srid=4326, spatial_index=True), nullable=True)
    latitude = Column(Double, nullable=False)
    longitude = Column(Double, nullable=False)
    altitude_meters = Column(Float, nullable=True)

    description = Column(Text, nullable=True)
    version = Column(Integer, nullable=False, default=1)
    created_by = Column(String(64), nullable=False, default="system")
    created_timestamp_utc = Column(BigInteger, nullable=False, default=lambda: int(time.time() * 1000))
    updated_timestamp_utc = Column(BigInteger, nullable=False, default=lambda: int(time.time() * 1000))

    location_history = relationship("AssetLocationHistoryModel", back_populates="asset", cascade="all, delete-orphan")
    identifiers = relationship("AssetIdentifierRegistryModel", back_populates="asset", cascade="all, delete-orphan")
    evidence_links = relationship("AssetEvidenceLinkModel", back_populates="asset", cascade="all, delete-orphan")
    verification_records = relationship("AssetVerificationRecordModel", back_populates="asset", cascade="all, delete-orphan")


class AssetLocationHistoryModel(Base):
    __tablename__ = "asset_location_history"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    asset_id = Column(String(36), ForeignKey("assets.id", ondelete="CASCADE"), nullable=False, index=True)
    previous_latitude = Column(Double, nullable=False)
    previous_longitude = Column(Double, nullable=False)
    new_latitude = Column(Double, nullable=False)
    new_longitude = Column(Double, nullable=False)
    reason = Column(Text, nullable=True)
    actor_id = Column(String(64), nullable=False)
    recorded_timestamp_utc = Column(BigInteger, nullable=False, default=lambda: int(time.time() * 1000))

    asset = relationship("AssetModel", back_populates="location_history")


class AssetIdentifierRegistryModel(Base):
    __tablename__ = "asset_identifier_registry"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    asset_id = Column(String(36), ForeignKey("assets.id", ondelete="CASCADE"), nullable=False, index=True)
    identifier_type = Column(String(32), nullable=False)
    identifier_value = Column(String(128), nullable=False, index=True)
    is_active = Column(Boolean, nullable=False, default=True)
    created_timestamp_utc = Column(BigInteger, nullable=False, default=lambda: int(time.time() * 1000))
    revoked_timestamp_utc = Column(BigInteger, nullable=True)

    asset = relationship("AssetModel", back_populates="identifiers")


class AssetEvidenceLinkModel(Base):
    __tablename__ = "asset_evidence_links"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    asset_id = Column(String(36), ForeignKey("assets.id", ondelete="CASCADE"), nullable=False, index=True)
    evidence_id = Column(String(64), nullable=False, index=True)
    relationship_type = Column(String(32), nullable=False, default="CONDITION_OBSERVATION")
    created_by = Column(String(64), nullable=False, default="system")
    created_timestamp_utc = Column(BigInteger, nullable=False, default=lambda: int(time.time() * 1000))

    asset = relationship("AssetModel", back_populates="evidence_links")


class AssetVerificationRecordModel(Base):
    __tablename__ = "asset_verification_records"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    asset_id = Column(String(36), ForeignKey("assets.id", ondelete="CASCADE"), nullable=False, index=True)
    decision = Column(String(32), nullable=False)
    reason = Column(Text, nullable=True)
    reviewer_id = Column(String(64), nullable=False)
    decision_timestamp_utc = Column(BigInteger, nullable=False, default=lambda: int(time.time() * 1000))

    asset = relationship("AssetModel", back_populates="verification_records")
