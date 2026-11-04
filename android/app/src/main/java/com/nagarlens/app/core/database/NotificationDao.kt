package com.nagarlens.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM notifications_records ORDER BY timestampUtc DESC")
    fun observeAllNotifications(): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications_records SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications_records SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("SELECT COUNT(*) FROM notifications_records WHERE isRead = 0")
    fun observeUnreadCount(): Flow<Int>
}
