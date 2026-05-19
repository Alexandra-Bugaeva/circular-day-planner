package com.example.circledayplanner

import android.content.Context
import androidx.room.Room
import com.example.circledayplanner.data.local.AppDatabase
import com.example.circledayplanner.data.repository.EventRepository
import com.example.circledayplanner.data.repository.TodoRepository
import com.example.circledayplanner.data.settings.SettingsDataStore
import com.example.circledayplanner.domain.model.PlannerEvent
import com.example.circledayplanner.domain.model.TodoTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(context, AppDatabase::class.java, "circle_day_planner.db")
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .build()
    val eventRepository = EventRepository(database.eventDao())
    val todoRepository = TodoRepository(database.todoTaskDao())
    val settingsDataStore = SettingsDataStore(context)

    init {
        CoroutineScope(Dispatchers.IO).launch { seedDemoIfNeeded() }
    }
    private suspend fun seedDemoIfNeeded() {
        val settings = settingsDataStore.settings.first()
        if (settings.demoSeeded || !eventRepository.isEmpty() || !todoRepository.isEmpty()) return
        val today = LocalDate.now()
        listOf(
            PlannerEvent(title = "Учеба", startDate = today, startTime = LocalTime.of(8, 0), endDate = today, endTime = LocalTime.of(10, 0), color = 0xFF56A9E8),
            PlannerEvent(title = "Спорт", startDate = today, startTime = LocalTime.of(10, 15), endDate = today, endTime = LocalTime.of(11, 30), color = 0xFF96E0B8),
            PlannerEvent(title = "Обед", startDate = today, startTime = LocalTime.of(12, 0), endDate = today, endTime = LocalTime.of(13, 0), color = 0xFFF7C76E),
            PlannerEvent(title = "Работа", startDate = today, startTime = LocalTime.of(14, 0), endDate = today, endTime = LocalTime.of(18, 0), color = 0xFFFFA08A),
            PlannerEvent(title = "Отдых", startDate = today, startTime = LocalTime.of(20, 0), endDate = today.plusDays(1), endTime = LocalTime.of(1, 0), color = 0xFFE487C0)
        ).forEach { eventRepository.save(it) }
        listOf(
            TodoTask(title = "Подготовить презентацию", note = "Слайды для курсового проекта", date = today, positionIndex = 0),
            TodoTask(title = "Купить продукты", note = "После учебы", date = today, positionIndex = 1),
            TodoTask(title = "Прочитать 30 страниц книги", note = "", date = today, positionIndex = 2)
        ).forEach { todoRepository.save(it) }
        settingsDataStore.update { it.demoSeeded(true) }
    }
}
