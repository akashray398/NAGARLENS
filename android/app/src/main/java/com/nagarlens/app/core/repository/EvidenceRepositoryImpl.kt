package com.nagarlens.app.core.repository

import com.nagarlens.app.core.database.EvidenceDao
import com.nagarlens.app.core.database.toDomainModel
import com.nagarlens.app.core.database.toEntity
import com.nagarlens.app.core.integrity.EvidenceFileStore
import com.nagarlens.app.core.integrity.Sha256Hasher
import com.nagarlens.app.core.location.LocationData
import com.nagarlens.app.core.model.Evidence
import com.nagarlens.app.core.model.EvidenceStatus
import com.nagarlens.app.core.model.MediaType
import com.nagarlens.app.core.model.SyncStatus
import com.nagarlens.app.core.sync.EvidenceSyncScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvidenceRepositoryImpl @Inject constructor(
    private val evidenceDao: EvidenceDao,
    private val evidenceFileStore: EvidenceFileStore,
    private val sha256Hasher: Sha256Hasher,
    private val evidenceSyncScheduler: EvidenceSyncScheduler
) : EvidenceRepository {

    override fun savePhotoEvidence(
        tempPhotoFile: File,
        locationData: LocationData?
    ): Flow<Result<Evidence>> = flow {
        try {
            val evidenceId = UUID.randomUUID().toString()
            val fileName = "EVID_PHOTO_${evidenceId}.jpg"

            // 1. Preserve original media in app-private store
            val storedFile = evidenceFileStore.storeOriginalEvidence(tempPhotoFile, fileName)

            // 2. Compute SHA-256 content hash on completed original file
            val sha256Hash = sha256Hasher.calculateFileSha256(storedFile)

            val now = System.currentTimeMillis()

            // 3. Construct immutable Evidence domain entity
            val initialEvidence = Evidence(
                id = evidenceId,
                mediaUri = storedFile.toURI().toString(),
                mediaType = MediaType.PHOTO,
                originalFileName = fileName,
                mimeType = "image/jpeg",
                fileSizeBytes = storedFile.length(),
                captureTimestampUtc = now,
                latitude = locationData?.latitude,
                longitude = locationData?.longitude,
                locationAccuracyMeters = locationData?.accuracyMeters,
                locationTimestampUtc = locationData?.timestampUtc,
                sha256Hash = sha256Hash,
                status = EvidenceStatus.RECORDED,
                syncStatus = SyncStatus.LOCAL_ONLY,
                createdTimestampUtc = now,
                updatedTimestampUtc = now
            )

            // 4. Persist metadata in Room database
            evidenceDao.insertEvidence(initialEvidence.toEntity())

            // 5. Enqueue background synchronization
            runCatching { evidenceSyncScheduler.scheduleSync(evidenceId) }
            val queuedEvidence = initialEvidence.copy(syncStatus = SyncStatus.QUEUED, updatedTimestampUtc = System.currentTimeMillis())
            evidenceDao.updateSyncStatus(evidenceId, SyncStatus.QUEUED.name, queuedEvidence.updatedTimestampUtc)

            emit(Result.success(queuedEvidence))
        } catch (exc: Exception) {
            emit(Result.failure(exc))
        } finally {
            if (tempPhotoFile.exists()) {
                tempPhotoFile.delete()
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun saveVideoEvidence(
        tempVideoFile: File,
        locationData: LocationData?
    ): Flow<Result<Evidence>> = flow {
        try {
            val evidenceId = UUID.randomUUID().toString()
            val fileName = "EVID_VIDEO_${evidenceId}.mp4"

            // 1. Preserve original media in app-private store
            val storedFile = evidenceFileStore.storeOriginalEvidence(tempVideoFile, fileName)

            // 2. Compute SHA-256 content hash on completed original file
            val sha256Hash = sha256Hasher.calculateFileSha256(storedFile)

            val now = System.currentTimeMillis()

            // 3. Construct immutable Evidence domain entity
            val initialEvidence = Evidence(
                id = evidenceId,
                mediaUri = storedFile.toURI().toString(),
                mediaType = MediaType.VIDEO,
                originalFileName = fileName,
                mimeType = "video/mp4",
                fileSizeBytes = storedFile.length(),
                captureTimestampUtc = now,
                latitude = locationData?.latitude,
                longitude = locationData?.longitude,
                locationAccuracyMeters = locationData?.accuracyMeters,
                locationTimestampUtc = locationData?.timestampUtc,
                sha256Hash = sha256Hash,
                status = EvidenceStatus.RECORDED,
                syncStatus = SyncStatus.LOCAL_ONLY,
                createdTimestampUtc = now,
                updatedTimestampUtc = now
            )

            // 4. Persist metadata in Room database
            evidenceDao.insertEvidence(initialEvidence.toEntity())

            // 5. Enqueue background synchronization
            runCatching { evidenceSyncScheduler.scheduleSync(evidenceId) }
            val queuedEvidence = initialEvidence.copy(syncStatus = SyncStatus.QUEUED, updatedTimestampUtc = System.currentTimeMillis())
            evidenceDao.updateSyncStatus(evidenceId, SyncStatus.QUEUED.name, queuedEvidence.updatedTimestampUtc)

            emit(Result.success(queuedEvidence))
        } catch (exc: Exception) {
            emit(Result.failure(exc))
        } finally {
            if (tempVideoFile.exists()) {
                tempVideoFile.delete()
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getAllEvidence(): Flow<List<Evidence>> {
        return evidenceDao.observeAllEvidence().map { list ->
            list.map { entity ->
                val domainModel = entity.toDomainModel()
                val file = evidenceFileStore.getEvidenceFile(domainModel.originalFileName)
                if (file == null || !file.exists() || file.length() == 0L) {
                    domainModel.copy(status = EvidenceStatus.CORRUPTED)
                } else {
                    domainModel
                }
            }
        }
    }

    override fun getEvidenceById(id: String): Flow<Evidence?> {
        return evidenceDao.observeEvidenceById(id).map { entity ->
            entity?.toDomainModel()?.let { domainModel ->
                val file = evidenceFileStore.getEvidenceFile(domainModel.originalFileName)
                if (file == null || !file.exists() || file.length() == 0L) {
                    domainModel.copy(status = EvidenceStatus.CORRUPTED)
                } else {
                    domainModel
                }
            }
        }
    }

    override fun getEvidenceBySyncStatus(status: SyncStatus): Flow<List<Evidence>> {
        return evidenceDao.observeEvidenceBySyncStatus(status.name).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun updateSyncStatus(id: String, targetStatus: SyncStatus, errorMessage: String?) {
        val existing = evidenceDao.getEvidenceById(id) ?: return
        val currentSyncStatus = runCatching { SyncStatus.valueOf(existing.syncStatus) }.getOrDefault(SyncStatus.LOCAL_ONLY)

        require(currentSyncStatus.canTransitionTo(targetStatus)) {
            "Invalid state transition requested for evidence $id: $currentSyncStatus -> $targetStatus"
        }

        val now = System.currentTimeMillis()
        if (errorMessage != null) {
            evidenceDao.recordSyncFailure(id, targetStatus.name, errorMessage, now)
        } else {
            evidenceDao.updateSyncStatus(id, targetStatus.name, now)
        }
    }

    override suspend fun queueForSync(id: String): Result<Unit> {
        return try {
            updateSyncStatus(id, SyncStatus.QUEUED)
            evidenceSyncScheduler.scheduleSync(id)
            Result.success(Unit)
        } catch (exc: Exception) {
            Result.failure(exc)
        }
    }

    override suspend fun retrySync(id: String): Result<Unit> {
        val existing = evidenceDao.getEvidenceById(id) ?: return Result.failure(IllegalArgumentException("Evidence record $id not found"))
        val currentStatus = runCatching { SyncStatus.valueOf(existing.syncStatus) }.getOrDefault(SyncStatus.LOCAL_ONLY)

        if (currentStatus != SyncStatus.RETRYABLE_FAILURE && currentStatus != SyncStatus.LOCAL_ONLY) {
            return Result.failure(IllegalStateException("Evidence $id is in status $currentStatus and cannot be retried."))
        }

        return try {
            updateSyncStatus(id, SyncStatus.QUEUED)
            evidenceSyncScheduler.forceRetrySync(id)
            Result.success(Unit)
        } catch (exc: Exception) {
            Result.failure(exc)
        }
    }

    override suspend fun auditAndRecoverOrphanFiles(): Int {
        var recoveredCount = 0

        // 1. Recover interrupted stale UPLOADING records
        recoveredCount += recoverStaleUploadingRecords()

        // 2. Discover unindexed files in private storage
        val evidenceFile = evidenceFileStore.getEvidenceFile("dummy.jpg")
        val evidenceDir = evidenceFile?.parentFile ?: return recoveredCount
        if (!evidenceDir.exists()) return recoveredCount

        evidenceDir.listFiles()?.forEach { file ->
            if (file.isFile && file.length() > 0 && file.name.startsWith("EVID_")) {
                val fileName = file.name
                val isPhoto = fileName.startsWith("EVID_PHOTO_")
                val isVideo = fileName.startsWith("EVID_VIDEO_")

                if (isPhoto || isVideo) {
                    val idFromFileName = fileName.substringAfter("EVID_PHOTO_").substringAfter("EVID_VIDEO_").substringBefore(".")
                    val existingInDb = evidenceDao.getEvidenceById(idFromFileName)

                    if (existingInDb == null) {
                        val sha256Hash = sha256Hasher.calculateFileSha256(file)
                        val now = file.lastModified()
                        val recoveredEntity = Evidence(
                            id = idFromFileName,
                            mediaUri = file.toURI().toString(),
                            mediaType = if (isPhoto) MediaType.PHOTO else MediaType.VIDEO,
                            originalFileName = fileName,
                            mimeType = if (isPhoto) "image/jpeg" else "video/mp4",
                            fileSizeBytes = file.length(),
                            captureTimestampUtc = now,
                            sha256Hash = sha256Hash,
                            status = EvidenceStatus.RECORDED,
                            syncStatus = SyncStatus.LOCAL_ONLY,
                            createdTimestampUtc = now,
                            updatedTimestampUtc = now
                        ).toEntity()

                        evidenceDao.insertEvidence(recoveredEntity)
                        recoveredCount++
                    }
                }
            }
        }
        return recoveredCount
    }

    override suspend fun recoverStaleUploadingRecords(staleThresholdMs: Long): Int {
        var recovered = 0
        val cutoff = System.currentTimeMillis() - staleThresholdMs
        val staleUploadingRecords = evidenceDao.getStaleRecordsBySyncStatus(SyncStatus.UPLOADING.name, cutoff)

        for (record in staleUploadingRecords) {
            val file = evidenceFileStore.getEvidenceFile(record.originalFileName)
            if (file == null || !file.exists() || file.length() == 0L) {
                evidenceDao.recordSyncFailure(
                    id = record.id,
                    syncStatus = SyncStatus.PERMANENT_FAILURE.name,
                    error = "Original evidence media file is missing or empty.",
                    updatedAt = System.currentTimeMillis()
                )
            } else if (!sha256Hasher.verifyFileHash(file, record.sha256Hash)) {
                evidenceDao.recordSyncFailure(
                    id = record.id,
                    syncStatus = SyncStatus.PERMANENT_FAILURE.name,
                    error = "SHA-256 hash integrity check failed for evidence.",
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                evidenceDao.recordSyncFailure(
                    id = record.id,
                    syncStatus = SyncStatus.RETRYABLE_FAILURE.name,
                    error = "Sync interrupted during execution; ready for retry.",
                    updatedAt = System.currentTimeMillis()
                )
            }
            recovered++
        }
        return recovered
    }
}
