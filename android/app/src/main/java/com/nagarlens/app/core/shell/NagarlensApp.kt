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
                    NagarlensTopAppBar(
                        onNotificationsClick = {
                            navController.navigate(NagarlensDestination.NOTIFICATIONS.route)
                        }
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
    }
}
