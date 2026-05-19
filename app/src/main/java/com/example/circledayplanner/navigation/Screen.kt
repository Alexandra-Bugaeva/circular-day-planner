package com.example.circledayplanner.navigation

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Planner : Screen("planner")
    data object Todo : Screen("todo")
    data object Settings : Screen("settings")
}

