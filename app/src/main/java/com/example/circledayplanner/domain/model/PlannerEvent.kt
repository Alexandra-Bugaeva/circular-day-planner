package com.example.circledayplanner.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class PlannerEvent(
    val id: Long = 0,
    val title: String,
    val startDate: LocalDate,
    val startTime: LocalTime,
    val endDate: LocalDate,
    val endTime: LocalTime,
    val color: Long,
    val location: String = "",
    val notes: String = "",
    val isCompleted: Boolean = false
) {
    val startDateTime: LocalDateTime get() = LocalDateTime.of(startDate, startTime)
    val endDateTime: LocalDateTime get() = LocalDateTime.of(endDate, endTime)

    fun occursOn(date: LocalDate): Boolean = !date.isBefore(startDate) && !date.isAfter(endDate)

    fun visibleStartFor(date: LocalDate): LocalTime = if (date == startDate) startTime else LocalTime.MIN

    fun visibleEndFor(date: LocalDate): LocalTime = if (date == endDate) endTime else LocalTime.MAX
}
