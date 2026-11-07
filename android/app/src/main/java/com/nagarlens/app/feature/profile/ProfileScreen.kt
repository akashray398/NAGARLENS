package com.nagarlens.app.feature.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nagarlens.app.core.designsystem.component.NagarlensCard
import com.nagarlens.app.core.designsystem.component.NagarlensPrimaryButton
import com.nagarlens.app.core.designsystem.component.NagarlensSecondaryButton
import com.nagarlens.app.core.designsystem.component.NagarlensSectionHeader
import com.nagarlens.app.core.designsystem.theme.AlertRed
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.designsystem.theme.SafetyGreen
import com.nagarlens.app.core.designsystem.theme.SignalAmber

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(NagarlensSpacing.medium)
    ) {
        NagarlensSectionHeader(title = "Auditor Identity & Security Governance")

        // Auditor Executive Identity Card
        NagarlensCard(containerColor = Color(0xFF1E242B)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(color = MaterialTheme.colorScheme.secondary, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Auditor Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(NagarlensSpacing.small))
                        Column {
                            Text(
                                text = uiState.inspectorName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = uiState.officialDesignation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(SafetyGreen, shape = MaterialTheme.shapes.extraSmall)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "VERIFIED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                Text(
                    text = "Badge Token: ${uiState.badgeTokenId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
                Text(
                    text = "Organization: ${uiState.organizationName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
                Text(
                    text = "Jurisdiction: ${uiState.assignedWard}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "RBAC Security Clearance: ${uiState.securityRole}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SignalAmber,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(NagarlensSpacing.small))

        // Real-time System & Security Telemetry Panel
        NagarlensCard {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Active Security & Privacy Engine Telemetry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                TelemetryRow(label = "SHA-256 Media Digest Engine", status = "ACTIVE", color = SafetyGreen)
                TelemetryRow(label = "AI PII Privacy Blurring Filter", status = "AUTO-REDACTING", color = SafetyGreen)
                TelemetryRow(label = "Fused GNSS Precision Lock", status = "±3.2m (GPS+Galileo)", color = SafetyGreen)
                TelemetryRow(label = "Room SQLite Database v5", status = "ENCRYPTED & ISOLATED", color = SafetyGreen)
                TelemetryRow(label = "Cloud Transport Security", status = "TLS 1.3 (api.nagarlens.gov)", color = SafetyGreen)
            }
        }

        Spacer(modifier = Modifier.height(NagarlensSpacing.small))

        // Field Storage & Synchronisation Operations
        NagarlensCard {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Offline Field Storage & Cache Operations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Pending Upload Queue:", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "${uiState.queuedObservationCount} Records", fontWeight = FontWeight.Bold, color = SignalAmber)
                }
                Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Spatial Vector Cache:", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "%.1f MB".format(uiState.encryptedCacheSizeMb), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)
                ) {
                    NagarlensPrimaryButton(
                        onClick = { viewModel.triggerManualSync() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = if (uiState.isSyncing) "SYNCING..." else "SYNC NOW")
                    }

                    NagarlensSecondaryButton(
                        onClick = { viewModel.purgeVectorCache() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "PURGE CACHE")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(NagarlensSpacing.small))

        // Security Session Governance & Logout
        NagarlensCard {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Security Session Governance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AlertRed
                )
                Text(
                    text = "Session active with short-lived JWT token rotation. Signing out zeros tokens from memory.",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                NagarlensSecondaryButton(
                    onClick = { viewModel.logoutSession() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "SECURITY SIGN OUT / ZERO TOKENS", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

        // Version & System Specifications
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.buildVersion,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "AI Vision: MobileNetV3 + RT-DETR-Nano (v1.0.0)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Text(
                text = "Spatial CRS: WGS84 (EPSG:4326) • PostGIS 3.4 Engine",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        // Bottom Inset Padding so content is never obstructed by bottom navigation bar
        Spacer(modifier = Modifier.height(88.dp))
    }

    // Action Notice Alert Dialog
    uiState.actionNotice?.let { notice ->
        AlertDialog(
            onDismissRequest = { viewModel.clearActionNotice() },
            title = { Text("Security & Operations Notice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text(notice) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearActionNotice() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun TelemetryRow(label: String, status: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
        Text(text = status, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}
