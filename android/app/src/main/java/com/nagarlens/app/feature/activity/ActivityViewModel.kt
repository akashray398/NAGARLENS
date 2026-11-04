package com.nagarlens.app.feature.activity

import androidx.lifecycle.ViewModel
import com.nagarlens.app.core.designsystem.component.AssetConditionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ObservationActivityItem(
    val id: String,
    val assetCode: String,
    val timestamp: String,
    val status: AssetConditionStatus,
    val locationText: String
)

data class ActivityUiState(
    val activeFilter: String = "All Activity",
    val filterOptions: List<String> = listOf("All Activity", "Observations", "Interventions", "Verified"),
    val recentActivities: List<ObservationActivityItem> = listOf(
        ObservationActivityItem("OBS-104", "DRAIN-CB-104", "10 minutes ago", AssetConditionStatus.CRITICAL_BLOCKAGE, "Ward 12 (12.9716°N, 77.5946°E)"),
        ObservationActivityItem("OBS-103", "DRAIN-MH-082", "2 hours ago", AssetConditionStatus.PARTIAL_SILTATION, "Ward 12 (12.9702°N, 77.5921°E)"),
        ObservationActivityItem("OBS-102", "DRAIN-CB-091", "Yesterday", AssetConditionStatus.NORMAL_FLOW, "Ward 12 (12.9688°N, 77.5910°E)")
    )
)

@HiltViewModel
class ActivityViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    fun selectFilter(filter: String) {
        _uiState.value = _uiState.value.copy(activeFilter = filter)
    }
}
