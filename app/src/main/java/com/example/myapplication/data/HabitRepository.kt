package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

data class HabitSummary(
    val habit: HabitEntity,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val totalCompletions: Int,
    val lastCompletedOn: String?
)

class HabitRepository(
    private val habitDao: HabitDao
) {
    fun observeHabitSummaries(today: LocalDate = LocalDate.now()): Flow<List<HabitSummary>> {
        val todayKey = today.toString()
        return combine(
            habitDao.observeHabits(),
            habitDao.observeCompletionsForDate(todayKey)
        ) { habits, todayCompletions ->
            val completedIds = todayCompletions.mapTo(hashSetOf()) { it.habitId }

            habits.map { habit ->
                val completions = habitDao.getCompletionsForHabit(habit.id)
                val completionDates = completions.map { LocalDate.parse(it.completedOn) }
                val streak = when (habit.frequency) {
                    HabitFrequency.DAILY -> StreakCalculator.calculateDailyStreak(completionDates, today)
                    HabitFrequency.WEEKLY -> StreakCalculator.calculateWeeklyStreak(
                        completionDates = completionDates,
                        weeklyTarget = habit.weeklyTarget,
                        today = today
                    )
                }

                HabitSummary(
                    habit = habit,
                    isCompletedToday = habit.id in completedIds,
                    currentStreak = streak,
                    totalCompletions = completions.size,
                    lastCompletedOn = completions.firstOrNull()?.completedOn
                )
            }
        }
    }

    suspend fun addHabit(
        name: String,
        icon: String,
        colorHex: String,
        frequency: HabitFrequency,
        weeklyTarget: Int
    ) {
        habitDao.insertHabit(
            HabitEntity(
                name = name,
                icon = icon,
                colorHex = colorHex,
                frequency = frequency,
                weeklyTarget = weeklyTarget
            )
        )
    }

    suspend fun updateHabit(
        id: Long,
        name: String,
        icon: String,
        colorHex: String,
        frequency: HabitFrequency,
        weeklyTarget: Int,
        createdAt: Long
    ) {
        habitDao.updateHabit(
            HabitEntity(
                id = id,
                name = name,
                icon = icon,
                colorHex = colorHex,
                frequency = frequency,
                weeklyTarget = weeklyTarget,
                createdAt = createdAt
            )
        )
    }

    suspend fun deleteHabit(habit: HabitEntity) {
        habitDao.deleteHabit(habit)
    }

    suspend fun toggleTodayCompletion(habitId: Long, today: LocalDate = LocalDate.now()) {
        val todayKey = today.toString()
        val existing = habitDao.findCompletion(habitId, todayKey)
        if (existing == null) {
            habitDao.insertCompletion(
                HabitCompletionEntity(
                    habitId = habitId,
                    completedOn = todayKey
                )
            )
        } else {
            habitDao.deleteCompletion(existing)
        }
    }
}
