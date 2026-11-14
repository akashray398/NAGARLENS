package com.nagarlens.app.feature.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nagarlens.app.core.designsystem.theme.AlertRed
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.designsystem.theme.SafetyGreen
import com.nagarlens.app.core.designsystem.theme.SignalAmber
import com.nagarlens.app.core.model.RiskLevel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Living City Theme Color Tokens
private val DarkCityBackground = Color(0xFF0B0F17)
private val HolographicCardBackground = Color(0xFF121824)
private val CyanAccent = Color(0xFF00F0FF)
private val ElectricBlue = Color(0xFF00B0FF)
private val CyanGlowBorder = Color(0xFF1E3A5F)
private val DarkCardBorder = Color(0xFF1A2638)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToMap: () -> Unit = {},
    onNavigateToCapture: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToIncidents: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCityBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = NagarlensSpacing.medium, vertical = NagarlensSpacing.small),
        verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.medium)
    ) {
        // Section 1 — Premium Top Header
        TopHeaderSection(
            locationBadge = uiState.locationBadge,
            tagline = uiState.tagline,
            onProfileClick = onNavigateToProfile
        )

        // Section 2 — Main Living City Engine Canvas Centerpiece
        LivingCityEngineCanvas(
            nodes = uiState.cityNodes,
            selectedNode = uiState.selectedMarkerNode,
            onNodeSelected = { viewModel.selectCityMarkerNode(it) },
            onNavigateToMap = onNavigateToMap,
            onNavigateToIncidents = onNavigateToIncidents
        )

        // Section 3 — City Intelligence Overview Grid
        CityIntelligenceOverviewSection(
            totalAssets = uiState.metrics?.totalAssets ?: 12480,
            healthyPercent = uiState.overallHealthPercent,
            alertCount = uiState.activeWarnings.size.coerceAtLeast(2),
            lastSyncText = "2 min"
        )

        // Section 4 — Infrastructure Status Gauge & Breakdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)
        ) {
            Box(modifier = Modifier.weight(1.1f)) {
                InfrastructureStatusGaugeCard(healthPercent = uiState.overallHealthPercent)
            }
            Box(modifier = Modifier.weight(0.9f)) {
                PriorityEarlyWarningCard(
                    onInvestigateClick = onNavigateToIncidents
                )
            }
        }

        // Section 5 — Quick Action Matrix (5 Glassmorphic Buttons)
        QuickActionMatrixSection(
            onCaptureClick = onNavigateToCapture,
            onExploreAssetsClick = onNavigateToMap,
            onViewInsightsClick = onNavigateToAnalytics,
            onInvestigateClick = onNavigateToIncidents,
            onAutomateActionsClick = onNavigateToIncidents
        )

        // Section 6 — City Insights / Real-time Event Feed
        CityInsightsEventFeedSection(insights = uiState.recentInsights)

        // Section 7 — System Operational Status Footnote
        SystemStatusFootnoteSection()

        // Inset padding for bottom navigation clearance
        Spacer(modifier = Modifier.height(72.dp))
    }

    // Selected Marker Node Information Dialog
    uiState.selectedMarkerNode?.let { node ->
        AlertDialog(
            onDismissRequest = { viewModel.selectCityMarkerNode(null) },
            containerColor = HolographicCardBackground,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = node.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (node.riskLevel) {
                                    RiskLevel.CRITICAL -> AlertRed
                                    RiskLevel.HIGH -> AlertRed
                                    RiskLevel.MODERATE -> SignalAmber
                                    else -> SafetyGreen
                                },
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${node.conditionScorePercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.extraSmall)) {
                    Text(
                        text = "Category: ${node.category.name} • ID: ${node.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccent
                    )
                    Text(
                        text = node.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.selectCityMarkerNode(null)
                        onNavigateToMap()
                    }
                ) {
                    Text("EXPLORE IN MAP", color = CyanAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.selectCityMarkerNode(null) }) {
                    Text("CLOSE", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
private fun TopHeaderSection(
    locationBadge: String,
    tagline: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "NAGARLENS",
                    style = MaterialTheme.typography.headlineLarge,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(NagarlensSpacing.small))
                // Live Location Badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E2A3A), shape = CircleShape)
                        .border(1.dp, CyanGlowBorder, shape = CircleShape)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(SafetyGreen, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = locationBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            Text(
                text = tagline,
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray,
                fontSize = 11.sp
            )
        }

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF1E2A3A), shape = CircleShape)
                .border(1.dp, CyanGlowBorder, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Auditor Profile",
                tint = CyanAccent,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun LivingCityEngineCanvas(
    nodes: List<CityMarkerNode>,
    selectedNode: CityMarkerNode?,
    onNodeSelected: (CityMarkerNode) -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToIncidents: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweepTransition")
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarAngle"
    )
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )

    var nodeOffsets by remember { mutableStateOf<List<Pair<CityMarkerNode, Offset>>>(emptyList()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF121A28), Color(0xFF0B101A))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(width = 1.5.dp, color = CyanGlowBorder, shape = RoundedCornerShape(16.dp))
    ) {
        // Holographic Living City Radar Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(nodes) {
                    detectTapGestures { tapOffset ->
                        val tapped = nodeOffsets.find { pair ->
                            val dx = pair.second.x - tapOffset.x
                            val dy = pair.second.y - tapOffset.y
                            sqrt(dx * dx + dy * dy) <= 35.0f
                        }
                        tapped?.let { onNodeSelected(it.first) }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2f, height / 2f)

            // 1. Perspective 3D Grid Lines
            val gridCols = 8
            val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

            for (i in 0..gridCols) {
                val x = width * (i / gridCols.toFloat())
                drawLine(
                    color = Color(0xFF1E2E44),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f,
                    pathEffect = dash
                )
            }
            val gridRows = 5
            for (j in 0..gridRows) {
                val y = height * (j / gridRows.toFloat())
                drawLine(
                    color = Color(0xFF1E2E44),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f,
                    pathEffect = dash
                )
            }

            // 2. Animated Holographic Expanding Scanning Ring
            val alphaPulse = (1.0f - (pulseRadius / 120f)).coerceIn(0f, 1f)
            drawCircle(
                color = CyanAccent.copy(alpha = alphaPulse * 0.4f),
                radius = pulseRadius,
                center = center,
                style = Stroke(width = 2f)
            )

            // 3. Rotating HUD Radar Sweep Line
            val rad = Math.toRadians(radarAngle.toDouble())
            val lineEnd = Offset(
                x = center.x + (160f * cos(rad)).toFloat(),
                y = center.y + (160f * sin(rad)).toFloat()
            )
            drawLine(
                color = CyanAccent.copy(alpha = 0.6f),
                start = center,
                end = lineEnd,
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )

            // 4. City Network Connections between Nodes
            val calculatedOffsets = nodes.map { n ->
                n to Offset(width * n.relativeX, height * n.relativeY)
            }

            for (i in 0 until calculatedOffsets.size - 1) {
                val p1 = calculatedOffsets[i].second
                val p2 = calculatedOffsets[i + 1].second
                drawLine(
                    color = ElectricBlue.copy(alpha = 0.35f),
                    start = p1,
                    end = p2,
                    strokeWidth = 1.5f,
                    pathEffect = dash
                )
            }

            // 5. Draw Interactive Infrastructure Node Pins
            calculatedOffsets.forEach { (node, offset) ->
                val isSelected = selectedNode?.id == node.id
                val nodeColor = when (node.riskLevel) {
                    RiskLevel.CRITICAL -> AlertRed
                    RiskLevel.HIGH -> AlertRed
                    RiskLevel.MODERATE -> SignalAmber
                    else -> SafetyGreen
                }

                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = 16f,
                        center = offset
                    )
                }

                drawCircle(
                    color = nodeColor,
                    radius = if (isSelected) 12f else 8f,
                    center = offset
                )
                drawCircle(
                    color = Color.Black,
                    radius = 3f,
                    center = offset
                )
            }

            nodeOffsets = calculatedOffsets
        }

        // Overlay Title Bar inside City Canvas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NagarlensSpacing.small)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "LIVING CITY ENGINE • RADAR HUD ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            }

            TextButton(onClick = onNavigateToMap) {
                Text("EXPLORE MAP", color = ElectricBlue, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }

        // Tap Hint Footnote
        Text(
            text = "TAP MARKER PIN TO INSPECT INFRASTRUCTURE NODE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = 9.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        )
    }
}

