package com.example.goalhub.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Date views over the data model. These are only *views* — items keep their own
 * start/end range and simply appear on every day they span.
 */
enum class PlanningPeriod(val label: String) {
    ACTIVE("Active"),
    TODAY("Today"),
    TOMORROW("Tomorrow"),
    THIS_WEEK("This Week"),
    NEXT_WEEK("Next Week"),
    UPCOMING("Upcoming"),
    COMPLETED("Completed"),
    ALL("All");

    /**
     * Whether [date] falls inside this period, relative to [today].
     * A week runs Monday → Sunday; "This Week" shows the rest of the current week.
     */
    fun contains(date: LocalDate, today: LocalDate): Boolean = when (this) {
        ACTIVE -> date >= today
        TODAY -> date == today
        TOMORROW -> date == today.plusDays(1)
        THIS_WEEK -> date >= today && date <= endOfWeek(today)
        NEXT_WEEK -> date in nextWeekRange(today)
        UPCOMING -> date > today
        COMPLETED -> false // handled by status, not date
        ALL -> true
    }

    private fun endOfWeek(today: LocalDate): LocalDate {
        val daysUntilSunday = 7 - today.dayOfWeek.value
        return today.plusDays(daysUntilSunday.toLong())
    }

    private fun nextWeekRange(today: LocalDate): ClosedRange<LocalDate> {
        val daysUntilNextMonday = (8 - today.dayOfWeek.value) % 7
        val nextMonday = today.plusDays(daysUntilNextMonday.toLong() + 1)
        return nextMonday..nextMonday.plusDays(6)
    }

    companion object {
        val WEEK_START: DayOfWeek = DayOfWeek.MONDAY
    }
}
