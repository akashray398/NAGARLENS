package com.nagarlens.app.feature.notifications

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
import androidx.compose.foundation.lazy.items
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
import com.nagarlens.app.core.designsystem.component.NagarlensSectionHeader
import com.nagarlens.app.core.designsystem.theme.AlertRed
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.designsystem.theme.SafetyGreen
import com.nagarlens.app.core.designsystem.theme.SignalAmber
import com.nagarlens.app.core.model.NotificationSeverity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel(),
    onNavigateToAsset: (String) -> Unit = {},
    onNavigateToIncident: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(NagarlensSpacing.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NagarlensSectionHeader(title = "Alerts & Notifications")
            if (uiState.unreadCount > 0) {
                TextButton(onClick = { viewModel.markAllAsRead() }) {
                    Text("MARK ALL READ (${uiState.unreadCount})")
                }
            }
        }

        if (uiState.notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No system alerts or notifications.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.notifications) { item ->
                    val color = when (item.severity) {
                        NotificationSeverity.CRITICAL -> AlertRed
                        NotificationSeverity.WARNING -> SignalAmber
                        else -> SafetyGreen
                    }

                    NagarlensCard(
                        modifier = Modifier.clickable {
                            viewModel.markAsRead(item.id)
                            item.relatedAssetId?.let { onNavigateToAsset(it) }
                            item.relatedIncidentId?.let { onNavigateToIncident(it) }
                        }
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isRead) MaterialTheme.colorScheme.onSurfaceVariant else color
                                )

                                Text(
                                    text = dateFormat.format(Date(item.timestampUtc)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))

                            Text(
                                text = item.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
