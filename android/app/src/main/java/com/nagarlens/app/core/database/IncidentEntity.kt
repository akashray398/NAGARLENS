package com.nagarlens.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.Incident
import com.nagarlens.app.core.model.IncidentPriority
import com.nagarlens.app.core.model.IncidentStatus

@Entity(tableName = "incidents_records")
data class IncidentEntity(
    @PrimaryKey val id: String,
    val assetId: String,
    val assetCode: String,
    val title: String,
    val description: String? = null,
    val status: String = IncidentStatus.OPEN.name,
    val priority: String = IncidentPriority.HIGH.name,
    val defectCategory: String = DefectCategory.DRAIN_BLOCKAGE.name,
    val reportedBy: String = "field_inspector",
    val createdTimestampUtc: Long = System.currentTimeMillis(),
    val updatedTimestampUtc: Long = System.currentTimeMillis()
)

fun IncidentEntity.toDomainModel(): Incident {
    return Incident(
        id = id,
        assetId = assetId,
        assetCode = assetCode,
        title = title,
        description = description,
        status = runCatching { IncidentStatus.valueOf(status) }.getOrDefault(IncidentStatus.OPEN),
        priority = runCatching { IncidentPriority.valueOf(priority) }.getOrDefault(IncidentPriority.HIGH),
        defectCategory = runCatching { DefectCategory.valueOf(defectCategory) }.getOrDefault(DefectCategory.DRAIN_BLOCKAGE),
        reportedBy = reportedBy,
        createdTimestampUtc = createdTimestampUtc,
        updatedTimestampUtc = updatedTimestampUtc
    )
}

fun Incident.toEntity(): IncidentEntity {
    return IncidentEntity(
        id = id,
        assetId = assetId,
        assetCode = assetCode,
        title = title,
        description = description,
        status = status.name,
        priority = priority.name,
        defectCategory = defectCategory.name,
        reportedBy = reportedBy,
        createdTimestampUtc = createdTimestampUtc,
        updatedTimestampUtc = updatedTimestampUtc
    )
}
