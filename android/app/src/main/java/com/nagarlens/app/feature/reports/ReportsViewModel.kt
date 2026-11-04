package com.nagarlens.app.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.model.InfrastructureReport
import com.nagarlens.app.core.model.ReportFormat
import com.nagarlens.app.core.model.ReportType
import com.nagarlens.app.core.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportsUiState(
    val reports: List<InfrastructureReport> = emptyList(),
    val actionMessage: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            reportRepository.observeAllReports().collect { list ->
                _uiState.value = _uiState.value.copy(reports = list)
            }
        }
    }

    fun generateReport(type: ReportType, format: ReportFormat) {
        viewModelScope.launch {
            val res = reportRepository.generateReport(type, format)
            res.onSuccess { r ->
                _uiState.value = _uiState.value.copy(actionMessage = "Report '${r.title}' generated successfully.")
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(actionMessage = "Generation failed: ${err.message}")
            }
        }
    }

    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null)
    }
}
