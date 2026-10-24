package com.example.goalhub.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalhub.data.repository.GoalDraft
import com.example.goalhub.data.repository.GoalPlannerRepository
import com.example.goalhub.data.repository.ProjectDraft
import com.example.goalhub.data.repository.TargetDraft
import com.example.goalhub.data.repository.TaskDraft
import com.example.goalhub.domain.model.Category
import com.example.goalhub.domain.model.Goal
import com.example.goalhub.domain.model.Milestone
import com.example.goalhub.domain.model.Priority
import com.example.goalhub.domain.model.Project
import com.example.goalhub.domain.model.Status
import com.example.goalhub.domain.model.Subtask
import com.example.goalhub.domain.model.Target
import com.example.goalhub.domain.model.Task
import com.example.goalhub.ui.components.CreateItemType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CreateItemUiState(
    val type: CreateItemType = CreateItemType.GOAL,
    val editId: Long? = null,
    val title: String = "",
    val description: String = "",
    val start: LocalDate = LocalDate.now(),
    val end: LocalDate = LocalDate.now().plusDays(7),
    val due: LocalDate? = null,
    val hasDue: Boolean = false,
    val hasStart: Boolean = false,
    val status: Status = Status.NOT_STARTED,
    val priority: Priority = Priority.MEDIUM,
    val category: Category = Category.OTHER,
    val gradientIndex: Int = 0,
    val goalId: Long? = null,
    val projectId: Long? = null,
    val targetId: Long? = null,
    val milestones: List<Milestone> = emptyList(),
    val subtasks: List<Subtask> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val projects: List<Project> = emptyList(),
    val targets: List<Target> = emptyList(),
    val loading: Boolean = false,
    val saved: Boolean = false,
) {
    val canSave: Boolean get() = title.isNotBlank()
    val isEditing: Boolean get() = editId != null
}

