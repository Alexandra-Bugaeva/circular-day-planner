package com.example.circledayplanner.data.repository

import com.example.circledayplanner.data.local.EventDao
import com.example.circledayplanner.data.model.toEntity
import com.example.circledayplanner.domain.model.PlannerEvent
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class EventRepository(private val dao: EventDao) {
    fun eventsByDate(date: LocalDate) = dao.eventsByDate(date).map { list -> list.map { it.toDomain() } }
    suspend fun save(event: PlannerEvent) = dao.upsert(event.toEntity())
    suspend fun delete(event: PlannerEvent) = dao.delete(event.toEntity())
    suspend fun isEmpty() = dao.count() == 0
}

