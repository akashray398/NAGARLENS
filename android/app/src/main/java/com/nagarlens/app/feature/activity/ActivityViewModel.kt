package com.nagarlens.app.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.model.Evidence
import com.nagarlens.app.core.repository.EvidenceRepository
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
    val actionMessage: String? = null
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val evidenceRepository: EvidenceRepository
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
    }

    fun selectFilter(filter: String) {
        _uiState.update { it.copy(activeFilter = filter) }
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
