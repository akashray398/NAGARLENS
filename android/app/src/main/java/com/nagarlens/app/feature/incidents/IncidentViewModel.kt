package com.nagarlens.app.feature.incidents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.model.Incident
import com.nagarlens.app.core.model.IncidentStatus
import com.nagarlens.app.core.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IncidentUiState(
    val incidents: List<Incident> = emptyList(),
    val selectedStatusFilter: String = "All",
    val selectedIncident: Incident? = null,
    val actionMessage: String? = null
)

@HiltViewModel
class IncidentViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncidentUiState())
    val uiState: StateFlow<IncidentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            incidentRepository.observeAllIncidents().collect { list ->
                _uiState.value = _uiState.value.copy(incidents = list)
            }
        }
    }

    fun selectStatusFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedStatusFilter = filter)
    }

    fun selectIncident(incident: Incident?) {
        _uiState.value = _uiState.value.copy(selectedIncident = incident)
    }

    fun updateStatus(incidentId: String, newStatus: IncidentStatus) {
        viewModelScope.launch {
            incidentRepository.updateIncidentStatus(incidentId, newStatus)
            _uiState.value = _uiState.value.copy(actionMessage = "Incident status updated to ${newStatus.name}.")
        }
    }

    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null)
    }
}
