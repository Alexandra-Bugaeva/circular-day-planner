package com.example.circledayplanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.circledayplanner.domain.model.UserSettings

@Composable
fun CircleDayPlannerTheme(
    settings: UserSettings,
    content: @Composable () -> Unit
) {
    val accent = Color(settings.accentColor)
    val dark = settings.darkThemeEnabled || (settings.username.isEmpty() && isSystemInDarkTheme())
    val scheme = if (dark) darkColorScheme(primary = accent, secondary = accent.softSecondary())
    else lightColorScheme(primary = accent, secondary = accent.softSecondary(), background = Color(0xFFFAFAFD))

    MaterialTheme(
        colorScheme = scheme.withSoftSurface(),
        typography = AppTypography,
        content = content
    )
}

private fun Color.softSecondary() = copy(alpha = 0.74f)

private fun ColorScheme.withSoftSurface(): ColorScheme = copy(
    surface = if (isLight()) Color.White else Color(0xFF171820),
    surfaceVariant = if (isLight()) Color(0xFFF1F1F8) else Color(0xFF262735),
    outlineVariant = if (isLight()) Color(0xFFE5E5EF) else Color(0xFF3A3B48)
)

private fun ColorScheme.isLight() = background.luminance() > 0.5f

private fun Color.luminance(): Float = (0.2126f * red + 0.7152f * green + 0.0722f * blue)

