package com.example.circledayplanner.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.circledayplanner.AppContainer
import com.example.circledayplanner.domain.model.UserSettings
import com.example.circledayplanner.presentation.auth.AuthScreen
import com.example.circledayplanner.presentation.auth.AuthViewModel
import com.example.circledayplanner.presentation.components.PlannerBottomBar
import com.example.circledayplanner.presentation.planner.PlannerScreen
import com.example.circledayplanner.presentation.planner.PlannerViewModel
import com.example.circledayplanner.presentation.settings.SettingsScreen
import com.example.circledayplanner.presentation.settings.SettingsViewModel
import com.example.circledayplanner.presentation.todo.TodoScreen
import com.example.circledayplanner.presentation.todo.TodoViewModel

@Composable
fun AppNavigation(container: AppContainer, settings: UserSettings) {
    val navController = rememberNavController()
    val current by navController.currentBackStackEntryAsState()
    val start = if (settings.loggedIn) Screen.Planner.route else Screen.Auth.route
    val showBottomBar = current?.destination?.route != Screen.Auth.route

    val plannerViewModel: PlannerViewModel = viewModel(factory = simpleFactory { PlannerViewModel(container.eventRepository) })
    val todoViewModel: TodoViewModel = viewModel(factory = simpleFactory { TodoViewModel(container.todoRepository) })
    val settingsViewModel: SettingsViewModel = viewModel(factory = simpleFactory { SettingsViewModel(container.settingsDataStore) })
    val authViewModel: AuthViewModel = viewModel(factory = simpleFactory { AuthViewModel(container.settingsDataStore) })

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                PlannerBottomBar(currentRoute = current?.destination?.route) { screen ->
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                        popUpTo(Screen.Planner.route) { saveState = true }
                        restoreState = true
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = start, modifier = Modifier.padding(padding)) {
            composable(Screen.Auth.route) {
                AuthScreen(viewModel = authViewModel) {
                    navController.navigate(Screen.Planner.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            }
            composable(Screen.Planner.route) {
                PlannerScreen(viewModel = plannerViewModel, settings = settings)
            }
            composable(Screen.Todo.route) {
                TodoScreen(viewModel = todoViewModel, settings = settings)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    settings = settings,
                    viewModel = settingsViewModel,
                    onLogout = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
    }
}

private fun <T : ViewModel> simpleFactory(create: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
    }

