package com.nagarlens.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
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
    ASSETS(
        route = "assets",
        title = "Assets",
        icon = Icons.Default.Build
    ),
    CAPTURE(
        route = "capture",
        title = "Capture",
        icon = Icons.Default.AddCircle
    ),
    INCIDENTS(
        route = "incidents",
        title = "Incidents",
        icon = Icons.Default.Warning
    ),
    ACTIVITY(
        route = "activity",
        title = "Activity",
        icon = Icons.Default.List
    ),
    ANALYTICS(
        route = "analytics",
        title = "Analytics",
        icon = Icons.Default.Info
    ),
    NOTIFICATIONS(
        route = "notifications",
        title = "Alerts",
        icon = Icons.Default.Notifications
    ),
    REPORTS(
        route = "reports",
        title = "Reports",
        icon = Icons.Default.List
    ),
    PROFILE(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person
    ),
    AUTH(
        route = "auth",
        title = "Sign In",
        icon = Icons.Default.Person
    );

    companion object {
        val topLevelDestinations = listOf(
            HOME,
            MAP,
            ASSETS,
            CAPTURE,
            INCIDENTS,
            ACTIVITY,
            ANALYTICS,
            NOTIFICATIONS,
            REPORTS,
            PROFILE
        )

        fun fromRoute(route: String?): NagarlensDestination {
            if (route == null) return HOME
            val baseRoute = route.substringBefore("/")
            return entries.find { it.route.substringBefore("/") == baseRoute } ?: HOME
        }
    }
}
