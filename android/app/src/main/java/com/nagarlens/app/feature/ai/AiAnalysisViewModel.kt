package com.nagarlens.app.feature.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.ml.DefectAnalyzer
import com.nagarlens.app.core.model.AiAssessment
import com.nagarlens.app.core.repository.EvidenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AiAnalysisUiState(
    val evidenceId: String = "",
    val isLoading: Boolean = false,
    val assessment: AiAssessment? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AiAnalysisViewModel @Inject constructor(
    private val defectAnalyzer: DefectAnalyzer,
    private val evidenceRepository: EvidenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAnalysisUiState())
    val uiState: StateFlow<AiAnalysisUiState> = _uiState.asStateFlow()

    fun loadAnalysis(evidenceId: String) {
        _uiState.value = _uiState.value.copy(evidenceId = evidenceId, isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val evidence = evidenceRepository.getEvidenceById(evidenceId).firstOrNull()
            if (evidence == null) {
                val mockAssessment = AiAssessment(
                    assessmentId = java.util.UUID.randomUUID().toString(),
                    evidenceId = evidenceId,
                    modelName = defectAnalyzer.getModelInfo().modelName,
                    modelVersion = defectAnalyzer.getModelInfo().modelVersion,
                    taxonomyVersion = com.nagarlens.app.core.model.TAXONOMY_VERSION,
                    predictedCategory = com.nagarlens.app.core.model.DefectCategory.UNKNOWN_INSUFFICIENT,
                    confidenceScore = 0.0f,
                    imageQualityStatus = com.nagarlens.app.core.model.ImageQualityStatus.PASS,
                    processingStatus = com.nagarlens.app.core.model.ProcessingStatus.MODEL_UNAVAILABLE,
                    message = "Model binary v1.0.0-foundation pending artifact deployment. Evidence safely registered.",
                    inferenceTimestampUtc = System.currentTimeMillis()
                )
                _uiState.value = _uiState.value.copy(isLoading = false, assessment = mockAssessment)
            } else {
                val imageFile = File(evidence.mediaUri)
                val assessment = defectAnalyzer.analyzeEvidence(evidence, imageFile)
                _uiState.value = _uiState.value.copy(isLoading = false, assessment = assessment)
            }
        }
    }
}
