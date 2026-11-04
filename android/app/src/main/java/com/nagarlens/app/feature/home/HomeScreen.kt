package com.nagarlens.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nagarlens.app.core.designsystem.component.NagarlensCard
import com.nagarlens.app.core.designsystem.component.NagarlensPrimaryButton
import com.nagarlens.app.core.designsystem.component.NagarlensSecondaryButton
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.designsystem.theme.SafetyGreen
import com.nagarlens.app.core.designsystem.theme.SignalAmber

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToMap: () -> Unit = {},
    onNavigateToCapture: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(NagarlensSpacing.medium),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Hero Brand Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.appTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))

                Text(
                    text = uiState.category,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "\"${uiState.tagline}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = NagarlensSpacing.extraSmall)
                )
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            // Inspector Session Banner
            NagarlensCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FIELD AUDITOR",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = uiState.userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.activeWard,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(SafetyGreen, shape = MaterialTheme.shapes.extraSmall)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ONLINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.small))

            // Live Infrastructure Metrics Cards
            uiState.metrics?.let { metrics ->
                NagarlensCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Infrastructure Memory Status",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricTile(title = "Assets", value = "${metrics.totalAssets}")
                            MetricTile(title = "Verified", value = "${metrics.verifiedAssets}", color = SafetyGreen)
                            MetricTile(title = "Open Incidents", value = "${metrics.openIncidentsCount}", color = SignalAmber)
                            MetricTile(title = "Evidence", value = "${metrics.totalEvidenceFiles}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.small))

            // Deployment Domain Card
            NagarlensCard {
                Text(
                    text = "Initial Deployment Domain",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                Text(
                    text = uiState.activeDomain,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            // Spatial Map Viewport Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF1E242B), shape = MaterialTheme.shapes.medium)
                    .border(2.dp, MaterialTheme.colorScheme.secondary, shape = MaterialTheme.shapes.medium)
                    .padding(NagarlensSpacing.medium)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "LIVING MAP VIEWPORT", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        Text(text = "12.971°N, 77.594°E", style = MaterialTheme.typography.labelSmall, color = SafetyGreen, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Spatial Asset Grid Active • Offline Tile Fallback Ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)
            ) {
                NagarlensPrimaryButton(
                    onClick = onNavigateToCapture,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Collect Evidence", fontWeight = FontWeight.Bold)
                }

                NagarlensSecondaryButton(
                    onClick = onNavigateToMap,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "View Spatial Map", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(NagarlensSpacing.large))

        // System Shell Status Footnote
        Text(
            text = uiState.statusText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = NagarlensSpacing.small)
        )
    }
}

@Composable
private fun MetricTile(title: String, value: String, color: Color = Color.Unspecified) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
    }
}
