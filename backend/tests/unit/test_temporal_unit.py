"""NAGARLENS — Temporal Reality Graph Domain Unit Tests."""

import time
import pytest
from app.ml.taxonomy import DefectCategory
from app.modules.temporal.enums import (
    ConditionSeverity,
    InterventionStatus,
    InterventionType,
    ObservationType,
    VerificationOutcome,
)
from app.modules.temporal.schemas import (
    AssetTimelineResponseSchema,
    InterventionCreateSchema,
    ObservationCreateSchema,
    OutcomeVerificationCreateSchema,
    TimelineEventSchema,
)


def test_observation_create_schema_validates_coordinates():
    obs = ObservationCreateSchema(
        observation_type=ObservationType.BLOCKAGE_REPORT,
        condition_category=DefectCategory.SOLID_WASTE,
        condition_severity=ConditionSeverity.HIGH,
        description="Heavy trash accumulation in catch basin",
        observed_at_utc=1711987200000,
        latitude=12.9716,
        longitude=77.5946,
    )
    assert obs.observation_type == ObservationType.BLOCKAGE_REPORT
    assert obs.condition_severity == ConditionSeverity.HIGH
    assert obs.latitude == 12.9716

    with pytest.raises(Exception):
        ObservationCreateSchema(
            observation_type=ObservationType.BLOCKAGE_REPORT,
            condition_category=DefectCategory.SOLID_WASTE,
            observed_at_utc=1711987200000,
            latitude=150.0,  # Invalid latitude (>90)
            longitude=77.5946,
        )


def test_intervention_create_schema():
    interv = InterventionCreateSchema(
        intervention_type=InterventionType.SILT_REMOVAL,
        title="Desilting Catch Basin NAG-CB-000104",
        description="Removed 50kg silt bed from invert",
        planned_at_utc=1711987200000,
    )
    assert interv.intervention_type == InterventionType.SILT_REMOVAL
    assert "Desilting" in interv.title


def test_asset_timeline_event_compilation():
    events = [
        TimelineEventSchema(
            event_id="EVT_02",
            event_type="OBSERVATION",
            timestamp_utc=2000,
            title="Routine Inspection",
            actor_id="worker_01",
        ),
        TimelineEventSchema(
            event_id="EVT_01",
            event_type="REGISTRATION",
            timestamp_utc=1000,
            title="Asset Created",
            actor_id="system",
        ),
    ]

    events.sort(key=lambda x: x.timestamp_utc)
    timeline = AssetTimelineResponseSchema(
        asset_id="AST_101",
        asset_code="NAG-CB-000101",
        total_events=len(events),
        events=events,
    )

    assert timeline.total_events == 2
    assert timeline.events[0].event_id == "EVT_01"  # Oldest event first
    assert timeline.events[1].event_id == "EVT_02"
