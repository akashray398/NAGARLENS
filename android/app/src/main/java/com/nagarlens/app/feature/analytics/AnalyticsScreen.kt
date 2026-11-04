package com.nagarlens.app.feature.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.nagarlens.app.core.designsystem.component.NagarlensCard
import com.nagarlens.app.core.designsystem.component.NagarlensSectionHeader
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.designsystem.theme.SafetyGreen
import com.nagarlens.app.core.designsystem.theme.SignalAmber

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val timeRanges = listOf("7 Days", "30 Days", "90 Days", "1 Year")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(NagarlensSpacing.medium)
    ) {
        NagarlensSectionHeader(title = "Infrastructure Analytics & Health Metrics")

        // Time range filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.small),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(timeRanges) { range ->
                FilterChip(
                    selected = uiState.selectedTimeRange == range,
                    onClick = { viewModel.setTimeRange(range) },
                    label = { Text(range) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

        uiState.metrics?.let { metrics ->
            // Overall Infrastructure Health Card
            NagarlensCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Infrastructure Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Assets", style = MaterialTheme.typography.labelSmall)
                            Text(text = "${metrics.totalAssets}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "Verified", style = MaterialTheme.typography.labelSmall)
                            Text(text = "${metrics.verifiedAssets}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SafetyGreen)
                        }
                        Column {
                            Text(text = "Open Incidents", style = MaterialTheme.typography.labelSmall)
                            Text(text = "${metrics.openIncidentsCount}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SignalAmber)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))
        }

        // Asset Condition Breakdown Card
        NagarlensCard {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Asset Condition Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                uiState.conditionDist.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = NagarlensSpacing.extraSmall),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.category, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${item.count} (${item.percentage}%)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

        // Defect Taxonomy Summary Card
        NagarlensCard {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Defect Taxonomy Frequency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                uiState.defectDist.forEach { defect ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = NagarlensSpacing.extraSmall),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = defect.defectCategory.name, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "${defect.incidentCount} cases (Sev: %.2f)".format(defect.severityAverage),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SignalAmber
                        )
                    }
                }
            }
        }
    }
}
