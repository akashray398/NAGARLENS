package com.nagarlens.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.designsystem.theme.SafetyGreen
import com.nagarlens.app.core.designsystem.theme.SignalAmber

private val TopBarBackground = Color(0xFF121824)
private val BorderColor = Color(0xFF1E2C3D)
private val CyanAccent = Color(0xFF00F0FF)

@Composable
fun NagarlensTopAppBar(
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TopBarBackground)
            .border(width = 1.dp, color = BorderColor)
            .statusBarsPadding()
            .padding(horizontal = NagarlensSpacing.medium, vertical = NagarlensSpacing.small)
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
                        style = MaterialTheme.typography.titleLarge,
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp
                    )
                    Spacer(modifier = Modifier.width(NagarlensSpacing.small))
                    // Online Status Badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1A2638), shape = RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF26384D), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(SafetyGreen, shape = CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ONLINE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = "Infrastructure Intelligence Network",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }

            // Top Right Notifications Bell Action Item
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.size(36.dp)
            ) {
                Box {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                    // Notification Alert Dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(SignalAmber, shape = CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}
