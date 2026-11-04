package com.nagarlens.app.core.model

enum class NotificationCategory {
    CRITICAL_ALERT,
    INCIDENT_UPDATE,
    AI_ASSESSMENT_READY,
    SYNC_COMPLETE,
    SYSTEM_NOTICE
}

enum class NotificationSeverity {
    CRITICAL,
    WARNING,
    INFO
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val category: NotificationCategory,
    val severity: NotificationSeverity,
    val relatedAssetId: String? = null,
    val relatedIncidentId: String? = null,
    val isRead: Boolean = false,
    val timestampUtc: Long = System.currentTimeMillis()
)
