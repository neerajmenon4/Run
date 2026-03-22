package com.kwyr.runnerplanner.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kwyr.runnerplanner.R
import com.kwyr.runnerplanner.util.Constants

data class NavigationItem(
    val route: String,
    val label: String,
    val iconContent: String
)

@Composable
fun BottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(80.dp),
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            NavigationItem(
                route = Constants.Navigation.HOME,
                label = stringResource(R.string.nav_home),
                iconContent = "home"
            ),
            NavigationItem(
                route = Constants.Navigation.PLAN,
                label = stringResource(R.string.nav_plan),
                iconContent = "plan"
            ),
            NavigationItem(
                route = Constants.Navigation.HISTORY,
                label = stringResource(R.string.nav_history),
                iconContent = "history"
            ),
            NavigationItem(
                route = Constants.Navigation.PROFILE,
                label = stringResource(R.string.nav_profile),
                iconContent = "profile"
            )
        )

        items.forEach { item ->
            // Treat Settings as part of Profile tab
            val isSelected = if (item.route == Constants.Navigation.PROFILE) {
                currentRoute == Constants.Navigation.PROFILE || currentRoute == Constants.Navigation.SETTINGS
            } else {
                currentRoute == item.route
            }
            
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    when (item.iconContent) {
                        "home" -> HomeIcon(color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
                        "plan" -> CalendarIcon(color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
                        "history" -> WatchIcon(color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
                        "profile" -> UserIcon(color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.tertiary,
                    indicatorColor = MaterialTheme.colorScheme.background
                )
            )
        }
    }
}
