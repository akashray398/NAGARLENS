package com.nagarlens.app.core.ml

import com.nagarlens.app.core.model.AiAssessment
import com.nagarlens.app.core.model.Evidence
import java.io.File

data class ModelInfo(
    val modelName: String,
    val modelVersion: String,
    val taxonomyVersion: String,
    val isAvailable: Boolean
)

interface DefectAnalyzer {
    suspend fun analyzeEvidence(evidence: Evidence, imageFile: File): AiAssessment
    fun getModelInfo(): ModelInfo
}
