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
import com.example.goalhub.util.toEpochDayLong
import java.time.LocalDate

/**
 * First-run content. Everything is relative to "today" so the app always looks
 * alive. Dates/values here are intentionally easy to edit.
 */
class SampleDataProvider(
    private val goalDao: GoalDao,
    private val projectDao: ProjectDao,
    private val targetDao: TargetDao,
    private val taskDao: TaskDao,
) {

    suspend fun seedIfEmpty() {
        if (goalDao.count() > 0) return
        seed()
    }

    private suspend fun seed() {
        val t = LocalDate.now()

        // ---------- Long-term goals ----------
        val androidId = goalDao.upsert(
            GoalEntity(
                title = "Learn Android Development",
                description = "Master modern Android development end to end — from Compose UI to Room, Hilt and shipping to the Play Store.",
                startDay = t.minusWeeks(10).toEpochDayLong(),
                endDay = t.plusWeeks(6).toEpochDayLong(),
                status = "IN_PROGRESS",
                priority = "HIGH",
                category = "STUDY",
                progress = 66,
            )
        )
        val portfolioId = goalDao.upsert(
            GoalEntity(
                title = "Build My Portfolio",
                description = "A personal site that shows who I am, what I ship, and why someone should hire me.",
                startDay = t.minusWeeks(5).toEpochDayLong(),
                endDay = t.plusWeeks(4).toEpochDayLong(),
                status = "IN_PROGRESS",
                priority = "HIGH",
                category = "PERSONAL",
                progress = 40,
                gradientIndex = 1,
            )
        )
        val gateId = goalDao.upsert(
            GoalEntity(
                title = "Prepare for GATE",
                description = "Systematic, syllabus-driven preparation for the GATE CSE exam with mock tests and revision cycles.",
                startDay = t.minusWeeks(12).toEpochDayLong(),
                endDay = t.plusWeeks(20).toEpochDayLong(),
                status = "IN_PROGRESS",
                priority = "HIGH",
                category = "STUDY",
                progress = 30,
            )
        )

        // ---------- Project ----------
        val expenseId = projectDao.upsert(
            ProjectEntity(
                goalId = androidId,
                title = "Expense Tracker App",
                description = "A complete offline-first expense tracker with budgets, charts and CSV export.",
                startDay = t.minusWeeks(3).toEpochDayLong(),
                endDay = t.plusWeeks(4).toEpochDayLong(),
                status = "IN_PROGRESS",
                priority = "HIGH",
                category = "PROJECTS",
                progress = 63,
            )
        )

        // ---------- Short-term targets ----------
        val roomTargetId = targetDao.upsert(
            TargetEntity(
                goalId = androidId,
                projectId = expenseId,
                title = "Finish Room Database",
                description = "Complete the data layer of the expense tracker: schema, DAOs and repository wiring.",
                startDay = t.toEpochDayLong(),
                endDay = t.plusDays(5).toEpochDayLong(),
                status = "IN_PROGRESS",
                priority = "HIGH",
                category = "STUDY",
                progress = 60,
            )
        )
        val expenseUiTargetId = targetDao.upsert(
            TargetEntity(
                goalId = androidId,
                projectId = expenseId,
                title = "Complete Expense Tracker UI",
                description = "Polish the screens: transactions list, add/edit flows and the spending chart.",
                startDay = t.plusDays(1).toEpochDayLong(),
                endDay = t.plusDays(7).toEpochDayLong(),
                status = "NOT_STARTED",
                priority = "MEDIUM",
                category = "PROJECTS",
                progress = 20,
            )
        )
        val coroutinesTargetId = targetDao.upsert(
            TargetEntity(
                goalId = androidId,
                title = "Finish Kotlin Coroutines",
                description = "Flow, structured concurrency and suspending functions — then build a small playground app.",
                startDay = t.plusDays(3).toEpochDayLong(),
                endDay = t.plusDays(6).toEpochDayLong(),
                status = "NOT_STARTED",
                priority = "MEDIUM",
                category = "STUDY",
                progress = 0,
            )
        )
        val dsaTargetId = targetDao.upsert(
            TargetEntity(
                goalId = gateId,
                title = "Solve 5 DSA problems",
                description = "Weekly DSA sprint — one problem per topic, with a short revision note each day.",
                startDay = t.toEpochDayLong(),
                endDay = t.plusDays(3).toEpochDayLong(),
                status = "IN_PROGRESS",
                priority = "HIGH",
                category = "STUDY",
                progress = 40,
            )
        )
        targetDao.upsert(
            TargetEntity(
                goalId = portfolioId,
                title = "Draft portfolio sections",
                description = "Write the about page, case studies and a first blog post.",
                startDay = t.plusDays(2).toEpochDayLong(),
                endDay = t.plusDays(9).toEpochDayLong(),
                status = "NOT_STARTED",
                priority = "MEDIUM",
                category = "PERSONAL",
                progress = 25,
                gradientIndex = 1,
            )
        )

        // ---------- Normal tasks ----------
        taskDao.upsert(
            TaskEntity(
                targetId = roomTargetId,
                title = "Read Room documentation",
                description = "Focus on DAOs, migrations and TypeConverters.",
                dueDay = t.toEpochDayLong(),
                priority = "HIGH",
                category = "STUDY",
            )
        )
        taskDao.upsert(
            TaskEntity(
                projectId = expenseId,
                title = "Push project to GitHub",
                dueDay = t.toEpochDayLong(),
                priority = "MEDIUM",
                category = "PROJECTS",
            )
        )
        taskDao.upsert(
            TaskEntity(
                title = "Complete Compose exercise",
                dueDay = t.toEpochDayLong(),
                priority = "MEDIUM",
                category = "STUDY",
            )
        )
        taskDao.upsert(
            TaskEntity(
                goalId = gateId,
                title = "Submit GATE assignment",
                dueDay = t.plusDays(5).toEpochDayLong(),
                priority = "HIGH",
                category = "STUDY",
            )
        )
        taskDao.upsert(
            TaskEntity(
                title = "Review Kotlin notes",
                dueDay = t.plusDays(1).toEpochDayLong(),
                priority = "LOW",
                category = "STUDY",
            )
        )
        taskDao.upsert(
            TaskEntity(
                title = "Buy groceries",
                dueDay = t.plusDays(1).toEpochDayLong(),
                priority = "LOW",
                category = "PERSONAL",
            )
        )
        taskDao.upsert(
            TaskEntity(
                title = "Call home",
                dueDay = t.plusDays(3).toEpochDayLong(),
                priority = "MEDIUM",
                category = "PERSONAL",
            )
        )
        taskDao.upsert(
            TaskEntity(
                title = "Plan weekly workout",
                dueDay = t.plusDays(2).toEpochDayLong(),
                priority = "MEDIUM",
                category = "FITNESS",
            )
        )
        taskDao.upsert(
            TaskEntity(
                title = "Complete Kotlin exercise",
                dueDay = t.minusDays(1).toEpochDayLong(),
                completed = true,
                completedAt = System.currentTimeMillis(),
                priority = "MEDIUM",
                category = "STUDY",
            )
        )

        // ---------- Milestones ----------
        seedMilestones(
            goalId = androidId, projectId = null,
            done = 8,
            listOf(
                "Kotlin fundamentals", "Compose basics", "Navigation", "ViewModel",
                "Coroutines", "Room", "Hilt", "Networking", "WorkManager",
                "Custom views", "Testing", "Publish app",
            ),
        )
        seedMilestones(
            goalId = portfolioId, projectId = null,
            done = 4,
            listOf(
                "Pick template", "Write about page", "Projects section", "Resume page",
                "Blog setup", "Dark mode", "Portfolio items", "SEO basics",
                "Deploy", "Custom domain",
            ),
        )
        seedMilestones(
            goalId = gateId, projectId = null,
            done = 6,
            listOf(
                "Discrete Maths", "Probability", "Algorithms", "Data Structures",
                "Operating Systems", "DBMS", "Computer Networks", "Compiler Design",
                "Theory of Computation", "Digital Logic", "Computer Architecture",
                "Software Engineering", "Aptitude 1", "Aptitude 2", "Aptitude 3",
                "Mock Test 1", "Mock Test 2", "Mock Test 3", "Previous Year Papers", "Revision",
            ),
        )
        seedMilestones(
            goalId = null, projectId = expenseId,
            done = 2,
            listOf("Design & architecture", "Core features", "Polish & ship"),
        )

        // ---------- Subtasks ----------
        seedSubtasks(
            targetId = roomTargetId,
            done = 3,
            listOf(
                "Learn Room basics", "Setup Room dependencies", "Create Entity",
                "Create DAO", "Connect ViewModel",
            ),
        )
        seedSubtasks(
            targetId = expenseUiTargetId,
            done = 1,
            listOf(
                "Design screens in Figma", "Build transactions list",
                "Add add/edit screen", "Chart the spending breakdown", "Polish empty states",
            ),
        )
        seedSubtasks(
            targetId = coroutinesTargetId,
            done = 0,
            listOf("Flow + StateFlow", "Structured concurrency", "Suspend functions in depth", "Build a playground app"),
        )
        seedSubtasks(
            targetId = dsaTargetId,
            done = 2,
            listOf("Arrays & strings", "Linked lists", "Trees", "Dynamic programming", "Revision of solved problems"),
        )

        // ---------- Notes ----------
        targetDao.upsertNote(
            NoteEntity(
                targetId = roomTargetId,
                text = "Remember to check the schema migration docs before changing entities.",
            )
        )

        // ---------- Project tasks (drive the Expense Tracker's progress) ----------
        val projectTasks = listOf(
            "Set up project" to true,
            "Room schema: Transaction + Category" to true,
            "Add/edit screens" to true,
            "List with filters" to true,
            "Search" to true,
            "Charts" to true,
            "Export CSV" to true,
            "Budget tracking" to true,
            "Categories editor" to true,
            "Dark mode" to true,
            "Settings screen" to true,
            "App icon" to true,
            "Unit tests" to false,
            "Onboarding" to false,
            "Home widget" to false,
            "Backup & restore" to false,
            "Release build" to false,
            "Play Store listing" to false,
            "Beta testing" to false,
        )
        projectTasks.forEachIndexed { index, (title, done) ->
            taskDao.upsert(
                TaskEntity(
                    projectId = expenseId,
                    goalId = androidId,
                    title = title,
                    completed = done,
                    completedAt = if (done) System.currentTimeMillis() - (index * 3_600_000L) else null,
                    dueDay = t.plusDays((index % 14).toLong()).toEpochDayLong(),
                    priority = if (done) "MEDIUM" else "HIGH",
                    category = "PROJECTS",
                )
            )
        }
    }

    private suspend fun seedMilestones(goalId: Long?, projectId: Long?, done: Int, titles: List<String>) {
        titles.forEachIndexed { index, title ->
            goalDao.upsertMilestone(
                MilestoneEntity(
                    goalId = goalId,
                    projectId = projectId,
                    title = title,
                    completed = index < done,
                    completedAt = if (index < done) System.currentTimeMillis() - (index * 86_400_000L) else null,
                    position = index,
                )
            )
        }
    }

    private suspend fun seedSubtasks(targetId: Long, done: Int, titles: List<String>) {
        titles.forEachIndexed { index, title ->
            targetDao.upsertSubtask(
                SubtaskEntity(
                    targetId = targetId,
                    title = title,
                    completed = index < done,
                    completedAt = if (index < done) System.currentTimeMillis() - (index * 3_600_000L) else null,
                    position = index,
                )
            )
        }
    }
}
