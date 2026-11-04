package com.nagarlens.app.core.model

enum class ObservationType {
    INITIAL_INSPECTION,
    ROUTINE_INSPECTION,
    DAMAGE_REPORT,
    WATERLOGGING_REPORT,
    BLOCKAGE_REPORT,
    POST_INTERVENTION_INSPECTION,
    FOLLOW_UP_INSPECTION,
    OTHER
}

enum class ConditionSeverity {
    UNKNOWN,
    LOW,
    MODERATE,
    HIGH,
    CRITICAL
}

enum class InterventionType {
    DRAIN_CLEANING,
    SILT_REMOVAL,
    SOLID_WASTE_REMOVAL,
    STRUCTURAL_REPAIR,
    COVER_REPLACEMENT,
    WATER_REMOVAL,
    INSPECTION,
    OTHER
}

enum class InterventionStatus {
    PROPOSED,
    APPROVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    FAILED
}

enum class InterventionEvidenceType {
    BEFORE_ACTION,
    IN_PROGRESS,
    AFTER_ACTION
}

enum class VerificationOutcome {
    IMPROVED,
    PARTIALLY_IMPROVED,
    UNCHANGED,
    DETERIORATED,
    INCONCLUSIVE
}

data class InfrastructureObservation(
    val id: String,
    val assetId: String,
    val observationType: ObservationType,
    val conditionCategory: DefectCategory,
    val conditionSeverity: ConditionSeverity = ConditionSeverity.UNKNOWN,
    val description: String? = null,
    val observedAtUtc: Long,  // Field capture event timestamp
    val recordedAtUtc: Long = System.currentTimeMillis(),
    val observedBy: String = "field_worker",
    val latitude: Double,
    val longitude: Double,
    val locationAccuracyMeters: Float? = null,
    val source: String = "FIELD_INSPECTION",
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
)

data class ObservationEvidenceLink(
    val id: String,
    val observationId: String,
    val evidenceId: String,
    val relationshipType: String = "CONDITION_PROOF",
    val linkedAtUtc: Long = System.currentTimeMillis()
)

data class Intervention(
    val id: String,
    val assetId: String,
    val interventionType: InterventionType,
    val title: String,
    val description: String? = null,
    val status: InterventionStatus = InterventionStatus.PROPOSED,
    val plannedAtUtc: Long? = null,
    val startedAtUtc: Long? = null,
    val completedAtUtc: Long? = null,
    val recordedBy: String = "field_worker",
    val createdAtUtc: Long = System.currentTimeMillis(),
    val updatedAtUtc: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
)

data class InterventionEvidenceLink(
    val id: String,
    val interventionId: String,
    val evidenceId: String,
    val relationshipType: InterventionEvidenceType = InterventionEvidenceType.BEFORE_ACTION,
    val linkedAtUtc: Long = System.currentTimeMillis()
)

data class OutcomeVerification(
    val id: String,
    val interventionId: String,
    val assetId: String,
    val verificationObservationId: String,
    val outcome: VerificationOutcome,
    val description: String? = null,
    val verifiedAtUtc: Long = System.currentTimeMillis(),
    val verifiedBy: String = "reviewer",
    val createdAtUtc: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
)

data class TimelineEvent(
    val eventId: String,
    val eventType: String,  // REGISTRATION, OBSERVATION, INTERVENTION, VERIFICATION
    val timestampUtc: Long,
    val title: String,
    val description: String? = null,
    val actorId: String,
    val severity: String? = null,
    val outcome: String? = null,
    val linkedEvidenceId: String? = null
)

data class AssetTimeline(
    val assetId: String,
    val assetCode: String,
    val totalEvents: Int,
    val events: List<TimelineEvent>
)
