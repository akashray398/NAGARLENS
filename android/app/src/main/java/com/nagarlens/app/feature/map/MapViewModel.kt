package com.nagarlens.app.feature.map

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class MapUiState(
    val activeWard: String = "Ward 12 — Central Catchment",
    val activeFilters: List<String> = listOf("Catch Basins", "Drain Lines", "Manholes", "Hotspots"),
    val selectedFilter: String = "All Assets",
    val mockAssetCount: Int = 142,
    val isGisOverlayActive: Boolean = true
)

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun selectFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }
}
