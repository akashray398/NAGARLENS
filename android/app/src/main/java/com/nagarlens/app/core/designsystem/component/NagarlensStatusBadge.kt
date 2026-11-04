package com.nagarlens.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nagarlens.app.core.designsystem.theme.AlertRed
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.designsystem.theme.SafetyGreen
import com.nagarlens.app.core.designsystem.theme.SignalAmber

enum class AssetConditionStatus(val label: String) {
    NORMAL_FLOW("CLEAR FLOW"),
    PARTIAL_SILTATION("MODERATE BLOCK"),
    CRITICAL_BLOCKAGE("CRITICAL ALERT"),
    UNVERIFIED("UNVERIFIED EVIDENCE")
}

@Composable
fun NagarlensStatusBadge(
    status: AssetConditionStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        AssetConditionStatus.NORMAL_FLOW -> SafetyGreen to Color.White
        AssetConditionStatus.PARTIAL_SILTATION -> SignalAmber to Color.Black
        AssetConditionStatus.CRITICAL_BLOCKAGE -> AlertRed to Color.White
        AssetConditionStatus.UNVERIFIED -> Color.Gray to Color.White
    }

    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = MaterialTheme.shapes.small)
            .padding(horizontal = NagarlensSpacing.small, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}
