package com.example.circledayplanner.presentation.planner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.circledayplanner.domain.model.PlannerEvent
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun DayDial(
    events: List<PlannerEvent>,
    selectedDate: LocalDate,
    now: LocalTime,
    use24HourFormat: Boolean,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var half by remember { mutableIntStateOf(0) }
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outlineVariant

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .aspectRatio(1f)
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 2.4f)
                    }
                }
                .pointerInput(scale, use24HourFormat) {
                    detectHorizontalDragGestures { _, amount ->
                        if (!use24HourFormat || scale > 1.15f) half = if (amount < 0) 1 else 0
                    }
                }
        ) {
            val side = min(size.width, size.height)
            val radius = side / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val fullDay = use24HourFormat && scale <= 1.15f
            val dayStart = if (fullDay || half == 0) 0f else 12f
            val span = if (fullDay) 24f else 12f
            val baseStroke = if (fullDay) 96.dp.toPx() else 118.dp.toPx()
            val ringRadius = radius - baseStroke / 2f - 22.dp.toPx()

            drawCircle(outline.copy(alpha = 0.42f), radius = ringRadius, center = center, style = Stroke(baseStroke))
            drawCircle(outline.copy(alpha = 0.16f), radius = ringRadius - baseStroke / 2f, center = center)
            drawCircle(onSurface.copy(alpha = 0.12f), radius = 5.dp.toPx(), center = center)

            val levels = assignLevels(events, selectedDate)
            levels.visible.forEach { leveled ->
                val startHour = leveled.event.visibleStartFor(selectedDate).toHourFloat(startOfDay = true)
                val endHour = leveled.event.visibleEndFor(selectedDate).toHourFloat(startOfDay = false)
                if (endHour <= dayStart || startHour >= dayStart + span) return@forEach
                val clippedStart = startHour.coerceAtLeast(dayStart)
                val clippedEnd = endHour.coerceAtMost(dayStart + span)
                val startAngle = -90f + (clippedStart - dayStart) / span * 360f
                val sweep = ((clippedEnd - clippedStart) / span * 360f).coerceAtLeast(1f)
                val stroke = (baseStroke - leveled.level * 28.dp.toPx()).coerceAtLeast(30.dp.toPx())
                val levelRadius = ringRadius - leveled.level * 36.dp.toPx()
                val topLeft = Offset(center.x - levelRadius, center.y - levelRadius)
                val arcSize = Size(levelRadius * 2f, levelRadius * 2f)

                drawArc(
                    color = Color(leveled.event.color).copy(alpha = 0.82f),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Butt)
                )

                drawBoundaryLabel(leveled.event.visibleStartFor(selectedDate).formatTime(use24HourFormat), startAngle, center, radius, onSurface)
                drawBoundaryLabel(leveled.event.visibleEndFor(selectedDate).formatTime(use24HourFormat), startAngle + sweep, center, radius, onSurface)

                if (sweep > 34f) {
                    val midAngle = Math.toRadians((startAngle + sweep / 2f).toDouble())
                    val textOffset = Offset(
                        center.x + cos(midAngle).toFloat() * levelRadius * 0.58f,
                        center.y + sin(midAngle).toFloat() * levelRadius * 0.58f
                    )
                    val label = leveled.event.title.ellipsize(14)
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                            color = android.graphics.Color.rgb(38, 39, 50)
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 12.dp.toPx()
                            isFakeBoldText = true
                        }
                        drawText(label, textOffset.x, textOffset.y, paint)
                    }
                }
            }

            if (levels.hiddenCount > 0) {
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = primary.toArgb()
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 16.dp.toPx()
                        isFakeBoldText = true
                    }
                    drawText("+${levels.hiddenCount}", center.x, center.y + 30.dp.toPx(), paint)
                }
            }

            val labels = if (fullDay) {
                listOf(0f to "24", 6f to "6", 12f to "12", 18f to "18")
            } else if (half == 0) {
                listOf(0f to "00", 3f to "3", 6f to "6", 9f to "9", 12f to "12")
            } else {
                listOf(12f to "12", 15f to "3", 18f to "6", 21f to "9", 24f to "12")
            }
            labels.forEach { (hour, label) ->
                if (!fullDay && hour !in dayStart..(dayStart + span)) return@forEach
                val angle = Math.toRadians((-90f + (hour - dayStart) / span * 360f).toDouble())
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = onSurface.copy(alpha = 0.64f).toArgb()
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 11.dp.toPx()
                    }
                    val yAdjust = (paint.descent() + paint.ascent()) / 2f
                    drawText(label, center.x + cos(angle).toFloat() * (radius - 10.dp.toPx()), center.y + sin(angle).toFloat() * (radius - 10.dp.toPx()) - yAdjust, paint)
                }
            }

            val nowHour = now.toHourFloat(startOfDay = true)
            if (fullDay || nowHour in dayStart..(dayStart + span)) {
                val angle = Math.toRadians((-90f + (nowHour - dayStart) / span * 360f).toDouble())
                drawLine(
                    color = onSurface.copy(alpha = 0.76f),
                    start = center,
                    end = Offset(center.x + cos(angle).toFloat() * ringRadius, center.y + sin(angle).toFloat() * ringRadius),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        if (!use24HourFormat || scale > 1.15f) {
            Text(
                text = if (half == 0) "00:00-12:00" else "12:00-24:00",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBoundaryLabel(
    label: String,
    angleDegrees: Float,
    center: Offset,
    radius: Float,
    color: Color
) {
    val angle = Math.toRadians(angleDegrees.toDouble())
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color.copy(alpha = 0.58f).toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 9.dp.toPx()
        }
        drawText(label, center.x + cos(angle).toFloat() * (radius - 2.dp.toPx()), center.y + sin(angle).toFloat() * (radius - 2.dp.toPx()), paint)
    }
}

private fun LocalTime.toHourFloat(startOfDay: Boolean): Float {
    if (!startOfDay && this == LocalTime.MAX) return 24f
    return hour + minute / 60f + second / 3600f
}

private fun String.ellipsize(max: Int): String = if (length <= max) this else take(max - 1) + "…"

private data class LeveledEvent(val event: PlannerEvent, val level: Int)
private data class LevelResult(val visible: List<LeveledEvent>, val hiddenCount: Int)

private fun assignLevels(events: List<PlannerEvent>, selectedDate: LocalDate): LevelResult {
    val occupied = mutableListOf<MutableList<PlannerEvent>>(mutableListOf(), mutableListOf(), mutableListOf())
    val visible = mutableListOf<LeveledEvent>()
    var hidden = 0
    events.sortedBy { it.visibleStartFor(selectedDate) }.forEach { event ->
        val start = event.visibleStartFor(selectedDate)
        val end = event.visibleEndFor(selectedDate)
        val level = occupied.indexOfFirst { levelEvents ->
            levelEvents.none { start < it.visibleEndFor(selectedDate) && end > it.visibleStartFor(selectedDate) }
        }
        if (level == -1) hidden++ else {
            occupied[level].add(event)
            visible.add(LeveledEvent(event, level))
        }
    }
    return LevelResult(visible, hidden)
}
