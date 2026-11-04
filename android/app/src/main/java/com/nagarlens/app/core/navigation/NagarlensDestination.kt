package com.nagarlens.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector

enum class NagarlensDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    HOME(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home
    ),
    MAP(
        route = "map",
        title = "Map",
        icon = Icons.Default.Place
    ),
    CAPTURE(
        route = "capture",
        title = "Capture",
        icon = Icons.Default.AddCircle
    ),
    ACTIVITY(
        route = "activity",
        title = "Activity",
        icon = Icons.Default.List
    ),
    PROFILE(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person
    );

    companion object {
        val topLevelDestinations = entries.toList()

        fun fromRoute(route: String?): NagarlensDestination {
            return entries.find { it.route == route } ?: HOME
        }
    }
}
