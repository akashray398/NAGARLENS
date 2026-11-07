"""NAGARLENS — Predictive Infrastructure Intelligence Router."""

import time
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from pydantic import BaseModel, Field

from app.core.security import AuthenticatedUser, get_current_user
from app.ml.predictive.engine import (
    AnomalyDetectionResult,
    EarlyWarningAlert,
    HumanFeedbackOutcome,
    RiskLevel,
    predictive_engine,
)
from app.ml.taxonomy import DefectCategory

router = APIRouter(prefix="/predictive", tags=["Predictive Intelligence & Early Warning Engine"])


class RiskEvaluationRequestSchema(BaseModel):
    asset_id: str
    asset_code: str
    observations: List[dict] = Field(default_factory=list)
    incidents: List[dict] = Field(default_factory=list)


class HumanFeedbackRequestSchema(BaseModel):
    warning_id: str
    asset_id: str
    feedback: HumanFeedbackOutcome
    reviewer_notes: Optional[str] = None


_RISK_STORE: dict[str, EarlyWarningAlert] = {
    "AST-10001": EarlyWarningAlert(
        warning_id="WARN-AST-1000-001",
        asset_id="AST-10001",
        asset_code="NAG-CB-000104",
        risk_level=RiskLevel.HIGH,
        risk_score=0.78,
        confidence_score=0.85,
        primary_defect_category=DefectCategory.DRAIN_BLOCKAGE,
        summary_explanation="Early warning signal: HIGH risk (78%) for asset NAG-CB-000104. Primary defect: DRAIN_BLOCKAGE.",
        contributing_factors=[
            "Observed defect recurrence count: 4 observations recorded.",
            "Latest photographic severity estimate: 75%.",
            "Active progressive deterioration trend detected across consecutive inspections."
        ],
        recommended_action="HIGH PRIORITY: Schedule physical work order inspection for DRAIN_BLOCKAGE within 48 hours.",
        evidence_ids=["e3b0c44298fc1c14"],
        generated_timestamp_utc=int(time.time() * 1000) - 1800000
    )
}


@router.post("/evaluate", response_model=EarlyWarningAlert, status_code=status.HTTP_200_OK, summary="Evaluate predictive risk score and early warning signal for an asset")
async def evaluate_asset_risk(
    payload: RiskEvaluationRequestSchema,
    current_user: AuthenticatedUser = Depends(get_current_user)
):
    alert = predictive_engine.evaluate_asset_risk(
        asset_id=payload.asset_id,
        asset_code=payload.asset_code,
        observations=payload.observations,
        incidents=payload.incidents
    )
    _RISK_STORE[payload.asset_id] = alert
    return alert


@router.get("/assets/{asset_id}/risk", response_model=EarlyWarningAlert, summary="Retrieve risk score and early warning signal for specific asset")
async def get_asset_risk(asset_id: str):
    if asset_id in _RISK_STORE:
        return _RISK_STORE[asset_id]

    # Generate default risk response for requested asset ID
    return predictive_engine.evaluate_asset_risk(
        asset_id=asset_id,
        asset_code=f"NAG-CB-{asset_id[:6].upper()}",
        observations=[
            {"id": "obs_01", "defect_category": "DRAIN_BLOCKAGE", "severity_score": 0.65},
            {"id": "obs_02", "defect_category": "DRAIN_BLOCKAGE", "severity_score": 0.78}
        ],
        incidents=[]
    )


@router.get("/warnings", response_model=List[EarlyWarningAlert], summary="List all active high-priority early warning alerts")
async def list_early_warning_alerts(
    min_risk_level: Optional[RiskLevel] = Query(None, description="Minimum risk level filter")
):
    alerts = list(_RISK_STORE.values())
    if min_risk_level:
        order = [RiskLevel.INFORMATIONAL, RiskLevel.LOW, RiskLevel.MODERATE, RiskLevel.HIGH, RiskLevel.CRITICAL]
        min_idx = order.index(min_risk_level)
        alerts = [a for a in alerts if order.index(a.risk_level) >= min_idx]
    return alerts


@router.post("/feedback", response_model=EarlyWarningAlert, summary="Submit human reviewer feedback on predictive early warning alert")
async def submit_human_feedback(
    payload: HumanFeedbackRequestSchema,
    current_user: AuthenticatedUser = Depends(get_current_user)
):
    alert = _RISK_STORE.get(payload.asset_id)
    if not alert:
        alert = predictive_engine.evaluate_asset_risk(
            asset_id=payload.asset_id,
            asset_code=f"NAG-CB-{payload.asset_id[:6].upper()}",
            observations=[],
            incidents=[]
        )

    alert.is_human_reviewed = True
    alert.human_feedback = payload.feedback
    _RISK_STORE[payload.asset_id] = alert
    return alert
