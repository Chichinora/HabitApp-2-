package com.example.myapplication

import com.example.myapplication.data.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {
    @Test
    fun dailyStreak_countsBackwardsFromTodayWhenCompleted() {
        val today = LocalDate.of(2026, 5, 7)
        val dates = listOf(
            today,
            today.minusDays(1),
            today.minusDays(2),
            today.minusDays(4)
        )

        val streak = StreakCalculator.calculateDailyStreak(dates, today)

        assertEquals(3, streak)
    }

    @Test
    fun dailyStreak_countsFromYesterdayWhenTodayNotCompleted() {
        val today = LocalDate.of(2026, 5, 7)
        val dates = listOf(
            today.minusDays(1),
            today.minusDays(2),
            today.minusDays(3)
        )

        val streak = StreakCalculator.calculateDailyStreak(dates, today)

        assertEquals(3, streak)
    }

    @Test
    fun weeklyStreak_requiresMeetingWeeklyTarget() {
        val today = LocalDate.of(2026, 5, 7)
        val dates = listOf(
            LocalDate.of(2026, 5, 6),
            LocalDate.of(2026, 5, 5),
            LocalDate.of(2026, 4, 29),
            LocalDate.of(2026, 4, 28),
            LocalDate.of(2026, 4, 22),
            LocalDate.of(2026, 4, 21)
        )

        val streak = StreakCalculator.calculateWeeklyStreak(
            completionDates = dates,
            weeklyTarget = 2,
            today = today
        )

        assertEquals(3, streak)
    }
}
