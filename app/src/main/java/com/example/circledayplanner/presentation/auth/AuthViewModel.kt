package com.example.circledayplanner.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.circledayplanner.data.settings.SettingsDataStore
import com.example.circledayplanner.domain.model.UserSettings
import com.example.circledayplanner.domain.util.PasswordHasher
import com.example.circledayplanner.domain.util.PlannerValidation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class AuthViewModel(private val settingsStore: SettingsDataStore) : ViewModel() {
    val settings: StateFlow<UserSettings> = settingsStore.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UserSettings()
    )

    fun login(username: String, password: String, onResult: (String?) -> Unit) = viewModelScope.launch {
        val current = settings.value
        val error = PlannerValidation.validateLogin(username) ?: PlannerValidation.validatePassword(password)
        when {
            error != null -> onResult(error)
            username != current.username || PasswordHasher.hash(password) != current.passwordHash -> onResult("Неверный логин или пароль")
            else -> {
                settingsStore.update { it.loggedIn(true) }
                onResult(null)
            }
        }
    }

    fun register(username: String, password: String, repeated: String, onResult: (String?, String?) -> Unit) = viewModelScope.launch {
        val error = PlannerValidation.validateLogin(username)
            ?: PlannerValidation.validatePassword(password)
            ?: if (password != repeated) "Пароли не совпадают" else null
        if (error != null) {
            onResult(error, null)
            return@launch
        }
        val code = Random.nextInt(1000, 9999).toString()
        settingsStore.update {
            it.username(username)
            it.passwordHash(PasswordHasher.hash(password))
            it.loggedIn(true)
        }
        onResult(null, code)
    }

    fun resetPassword(username: String, newPassword: String, onResult: (String?, String?) -> Unit) = viewModelScope.launch {
        val current = settings.value
        val error = PlannerValidation.validateLogin(username)
            ?: PlannerValidation.validatePassword(newPassword)
            ?: if (username != current.username) "Пользователь не найден" else null
        if (error != null) {
            onResult(error, null)
            return@launch
        }
        val code = Random.nextInt(1000, 9999).toString()
        settingsStore.update { it.passwordHash(PasswordHasher.hash(newPassword)) }
        onResult(null, code)
    }
}
