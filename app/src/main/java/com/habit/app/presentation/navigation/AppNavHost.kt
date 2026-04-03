package com.habit.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.habit.app.presentation.addedithabit.AddEditHabitScreen
import com.habit.app.presentation.detail.HabitDetailScreen
import com.habit.app.presentation.home.HomeScreen
import com.habit.app.presentation.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home,
        modifier = modifier,
    ) {
        composable(Routes.Home) {
            HomeScreen(
                onAddHabit = { navController.navigate(Routes.Add) },
                onSettings = { navController.navigate(Routes.Settings) },
                onAllHabits = { navController.navigate(Routes.All) },
                onOpenHabit = { navController.navigate(Routes.detail(it)) },
            )
        }
        composable(Routes.Add) {
            AddEditHabitScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.Edit,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType },
            ),
        ) {
            AddEditHabitScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.Detail,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType },
            ),
        ) {
            HabitDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.edit(it)) },
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.All) {
            com.habit.app.presentation.allhabits.AllHabitsScreen(
                onBack = { navController.popBackStack() },
                onHabitClick = { navController.navigate(Routes.detail(it)) }
            )
        }
    }
}