class CreateItemViewModel(
    private val repository: GoalPlannerRepository,
    type: CreateItemType,
    editId: Long?,
    goalLink: Long?,
    projectLink: Long?,
    targetLink: Long?,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CreateItemUiState(
            type = type,
            editId = editId,
            goalId = goalLink,
            projectId = projectLink,
            targetId = targetLink,
            due = LocalDate.now().plusDays(1),
            hasDue = type == CreateItemType.TASK,
            loading = editId != null,
        )
    )
    val uiState: StateFlow<CreateItemUiState> = _state.asStateFlow()

    init {
        // Lists for the "belongs to" dropdowns.
        viewModelScope.launch {
            combine(
                repository.observeGoals(),
                repository.observeProjects(),
                repository.observeTargets(),
            ) { goals, projects, targets ->
                Triple(
                    goals.filter { !it.status.isArchived },
                    projects.filter { !it.status.isArchived },
                    targets.filter { !it.status.isArchived },
                )
            }.collect { (goals, projects, targets) ->
                _state.value = _state.value.copy(
                    goals = goals,
                    projects = projects,
                    targets = targets,
                )
            }
        }

        if (editId != null) {
            viewModelScope.launch { loadExisting(type, editId) }
        }
    }

    private suspend fun loadExisting(type: CreateItemType, id: Long) {
        when (type) {
            CreateItemType.GOAL -> {
                val goal = repository.getGoal(id) ?: return
                _state.value = _state.value.copy(
                    title = goal.title,
                    description = goal.description,
                    start = goal.start,
                    end = goal.end,
                    status = goal.storedStatus,
                    priority = goal.priority,
                    category = goal.category,
                    gradientIndex = goal.gradientIndex,
                    milestones = goal.milestones,
                    loading = false,
                )
            }
            CreateItemType.PROJECT -> {
                val project = repository.getProject(id) ?: return
                _state.value = _state.value.copy(
                    title = project.title,
                    description = project.description,
                    start = project.start,
                    end = project.end,
                    status = project.storedStatus,
                    priority = project.priority,
                    category = project.category,
                    gradientIndex = project.gradientIndex,
                    goalId = project.goalId,
                    milestones = project.milestones,
                    loading = false,
                )
            }
            CreateItemType.TARGET -> {
                val target = repository.getTarget(id) ?: return
                _state.value = _state.value.copy(
                    title = target.title,
                    description = target.description,
                    start = target.start,
                    end = target.end,
                    status = target.storedStatus,
                    priority = target.priority,
                    category = target.category,
                    gradientIndex = target.gradientIndex,
                    goalId = target.goalId,
                    projectId = target.projectId,
                    subtasks = target.subtasks,
                    loading = false,
                )
            }
            CreateItemType.TASK -> {
                val task = repository.getTask(id) ?: return
                _state.value = _state.value.copy(
                    title = task.title,
                    description = task.description,
                    start = task.start ?: LocalDate.now(),
                    hasStart = task.start != null,
                    due = task.due,
                    hasDue = task.due != null,
                    priority = task.priority,
                    category = task.category,
                    goalId = task.goalId,
                    projectId = task.projectId,
                    targetId = task.targetId,
                    loading = false,
                )
            }
        }
    }

    // ---- Field setters ----
    fun setType(type: CreateItemType) = update {
        it.copy(
            type = type,
            goalId = null,
            projectId = null,
            targetId = null,
            hasDue = type == CreateItemType.TASK,
        )
    }

    fun setTitle(title: String) = update { it.copy(title = title) }
    fun setDescription(description: String) = update { it.copy(description = description) }
    fun setStart(date: LocalDate) = update { it.copy(start = date, hasStart = true) }
    fun toggleHasStart() = update { it.copy(hasStart = !it.hasStart) }
    fun setEnd(date: LocalDate) = update { it.copy(end = date) }
    fun setDue(date: LocalDate) = update { it.copy(due = date, hasDue = true) }
    fun toggleHasDue() = update { it.copy(hasDue = !it.hasDue, due = if (!it.hasDue) it.due ?: LocalDate.now().plusDays(1) else it.due) }
    fun setStatus(status: Status) = update { it.copy(status = status) }
    fun setPriority(priority: Priority) = update { it.copy(priority = priority) }
    fun setCategory(category: Category) = update { it.copy(category = category, gradientIndex = category.presetIndex) }
    fun setGradient(index: Int) = update { it.copy(gradientIndex = index) }
    fun setGoalLink(id: Long?) = update { it.copy(goalId = id) }
    fun setProjectLink(id: Long?) = update { it.copy(projectId = id) }
    fun setTargetLink(id: Long?) = update { it.copy(targetId = id) }

    fun addMilestone(title: String) = update {
        it.copy(milestones = it.milestones + Milestone(id = 0, goalId = null, projectId = null, title = title, completed = false, completedAt = null, position = it.milestones.size))
    }
    fun removeMilestone(index: Int) = update {
        it.copy(milestones = it.milestones.filterIndexed { i, _ -> i != index })
    }
    fun addSubtask(title: String) = update {
        it.copy(subtasks = it.subtasks + Subtask(id = 0, targetId = null, taskId = null, title = title, completed = false, completedAt = null, position = it.subtasks.size))
    }
    fun removeSubtask(index: Int) = update {
        it.copy(subtasks = it.subtasks.filterIndexed { i, _ -> i != index })
    }

    fun save() {
        val s = _state.value
        if (!s.canSave) return
        viewModelScope.launch {
            when (s.type) {
                CreateItemType.GOAL -> repository.saveGoal(
                    GoalDraft(
                        id = s.editId,
                        title = s.title,
                        description = s.description,
                        start = s.start,
                        end = s.end,
                        status = s.status,
                        priority = s.priority,
                        category = s.category,
                        gradientIndex = s.gradientIndex,
                        milestones = if (s.isEditing) emptyList() else s.milestones,
                    )
                )
                CreateItemType.PROJECT -> repository.saveProject(
                    ProjectDraft(
                        id = s.editId,
                        goalId = s.goalId,
                        title = s.title,
                        description = s.description,
                        start = s.start,
                        end = s.end,
                        status = s.status,
                        priority = s.priority,
                        category = s.category,
                        gradientIndex = s.gradientIndex,
                        milestones = if (s.isEditing) emptyList() else s.milestones,
                    )
                )
                CreateItemType.TARGET -> repository.saveTarget(
                    TargetDraft(
                        id = s.editId,
                        goalId = s.goalId,
                        projectId = s.projectId,
                        title = s.title,
                        description = s.description,
                        start = s.start,
                        end = s.end,
                        status = s.status,
                        priority = s.priority,
                        category = s.category,
                        gradientIndex = s.gradientIndex,
                        subtasks = if (s.isEditing) emptyList() else s.subtasks,
                    )
                )
                CreateItemType.TASK -> repository.saveTask(
                    TaskDraft(
                        id = s.editId,
                        goalId = s.goalId,
                        projectId = s.projectId,
                        targetId = s.targetId,
                        title = s.title,
                        description = s.description,
                        start = s.start.takeIf { s.hasStart },
                        due = s.due.takeIf { s.hasDue },
                        priority = s.priority,
                        category = s.category,
                    )
                )
            }
            _state.value = _state.value.copy(saved = true)
        }
    }

    private fun update(transform: (CreateItemUiState) -> CreateItemUiState) {
        _state.value = transform(_state.value)
    }
}

private val Status.isArchived: Boolean get() = this == Status.ARCHIVED
