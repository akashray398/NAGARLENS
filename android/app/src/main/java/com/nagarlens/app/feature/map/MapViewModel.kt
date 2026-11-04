package com.nagarlens.app.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.location.LocationData
import com.nagarlens.app.core.location.LocationProvider
import com.nagarlens.app.core.model.Asset
import com.nagarlens.app.core.model.AssetCategory
import com.nagarlens.app.core.model.AssetVerificationStatus
import com.nagarlens.app.core.model.VerificationDecision
import com.nagarlens.app.core.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val selectedFilter: String = "All Categories",
    val activeFilters: List<String> = listOf("All Categories", "Catch Basins", "Storm Water Drains", "Manholes", "Culverts"),
    val activeWard: String = "Ward 12 — Bangalore Central",
    val assets: List<Asset> = emptyList(),
    val selectedAsset: Asset? = null,
    val isRegistrationDialogOpen: Boolean = false,
    val userLocation: LocationData? = null,
    val uiMessage: String? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val assetRepository: AssetRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            assetRepository.getAllAssets().collectLatest { list ->
                _uiState.update { it.copy(assets = list) }
            }
        }
    }

    fun selectFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun selectAsset(asset: Asset?) {
        _uiState.update { it.copy(selectedAsset = asset) }
    }

    fun openRegistrationDialog() {
        _uiState.update { it.copy(isRegistrationDialogOpen = true) }
        refreshUserLocation()
    }

    fun closeRegistrationDialog() {
        _uiState.update { it.copy(isRegistrationDialogOpen = false) }
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
                },
                onFailure = { error ->
                    _uiState.update { it.copy(uiMessage = "Registration failed: ${error.message}") }
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