@Composable
private fun CityIntelligenceOverviewSection(
    totalAssets: Int,
    healthyPercent: Float,
    alertCount: Int,
    lastSyncText: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "City Intelligence Overview",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)
        ) {
            HudMetricCard(
                title = "Total Assets",
                value = "%,d".format(totalAssets),
                color = CyanAccent,
                modifier = Modifier.weight(1f)
            )
            HudMetricCard(
                title = "Healthy",
                value = "%.1f%%".format(healthyPercent),
                color = SafetyGreen,
                modifier = Modifier.weight(1f)
            )
            HudMetricCard(
                title = "Alerts",
                value = "$alertCount",
                color = SignalAmber,
                modifier = Modifier.weight(1f)
            )
            HudMetricCard(
                title = "Last Sync",
                value = lastSyncText,
                color = Color.LightGray,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HudMetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(HolographicCardBackground, shape = RoundedCornerShape(10.dp))
            .border(1.dp, DarkCardBorder, shape = RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color, fontSize = 15.sp)
        }
    }
}

@Composable
private fun InfrastructureStatusGaugeCard(healthPercent: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HolographicCardBackground, shape = RoundedCornerShape(12.dp))
            .border(1.dp, DarkCardBorder, shape = RoundedCornerShape(12.dp))
            .padding(NagarlensSpacing.small)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Infrastructure Status",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(NagarlensSpacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular Ring Chart Representation
                Box(
                    modifier = Modifier.size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color(0xFF1E2E44),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 8f)
                        )
                        drawArc(
                            color = SafetyGreen,
                            startAngle = -90f,
                            sweepAngle = 360f * (healthPercent / 100f),
                            useCenter = false,
                            style = Stroke(width = 8f, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "%.1f%%".format(healthPercent),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    StatusCheckRow("Roads", true)
                    StatusCheckRow("Bridges", true)
                    StatusCheckRow("Drainage", true)
                    StatusCheckRow("Utilities", true)
                }
            }
        }
    }
}

