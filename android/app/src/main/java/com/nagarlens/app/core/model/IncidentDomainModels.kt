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

enum class InvestigationStatus {
    DRAFT,
    OPEN,
    TRIAGED,
    INVESTIGATING,
    AWAITING_EVIDENCE,
    AWAITING_REVIEW,
    ACTION_PROPOSED,
    AWAITING_APPROVAL,
    ACTION_IN_PROGRESS,
    RESOLUTION_REVIEW,
    RESOLVED,
    CLOSED,
    REJECTED,
    REOPENED
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

data class InvestigationTimelineEvent(
    val eventId: String,
    val investigationId: String,
    val eventType: String,
    val actorId: String,
    val timestampUtc: Long,
    val title: String,
    val description: String? = null
)

data class EvidenceRequestRecord(
    val requestId: String,
    val investigationId: String,
    val assetId: String,
    val requestedEvidenceType: String,
    val instructions: String,
    val requestedBy: String,
    val assignedTo: String,
    val status: String = "PENDING",
    val createdTimestampUtc: Long = System.currentTimeMillis()
)

data class InvestigationCase(
    val id: String,
    val title: String,
    val description: String?,
    val category: String = "DRAINAGE_BLOCKAGE_INVESTIGATION",
    val assetId: String,
    val assetCode: String,
    val incidentId: String? = null,
    val status: InvestigationStatus = InvestigationStatus.INVESTIGATING,
    val priority: IncidentPriority = IncidentPriority.HIGH,
    val assignedInvestigator: String = "usr_dev_auditor_001",
    val createdTimestampUtc: Long = System.currentTimeMillis(),
    val updatedTimestampUtc: Long = System.currentTimeMillis(),
    val timelineEvents: List<InvestigationTimelineEvent> = emptyList()
)
