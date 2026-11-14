package com.nagarlens.app.core.designsystem.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nagarlens.app.core.designsystem.theme.SafetyGreen
import kotlin.math.cos
import kotlin.math.sin

object NagarlensMotionTokens {
    const val FastInteractionMs = 150
    const val StandardTransitionMs = 250
    const val ExpressiveTransitionMs = 450
    const val RadarScanDurationMs = 5000
    const val LogoRevealDurationMs = 1800

    val EmphasizedEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val StandardEasing: Easing = FastOutSlowInEasing
    val ExitEasing: Easing = LinearOutSlowInEasing
}

@Composable
fun NagarlensRadarPulse(
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF00D9FF)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulseTransition")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 140f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = NagarlensMotionTokens.RadarScanDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val alpha = (1.0f - (pulseRadius / 140f)).coerceIn(0f, 1f)

        drawCircle(
            color = accentColor.copy(alpha = alpha * 0.45f),
            radius = pulseRadius,
            center = center,
            style = Stroke(width = 2f)
        )
    }
}

@Composable
fun NagarlensScanningLine(
    modifier: Modifier = Modifier,
    scanColor: Color = Color(0xFF00D9FF)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ScanningLineTransition")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val rad = Math.toRadians(sweepAngle.toDouble())
        val end = Offset(
            x = center.x + (size.width / 2f * cos(rad)).toFloat(),
            y = center.y + (size.height / 2f * sin(rad)).toFloat()
        )

        drawLine(
            color = scanColor.copy(alpha = 0.6f),
            start = center,
            end = end,
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun NagarlensGlowMarker(
    modifier: Modifier = Modifier,
    markerColor: Color = Color(0xFF00D9FF)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GlowMarkerTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
            color = markerColor.copy(alpha = 0.35f),
            radius = (size.width / 2f) * pulseScale,
            center = center
        )
        drawCircle(
            color = markerColor,
            radius = size.width / 4f,
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = size.width / 8f,
            center = center
        )
    }
}

@Composable
fun NagarlensStatusIndicator(
    statusText: String,
    statusColor: Color = SafetyGreen,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF162436), shape = CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(statusColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}
