package com.example.circledayplanner.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.circledayplanner.data.settings.SettingsDataStore
import com.example.circledayplanner.domain.util.PasswordHasher
import com.example.circledayplanner.domain.util.PlannerValidation
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsStore: SettingsDataStore) : ViewModel() {
    fun updateUsername(username: String, onResult: (String?) -> Unit) = viewModelScope.launch {
        val error = PlannerValidation.validateLogin(username)
        if (error != null) onResult(error) else {
            settingsStore.update { it.username(username) }
            onResult(null)
        }
    }

    fun updatePassword(currentHash: String, currentPassword: String, newPassword: String, onResult: (String?) -> Unit) = viewModelScope.launch {
        val error = PlannerValidation.validatePassword(newPassword)
            ?: if (PasswordHasher.hash(currentPassword) != currentHash) "Текущий пароль введен неверно" else null
        if (error != null) onResult(error) else {
            settingsStore.update { it.passwordHash(PasswordHasher.hash(newPassword)) }
            onResult(null)
        }
    }

    fun setDarkTheme(value: Boolean) = viewModelScope.launch { settingsStore.update { it.darkTheme(value) } }
    fun setAccentColor(value: Long) = viewModelScope.launch { settingsStore.update { it.accentColor(value) } }
    fun setFormat24(value: Boolean) = viewModelScope.launch { settingsStore.update { it.format24(value) } }
    fun setMoveDone(value: Boolean) = viewModelScope.launch { settingsStore.update { it.moveDone(value) } }
    fun logout() = viewModelScope.launch { settingsStore.update { it.loggedIn(false) } }
}

