package com.example.circledayplanner.presentation.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.circledayplanner.data.repository.EventRepository
import com.example.circledayplanner.domain.model.PlannerEvent
import com.example.circledayplanner.domain.util.PlannerValidation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class PlannerViewModel(private val repository: EventRepository) : ViewModel() {
    val selectedDate = MutableStateFlow(LocalDate.now())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val events: StateFlow<List<PlannerEvent>> = selectedDate.flatMapLatest { repository.eventsByDate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val now: StateFlow<LocalTime> = flow {
        while (true) {
            emit(LocalTime.now())
            delay(30_000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LocalTime.now())

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun save(event: PlannerEvent, onResult: (Boolean, String?) -> Unit) = viewModelScope.launch {
        val error = PlannerValidation.validateEvent(event.title, event.startDate, event.startTime, event.endDate, event.endTime, event.notes)
        if (error != null) {
            onResult(false, error)
            return@launch
        }
        val warning = if (PlannerValidation.hasOverlap(event, events.value)) "Событие сохранено, но пересекается с другим" else null
        repository.save(event)
        onResult(true, warning)
    }

    fun delete(event: PlannerEvent) = viewModelScope.launch {
        repository.delete(event)
    }
}
