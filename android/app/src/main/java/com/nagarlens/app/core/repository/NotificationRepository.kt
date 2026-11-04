package com.nagarlens.app.core.repository

import com.nagarlens.app.core.model.NotificationItem
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeAllNotifications(): Flow<List<NotificationItem>>
    fun observeUnreadCount(): Flow<Int>
    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
}
