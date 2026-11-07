package com.nagarlens.app.core.model

enum class BoundaryType {
    MUNICIPAL_WARD,
    DRAINAGE_CATCHMENT,
    MAINTENANCE_ZONE,
    SUB_CATCHMENT
}

data class SpatialBoundary(
    val id: String,
    val name: String,
    val boundaryType: BoundaryType,
    val wardNumber: Int?,
    val minLatitude: Double,
    val minLongitude: Double,
    val maxLatitude: Double,
    val maxLongitude: Double,
    val totalRegisteredAssets: Int = 0
)

data class SpatialRelationship(
    val sourceAssetId: String,
    val targetAssetId: String,
    val relationshipType: String, // e.g. "INFLOW_TO", "OUTFLOW_TO", "ADJACENT_TO"
    val distanceMeters: Double
)

data class DigitalTwinAsset(
    val asset: Asset,
    val distanceToUserMeters: Double? = null,
    val bearingFromUserDegrees: Double? = null,
    val linkedEvidenceCount: Int = 0,
    val openIncidentsCount: Int = 0,
    val locationCorrectionCount: Int = 0,
    val catchmentBoundaryName: String = "Ward 12 — Bangalore Central",
    val lastInspectionTimestampUtc: Long? = null
)
