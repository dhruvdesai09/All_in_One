package com.habit.app.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.habit.app.presentation.theme.AccentEmerald
import com.habit.app.presentation.theme.SurfaceCard
import com.habit.app.presentation.theme.TextMuted

private data class NavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavDestinations = listOf(
    NavDestination(
        route = Routes.Home,
        label = "Habits",
        selectedIcon = Icons.Filled.Today,
        unselectedIcon = Icons.Outlined.Today,
    ),
    NavDestination(
        route = Routes.PinEntry,
        label = "Vault",
        selectedIcon = Icons.Filled.Lock,
        unselectedIcon = Icons.Outlined.Lock,
    ),
)

/** Routes where the bottom navigation bar should be visible. */
private val topLevelRoutes = setOf(
    Routes.Home,
    Routes.All,
    Routes.PinEntry,
    Routes.PasswordList,
)

/** Routes that exist but must NOT show the bottom bar (full-screen experiences). */
private val noNavBarRoutes = setOf(
    Routes.Welcome,
)

@Composable
fun MainAppScreen(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            ) {
                DailyBaseNavBar(
                    currentRoute = currentRoute,
                    navController = navController,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}

@Composable
private fun DailyBaseNavBar(
    currentRoute: String?,
    navController: NavHostController,
) {
    NavigationBar(
        containerColor = SurfaceCard,
        tonalElevation = androidx.compose.ui.unit.Dp.Unspecified,
    ) {
        bottomNavDestinations.forEach { dest ->
            val isSelected = when (dest.route) {
                Routes.Home -> currentRoute == Routes.Home || currentRoute == Routes.All
                Routes.PinEntry -> currentRoute == Routes.PinEntry || currentRoute == Routes.PasswordList
                else -> currentRoute == dest.route
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(dest.route) {
                            // Pop back to Home when switching tabs so back-stack doesn't grow
                            popUpTo(Routes.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                        contentDescription = dest.label,
                    )
                },
                label = {
                    Text(
                        text = dest.label,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentEmerald,
                    selectedTextColor = AccentEmerald,
                    indicatorColor = AccentEmerald.copy(alpha = 0.12f),
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                ),
            )
        }
    }
}
