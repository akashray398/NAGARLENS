package com.nagarlens.app.feature.incidents

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.nagarlens.app.core.model.Incident
import com.nagarlens.app.core.model.IncidentPriority
import com.nagarlens.app.core.model.IncidentStatus

@Composable
fun IncidentScreen(
    viewModel: IncidentViewModel = hiltViewModel(),
    onNavigateToInvestigation: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val filterOptions = listOf("All", "Open", "In Progress", "Resolved")

    val filteredIncidents = uiState.incidents.filter { inc ->
        when (uiState.selectedStatusFilter) {
            "Open" -> inc.status == IncidentStatus.OPEN
            "In Progress" -> inc.status == IncidentStatus.IN_PROGRESS
            "Resolved" -> inc.status == IncidentStatus.RESOLVED || inc.status == IncidentStatus.CLOSED
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(NagarlensSpacing.medium)
    ) {
        NagarlensSectionHeader(title = "Municipal Incident & Failure Work Orders")

        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.small),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterOptions) { filter ->
                FilterChip(
                    selected = uiState.selectedStatusFilter == filter,
                    onClick = { viewModel.selectStatusFilter(filter) },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

        if (filteredIncidents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No incidents logged matching active filter.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredIncidents) { incident ->
                    NagarlensCard(
                        modifier = Modifier.clickable { viewModel.selectIncident(incident) }
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = incident.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )

                                PriorityBadge(priority = incident.priority)
                            }

                            Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))

                            Text(
                                text = "Asset: ${incident.assetCode} • Category: ${incident.defectCategory.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            incident.description?.let { desc ->
                                Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(NagarlensSpacing.small))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status: ${incident.status.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                TextButton(
                                    onClick = { onNavigateToInvestigation(incident.id) }
                                ) {
                                    Text("INVESTIGATE")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.selectedIncident?.let { selected ->
        AlertDialog(
            onDismissRequest = { viewModel.selectIncident(null) },
            title = { Text(selected.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.extraSmall)) {
                    Text(text = "Asset Code: ${selected.assetCode}")
                    Text(text = "Priority: ${selected.priority.name}")
                    Text(text = "Current Status: ${selected.status.name}")
                    Text(text = "Reported By: ${selected.reportedBy}")
                    selected.description?.let { Text(text = "Details: $it") }
                }
            },
            confirmButton = {
                NagarlensPrimaryButton(
                    onClick = {
                        val nextStatus = when (selected.status) {
                            IncidentStatus.OPEN -> IncidentStatus.IN_PROGRESS
                            IncidentStatus.IN_PROGRESS -> IncidentStatus.RESOLVED
                            else -> IncidentStatus.CLOSED
                        }
                        viewModel.updateStatus(selected.id, nextStatus)
                        viewModel.selectIncident(null)
                    }
                ) {
                    Text("ADVANCE STATUS")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.selectIncident(null) }) {
                    Text("DISMISS")
                }
            }
        )
    }

    uiState.actionMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearActionMessage() },
            title = { Text("Incident State Updated") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearActionMessage() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun PriorityBadge(priority: IncidentPriority) {
    val (color, text) = when (priority) {
        IncidentPriority.CRITICAL -> AlertRed to "CRITICAL"
        IncidentPriority.HIGH -> SignalAmber to "HIGH"
        IncidentPriority.MEDIUM -> MaterialTheme.colorScheme.secondary to "MEDIUM"
        else -> SafetyGreen to "LOW"
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
