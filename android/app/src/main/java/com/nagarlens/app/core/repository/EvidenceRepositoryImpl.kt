package com.nagarlens.app.core.repository

import com.nagarlens.app.core.integrity.EvidenceFileStore
import com.nagarlens.app.core.integrity.Sha256Hasher
import com.nagarlens.app.core.location.LocationData
import com.nagarlens.app.core.model.Evidence
import com.nagarlens.app.core.model.EvidenceStatus
import com.nagarlens.app.core.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvidenceRepositoryImpl @Inject constructor(
    private val evidenceFileStore: EvidenceFileStore,
    private val sha256Hasher: Sha256Hasher
) : EvidenceRepository {

    private val _evidenceList = MutableStateFlow<List<Evidence>>(emptyList())

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

            // 3. Construct immutable Evidence domain entity
            val evidence = Evidence(
                id = evidenceId,
                mediaUri = storedFile.toURI().toString(),
                mediaType = MediaType.PHOTO,
                originalFileName = fileName,
                mimeType = "image/jpeg",
                fileSizeBytes = storedFile.length(),
                captureTimestampUtc = System.currentTimeMillis(),
                latitude = locationData?.latitude,
                longitude = locationData?.longitude,
                locationAccuracyMeters = locationData?.accuracyMeters,
                locationTimestampUtc = locationData?.timestampUtc,
                sha256Hash = sha256Hash,
                status = EvidenceStatus.RECORDED
            )

            // 4. Append to memory repository state
            _evidenceList.value = _evidenceList.value + evidence
            emit(Result.success(evidence))
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

            // 3. Construct immutable Evidence domain entity
            val evidence = Evidence(
                id = evidenceId,
                mediaUri = storedFile.toURI().toString(),
                mediaType = MediaType.VIDEO,
                originalFileName = fileName,
                mimeType = "video/mp4",
                fileSizeBytes = storedFile.length(),
                captureTimestampUtc = System.currentTimeMillis(),
                latitude = locationData?.latitude,
                longitude = locationData?.longitude,
                locationAccuracyMeters = locationData?.accuracyMeters,
                locationTimestampUtc = locationData?.timestampUtc,
                sha256Hash = sha256Hash,
                status = EvidenceStatus.RECORDED
            )

            // 4. Append to memory repository state
            _evidenceList.value = _evidenceList.value + evidence
            emit(Result.success(evidence))
        } catch (exc: Exception) {
            emit(Result.failure(exc))
        } finally {
            if (tempVideoFile.exists()) {
                tempVideoFile.delete()
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getAllEvidence(): Flow<List<Evidence>> = _evidenceList.asStateFlow()

    override fun getEvidenceById(id: String): Flow<Evidence?> = flow {
        emit(_evidenceList.value.find { it.id == id })
    }
}
