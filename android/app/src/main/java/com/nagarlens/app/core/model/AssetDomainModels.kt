package com.nagarlens.app.core.model

enum class AssetCategory {
    STORM_WATER_DRAIN,
    OPEN_DRAIN,
    COVERED_DRAIN,
    CATCH_BASIN,
    MANHOLE,
    CULVERT,
    DRAIN_OUTLET,
    WATER_CHANNEL,
    OTHER,
    UNKNOWN;

    companion object {
        fun getPrefix(category: AssetCategory): String {
            return when (category) {
                STORM_WATER_DRAIN -> "SWD"
                OPEN_DRAIN -> "OPD"
                COVERED_DRAIN -> "CVD"
                CATCH_BASIN -> "CB"
                MANHOLE -> "MH"
                CULVERT -> "CVT"
                DRAIN_OUTLET -> "OUT"
                WATER_CHANNEL -> "WCH"
                OTHER -> "OTH"
                UNKNOWN -> "UNK"
            }
        }
    }
}

enum class AssetLifecycleStatus {
    ACTIVE,
    UNDER_CONSTRUCTION,
    TEMPORARILY_CLOSED,
    OUT_OF_SERVICE,
    RETIRED
}

enum class AssetVerificationStatus {
    UNVERIFIED,
    PENDING_REVIEW,
    VERIFIED,
    REJECTED,
    CORRECTION_REQUIRED
}

enum class IdentifierType {
    QR_CODE,
    NFC_TAG,
    MUNICIPAL_ID
}

enum class AssetRelationshipType {
    INITIAL_REGISTRATION,
    CONDITION_OBSERVATION,
    DAMAGE_EVIDENCE,
    MAINTENANCE_EVIDENCE,
    LOCATION_VERIFICATION,
    OTHER
}

enum class VerificationDecision {
    APPROVED,
    REJECTED,
    CORRECTION_REQUESTED
}

data class Asset(
    val id: String,
    val assetCode: String,
    val assetCategory: AssetCategory,
    val lifecycleStatus: AssetLifecycleStatus = AssetLifecycleStatus.ACTIVE,
    val verificationStatus: AssetVerificationStatus = AssetVerificationStatus.UNVERIFIED,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Float? = null,
    val description: String? = null,
    val version: Int = 1,
    val createdBy: String = "field_worker",
    val createdTimestampUtc: Long = System.currentTimeMillis(),
    val updatedTimestampUtc: Long = System.currentTimeMillis()
)

data class AssetLocationHistory(
    val id: String,
    val assetId: String,
    val previousLatitude: Double,
    val previousLongitude: Double,
    val newLatitude: Double,
    val newLongitude: Double,
    val reason: String? = null,
    val actorId: String,
    val recordedTimestampUtc: Long = System.currentTimeMillis()
)

data class AssetIdentifier(
    val id: String,
    val assetId: String,
    val identifierType: IdentifierType,
    val identifierValue: String,
    val isActive: Boolean = true,
    val createdTimestampUtc: Long = System.currentTimeMillis(),
    val revokedTimestampUtc: Long? = null
)

data class AssetEvidenceLink(
    val id: String,
    val assetId: String,
    val evidenceId: String,
    val relationshipType: AssetRelationshipType = AssetRelationshipType.CONDITION_OBSERVATION,
    val createdBy: String = "field_worker",
    val createdTimestampUtc: Long = System.currentTimeMillis()
)

data class DuplicateCandidate(
    val candidateAssetId: String,
    val candidateAssetCode: String,
    val assetCategory: AssetCategory,
    val distanceMeters: Float,
    val verificationStatus: AssetVerificationStatus,
    val advisoryMessage: String
)
