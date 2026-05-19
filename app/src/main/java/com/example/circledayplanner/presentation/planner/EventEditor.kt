package com.example.circledayplanner.presentation.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.circledayplanner.domain.model.PlannerEvent
import com.example.circledayplanner.domain.util.PlannerValidation
import com.example.circledayplanner.ui.theme.AccentOptions
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun EventEditor(
    date: LocalDate,
    event: PlannerEvent?,
    use24HourFormat: Boolean,
    onDismiss: () -> Unit,
    onSave: (PlannerEvent) -> Unit
) {
    var title by remember { mutableStateOf(event?.title.orEmpty()) }
    var startDate by remember { mutableStateOf((event?.startDate ?: date).toString()) }
    var endDate by remember { mutableStateOf((event?.endDate ?: date).toString()) }
    var start by remember { mutableStateOf((event?.startTime ?: LocalTime.of(9, 0)).toDigits()) }
    var end by remember { mutableStateOf((event?.endTime ?: LocalTime.of(10, 0)).toDigits()) }
    var color by remember { mutableStateOf(event?.color ?: 0xFF56A9E8) }
    var location by remember { mutableStateOf(event?.location.orEmpty()) }
    var notes by remember { mutableStateOf(event?.notes.orEmpty()) }
    var localError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (event == null) "Новое событие" else "Редактировать событие") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it.take(40) }, label = { Text("Название") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = startDate, onValueChange = { startDate = it.take(10) }, label = { Text("Дата начала") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = endDate, onValueChange = { endDate = it.take(10) }, label = { Text("Дата конца") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = start.maskTime(),
                        onValueChange = { start = it.filter(Char::isDigit).take(4) },
                        label = { Text(if (use24HourFormat) "Начало" else "Начало (HH:mm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = end.maskTime(),
                        onValueChange = { end = it.filter(Char::isDigit).take(4) },
                        label = { Text(if (use24HourFormat) "Конец" else "Конец (HH:mm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                if (!use24HourFormat) {
                    Text(
                        text = "${start.parseMaskedTime()?.formatTime(false) ?: "--"} - ${end.parseMaskedTime()?.formatTime(false) ?: "--"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                    )
                }
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Место") }, singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it.take(300) }, label = { Text("Заметки") }, modifier = Modifier.height(110.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    AccentOptions.forEach { (raw, composeColor) ->
                        Box(
                            modifier = Modifier
                                .size(if (raw == color) 34.dp else 28.dp)
                                .clip(CircleShape)
                                .background(composeColor)
                                .clickable { color = raw },
                            contentAlignment = Alignment.Center
                        ) {
                            if (raw == color) Box(Modifier.size(10.dp).clip(CircleShape).background(Color.White))
                        }
                    }
                }
                localError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsedStartDate = runCatching { LocalDate.parse(startDate) }.getOrNull()
                val parsedEndDate = runCatching { LocalDate.parse(endDate) }.getOrNull()
                val parsedStart = start.parseMaskedTime()
                val parsedEnd = end.parseMaskedTime()
                if (parsedStartDate == null || parsedEndDate == null) {
                    localError = "Введите дату в формате YYYY-MM-DD"
                    return@Button
                }
                if (parsedStart == null || parsedEnd == null) {
                    localError = "Введите корректное время: часы 00-23, минуты 00-59"
                    return@Button
                }
                val error = PlannerValidation.validateEvent(title, parsedStartDate, parsedStart, parsedEndDate, parsedEnd, notes)
                if (error != null) {
                    localError = error
                    return@Button
                }
                onSave(
                    PlannerEvent(
                        id = event?.id ?: 0,
                        title = title,
                        startDate = parsedStartDate,
                        startTime = parsedStart,
                        endDate = parsedEndDate,
                        endTime = parsedEnd,
                        color = color,
                        location = location,
                        notes = notes,
                        isCompleted = event?.isCompleted ?: false
                    )
                )
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

private fun LocalTime.toDigits(): String = "%02d%02d".format(hour, minute)

private fun String.maskTime(): String = when (length) {
    0, 1, 2 -> this
    else -> take(2) + ":" + drop(2)
}

private fun String.parseMaskedTime(): LocalTime? {
    if (length != 4) return null
    val hour = substring(0, 2).toIntOrNull() ?: return null
    val minute = substring(2, 4).toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return LocalTime.of(hour, minute)
}
