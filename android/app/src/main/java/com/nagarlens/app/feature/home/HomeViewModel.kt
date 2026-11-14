package com.nagarlens.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.model.AssetCategory
import com.nagarlens.app.core.model.AuthState
import com.nagarlens.app.core.model.EarlyWarningAlert
import com.nagarlens.app.core.model.RiskLevel
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.repository.AnalyticsRepository
import com.nagarlens.app.core.repository.AuthRepository
import com.nagarlens.app.core.repository.PredictiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CityMarkerNode(
    val id: String,
    val title: String,
    val category: AssetCategory,
    val relativeX: Float, // 0.0 to 1.0 on canvas
    val relativeY: Float, // 0.0 to 1.0 on canvas
    val conditionScorePercent: Int,
    val riskLevel: RiskLevel,
    val description: String
)

data class CityInsightEvent(
    val id: String,
    val timestampText: String,
    val title: String,
    val categoryText: String,
    val locationText: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val authRepository: AuthRepository,
    private val predictiveRepository: PredictiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val sampleNodes = listOf(
        CityMarkerNode(
            id = "NODE-CB-104",
            title = "Catch Basin NAG-CB-000104",
            category = AssetCategory.CATCH_BASIN,
            relativeX = 0.50f,
            relativeY = 0.55f,
            conditionScorePercent = 62,
            riskLevel = RiskLevel.HIGH,
            description = "High silt accumulation detected at channel invert."
        ),
        CityMarkerNode(
            id = "NODE-SWD-201",
            title = "Stormwater Drain NAG-SWD-000201",
            category = AssetCategory.STORM_WATER_DRAIN,
            relativeX = 0.28f,
            relativeY = 0.38f,
            conditionScorePercent = 95,
            riskLevel = RiskLevel.LOW,
            description = "Flow capacity nominal; 100% open cross-section."
        ),
        CityMarkerNode(
            id = "NODE-MH-052",
            title = "Manhole NAG-MH-000052",
            category = AssetCategory.MANHOLE,
            relativeX = 0.72f,
            relativeY = 0.42f,
            conditionScorePercent = 88,
            riskLevel = RiskLevel.LOW,
            description = "Heavy-duty grate cover secure; zero structural deflection."
        ),
        CityMarkerNode(
            id = "NODE-CVT-012",
            title = "Culvert NAG-CVT-000012",
            category = AssetCategory.CULVERT,
            relativeX = 0.82f,
            relativeY = 0.68f,
            conditionScorePercent = 74,
            riskLevel = RiskLevel.MODERATE,
            description = "Minor vegetation intrusion near inlet barrel."
        )
    )

    private val sampleInsights = listOf(
        CityInsightEvent(
            id = "INS-01",
            timestampText = "2 min ago",
            title = "Desilting Intervention Proposed",
            categoryText = "DRAINAGE",
            locationText = "Central Catchment Ward 12"
        ),
        CityInsightEvent(
            id = "INS-02",
            timestampText = "14 min ago",
            title = "AI Defect Analysis Verified",
            categoryText = "SOLID_WASTE",
            locationText = "Grate Invert Node #104"
        ),
        CityInsightEvent(
            id = "INS-03",
            timestampText = "1 hour ago",
            title = "GNSS Telemetry Lock Confirmed",
            categoryText = "GEOSPATIAL",
            locationText = "Bangalore Central Grid"
        )
    )

    init {
        _uiState.update {
            it.copy(
                cityNodes = sampleNodes,
                recentInsights = sampleInsights
            )
        }

        viewModelScope.launch {
            analyticsRepository.observeMetrics().collectLatest { m ->
                _uiState.update { it.copy(metrics = m) }
            }
        }

        viewModelScope.launch {
            predictiveRepository.observeActiveWarnings(RiskLevel.MODERATE).collectLatest { warnings ->
                _uiState.update { it.copy(activeWarnings = warnings) }
            }
        }

        viewModelScope.launch {
            authRepository.authState.collectLatest { auth ->
                if (auth is AuthState.Authenticated) {
                    _uiState.update {
                        it.copy(
                            userName = auth.session.fullName,
                            activeWard = auth.session.assignedWard
                        )
                    }
                }
            }
        }
    }

    fun selectCityMarkerNode(node: CityMarkerNode?) {
        _uiState.update { it.copy(selectedMarkerNode = node) }
    }
}
