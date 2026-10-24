package com.example.goalhub

import android.app.Application
import android.content.Context
import com.example.goalhub.data.local.GoalPlannerDatabase
import com.example.goalhub.data.local.SettingsStore
import com.example.goalhub.data.notifier.AlarmReminderScheduler
import com.example.goalhub.data.notifier.ReminderScheduler
import com.example.goalhub.data.repository.GoalPlannerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Manual dependency container — intentionally Hilt-free so the project stays
 * easy to read and refactor. Swap for Hilt/Koin later without touching screens:
 * they only ever see the repository / settings from [container].
 */
class AppContainer(context: Context) {

    private val database: GoalPlannerDatabase = GoalPlannerDatabase.build(context)

    val settings: SettingsStore = SettingsStore(context)

    val scheduler: ReminderScheduler = AlarmReminderScheduler(context)

    val repository: GoalPlannerRepository = GoalPlannerRepository(
        goalDao = database.goalDao(),
        projectDao = database.projectDao(),
        targetDao = database.targetDao(),
        taskDao = database.taskDao(),
        scheduler = scheduler,
    )
}

class GoalHubApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Seed sample data on first launch so the UI never looks empty.
        applicationScope.launch { container.repository.seedIfEmpty() }
    }
}
