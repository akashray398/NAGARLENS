package com.nagarlens.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.nagarlens.app.core.designsystem.component.NagarlensCard
import com.nagarlens.app.core.designsystem.component.NagarlensPrimaryButton
import com.nagarlens.app.core.designsystem.component.NagarlensSecondaryButton
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing

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
            .padding(NagarlensSpacing.medium),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Hero Brand & Product Identity Section
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

                Spacer(modifier = Modifier.height(NagarlensSpacing.small))

                Text(
                    text = "\"${uiState.tagline}\"",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.large))

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
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

            // Operational Field Quick Actions
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
