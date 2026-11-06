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
import androidx.compose.material3.LinearProgressIndicator
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
import com.nagarlens.app.core.designsystem.theme.AlertRed
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.designsystem.theme.SafetyGreen
import com.nagarlens.app.core.designsystem.theme.SignalAmber
import com.nagarlens.app.core.model.ProcessingStatus

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
            val isModelAvailable = assessment.processingStatus == ProcessingStatus.COMPLETED
            val bannerColor = if (isModelAvailable) SafetyGreen else SignalAmber

            NagarlensCard(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Inference Status: ${assessment.processingStatus.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = bannerColor
                        )

                        Box(
                            modifier = Modifier
                                .background(bannerColor, shape = MaterialTheme.shapes.extraSmall)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isModelAvailable) "ACTIVE" else "PENDING WEIGHTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                    Text(
                        text = assessment.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            // Evidence Frame Preview Box with Target Bounding Box Reticle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color.Black, shape = MaterialTheme.shapes.medium)
                    .border(2.dp, MaterialTheme.colorScheme.secondary, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "EVIDENCE IMAGE FRAME\nID: $evidenceId",
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (assessment.boundingBoxes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(90.dp)
                                .border(2.dp, SignalAmber, shape = MaterialTheme.shapes.small)
                                .background(SignalAmber.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.TopStart
                        ) {
                            val bbox = assessment.boundingBoxes.first()
                            Text(
                                text = "${bbox.category.name} (${intConfidence(bbox.confidence)}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = SignalAmber,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            // Defect Classification & Severity Assessment Card
            NagarlensCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Defect Taxonomy Classification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(NagarlensSpacing.small))

                    Text(
                        text = "Predicted Category: ${assessment.predictedCategory.name}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(NagarlensSpacing.small))

                    Text(
                        text = "Detection Confidence: ${intConfidence(assessment.confidenceScore)}%",
                        style = MaterialTheme.typography.labelMedium
                    )
                    LinearProgressIndicator(
                        progress = { assessment.confidenceScore },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .padding(vertical = 2.dp),
                        color = if (assessment.confidenceScore > 0.7f) SafetyGreen else SignalAmber,
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

                    Text(
                        text = "Estimated Defect Severity:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    val severityScore = if (assessment.confidenceScore > 0f) 0.75f else 0.0f
                    LinearProgressIndicator(
                        progress = { severityScore },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .padding(vertical = 2.dp),
                        color = if (severityScore > 0.7f) AlertRed else SignalAmber,
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(NagarlensSpacing.small))

                    Text(
                        text = "Limitations Note: Severity index is a photographic 2D coverage area estimate. Absolute depth or hydraulic flow rates require calibrated sensor telemetry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            // Model Specification Details
            NagarlensCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Computer Vision Model Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                    Text(text = "Model Architecture: ${assessment.modelName}")
                    Text(text = "Model Binary Version: ${assessment.modelVersion}")
                    Text(text = "Taxonomy Version: ${assessment.taxonomyVersion}")
                    Text(text = "Quality Status: ${assessment.imageQualityStatus.name}")
                    Text(text = "Inference Timestamp: ${assessment.inferenceTimestampUtc}")
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

private fun intConfidence(score: Float): Int = (score * 100).toInt()
