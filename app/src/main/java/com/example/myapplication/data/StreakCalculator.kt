package com.example.myapplication.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object StreakCalculator {
    fun calculateDailyStreak(
        completionDates: List<LocalDate>,
        today: LocalDate = LocalDate.now()
    ): Int {
        if (completionDates.isEmpty()) return 0

        val uniqueDates = completionDates.toSet()
        var streak = 0
        var cursor = if (today in uniqueDates) today else today.minusDays(1)

        while (cursor in uniqueDates) {
            streak += 1
            cursor = cursor.minusDays(1)
        }

        return streak
    }

    fun calculateWeeklyStreak(
        completionDates: List<LocalDate>,
        weeklyTarget: Int,
        today: LocalDate = LocalDate.now()
    ): Int {
        if (completionDates.isEmpty()) return 0

        val target = weeklyTarget.coerceAtLeast(1)
        val countsByWeekStart = completionDates
            .groupingBy { it.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
            .eachCount()

        var streak = 0
        var cursorWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        if ((countsByWeekStart[cursorWeek] ?: 0) < target) {
            cursorWeek = cursorWeek.minusWeeks(1)
        }

        while ((countsByWeekStart[cursorWeek] ?: 0) >= target) {
            streak += 1
            cursorWeek = cursorWeek.minusWeeks(1)
        }

        return streak
    }
}
