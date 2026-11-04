package com.nagarlens.app.feature.assets

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

@Composable
fun AssetsScreen(
    viewModel: AssetsViewModel = hiltViewModel(),
    onNavigateToAssetDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val filterOptions = listOf("All", "Catch Basins", "Storm Water Drains", "Manholes", "Culverts")

    val filteredAssets = uiState.assets.filter { asset ->
        val matchesCategory = when (uiState.selectedCategoryFilter) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(NagarlensSpacing.medium)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NagarlensSectionHeader(title = "Infrastructure Asset Registry")

            // Search input field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Search by Asset Code or Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(NagarlensSpacing.small))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { filter ->
                    FilterChip(
                        selected = uiState.selectedCategoryFilter == filter,
                        onClick = { viewModel.selectCategoryFilter(filter) },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            if (filteredAssets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No assets found matching filter criteria.\nTap + REGISTER to add a new asset.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredAssets) { asset ->
                        NagarlensCard(
                            modifier = Modifier.clickable {
                                viewModel.selectAsset(asset)
                                onNavigateToAssetDetail(asset.id)
                            }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = asset.assetCode,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    StatusBadge(status = asset.verificationStatus)
                                }

                                Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))

                                Text(
                                    text = "Category: ${asset.assetCategory.name} • Lifecycle: ${asset.lifecycleStatus.name}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    text = "Coordinates: %.4f°N, %.4f°E".format(asset.latitude, asset.longitude),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                asset.description?.let { desc ->
                                    Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.openRegistrationDialog() },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = NagarlensSpacing.large, end = NagarlensSpacing.small)
        ) {
            Text("+ REGISTER", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = NagarlensSpacing.medium))
        }
    }

    if (uiState.isRegistrationDialogOpen) {
        AssetRegistrationDialog(
            userLat = 12.9716,
            userLon = 77.5946,
            onDismiss = { viewModel.closeRegistrationDialog() },
            onSubmit = { cat, lat, lon, desc ->
                viewModel.registerAsset(cat, lat, lon, desc)
            }
        )
    }

    uiState.message?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            title = { Text("Asset Registry Notice") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessage() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun StatusBadge(status: AssetVerificationStatus) {
    val (color, text) = when (status) {
        AssetVerificationStatus.VERIFIED -> SafetyGreen to "VERIFIED"
        AssetVerificationStatus.REJECTED -> AlertRed to "REJECTED"
        AssetVerificationStatus.PENDING_REVIEW -> SignalAmber to "PENDING"
        else -> MaterialTheme.colorScheme.secondary to "UNVERIFIED"
    }

    Box(
        modifier = Modifier
            .background(color = color, shape = MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
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
