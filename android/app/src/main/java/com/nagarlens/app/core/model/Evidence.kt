package com.nagarlens.app.core.model

enum class MediaType {
    PHOTO,
    VIDEO
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
    val createdTimestampUtc: Long = System.currentTimeMillis()
)
