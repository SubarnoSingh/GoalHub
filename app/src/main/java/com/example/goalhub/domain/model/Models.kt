package com.example.goalhub.domain.model

import java.time.LocalDate

/**
 * Long-term objective: weeks to months. Progress is usually driven by
 * milestones, linked projects, targets or tasks (see [ProgressCalculator]).
 */
data class Goal(
    val id: Long,
    val title: String,
    val description: String,
    val start: LocalDate,
    val end: LocalDate,
    /** Effective status — may be [Status.OVERDUE] (derived). */
    val status: Status,
    /** Exact status stored in the database (never OVERDUE). */
    val storedStatus: Status,
    val priority: Priority,
    val category: Category,
    val progress: Int,
    val progressMode: ProgressMode,
    val gradientIndex: Int,
    val createdAt: Long,
    val completedAt: Long?,
    val milestones: List<Milestone> = emptyList(),
    val projects: List<Project> = emptyList(),
    val targets: List<Target> = emptyList(),
    val tasks: List<Task> = emptyList(),
)

/**
 * A multi-step undertaking that lives under a goal (optional). Progress is
 * derived from its tasks, then targets, then milestones.
 */
data class Project(
    val id: Long,
    val goalId: Long?,
    val title: String,
    val description: String,
    val start: LocalDate,
    val end: LocalDate,
    val status: Status,
    val storedStatus: Status,
    val priority: Priority,
    val category: Category,
    val progress: Int,
    val progressMode: ProgressMode,
    val gradientIndex: Int,
    val createdAt: Long,
    val completedAt: Long?,
    val milestones: List<Milestone> = emptyList(),
    val targets: List<Target> = emptyList(),
    val tasks: List<Task> = emptyList(),
)

/**
 * Short-term target: spans a date *range* (not a single day). Progress is
 * derived from its subtasks.
 */
data class Target(
    val id: Long,
    val goalId: Long?,
    val projectId: Long?,
    val title: String,
    val description: String,
    val start: LocalDate,
    val end: LocalDate,
    val status: Status,
    val storedStatus: Status,
    val priority: Priority,
    val category: Category,
    val progress: Int,
    val progressMode: ProgressMode,
    val gradientIndex: Int,
    val createdAt: Long,
    val completedAt: Long?,
    val subtasks: List<Subtask> = emptyList(),
    val notes: List<Note> = emptyList(),
) {
    /** Inclusive number of days the target spans. */
    val durationDays: Long
        get() = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1
}

/** A normal day-to-day task. May exist standalone or hang off a goal/project/target. */
data class Task(
    val id: Long,
    val goalId: Long?,
    val projectId: Long?,
    val targetId: Long?,
    val title: String,
    val description: String,
    val start: LocalDate?,
    val due: LocalDate?,
    val priority: Priority,
    val category: Category,
    val completed: Boolean,
    val completedAt: Long?,
    val createdAt: Long,
)

/** A checkpoint of a goal or project. */
data class Milestone(
    val id: Long,
    val goalId: Long?,
    val projectId: Long?,
    val title: String,
    val completed: Boolean,
    val completedAt: Long?,
    val position: Int,
)

/** A checkbox item inside a target. */
data class Subtask(
    val id: Long,
    val targetId: Long?,
    val taskId: Long?,
    val title: String,
    val completed: Boolean,
    val completedAt: Long?,
    val position: Int,
)

/** A free-form note attached to a target. */
data class Note(
    val id: Long,
    val targetId: Long,
    val text: String,
    val createdAt: Long,
)

/** Everything the home screen needs in one emission. */
data class DashboardData(
    val activeGoals: List<Goal>,
    val activeTargets: List<Target>,
    val todayTasks: List<Task>,
    /** Targets ending within the next seven days. */
    val weekDeadlines: List<Target>,
)
