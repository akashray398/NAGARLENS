package com.nagarlens.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nagarlens.app.core.model.NotificationCategory
import com.nagarlens.app.core.model.NotificationItem
import com.nagarlens.app.core.model.NotificationSeverity

@Entity(tableName = "notifications_records")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val category: String,
    val severity: String,
    val relatedAssetId: String? = null,
    val relatedIncidentId: String? = null,
    val isRead: Boolean = false,
    val timestampUtc: Long = System.currentTimeMillis()
)

fun NotificationEntity.toDomainModel(): NotificationItem {
    return NotificationItem(
        id = id,
        title = title,
        message = message,
        category = runCatching { NotificationCategory.valueOf(category) }.getOrDefault(NotificationCategory.SYSTEM_NOTICE),
        severity = runCatching { NotificationSeverity.valueOf(severity) }.getOrDefault(NotificationSeverity.INFO),
        relatedAssetId = relatedAssetId,
        relatedIncidentId = relatedIncidentId,
        isRead = isRead,
        timestampUtc = timestampUtc
    )
}

fun NotificationItem.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        title = title,
        message = message,
        category = category.name,
        severity = severity.name,
        relatedAssetId = relatedAssetId,
        relatedIncidentId = relatedIncidentId,
        isRead = isRead,
        timestampUtc = timestampUtc
    )
}
