package com.nagarlens.app.core.ml

import com.nagarlens.app.core.integrity.Sha256Hasher
import com.nagarlens.app.core.model.AiAssessment
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.Evidence
import com.nagarlens.app.core.model.ImageQualityStatus
import com.nagarlens.app.core.model.ProcessingStatus
import com.nagarlens.app.core.model.TAXONOMY_VERSION
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnavailableDefectAnalyzer @Inject constructor(
    private val sha256Hasher: Sha256Hasher,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DefectAnalyzer {

    override fun getModelInfo(): ModelInfo {
        return ModelInfo(
            modelName = "MobileNetV3-Small-DrainDefect",
            modelVersion = "v1.0.0-foundation",
            taxonomyVersion = TAXONOMY_VERSION,
            isAvailable = false
        )
    }

    override suspend fun analyzeEvidence(evidence: Evidence, imageFile: File): AiAssessment = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()

        // 1. Verify image file existence
        if (!imageFile.exists() || imageFile.length() == 0L) {
            return@withContext AiAssessment(
                assessmentId = UUID.randomUUID().toString(),
                evidenceId = evidence.id,
                modelName = getModelInfo().modelName,
                modelVersion = getModelInfo().modelVersion,
                taxonomyVersion = TAXONOMY_VERSION,
                predictedCategory = DefectCategory.UNKNOWN_INSUFFICIENT,
                confidenceScore = 0.0f,
                imageQualityStatus = ImageQualityStatus.FAIL_CORRUPTED,
                processingStatus = ProcessingStatus.FAILED,
                message = "Image file is missing or corrupt at path: ${imageFile.absolutePath}",
                inferenceTimestampUtc = now
            )
        }

        // 2. Verify SHA-256 hash match against trusted stored digest
        if (!sha256Hasher.verifyFileHash(imageFile, evidence.sha256Hash)) {
            return@withContext AiAssessment(
                assessmentId = UUID.randomUUID().toString(),
                evidenceId = evidence.id,
                modelName = getModelInfo().modelName,
                modelVersion = getModelInfo().modelVersion,
                taxonomyVersion = TAXONOMY_VERSION,
                predictedCategory = DefectCategory.UNKNOWN_INSUFFICIENT,
                confidenceScore = 0.0f,
                imageQualityStatus = ImageQualityStatus.FAIL_CORRUPTED,
                processingStatus = ProcessingStatus.FAILED,
                message = "SHA-256 hash integrity check failed before AI processing.",
                inferenceTimestampUtc = now
            )
        }

        // 3. Return explicit MODEL_UNAVAILABLE result without fake predictions
        return@withContext AiAssessment(
            assessmentId = UUID.randomUUID().toString(),
            evidenceId = evidence.id,
            modelName = getModelInfo().modelName,
            modelVersion = getModelInfo().modelVersion,
            taxonomyVersion = TAXONOMY_VERSION,
            predictedCategory = DefectCategory.UNKNOWN_INSUFFICIENT,
            confidenceScore = 0.0f,
            imageQualityStatus = ImageQualityStatus.PASS,
            processingStatus = ProcessingStatus.MODEL_UNAVAILABLE,
            message = "No trained LiteRT model binary packaged. System ready for model artifact deployment.",
            inferenceTimestampUtc = now
        )
    }
}
