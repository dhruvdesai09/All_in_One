package com.habit.app.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.habit.app.presentation.theme.NexoraGold
import com.habit.app.presentation.theme.NexoraGoldDim
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.SurfaceElevated
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
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    NavDestination(
        route = Routes.All,
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
    NavDestination(
        route = Routes.BudgetOverview,
        label = "Budget",
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet,
    ),
)

/** Routes where the bottom navigation bar should be visible. */
private val topLevelRoutes = setOf(
    Routes.Home,
    Routes.All,
    Routes.PinEntry,
    Routes.PasswordList,
    Routes.BudgetOverview,
    Routes.TransactionHistory,
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
                NexoraNavBar(
                    currentRoute = currentRoute,
                    navController = navController,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
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
private fun NexoraNavBar(
    currentRoute: String?,
    navController: NavHostController,
) {
    Surface(
        color = SurfaceElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceBorder))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp) // A bit taller for the new pill design
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavDestinations.forEach { dest ->
                val isSelected = when (dest.route) {
                    Routes.Home -> currentRoute == Routes.Home
                    Routes.All -> currentRoute == Routes.All
                    Routes.PinEntry -> currentRoute == Routes.PinEntry || currentRoute == Routes.PasswordList
                    Routes.BudgetOverview -> currentRoute == Routes.BudgetOverview || currentRoute == Routes.TransactionHistory
                    else -> currentRoute == dest.route
                }

                NavBarItem(
                    dest = dest,
                    isSelected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(dest.route) {
                                popUpTo(Routes.Home) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    dest: NavDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (isSelected) Modifier.background(Brush.linearGradient(listOf(NexoraGold.copy(alpha = 0.2f), NexoraGoldDim.copy(alpha = 0.15f))))
                    else Modifier.background(Color.Transparent)
                )
                .then(
                    if (isSelected) Modifier.border(1.dp, NexoraGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                contentDescription = dest.label,
                tint = if (isSelected) NexoraGold else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = dest.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) NexoraGold else TextMuted,
            fontSize = 11.sp
        )
    }
}
