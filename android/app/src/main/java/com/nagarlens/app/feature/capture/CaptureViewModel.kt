package com.nagarlens.app.feature.capture

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class CaptureUiState(
    val gpsAccuracy: String = "Precise ±1.8m",
    val sensorBearing: String = "184° S",
    val altitude: String = "920.4m MSL",
    val cryptoHashNotice: String = "SHA-256 Anti-Tamper Binding Active",
    val isCameraPermissionGranted: Boolean = true
)

@HiltViewModel
class CaptureViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()
}
