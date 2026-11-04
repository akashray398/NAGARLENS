package com.nagarlens.app.core.camera

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

enum class FlashMode {
    OFF,
    ON,
    AUTO
}

enum class CameraLens {
    BACK,
    FRONT
}

sealed interface CameraState {
    data object Uninitialized : CameraState
    data object Ready : CameraState
    data class Recording(val durationMs: Long) : CameraState
    data class Error(val message: String) : CameraState
}

@Singleton
open class CameraXController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Uninitialized)
    open val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private val _flashMode = MutableStateFlow(FlashMode.OFF)
    open val flashMode: StateFlow<FlashMode> = _flashMode.asStateFlow()

    private val _cameraLens = MutableStateFlow(CameraLens.BACK)
    open val cameraLens: StateFlow<CameraLens> = _cameraLens.asStateFlow()

    open suspend fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val provider = getCameraProvider()
        cameraProvider = provider

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder()
            .setFlashMode(
                when (_flashMode.value) {
                    FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
                    FlashMode.ON -> ImageCapture.FLASH_MODE_ON
                    FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
                }
            )
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val recorder = Recorder.Builder().build()
        videoCapture = VideoCapture.withOutput(recorder)

        val cameraSelector = if (_cameraLens.value == CameraLens.BACK) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        try {
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
            )
            _cameraState.value = CameraState.Ready
        } catch (exc: Exception) {
            _cameraState.value = CameraState.Error("Failed to bind CameraX lifecycle: ${exc.message}")
        }
    }

    open suspend fun capturePhoto(outputFile: File): File = suspendCoroutine { continuation ->
        val capture = imageCapture
        if (capture == null) {
            continuation.resumeWithException(IllegalStateException("Camera is not bound or image capture is unavailable."))
            return@suspendCoroutine
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    continuation.resume(outputFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    continuation.resumeWithException(exception)
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    open fun startVideoRecording(
        outputFile: File,
        withAudio: Boolean = true,
        onRecordingError: (String) -> Unit
    ) {
        val capture = videoCapture ?: run {
            onRecordingError("Video capture is not bound.")
            return
        }

        val outputOptions = FileOutputOptions.Builder(outputFile).build()

        var pendingRecording = capture.output.prepareRecording(context, outputOptions)
        if (withAudio) {
            pendingRecording = pendingRecording.withAudioEnabled()
        }

        activeRecording = pendingRecording
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        _cameraState.value = CameraState.Recording(0)
                    }
                    is VideoRecordEvent.Status -> {
                        _cameraState.value = CameraState.Recording(recordEvent.recordingStats.recordedDurationNanos / 1_000_000)
                    }
                    is VideoRecordEvent.Finalize -> {
                        _cameraState.value = CameraState.Ready
                        if (recordEvent.hasError()) {
                            onRecordingError("Video recording failed: ${recordEvent.error}")
                        }
                    }
                }
            }
    }

    open fun stopVideoRecording() {
        activeRecording?.stop()
        activeRecording = null
        _cameraState.value = CameraState.Ready
    }

    open fun toggleFlash() {
        val newMode = when (_flashMode.value) {
            FlashMode.OFF -> FlashMode.ON
            FlashMode.ON -> FlashMode.AUTO
            FlashMode.AUTO -> FlashMode.OFF
        }
        _flashMode.value = newMode
        imageCapture?.flashMode = when (newMode) {
            FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
            FlashMode.ON -> ImageCapture.FLASH_MODE_ON
            FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
        }
    }

    open fun toggleCameraLens(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        _cameraLens.value = if (_cameraLens.value == CameraLens.BACK) CameraLens.FRONT else CameraLens.BACK
        // Re-bind camera with new selector
        val provider = cameraProvider ?: return
        provider.unbindAll()
        val cameraSelector = if (_cameraLens.value == CameraLens.BACK) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        try {
            provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture, videoCapture)
        } catch (exc: Exception) {
            _cameraState.value = CameraState.Error("Failed to switch camera lens: ${exc.message}")
        }
    }

    private suspend fun getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                continuation.resume(cameraProviderFuture.get())
            } catch (exc: Exception) {
                continuation.resumeWithException(exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    open fun release() {
        cameraProvider?.unbindAll()
        _cameraState.value = CameraState.Uninitialized
    }
}
