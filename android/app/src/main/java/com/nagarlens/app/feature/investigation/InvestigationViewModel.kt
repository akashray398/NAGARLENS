package com.nagarlens.app.feature.investigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.model.InvestigationCase
import com.nagarlens.app.core.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvestigationUiState(
    val incidentId: String = "",
    val isLoading: Boolean = false,
    val investigationCase: InvestigationCase? = null
)

@HiltViewModel
class InvestigationViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvestigationUiState())
    val uiState: StateFlow<InvestigationUiState> = _uiState.asStateFlow()

    fun loadInvestigation(incidentId: String) {
        _uiState.value = _uiState.value.copy(incidentId = incidentId, isLoading = true)
        viewModelScope.launch {
            val invCase = incidentRepository.getInvestigationCaseForIncident(incidentId)
            _uiState.value = _uiState.value.copy(isLoading = false, investigationCase = invCase)
        }
    }
}
