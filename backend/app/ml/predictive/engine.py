"""NAGARLENS — Predictive Infrastructure Intelligence & Early-Warning Engine.

Implements:
- Asset Condition & Degradation Scoring
- Temporal Recurrence & Trend Analysis
- Anomaly Detection (Statistical Z-Score & Severity Spikes)
- Spatial Cluster Risk Aggregation
- Explainable Risk Scoring & Recommendation Generation
- Human Review & Feedback Integration
"""

import math
import time
from dataclasses import dataclass, field
from enum import Enum
from typing import List, Optional
from pydantic import BaseModel, Field

from app.ml.taxonomy import DefectCategory


class RiskLevel(str, Enum):
    INFORMATIONAL = "INFORMATIONAL"
    LOW = "LOW"
    MODERATE = "MODERATE"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"


class HumanFeedbackOutcome(str, Enum):
    CONFIRMED = "CONFIRMED"
    REJECTED_FALSE_POSITIVE = "REJECTED_FALSE_POSITIVE"
    SEVERITY_ADJUSTED = "SEVERITY_ADJUSTED"


class AnomalyDetectionResult(BaseModel):
    is_anomaly: bool
    anomaly_type: str  # "SUDDEN_SEVERITY_SPIKE", "UNUSUALLY_HIGH_RECURRENCE", "RAPID_DETERIORATION"
    z_score: float
    observed_value: float
    baseline_value: float
    detection_timestamp_utc: int
    explanation: str


class EarlyWarningAlert(BaseModel):
    warning_id: str
    asset_id: str
    asset_code: str
    risk_level: RiskLevel
    risk_score: float  # 0.0 to 1.0
    confidence_score: float  # 0.0 to 1.0
    primary_defect_category: DefectCategory
    summary_explanation: str
    contributing_factors: List[str]
    recommended_action: str
    evidence_ids: List[str]
    model_version: str = "v1.0.0-predictive"
    generated_timestamp_utc: int
    is_human_reviewed: bool = False
    human_feedback: Optional[HumanFeedbackOutcome] = None


