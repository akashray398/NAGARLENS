package com.nagarlens.app.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nagarlens.app.core.designsystem.component.NagarlensSectionHeader
import com.nagarlens.app.core.designsystem.theme.AlertRed
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.designsystem.theme.SafetyGreen
import com.nagarlens.app.core.designsystem.theme.SignalAmber
import com.nagarlens.app.core.model.NotificationSeverity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Living City Dark Theme Colors
private val DarkCityBackground = Color(0xFF0B0F17)
private val HolographicCardBackground = Color(0xFF121824)
private val CyanAccent = Color(0xFF00F0FF)
private val ElectricBlue = Color(0xFF00B0FF)
private val CyanGlowBorder = Color(0xFF1E3A5F)
private val DarkCardBorder = Color(0xFF1A2638)

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel(),
    onNavigateToAsset: (String) -> Unit = {},
    onNavigateToIncident: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm 'UTC'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    val filteredNotifications = uiState.notifications.filter { item ->
        when (uiState.selectedFilter) {
            "UNREAD" -> !item.isRead
            "CRITICAL" -> item.severity == NotificationSeverity.CRITICAL
            "WARNING" -> item.severity == NotificationSeverity.WARNING
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCityBackground)
            .padding(horizontal = NagarlensSpacing.medium, vertical = NagarlensSpacing.small),
        verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)
    ) {
        NagarlensSectionHeader(title = "Alerts & System Notifications")

        // Top Header Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "STATUS FEED",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                if (uiState.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(SignalAmber, shape = CircleShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${uiState.unreadCount} UNREAD",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            if (uiState.unreadCount > 0) {
                TextButton(onClick = { viewModel.markAllAsRead() }) {
                    Text("MARK ALL READ", color = ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(uiState.activeFilters) { filter ->
                FilterChip(
                    selected = uiState.selectedFilter == filter,
                    onClick = { viewModel.selectFilter(filter) },
                    label = { Text(filter, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = Color.White,
                        containerColor = HolographicCardBackground,
                        labelColor = Color.LightGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        if (filteredNotifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                    Text(
                        text = "No system alerts or notifications matching filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredNotifications) { item ->
                    val severityColor = when (item.severity) {
                        NotificationSeverity.CRITICAL -> AlertRed
                        NotificationSeverity.WARNING -> SignalAmber
                        else -> SafetyGreen
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (!item.isRead) Color(0xFF141D2B) else HolographicCardBackground,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = if (!item.isRead) 1.5.dp else 1.dp,
                                color = if (!item.isRead) CyanGlowBorder else DarkCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                viewModel.markAsRead(item.id)
                                item.relatedAssetId?.let { onNavigateToAsset(it) }
                                item.relatedIncidentId?.let { onNavigateToIncident(it) }
                            }
                            .padding(NagarlensSpacing.small)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = when (item.severity) {
                                            NotificationSeverity.CRITICAL -> Icons.Default.Warning
                                            NotificationSeverity.WARNING -> Icons.Default.Warning
                                            else -> Icons.Default.Info
                                        },
                                        contentDescription = null,
                                        tint = severityColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(NagarlensSpacing.small))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (!item.isRead) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(CyanAccent, shape = CircleShape)
                                        )
                                    }
                                }

                                Text(
                                    text = dateFormat.format(Date(item.timestampUtc)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontSize = 9.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))

                            Text(
                                text = item.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )

                            // Related Navigation Action Link Indicator
                            if (item.relatedAssetId != null || item.relatedIncidentId != null) {
                                Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (item.relatedIncidentId != null) "INVESTIGATE WORK ORDER" else "EXPLORE DIGITAL TWIN",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Inset padding for bottom navigation bar clearance
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}
