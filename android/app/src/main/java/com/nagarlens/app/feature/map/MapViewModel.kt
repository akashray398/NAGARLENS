package com.nagarlens.app.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.location.AssetCluster
import com.nagarlens.app.core.location.LocationData
import com.nagarlens.app.core.location.LocationProvider
import com.nagarlens.app.core.location.SpatialQueryEngine
import com.nagarlens.app.core.model.Asset
import com.nagarlens.app.core.model.AssetCategory
import com.nagarlens.app.core.model.AssetTimeline
import com.nagarlens.app.core.model.ConditionSeverity
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.DigitalTwinAsset
import com.nagarlens.app.core.model.InterventionType
import com.nagarlens.app.core.model.ObservationType
import com.nagarlens.app.core.model.SpatialBoundary
import com.nagarlens.app.core.model.VerificationDecision
import com.nagarlens.app.core.repository.AssetRepository
import com.nagarlens.app.core.repository.SpatialRepository
import com.nagarlens.app.core.repository.TemporalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val searchQuery: String = "",
    val selectedFilter: String = "All Categories",
    val activeFilters: List<String> = listOf("All Categories", "Catch Basins", "Storm Water Drains", "Manholes", "Culverts"),
    val activeWard: String = "Ward 12 — Bangalore Central",
    val assets: List<Asset> = emptyList(),
    val spatialClusters: List<AssetCluster> = emptyList(),
    val spatialBoundaries: List<SpatialBoundary> = emptyList(),
    val selectedAsset: Asset? = null,
    val selectedDigitalTwin: DigitalTwinAsset? = null,
    val selectedAssetTimeline: AssetTimeline? = null,
    val isRegistrationDialogOpen: Boolean = false,
    val isObservationDialogOpen: Boolean = false,
    val isInterventionDialogOpen: Boolean = false,
    val userLocation: LocationData? = null,
    val uiMessage: String? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val assetRepository: AssetRepository,
    private val spatialRepository: SpatialRepository,
    private val temporalRepository: TemporalRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            assetRepository.getAllAssets().collectLatest { list ->
                _uiState.update { it.copy(assets = list) }
                val clusters = spatialRepository.getSpatialClusters(radiusMeters = 50.0)
                _uiState.update { it.copy(spatialClusters = clusters) }
            }
        }
        viewModelScope.launch {
            spatialRepository.observeSpatialBoundaries().collectLatest { boundaries ->
                _uiState.update { it.copy(spatialBoundaries = boundaries) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun selectAsset(asset: Asset?) {
        _uiState.update { it.copy(selectedAsset = asset, selectedAssetTimeline = null, selectedDigitalTwin = null) }
        if (asset != null) {
            viewModelScope.launch {
                val loc = _uiState.value.userLocation
                val digitalTwin = spatialRepository.getDigitalTwinAsset(asset.id, loc?.latitude, loc?.longitude)
                _uiState.update { it.copy(selectedDigitalTwin = digitalTwin) }

                temporalRepository.getAssetTimeline(asset.id, asset.assetCode).collectLatest { timeline ->
                    _uiState.update { it.copy(selectedAssetTimeline = timeline) }
                }
            }
        }
    }

    fun openRegistrationDialog() {
        _uiState.update { it.copy(isRegistrationDialogOpen = true) }
        refreshUserLocation()
    }

    fun closeRegistrationDialog() {
        _uiState.update { it.copy(isRegistrationDialogOpen = false) }
    }

    fun openObservationDialog() {
        _uiState.update { it.copy(isObservationDialogOpen = true) }
    }

    fun closeObservationDialog() {
        _uiState.update { it.copy(isObservationDialogOpen = false) }
    }

    fun openInterventionDialog() {
        _uiState.update { it.copy(isInterventionDialogOpen = true) }
    }

    fun closeInterventionDialog() {
        _uiState.update { it.copy(isInterventionDialogOpen = false) }
    }

    fun refreshUserLocation() {
        viewModelScope.launch {
            val loc = locationProvider.getCurrentLocation()
            _uiState.update { it.copy(userLocation = loc) }
        }
    }

    fun registerAsset(
        category: AssetCategory,
        latitude: Double,
        longitude: Double,
        description: String?
    ) {
        viewModelScope.launch {
            assetRepository.registerAsset(
                category = category,
                latitude = latitude,
                longitude = longitude,
                description = description
            ).fold(
                onSuccess = { newAsset ->
                    _uiState.update {
                        it.copy(
                            isRegistrationDialogOpen = false,
                            selectedAsset = newAsset,
                            uiMessage = "Asset ${newAsset.assetCode} registered successfully."
                        )
                    }
                    selectAsset(newAsset)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(uiMessage = "Registration failed: ${error.message}") }
                }
            )
        }
    }

    fun recordObservation(
        type: ObservationType,
        category: DefectCategory,
        severity: ConditionSeverity,
        description: String?
    ) {
        val currentAsset = _uiState.value.selectedAsset ?: return
        viewModelScope.launch {
            temporalRepository.recordObservation(
                assetId = currentAsset.id,
                type = type,
                category = category,
                severity = severity,
                description = description,
                latitude = currentAsset.latitude,
                longitude = currentAsset.longitude
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isObservationDialogOpen = false,
                            uiMessage = "Observation recorded for ${currentAsset.assetCode}."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(uiMessage = "Observation failed: ${error.message}") }
                }
            )
        }
    }

    fun recordIntervention(
        type: InterventionType,
        title: String,
        description: String?
    ) {
        val currentAsset = _uiState.value.selectedAsset ?: return
        viewModelScope.launch {
            temporalRepository.recordIntervention(
                assetId = currentAsset.id,
                type = type,
                title = title,
                description = description
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isInterventionDialogOpen = false,
                            uiMessage = "Intervention '$title' recorded."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(uiMessage = "Intervention failed: ${error.message}") }
                }
            )
        }
    }

    fun submitVerification(assetId: String, decision: VerificationDecision) {
        viewModelScope.launch {
            assetRepository.submitVerification(
                assetId = assetId,
                decision = decision,
                reviewerId = "municipal_reviewer"
            ).fold(
                onSuccess = { updatedAsset ->
                    _uiState.update {
                        it.copy(
                            selectedAsset = updatedAsset,
                            uiMessage = "Verification decision recorded: ${decision.name}"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(uiMessage = "Verification failed: ${error.message}") }
                }
            )
        }
    }

    fun clearUiMessage() {
        _uiState.update { it.copy(uiMessage = null) }
    }
}
