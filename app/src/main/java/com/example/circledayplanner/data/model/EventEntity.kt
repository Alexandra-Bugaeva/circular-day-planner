package com.example.circledayplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.circledayplanner.domain.model.PlannerEvent
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startDate: LocalDate,
    val startTime: LocalTime,
    val endDate: LocalDate,
    val endTime: LocalTime,
    val color: Long,
    val location: String,
    val notes: String,
    val isCompleted: Boolean
) {
    fun toDomain() = PlannerEvent(id, title, startDate, startTime, endDate, endTime, color, location, notes, isCompleted)
}

fun PlannerEvent.toEntity() = EventEntity(id, title, startDate, startTime, endDate, endTime, color, location, notes, isCompleted)
