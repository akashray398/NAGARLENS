package com.nagarlens.app.feature.activity

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
import com.nagarlens.app.core.model.InvestigationCase
import com.nagarlens.app.core.model.InvestigationStatus
import com.nagarlens.app.core.model.SyncStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val filteredRecords = uiState.evidenceList.filter { evidence ->
        when (uiState.activeFilter) {
            "Queued / Local" -> evidence.syncStatus == SyncStatus.LOCAL_ONLY || evidence.syncStatus == SyncStatus.QUEUED || evidence.syncStatus == SyncStatus.UPLOADING
            "Synced" -> evidence.syncStatus == SyncStatus.SYNCED
            "Failures" -> evidence.syncStatus == SyncStatus.RETRYABLE_FAILURE || evidence.syncStatus == SyncStatus.PERMANENT_FAILURE
            else -> true
        }
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm 'UTC'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(NagarlensSpacing.medium)
    ) {
        NagarlensSectionHeader(title = "Investigation Command & Evidence Sync")

        // Active Investigation Command Center Section
        if (uiState.investigationsList.isNotEmpty()) {
            NagarlensCard(containerColor = Color(0xFF1E242B)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "INVESTIGATION COMMAND CENTER",
                        style = MaterialTheme.typography.labelSmall,
                        color = SignalAmber,
                        fontWeight = FontWeight.Bold
                    )

                    uiState.investigationsList.forEach { invCase ->
                        Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectInvestigation(invCase) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = invCase.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Asset: ${invCase.assetCode} • Assigned: ${invCase.assignedInvestigator}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(SafetyGreen, shape = MaterialTheme.shapes.extraSmall)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = invCase.status.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(NagarlensSpacing.small))
        }

        // Selected Investigation Detail Drawer
        uiState.selectedInvestigation?.let { invCase ->
            NagarlensCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Workspace: ${invCase.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { viewModel.selectInvestigation(null) }) {
                            Text("CLOSE")
                        }
                    }
                    Text(text = "Status: ${invCase.status.name} • Priority: ${invCase.priority.name}")
                    invCase.description?.let { desc ->
                        Text(text = desc, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                    Text(
                        text = "Investigation Timeline Events:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )

                    invCase.timelineEvents.forEach { event ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "• ${event.title}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text(text = dateFormat.format(Date(event.timestampUtc)), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)
                    ) {
                        NagarlensPrimaryButton(
                            onClick = {
                                viewModel.requestEvidence(invCase.id, invCase.assetId, "Take high-angle photo of grate invert channel.")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ REQUEST EVIDENCE")
                        }
                        NagarlensPrimaryButton(
                            onClick = {
                                viewModel.advanceInvestigationStatus(invCase.id, InvestigationStatus.ACTION_PROPOSED)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("PROPOSE ACTION")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(NagarlensSpacing.small))
        }

        // Filter Chips for Evidence
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.small),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(uiState.filterOptions) { filter ->
                FilterChip(
                    selected = uiState.activeFilter == filter,
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

        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No evidence records found.\nCapture evidence using the Camera.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredRecords) { evidence ->
                    NagarlensCard {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = evidence.originalFileName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                SyncStatusBadge(status = evidence.syncStatus)
                            }

                            Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                            Text(
                                text = "ID: ${evidence.id} • %.2f KB".format(evidence.fileSizeBytes / 1024.0),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Captured: ${dateFormat.format(Date(evidence.captureTimestampUtc))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            val locStr = if (evidence.latitude != null && evidence.longitude != null) {
                                "GNSS: %.4f°N, %.4f°E (±%.1fm)".format(evidence.latitude, evidence.longitude, evidence.locationAccuracyMeters ?: 0f)
                            } else {
                                "GNSS: Disabled / Unavailable"
                            }
                            Text(
                                text = locStr,
                                style = MaterialTheme.typography.labelSmall
                            )

                            Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                            Text(
                                text = "SHA-256 Digest:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = evidence.sha256Hash,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )

                            evidence.lastSyncError?.let { error ->
                                Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                                Text(
                                    text = "Status Detail: $error",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AlertRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (evidence.syncStatus == SyncStatus.RETRYABLE_FAILURE || evidence.syncStatus == SyncStatus.LOCAL_ONLY) {
                                Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                                NagarlensPrimaryButton(
                                    onClick = { viewModel.retrySync(evidence.id) }
                                ) {
                                    Text("RETRY SYNC")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.actionMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearActionMessage() },
            title = { Text("Command Center Notice") },
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
private fun SyncStatusBadge(status: SyncStatus) {
    val (color, text) = when (status) {
        SyncStatus.LOCAL_ONLY -> MaterialTheme.colorScheme.secondary to "LOCAL ONLY"
        SyncStatus.QUEUED -> SignalAmber to "QUEUED"
        SyncStatus.UPLOADING -> SignalAmber to "UPLOADING"
        SyncStatus.SYNCED -> SafetyGreen to "SYNCED"
        SyncStatus.RETRYABLE_FAILURE -> SignalAmber to "RETRYABLE FAIL"
        SyncStatus.PERMANENT_FAILURE -> AlertRed to "PERMANENT FAIL"
    }

    Box(
        modifier = Modifier
            .background(
                color = color,
                shape = MaterialTheme.shapes.extraSmall
            )
            .padding(horizontal = NagarlensSpacing.small, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
