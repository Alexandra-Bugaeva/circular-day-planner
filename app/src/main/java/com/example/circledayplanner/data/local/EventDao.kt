package com.example.circledayplanner.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.circledayplanner.data.model.EventEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE startDate <= :date AND endDate >= :date ORDER BY startDate ASC, startTime ASC")
    fun eventsByDate(date: LocalDate): Flow<List<EventEntity>>

    @Query("SELECT COUNT(*) FROM events")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)
}
