package com.example.myapplication.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitEditorState(
    val id: Long = 0,
    val name: String = "",
    val icon: String = "✅",
    val colorHex: String = "#4CAF50",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val weeklyTarget: Int = 3,
    val createdAt: Long = System.currentTimeMillis()
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HabitRepository(
        HabitDatabase.getInstance(application).habitDao()
    )

    val habits = repository.observeHabitSummaries().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val completions: StateFlow<List<HabitCompletionEntity>> =
        repository.observeAllCompletions()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
    private val _editorState = MutableStateFlow(HabitEditorState())
    val editorState: StateFlow<HabitEditorState> = _editorState.asStateFlow()

    fun startCreate() {
        _editorState.value = HabitEditorState()
    }

    fun startEdit(summary: HabitSummary) {
        _editorState.value = HabitEditorState(
            id = summary.habit.id,
            name = summary.habit.name,
            icon = summary.habit.icon,
            colorHex = summary.habit.colorHex,
            frequency = summary.habit.frequency,
            weeklyTarget = summary.habit.weeklyTarget,
            createdAt = summary.habit.createdAt
        )
    }

    fun updateEditor(
        name: String? = null,
        icon: String? = null,
        colorHex: String? = null,
        frequency: HabitFrequency? = null,
        weeklyTarget: Int? = null
    ) {
        _editorState.value = _editorState.value.copy(
            name = name ?: _editorState.value.name,
            icon = icon ?: _editorState.value.icon,
            colorHex = colorHex ?: _editorState.value.colorHex,
            frequency = frequency ?: _editorState.value.frequency,
            weeklyTarget = weeklyTarget ?: _editorState.value.weeklyTarget
        )
    }

    fun saveHabit(onSaved: () -> Unit) {
        val current = _editorState.value
        if (current.name.isBlank()) return

        viewModelScope.launch {
            if (current.id == 0L) {
                repository.addHabit(
                    name = current.name.trim(),
                    icon = current.icon.ifBlank { "✅" },
                    colorHex = current.colorHex.ifBlank { "#4CAF50" },
                    frequency = current.frequency,
                    weeklyTarget = current.weeklyTarget
                )
            } else {
                repository.updateHabit(
                    id = current.id,
                    name = current.name.trim(),
                    icon = current.icon.ifBlank { "✅" },
                    colorHex = current.colorHex.ifBlank { "#4CAF50" },
                    frequency = current.frequency,
                    weeklyTarget = current.weeklyTarget,
                    createdAt = current.createdAt
                )
            }
            onSaved()
        }
    }

    fun toggleCompletion(habitId: Long) {
        viewModelScope.launch {
            repository.toggleTodayCompletion(habitId)
        }
    }

    fun deleteHabit(summary: HabitSummary) {
        viewModelScope.launch {
            repository.deleteHabit(summary.habit)
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }
}