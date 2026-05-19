package com.example.circledayplanner.domain.model

import java.time.LocalDate

data class TodoTask(
    val id: Long = 0,
    val title: String,
    val note: String = "",
    val date: LocalDate,
    val isCompleted: Boolean = false,
    val positionIndex: Int = 0
)

