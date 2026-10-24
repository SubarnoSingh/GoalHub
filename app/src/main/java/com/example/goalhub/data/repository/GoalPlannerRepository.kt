package com.example.goalhub.data.repository

import com.example.goalhub.data.local.dao.GoalDao
import com.example.goalhub.data.local.dao.ProjectDao
import com.example.goalhub.data.local.dao.TargetDao
import com.example.goalhub.data.local.dao.TaskDao
import com.example.goalhub.data.local.entities.GoalEntity
import com.example.goalhub.data.local.entities.MilestoneEntity
import com.example.goalhub.data.local.entities.NoteEntity
import com.example.goalhub.data.local.entities.ProjectEntity
import com.example.goalhub.data.local.entities.SubtaskEntity
import com.example.goalhub.data.local.entities.TargetEntity
import com.example.goalhub.data.local.entities.TaskEntity
import com.example.goalhub.data.mapper.toDomain
import com.example.goalhub.data.notifier.ReminderScheduler
import com.example.goalhub.domain.model.Category
import com.example.goalhub.domain.model.DashboardData
import com.example.goalhub.domain.model.Goal
import com.example.goalhub.domain.model.Priority
import com.example.goalhub.domain.model.ProgressCalculator
import com.example.goalhub.domain.model.ProgressMode
import com.example.goalhub.domain.model.Project
import com.example.goalhub.domain.model.Status
import com.example.goalhub.domain.model.Target
import com.example.goalhub.domain.model.Task
import com.example.goalhub.util.nowMillis
import com.example.goalhub.util.toEpochDayLong
import com.example.goalhub.util.toLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Single facade over the four aggregate roots (goals, projects, targets,
 * tasks) plus their children. All writes that affect progress recompute the
 * stored progress of the owning item(s) so reads stay O(1).
 */
