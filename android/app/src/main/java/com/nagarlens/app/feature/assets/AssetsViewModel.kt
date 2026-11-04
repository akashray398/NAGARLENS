package com.nagarlens.app.feature.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.model.Asset
import com.nagarlens.app.core.model.AssetCategory
import com.nagarlens.app.core.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssetsUiState(
    val searchQuery: String = "",
    val selectedCategoryFilter: String = "All",
    val assets: List<Asset> = emptyList(),
    val selectedAsset: Asset? = null,
    val isRegistrationDialogOpen: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class AssetsViewModel @Inject constructor(
    private val assetRepository: AssetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetsUiState())
    val uiState: StateFlow<AssetsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            assetRepository.getAllAssets().collect { list ->
                _uiState.value = _uiState.value.copy(assets = list)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectCategoryFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = filter)
    }

    fun selectAsset(asset: Asset?) {
        _uiState.value = _uiState.value.copy(selectedAsset = asset)
    }

    fun openRegistrationDialog() {
        _uiState.value = _uiState.value.copy(isRegistrationDialogOpen = true)
    }

    fun closeRegistrationDialog() {
        _uiState.value = _uiState.value.copy(isRegistrationDialogOpen = false)
    }

    fun registerAsset(
        category: AssetCategory,
        lat: Double,
        lon: Double,
        description: String?
    ) {
        viewModelScope.launch {
            val res = assetRepository.registerAsset(
                category = category,
                latitude = lat,
                longitude = lon,
                description = description
            )
            res.onSuccess { asset ->
                _uiState.value = _uiState.value.copy(
                    isRegistrationDialogOpen = false,
                    message = "Asset ${asset.assetCode} successfully registered."
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    message = "Registration failed: ${err.message}"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
