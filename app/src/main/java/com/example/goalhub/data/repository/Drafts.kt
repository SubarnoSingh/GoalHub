package com.example.goalhub.data.repository

import com.example.goalhub.domain.model.Category
import com.example.goalhub.domain.model.Milestone
import com.example.goalhub.domain.model.Priority
import com.example.goalhub.domain.model.Status
import com.example.goalhub.domain.model.Subtask
import java.time.LocalDate

/**
 * Immutable snapshots of the create/edit form. The presentation layer builds
 * these and the repository decides insert-vs-update.
 */
data class GoalDraft(
    val id: Long? = null,
    val title: String,
    val description: String = "",
    val start: LocalDate,
    val end: LocalDate,
    val status: Status = Status.NOT_STARTED,
    val priority: Priority = Priority.MEDIUM,
    val category: Category = Category.OTHER,
    val gradientIndex: Int = 0,
    val milestones: List<Milestone> = emptyList(),
)

data class ProjectDraft(
    val id: Long? = null,
    val goalId: Long? = null,
    val title: String,
    val description: String = "",
    val start: LocalDate,
    val end: LocalDate,
    val status: Status = Status.NOT_STARTED,
    val priority: Priority = Priority.MEDIUM,
    val category: Category = Category.PROJECTS,
    val gradientIndex: Int = 0,
    val milestones: List<Milestone> = emptyList(),
)

data class TargetDraft(
    val id: Long? = null,
    val goalId: Long? = null,
    val projectId: Long? = null,
    val title: String,
    val description: String = "",
    val start: LocalDate,
    val end: LocalDate,
    val status: Status = Status.NOT_STARTED,
    val priority: Priority = Priority.MEDIUM,
    val category: Category = Category.OTHER,
    val gradientIndex: Int = 0,
    val subtasks: List<Subtask> = emptyList(),
)

data class TaskDraft(
    val id: Long? = null,
    val goalId: Long? = null,
    val projectId: Long? = null,
    val targetId: Long? = null,
    val title: String,
    val description: String = "",
    val start: LocalDate? = null,
    val due: LocalDate? = null,
    val priority: Priority = Priority.MEDIUM,
    val category: Category = Category.OTHER,
)