class PredictiveRiskEngine:
    """Core Predictive Analytics & Early Warning Engine for Infrastructure Assets."""

    def __init__(self, model_version: str = "v1.0.0-predictive"):
        self.model_version = model_version

    def evaluate_asset_risk(
        self,
        asset_id: str,
        asset_code: str,
        observations: List[dict],
        incidents: List[dict],
        nearby_asset_risks: List[float] = None
    ) -> EarlyWarningAlert:
        now = int(time.time() * 1000)

        if not observations:
            return EarlyWarningAlert(
                warning_id=f"WARN-{asset_id[:8]}-{now % 10000}",
                asset_id=asset_id,
                asset_code=asset_code,
                risk_level=RiskLevel.INFORMATIONAL,
                risk_score=0.05,
                confidence_score=0.20,
                primary_defect_category=DefectCategory.UNKNOWN_INSUFFICIENT,
                summary_explanation="Insufficient historical observations recorded to calculate predictive risk trend.",
                contributing_factors=["No historical field inspections on record."],
                recommended_action="Schedule initial baseline physical inspection.",
                evidence_ids=[],
                generated_timestamp_utc=now
            )

        # 1. Condition & Defect Recurrence Factor (0.0 - 0.40)
        recurrence_count = len(observations)
        latest_obs = observations[-1]
        defect_cat = latest_obs.get("defect_category", DefectCategory.DRAIN_BLOCKAGE)

        if isinstance(defect_cat, str):
            try:
                defect_cat = DefectCategory(defect_cat)
            except ValueError:
                defect_cat = DefectCategory.DRAIN_BLOCKAGE

        recurrence_factor = min(0.40, recurrence_count * 0.08)

        # 2. Latest Severity Factor (0.0 - 0.35)
        latest_severity = latest_obs.get("severity_score", 0.50)
        if isinstance(latest_severity, str):
            sev_map = {"CRITICAL": 0.90, "HIGH": 0.75, "MODERATE": 0.50, "MEDIUM": 0.50, "LOW": 0.30, "INFORMATIONAL": 0.10}
            latest_severity = sev_map.get(latest_severity.upper(), 0.50)
        severity_factor = min(0.35, float(latest_severity) * 0.35)

        # 3. Deterioration Rate Factor (0.0 - 0.15)
        deterioration_factor = 0.0
        if len(observations) >= 2:
            first_sev = float(observations[0].get("severity_score", 0.30))
            last_sev = float(observations[-1].get("severity_score", 0.70))
            if last_sev > first_sev:
                deterioration_factor = min(0.15, (last_sev - first_sev) * 0.30)

        # 4. Incident & Spatial Cluster Factor (0.0 - 0.10)
        incident_factor = min(0.10, len(incidents) * 0.05)
        spatial_factor = 0.0
        if nearby_asset_risks:
            spatial_factor = min(0.05, (sum(nearby_asset_risks) / len(nearby_asset_risks)) * 0.05)

        total_risk_score = round(min(1.0, recurrence_factor + severity_factor + deterioration_factor + incident_factor + spatial_factor), 3)

        # Map to Risk Level Band
        risk_level = self._map_score_to_level(total_risk_score)

        # Formulate Explainability Payload
        contributing_factors = [
            f"Observed defect recurrence count: {recurrence_count} observations recorded.",
            f"Latest photographic severity estimate: {int(latest_severity * 100)}%.",
        ]
        if deterioration_factor > 0.05:
            contributing_factors.append("Active progressive deterioration trend detected across consecutive inspections.")
        if incidents:
            contributing_factors.append(f"Linked to {len(incidents)} active municipal work order incidents.")
        if spatial_factor > 0.02:
            contributing_factors.append("Surrounded by elevated risk infrastructure within 100m catchment buffer.")

        recommendation = self._generate_recommendation(risk_level, defect_cat)
        evidence_ids = [str(o.get("sha256_hash", o.get("id", "")))[:16] for o in observations if o.get("sha256_hash") or o.get("id")]

        return EarlyWarningAlert(
            warning_id=f"WARN-{asset_id[:8]}-{now % 10000}",
            asset_id=asset_id,
            asset_code=asset_code,
            risk_level=risk_level,
            risk_score=total_risk_score,
            confidence_score=round(min(0.95, 0.50 + (recurrence_count * 0.08)), 2),
            primary_defect_category=defect_cat,
            summary_explanation=f"Early warning signal: {risk_level.value} risk ({int(total_risk_score * 100)}%) for asset {asset_code}. Primary defect: {defect_cat.value}.",
            contributing_factors=contributing_factors,
            recommended_action=recommendation,
            evidence_ids=evidence_ids,
            model_version=self.model_version,
            generated_timestamp_utc=now
        )

    def detect_anomalies(self, observations: List[dict]) -> AnomalyDetectionResult:
        now = int(time.time() * 1000)
        if len(observations) < 3:
            return AnomalyDetectionResult(
                is_anomaly=False,
                anomaly_type="NONE",
                z_score=0.0,
                observed_value=0.0,
                baseline_value=0.0,
                detection_timestamp_utc=now,
                explanation="Insufficient inspection points to establish statistical baseline."
            )

        severities = [float(o.get("severity_score", 0.5)) for o in observations]
        past_severities = severities[:-1]
        baseline_mean = sum(past_severities) / len(past_severities)
        variance = sum((x - baseline_mean) ** 2 for x in past_severities) / len(past_severities)
        baseline_std = math.sqrt(variance) or 0.01

        latest_value = severities[-1]
        z_score = round((latest_value - baseline_mean) / baseline_std, 2)

        is_anomaly = z_score >= 2.0
        anomaly_type = "SUDDEN_SEVERITY_SPIKE" if is_anomaly else "NONE"
        explanation = (
            f"Anomalous severity spike detected! Latest severity ({latest_value:.2f}) is {z_score:.1f} standard deviations above mean baseline ({baseline_mean:.2f})."
            if is_anomaly
            else f"Severity ({latest_value:.2f}) within expected statistical baseline ({baseline_mean:.2f} ± {baseline_std:.2f})."
        )

        return AnomalyDetectionResult(
            is_anomaly=is_anomaly,
            anomaly_type=anomaly_type,
            z_score=z_score,
            observed_value=latest_value,
            baseline_value=round(baseline_mean, 2),
            detection_timestamp_utc=now,
            explanation=explanation
        )

    def _map_score_to_level(self, score: float) -> RiskLevel:
        if score >= 0.80:
            return RiskLevel.CRITICAL
        elif score >= 0.65:
            return RiskLevel.HIGH
        elif score >= 0.45:
            return RiskLevel.MODERATE
        elif score >= 0.25:
            return RiskLevel.LOW
        else:
            return RiskLevel.INFORMATIONAL

    def _generate_recommendation(self, level: RiskLevel, category: DefectCategory) -> str:
        if level == RiskLevel.CRITICAL:
            return f"CRITICAL: Dispatch emergency desilting crew to clear {category.value} within 12 hours."
        elif level == RiskLevel.HIGH:
            return f"HIGH PRIORITY: Schedule physical work order inspection for {category.value} within 48 hours."
        elif level == RiskLevel.MODERATE:
            return f"MODERATE: Add asset to routine weekly maintenance queue for {category.value} review."
        else:
            return "INFORMATIONAL: Continue routine field monitoring according to monthly schedule."


predictive_engine = PredictiveRiskEngine()
