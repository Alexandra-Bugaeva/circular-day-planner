package com.example.circledayplanner.presentation.todo

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.circledayplanner.domain.model.TodoTask
import com.example.circledayplanner.domain.model.UserSettings
import com.example.circledayplanner.presentation.components.CalendarHeader
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodoScreen(viewModel: TodoViewModel, settings: UserSettings) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val visibleTasks = remember(tasks, settings.moveCompletedTasksToBottom) {
        if (settings.moveCompletedTasksToBottom) {
            tasks.sortedWith(compareBy<TodoTask> { it.isCompleted }.thenBy { it.positionIndex })
        } else {
            tasks.sortedBy { it.positionIndex }
        }
    }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var reorderId by remember { mutableLongStateOf(-1L) }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        floatingActionButton = {
            FilledIconButton(onClick = { showDialog = true }, modifier = Modifier.size(58.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = "Добавить задачу")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                CalendarHeader(selectedDate = selectedDate, onDateSelected = viewModel::selectDate)
                Text(
                    text = if (selectedDate == LocalDate.now()) "Запланировано на сегодня"
                    else "Запланировано на ${selectedDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru")))}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                )
            }
            if (visibleTasks.isEmpty()) {
                item {
                    Text(
                        "На этот день пока нет задач",
                        modifier = Modifier.padding(28.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            } else {
                itemsIndexed(visibleTasks, key = { _, task -> task.id }) { index, task ->
                    TodoRow(
                        task = task,
                        index = index,
                        lastIndex = visibleTasks.lastIndex,
                        reorderActive = reorderId == task.id,
                        onLongPress = { reorderId = if (reorderId == task.id) -1 else task.id },
                        onToggle = { viewModel.toggle(task, settings.moveCompletedTasksToBottom) },
                        onDelete = { viewModel.delete(task) },
                        onMove = { to -> viewModel.move(index, to) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        TaskDialog(
            onDismiss = { showDialog = false },
            onSave = { title, note ->
                viewModel.add(title, note) { error ->
                    if (error == null) showDialog = false else scope.launch { snack.showSnackbar(error) }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TodoRow(
    task: TodoTask,
    index: Int,
    lastIndex: Int,
    reorderActive: Boolean,
    onLongPress: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onMove: (Int) -> Unit
) {
    val state = rememberSwipeToDismissBoxState(confirmValueChange = {
        if (it == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            true
        } else false
    })
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(Modifier.fillMaxSize().padding(end = 24.dp), contentAlignment = Alignment.CenterEnd) {
                Text("Удалить", color = MaterialTheme.colorScheme.error)
            }
        }
    ) {
        val elevation by animateDpAsState(if (reorderActive) 6.dp else 0.dp, label = "todoElevation")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 4.dp)
                .combinedClickable(onClick = {}, onLongClick = onLongPress)
                .animateContentSize(),
            elevation = CardDefaults.cardElevation(elevation),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                IconButton(onClick = onToggle, modifier = Modifier.size(34.dp)) {
                    if (task.isCompleted) Icon(Icons.Rounded.Check, contentDescription = "Выполнено", tint = MaterialTheme.colorScheme.primary)
                    else Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.foundation.Canvas(Modifier.size(22.dp)) {
                            drawCircle(color = androidx.compose.ui.graphics.Color.Gray, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                        }
                    }
                }
                Column(Modifier.weight(1f).alpha(if (task.isCompleted) 0.45f else 1f)) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    if (task.note.isNotBlank()) Text(task.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f))
                }
                if (reorderActive) {
                    IconButton(enabled = index > 0, onClick = { onMove(index - 1) }) {
                        Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = "Выше")
                    }
                    IconButton(enabled = index < lastIndex, onClick = { onMove(index + 1) }) {
                        Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Ниже")
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it.take(40) }, label = { Text("Название") }, singleLine = true)
                OutlinedTextField(value = note, onValueChange = { note = it.take(300) }, label = { Text("Заметка") })
            }
        },
        confirmButton = { Button(onClick = { onSave(title, note) }) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
