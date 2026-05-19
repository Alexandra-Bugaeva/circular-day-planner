package com.example.circledayplanner.presentation.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.circledayplanner.domain.model.PlannerEvent
import com.example.circledayplanner.domain.model.UserSettings
import com.example.circledayplanner.presentation.components.CalendarHeader
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun PlannerScreen(viewModel: PlannerViewModel, settings: UserSettings) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val now by viewModel.now.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var editorEvent by remember { mutableStateOf<PlannerEvent?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var details by remember { mutableStateOf<PlannerEvent?>(null) }
    var showPastEvents by remember { mutableStateOf(false) }
    val visibleEvents = remember(events, selectedDate, now, showPastEvents) {
        events
            .filter { showPastEvents || !it.isPastFor(selectedDate, now) }
            .sortedBy { it.visibleStartFor(selectedDate) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        floatingActionButton = {
            FilledIconButton(
                onClick = { editorEvent = null; showEditor = true },
                modifier = Modifier.size(58.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Добавить событие")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, drag ->
                        if (drag > 16f) showPastEvents = true
                        if (drag < -16f) showPastEvents = false
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                CalendarHeader(selectedDate = selectedDate, onDateSelected = viewModel::selectDate)
                DayDial(
                    events = events,
                    selectedDate = selectedDate,
                    now = now,
                    use24HourFormat = settings.use24HourFormat,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(Modifier.height(14.dp))
            }
            item {
                CurrentEventBlock(events = events, selectedDate = selectedDate, now = now, use24 = settings.use24HourFormat)
            }
            if (events.isEmpty()) {
                item {
                    Text(
                        "На этот день пока нет событий",
                        modifier = Modifier.padding(28.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            } else {
                items(visibleEvents, key = { it.id }) { event ->
                    EventRow(event = event, selectedDate = selectedDate, now = now, use24 = settings.use24HourFormat) { details = event }
                }
            }
        }
    }

    if (showEditor) {
        EventEditor(
            date = selectedDate,
            event = editorEvent,
            use24HourFormat = settings.use24HourFormat,
            onDismiss = { showEditor = false }
        ) { event ->
            viewModel.save(event) { saved, message ->
                if (saved) showEditor = false
                message?.let { scope.launch { snack.showSnackbar(it) } }
            }
        }
    }

    details?.let { event ->
        AlertDialog(
            onDismissRequest = { details = null },
            title = { Text(event.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(event.formatRange(settings.use24HourFormat))
                    if (event.location.isNotBlank()) Text("Место: ${event.location}")
                    if (event.notes.isNotBlank()) Text(event.notes)
                }
            },
            confirmButton = {
                TextButton(onClick = { editorEvent = event; details = null; showEditor = true }) { Text("Редактировать") }
            },
            dismissButton = { TextButton(onClick = { details = null }) { Text("Закрыть") } }
        )
    }
}

@Composable
private fun CurrentEventBlock(events: List<PlannerEvent>, selectedDate: LocalDate, now: LocalTime, use24: Boolean) {
    val current = events.firstOrNull {
        now >= it.visibleStartFor(selectedDate) && now <= it.visibleEndFor(selectedDate)
    } ?: events.firstOrNull { it.visibleStartFor(selectedDate).isAfter(now) }
    if (current != null) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Text("Сейчас", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            Text(
                "${current.formatRangeForDay(selectedDate, use24)}    ${current.title}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EventRow(event: PlannerEvent, selectedDate: LocalDate, now: LocalTime, use24: Boolean, onClick: () -> Unit) {
    val past = event.isPastFor(selectedDate, now)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .alpha(if (past) 0.45f else 1f)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(11.dp).clip(CircleShape).background(Color(event.color)))
        Text(event.formatRangeForDay(selectedDate, use24), style = MaterialTheme.typography.bodyMedium)
        Text(event.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

fun LocalTime.formatTime(use24: Boolean): String {
    val formatter = if (use24) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("h:mm a")
    return format(formatter)
}

fun PlannerEvent.formatRangeForDay(date: LocalDate, use24: Boolean): String {
    return "${visibleStartFor(date).formatTime(use24)}-${visibleEndFor(date).formatTime(use24)}"
}

private fun PlannerEvent.isPastFor(date: LocalDate, now: LocalTime): Boolean {
    return date == LocalDate.now() && visibleEndFor(date).isBefore(now)
}

private fun PlannerEvent.formatRange(use24: Boolean): String {
    val dateFormat = DateTimeFormatter.ofPattern("dd.MM")
    val start = "${startDate.format(dateFormat)} ${startTime.formatTime(use24)}"
    val end = "${endDate.format(dateFormat)} ${endTime.formatTime(use24)}"
    return "$start - $end"
}
