package com.example.goalhub.presentation.targets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalhub.data.repository.GoalPlannerRepository
import com.example.goalhub.domain.model.PlanningPeriod
import com.example.goalhub.domain.model.Status
import com.example.goalhub.domain.model.Target
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class TargetsUiState(
    val filter: PlanningPeriod = PlanningPeriod.ACTIVE,
    val targets: List<Target> = emptyList(),
) {
    val visible: List<Target> get() = targets.filter { matches(it) }

    private fun matches(target: Target): Boolean {
        val today = LocalDate.now()
        return when (filter) {
            PlanningPeriod.ACTIVE ->
                target.status.isOpen && target.end >= today && !target.completed
            PlanningPeriod.COMPLETED -> target.status == Status.COMPLETED
            PlanningPeriod.ALL -> true
            PlanningPeriod.UPCOMING -> target.start > today && target.status.isOpen
            else -> {
                // Period-based: target spans at least one day of the period.
                val from = when (filter) {
                    PlanningPeriod.TODAY -> today
                    PlanningPeriod.TOMORROW -> today.plusDays(1)
                    PlanningPeriod.THIS_WEEK -> today
                    PlanningPeriod.NEXT_WEEK -> today.plusDays((8 - today.dayOfWeek.value).toLong() + 1)
                    else -> today
                }
                val to = when (filter) {
                    PlanningPeriod.TODAY -> today
                    PlanningPeriod.TOMORROW -> today.plusDays(1)
                    PlanningPeriod.THIS_WEEK -> today.plusDays((7 - today.dayOfWeek.value).toLong())
                    PlanningPeriod.NEXT_WEEK -> from.plusDays(6)
                    else -> today
                }
                target.start <= to && target.end >= from
            }
        }
    }

    private val Target.completed: Boolean get() = status == Status.COMPLETED
}

class TargetsViewModel(repository: GoalPlannerRepository) : ViewModel() {

    private val _filter = MutableStateFlow(PlanningPeriod.ACTIVE)
    val filter: StateFlow<PlanningPeriod> = _filter.asStateFlow()

    val uiState: StateFlow<TargetsUiState> = combine(
        repository.observeTargets(),
        _filter,
    ) { targets, filter ->
        TargetsUiState(
            filter = filter,
            targets = targets.sortedWith(
                compareBy<Target> { it.status == Status.COMPLETED }
                    .thenBy { it.end }
                    .thenByDescending { it.priority.ordinal },
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TargetsUiState(),
    )

    fun setFilter(filter: PlanningPeriod) { _filter.value = filter }
}
