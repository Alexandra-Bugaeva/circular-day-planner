package com.example.circledayplanner.presentation.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.circledayplanner.data.repository.TodoRepository
import com.example.circledayplanner.domain.model.TodoTask
import com.example.circledayplanner.domain.util.PlannerValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {
    val selectedDate = MutableStateFlow(LocalDate.now())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<TodoTask>> = selectedDate.flatMapLatest { repository.tasksByDate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun add(title: String, note: String, onResult: (String?) -> Unit) = viewModelScope.launch {
        val error = PlannerValidation.validateTask(title, note)
        if (error != null) {
            onResult(error)
            return@launch
        }
        repository.save(TodoTask(title = title, note = note, date = selectedDate.value, positionIndex = tasks.value.size))
        onResult(null)
    }

    fun toggle(task: TodoTask, moveDone: Boolean) = viewModelScope.launch {
        val index = if (moveDone && !task.isCompleted) tasks.value.size + 1 else task.positionIndex
        repository.save(task.copy(isCompleted = !task.isCompleted, positionIndex = index))
    }

    fun delete(task: TodoTask) = viewModelScope.launch {
        repository.delete(task)
    }

    fun move(from: Int, to: Int) = viewModelScope.launch {
        val mutable = tasks.value.toMutableList()
        val item = mutable.removeAt(from)
        mutable.add(to, item)
        mutable.forEachIndexed { index, task -> repository.save(task.copy(positionIndex = index)) }
    }
}
