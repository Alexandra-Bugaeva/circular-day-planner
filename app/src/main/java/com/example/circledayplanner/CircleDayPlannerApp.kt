package com.example.circledayplanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.circledayplanner.navigation.AppNavigation
import com.example.circledayplanner.ui.theme.CircleDayPlannerTheme

@Composable
fun CircleDayPlannerApp(container: AppContainer) {
    val settings by container.settingsDataStore.settings.collectAsStateWithLifecycle(
        initialValue = com.example.circledayplanner.domain.model.UserSettings()
    )
    CircleDayPlannerTheme(settings = settings) {
        AppNavigation(container = container, settings = settings)
    }
}

