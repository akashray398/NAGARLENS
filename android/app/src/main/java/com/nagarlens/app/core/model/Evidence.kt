package com.nagarlens.app.core.model

enum class MediaType {
    PHOTO,
    VIDEO
}

enum class SyncStatus {
    LOCAL_ONLY,
    QUEUED,
    UPLOADING,
    SYNCED,
    RETRYABLE_FAILURE,
    PERMANENT_FAILURE;

    fun canTransitionTo(target: SyncStatus): Boolean {
        if (this == target) return true
        if (target == PERMANENT_FAILURE) return true
        return when (this) {
            LOCAL_ONLY -> target == QUEUED
            QUEUED -> target == UPLOADING || target == RETRYABLE_FAILURE
            UPLOADING -> target == SYNCED || target == RETRYABLE_FAILURE || target == PERMANENT_FAILURE
            SYNCED -> false
            RETRYABLE_FAILURE -> target == QUEUED || target == UPLOADING
            PERMANENT_FAILURE -> false
        }
    }
}

enum class EvidenceStatus {
    RECORDED,
    VERIFIED,
    CORRUPTED,
    PENDING_HASH
}

data class Evidence(
    val id: String,
    val mediaUri: String,
    val mediaType: MediaType,
    val originalFileName: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val captureTimestampUtc: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationAccuracyMeters: Float? = null,
    val locationTimestampUtc: Long? = null,
    val sha256Hash: String,
    val status: EvidenceStatus = EvidenceStatus.RECORDED,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val retryCount: Int = 0,
    val lastSyncError: String? = null,
    val createdTimestampUtc: Long = System.currentTimeMillis(),
    val updatedTimestampUtc: Long = System.currentTimeMillis()
)
