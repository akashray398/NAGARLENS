package com.nagarlens.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intervention_status_history_records")
data class InterventionStatusHistoryEntity(
    @PrimaryKey val id: String,
    val interventionId: String,
    val previousStatus: String,
    val newStatus: String,
    val reason: String? = null,
    val actorId: String,
    val recordedAtUtc: Long = System.currentTimeMillis()
)
