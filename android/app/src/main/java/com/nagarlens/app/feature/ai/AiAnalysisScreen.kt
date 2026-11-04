package com.nagarlens.app.feature.ai

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nagarlens.app.core.designsystem.component.NagarlensCard
import com.nagarlens.app.core.designsystem.component.NagarlensPrimaryButton
import com.nagarlens.app.core.designsystem.component.NagarlensSectionHeader
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.designsystem.theme.SignalAmber

@Composable
fun AiAnalysisScreen(
    evidenceId: String,
    viewModel: AiAnalysisViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(evidenceId) {
        viewModel.loadAnalysis(evidenceId)
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
            NagarlensSectionHeader(title = "AI Computer Vision Evidence Analysis")
            TextButton(onClick = onNavigateBack) {
                Text("CLOSE")
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SignalAmber)
            }
        } else if (uiState.assessment != null) {
            val assessment = uiState.assessment!!

            // Model Status Banner
            NagarlensCard(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Inference Status: ${assessment.processingStatus.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SignalAmber
                    )
                    Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                    Text(
                        text = assessment.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            // Evidence Frame Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color.Black, shape = MaterialTheme.shapes.medium)
                    .border(2.dp, MaterialTheme.colorScheme.secondary, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EVIDENCE IMAGE PREVIEW\nID: $evidenceId\nBounding Boxes: ${assessment.boundingBoxes.size}",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            // Analysis Metrics Specification
            NagarlensCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Computer Vision Model Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                    Text(text = "Model Name: ${assessment.modelName}")
                    Text(text = "Model Version: ${assessment.modelVersion}")
                    Text(text = "Taxonomy Version: ${assessment.taxonomyVersion}")
                    Text(text = "Predicted Category: ${assessment.predictedCategory.name}")
                    Text(text = "Confidence Score: %.1f%%".format(assessment.confidenceScore * 100))
                    Text(text = "Image Quality Check: ${assessment.imageQualityStatus.name}")
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            NagarlensPrimaryButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("RETURN TO EVIDENCE ACTIVITY")
            }
        }
    }
}
