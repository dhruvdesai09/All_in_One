package com.habit.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.habit.app.presentation.addedithabit.AddEditHabitScreen
import com.habit.app.presentation.allhabits.AllHabitsScreen
import com.habit.app.presentation.detail.HabitDetailScreen
import com.habit.app.presentation.home.HomeScreen
import com.habit.app.presentation.passwords.AddEditPasswordScreen
import com.habit.app.presentation.passwords.PasswordDetailScreen
import com.habit.app.presentation.passwords.PasswordListScreen
import com.habit.app.presentation.passwords.PinEntryScreen
import com.habit.app.presentation.settings.SettingsScreen
import com.habit.app.presentation.welcome.WelcomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Welcome,
        modifier = modifier,
    ) {
        // ── Welcome ─────────────────────────────────────────────────
        composable(Routes.Welcome) {
            WelcomeScreen(
                onEnter = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                    }
                },
            )
        }

        // ── Habits ──────────────────────────────────────────────────
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
            arguments = listOf(navArgument("habitId") { type = NavType.LongType }),
        ) {
            AddEditHabitScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.Detail,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType }),
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
            AllHabitsScreen(
                onBack = { navController.popBackStack() },
                onHabitClick = { navController.navigate(Routes.detail(it)) },
            )
        }

        // ── Password Vault ───────────────────────────────────────────
        composable(Routes.PinEntry) {
            PinEntryScreen(
                onUnlocked = {
                    navController.navigate(Routes.PasswordList) {
                        popUpTo(Routes.PinEntry) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.PasswordList) {
            PasswordListScreen(
                onAdd = { navController.navigate(Routes.AddPassword) },
                onItemClick = { navController.navigate(Routes.passwordDetail(it)) },
                onLocked = {
                    navController.navigate(Routes.PinEntry) {
                        popUpTo(Routes.PasswordList) { inclusive = true }
                    }
                },
                onBackup = { navController.navigate(Routes.VaultBackup) },
            )
        }
        composable(Routes.AddPassword) {
            AddEditPasswordScreen(
                editId = null,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.EditPassword,
            arguments = listOf(navArgument("passwordId") { type = NavType.LongType }),
        ) { backStack ->
            val id = backStack.arguments?.getLong("passwordId")
            AddEditPasswordScreen(
                editId = id,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.PasswordDetail,
            arguments = listOf(navArgument("passwordId") { type = NavType.LongType }),
        ) { backStack ->
            val id = backStack.arguments?.getLong("passwordId") ?: return@composable
            PasswordDetailScreen(
                passwordId = id,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.editPassword(it)) },
            )
        }
        composable(Routes.VaultBackup) {
            com.habit.app.presentation.passwords.VaultBackupScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
