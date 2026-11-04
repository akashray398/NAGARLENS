package com.nagarlens.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nagarlens.app.core.model.ConditionSeverity
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.InfrastructureObservation
import com.nagarlens.app.core.model.ObservationType
import com.nagarlens.app.core.model.SyncStatus

@Entity(tableName = "observations_records")
data class ObservationEntity(
    @PrimaryKey val id: String,
    val assetId: String,
    val observationType: String,
    val conditionCategory: String,
    val conditionSeverity: String = ConditionSeverity.UNKNOWN.name,
    val description: String? = null,
    val observedAtUtc: Long,
    val recordedAtUtc: Long = System.currentTimeMillis(),
    val observedBy: String = "field_worker",
    val latitude: Double,
    val longitude: Double,
    val locationAccuracyMeters: Float? = null,
    val source: String = "FIELD_INSPECTION",
    val syncStatus: String = SyncStatus.LOCAL_ONLY.name
)

fun ObservationEntity.toDomainModel(): InfrastructureObservation {
    return InfrastructureObservation(
        id = id,
        assetId = assetId,
        observationType = runCatching { ObservationType.valueOf(observationType) }.getOrDefault(ObservationType.ROUTINE_INSPECTION),
        conditionCategory = runCatching { DefectCategory.valueOf(conditionCategory) }.getOrDefault(DefectCategory.UNKNOWN_INSUFFICIENT),
        conditionSeverity = runCatching { ConditionSeverity.valueOf(conditionSeverity) }.getOrDefault(ConditionSeverity.UNKNOWN),
        description = description,
        observedAtUtc = observedAtUtc,
        recordedAtUtc = recordedAtUtc,
        observedBy = observedBy,
        latitude = latitude,
        longitude = longitude,
        locationAccuracyMeters = locationAccuracyMeters,
        source = source,
        syncStatus = runCatching { SyncStatus.valueOf(syncStatus) }.getOrDefault(SyncStatus.LOCAL_ONLY)
    )
}

fun InfrastructureObservation.toEntity(): ObservationEntity {
    return ObservationEntity(
        id = id,
        assetId = assetId,
        observationType = observationType.name,
        conditionCategory = conditionCategory.name,
        conditionSeverity = conditionSeverity.name,
        description = description,
        observedAtUtc = observedAtUtc,
        recordedAtUtc = recordedAtUtc,
        observedBy = observedBy,
        latitude = latitude,
        longitude = longitude,
        locationAccuracyMeters = locationAccuracyMeters,
        source = source,
        syncStatus = syncStatus.name
    )
}
