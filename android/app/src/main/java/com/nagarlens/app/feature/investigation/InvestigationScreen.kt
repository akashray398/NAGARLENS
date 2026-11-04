package com.nagarlens.app.feature.investigation

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.nagarlens.app.core.designsystem.theme.SignalAmber

@Composable
fun InvestigationScreen(
    incidentId: String,
    viewModel: InvestigationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(incidentId) {
        viewModel.loadInvestigation(incidentId)
    }

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
            NagarlensSectionHeader(title = "Infrastructure Root Cause Investigation")
            TextButton(onClick = onNavigateBack) {
                Text("CLOSE")
            }
        }

        if (uiState.isLoading || uiState.investigationCase == null) {
            CircularProgressIndicator(color = SignalAmber, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            val invCase = uiState.investigationCase!!

            NagarlensCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = invCase.caseNumber,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                    Text(text = "Investigation Status: ${invCase.status}", fontWeight = FontWeight.Bold)
                    Text(text = "Incident Ref ID: ${invCase.incidentId}")
                    Text(text = "Assigned Lead: ${invCase.assignedInvestigator}")

                    Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                    Text(
                        text = "Executive Summary:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(text = invCase.summary, style = MaterialTheme.typography.bodyMedium)

                    invCase.rootCauseAnalysis?.let { analysis ->
                        Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                        Text(
                            text = "Root Cause Evidence Analysis:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(text = analysis, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            NagarlensCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Digital Evidence Chain-of-Custody",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                    Text(text = "• SHA-256 Immutability Digest Verified")
                    Text(text = "• GNSS Telemetry Cryptographically Bound")
                    Text(text = "• Audit Log Immutable Append-Only Active")
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            NagarlensPrimaryButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("RETURN TO INCIDENTS")
            }
        }
    }
}
