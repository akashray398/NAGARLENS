package com.nagarlens.app.core.model

enum class RiskLevel {
    INFORMATIONAL,
    LOW,
    MODERATE,
    HIGH,
    CRITICAL
}

enum class HumanFeedbackOutcome {
    CONFIRMED,
    REJECTED_FALSE_POSITIVE,
    SEVERITY_ADJUSTED
}

data class EarlyWarningAlert(
    val warningId: String,
    val assetId: String,
    val assetCode: String,
    val riskLevel: RiskLevel,
    val riskScore: Float, // 0.0 to 1.0
    val confidenceScore: Float, // 0.0 to 1.0
    val primaryDefectCategory: DefectCategory,
    val summaryExplanation: String,
    val contributingFactors: List<String>,
    val recommendedAction: String,
    val evidenceIds: List<String>,
    val modelVersion: String = "v1.0.0-predictive",
    val generatedTimestampUtc: Long = System.currentTimeMillis(),
    val isHumanReviewed: Boolean = false,
    val humanFeedback: HumanFeedbackOutcome? = null
)

data class PredictiveAssetProfile(
    val asset: Asset,
    val alert: EarlyWarningAlert,
    val recurrenceCount: Int,
    val severityTrendSlope: Float, // > 0 means deteriorating
    val hasAnomalousSpike: Boolean = false
)
