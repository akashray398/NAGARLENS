package com.nagarlens.app.feature.reports

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
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
import com.nagarlens.app.core.model.ReportFormat
import com.nagarlens.app.core.model.ReportType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(NagarlensSpacing.medium)
    ) {
        NagarlensSectionHeader(title = "Municipal Compliance & Audit Reports")

        NagarlensCard {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Generate One-Click Report",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)
                ) {
                    NagarlensPrimaryButton(
                        onClick = { viewModel.generateReport(ReportType.MUNICIPAL_INFRASTRUCTURE_AUDIT, ReportFormat.PDF) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ AUDIT PDF")
                    }

                    NagarlensPrimaryButton(
                        onClick = { viewModel.generateReport(ReportType.INCIDENT_SUMMARY_REPORT, ReportFormat.CSV) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ INCIDENTS CSV")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

        if (uiState.reports.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No reports generated yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.reports) { report ->
                    NagarlensCard {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = report.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = report.format.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                            Text(text = "Report ID: ${report.id} • Records: ${report.totalRecordsIncluded}")
                            Text(
                                text = "Generated: ${dateFormat.format(Date(report.generatedTimestampUtc))} by ${report.generatedBy}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    uiState.actionMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearActionMessage() },
            title = { Text("Report Engine Notice") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearActionMessage() }) {
                    Text("OK")
                }
            }
        )
    }
}
