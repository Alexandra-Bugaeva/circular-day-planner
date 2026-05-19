package com.example.circledayplanner.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarHeader(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (expanded) 1f else 0.92f, label = "calendarAlpha")
    val start = selectedDate.with(DayOfWeek.MONDAY)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, drag ->
                    if (drag > 18) expanded = true
                    if (drag < -18) expanded = false
                }
            }
            .alpha(alpha)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            repeat(7) { index ->
                DayCell(date = start.plusDays(index.toLong()), selected = start.plusDays(index.toLong()) == selectedDate) {
                    onDateSelected(it)
                }
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = selectedDate.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru")).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val first = selectedDate.withDayOfMonth(1)
                    repeat(selectedDate.lengthOfMonth()) { day ->
                        val date = first.plusDays(day.toLong())
                        DayCell(date = date, selected = date == selectedDate, compact = true) {
                            expanded = false
                            onDateSelected(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, selected: Boolean, compact: Boolean = false, onClick: (LocalDate) -> Unit) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val background = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface
    Column(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .clickable { onClick(date) }
            .padding(horizontal = if (compact) 11.dp else 10.dp, vertical = 8.dp)
            .size(width = if (compact) 30.dp else 32.dp, height = if (compact) 32.dp else 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!compact) {
            Text(
                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru")).take(2),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}
