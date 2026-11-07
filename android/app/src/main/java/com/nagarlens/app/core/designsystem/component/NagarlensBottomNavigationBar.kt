package com.nagarlens.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        NagarlensDestination.INCIDENTS,
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
                .height(64.dp)
                .background(Color(0xFF1E242B))
                .border(width = 1.dp, color = Color(0xFF2A323D))
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly,
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
            Box(modifier = Modifier.width(56.dp))

            // Right Group: Incidents, Profile
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
                .offset(y = (-14).dp)
                .size(56.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = NagarlensDestination.CAPTURE.title,
                modifier = Modifier.size(30.dp)
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
    val color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.LightGray

    IconButton(
        onClick = onClick,
        modifier = Modifier.width(72.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = destination.title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = color,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
