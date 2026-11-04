package com.nagarlens.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nagarlens.app.feature.activity.ActivityScreen
import com.nagarlens.app.feature.capture.CaptureScreen
import com.nagarlens.app.feature.home.HomeScreen
import com.nagarlens.app.feature.map.MapScreen
import com.nagarlens.app.feature.profile.ProfileScreen

@Composable
fun NagarlensNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NagarlensDestination.HOME.route,
        modifier = modifier
    ) {
        composable(NagarlensDestination.HOME.route) {
            HomeScreen(
                onNavigateToMap = {
                    navController.navigate(NagarlensDestination.MAP.route)
                },
                onNavigateToCapture = {
                    navController.navigate(NagarlensDestination.CAPTURE.route)
                }
            )
        }
        composable(NagarlensDestination.MAP.route) {
            MapScreen(
                onNavigateToCapture = {
                    navController.navigate(NagarlensDestination.CAPTURE.route)
                }
            )
        }
        composable(NagarlensDestination.CAPTURE.route) {
            CaptureScreen()
        }
        composable(NagarlensDestination.ACTIVITY.route) {
            ActivityScreen()
        }
        composable(NagarlensDestination.PROFILE.route) {
            ProfileScreen()
        }
    }
}
