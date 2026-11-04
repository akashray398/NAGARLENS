package com.nagarlens.app.feature.assets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.nagarlens.app.core.designsystem.component.NagarlensCard
import com.nagarlens.app.core.designsystem.component.NagarlensPrimaryButton
import com.nagarlens.app.core.designsystem.component.NagarlensSectionHeader
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing

@Composable
fun AssetDetailScreen(
    assetId: String,
    viewModel: AssetsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val asset = uiState.assets.find { it.id == assetId } ?: uiState.selectedAsset

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(NagarlensSpacing.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NagarlensSectionHeader(title = "Asset Memory Specification")
            TextButton(onClick = onNavigateBack) {
                Text("BACK")
            }
        }

        if (asset == null) {
            NagarlensCard {
                Text(
                    text = "Asset with ID '$assetId' not found in local memory cache.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            NagarlensCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = asset.assetCode,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                    Text(text = "Category: ${asset.assetCategory.name}")
                    Text(text = "Lifecycle Status: ${asset.lifecycleStatus.name}")
                    Text(text = "Verification Status: ${asset.verificationStatus.name}")
                    Text(text = "UUID: ${asset.id}", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                    Text(
                        text = "Spatial Position:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(text = "Latitude: %.6f°N".format(asset.latitude))
                    Text(text = "Longitude: %.6f°E".format(asset.longitude))
                    asset.altitudeMeters?.let { alt ->
                        Text(text = "Altitude: %.1f m".format(alt))
                    }

                    asset.description?.let { desc ->
                        Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                        Text(
                            text = "Description:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(text = desc, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(modifier = Modifier.height(NagarlensSpacing.medium))
                    Text(
                        text = "Version & Metadata:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(text = "Schema Version: v${asset.version}")
                    Text(text = "Created By: ${asset.createdBy}")
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            NagarlensCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Physical Identifiers & Tags",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                    Text(text = "• QR Code: QR-${asset.assetCode}")
                    Text(text = "• Municipal Tag: MUNI-BLR-${asset.assetCode.takeLast(6)}")
                    Text(text = "• NFC Chip Tag: NFC-379A-${asset.id.take(4)}")
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            NagarlensPrimaryButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("RETURN TO REGISTRY")
            }
        }
    }
}
