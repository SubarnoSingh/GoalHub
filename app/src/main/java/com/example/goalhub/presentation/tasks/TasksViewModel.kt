package com.example.goalhub.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalhub.data.repository.GoalPlannerRepository
import com.example.goalhub.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class TasksFilter(val label: String) {
    TODAY("Today"),
    SCHEDULED("Scheduled"),
    COMPLETED("Completed"),
    ALL("All"),
}

/** A task group header ("Overdue", "Today", "Tomorrow", "This week", "Later", "No date"). */
enum class TaskBucket(val label: String) {
    OVERDUE("Overdue"),
    TODAY("Today"),
    TOMORROW("Tomorrow"),
    THIS_WEEK("This week"),
    LATER("Later"),
    NO_DATE("No date"),
    COMPLETED("Completed");

    companion object {
        fun of(task: Task, today: LocalDate): TaskBucket = when {
            task.completed -> COMPLETED
            task.due == null -> NO_DATE
            task.due < today -> OVERDUE
            task.due == today -> TODAY
            task.due == today.plusDays(1) -> TOMORROW
            task.due <= today.plusDays((7 - today.dayOfWeek.value).toLong()) -> THIS_WEEK
            else -> LATER
        }
    }
}

data class TasksUiState(
    val filter: TasksFilter = TasksFilter.TODAY,
    val query: String = "",
    val tasks: List<Task> = emptyList(),
) {
    val grouped: List<Pair<TaskBucket, List<Task>>> by lazy {
        val today = LocalDate.now()
        val q = query.trim().lowercase()
        val filtered = tasks.filter { task ->
            when (filter) {
                TasksFilter.TODAY -> !task.completed && (task.due == null || task.due <= today.plusDays(1))
                TasksFilter.SCHEDULED -> !task.completed && task.due != null
                TasksFilter.COMPLETED -> task.completed
                TasksFilter.ALL -> true
            } && (q.isEmpty() || task.title.lowercase().contains(q) || task.description.lowercase().contains(q))
        }
        val order = TaskBucket.entries.toList()
        filtered
            .groupBy { TaskBucket.of(it, today) }
            .toSortedMap(compareBy { order.indexOf(it) })
            .toList()
            .map { (bucket, list) ->
                bucket to list.sortedWith(
                    compareBy<Task> { it.due ?: LocalDate.MAX }
                        .thenBy { it.priority.ordinal }
                        .thenBy { it.title }
                )
            }
    }
}

class TasksViewModel(private val repository: GoalPlannerRepository) : ViewModel() {

    private val _filter = MutableStateFlow(TasksFilter.TODAY)
    val filter: StateFlow<TasksFilter> = _filter.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val uiState: StateFlow<TasksUiState> = combine(
        repository.observeTasks(),
        _filter,
        _query,
    ) { tasks, filter, query ->
        TasksUiState(filter = filter, query = query, tasks = tasks)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TasksUiState(),
    )

    fun setFilter(filter: TasksFilter) { _filter.value = filter }
    fun setQuery(query: String) { _query.value = query }

    fun toggleTask(taskId: Long) = viewModelScope.launch {
        repository.toggleTask(taskId)
    }

    fun deleteTask(taskId: Long) = viewModelScope.launch {
        repository.deleteTask(taskId)
    }
}
