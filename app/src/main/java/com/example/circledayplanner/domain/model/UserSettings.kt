package com.example.circledayplanner.domain.model

data class UserSettings(
    val username: String = "",
    val passwordHash: String = "",
    val loggedIn: Boolean = false,
    val darkThemeEnabled: Boolean = false,
    val accentColor: Long = 0xFF6D6AE8,
    val use24HourFormat: Boolean = true,
    val moveCompletedTasksToBottom: Boolean = true,
    val demoSeeded: Boolean = false
)

