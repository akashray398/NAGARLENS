package com.nagarlens.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nagarlens.app.core.model.Evidence
import com.nagarlens.app.core.model.EvidenceStatus
import com.nagarlens.app.core.model.MediaType
import com.nagarlens.app.core.model.SyncStatus

@Entity(tableName = "evidence_records")
data class EvidenceEntity(
    @PrimaryKey val id: String,
    val mediaUri: String,
    val mediaType: String,
    val originalFileName: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val captureTimestampUtc: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationAccuracyMeters: Float? = null,
    val locationTimestampUtc: Long? = null,
    val sha256Hash: String,
    val status: String = EvidenceStatus.RECORDED.name,
    val syncStatus: String = SyncStatus.LOCAL_ONLY.name,
    val retryCount: Int = 0,
    val lastSyncError: String? = null,
    val createdTimestampUtc: Long = System.currentTimeMillis(),
    val updatedTimestampUtc: Long = System.currentTimeMillis()
)

fun EvidenceEntity.toDomainModel(): Evidence {
    return Evidence(
        id = id,
        mediaUri = mediaUri,
        mediaType = runCatching { MediaType.valueOf(mediaType) }.getOrDefault(MediaType.PHOTO),
        originalFileName = originalFileName,
        mimeType = mimeType,
        fileSizeBytes = fileSizeBytes,
        captureTimestampUtc = captureTimestampUtc,
        latitude = latitude,
        longitude = longitude,
        locationAccuracyMeters = locationAccuracyMeters,
        locationTimestampUtc = locationTimestampUtc,
        sha256Hash = sha256Hash,
        status = runCatching { EvidenceStatus.valueOf(status) }.getOrDefault(EvidenceStatus.RECORDED),
        syncStatus = runCatching { SyncStatus.valueOf(syncStatus) }.getOrDefault(SyncStatus.LOCAL_ONLY),
        retryCount = retryCount,
        lastSyncError = lastSyncError,
        createdTimestampUtc = createdTimestampUtc,
        updatedTimestampUtc = updatedTimestampUtc
    )
}

fun Evidence.toEntity(): EvidenceEntity {
    return EvidenceEntity(
        id = id,
        mediaUri = mediaUri,
        mediaType = mediaType.name,
        originalFileName = originalFileName,
        mimeType = mimeType,
        fileSizeBytes = fileSizeBytes,
        captureTimestampUtc = captureTimestampUtc,
        latitude = latitude,
        longitude = longitude,
        locationAccuracyMeters = locationAccuracyMeters,
        locationTimestampUtc = locationTimestampUtc,
        sha256Hash = sha256Hash,
        status = status.name,
        syncStatus = syncStatus.name,
        retryCount = retryCount,
        lastSyncError = lastSyncError,
        createdTimestampUtc = createdTimestampUtc,
        updatedTimestampUtc = updatedTimestampUtc
    )
}
