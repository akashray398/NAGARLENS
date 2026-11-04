package com.nagarlens.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.navigation.NagarlensDestination

@Composable
fun NagarlensBottomNavigationBar(
    currentRoute: String?,
    onNavigateToDestination: (NagarlensDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val leftDestinations = listOf(
        NagarlensDestination.HOME,
        NagarlensDestination.MAP
    )
    val rightDestinations = listOf(
        NagarlensDestination.ACTIVITY,
        NagarlensDestination.PROFILE
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        // Bottom Bar Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Group: Home, Map
            leftDestinations.forEach { destination ->
                val isSelected = currentRoute == destination.route
                NavIconButton(
                    destination = destination,
                    isSelected = isSelected,
                    onClick = { onNavigateToDestination(destination) }
                )
            }

            // Spacer gap for central Capture action button
            Box(modifier = Modifier.size(56.dp))

            // Right Group: Activity, Profile
            rightDestinations.forEach { destination ->
                val isSelected = currentRoute == destination.route
                NavIconButton(
                    destination = destination,
                    isSelected = isSelected,
                    onClick = { onNavigateToDestination(destination) }
                )
            }
        }

        // Prominent Central Capture Floating Action Button
        FloatingActionButton(
            onClick = { onNavigateToDestination(NagarlensDestination.CAPTURE) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
                .size(64.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = NagarlensDestination.CAPTURE.title,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun NavIconButton(
    destination: NagarlensDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    IconButton(onClick = onClick) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = destination.title,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
