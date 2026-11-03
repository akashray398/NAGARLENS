package com.nagarlens.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nagarlens.app.feature.home.HomeScreen

object NagarlensDestinations {
    const val HOME_ROUTE = "home"
}

@Composable
fun NagarlensNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NagarlensDestinations.HOME_ROUTE
    ) {
        composable(NagarlensDestinations.HOME_ROUTE) {
            HomeScreen()
        }
    }
}