class GoalPlannerRepository(
    private val goalDao: GoalDao,
    private val projectDao: ProjectDao,
    private val targetDao: TargetDao,
    private val taskDao: TaskDao,
    private val scheduler: ReminderScheduler,
) {

    // ---------------- Reads (reactive) ----------------

    fun observeGoals(): Flow<List<Goal>> =
        goalDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeProjects(): Flow<List<Project>> =
        projectDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeTargets(): Flow<List<Target>> =
        targetDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeTasks(): Flow<List<Task>> =
        taskDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeGoal(id: Long): Flow<Goal?> = goalDao.observeById(id).map { it?.toDomain() }
    fun observeProject(id: Long): Flow<Project?> = projectDao.observeById(id).map { it?.toDomain() }
    fun observeTarget(id: Long): Flow<Target?> = targetDao.observeById(id).map { it?.toDomain() }
    fun observeTask(id: Long): Flow<Task?> = taskDao.observeById(id).map { it?.toDomain() }

    // One-shot reads (used by the edit form to prefill).
    suspend fun getGoal(id: Long): Goal? = goalDao.observeById(id).first()?.toDomain()
    suspend fun getProject(id: Long): Project? = projectDao.observeById(id).first()?.toDomain()
    suspend fun getTarget(id: Long): Target? = targetDao.observeById(id).first()?.toDomain()
    suspend fun getTask(id: Long): Task? = taskDao.observeById(id).first()?.toDomain()

    /** Everything the home dashboard needs, derived from the three main streams. */
    fun observeDashboard(): Flow<DashboardData> =
        combine(observeGoals(), observeTargets(), observeTasks()) { goals, targets, tasks ->
            val today = LocalDate.now()
            DashboardData(
                activeGoals = goals
                    .filter { it.status.isOpen }
                    .sortedWith(
                        compareByDescending<Goal> { it.priority.ordinal }
                            .thenByDescending { it.progress }
                            .thenBy { it.end }
                    )
                    .take(6),
                activeTargets = targets
                    .filter { it.status.isOpen && it.end >= today }
                    .sortedBy { it.end }
                    .take(6),
                todayTasks = (
                    tasks.filter { !it.completed && it.due != null && it.due <= today }
                        .sortedBy { it.due } +
                        tasks.filter { !it.completed && it.due == null }.take(3)
                    ).distinctBy { it.id },
                weekDeadlines = targets
                    .filter { it.status.isOpen && it.end > today && it.end <= today.plusDays(7) }
                    .sortedBy { it.end },
            )
        }

    // ---------------- Create / update ----------------

    suspend fun saveGoal(draft: GoalDraft): Long {
        val id = if (draft.id == null) {
            goalDao.upsert(
                GoalEntity(
                    title = draft.title.trim(),
                    description = draft.description.trim(),
                    startDay = draft.start.toEpochDayLong(),
                    endDay = draft.end.toEpochDayLong(),
                    status = draft.status.name,
                    priority = draft.priority.name,
                    category = draft.category.name,
                    progressMode = ProgressMode.AUTO.name,
                    gradientIndex = draft.gradientIndex,
                )
            ).also { newId ->
                draft.milestones
                    .filter { it.title.isNotBlank() }
                    .forEachIndexed { index, m ->
                        goalDao.upsertMilestone(MilestoneEntity(goalId = newId, title = m.title.trim(), position = index))
                    }
            }
        } else {
            goalDao.updateFields(
                id = draft.id,
                title = draft.title.trim(),
                description = draft.description.trim(),
                startDay = draft.start.toEpochDayLong(),
                endDay = draft.end.toEpochDayLong(),
                status = draft.status.name,
                priority = draft.priority.name,
                category = draft.category.name,
                gradientIndex = draft.gradientIndex,
            )
            draft.id
        }
        recomputeGoal(id)
        return id
    }

    suspend fun saveProject(draft: ProjectDraft): Long {
        val id = if (draft.id == null) {
            projectDao.upsert(
                ProjectEntity(
                    goalId = draft.goalId,
                    title = draft.title.trim(),
                    description = draft.description.trim(),
                    startDay = draft.start.toEpochDayLong(),
                    endDay = draft.end.toEpochDayLong(),
                    status = draft.status.name,
                    priority = draft.priority.name,
                    category = draft.category.name,
                    progressMode = ProgressMode.AUTO.name,
                    gradientIndex = draft.gradientIndex,
                )
            ).also { newId ->
                draft.milestones
                    .filter { it.title.isNotBlank() }
                    .forEachIndexed { index, m ->
                        projectDao.upsertMilestone(MilestoneEntity(projectId = newId, title = m.title.trim(), position = index))
                    }
            }
        } else {
            projectDao.updateFields(
                id = draft.id,
                goalId = draft.goalId,
                title = draft.title.trim(),
                description = draft.description.trim(),
                startDay = draft.start.toEpochDayLong(),
                endDay = draft.end.toEpochDayLong(),
                status = draft.status.name,
                priority = draft.priority.name,
                category = draft.category.name,
                gradientIndex = draft.gradientIndex,
            )
            draft.id
        }
        recomputeProjectAndParents(id)
        return id
    }

    suspend fun saveTarget(draft: TargetDraft): Long {
        val id = if (draft.id == null) {
            targetDao.upsert(
                TargetEntity(
                    goalId = draft.goalId,
                    projectId = draft.projectId,
                    title = draft.title.trim(),
                    description = draft.description.trim(),
                    startDay = draft.start.toEpochDayLong(),
                    endDay = draft.end.toEpochDayLong(),
                    status = draft.status.name,
                    priority = draft.priority.name,
                    category = draft.category.name,
                    progressMode = ProgressMode.AUTO.name,
                    gradientIndex = draft.gradientIndex,
                )
            ).also { newId ->
                draft.subtasks
                    .filter { it.title.isNotBlank() }
                    .forEachIndexed { index, s ->
                        targetDao.upsertSubtask(SubtaskEntity(targetId = newId, title = s.title.trim(), position = index))
                    }
            }
        } else {
            targetDao.updateFields(
                id = draft.id,
                goalId = draft.goalId,
                projectId = draft.projectId,
                title = draft.title.trim(),
                description = draft.description.trim(),
                startDay = draft.start.toEpochDayLong(),
                endDay = draft.end.toEpochDayLong(),
                status = draft.status.name,
                priority = draft.priority.name,
                category = draft.category.name,
                gradientIndex = draft.gradientIndex,
            )
            draft.id
        }
        recomputeTargetAndParents(id)
        syncReminders(id, isTarget = true)
        return id
    }

    suspend fun saveTask(draft: TaskDraft): Long {
        val id = if (draft.id == null) {
            taskDao.upsert(
                TaskEntity(
                    goalId = draft.goalId,
                    projectId = draft.projectId,
                    targetId = draft.targetId,
                    title = draft.title.trim(),
                    description = draft.description.trim(),
                    startDay = draft.start?.toEpochDayLong(),
                    dueDay = draft.due?.toEpochDayLong(),
                    priority = draft.priority.name,
                    category = draft.category.name,
                )
            )
        } else {
            taskDao.updateFields(
                id = draft.id,
                goalId = draft.goalId,
                projectId = draft.projectId,
                targetId = draft.targetId,
                title = draft.title.trim(),
                description = draft.description.trim(),
                startDay = draft.start?.toEpochDayLong(),
                dueDay = draft.due?.toEpochDayLong(),
                priority = draft.priority.name,
                category = draft.category.name,
            )
            draft.id
        }
        refreshParentsOfTask(id)
        syncReminders(id, isTarget = false)
        return id
    }

    // ---------------- Delete ----------------

    suspend fun deleteGoal(id: Long) {
        val with = goalDao.observeById(id).first()
        with?.targets?.forEach { scheduler.cancelTarget(it.id) }
        with?.tasks?.forEach { scheduler.cancelTask(it.id) }
        goalDao.delete(id)
    }

    suspend fun deleteProject(id: Long) {
        val with = projectDao.observeById(id).first()
        with?.targets?.forEach { scheduler.cancelTarget(it.id) }
        with?.tasks?.forEach { scheduler.cancelTask(it.id) }
        projectDao.delete(id)
    }

    suspend fun deleteTarget(id: Long) {
        scheduler.cancelTarget(id)
        targetDao.delete(id)
    }

    suspend fun deleteTask(id: Long) {
        scheduler.cancelTask(id)
        taskDao.delete(id)
    }

    // ---------------- Progress ----------------

    suspend fun setProgress(kind: ItemKind, id: Long, progress: Int, mode: ProgressMode) {
        val clamped = progress.coerceIn(0, 100)
        when (kind) {
            ItemKind.GOAL -> {
                // Preserve the stored status — only progress/mode change here.
                val existing = goalDao.observeById(id).first()?.goal
                goalDao.updateProgress(
                    id, clamped, mode.name,
                    status = existing?.status ?: Status.NOT_STARTED.name,
                    completedAt = existing?.completedAt,
                )
                if (existing != null) applyStatusTransition(existing)
            }
            ItemKind.PROJECT -> {
                val existing = projectDao.observeById(id).first()?.project
                projectDao.updateProgress(
                    id, clamped, mode.name,
                    status = existing?.status ?: Status.NOT_STARTED.name,
                    completedAt = existing?.completedAt,
                )
                recomputeProjectAndParents(id)
            }
            ItemKind.TARGET -> {
                val existing = targetDao.observeById(id).first()?.target
                targetDao.updateProgress(
                    id, clamped, mode.name,
                    status = existing?.status ?: Status.NOT_STARTED.name,
                    completedAt = existing?.completedAt,
                )
                recomputeTargetAndParents(id)
            }
            ItemKind.TASK -> Unit // tasks are binary
        }
    }

    suspend fun resetProgressToAuto(kind: ItemKind, id: Long) {
        when (kind) {
            ItemKind.GOAL -> {
                val existing = goalDao.observeById(id).first()?.goal
                goalDao.updateProgress(
                    id, 0, ProgressMode.AUTO.name,
                    status = existing?.status ?: Status.NOT_STARTED.name,
                    completedAt = existing?.completedAt,
                )
                recomputeGoal(id)
            }
            ItemKind.PROJECT -> {
                val existing = projectDao.observeById(id).first()?.project
                projectDao.updateProgress(
                    id, 0, ProgressMode.AUTO.name,
                    status = existing?.status ?: Status.NOT_STARTED.name,
                    completedAt = existing?.completedAt,
                )
                recomputeProjectAndParents(id)
            }
            ItemKind.TARGET -> {
                val existing = targetDao.observeById(id).first()?.target
                targetDao.updateProgress(
                    id, 0, ProgressMode.AUTO.name,
                    status = existing?.status ?: Status.NOT_STARTED.name,
                    completedAt = existing?.completedAt,
                )
                recomputeTargetAndParents(id)
            }
            ItemKind.TASK -> Unit
        }
    }

    suspend fun toggleTask(id: Long) {
        val with = taskDao.observeById(id).first() ?: return
        val completing = !with.task.completed
        taskDao.setCompleted(id, completing, if (completing) nowMillis() else null)
        if (completing) scheduler.cancelTask(id) else syncReminders(id, isTarget = false)
        refreshParentsOfTask(id)
    }

    // ---------------- Milestones / subtasks / notes ----------------

    suspend fun addMilestone(goalId: Long?, projectId: Long?, title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        val position = if (goalId != null) {
            goalDao.observeById(goalId).first()?.milestones?.size ?: 0
        } else if (projectId != null) {
            projectDao.observeById(projectId).first()?.milestones?.size ?: 0
        } else 0
        val id = goalDao.upsertMilestone(
            MilestoneEntity(goalId = goalId, projectId = projectId, title = trimmed, position = position)
        )
        if (goalId != null) recomputeGoal(goalId)
        else if (projectId != null) recomputeProjectAndParents(projectId)
    }

    suspend fun toggleMilestone(id: Long) {
        val milestone = goalDao.milestoneById(id) ?: return
        goalDao.setMilestoneCompleted(id, !milestone.completed, if (!milestone.completed) nowMillis() else null)
        if (milestone.goalId != null) recomputeGoal(milestone.goalId)
        if (milestone.projectId != null) recomputeProjectAndParents(milestone.projectId)
    }

    suspend fun deleteMilestone(id: Long) {
        val milestone = goalDao.milestoneById(id) ?: return
        goalDao.deleteMilestone(id)
        if (milestone.goalId != null) recomputeGoal(milestone.goalId)
        if (milestone.projectId != null) recomputeProjectAndParents(milestone.projectId)
    }

    suspend fun addSubtask(targetId: Long, title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        val position = targetDao.observeById(targetId).first()?.subtasks?.size ?: 0
        targetDao.upsertSubtask(SubtaskEntity(targetId = targetId, title = trimmed, position = position))
        recomputeTargetAndParents(targetId)
    }

    suspend fun toggleSubtask(id: Long) {
        val subtask = targetDao.subtaskById(id) ?: return
        val targetId = subtask.targetId ?: return
        targetDao.setSubtaskCompleted(id, !subtask.completed, if (!subtask.completed) nowMillis() else null)
        recomputeTargetAndParents(targetId)
    }

    suspend fun deleteSubtask(id: Long) {
        val subtask = targetDao.subtaskById(id) ?: return
        targetDao.deleteSubtask(id)
        subtask.targetId?.let { recomputeTargetAndParents(it) }
    }

    suspend fun addNote(targetId: Long, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        targetDao.upsertNote(NoteEntity(targetId = targetId, text = trimmed))
    }

    suspend fun deleteNote(id: Long) = targetDao.deleteNote(id)

    // ---------------- Counts (for the More screen) ----------------

    suspend fun countGoals(): Int = goalDao.count()
    suspend fun countProjects(): Int = projectDao.count()
    suspend fun countTargets(): Int = targetDao.count()
    suspend fun countTasks(): Int = taskDao.count()

    // ---------------- Sample data ----------------

    suspend fun seedIfEmpty() =
        SampleDataProvider(goalDao, projectDao, targetDao, taskDao).seedIfEmpty()
    suspend fun clearAndReseed() {
        scheduler.cancelAll()
        goalDao.deleteAll()
        projectDao.deleteAll()
        targetDao.deleteAll()
        taskDao.deleteAll()
        SampleDataProvider(goalDao, projectDao, targetDao, taskDao).seedIfEmpty()
    }

    // ---------------- Internal helpers ----------------

    private suspend fun recomputeTargetAndParents(targetId: Long) {
        val row = targetDao.observeById(targetId).first() ?: return
        val mode = ProgressMode.fromName(row.target.progressMode)
        val progress = ProgressCalculator.targetProgress(
            subtasks = row.subtasks.map {
                com.example.goalhub.domain.model.Subtask(
                    id = it.id, targetId = it.targetId, taskId = it.taskId, title = it.title,
                    completed = it.completed, completedAt = it.completedAt, position = it.position,
                )
            },
            storedProgress = row.target.progress,
            mode = mode,
        )
        val (status, completedAt) = transitionStatus(row.target.status, progress, row.target.completedAt)
        targetDao.updateProgress(row.target.id, progress, mode.name, status, completedAt)

        row.target.projectId?.let { recomputeProjectAndParents(it) }
        row.target.goalId?.let { recomputeGoal(it) }
    }

    private suspend fun recomputeProjectAndParents(projectId: Long) {
        val row = projectDao.observeById(projectId).first() ?: return
        val mode = ProgressMode.fromName(row.project.progressMode)
        val progress = ProgressCalculator.projectProgress(
            tasks = row.tasks.map { it.toDomain() },
            targets = row.targets.map { it.toDomain() },
            milestones = row.milestones.map {
                com.example.goalhub.domain.model.Milestone(
                    id = it.id, goalId = it.goalId, projectId = it.projectId, title = it.title,
                    completed = it.completed, completedAt = it.completedAt, position = it.position,
                )
            },
            storedProgress = row.project.progress,
            mode = mode,
        )
        val (status, completedAt) = transitionStatus(row.project.status, progress, row.project.completedAt)
        projectDao.updateProgress(row.project.id, progress, mode.name, status, completedAt)

        row.project.goalId?.let { recomputeGoal(it) }
    }

    private suspend fun recomputeGoal(goalId: Long) {
        val row = goalDao.observeById(goalId).first() ?: return
        val mode = ProgressMode.fromName(row.goal.progressMode)
        val progress = ProgressCalculator.goalProgress(
            milestones = row.milestones.map {
                com.example.goalhub.domain.model.Milestone(
                    id = it.id, goalId = it.goalId, projectId = it.projectId, title = it.title,
                    completed = it.completed, completedAt = it.completedAt, position = it.position,
                )
            },
            projects = row.projects.map {
                com.example.goalhub.domain.model.Project(
                    id = it.id, goalId = it.goalId, title = it.title, description = it.description,
                    start = it.startDay.toLocalDate(), end = it.endDay.toLocalDate(),
                    status = Status.fromName(it.status), storedStatus = Status.fromName(it.status),
                    priority = Priority.fromName(it.priority), category = Category.fromName(it.category),
                    progress = it.progress, progressMode = ProgressMode.fromName(it.progressMode),
                    gradientIndex = it.gradientIndex, createdAt = it.createdAt, completedAt = it.completedAt,
                )
            },
            targets = row.targets.map {
                com.example.goalhub.domain.model.Target(
                    id = it.id, goalId = it.goalId, projectId = it.projectId, title = it.title,
                    description = it.description, start = it.startDay.toLocalDate(), end = it.endDay.toLocalDate(),
                    status = Status.fromName(it.status), storedStatus = Status.fromName(it.status),
                    priority = Priority.fromName(it.priority), category = Category.fromName(it.category),
                    progress = it.progress, progressMode = ProgressMode.fromName(it.progressMode),
                    gradientIndex = it.gradientIndex, createdAt = it.createdAt, completedAt = it.completedAt,
                )
            },
            tasks = row.tasks.map { it.toDomain() },
            storedProgress = row.goal.progress,
            mode = mode,
        )
        val (status, completedAt) = transitionStatus(row.goal.status, progress, row.goal.completedAt)
        goalDao.updateProgress(row.goal.id, progress, mode.name, status, completedAt)
    }

    private suspend fun refreshParentsOfTask(taskId: Long) {
        val with = taskDao.observeById(taskId).first() ?: return
        with.task.projectId?.let { recomputeProjectAndParents(it) }
        with.task.goalId?.let { recomputeGoal(it) }
    }

    private suspend fun syncReminders(id: Long, isTarget: Boolean) {
        if (isTarget) {
            val target = targetDao.observeById(id).first()?.toDomain() ?: return
            if (target.status == Status.COMPLETED) scheduler.cancelTarget(id) else scheduler.scheduleTarget(target)
        } else {
            val task = taskDao.observeById(id).first()?.toDomain() ?: return
            if (task.completed) scheduler.cancelTask(id) else scheduler.scheduleTask(task)
        }
    }

    /** Auto-transition status between open/completed without touching paused/archived. */
    private fun transitionStatus(storedStatusName: String, progress: Int, completedAt: Long?): Pair<String, Long?> {
        val stored = Status.fromName(storedStatusName)
        val status = when {
            stored == Status.ARCHIVED || stored == Status.PAUSED -> stored
            progress >= 100 -> Status.COMPLETED
            progress > 0 && stored == Status.NOT_STARTED -> Status.IN_PROGRESS
            stored == Status.COMPLETED -> Status.IN_PROGRESS
            else -> stored
        }
        val at = when (status) {
            Status.COMPLETED -> completedAt ?: nowMillis()
            else -> null
        }
        return status.name to at
    }

    /** Re-read after a manual write and let status follow the stored progress. */
    private suspend fun applyStatusTransition(entity: GoalEntity) {
        val row = goalDao.observeById(entity.id).first() ?: return
        val (status, completedAt) = transitionStatus(row.goal.status, row.goal.progress, row.goal.completedAt)
        goalDao.updateProgress(row.goal.id, row.goal.progress, row.goal.progressMode, status, completedAt)
    }
}

enum class ItemKind { GOAL, PROJECT, TARGET, TASK }
