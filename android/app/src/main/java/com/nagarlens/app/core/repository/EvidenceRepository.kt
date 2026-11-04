package com.nagarlens.app.core.repository

import com.nagarlens.app.core.location.LocationData
import com.nagarlens.app.core.model.Evidence
import com.nagarlens.app.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EvidenceRepository {
    fun savePhotoEvidence(tempPhotoFile: File, locationData: LocationData?): Flow<Result<Evidence>>
    fun saveVideoEvidence(tempVideoFile: File, locationData: LocationData?): Flow<Result<Evidence>>
    fun getAllEvidence(): Flow<List<Evidence>>
    fun getEvidenceById(id: String): Flow<Evidence?>
    fun getEvidenceBySyncStatus(status: SyncStatus): Flow<List<Evidence>>
    suspend fun updateSyncStatus(id: String, targetStatus: SyncStatus, errorMessage: String? = null)
    suspend fun queueForSync(id: String): Result<Unit>
    suspend fun retrySync(id: String): Result<Unit>
    suspend fun auditAndRecoverOrphanFiles(): Int
    suspend fun recoverStaleUploadingRecords(staleThresholdMs: Long = 30_000L): Int
}
