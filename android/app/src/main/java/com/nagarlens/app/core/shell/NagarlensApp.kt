package com.nagarlens.app.core.shell

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nagarlens.app.core.designsystem.component.NagarlensBottomNavigationBar
import com.nagarlens.app.core.designsystem.component.NagarlensTopAppBar
import com.nagarlens.app.core.navigation.NagarlensDestination
import com.nagarlens.app.core.navigation.NagarlensNavHost

@Composable
fun NagarlensApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentDestination = NagarlensDestination.fromRoute(currentRoute)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            NagarlensTopAppBar(
                title = currentDestination.title,
                subtitle = "Infrastructure Intelligence Network"
            )
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
