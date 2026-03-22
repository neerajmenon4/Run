package com.kwyr.runnerplanner

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kwyr.runnerplanner.ui.components.BottomNavigation
import com.kwyr.runnerplanner.ui.screens.home.HomeScreen
import com.kwyr.runnerplanner.ui.screens.plan.TrainingPlanScreen
import com.kwyr.runnerplanner.ui.screens.history.GarminActivitiesScreen
import com.kwyr.runnerplanner.ui.screens.profile.ProfileScreen
import com.kwyr.runnerplanner.ui.screens.import_gpx.ImportGPXScreen
import com.kwyr.runnerplanner.ui.screens.settings.SettingsScreen
import com.kwyr.runnerplanner.util.Constants

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Constants.Navigation.HOME

    Scaffold(
        bottomBar = {
            BottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    // If on Settings and clicking Profile, just pop back
                    if (currentRoute == Constants.Navigation.SETTINGS && route == Constants.Navigation.PROFILE) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(route) {
                            // Pop everything up to HOME to clear any nested screens
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Constants.Navigation.HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Constants.Navigation.HOME) {
                HomeScreen(
                    onNavigateToImport = {
                        navController.navigate(Constants.Navigation.IMPORT_GPX)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Constants.Navigation.HISTORY) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            composable(Constants.Navigation.PLAN) {
                TrainingPlanScreen()
            }
            
            composable(Constants.Navigation.HISTORY) {
                GarminActivitiesScreen()
            }
            
            composable(Constants.Navigation.PROFILE) {
                ProfileScreen(
                    onNavigateToSettings = {
                        navController.navigate(Constants.Navigation.SETTINGS)
                    }
                )
            }
            
            composable(Constants.Navigation.SETTINGS) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Constants.Navigation.IMPORT_GPX) {
                ImportGPXScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onImportSuccess = {
                        // Simply pop back to home and then navigate to history
                        navController.popBackStack(Constants.Navigation.HOME, inclusive = false)
                        navController.navigate(Constants.Navigation.HISTORY) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
