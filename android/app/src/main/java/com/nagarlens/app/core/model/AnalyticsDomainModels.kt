package com.nagarlens.app.core.model

data class InfrastructureMetrics(
    val totalAssets: Int,
    val verifiedAssets: Int,
    val pendingReviewAssets: Int,
    val unverifiedAssets: Int,
    val totalObservations: Int,
    val totalEvidenceFiles: Int,
    val openIncidentsCount: Int,
    val resolvedIncidentsCount: Int,
    val averageResolutionTimeHours: Float
)

data class ConditionDistribution(
    val category: String,
    val count: Int,
    val percentage: Float
)

data class DefectDistribution(
    val defectCategory: DefectCategory,
    val incidentCount: Int,
    val severityAverage: Float
)
