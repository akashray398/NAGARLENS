package com.nagarlens.app.feature.capture

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.camera.CameraLens
import com.nagarlens.app.core.camera.CameraState
import com.nagarlens.app.core.camera.CameraXController
import com.nagarlens.app.core.camera.FlashMode
import com.nagarlens.app.core.location.LocationData
import com.nagarlens.app.core.location.LocationProvider
import com.nagarlens.app.core.model.Evidence
import com.nagarlens.app.core.repository.EvidenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class CameraMode {
    PHOTO,
    VIDEO
}

enum class LocationState {
    DISABLED,
    CAPTURED,
    UNAVAILABLE,
    PERMISSION_DENIED
}

data class CaptureUiState(
    val cameraMode: CameraMode = CameraMode.PHOTO,
    val cameraState: CameraState = CameraState.Uninitialized,
    val flashMode: FlashMode = FlashMode.OFF,
    val cameraLens: CameraLens = CameraLens.BACK,
    val locationState: LocationState = LocationState.DISABLED,
    val isLocationTaggingEnabled: Boolean = false,
    val currentLocation: LocationData? = null,
    val isCameraPermissionGranted: Boolean = false,
    val isLocationPermissionGranted: Boolean = false,
    val isAudioPermissionGranted: Boolean = false,
    val audioNotice: String? = null,
    val capturedEvidence: Evidence? = null,
    val captureError: String? = null,
    val isProcessing: Boolean = false
)

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val cameraXController: CameraXController,
    private val locationProvider: LocationProvider,
    private val evidenceRepository: EvidenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            cameraXController.cameraState.collectLatest { state ->
                _uiState.update { it.copy(cameraState = state) }
            }
        }
        viewModelScope.launch {
            cameraXController.flashMode.collectLatest { mode ->
                _uiState.update { it.copy(flashMode = mode) }
            }
        }
        viewModelScope.launch {
            cameraXController.cameraLens.collectLatest { lens ->
                _uiState.update { it.copy(cameraLens = lens) }
            }
        }
    }

    fun onCameraPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(isCameraPermissionGranted = granted) }
    }

    fun onAudioPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                isAudioPermissionGranted = granted,
                audioNotice = if (!granted) "Audio unavailable. Recording silent video." else null
            )
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                isLocationPermissionGranted = granted,
                locationState = if (granted) LocationState.UNAVAILABLE else LocationState.PERMISSION_DENIED
            )
        }
        if (granted && _uiState.value.isLocationTaggingEnabled) {
            refreshLocation()
        }
    }

    fun toggleLocationTagging(enable: Boolean) {
        _uiState.update {
            it.copy(
                isLocationTaggingEnabled = enable,
                locationState = if (enable) {
                    if (it.isLocationPermissionGranted) LocationState.UNAVAILABLE else LocationState.PERMISSION_DENIED
                } else {
                    LocationState.DISABLED
                }
            )
        }
        if (enable && _uiState.value.isLocationPermissionGranted) {
            refreshLocation()
        }
    }

    fun refreshLocation() {
        if (!_uiState.value.isLocationTaggingEnabled || !_uiState.value.isLocationPermissionGranted) return
        viewModelScope.launch {
            val locationData = locationProvider.getCurrentLocation()
            if (locationData != null) {
                _uiState.update {
                    it.copy(
                        currentLocation = locationData,
                        locationState = LocationState.CAPTURED
                    )
                }
            } else {
                _uiState.update { it.copy(locationState = LocationState.UNAVAILABLE) }
            }
        }
    }

    fun bindCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        viewModelScope.launch {
            cameraXController.bindCamera(lifecycleOwner, previewView)
        }
    }

    fun setCameraMode(mode: CameraMode) {
        if (_uiState.value.cameraState is CameraState.Recording) {
            cameraXController.stopVideoRecording()
        }
        _uiState.update { it.copy(cameraMode = mode) }
    }

    fun toggleFlash() {
        cameraXController.toggleFlash()
    }

    fun toggleLens(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        cameraXController.toggleCameraLens(lifecycleOwner, previewView)
    }

    fun capturePhoto(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, captureError = null) }
            try {
                val tempPhotoFile = File(context.cacheDir, "temp_capture_${System.currentTimeMillis()}.jpg")
                cameraXController.capturePhoto(tempPhotoFile)

                if (_uiState.value.isLocationTaggingEnabled) {
                    refreshLocation()
                }
                val locationData = if (_uiState.value.isLocationTaggingEnabled) _uiState.value.currentLocation else null

                evidenceRepository.savePhotoEvidence(tempPhotoFile, locationData)
                    .collectLatest { result ->
                        result.fold(
                            onSuccess = { evidence ->
                                _uiState.update {
                                    it.copy(
                                        isProcessing = false,
                                        capturedEvidence = evidence,
                                        captureError = null
                                    )
                                }
                            },
                            onFailure = { error ->
                                _uiState.update {
                                    it.copy(
                                        isProcessing = false,
                                        captureError = "Failed to preserve photo evidence: ${error.message}"
                                    )
                                }
                            }
                        )
                    }
            } catch (exc: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        captureError = "Photo capture failed: ${exc.message}"
                    )
                }
            }
        }
    }

    fun startVideoRecording(context: Context, withAudio: Boolean) {
        val state = _uiState.value.cameraState
        if (state is CameraState.Recording) {
            cameraXController.stopVideoRecording()
        } else {
            val tempVideoFile = File(context.cacheDir, "temp_recording_${System.currentTimeMillis()}.mp4")
            if (_uiState.value.isLocationTaggingEnabled) {
                refreshLocation()
            }
            val locationData = if (_uiState.value.isLocationTaggingEnabled) _uiState.value.currentLocation else null

            cameraXController.startVideoRecording(
                outputFile = tempVideoFile,
                withAudio = withAudio,
                onRecordingError = { errorMsg ->
                    _uiState.update { it.copy(captureError = errorMsg) }
                }
            )

            // Listen for recording finalization to save evidence
            viewModelScope.launch {
                cameraXController.cameraState.collectLatest { camState ->
                    if (camState is CameraState.Ready && tempVideoFile.exists() && tempVideoFile.length() > 0) {
                        _uiState.update { it.copy(isProcessing = true) }
                        evidenceRepository.saveVideoEvidence(tempVideoFile, locationData)
                            .collectLatest { result ->
                                result.fold(
                                    onSuccess = { evidence ->
                                        _uiState.update {
                                            it.copy(
                                                isProcessing = false,
                                                capturedEvidence = evidence,
                                                captureError = null
                                            )
                                        }
                                    },
                                    onFailure = { error ->
                                        _uiState.update {
                                            it.copy(
                                                isProcessing = false,
                                                captureError = "Failed to preserve video evidence: ${error.message}"
                                            )
                                        }
                                    }
                                )
                            }
                    }
                }
            }
        }
    }

    fun stopVideoRecording() {
        cameraXController.stopVideoRecording()
    }

    fun dismissEvidenceDialog() {
        _uiState.update { it.copy(capturedEvidence = null, captureError = null) }
    }

    fun clearAudioNotice() {
        _uiState.update { it.copy(audioNotice = null) }
    }

    override fun onCleared() {
        super.onCleared()
        cameraXController.release()
    }
}
