package com.example.goalhub.data.mapper

import com.example.goalhub.data.local.entities.MilestoneEntity
import com.example.goalhub.data.local.entities.NoteEntity
import com.example.goalhub.data.local.entities.SubtaskEntity
import com.example.goalhub.data.local.relations.GoalWithDetails
import com.example.goalhub.data.local.relations.ProjectWithDetails
import com.example.goalhub.data.local.relations.TargetWithDetails
import com.example.goalhub.data.local.relations.TaskWithDetails
import com.example.goalhub.domain.model.Category
import com.example.goalhub.domain.model.Goal
import com.example.goalhub.domain.model.Milestone
import com.example.goalhub.domain.model.Note
import com.example.goalhub.domain.model.Priority
import com.example.goalhub.domain.model.ProgressMode
import com.example.goalhub.domain.model.Project
import com.example.goalhub.domain.model.Status
import com.example.goalhub.domain.model.Subtask
import com.example.goalhub.domain.model.Target
import com.example.goalhub.domain.model.Task
import com.example.goalhub.util.toLocalDate

/** Derive the effective status: open items whose end date passed become OVERDUE. */
internal fun effectiveStatus(stored: Status, endDay: Long, now: java.time.LocalDate): Status =
    if (stored.isOpen && endDay.toLocalDate() < now) Status.OVERDUE else stored

fun GoalWithDetails.toDomain(now: java.time.LocalDate = java.time.LocalDate.now()): Goal {
    val stored = Status.fromName(goal.status)
    return Goal(
        id = goal.id,
        title = goal.title,
        description = goal.description,
        start = goal.startDay.toLocalDate(),
        end = goal.endDay.toLocalDate(),
        status = effectiveStatus(stored, goal.endDay, now),
        storedStatus = stored,
        priority = Priority.fromName(goal.priority),
        category = Category.fromName(goal.category),
        progress = goal.progress,
        progressMode = ProgressMode.fromName(goal.progressMode),
        gradientIndex = goal.gradientIndex,
        createdAt = goal.createdAt,
        completedAt = goal.completedAt,
        milestones = milestones.sortedBy { it.position }.map { it.toDomain() },
        projects = projects.map { it.toDomain(now) },
        targets = targets.map { it.toDomain(now) },
        tasks = tasks.map { it.toDomain() },
    )
}

fun ProjectWithDetails.toDomain(now: java.time.LocalDate = java.time.LocalDate.now()): Project {
    val stored = Status.fromName(project.status)
    return Project(
        id = project.id,
        goalId = project.goalId,
        title = project.title,
        description = project.description,
        start = project.startDay.toLocalDate(),
        end = project.endDay.toLocalDate(),
        status = effectiveStatus(stored, project.endDay, now),
        storedStatus = stored,
        priority = Priority.fromName(project.priority),
        category = Category.fromName(project.category),
        progress = project.progress,
        progressMode = ProgressMode.fromName(project.progressMode),
        gradientIndex = project.gradientIndex,
        createdAt = project.createdAt,
        completedAt = project.completedAt,
        milestones = milestones.sortedBy { it.position }.map { it.toDomain() },
        targets = targets.map { it.toDomain(now) },
        tasks = tasks.map { it.toDomain() },
    )
}

/** Entity-level mapping for targets nested inside goals/projects (children not loaded). */
fun com.example.goalhub.data.local.entities.TargetEntity.toDomain(
    now: java.time.LocalDate = java.time.LocalDate.now(),
): Target {
    val stored = Status.fromName(status)
    return Target(
        id = id,
        goalId = goalId,
        projectId = projectId,
        title = title,
        description = description,
        start = startDay.toLocalDate(),
        end = endDay.toLocalDate(),
        status = effectiveStatus(stored, endDay, now),
        storedStatus = stored,
        priority = Priority.fromName(priority),
        category = Category.fromName(category),
        progress = progress,
        progressMode = ProgressMode.fromName(progressMode),
        gradientIndex = gradientIndex,
        createdAt = createdAt,
        completedAt = completedAt,
    )
}

fun com.example.goalhub.data.local.entities.ProjectEntity.toDomain(
    now: java.time.LocalDate = java.time.LocalDate.now(),
): Project {
    val stored = Status.fromName(status)
    return Project(
        id = id,
        goalId = goalId,
        title = title,
        description = description,
        start = startDay.toLocalDate(),
        end = endDay.toLocalDate(),
        status = effectiveStatus(stored, endDay, now),
        storedStatus = stored,
        priority = Priority.fromName(priority),
        category = Category.fromName(category),
        progress = progress,
        progressMode = ProgressMode.fromName(progressMode),
        gradientIndex = gradientIndex,
        createdAt = createdAt,
        completedAt = completedAt,
    )
}

fun TargetWithDetails.toDomain(now: java.time.LocalDate = java.time.LocalDate.now()): Target {
    val stored = Status.fromName(target.status)
    return Target(
        id = target.id,
        goalId = target.goalId,
        projectId = target.projectId,
        title = target.title,
        description = target.description,
        start = target.startDay.toLocalDate(),
        end = target.endDay.toLocalDate(),
        status = effectiveStatus(stored, target.endDay, now),
        storedStatus = stored,
        priority = Priority.fromName(target.priority),
        category = Category.fromName(target.category),
        progress = target.progress,
        progressMode = ProgressMode.fromName(target.progressMode),
        gradientIndex = target.gradientIndex,
        createdAt = target.createdAt,
        completedAt = target.completedAt,
        subtasks = subtasks.sortedBy { it.position }.map { it.toDomain() },
        notes = notes.sortedByDescending { it.createdAt }.map { it.toDomain() },
    )
}

fun TaskWithDetails.toDomain(): Task = task.toDomain()

internal fun com.example.goalhub.data.local.entities.TaskEntity.toDomain(): Task = Task(
    id = id,
    goalId = goalId,
    projectId = projectId,
    targetId = targetId,
    title = title,
    description = description,
    start = startDay?.toLocalDate(),
    due = dueDay?.toLocalDate(),
    priority = Priority.fromName(priority),
    category = Category.fromName(category),
    completed = completed,
    completedAt = completedAt,
    createdAt = createdAt,
)

private fun MilestoneEntity.toDomain(): Milestone = Milestone(
    id = id,
    goalId = goalId,
    projectId = projectId,
    title = title,
    completed = completed,
    completedAt = completedAt,
    position = position,
)

private fun SubtaskEntity.toDomain(): Subtask = Subtask(
    id = id,
    targetId = targetId,
    taskId = taskId,
    title = title,
    completed = completed,
    completedAt = completedAt,
    position = position,
)

private fun NoteEntity.toDomain(): Note = Note(
    id = id,
    targetId = targetId,
    text = text,
    createdAt = createdAt,
)
