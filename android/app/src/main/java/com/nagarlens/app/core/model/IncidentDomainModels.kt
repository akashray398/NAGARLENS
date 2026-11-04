package com.nagarlens.app.core.model

enum class IncidentStatus {
    OPEN,
    IN_PROGRESS,
    UNDER_INVESTIGATION,
    RESOLVED,
    CLOSED
}

enum class IncidentPriority {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

data class Incident(
    val id: String,
    val assetId: String,
    val assetCode: String,
    val title: String,
    val description: String?,
    val status: IncidentStatus = IncidentStatus.OPEN,
    val priority: IncidentPriority = IncidentPriority.HIGH,
    val defectCategory: DefectCategory = DefectCategory.DRAIN_BLOCKAGE,
    val reportedBy: String = "field_inspector",
    val createdTimestampUtc: Long = System.currentTimeMillis(),
    val updatedTimestampUtc: Long = System.currentTimeMillis()
)

data class InvestigationCase(
    val id: String,
    val incidentId: String,
    val caseNumber: String,
    val summary: String,
    val rootCauseAnalysis: String?,
    val assignedInvestigator: String,
    val status: String,
    val createdTimestampUtc: Long = System.currentTimeMillis()
)
