package com.example.circledayplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.circledayplanner.domain.model.TodoTask
import java.time.LocalDate

@Entity(tableName = "todo_tasks")
data class TodoTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String,
    val date: LocalDate,
    val isCompleted: Boolean,
    val positionIndex: Int
) {
    fun toDomain() = TodoTask(id, title, note, date, isCompleted, positionIndex)
}

fun TodoTask.toEntity() = TodoTaskEntity(id, title, note, date, isCompleted, positionIndex)

