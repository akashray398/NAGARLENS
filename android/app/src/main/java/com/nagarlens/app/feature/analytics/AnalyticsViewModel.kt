package com.nagarlens.app.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.model.ConditionDistribution
import com.nagarlens.app.core.model.DefectDistribution
import com.nagarlens.app.core.model.InfrastructureMetrics
import com.nagarlens.app.core.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val metrics: InfrastructureMetrics? = null,
    val conditionDist: List<ConditionDistribution> = emptyList(),
    val defectDist: List<DefectDistribution> = emptyList(),
    val selectedTimeRange: String = "30 Days"
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            analyticsRepository.observeMetrics().collect { m ->
                _uiState.value = _uiState.value.copy(metrics = m)
            }
        }
        viewModelScope.launch {
            analyticsRepository.observeConditionDistribution().collect { list ->
                _uiState.value = _uiState.value.copy(conditionDist = list)
            }
        }
        viewModelScope.launch {
            analyticsRepository.observeDefectDistribution().collect { list ->
                _uiState.value = _uiState.value.copy(defectDist = list)
            }
        }
    }

    fun setTimeRange(range: String) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = range)
    }
}
