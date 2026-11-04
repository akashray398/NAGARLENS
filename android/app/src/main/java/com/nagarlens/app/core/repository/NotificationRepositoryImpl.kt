package com.nagarlens.app.core.repository

import com.nagarlens.app.core.database.NotificationDao
import com.nagarlens.app.core.database.toDomainModel
import com.nagarlens.app.core.database.toEntity
import com.nagarlens.app.core.model.NotificationCategory
import com.nagarlens.app.core.model.NotificationItem
import com.nagarlens.app.core.model.NotificationSeverity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override fun observeAllNotifications(): Flow<List<NotificationItem>> {
        return notificationDao.observeAllNotifications().map { entities ->
            if (entities.isEmpty()) {
                val seed = getSeedNotifications()
                notificationDao.insertNotifications(seed.map { it.toEntity() })
                seed
            } else {
                entities.map { it.toDomainModel() }
            }
        }
    }

    override fun observeUnreadCount(): Flow<Int> {
        return notificationDao.observeUnreadCount()
    }

    override suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    private fun getSeedNotifications(): List<NotificationItem> {
        val now = System.currentTimeMillis()
        return listOf(
            NotificationItem(
                id = "NOTIF-101",
                title = "Critical Silt Overflow Warning",
                message = "Catch basin NAG-CB-000001 exceeded 80% blockage threshold in Ward 12.",
                category = NotificationCategory.CRITICAL_ALERT,
                severity = NotificationSeverity.CRITICAL,
                relatedAssetId = "511a2c72-86f0-4c1a-9509-09c928410811",
                timestampUtc = now - (1000 * 60 * 15)
            ),
            NotificationItem(
                id = "NOTIF-102",
                title = "Evidence Sync Completed",
                message = "3 field evidence photos successfully synchronized with central backend.",
                category = NotificationCategory.SYNC_COMPLETE,
                severity = NotificationSeverity.INFO,
                timestampUtc = now - (1000 * 60 * 45)
            ),
            NotificationItem(
                id = "NOTIF-103",
                title = "Work Order Dispatched",
                message = "Intervention work order desilting assigned for NAG-SWD-000104.",
                category = NotificationCategory.INCIDENT_UPDATE,
                severity = NotificationSeverity.WARNING,
                relatedIncidentId = "INC-10002",
                timestampUtc = now - (1000 * 60 * 120)
            )
        )
    }
}
