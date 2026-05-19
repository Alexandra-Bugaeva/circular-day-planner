package com.example.circledayplanner.domain.util

import com.example.circledayplanner.domain.model.PlannerEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object PlannerValidation {
    private val loginRegex = Regex("^[A-Za-z0-9_]{3,20}$")

    fun validateLogin(login: String): String? = when {
        !loginRegex.matches(login) -> "Логин: 3-20 символов, английские буквы, цифры или underscore"
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.length < 6 -> "Пароль должен быть не короче 6 символов"
        else -> null
    }

    fun validateEvent(
        title: String,
        startDate: LocalDate,
        start: LocalTime,
        endDate: LocalDate,
        end: LocalTime,
        notes: String
    ): String? = when {
        title.isBlank() -> "Введите название события"
        title.length > 40 -> "Название события максимум 40 символов"
        !LocalDateTime.of(endDate, end).isAfter(LocalDateTime.of(startDate, start)) -> "Дата и время окончания должны быть позже начала"
        notes.length > 300 -> "Заметки максимум 300 символов"
        else -> null
    }

    fun validateTask(title: String, note: String): String? = when {
        title.isBlank() -> "Введите название задачи"
        title.length > 40 -> "Название задачи максимум 40 символов"
        note.length > 300 -> "Заметка максимум 300 символов"
        else -> null
    }

    fun hasOverlap(candidate: PlannerEvent, events: List<PlannerEvent>): Boolean {
        return events.any {
            it.id != candidate.id &&
            candidate.startDateTime < it.endDateTime &&
                candidate.endDateTime > it.startDateTime
        }
    }
}
