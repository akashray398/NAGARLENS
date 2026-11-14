package com.nagarlens.app.feature.intro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import kotlin.math.cos
import kotlin.math.sin

// Living City Brand Colors
private val MidnightNavy = Color(0xFF061426)
private val DeepNavy = Color(0xFF0A1D32)
private val ElectricCyan = Color(0xFF00D9FF)
private val IntelligenceBlue = Color(0xFF168BFF)
private val TealHighlight = Color(0xFF00D6B5)
private val SoftWhite = Color(0xFFF2F8FC)

@Composable
fun LivingCityIntroScreen(
    onIntroFinished: () -> Unit
) {
    var animationStage by remember { mutableIntStateOf(1) } // 1: Awakening, 2: Radar, 3: City, 4: Logo Reveal

    val logoAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val skylineAlpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "IntroRadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )
    val radarPulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarPulseRadius"
    )

    // Coordinated Animation Sequence Storyboard
    LaunchedEffect(Unit) {
        // Scene 1: Awakening (0 - 300ms)
        animationStage = 1

        // Scene 2: Radar Activation (300 - 800ms)
        kotlinx.coroutines.delay(300)
        animationStage = 2

        // Scene 3: City Formation (800 - 1300ms)
        kotlinx.coroutines.delay(500)
        animationStage = 3
        skylineAlpha.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))

        // Scene 4: NAGARLENS Logo Reveal & Tagline (1300 - 1800ms)
        animationStage = 4
        logoAlpha.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        taglineAlpha.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))

        // Hold reveal & Transition to Living City Home (at ~2400ms)
        kotlinx.coroutines.delay(900)
        onIntroFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MidnightNavy, DeepNavy)
                )
            )
    ) {
        // Canvas — Radar Sweep & City Skyline Animation
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2f, height / 2.2f)

            // 1. Perspective Grid Lines (Awakening)
            val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            for (i in 0..6) {
                val y = height * (0.4f + (i * 0.08f))
                drawLine(
                    color = Color(0xFF0E2B4A),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f,
                    pathEffect = dash
                )
            }

            // 2. Animated Expanding Radar Rings (Radar Activation)
            if (animationStage >= 2) {
                val alpha = (1.0f - (radarPulseRadius / 180f)).coerceIn(0f, 1f)
                drawCircle(
                    color = ElectricCyan.copy(alpha = alpha * 0.5f),
                    radius = radarPulseRadius * 1.5f,
                    center = center,
                    style = Stroke(width = 2f)
                )

                drawCircle(
                    color = TealHighlight.copy(alpha = 0.3f),
                    radius = 90f,
                    center = center,
                    style = Stroke(width = 1.5f)
                )

                // Sweep Line
                val rad = Math.toRadians(sweepAngle.toDouble())
                val cosVal = cos(rad).toFloat()
                val sinVal = sin(rad).toFloat()
                val end = Offset(
                    x = center.x + (200f * cosVal),
                    y = center.y + (200f * sinVal)
                )
                drawLine(
                    color = ElectricCyan.copy(alpha = 0.7f),
                    start = center,
                    end = end,
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }

            // 3. Illuminated Futuristic City Skyline Silhouette (City Formation)
            if (animationStage >= 3) {
                val opacity = skylineAlpha.value

                // Building 1
                drawRoundRect(
                    color = IntelligenceBlue.copy(alpha = opacity * 0.6f),
                    topLeft = Offset(width * 0.12f, height * 0.42f),
                    size = Size(width * 0.14f, height * 0.22f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Building 2 (Tower)
                drawRoundRect(
                    color = ElectricCyan.copy(alpha = opacity * 0.8f),
                    topLeft = Offset(width * 0.28f, height * 0.32f),
                    size = Size(width * 0.12f, height * 0.32f),
                    cornerRadius = CornerRadius(6f, 6f),
                    style = Stroke(width = 1.5f)
                )
                // Building 3 (Center High-Rise)
                drawRoundRect(
                    color = TealHighlight.copy(alpha = opacity * 0.85f),
                    topLeft = Offset(width * 0.44f, height * 0.26f),
                    size = Size(width * 0.14f, height * 0.38f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Building 4
                drawRoundRect(
                    color = IntelligenceBlue.copy(alpha = opacity * 0.6f),
                    topLeft = Offset(width * 0.60f, height * 0.36f),
                    size = Size(width * 0.13f, height * 0.28f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Building 5
                drawRoundRect(
                    color = ElectricCyan.copy(alpha = opacity * 0.5f),
                    topLeft = Offset(width * 0.75f, height * 0.44f),
                    size = Size(width * 0.13f, height * 0.20f),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // Network Connections between Building Tops
                drawLine(
                    color = ElectricCyan.copy(alpha = opacity * 0.4f),
                    start = Offset(width * 0.19f, height * 0.42f),
                    end = Offset(width * 0.34f, height * 0.32f),
                    strokeWidth = 1.5f,
                    pathEffect = dash
                )
                drawLine(
                    color = TealHighlight.copy(alpha = opacity * 0.4f),
                    start = Offset(width * 0.34f, height * 0.32f),
                    end = Offset(width * 0.51f, height * 0.26f),
                    strokeWidth = 1.5f,
                    pathEffect = dash
                )
                drawLine(
                    color = ElectricCyan.copy(alpha = opacity * 0.4f),
                    start = Offset(width * 0.51f, height * 0.26f),
                    end = Offset(width * 0.66f, height * 0.36f),
                    strokeWidth = 1.5f,
                    pathEffect = dash
                )
            }
        }

        // Top Right Skip Button
        TextButton(
            onClick = onIntroFinished,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
        ) {
            Text("SKIP INTRO", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // Scene 4 — NAGARLENS Wordmark & Tagline Reveal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand Logo Wordmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "NAGAR",
                    style = MaterialTheme.typography.displayMedium,
                    color = SoftWhite.copy(alpha = logoAlpha.value),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "LENS",
                    style = MaterialTheme.typography.displayMedium,
                    color = ElectricCyan.copy(alpha = logoAlpha.value),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
            }

            // Category Subtitle
            Text(
                text = "INFRASTRUCTURE INTELLIGENCE NETWORK",
                style = MaterialTheme.typography.labelSmall,
                color = TealHighlight.copy(alpha = logoAlpha.value),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(NagarlensSpacing.small))

            // Tagline Reveal
            Text(
                text = "\"Giving the physical world a living memory.\"",
                style = MaterialTheme.typography.bodyLarge,
                color = SoftWhite.copy(alpha = taglineAlpha.value),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}
