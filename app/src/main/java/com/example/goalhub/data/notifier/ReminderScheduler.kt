package com.example.goalhub.data.notifier

import com.example.goalhub.domain.model.Target
import com.example.goalhub.domain.model.Task

/**
 * Abstraction for deadline reminders. The current implementation is offline
 * (AlarmManager + a broadcast receiver, see [AlarmReminderScheduler]).
 *
 * Future backends (WorkManager jobs, a foreground service, or server-side
 * push after cloud sync) can swap in by implementing this interface — the
 * repository only talks to this abstraction.
 */
interface ReminderScheduler {

    /** Remind about [target]'s deadline (usually on its end date, morning). */
    fun scheduleTarget(target: Target)

    fun cancelTarget(targetId: Long)

    /** Remind about [task]'s due date. */
    fun scheduleTask(task: Task)

    fun cancelTask(taskId: Long)

    /** Drop every scheduled reminder (e.g. when data is wiped). */
    fun cancelAll()
}
