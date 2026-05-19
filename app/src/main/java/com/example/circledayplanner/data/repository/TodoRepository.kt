package com.example.circledayplanner.data.repository

import com.example.circledayplanner.data.local.TodoTaskDao
import com.example.circledayplanner.data.model.toEntity
import com.example.circledayplanner.domain.model.TodoTask
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class TodoRepository(private val dao: TodoTaskDao) {
    fun tasksByDate(date: LocalDate) = dao.tasksByDate(date).map { list -> list.map { it.toDomain() } }
    suspend fun save(task: TodoTask) = dao.upsert(task.toEntity())
    suspend fun delete(task: TodoTask) = dao.delete(task.toEntity())
    suspend fun isEmpty() = dao.count() == 0
}

