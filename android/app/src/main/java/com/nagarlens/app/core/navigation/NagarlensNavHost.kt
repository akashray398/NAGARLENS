package com.nagarlens.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nagarlens.app.feature.activity.ActivityScreen
import com.nagarlens.app.feature.ai.AiAnalysisScreen
import com.nagarlens.app.feature.analytics.AnalyticsScreen
import com.nagarlens.app.feature.assets.AssetDetailScreen
import com.nagarlens.app.feature.assets.AssetsScreen
import com.nagarlens.app.feature.auth.AuthScreen
import com.nagarlens.app.feature.capture.CaptureScreen
import com.nagarlens.app.feature.home.HomeScreen
import com.nagarlens.app.feature.incidents.IncidentScreen
import com.nagarlens.app.feature.investigation.InvestigationScreen
import com.nagarlens.app.feature.map.MapScreen
import com.nagarlens.app.feature.notifications.NotificationsScreen
import com.nagarlens.app.feature.profile.ProfileScreen
import com.nagarlens.app.feature.reports.ReportsScreen

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
                onNavigateToMap = { navController.navigate(NagarlensDestination.MAP.route) },
                onNavigateToCapture = { navController.navigate(NagarlensDestination.CAPTURE.route) }
            )
        }
        composable(NagarlensDestination.MAP.route) {
            MapScreen(
                onNavigateToCapture = { navController.navigate(NagarlensDestination.CAPTURE.route) }
            )
        }
        composable(NagarlensDestination.ASSETS.route) {
            AssetsScreen(
                onNavigateToAssetDetail = { assetId ->
                    navController.navigate("asset_detail/$assetId")
                }
            )
        }
        composable(
            route = "asset_detail/{assetId}",
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val assetId = backStackEntry.arguments?.getString("assetId") ?: ""
            AssetDetailScreen(
                assetId = assetId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NagarlensDestination.CAPTURE.route) {
            CaptureScreen()
        }
        composable(NagarlensDestination.INCIDENTS.route) {
            IncidentScreen(
                onNavigateToInvestigation = { incidentId ->
                    navController.navigate("investigation/$incidentId")
                }
            )
        }
        composable(
            route = "investigation/{incidentId}",
            arguments = listOf(navArgument("incidentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""
            InvestigationScreen(
                incidentId = incidentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NagarlensDestination.ACTIVITY.route) {
            ActivityScreen()
        }
        composable(
            route = "ai_analysis/{evidenceId}",
            arguments = listOf(navArgument("evidenceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val evidenceId = backStackEntry.arguments?.getString("evidenceId") ?: ""
            AiAnalysisScreen(
                evidenceId = evidenceId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NagarlensDestination.ANALYTICS.route) {
            AnalyticsScreen()
        }
        composable(NagarlensDestination.NOTIFICATIONS.route) {
            NotificationsScreen(
                onNavigateToAsset = { assetId -> navController.navigate("asset_detail/$assetId") },
                onNavigateToIncident = { incidentId -> navController.navigate("investigation/$incidentId") }
            )
        }
        composable(NagarlensDestination.REPORTS.route) {
            ReportsScreen()
        }
        composable(NagarlensDestination.PROFILE.route) {
            ProfileScreen()
        }
        composable(NagarlensDestination.AUTH.route) {
            AuthScreen(
                onAuthenticated = {
                    navController.navigate(NagarlensDestination.HOME.route) {
                        popUpTo(NagarlensDestination.AUTH.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
