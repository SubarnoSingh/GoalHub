package com.example.goalhub.domain.model

/**
 * Central place for the "how is progress calculated?" rules. The repository
 * calls these whenever a child item changes and stores the resulting value on
 * the parent, so reads are always O(1).
 *
 * Rules:
 *  - Target  → completed subtasks / total subtasks (when in AUTO mode).
 *  - Project → completed tasks; fall back to target average; then milestones.
 *  - Goal    → completed milestones; fall back to project average; then target
 *              average; then completed tasks.
 *
 * If an item has no children at all, its stored progress is kept (so a manual
 * value or a fresh 0 survives).
 */
object ProgressCalculator {

    fun percent(done: Int, total: Int): Int =
        if (total <= 0) 0 else (done.coerceIn(0, total) * 100) / total

    fun targetProgress(
        subtasks: List<Subtask>,
        storedProgress: Int,
        mode: ProgressMode,
    ): Int = when {
        mode == ProgressMode.MANUAL -> storedProgress
        subtasks.isNotEmpty() -> percent(subtasks.count { it.completed }, subtasks.size)
        else -> storedProgress
    }

    fun projectProgress(
        tasks: List<Task>,
        targets: List<Target>,
        milestones: List<Milestone>,
        storedProgress: Int,
        mode: ProgressMode,
    ): Int {
        if (mode == ProgressMode.MANUAL) return storedProgress
        return when {
            tasks.isNotEmpty() -> percent(tasks.count { it.completed }, tasks.size)
            targets.isNotEmpty() -> targets.map { it.progress }.average().toInt()
            milestones.isNotEmpty() -> percent(milestones.count { it.completed }, milestones.size)
            else -> storedProgress
        }
    }

    fun goalProgress(
        milestones: List<Milestone>,
        projects: List<Project>,
        targets: List<Target>,
        tasks: List<Task>,
        storedProgress: Int,
        mode: ProgressMode,
    ): Int {
        if (mode == ProgressMode.MANUAL) return storedProgress
        return when {
            milestones.isNotEmpty() -> percent(milestones.count { it.completed }, milestones.size)
            projects.isNotEmpty() -> projects.map { it.progress }.average().toInt()
            targets.isNotEmpty() -> targets.map { it.progress }.average().toInt()
            tasks.isNotEmpty() -> percent(tasks.count { it.completed }, tasks.size)
            else -> storedProgress
        }
    }
}
