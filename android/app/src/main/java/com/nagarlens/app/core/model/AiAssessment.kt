package com.nagarlens.app.core.model

enum class ProcessingStatus {
    COMPLETED,
    MODEL_UNAVAILABLE,
    INSUFFICIENT_EVIDENCE,
    FAILED
}

enum class ImageQualityStatus {
    PASS,
    FAIL_CORRUPTED,
    FAIL_LOW_RESOLUTION,
    FAIL_DARK,
    FAIL_BLURRY
}

data class BoundingBox(
    val category: DefectCategory,
    val confidence: Float,
    val xMin: Float,
    val yMin: Float,
    val xMax: Float,
    val yMax: Float
)

data class AiAssessment(
    val assessmentId: String,
    val evidenceId: String,
    val modelName: String,
    val modelVersion: String,
    val taxonomyVersion: String = TAXONOMY_VERSION,
    val predictedCategory: DefectCategory,
    val confidenceScore: Float,
    val boundingBoxes: List<BoundingBox> = emptyList(),
    val imageQualityStatus: ImageQualityStatus = ImageQualityStatus.PASS,
    val processingStatus: ProcessingStatus = ProcessingStatus.COMPLETED,
    val message: String = "",
    val inferenceTimestampUtc: Long = System.currentTimeMillis()
)
