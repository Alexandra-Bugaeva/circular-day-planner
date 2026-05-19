package com.example.circledayplanner.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.circledayplanner.domain.model.UserSettings
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_settings")

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val username = stringPreferencesKey("username")
        val passwordHash = stringPreferencesKey("password_hash")
        val loggedIn = booleanPreferencesKey("logged_in")
        val darkTheme = booleanPreferencesKey("dark_theme")
        val accentColor = longPreferencesKey("accent_color")
        val format24 = booleanPreferencesKey("format_24")
        val moveDone = booleanPreferencesKey("move_done")
        val demoSeeded = booleanPreferencesKey("demo_seeded")
    }

    val settings = context.dataStore.data.map { prefs ->
        UserSettings(
            username = prefs[Keys.username].orEmpty(),
            passwordHash = prefs[Keys.passwordHash].orEmpty(),
            loggedIn = prefs[Keys.loggedIn] ?: false,
            darkThemeEnabled = prefs[Keys.darkTheme] ?: false,
            accentColor = prefs[Keys.accentColor] ?: 0xFF6D6AE8,
            use24HourFormat = prefs[Keys.format24] ?: true,
            moveCompletedTasksToBottom = prefs[Keys.moveDone] ?: true,
            demoSeeded = prefs[Keys.demoSeeded] ?: false
        )
    }

    suspend fun update(block: (MutablePreferencesEditor) -> Unit) {
        context.dataStore.edit { prefs -> block(MutablePreferencesEditor(prefs)) }
    }

    class MutablePreferencesEditor internal constructor(private val prefs: MutablePreferences) {
        fun username(value: String) { prefs[Keys.username] = value }
        fun passwordHash(value: String) { prefs[Keys.passwordHash] = value }
        fun loggedIn(value: Boolean) { prefs[Keys.loggedIn] = value }
        fun darkTheme(value: Boolean) { prefs[Keys.darkTheme] = value }
        fun accentColor(value: Long) { prefs[Keys.accentColor] = value }
        fun format24(value: Boolean) { prefs[Keys.format24] = value }
        fun moveDone(value: Boolean) { prefs[Keys.moveDone] = value }
        fun demoSeeded(value: Boolean) { prefs[Keys.demoSeeded] = value }
    }
}
