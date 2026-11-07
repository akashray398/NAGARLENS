package com.nagarlens.app.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.model.Evidence
import com.nagarlens.app.core.model.InvestigationCase
import com.nagarlens.app.core.model.InvestigationStatus
import com.nagarlens.app.core.repository.EvidenceRepository
import com.nagarlens.app.core.repository.InvestigationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityUiState(
    val activeFilter: String = "All Records",
    val filterOptions: List<String> = listOf("All Records", "Queued / Local", "Synced", "Failures"),
    val evidenceList: List<Evidence> = emptyList(),
    val investigationsList: List<InvestigationCase> = emptyList(),
    val selectedInvestigation: InvestigationCase? = null,
    val actionMessage: String? = null
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val evidenceRepository: EvidenceRepository,
    private val investigationRepository: InvestigationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            evidenceRepository.auditAndRecoverOrphanFiles()
        }
        viewModelScope.launch {
            evidenceRepository.getAllEvidence().collectLatest { records ->
                _uiState.update { it.copy(evidenceList = records) }
            }
        }
        viewModelScope.launch {
            investigationRepository.observeInvestigations().collectLatest { cases ->
                _uiState.update { it.copy(investigationsList = cases) }
            }
        }
    }

    fun selectFilter(filter: String) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun selectInvestigation(invCase: InvestigationCase?) {
        _uiState.update { it.copy(selectedInvestigation = invCase) }
    }

    fun advanceInvestigationStatus(id: String, targetStatus: InvestigationStatus) {
        viewModelScope.launch {
            investigationRepository.updateInvestigationStatus(id, targetStatus, "Advanced via Mobile Command Center").fold(
                onSuccess = { updated ->
                    _uiState.update { it.copy(selectedInvestigation = updated, actionMessage = "Investigation status advanced to ${targetStatus.name}.") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(actionMessage = "Status update failed: ${error.message}") }
                }
            )
        }
    }

    fun requestEvidence(investigationId: String, assetId: String, instructions: String) {
        viewModelScope.launch {
            investigationRepository.requestEvidence(investigationId, assetId, "PHOTO_CLOSEUP_INVERT", instructions).fold(
                onSuccess = {
                    _uiState.update { it.copy(actionMessage = "Evidence request issued to field inspector.") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(actionMessage = "Request failed: ${error.message}") }
                }
            )
        }
    }

    fun retrySync(evidenceId: String) {
        viewModelScope.launch {
            evidenceRepository.retrySync(evidenceId).fold(
                onSuccess = {
                    _uiState.update { it.copy(actionMessage = "Enqueued retry for evidence record.") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(actionMessage = "Retry failed: ${error.message}") }
                }
            )
        }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }
}
