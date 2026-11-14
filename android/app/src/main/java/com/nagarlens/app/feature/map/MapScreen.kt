package com.nagarlens.app.feature.map

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nagarlens.app.core.designsystem.component.NagarlensCard
import com.nagarlens.app.core.designsystem.component.NagarlensPrimaryButton
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
import kotlin.math.sqrt

// Indian GIS Map Theme Colors
private val MapDarkBackground = Color(0xFF070B12)
private val CardGlassBackground = Color(0xF5101726)
private val CyanAccent = Color(0xFF00F0FF)
private val ElectricBlue = Color(0xFF00B0FF)
private val HydraulicFlowBlue = Color(0xFF00E5FF)
private val RoadAsphaltColor = Color(0xFF1B2433)
private val BuildingParcelColor = Color(0xFF111824)

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
    ) {
        if (layerMode == MapLayerMode.SATELLITE_GIS) {
            // Edge-to-Edge Fullscreen Interactive GIS Map Canvas
            HyperRealisticGisMapCanvas(
                assets = filteredAssets,
                selectedAsset = uiState.selectedAsset,
                userLat = uiState.userLocation?.latitude ?: 12.9716,
                userLon = uiState.userLocation?.longitude ?: 77.5946,
                zoomScale = zoomScale,
                onAssetSelected = { viewModel.selectAsset(it) }
            )

            // Top Floating Search Bar & Filter HUD Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(NagarlensSpacing.small),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Top Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardGlassBackground, shape = RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF1E3A5F), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = NagarlensSpacing.small, vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(NagarlensSpacing.small))
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                placeholder = { Text("Search Map Code or Location...", color = Color.Gray, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        // GIS MAP vs LIST Toggle
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = layerMode == MapLayerMode.SATELLITE_GIS,
                                onClick = { layerMode = MapLayerMode.SATELLITE_GIS },
                                label = { Text("GIS", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
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
                }

                // Category Filter Chips Floating Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.extraSmall),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.activeFilters) { filter ->
                        FilterChip(
                            selected = uiState.selectedFilter == filter,
                            onClick = { viewModel.selectFilter(filter) },
                            label = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = Color.White,
                                containerColor = CardGlassBackground,
                                labelColor = Color.LightGray
                            )
                        )
                    }
                }
            }

            // Floating Zoom & GPS Map Controls Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = NagarlensSpacing.small),
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
                    .padding(start = NagarlensSpacing.medium, bottom = 80.dp)
                    .background(Color.Black.copy(alpha = 0.80f), shape = RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF1E3A5F), shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).background(SafetyGreen, shape = CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HYDRAULIC FLOW • ZOOM %.1fx • %d ASSETS ON CANVAS".format(zoomScale, filteredAssets.size),
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

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = layerMode == MapLayerMode.SATELLITE_GIS,
                            onClick = { layerMode = MapLayerMode.SATELLITE_GIS },
                            label = { Text("GIS", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
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
                                        color = if (uiState.selectedAsset?.id == asset.id) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF121A28),
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

        // Floating Asset Digital Twin Memory Drawer Bottom Sheet
        uiState.selectedAsset?.let { asset ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp, start = NagarlensSpacing.small, end = NagarlensSpacing.small)
                    .background(CardGlassBackground, shape = RoundedCornerShape(16.dp))
                    .border(1.5.dp, CyanAccent, shape = RoundedCornerShape(16.dp))
                    .padding(NagarlensSpacing.small)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Digital Twin: ${asset.assetCode}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Category: ${asset.assetCategory.name} • Status: ${asset.verificationStatus.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent
                            )
                        }
                        IconButton(onClick = { viewModel.selectAsset(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                        }
                    }

                    Text(text = "WGS84: %.6f°N, %.6f°E".format(asset.latitude, asset.longitude), style = MaterialTheme.typography.bodySmall, color = Color.LightGray)

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

                    Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.extraSmall)
                    ) {
                        NagarlensPrimaryButton(
                            onClick = { viewModel.openObservationDialog() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ OBSERVE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        NagarlensPrimaryButton(
                            onClick = { viewModel.openInterventionDialog() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ INTERVENE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = { viewModel.openDeleteConfirmationDialog() }
                        ) {
                            Text("DELETE", color = AlertRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
                .padding(bottom = 76.dp, end = NagarlensSpacing.medium)
        ) {
            Text("+ REGISTER", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = NagarlensSpacing.medium))
        }
    }

    // Delete Confirmation Dialog
    if (uiState.isDeleteConfirmationDialogOpen && uiState.selectedAsset != null) {
        val assetToDelete = uiState.selectedAsset!!
        AlertDialog(
            onDismissRequest = { viewModel.closeDeleteConfirmationDialog() },
            containerColor = CardGlassBackground,
            title = { Text("Delete Asset ${assetToDelete.assetCode}?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Text("Are you sure you want to delete asset ${assetToDelete.assetCode} (${assetToDelete.assetCategory.name})? This action will remove the asset record from the local database.", color = Color.LightGray)
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
                    Text("CANCEL", color = Color.Gray)
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
            containerColor = CardGlassBackground,
            title = { Text("Asset Memory Graph Notice", color = Color.White) },
            text = { Text(msg, color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearUiMessage() }) {
                    Text("OK", color = CyanAccent, fontWeight = FontWeight.Bold)
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
    val textMeasurer = rememberTextMeasurer()

    val centerLat = userLat
    val centerLon = userLon
    val latSpan = 0.035 / zoomScale
    val lonSpan = 0.035 / zoomScale

    val minLat = centerLat - (latSpan / 2)
    val maxLat = centerLat + (latSpan / 2)
    val minLon = centerLon - (lonSpan / 2)
    val maxLon = centerLon + (lonSpan / 2)

    val infiniteTransition = rememberInfiniteTransition(label = "GisAnimations")
    val flowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FlowOffset"
    )
    val faultPulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FaultPulse"
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
                        sqrt(dx * dx + dy * dy) <= 50.0f
                    }
                    tappedPair?.let { onAssetSelected(it.first) }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        // 1. Draw Real City Land Parcels & Greenery Corridors (Bangalore Central Ward 12 GIS)
        drawRoundRect(
            color = BuildingParcelColor,
            topLeft = Offset(width * 0.04f, height * 0.08f),
            size = Size(width * 0.42f, height * 0.38f),
            cornerRadius = CornerRadius(14f, 14f)
        )
        drawRoundRect(
            color = Color(0xFF0F1B18), // Cubbon Park Greenery Zone
            topLeft = Offset(width * 0.54f, height * 0.08f),
            size = Size(width * 0.42f, height * 0.38f),
            cornerRadius = CornerRadius(14f, 14f)
        )
        drawRoundRect(
            color = BuildingParcelColor,
            topLeft = Offset(width * 0.04f, height * 0.52f),
            size = Size(width * 0.42f, height * 0.42f),
            cornerRadius = CornerRadius(14f, 14f)
        )
        drawRoundRect(
            color = BuildingParcelColor,
            topLeft = Offset(width * 0.55f, height * 0.52f),
            size = Size(width * 0.40f, height * 0.40f),
            cornerRadius = CornerRadius(14f, 14f)
        )

        // 2. Draw Indian Urban Road Network Corridors & Asphalt Casings
        val roadDash = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)

        // MG Road Horizontal Main Avenue
        drawLine(
            color = RoadAsphaltColor,
            start = Offset(0f, height * 0.48f),
            end = Offset(width, height * 0.48f),
            strokeWidth = 32f
        )
        drawLine(
            color = Color(0xFF00F0FF).copy(alpha = 0.4f),
            start = Offset(0f, height * 0.48f),
            end = Offset(width, height * 0.48f),
            strokeWidth = 2f,
            pathEffect = roadDash
        )

        // Residency Road Vertical Boulevard
        drawLine(
            color = RoadAsphaltColor,
            start = Offset(width * 0.48f, 0f),
            end = Offset(width * 0.48f, height),
            strokeWidth = 32f
        )
        drawLine(
            color = Color(0xFF00F0FF).copy(alpha = 0.4f),
            start = Offset(width * 0.48f, 0f),
            end = Offset(width * 0.48f, height),
            strokeWidth = 2f,
            pathEffect = roadDash
        )

        // Street Name Overlay Labels
        val street1 = textMeasurer.measure("MG ROAD ARTERIAL DRAIN", androidx.compose.ui.text.TextStyle(color = Color.LightGray.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold))
        drawText(street1, topLeft = Offset(width * 0.05f, height * 0.45f))

        val street2 = textMeasurer.measure("RESIDENCY RD CORRIDOR", androidx.compose.ui.text.TextStyle(color = Color.LightGray.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold))
        drawText(street2, topLeft = Offset(width * 0.50f, height * 0.04f))

        // 3. Draw GIS Ward Boundary Outline Line
        drawRoundRect(
            color = CyanAccent.copy(alpha = 0.4f),
            topLeft = Offset(width * 0.02f, height * 0.02f),
            size = Size(width * 0.96f, height * 0.96f),
            cornerRadius = CornerRadius(18f, 18f),
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f))
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

        // 5. Draw Animated Hydraulic Flow Drainage Network Lines Connecting Assets
        for (i in 0 until newPinOffsets.size - 1) {
            val p1 = newPinOffsets[i].second
            val p2 = newPinOffsets[i + 1].second

            val flowDash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), flowOffset)
            drawLine(
                color = HydraulicFlowBlue.copy(alpha = 0.85f),
                start = p1,
                end = p2,
                strokeWidth = 3.5f,
                pathEffect = flowDash
            )
        }

        // 6. Draw User GPS Location Point & Glowing Precision Halo
        val userX = ((userLon - minLon) / (maxLon - minLon) * width).toFloat()
        val userY = (height - ((userLat - minLat) / (maxLat - minLat) * height)).toFloat()

        if (userX in 0f..width && userY in 0f..height) {
            drawCircle(
                color = Color(0x3300B0FF),
                radius = 38.0f * zoomScale,
                center = Offset(userX, userY)
            )
            drawCircle(
                color = Color(0xFF00B0FF),
                radius = 12.0f,
                center = Offset(userX, userY)
            )
            drawCircle(
                color = Color.White,
                radius = 4.0f,
                center = Offset(userX, userY)
            )
        }

        // 7. Draw 3D Asset Teardrop HUD Pins, Fault Hotspots & Zoom Detail Scaling
        newPinOffsets.forEach { (asset, offset) ->
            val isSelected = selectedAsset?.id == asset.id
            val isFaultHotspot = asset.verificationStatus != AssetVerificationStatus.VERIFIED

            val pinColor = when (asset.verificationStatus) {
                AssetVerificationStatus.VERIFIED -> SafetyGreen
                AssetVerificationStatus.REJECTED -> AlertRed
                else -> SignalAmber
            }

            // Draw Expanding Fault Hotspot Pulse Ring for unverified/critical assets
            if (isFaultHotspot) {
                val faultAlpha = (1.0f - (faultPulseRadius / 42f)).coerceIn(0f, 1f)
                drawCircle(
                    color = pinColor.copy(alpha = faultAlpha * 0.5f),
                    radius = faultPulseRadius * zoomScale,
                    center = offset
                )
            }

            // Draw selection halo
            if (isSelected) {
                drawCircle(
                    color = Color.White,
                    radius = 24.0f,
                    center = offset
                )
                drawCircle(
                    color = CyanAccent.copy(alpha = 0.6f),
                    radius = 36.0f,
                    center = offset
                )
            }

            // Outer Pin Marker
            drawCircle(
                color = pinColor,
                radius = if (isSelected) 15.0f else 11.0f,
                center = offset
            )
            // Core Dot
            drawCircle(
                color = Color.Black,
                radius = 4.5f,
                center = offset
            )

            // Draw Callout Badge with Asset Code & Zoom Fault Detail Scaling
            val labelText = if (zoomScale >= 1.5f && isFaultHotspot) {
                "${asset.assetCode} • FAULT: SILT 78%"
            } else {
                asset.assetCode
            }

            val textLayoutResult = textMeasurer.measure(
                text = labelText,
                style = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            val badgeWidth = textLayoutResult.size.width + 12f
            val badgeHeight = textLayoutResult.size.height + 6f
            val badgeOffset = Offset(offset.x - (badgeWidth / 2f), offset.y - 34f)

            drawRoundRect(
                color = Color.Black.copy(alpha = 0.88f),
                topLeft = badgeOffset,
                size = Size(badgeWidth, badgeHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = pinColor,
                topLeft = badgeOffset,
                size = Size(badgeWidth, badgeHeight),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 1.5f)
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(badgeOffset.x + 6f, badgeOffset.y + 3f)
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
        containerColor = CardGlassBackground,
        title = { Text("Register Infrastructure Asset", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)) {
                Text(text = "Select Asset Category:", style = MaterialTheme.typography.labelMedium, color = CyanAccent)
                Box {
                    TextButton(onClick = { isCategoryDropdownExpanded = true }) {
                        Text(text = "CATEGORY: ${selectedCategory.name}", fontWeight = FontWeight.Bold, color = Color.White)
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
                Text("CANCEL", color = Color.Gray)
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
        containerColor = CardGlassBackground,
        title = { Text("Record Field Observation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)) {
                Text(text = "Observation Type: ${selectedType.name}", style = MaterialTheme.typography.bodySmall, color = CyanAccent)
                Text(text = "Defect Category: ${selectedCategory.name}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Text(text = "Condition Severity: ${selectedSeverity.name}", style = MaterialTheme.typography.bodySmall, color = SignalAmber)

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
                Text("CANCEL", color = Color.Gray)
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
        containerColor = CardGlassBackground,
        title = { Text("Record Physical Intervention", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)) {
                Text(text = "Intervention Type: ${selectedType.name}", style = MaterialTheme.typography.bodySmall, color = CyanAccent)

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
                Text("CANCEL", color = Color.Gray)
            }
        }
    )
}
