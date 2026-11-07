package com.nagarlens.app.feature.map

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

@Composable
fun MapScreen(
    onNavigateToCapture: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            .padding(NagarlensSpacing.medium)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NagarlensSectionHeader(title = "Living Infrastructure Map & Spatial Twin")

            Text(
                text = uiState.activeWard,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(NagarlensSpacing.small))

            // Spatial Search input
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Filter map by Code or Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(NagarlensSpacing.small))

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

            Spacer(modifier = Modifier.height(NagarlensSpacing.small))

            // Living Map Viewport Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(color = Color(0xFF1E242B), shape = MaterialTheme.shapes.medium)
                    .border(width = 2.dp, color = MaterialTheme.colorScheme.secondary, shape = MaterialTheme.shapes.medium)
            ) {
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
                            text = "VIEWPORT: 12.960°-12.985°N, 77.580°-77.610°E",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                        Text(
                            text = "${filteredAssets.size} Assets • ${uiState.spatialClusters.size} Clusters",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SafetyGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(NagarlensSpacing.small))

                    // Spatial Asset & Cluster Grid List
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
                                            color = if (uiState.selectedAsset?.id == asset.id) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF2A323D),
                                            shape = MaterialTheme.shapes.small
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
                                Text("CLOSE")
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
                            color = MaterialTheme.colorScheme.secondary
                        )

                        uiState.selectedAssetTimeline?.let { timeline ->
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)
                        ) {
                            NagarlensPrimaryButton(
                                onClick = { viewModel.openObservationDialog() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+ OBSERVATION")
                            }
                            NagarlensPrimaryButton(
                                onClick = { viewModel.openInterventionDialog() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+ INTERVENTION")
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
