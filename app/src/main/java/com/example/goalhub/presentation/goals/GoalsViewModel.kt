package com.example.goalhub.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalhub.data.repository.GoalPlannerRepository
import com.example.goalhub.domain.model.Goal
import com.example.goalhub.domain.model.Project
import com.example.goalhub.domain.model.Status
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class GoalsSegment(val label: String) { GOALS("Goals"), PROJECTS("Projects") }

enum class StatusFilter(val label: String) {
    ACTIVE("Active"),
    COMPLETED("Completed"),
    ARCHIVED("Archived"),
    ALL("All");

    fun matches(status: Status): Boolean = when (this) {
        ACTIVE -> status.isOpen
        COMPLETED -> status == Status.COMPLETED
        ARCHIVED -> status == Status.ARCHIVED
        ALL -> true
    }
}

data class GoalsUiState(
    val segment: GoalsSegment = GoalsSegment.GOALS,
    val filter: StatusFilter = StatusFilter.ACTIVE,
    val goals: List<Goal> = emptyList(),
    val projects: List<Project> = emptyList(),
) {
    val visibleGoals: List<Goal> get() = goals.filter { filter.matches(it.status) }
    val visibleProjects: List<Project> get() = projects.filter { filter.matches(it.status) }
}

class GoalsViewModel(repository: GoalPlannerRepository) : ViewModel() {

    private val _segment = MutableStateFlow(GoalsSegment.GOALS)
    val segment: StateFlow<GoalsSegment> = _segment.asStateFlow()

    private val _filter = MutableStateFlow(StatusFilter.ACTIVE)
    val filter: StateFlow<StatusFilter> = _filter.asStateFlow()

    val uiState: StateFlow<GoalsUiState> = combine(
        repository.observeGoals(),
        repository.observeProjects(),
        _segment,
        _filter,
    ) { goals, projects, segment, filter ->
        GoalsUiState(
            segment = segment,
            filter = filter,
            goals = goals.sortedWith(
                compareByDescending<Goal> { it.priority.ordinal }
                    .thenByDescending { it.progress },
            ),
            projects = projects.sortedBy { it.end },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GoalsUiState(),
    )

    fun setSegment(segment: GoalsSegment) { _segment.value = segment }
    fun setFilter(filter: StatusFilter) { _filter.value = filter }
}
