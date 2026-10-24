package com.example.goalhub.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalhub.data.repository.GoalPlannerRepository
import com.example.goalhub.domain.model.Status
import com.example.goalhub.domain.model.Target
import com.example.goalhub.domain.model.Task
import com.example.goalhub.util.mondayBasedIndex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val selected: LocalDate = LocalDate.now(),
    val targets: List<Target> = emptyList(),
    val tasks: List<Task> = emptyList(),
) {
    /** First day of the 6-week grid (Monday-based). */
    val gridStart: LocalDate
        get() = month.atDay(1).minusDays(month.atDay(1).mondayBasedIndex().toLong())

    val days: List<LocalDate> get() = (0 until 42).map { gridStart.plusDays(it.toLong()) }

    /** Targets whose range overlaps [date]. */
    fun targetsOn(date: LocalDate): List<Target> =
        targets.filter { !it.start.isAfter(date) && !it.end.isBefore(date) && it.status.isOpen }

    fun tasksOn(date: LocalDate): List<Task> =
        tasks.filter { it.due == date && !it.completed }

    fun markerCount(date: LocalDate): Int =
        (targetsOn(date).size + tasksOn(date).size).coerceIn(0, 3)
}

class CalendarViewModel(repository: GoalPlannerRepository) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    private val _selected = MutableStateFlow(LocalDate.now())
    val selected: StateFlow<LocalDate> = _selected.asStateFlow()

    val uiState: StateFlow<CalendarUiState> = combine(
        repository.observeTargets(),
        repository.observeTasks(),
        _month,
        _selected,
    ) { targets, tasks, month, selected ->
        CalendarUiState(month = month, selected = selected, targets = targets, tasks = tasks)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState(),
    )

    fun previousMonth() { _month.value = _month.value.minusMonths(1) }
    fun nextMonth() { _month.value = _month.value.plusMonths(1) }
    fun goToToday() {
        val today = LocalDate.now()
        _month.value = YearMonth.from(today)
        _selected.value = today
    }
    fun select(date: LocalDate) { _selected.value = date }
}
