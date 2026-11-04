package com.nagarlens.app.core.network

import com.nagarlens.app.core.model.Evidence
import java.io.File

sealed interface UploadResult {
    data class Success(val remoteAssetId: String, val timestampUtc: Long) : UploadResult
    data class BackendNotConfigured(val message: String) : UploadResult
    data class Failure(val errorMessage: String, val isRetryable: Boolean) : UploadResult
}

interface EvidenceUploadClient {
    suspend fun uploadEvidence(evidence: Evidence, mediaFile: File): UploadResult
}
