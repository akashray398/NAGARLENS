package com.nagarlens.app.feature.map

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nagarlens.app.core.designsystem.component.NagarlensCard
import com.nagarlens.app.core.designsystem.component.NagarlensPrimaryButton
import com.nagarlens.app.core.designsystem.component.NagarlensSecondaryButton
import com.nagarlens.app.core.designsystem.component.NagarlensSectionHeader
import com.nagarlens.app.core.designsystem.theme.AlertRed
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.designsystem.theme.SafetyGreen
import com.nagarlens.app.core.designsystem.theme.SignalAmber
import com.nagarlens.app.core.model.Asset
import com.nagarlens.app.core.model.AssetCategory
import com.nagarlens.app.core.model.AssetVerificationStatus
import com.nagarlens.app.core.model.ConditionSeverity
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.InterventionType
import com.nagarlens.app.core.model.ObservationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// GIS Map Theme Colors
private val MapDarkBackground = Color(0xFF0B1017)
private val CyanAccent = Color(0xFF00F0FF)
private val ElectricBlue = Color(0xFF00B0FF)
private val HydraulicFlowBlue = Color(0xFF00E5FF)

enum class MapLayerMode {
    SATELLITE_GIS,
    SPATIAL_LIST
}

@Composable
fun MapScreen(
    onNavigateToCapture: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var layerMode by remember { mutableStateOf(MapLayerMode.SATELLITE_GIS) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    val filteredAssets = uiState.assets.filter { asset ->
        val matchesCategory = when (uiState.selectedFilter) {
            "Catch Basins" -> asset.assetCategory == AssetCategory.CATCH_BASIN
            "Storm Water Drains" -> asset.assetCategory == AssetCategory.STORM_WATER_DRAIN
            "Manholes" -> asset.assetCategory == AssetCategory.MANHOLE
            "Culverts" -> asset.assetCategory == AssetCategory.CULVERT
            else -> true
        }
        val matchesSearch = uiState.searchQuery.isBlank() ||
                asset.assetCode.contains(uiState.searchQuery, ignoreCase = true) ||
                asset.description?.contains(uiState.searchQuery, ignoreCase = true) == true
        matchesCategory && matchesSearch
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm 'UTC'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MapDarkBackground)
            .padding(NagarlensSpacing.medium)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NagarlensSectionHeader(title = "Living Infrastructure Map & Spatial Twin")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.activeWard,
                    style = MaterialTheme.typography.labelMedium,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold
                )

                // 2-Way Map Layer Selector Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = layerMode == MapLayerMode.SATELLITE_GIS,
                        onClick = { layerMode = MapLayerMode.SATELLITE_GIS },
                        label = { Text("GIS MAP", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = layerMode == MapLayerMode.SPATIAL_LIST,
                        onClick = { layerMode = MapLayerMode.SPATIAL_LIST },
                        label = { Text("LIST", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))

            // Spatial Search input
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Search Map by Asset Code or Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanAccent) }
            )

            Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.activeFilters) { filter ->
                    FilterChip(
                        selected = uiState.selectedFilter == filter,
                        onClick = { viewModel.selectFilter(filter) },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))

            // Living Interactive GIS Canvas Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(color = MapDarkBackground, shape = RoundedCornerShape(12.dp))
                    .border(width = 1.5.dp, color = Color(0xFF1E3A5F), shape = RoundedCornerShape(12.dp))
            ) {
                if (layerMode == MapLayerMode.SATELLITE_GIS) {
                    HyperRealisticGisMapCanvas(
                        assets = filteredAssets,
                        selectedAsset = uiState.selectedAsset,
                        userLat = uiState.userLocation?.latitude ?: 12.9716,
                        userLon = uiState.userLocation?.longitude ?: 77.5946,
                        zoomScale = zoomScale,
                        onAssetSelected = { viewModel.selectAsset(it) }
                    )

                    // Zoom & GPS Map Controls Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(NagarlensSpacing.small),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FloatingActionButton(
                            onClick = { zoomScale = (zoomScale * 1.3f).coerceAtMost(4.0f) },
                            modifier = Modifier.size(38.dp),
                            containerColor = Color(0xFF1E2A3A),
                            contentColor = Color.White
                        ) {
                            Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        FloatingActionButton(
                            onClick = { zoomScale = (zoomScale / 1.3f).coerceAtLeast(0.5f) },
                            modifier = Modifier.size(38.dp),
                            containerColor = Color(0xFF1E2A3A),
                            contentColor = Color.White
                        ) {
                            Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        FloatingActionButton(
                            onClick = {
                                zoomScale = 1.0f
                                viewModel.refreshUserLocation()
                            },
                            modifier = Modifier.size(38.dp),
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White
                        ) {
                            Text("GPS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }

                    // Map Status HUD Footer
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(NagarlensSpacing.small)
                            .background(Color.Black.copy(alpha = 0.75f), shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(SafetyGreen, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HYDRAULIC FLOW ACTIVE • ZOOM %.1fx • %d ASSETS ON CANVAS".format(zoomScale, filteredAssets.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Spatial List View Mode
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(NagarlensSpacing.medium)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CATCHMENT VIEWPORT: 12.960°-12.985°N, 77.580°-77.610°E",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${filteredAssets.size} Assets",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SafetyGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(NagarlensSpacing.small))

                        if (filteredAssets.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No assets registered in this viewport region.\nTap + REGISTER to create a new spatial asset.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredAssets) { asset ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = if (uiState.selectedAsset?.id == asset.id) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF1E2A3A),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { viewModel.selectAsset(asset) }
                                            .padding(NagarlensSpacing.small),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val markerColor = when (asset.verificationStatus) {
                                                AssetVerificationStatus.VERIFIED -> SafetyGreen
                                                AssetVerificationStatus.REJECTED -> AlertRed
                                                else -> SignalAmber
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .background(color = markerColor, shape = CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(NagarlensSpacing.small))
                                            Column {
                                                Text(
                                                    text = asset.assetCode,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "${asset.assetCategory.name} • %.4f°N, %.4f°E".format(asset.latitude, asset.longitude),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.LightGray
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = Color.Black.copy(alpha = 0.5f),
                                                    shape = MaterialTheme.shapes.extraSmall
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = asset.verificationStatus.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Asset Digital Twin Memory Drawer Card
            uiState.selectedAsset?.let { asset ->
                Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                NagarlensCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Digital Twin: ${asset.assetCode}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { viewModel.selectAsset(null) }) {
                                Text("CLOSE", color = Color.Gray)
                            }
                        }
                        Text(text = "Category: ${asset.assetCategory.name} • Status: ${asset.verificationStatus.name}")
                        Text(text = "WGS84: %.6f°N, %.6f°E".format(asset.latitude, asset.longitude), style = MaterialTheme.typography.bodySmall)

                        uiState.selectedDigitalTwin?.let { twin ->
                            twin.distanceToUserMeters?.let { dist ->
                                Text(
                                    text = "Spatial Distance to Inspector: %.1fm (Heading: %.0f°)".format(dist, twin.bearingFromUserDegrees ?: 0.0),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SafetyGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                        Text(
                            text = "Chronological Event Timeline:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )

                        uiState.selectedAssetTimeline?.let { timeline ->
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .padding(vertical = NagarlensSpacing.extraSmall)
                            ) {
                                items(timeline.events) { event ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "• ${event.title}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            event.description?.let { desc ->
                                                Text(
                                                    text = desc,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Text(
                                            text = dateFormat.format(Date(event.timestampUtc)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.extraSmall)
                        ) {
                            NagarlensPrimaryButton(
                                onClick = { viewModel.openObservationDialog() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+ OBSERVE")
                            }
                            NagarlensPrimaryButton(
                                onClick = { viewModel.openInterventionDialog() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+ INTERVENE")
                            }
                            TextButton(
                                onClick = { viewModel.openDeleteConfirmationDialog() }
                            ) {
                                Text("DELETE", color = AlertRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to Register New Asset
        FloatingActionButton(
            onClick = { viewModel.openRegistrationDialog() },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 72.dp, end = NagarlensSpacing.small)
        ) {
            Text("+ REGISTER", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = NagarlensSpacing.medium))
        }
    }

    // Delete Confirmation Dialog
    if (uiState.isDeleteConfirmationDialogOpen && uiState.selectedAsset != null) {
        val assetToDelete = uiState.selectedAsset!!
        AlertDialog(
            onDismissRequest = { viewModel.closeDeleteConfirmationDialog() },
            title = { Text("Delete Asset ${assetToDelete.assetCode}?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete asset ${assetToDelete.assetCode} (${assetToDelete.assetCategory.name})? This action will remove the asset record from the local database.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteSelectedAsset() }
                ) {
                    Text("CONFIRM DELETE", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeDeleteConfirmationDialog() }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Asset Registration Dialog
    if (uiState.isRegistrationDialogOpen) {
        AssetRegistrationDialog(
            userLat = uiState.userLocation?.latitude ?: 12.9716,
            userLon = uiState.userLocation?.longitude ?: 77.5946,
            onDismiss = { viewModel.closeRegistrationDialog() },
            onSubmit = { category, lat, lon, desc ->
                viewModel.registerAsset(category, lat, lon, desc)
            }
        )
    }

    // Record Observation Dialog
    if (uiState.isObservationDialogOpen && uiState.selectedAsset != null) {
        ObservationDialog(
            onDismiss = { viewModel.closeObservationDialog() },
            onSubmit = { type, cat, sev, desc ->
                viewModel.recordObservation(type, cat, sev, desc)
            }
        )
    }

    // Record Intervention Dialog
    if (uiState.isInterventionDialogOpen && uiState.selectedAsset != null) {
        InterventionDialog(
            onDismiss = { viewModel.closeInterventionDialog() },
            onSubmit = { type, title, desc ->
                viewModel.recordIntervention(type, title, desc)
            }
        )
    }

    // UI Notice Dialog
    uiState.uiMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearUiMessage() },
            title = { Text("Asset Memory Graph Notice") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearUiMessage() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun HyperRealisticGisMapCanvas(
    assets: List<Asset>,
    selectedAsset: Asset?,
    userLat: Double,
    userLon: Double,
    zoomScale: Float,
    onAssetSelected: (Asset) -> Unit
) {
    val centerLat = userLat
    val centerLon = userLon
    // Use wider 0.030 degree span so all registered assets in Ward 12 catchment are always visible on canvas
    val latSpan = 0.030 / zoomScale
    val lonSpan = 0.030 / zoomScale

    val minLat = centerLat - (latSpan / 2)
    val maxLat = centerLat + (latSpan / 2)
    val minLon = centerLon - (lonSpan / 2)
    val maxLon = centerLon + (lonSpan / 2)

    val infiniteTransition = rememberInfiniteTransition(label = "HydraulicFlow")
    val flowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FlowOffset"
    )

    var pinOffsets by remember { mutableStateOf<List<Pair<Asset, Offset>>>(emptyList()) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(assets, zoomScale, minLat, maxLat, minLon, maxLon) {
                detectTapGestures { tapOffset ->
                    val tappedPair = pinOffsets.find { pair ->
                        val dx = pair.second.x - tapOffset.x
                        val dy = pair.second.y - tapOffset.y
                        sqrt(dx * dx + dy * dy) <= 45.0f
                    }
                    tappedPair?.let { onAssetSelected(it.first) }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        // 1. Draw City Block Parcels Background
        drawRoundRect(
            color = Color(0xFF141C28),
            topLeft = Offset(width * 0.05f, height * 0.08f),
            size = Size(width * 0.40f, height * 0.35f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = Color(0xFF141C28),
            topLeft = Offset(width * 0.55f, height * 0.08f),
            size = Size(width * 0.40f, height * 0.35f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = Color(0xFF141C28),
            topLeft = Offset(width * 0.05f, height * 0.52f),
            size = Size(width * 0.40f, height * 0.40f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = Color(0xFF141C28),
            topLeft = Offset(width * 0.55f, height * 0.52f),
            size = Size(width * 0.40f, height * 0.40f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // 2. Draw Major Road Network Corridors
        val roadDash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        // Horizontal Main Avenue
        drawLine(
            color = Color(0xFF263445),
            start = Offset(0f, height * 0.47f),
            end = Offset(width, height * 0.47f),
            strokeWidth = 24f
        )
        drawLine(
            color = Color(0xFF00F0FF).copy(alpha = 0.3f),
            start = Offset(0f, height * 0.47f),
            end = Offset(width, height * 0.47f),
            strokeWidth = 2f,
            pathEffect = roadDash
        )

        // Vertical Main Boulevard
        drawLine(
            color = Color(0xFF263445),
            start = Offset(width * 0.48f, 0f),
            end = Offset(width * 0.48f, height),
            strokeWidth = 24f
        )
        drawLine(
            color = Color(0xFF00F0FF).copy(alpha = 0.3f),
            start = Offset(width * 0.48f, 0f),
            end = Offset(width * 0.48f, height),
            strokeWidth = 2f,
            pathEffect = roadDash
        )

        // 3. Draw GIS Ward Boundary Line
        drawRoundRect(
            color = CyanAccent.copy(alpha = 0.5f),
            topLeft = Offset(width * 0.02f, height * 0.02f),
            size = Size(width * 0.96f, height * 0.96f),
            cornerRadius = CornerRadius(16f, 16f),
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        )

        // 4. Calculate Pin Offsets for Asset Coordinates
        val newPinOffsets = mutableListOf<Pair<Asset, Offset>>()

        assets.forEach { asset ->
            val x = ((asset.longitude - minLon) / (maxLon - minLon) * width).toFloat()
            val y = (height - ((asset.latitude - minLat) / (maxLat - minLat) * height)).toFloat()
            if (x in 0f..width && y in 0f..height) {
                newPinOffsets.add(asset to Offset(x, y))
            }
        }

        // 5. Draw Hydraulic Flow Network Lines Connecting Nearby Drainage Assets
        for (i in 0 until newPinOffsets.size - 1) {
            val p1 = newPinOffsets[i].second
            val p2 = newPinOffsets[i + 1].second

            val flowDash = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), flowOffset)
            drawLine(
                color = HydraulicFlowBlue.copy(alpha = 0.7f),
                start = p1,
                end = p2,
                strokeWidth = 3f,
                pathEffect = flowDash
            )
        }

        // 6. Draw User GPS Location Point & Accuracy Halo
        val userX = ((userLon - minLon) / (maxLon - minLon) * width).toFloat()
        val userY = (height - ((userLat - minLat) / (maxLat - minLat) * height)).toFloat()

        if (userX in 0f..width && userY in 0f..height) {
            drawCircle(
                color = Color(0x3300B0FF),
                radius = 35.0f * zoomScale,
                center = Offset(userX, userY)
            )
            drawCircle(
                color = Color(0xFF00B0FF),
                radius = 10.0f,
                center = Offset(userX, userY)
            )
            drawCircle(
                color = Color.White,
                radius = 4.0f,
                center = Offset(userX, userY)
            )
        }

        // 7. Draw Asset Pins & Selection Rings
        newPinOffsets.forEach { (asset, offset) ->
            val isSelected = selectedAsset?.id == asset.id
            val pinColor = when (asset.verificationStatus) {
                AssetVerificationStatus.VERIFIED -> SafetyGreen
                AssetVerificationStatus.REJECTED -> AlertRed
                else -> SignalAmber
            }

            if (isSelected) {
                drawCircle(
                    color = Color.White,
                    radius = 22.0f,
                    center = offset
                )
                drawCircle(
                    color = CyanAccent.copy(alpha = 0.5f),
                    radius = 32.0f,
                    center = offset
                )
            }

            // Outer Pin Ring
            drawCircle(
                color = pinColor,
                radius = if (isSelected) 14.0f else 10.0f,
                center = offset
            )
            // Core Dot
            drawCircle(
                color = Color.Black,
                radius = 4.0f,
                center = offset
            )
        }

        pinOffsets = newPinOffsets
    }
}

@Composable
private fun AssetRegistrationDialog(
    userLat: Double,
    userLon: Double,
    onDismiss: () -> Unit,
    onSubmit: (AssetCategory, Double, Double, String?) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(AssetCategory.CATCH_BASIN) }
    var latText by remember { mutableStateOf(userLat.toString()) }
    var lonText by remember { mutableStateOf(userLon.toString()) }
    var descriptionText by remember { mutableStateOf("") }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Infrastructure Asset", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)) {
                Text(text = "Select Asset Category:", style = MaterialTheme.typography.labelMedium)
                Box {
                    TextButton(onClick = { isCategoryDropdownExpanded = true }) {
                        Text(text = "CATEGORY: ${selectedCategory.name}", fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false }
                    ) {
                        AssetCategory.entries.filter { it != AssetCategory.UNKNOWN }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat
                                    isCategoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = latText,
                    onValueChange = { latText = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lonText,
                    onValueChange = { lonText = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            NagarlensPrimaryButton(
                onClick = {
                    val lat = latText.toDoubleOrNull() ?: userLat
                    val lon = lonText.toDoubleOrNull() ?: userLon
                    onSubmit(selectedCategory, lat, lon, descriptionText.ifBlank { null })
                }
            ) {
                Text("SUBMIT REGISTRATION")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
private fun ObservationDialog(
    onDismiss: () -> Unit,
    onSubmit: (ObservationType, DefectCategory, ConditionSeverity, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(ObservationType.ROUTINE_INSPECTION) }
    var selectedCategory by remember { mutableStateOf(DefectCategory.SOLID_WASTE) }
    var selectedSeverity by remember { mutableStateOf(ConditionSeverity.MODERATE) }
    var descriptionText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Field Observation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)) {
                Text(text = "Observation Type: ${selectedType.name}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Defect Category: ${selectedCategory.name}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Condition Severity: ${selectedSeverity.name}", style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Observation Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            NagarlensPrimaryButton(
                onClick = {
                    onSubmit(selectedType, selectedCategory, selectedSeverity, descriptionText.ifBlank { null })
                }
            ) {
                Text("RECORD OBSERVATION")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
private fun InterventionDialog(
    onDismiss: () -> Unit,
    onSubmit: (InterventionType, String, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(InterventionType.SILT_REMOVAL) }
    var titleText by remember { mutableStateOf("Desilting Invert Channel") }
    var descriptionText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Physical Intervention", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)) {
                Text(text = "Intervention Type: ${selectedType.name}", style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Intervention Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            NagarlensPrimaryButton(
                onClick = {
                    if (titleText.isNotBlank()) {
                        onSubmit(selectedType, titleText, descriptionText.ifBlank { null })
                    }
                }
            ) {
                Text("RECORD INTERVENTION")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
