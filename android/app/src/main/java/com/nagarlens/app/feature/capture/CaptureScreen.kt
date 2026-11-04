package com.nagarlens.app.feature.capture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.nagarlens.app.core.camera.CameraLens
import com.nagarlens.app.core.camera.CameraState
import com.nagarlens.app.core.camera.FlashMode
import com.nagarlens.app.core.designsystem.component.NagarlensCard
import com.nagarlens.app.core.designsystem.component.NagarlensPrimaryButton
import com.nagarlens.app.core.designsystem.theme.AlertRed
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.designsystem.theme.SafetyGreen
import com.nagarlens.app.core.designsystem.theme.SignalAmber
import com.nagarlens.app.core.model.Evidence
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    // 1. Contextual Camera Permission Launcher (Only CAMERA requested on launch)
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onCameraPermissionResult(isGranted)
    }

    // 2. Contextual Location Permission Launcher (Requested ONLY when user enables location tagging)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        viewModel.onLocationPermissionResult(locationGranted)
    }

    // 3. Contextual Audio Permission Launcher (Requested ONLY when starting video recording)
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onAudioPermissionResult(isGranted)
        viewModel.startVideoRecording(context, withAudio = isGranted)
    }

    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner, uiState.isCameraPermissionGranted) {
        if (uiState.isCameraPermissionGranted) {
            viewModel.bindCamera(lifecycleOwner, previewView)
        }
        onDispose {
            // Camera cleanup managed by ViewModel/CameraX lifecycle
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(NagarlensSpacing.medium)
    ) {
        // Real-Time GNSS Telemetry & Tagging Bar
        NagarlensCard(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GNSS Tagging",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    val locText = when (uiState.locationState) {
                        LocationState.CAPTURED -> "Precise ±${uiState.currentLocation?.accuracyMeters ?: 0f}m"
                        LocationState.UNAVAILABLE -> "GNSS Searching..."
                        LocationState.PERMISSION_DENIED -> "Location Denied"
                        LocationState.DISABLED -> "Tagging Disabled"
                    }
                    Text(
                        text = locText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.locationState == LocationState.CAPTURED) SafetyGreen else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Location Tagging Toggle Button
                TextButton(
                    onClick = {
                        val nextState = !uiState.isLocationTaggingEnabled
                        viewModel.toggleLocationTagging(nextState)
                        if (nextState && !uiState.isLocationPermissionGranted) {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                ) {
                    Text(
                        text = if (uiState.isLocationTaggingEnabled) "TAGGING: ON" else "ENABLE TAGGING",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(NagarlensSpacing.small))

        // Camera Controls Header (Flash / Lens Flip / Mode Tabs)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash Mode Selector
            TextButton(onClick = { viewModel.toggleFlash() }) {
                Text(
                    text = "FLASH: ${uiState.flashMode.name}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Photo / Video Mode Selector Tabs
            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(4.dp)
            ) {
                val isPhoto = uiState.cameraMode == CameraMode.PHOTO
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isPhoto) MaterialTheme.colorScheme.secondary else Color.Transparent,
                            shape = MaterialTheme.shapes.extraSmall
                        )
                        .clickable { viewModel.setCameraMode(CameraMode.PHOTO) }
                        .padding(horizontal = NagarlensSpacing.small, vertical = 6.dp)
                ) {
                    Text(
                        text = "PHOTO",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPhoto) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            color = if (!isPhoto) MaterialTheme.colorScheme.secondary else Color.Transparent,
                            shape = MaterialTheme.shapes.extraSmall
                        )
                        .clickable { viewModel.setCameraMode(CameraMode.VIDEO) }
                        .padding(horizontal = NagarlensSpacing.small, vertical = 6.dp)
                ) {
                    Text(
                        text = "VIDEO",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (!isPhoto) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Lens Switch Button
            TextButton(onClick = { viewModel.toggleLens(lifecycleOwner, previewView) }) {
                Text(
                    text = "LENS: ${uiState.cameraLens.name}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(NagarlensSpacing.small))

        // Camera Viewfinder Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = Color.Black,
                    shape = MaterialTheme.shapes.medium
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isCameraPermissionGranted) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "CAMERA PERMISSION REQUIRED\nTo acquire evidence, grant camera access.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(NagarlensSpacing.medium)
                )
            }

            // Viewfinder Grid Reticle Target Overlay
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CENTER DRAIN STRUCTURE\nIN RETICLE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }

            // Recording Timer Badge
            val camState = uiState.cameraState
            if (camState is CameraState.Recording) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = NagarlensSpacing.medium)
                        .background(
                            color = AlertRed,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = NagarlensSpacing.medium, vertical = 6.dp)
                ) {
                    val sec = camState.durationMs / 1000
                    Text(
                        text = "REC 00:%02d".format(sec),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Audio Denied Notice Banner
            uiState.audioNotice?.let { notice ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp)
                        .background(
                            color = SignalAmber,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = NagarlensSpacing.small, vertical = 4.dp)
                ) {
                    Text(
                        text = notice,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // SHA-256 Anti-Tamper Notice
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = NagarlensSpacing.small)
                    .background(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = NagarlensSpacing.small, vertical = 4.dp)
            ) {
                Text(
                    text = "SHA-256 Anti-Tamper Binding Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

        // Capture Shutter Control Row
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isProcessing) {
                CircularProgressIndicator(color = SignalAmber)
            } else {
                val isRecording = uiState.cameraState is CameraState.Recording
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(color = Color.White, shape = CircleShape)
                        .clickable {
                            if (uiState.cameraMode == CameraMode.PHOTO) {
                                viewModel.capturePhoto(context)
                            } else {
                                if (isRecording) {
                                    viewModel.stopVideoRecording()
                                } else {
                                    // Check RECORD_AUDIO permission contextually before starting video
                                    val audioGranted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (audioGranted) {
                                        viewModel.startVideoRecording(context, withAudio = true)
                                    } else {
                                        // Request RECORD_AUDIO contextually
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = if (uiState.cameraMode == CameraMode.VIDEO || isRecording) AlertRed else MaterialTheme.colorScheme.secondary,
                                shape = if (isRecording) MaterialTheme.shapes.small else CircleShape
                            )
                    )
                }
            }
        }
    }

    // Evidence Preserved Detail Dialog
    uiState.capturedEvidence?.let { evidence ->
        EvidencePreservedDialog(
            evidence = evidence,
            onDismiss = { viewModel.dismissEvidenceDialog() }
        )
    }

    // Capture Error Dialog
    uiState.captureError?.let { errorMsg ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissEvidenceDialog() },
            title = { Text("Evidence Capture Error") },
            text = { Text(errorMsg) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissEvidenceDialog() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun EvidencePreservedDialog(
    evidence: Evidence,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val formattedTimestamp = dateFormat.format(Date(evidence.captureTimestampUtc))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Original Evidence Preserved",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        },
        text = {
            Column {
                Text(
                    text = "Evidence ID: ${evidence.id}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                Text(text = "Media Type: ${evidence.mediaType.name}")
                Text(text = "File Name: ${evidence.originalFileName}")
                Text(text = "Size: %.2f KB".format(evidence.fileSizeBytes / 1024.0))
                Text(text = "Captured: $formattedTimestamp")
                Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                val locStr = if (evidence.latitude != null && evidence.longitude != null) {
                    "%.4f°N, %.4f°E (±%.1fm)".format(
                        evidence.latitude,
                        evidence.longitude,
                        evidence.locationAccuracyMeters ?: 0f
                    )
                } else {
                    "Location Tagging Disabled / Denied"
                }
                Text(text = "GNSS: $locStr", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                Text(
                    text = "SHA-256 Immutability Digest:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = evidence.sha256Hash,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            NagarlensPrimaryButton(onClick = onDismiss) {
                Text("DISMISS")
            }
        }
    )
}
