package com.example.circledayplanner.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.circledayplanner.domain.model.UserSettings
import com.example.circledayplanner.ui.theme.AccentOptions
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(settings: UserSettings, viewModel: SettingsViewModel, onLogout: () -> Unit) {
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var usernameDialog by remember { mutableStateOf(false) }
    var passwordDialog by remember { mutableStateOf(false) }
    var logoutDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Настройки", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 10.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                ListItem(
                    headlineContent = { Text(settings.username.ifBlank { "Гость" }) },
                    supportingContent = { Text("Локальный аккаунт") },
                    leadingContent = { Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { usernameDialog = true }
                )
            }
            SettingsSwitch(Icons.Outlined.DarkMode, "Темная тема", settings.darkThemeEnabled, viewModel::setDarkTheme)
            SettingsSwitch(Icons.Outlined.Schedule, "24-часовой формат", settings.use24HourFormat, viewModel::setFormat24)
            SettingsSwitch(Icons.Outlined.TaskAlt, "Выполненные задачи вниз", settings.moveCompletedTasksToBottom, viewModel::setMoveDone)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.ColorLens, contentDescription = null)
                        Text("Основной цвет", fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        AccentOptions.forEach { (raw, composeColor) ->
                            Box(
                                modifier = Modifier
                                    .size(if (settings.accentColor == raw) 38.dp else 32.dp)
                                    .clip(CircleShape)
                                    .background(composeColor)
                                    .clickable { viewModel.setAccentColor(raw) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (settings.accentColor == raw) Box(Modifier.size(11.dp).clip(CircleShape).background(Color.White))
                            }
                        }
                    }
                }
            }
            SettingsAction(Icons.Outlined.Password, "Изменить пароль", "••••••") { passwordDialog = true }
            SettingsAction(Icons.Outlined.Logout, "Выйти из аккаунта", "Подтверждение") { logoutDialog = true }
        }
        SnackbarHost(snack, modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (usernameDialog) {
        TextInputDialog(
            title = "Изменить логин",
            label = "Логин",
            initial = settings.username,
            onDismiss = { usernameDialog = false }
        ) { value ->
            viewModel.updateUsername(value) { error ->
                if (error == null) usernameDialog = false else scope.launch { snack.showSnackbar(error) }
            }
        }
    }
    if (passwordDialog) {
        PasswordDialog(
            onDismiss = { passwordDialog = false },
            onSave = { current, new ->
                viewModel.updatePassword(settings.passwordHash, current, new) { error ->
                    if (error == null) passwordDialog = false else scope.launch { snack.showSnackbar(error) }
                }
            }
        )
    }
    if (logoutDialog) {
        AlertDialog(
            onDismissRequest = { logoutDialog = false },
            title = { Text("Выйти из аккаунта?") },
            text = { Text("Данные планировщика останутся на устройстве.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.logout()
                    logoutDialog = false
                    onLogout()
                }) { Text("Выйти") }
            },
            dismissButton = { TextButton(onClick = { logoutDialog = false }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun SettingsSwitch(icon: ImageVector, title: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null)
            Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Switch(checked = checked, onCheckedChange = onChecked)
        }
    }
}

@Composable
private fun SettingsAction(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null)
            Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Text(value, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun TextInputDialog(title: String, label: String, initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text(label) }, singleLine = true) },
        confirmButton = { Button(onClick = { onSave(value) }) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun PasswordDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить пароль") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = current, onValueChange = { current = it }, label = { Text("Текущий пароль") }, visualTransformation = PasswordVisualTransformation())
                OutlinedTextField(value = new, onValueChange = { new = it }, label = { Text("Новый пароль") }, visualTransformation = PasswordVisualTransformation())
            }
        },
        confirmButton = { Button(onClick = { onSave(current, new) }) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
