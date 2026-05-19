package com.example.circledayplanner.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.circledayplanner.data.model.TodoTaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TodoTaskDao {
    @Query("SELECT * FROM todo_tasks WHERE date = :date ORDER BY positionIndex ASC")
    fun tasksByDate(date: LocalDate): Flow<List<TodoTaskEntity>>

    @Query("SELECT COUNT(*) FROM todo_tasks")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(task: TodoTaskEntity)

    @Delete
    suspend fun delete(task: TodoTaskEntity)
}

