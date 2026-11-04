"""NAGARLENS — SQLAlchemy Temporal Reality Graph Data Models."""

import time
import uuid
from sqlalchemy import BigInteger, Column, Double, Float, ForeignKey, String, Text
from sqlalchemy.orm import relationship

from app.infrastructure.db.base import Base


class ObservationModel(Base):
    __tablename__ = "observations"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    asset_id = Column(String(36), ForeignKey("assets.id", ondelete="CASCADE"), nullable=False, index=True)
    observation_type = Column(String(32), nullable=False)
    condition_category = Column(String(32), nullable=False)
    condition_severity = Column(String(32), nullable=False, default="UNKNOWN")
    description = Column(Text, nullable=True)
    observed_at_utc = Column(BigInteger, nullable=False, index=True)  # Field event time
    recorded_at_utc = Column(BigInteger, nullable=False, default=lambda: int(time.time() * 1000))  # Server ingestion time
    observed_by = Column(String(64), nullable=False)
    latitude = Column(Double, nullable=False)
    longitude = Column(Double, nullable=False)
    location_accuracy_meters = Column(Float, nullable=True)
    source = Column(String(32), nullable=False, default="FIELD_INSPECTION")

    evidence_links = relationship("ObservationEvidenceLinkModel", back_populates="observation", cascade="all, delete-orphan")


class ObservationEvidenceLinkModel(Base):
    __tablename__ = "observation_evidence_links"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    observation_id = Column(String(36), ForeignKey("observations.id", ondelete="CASCADE"), nullable=False, index=True)
    evidence_id = Column(String(64), nullable=False, index=True)
    relationship_type = Column(String(32), nullable=False, default="CONDITION_PROOF")
    linked_at_utc = Column(BigInteger, nullable=False, default=lambda: int(time.time() * 1000))

    observation = relationship("ObservationModel", back_populates="evidence_links")


class InterventionModel(Base):
    __tablename__ = "interventions"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    asset_id = Column(String(36), ForeignKey("assets.id", ondelete="CASCADE"), nullable=False, index=True)
    intervention_type = Column(String(32), nullable=False)
    title = Column(String(128), nullable=False)
    description = Column(Text, nullable=True)
    status = Column(String(32), nullable=False, default="PROPOSED")
    planned_at_utc = Column(BigInteger, nullable=True)
    started_at_utc = Column(BigInteger, nullable=True)
    completed_at_utc = Column(BigInteger, nullable=True)
    recorded_by = Column(String(64), nullable=False)
    created_at_utc = Column(BigInteger, nullable=False, default=lambda: int(time.time() * 1000))
    updated_at_utc = Column(BigInteger, nullable=False, default=lambda: int(time.time() * 1000))

    evidence_links = relationship("InterventionEvidenceLinkModel", back_populates="intervention", cascade="all, delete-orphan")
    outcome_verifications = relationship("OutcomeVerificationModel", back_populates="intervention", cascade="all, delete-orphan")
    status_history = relationship("InterventionStatusHistoryModel", back_populates="intervention", cascade="all, delete-orphan")


class InterventionEvidenceLinkModel(Base):
    __tablename__ = "intervention_evidence_links"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    intervention_id = Column(String(36), ForeignKey("interventions.id", ondelete="CASCADE"), nullable=False, index=True)
    evidence_id = Column(String(64), nullable=False, index=True)
    relationship_type = Column(String(32), nullable=False, default="BEFORE_ACTION")
    linked_at_utc = Column(BigInteger, nullable=False, default=lambda: int(time.time() * 1000))

    intervention = relationship("InterventionModel", back_populates="evidence_links")


class InterventionStatusHistoryModel(Base):
    __tablename__ = "intervention_status_history"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    intervention_id = Column(String(36), ForeignKey("interventions.id", ondelete="CASCADE"), nullable=False, index=True)
    previous_status = Column(String(32), nullable=False)
    new_status = Column(String(32), nullable=False)
    reason = Column(Text, nullable=True)
    actor_id = Column(String(64), nullable=False)
    recorded_at_utc = Column(BigInteger, nullable=False, default=lambda: int(time.time() * 1000))

    intervention = relationship("InterventionModel", back_populates="status_history")


class OutcomeVerificationModel(Base):
    __tablename__ = "outcome_verifications"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    intervention_id = Column(String(36), ForeignKey("interventions.id", ondelete="CASCADE"), nullable=False, index=True)
    asset_id = Column(String(36), ForeignKey("assets.id", ondelete="CASCADE"), nullable=False, index=True)
    verification_observation_id = Column(String(36), ForeignKey("observations.id", ondelete="CASCADE"), nullable=False, index=True)
    outcome = Column(String(32), nullable=False)
    description = Column(Text, nullable=True)
    verified_at_utc = Column(BigInteger, nullable=False, default=lambda: int(time.time() * 1000))
    verified_by = Column(String(64), nullable=False)
    created_at_utc = Column(BigInteger, nullable=False, default=lambda: int(time.time() * 1000))

    intervention = relationship("InterventionModel", back_populates="outcome_verifications")
