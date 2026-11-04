package com.nagarlens.app.core.network

import com.nagarlens.app.core.integrity.Sha256Hasher
import com.nagarlens.app.core.model.Evidence
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeferredEvidenceUploadClient @Inject constructor(
    private val sha256Hasher: Sha256Hasher
) : EvidenceUploadClient {

    override suspend fun uploadEvidence(evidence: Evidence, mediaFile: File): UploadResult {
        if (!mediaFile.exists() || mediaFile.length() == 0L) {
            return UploadResult.Failure(
                errorMessage = "Original media file is missing or empty at path: ${mediaFile.absolutePath}",
                isRetryable = false
            )
        }

        if (!sha256Hasher.verifyFileHash(mediaFile, evidence.sha256Hash)) {
            return UploadResult.Failure(
                errorMessage = "SHA-256 hash integrity check failed for evidence ID: ${evidence.id}",
                isRetryable = false
            )
        }

        return UploadResult.BackendNotConfigured(
            message = "Backend upload endpoint is not configured in current milestone. Evidence remains safely persisted offline."
        )
    }
}
