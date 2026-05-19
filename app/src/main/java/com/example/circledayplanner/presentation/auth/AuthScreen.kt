package com.example.circledayplanner.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(viewModel: AuthViewModel, onAuthorized: () -> Unit) {
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var registerMode by remember { mutableStateOf(false) }
    var resetMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeated by remember { mutableStateOf("") }
    var codeDialog by remember { mutableStateOf<String?>(null) }
    var authorizeAfterCode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Круговой планировщик дня", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(if (registerMode) "Регистрация" else if (resetMode) "Восстановление пароля" else "Вход", modifier = Modifier.padding(top = 8.dp, bottom = 26.dp))
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Логин") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(if (resetMode) "Новый пароль" else "Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            singleLine = true
        )
        if (registerMode) {
            OutlinedTextField(
                value = repeated,
                onValueChange = { repeated = it },
                label = { Text("Повтор пароля") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                singleLine = true
            )
        }
        Button(
            onClick = {
                when {
                    registerMode -> viewModel.register(username, password, repeated) { error, code ->
                        if (error != null) scope.launch { snack.showSnackbar(error) } else {
                            authorizeAfterCode = true
                            codeDialog = "Код подтверждения: $code"
                        }
                    }
                    resetMode -> viewModel.resetPassword(username, password) { error, code ->
                        if (error != null) scope.launch { snack.showSnackbar(error) } else {
                            authorizeAfterCode = false
                            codeDialog = "Код восстановления: $code"
                        }
                    }
                    else -> viewModel.login(username, password) { error ->
                        if (error != null) scope.launch { snack.showSnackbar(error) } else onAuthorized()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp)
        ) {
            Text(if (registerMode) "Зарегистрироваться" else if (resetMode) "Сменить пароль" else "Войти")
        }
        TextButton(onClick = { resetMode = !resetMode; registerMode = false }) { Text("Забыли пароль?") }
        Text("или", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        TextButton(onClick = { registerMode = !registerMode; resetMode = false }) {
            Text(if (registerMode) "У меня уже есть аккаунт" else "Регистрация")
        }
        SnackbarHost(snack)
    }

    codeDialog?.let { text ->
        AlertDialog(
            onDismissRequest = {
                codeDialog = null
                if (authorizeAfterCode) onAuthorized()
            },
            title = { Text("Локальный код") },
            text = { Text(text) },
            confirmButton = {
                Button(onClick = {
                    codeDialog = null
                    if (authorizeAfterCode) onAuthorized()
                }) { Text("ОК") }
            }
        )
    }
}
