package com.example.goalhub.data.notifier

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.example.goalhub.domain.model.Target
import com.example.goalhub.domain.model.Task
import com.example.goalhub.util.toDeviceZoneMillis
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Offline reminders using [AlarmManager]. A reminder fires on the item's end /
 * due date at [REMINDER_HOUR] in the device's local timezone and posts a
 * notification via [ReminderReceiver].
 *
 * If notifications are not granted (Android 13+), scheduling silently no-ops.
 */
class AlarmReminderScheduler(private val context: Context) : ReminderScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleTarget(target: Target) {
        schedule(target.id, target.title, target.end, "Target deadline")
    }

    override fun cancelTarget(targetId: Long) = cancel(targetId, ReminderReceiver.TYPE_TARGET)

    override fun scheduleTask(task: Task) {
        val due = task.due ?: return
        schedule(task.id, task.title, due, "Task due")
    }

    override fun cancelTask(taskId: Long) = cancel(taskId, ReminderReceiver.TYPE_TASK)

    override fun cancelAll() {
        ReminderReceiver.ALL_TYPES.forEach { type ->
            // A single wildcard intent id: request codes must be unique per type.
            cancel(ReminderReceiver.WILDCARD_ID, type)
        }
    }

    private fun schedule(itemId: Long, title: String, date: LocalDate, channel: String) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        if (date < LocalDate.now()) return

        val triggerAt = date.atTime(REMINDER_HOUR).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val requestCode = requestCode(itemId, channel)
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMIND
            putExtra(ReminderReceiver.EXTRA_TYPE, channel)
            putExtra(ReminderReceiver.EXTRA_ID, itemId)
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_DATE, date.toDeviceZoneMillis())
        }
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    private fun cancel(itemId: Long, channel: String) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMIND
            putExtra(ReminderReceiver.EXTRA_TYPE, channel)
            putExtra(ReminderReceiver.EXTRA_ID, itemId)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode(itemId, channel),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pending)
    }

    /** Stable per (type, id) request codes so updating a reminder replaces it. */
    private fun requestCode(itemId: Long, channel: String): Int {
        val base = ReminderReceiver.typeRequestBase(channel)
        return base + (itemId % 1_000_000).toInt()
    }

    companion object {
        private val REMINDER_HOUR: LocalTime = LocalTime.of(9, 0)
    }
}
