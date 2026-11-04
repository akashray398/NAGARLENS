package com.nagarlens.app

import com.nagarlens.app.core.integrity.Sha256Hasher
import com.nagarlens.app.core.model.Evidence
import com.nagarlens.app.core.model.MediaType
import com.nagarlens.app.core.model.SyncStatus
import com.nagarlens.app.core.network.DeferredEvidenceUploadClient
import com.nagarlens.app.core.network.UploadResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class EvidenceSyncWorkerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val sha256Hasher = Sha256Hasher()
    private val deferredClient = DeferredEvidenceUploadClient(sha256Hasher)

    @Test
    fun `sync status transitions validate permitted and rejected state paths`() {
        // Valid transitions
        assertTrue(SyncStatus.LOCAL_ONLY.canTransitionTo(SyncStatus.QUEUED))
        assertTrue(SyncStatus.QUEUED.canTransitionTo(SyncStatus.UPLOADING))
        assertTrue(SyncStatus.UPLOADING.canTransitionTo(SyncStatus.SYNCED))
        assertTrue(SyncStatus.UPLOADING.canTransitionTo(SyncStatus.RETRYABLE_FAILURE))
        assertTrue(SyncStatus.RETRYABLE_FAILURE.canTransitionTo(SyncStatus.QUEUED))

        // Any state can transition to PERMANENT_FAILURE on file corruption/loss
        assertTrue(SyncStatus.LOCAL_ONLY.canTransitionTo(SyncStatus.PERMANENT_FAILURE))
        assertTrue(SyncStatus.QUEUED.canTransitionTo(SyncStatus.PERMANENT_FAILURE))

        // Invalid transitions
        assertFalse(SyncStatus.SYNCED.canTransitionTo(SyncStatus.UPLOADING))
        assertFalse(SyncStatus.PERMANENT_FAILURE.canTransitionTo(SyncStatus.QUEUED))
    }

    @Test
    fun `deferred upload client returns BackendNotConfigured and never marks evidence SYNCED`() = runTest {
        val mediaFile = tempFolder.newFile("test_media.jpg")
        mediaFile.writeBytes("NAGARLENS_ORIGINAL_BINARY_MEDIA".toByteArray())

        val hash = sha256Hasher.calculateFileSha256(mediaFile)

        val evidence = Evidence(
            id = "EVID-001",
            mediaUri = mediaFile.toURI().toString(),
            mediaType = MediaType.PHOTO,
            originalFileName = mediaFile.name,
            mimeType = "image/jpeg",
            fileSizeBytes = mediaFile.length(),
            captureTimestampUtc = System.currentTimeMillis(),
            sha256Hash = hash,
            syncStatus = SyncStatus.QUEUED
        )

        val result = deferredClient.uploadEvidence(evidence, mediaFile)

        assertTrue(result is UploadResult.BackendNotConfigured)
        val notice = (result as UploadResult.BackendNotConfigured).message
        assertTrue(notice.contains("not configured"))
    }

    @Test
    fun `deferred upload client fails when original file is missing or tampered`() = runTest {
        val mediaFile = tempFolder.newFile("tampered_media.jpg")
        mediaFile.writeBytes("ORIGINAL_BYTES".toByteArray())

        val evidence = Evidence(
            id = "EVID-002",
            mediaUri = mediaFile.toURI().toString(),
            mediaType = MediaType.PHOTO,
            originalFileName = mediaFile.name,
            mimeType = "image/jpeg",
            fileSizeBytes = mediaFile.length(),
            captureTimestampUtc = System.currentTimeMillis(),
            sha256Hash = "0000000000000000000000000000000000000000000000000000000000000000",
            syncStatus = SyncStatus.QUEUED
        )

        val result = deferredClient.uploadEvidence(evidence, mediaFile)

        assertTrue(result is UploadResult.Failure)
        val failure = result as UploadResult.Failure
        assertFalse(failure.isRetryable)
        assertTrue(failure.errorMessage.contains("hash integrity check failed"))
    }
}
