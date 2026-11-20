package com.nagarlens.app.core.shell

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nagarlens.app.core.designsystem.component.NagarlensBottomNavigationBar
import com.nagarlens.app.core.designsystem.component.NagarlensTopAppBar
import com.nagarlens.app.core.navigation.NagarlensDestination
import com.nagarlens.app.core.navigation.NagarlensNavHost
import com.nagarlens.app.feature.intro.LivingCityIntroScreen

@Composable
fun NagarlensApp(
    navController: NavHostController = rememberNavController()
) {
    var isIntroShowing by remember { mutableStateOf(true) }

    Crossfade(
        targetState = isIntroShowing,
        animationSpec = tween(durationMillis = 600),
        label = "IntroToHomeCrossfade"
    ) { showIntro ->
        if (showIntro) {
            LivingCityIntroScreen(
                onIntroFinished = { isIntroShowing = false }
            )
        } else {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    // Pinned Fixed Non-Scrollable Top Bar customized for every screen
                    when (currentRoute) {
                        NagarlensDestination.HOME.route -> {
                            NagarlensTopAppBar(
                                title = "NAGARLENS",
                                subtitle = "Infrastructure Intelligence Network",
                                showOnlineStatus = true,
                                onNotificationsClick = {
                                    navController.navigate(NagarlensDestination.NOTIFICATIONS.route)
                                }
                            )
                        }
                        NagarlensDestination.MAP.route -> {
                            NagarlensTopAppBar(
                                title = "Living Map & Spatial Twin",
                                subtitle = "Ward 12 — Bangalore Central",
                                onNotificationsClick = {
                                    navController.navigate(NagarlensDestination.NOTIFICATIONS.route)
                                }
                            )
                        }
                        NagarlensDestination.INCIDENTS.route -> {
                            NagarlensTopAppBar(
                                title = "Municipal Incidents & Work Orders",
                                subtitle = "Emergency Work Order Dispatch",
                                onNotificationsClick = {
                                    navController.navigate(NagarlensDestination.NOTIFICATIONS.route)
                                }
                            )
                        }
                        NagarlensDestination.PROFILE.route -> {
                            NagarlensTopAppBar(
                                title = "Settings & Persona Governance",
                                subtitle = "RBAC Security Clearance",
                                onNotificationsClick = {
                                    navController.navigate(NagarlensDestination.NOTIFICATIONS.route)
                                }
                            )
                        }
                        NagarlensDestination.ACTIVITY.route -> {
                            NagarlensTopAppBar(
                                title = "Evidence Audit Trail",
                                subtitle = "SHA-256 Reality Captures",
                                onNotificationsClick = {
                                    navController.navigate(NagarlensDestination.NOTIFICATIONS.route)
                                }
                            )
                        }
                        NagarlensDestination.ANALYTICS.route -> {
                            NagarlensTopAppBar(
                                title = "Intelligence Analytics",
                                subtitle = "City Infrastructure Metrics",
                                onNotificationsClick = {
                                    navController.navigate(NagarlensDestination.NOTIFICATIONS.route)
                                }
                            )
                        }
                        NagarlensDestination.NOTIFICATIONS.route -> {
                            NagarlensTopAppBar(
                                title = "Alerts & System Notifications",
                                subtitle = "Real-Time Infrastructure Feeds",
                                onNotificationsClick = {}
                            )
                        }
                        // Camera capture mode has topBar = null for 100% viewfinder space
                        else -> {}
                    }
                },
                bottomBar = {
                    NagarlensBottomNavigationBar(
                        currentRoute = currentRoute,
                        onNavigateToDestination = { destination ->
                            if (currentRoute != destination.route) {
                                navController.navigate(destination.route) {
                                    popUpTo(NagarlensDestination.HOME.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            ) { innerPadding ->
                NagarlensNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
