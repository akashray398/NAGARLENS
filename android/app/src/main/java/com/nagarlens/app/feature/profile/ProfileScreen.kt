package com.nagarlens.app.feature.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nagarlens.app.core.designsystem.component.NagarlensPrimaryButton
import com.nagarlens.app.core.designsystem.component.NagarlensSecondaryButton
import com.nagarlens.app.core.designsystem.theme.AlertRed
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.designsystem.theme.SafetyGreen
import com.nagarlens.app.core.designsystem.theme.SignalAmber

private val DarkCityBackground = Color(0xFF0B0F17)
private val HolographicCardBackground = Color(0xFF121824)
private val CyanAccent = Color(0xFF00F0FF)
private val ElectricBlue = Color(0xFF00B0FF)
private val CyanGlowBorder = Color(0xFF1E3A5F)
private val DarkCardBorder = Color(0xFF1A2638)

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRoleLoginDialogOpen by remember { mutableStateOf(false) }
    var isRegisterAccountDialogOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCityBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = NagarlensSpacing.medium, vertical = NagarlensSpacing.small),
        verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.medium)
    ) {
        // 1. Role Persona Switcher Chips Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE PERSONA ACCOUNT",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { isRoleLoginDialogOpen = true }) {
                        Text("LOGIN ROLE", color = ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { isRegisterAccountDialogOpen = true }) {
                        Text("+ NEW USER", color = SafetyGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(UserRolePersona.entries) { persona ->
                    val isSelected = uiState.activeRolePersona == persona
                    val labelText = when (persona) {
                        UserRolePersona.CITIZEN_USER -> "CITIZEN"
                        UserRolePersona.FIELD_INSPECTOR -> "INSPECTOR"
                        UserRolePersona.REVIEWER_ADMIN -> "REVIEWER"
                        UserRolePersona.SYSTEM_ADMINISTRATOR -> "SYS ADMIN (YOU)"
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.switchRolePersona(persona) },
                        label = { Text(labelText, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = Color.White,
                            containerColor = HolographicCardBackground,
                            labelColor = Color.LightGray
                        )
                    )
                }
            }
        }

        // 2. Clean Executive Identity Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HolographicCardBackground, shape = RoundedCornerShape(14.dp))
                .border(1.dp, CyanGlowBorder, shape = RoundedCornerShape(14.dp))
                .padding(NagarlensSpacing.medium)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                                contentDescription = "Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(NagarlensSpacing.small))
                        Column {
                            Text(
                                text = uiState.inspectorName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = uiState.officialDesignation,
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanAccent,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(SafetyGreen, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "VERIFIED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                Text(
                    text = "Email: ${uiState.userEmail} • Token: ${uiState.badgeTokenId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Text(
                    text = "Jurisdiction: ${uiState.assignedWard}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
                Text(
                    text = "RBAC Clearance: ${uiState.securityRole}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SignalAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }

        // 3. Persona Capability Panel
        PersonaFeaturesPanel(
            persona = uiState.activeRolePersona,
            apiEndpointUrl = uiState.apiEndpointUrl,
            aiModelSpec = uiState.selectedAiModel,
            onExportAuditLog = { viewModel.exportAuditLog() }
        )

        // 4. Real-Time User Activity Journey Inspector (For System Admin YOU)
        if (uiState.activeRolePersona == UserRolePersona.SYSTEM_ADMINISTRATOR) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF162232), Color(0xFF0F1824))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(1.5.dp, CyanAccent, shape = RoundedCornerShape(12.dp))
                    .padding(NagarlensSpacing.small)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REAL-TIME USER JOURNEY INSPECTOR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                        Box(
                            modifier = Modifier
                                .background(SafetyGreen, shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("LIVE", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Inspect real-time field journeys and evidence capture events in the network:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.liveJourneysStream) { journey ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0B121C), shape = RoundedCornerShape(6.dp))
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = journey.actorName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF1E2F45), shape = RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(text = journey.roleBadge, style = MaterialTheme.typography.labelSmall, color = CyanAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(text = journey.actionDescription, style = MaterialTheme.typography.labelSmall, color = Color.LightGray, fontSize = 10.sp)
                                }
                                Text(text = journey.timestampText, style = MaterialTheme.typography.labelSmall, color = ElectricBlue, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        // 5. System Preferences & Network Toggles
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HolographicCardBackground, shape = RoundedCornerShape(12.dp))
                .border(1.dp, DarkCardBorder, shape = RoundedCornerShape(12.dp))
                .padding(NagarlensSpacing.small)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "System Preferences",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(4.dp))
                TogglePreferenceRow(
                    title = "Low Data Mode (3G/4G)",
                    subtitle = "Compress field imagery before network sync",
                    isChecked = uiState.isLowDataCompressionEnabled,
                    onCheckedChange = { viewModel.toggleLowDataCompression(it) }
                )
                TogglePreferenceRow(
                    title = "Geofenced Risk Warnings",
                    subtitle = "Receive push alerts for high-risk catchments",
                    isChecked = uiState.isEarlyWarningPushEnabled,
                    onCheckedChange = { viewModel.toggleEarlyWarningPush(it) }
                )
                TogglePreferenceRow(
                    title = "AI Privacy PII Auto-Redact",
                    subtitle = "Automatically blur bystander faces & plates",
                    isChecked = uiState.isPiiPrivacyBlurActive,
                    onCheckedChange = { viewModel.togglePiiPrivacyBlur(it) }
                )
            }
        }

        // 6. Offline Storage & Sync Operations
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HolographicCardBackground, shape = RoundedCornerShape(12.dp))
                .border(1.dp, DarkCardBorder, shape = RoundedCornerShape(12.dp))
                .padding(NagarlensSpacing.small)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Offline Field Storage & Cache",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Pending Upload Queue:", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                    Text(text = "${uiState.queuedObservationCount} Records", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SignalAmber)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Spatial Vector Cache:", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                    Text(text = "%.1f MB".format(uiState.encryptedCacheSizeMb), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
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

        // 7. Security Session Governance & Sign Out
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HolographicCardBackground, shape = RoundedCornerShape(12.dp))
                .border(1.dp, AlertRed.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
                .padding(NagarlensSpacing.small)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Security Session",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AlertRed
                )
                Text(
                    text = "Signing out clears active JWT session tokens permanently.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(NagarlensSpacing.small))
                NagarlensSecondaryButton(
                    onClick = { viewModel.logoutSession() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "SIGN OUT / ZERO TOKENS", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Version Footnote
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
                color = Color.Gray,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(88.dp))
    }

    // Role Login Dialog
    if (isRoleLoginDialogOpen) {
        AlertDialog(
            onDismissRequest = { isRoleLoginDialogOpen = false },
            containerColor = HolographicCardBackground,
            title = {
                Text(
                    text = "Select Account Role",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)) {
                    RoleSelectCard(
                        title = "👑 System Administrator (YOU)",
                        description = "Full RBAC control, real-time user journey inspector, DB migrations, audit logs.",
                        color = CyanAccent,
                        onClick = {
                            viewModel.loginRealTimeUser("admin", "admin_pass", UserRolePersona.SYSTEM_ADMINISTRATOR)
                            isRoleLoginDialogOpen = false
                        }
                    )
                    RoleSelectCard(
                        title = "🛡️ Municipal Reviewer",
                        description = "Asset verification (APPROVE/REJECT), work order dispatch.",
                        color = SignalAmber,
                        onClick = {
                            viewModel.loginRealTimeUser("reviewer", "reviewer_pass", UserRolePersona.REVIEWER_ADMIN)
                            isRoleLoginDialogOpen = false
                        }
                    )
                    RoleSelectCard(
                        title = "👷 Field Auditor / Inspector",
                        description = "CameraX capture, SHA-256 digests, offline Room queue.",
                        color = SafetyGreen,
                        onClick = {
                            viewModel.loginRealTimeUser("auditor_central", "inspector_pass", UserRolePersona.FIELD_INSPECTOR)
                            isRoleLoginDialogOpen = false
                        }
                    )
                    RoleSelectCard(
                        title = "👤 Citizen User",
                        description = "Report local waterlogging defects, track status, view alerts.",
                        color = Color.LightGray,
                        onClick = {
                            viewModel.loginRealTimeUser("citizen", "citizen_pass", UserRolePersona.CITIZEN_USER)
                            isRoleLoginDialogOpen = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { isRoleLoginDialogOpen = false }) {
                    Text("CLOSE", color = Color.Gray)
                }
            }
        )
    }

    // Register Account Dialog
    if (isRegisterAccountDialogOpen) {
        var newUsername by remember { mutableStateOf("") }
        var newEmail by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var newWard by remember { mutableStateOf("Ward 12 — Bangalore Central") }
        var selectedRolePersona by remember { mutableStateOf(UserRolePersona.FIELD_INSPECTOR) }

        AlertDialog(
            onDismissRequest = { isRegisterAccountDialogOpen = false },
            containerColor = HolographicCardBackground,
            title = {
                Text(
                    text = "Register Permanent User Account",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.extraSmall)) {
                    Text("Enter registration details (info saves permanently):", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)

                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("Username / Badge ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newWard,
                        onValueChange = { newWard = it },
                        label = { Text("Assigned Ward / City") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                NagarlensPrimaryButton(
                    onClick = {
                        if (newUsername.isNotBlank() && newEmail.isNotBlank()) {
                            viewModel.registerNewRealTimeAccount(
                                username = newUsername,
                                email = newEmail,
                                password = newPassword.ifBlank { "password" },
                                ward = newWard,
                                persona = selectedRolePersona
                            )
                            isRegisterAccountDialogOpen = false
                        }
                    }
                ) {
                    Text("REGISTER & SAVE ACCOUNT")
                }
            },
            dismissButton = {
                TextButton(onClick = { isRegisterAccountDialogOpen = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }

    // Action Notice Dialog
    uiState.actionNotice?.let { notice ->
        AlertDialog(
            onDismissRequest = { viewModel.clearActionNotice() },
            containerColor = HolographicCardBackground,
            title = { Text("System Operations Notice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White) },
            text = { Text(notice, color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearActionNotice() }) {
                    Text("OK", color = CyanAccent, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun RoleSelectCard(
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E2A3A), shape = RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(NagarlensSpacing.small)
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, style = MaterialTheme.typography.labelSmall, color = Color.LightGray, fontSize = 10.sp)
        }
    }
}

@Composable
private fun PersonaFeaturesPanel(
    persona: UserRolePersona,
    apiEndpointUrl: String,
    aiModelSpec: String,
    onExportAuditLog: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HolographicCardBackground, shape = RoundedCornerShape(12.dp))
            .border(1.dp, CyanGlowBorder, shape = RoundedCornerShape(12.dp))
            .padding(NagarlensSpacing.small)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = when (persona) {
                    UserRolePersona.CITIZEN_USER -> "CITIZEN USER CAPABILITIES"
                    UserRolePersona.FIELD_INSPECTOR -> "FIELD AUDITOR CAPABILITIES"
                    UserRolePersona.REVIEWER_ADMIN -> "REVIEWER & MUNICIPAL ADMIN CAPABILITIES"
                    UserRolePersona.SYSTEM_ADMINISTRATOR -> "SYSTEM ADMINISTRATOR COMMAND TOOLS (YOU)"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )

            Spacer(modifier = Modifier.height(4.dp))

            when (persona) {
                UserRolePersona.CITIZEN_USER -> {
                    FeatureCheckBullet("Report Local Drainage Defect & Waterlogging")
                    FeatureCheckBullet("View Geofenced Community Early Warning Alerts")
                    FeatureCheckBullet("Track Submitted Observation Verification Status")
                }
                UserRolePersona.FIELD_INSPECTOR -> {
                    FeatureCheckBullet("CameraX Reality Capture & SHA-256 Digest Engine")
                    FeatureCheckBullet("Offline Room v5 Database Ingestion & Retry Queue")
                    FeatureCheckBullet("Fused Location Provider Precision Telemetry Lock")
                }
                UserRolePersona.REVIEWER_ADMIN -> {
                    FeatureCheckBullet("Submit Asset Verification Decisions (APPROVED / REJECTED)")
                    FeatureCheckBullet("14-State Investigation Command & Work Order Dispatch")
                    FeatureCheckBullet("Human-in-the-Loop Predictive Risk Score Overrides")
                }
                UserRolePersona.SYSTEM_ADMINISTRATOR -> {
                    FeatureCheckBullet("API Gateway URL: $apiEndpointUrl")
                    FeatureCheckBullet("Active Vision AI Model: $aiModelSpec")
                    FeatureCheckBullet("PostGIS Spatial Schema v5 & Alembic Async Migrations")
                    Spacer(modifier = Modifier.height(4.dp))
                    NagarlensSecondaryButton(
                        onClick = onExportAuditLog,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("EXPORT SHA-256 AUDIT CHAIN LOG", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCheckBullet(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
private fun TelemetryRow(label: String, status: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
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
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.LightGray, fontSize = 11.sp)
        }
        Text(text = status, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }
}

@Composable
private fun TogglePreferenceRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CyanAccent,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF1E2E44)
            )
        )
    }
}
