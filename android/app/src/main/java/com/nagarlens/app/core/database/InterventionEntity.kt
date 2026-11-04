package com.nagarlens.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nagarlens.app.core.model.Intervention
import com.nagarlens.app.core.model.InterventionStatus
import com.nagarlens.app.core.model.InterventionType
import com.nagarlens.app.core.model.SyncStatus

@Entity(tableName = "interventions_records")
data class InterventionEntity(
    @PrimaryKey val id: String,
    val assetId: String,
    val interventionType: String,
    val title: String,
    val description: String? = null,
    val status: String = InterventionStatus.PROPOSED.name,
    val plannedAtUtc: Long? = null,
    val startedAtUtc: Long? = null,
    val completedAtUtc: Long? = null,
    val recordedBy: String = "field_worker",
    val createdAtUtc: Long = System.currentTimeMillis(),
    val updatedAtUtc: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.LOCAL_ONLY.name
)

fun InterventionEntity.toDomainModel(): Intervention {
    return Intervention(
        id = id,
        assetId = assetId,
        interventionType = runCatching { InterventionType.valueOf(interventionType) }.getOrDefault(InterventionType.INSPECTION),
        title = title,
        description = description,
        status = runCatching { InterventionStatus.valueOf(status) }.getOrDefault(InterventionStatus.PROPOSED),
        plannedAtUtc = plannedAtUtc,
        startedAtUtc = startedAtUtc,
        completedAtUtc = completedAtUtc,
        recordedBy = recordedBy,
        createdAtUtc = createdAtUtc,
        updatedAtUtc = updatedAtUtc,
        syncStatus = runCatching { SyncStatus.valueOf(syncStatus) }.getOrDefault(SyncStatus.LOCAL_ONLY)
    )
}

fun Intervention.toEntity(): InterventionEntity {
    return InterventionEntity(
        id = id,
        assetId = assetId,
        interventionType = interventionType.name,
        title = title,
        description = description,
        status = status.name,
        plannedAtUtc = plannedAtUtc,
        startedAtUtc = startedAtUtc,
        completedAtUtc = completedAtUtc,
        recordedBy = recordedBy,
        createdAtUtc = createdAtUtc,
        updatedAtUtc = updatedAtUtc,
        syncStatus = syncStatus.name
    )
}
