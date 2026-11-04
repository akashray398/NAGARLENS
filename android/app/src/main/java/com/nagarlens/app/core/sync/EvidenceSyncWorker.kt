package com.nagarlens.app.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nagarlens.app.core.integrity.EvidenceFileStore
import com.nagarlens.app.core.network.EvidenceUploadClient
import com.nagarlens.app.core.network.UploadResult
import com.nagarlens.app.core.model.SyncStatus
import com.nagarlens.app.core.repository.EvidenceRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull

class EvidenceSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncWorkerEntryPoint {
        fun evidenceRepository(): EvidenceRepository
        fun evidenceFileStore(): EvidenceFileStore
        fun evidenceUploadClient(): EvidenceUploadClient
    }

    private val entryPoint: SyncWorkerEntryPoint by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            SyncWorkerEntryPoint::class.java
        )
    }

    override suspend fun doWork(): Result {
        val evidenceId = inputData.getString(KEY_EVIDENCE_ID) ?: return Result.failure()

        val repository = entryPoint.evidenceRepository()
        val fileStore = entryPoint.evidenceFileStore()
        val uploadClient = entryPoint.evidenceUploadClient()

        val evidence = repository.getEvidenceById(evidenceId).firstOrNull() ?: return Result.failure()

        if (evidence.syncStatus == SyncStatus.PERMANENT_FAILURE || evidence.syncStatus == SyncStatus.SYNCED) {
            return Result.failure()
        }

        // Validate state transition
        if (!evidence.syncStatus.canTransitionTo(SyncStatus.UPLOADING)) {
            return Result.failure()
        }

        // Verify media file existence
        val mediaFile = fileStore.getEvidenceFile(evidence.originalFileName)
        if (mediaFile == null || !mediaFile.exists() || mediaFile.length() == 0L) {
            repository.updateSyncStatus(
                id = evidenceId,
                targetStatus = SyncStatus.PERMANENT_FAILURE,
                errorMessage = "Original evidence media file is missing or corrupted."
            )
            return Result.failure(workDataOf(KEY_ERROR_MESSAGE to "Missing original file"))
        }

        // Transition to UPLOADING
        repository.updateSyncStatus(evidenceId, SyncStatus.UPLOADING)

        return when (val uploadResult = uploadClient.uploadEvidence(evidence, mediaFile)) {
            is UploadResult.Success -> {
                repository.updateSyncStatus(evidenceId, SyncStatus.SYNCED)
                Result.success()
            }
            is UploadResult.BackendNotConfigured -> {
                repository.updateSyncStatus(
                    id = evidenceId,
                    targetStatus = SyncStatus.RETRYABLE_FAILURE,
                    errorMessage = uploadResult.message
                )
                Result.success(workDataOf(KEY_ERROR_MESSAGE to uploadResult.message))
            }
            is UploadResult.Failure -> {
                val nextStatus = if (uploadResult.isRetryable) SyncStatus.RETRYABLE_FAILURE else SyncStatus.PERMANENT_FAILURE
                repository.updateSyncStatus(
                    id = evidenceId,
                    targetStatus = nextStatus,
                    errorMessage = uploadResult.errorMessage
                )
                if (uploadResult.isRetryable) Result.retry() else Result.failure(workDataOf(KEY_ERROR_MESSAGE to uploadResult.errorMessage))
            }
        }
    }

    companion object {
        const val KEY_EVIDENCE_ID = "KEY_EVIDENCE_ID"
        const val KEY_ERROR_MESSAGE = "KEY_ERROR_MESSAGE"
    }
}