@Composable
private fun StatusCheckRow(label: String, isHealthy: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SafetyGreen,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.LightGray, fontSize = 10.sp)
    }
}

@Composable
private fun PriorityEarlyWarningCard(
    onInvestigateClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF2E1218), Color(0xFF1A0B10))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(1.dp, AlertRed.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp))
            .clickable { onInvestigateClick() }
            .padding(NagarlensSpacing.small)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AlertRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Priority Alert", style = MaterialTheme.typography.labelSmall, color = AlertRed, fontWeight = FontWeight.Bold)
                }
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Waterlogging Risk", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = "Central District Catchment", style = MaterialTheme.typography.labelSmall, color = Color.LightGray, fontSize = 10.sp)

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "HIGH RISK (78%) • Invert Silt Accumulation",
                style = MaterialTheme.typography.labelSmall,
                color = SignalAmber,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun QuickActionMatrixSection(
    onCaptureClick: () -> Unit,
    onExploreAssetsClick: () -> Unit,
    onViewInsightsClick: () -> Unit,
    onInvestigateClick: () -> Unit,
    onAutomateActionsClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Quick Actions", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(NagarlensSpacing.small),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                QuickActionButton("Capture\nEvidence", Icons.Default.Add, CyanAccent, onCaptureClick)
            }
            item {
                QuickActionButton("Explore\nAssets", Icons.Default.Search, ElectricBlue, onExploreAssetsClick)
            }
            item {
                QuickActionButton("View\nInsights", Icons.Default.Info, SafetyGreen, onViewInsightsClick)
            }
            item {
                QuickActionButton("Investigate\nIncidents", Icons.Default.Warning, SignalAmber, onInvestigateClick)
            }
            item {
                QuickActionButton("Automate\nActions", Icons.Default.Build, CyanAccent, onAutomateActionsClick)
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(96.dp)
            .height(84.dp)
            .background(HolographicCardBackground, shape = RoundedCornerShape(12.dp))
            .border(1.dp, DarkCardBorder, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun CityInsightsEventFeedSection(insights: List<CityInsightEvent>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "City Insights & Real-Time Events", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(NagarlensSpacing.extraSmall))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HolographicCardBackground, shape = RoundedCornerShape(12.dp))
                .border(1.dp, DarkCardBorder, shape = RoundedCornerShape(12.dp))
                .padding(NagarlensSpacing.small)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                insights.forEach { event ->
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
                                    .size(6.dp)
                                    .background(CyanAccent, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(text = event.title, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text(text = "${event.categoryText} • ${event.locationText}", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                            }
                        }
                        Text(text = event.timestampText, style = MaterialTheme.typography.labelSmall, color = CyanAccent, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemStatusFootnoteSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF12221A), shape = CircleShape)
                .border(1.dp, SafetyGreen.copy(alpha = 0.6f), shape = CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(SafetyGreen, shape = CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "System Operational — All systems normal",
                    style = MaterialTheme.typography.labelSmall,
                    color = SafetyGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}
