package com.example.goalhub.data.notifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.goalhub.MainActivity
import com.example.goalhub.R
import com.example.goalhub.util.formatShort
import com.example.goalhub.util.fromUtcMillis

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMIND) return

        val type = intent.getStringExtra(EXTRA_TYPE) ?: return
        val id = intent.getLongExtra(EXTRA_ID, 0L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Reminder"
        val dateMillis = intent.getLongExtra(EXTRA_DATE, 0L)

        ensureChannel(context)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TYPE, type)
            putExtra(EXTRA_ID, id)
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            id.toInt(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val dateLabel = if (dateMillis != 0L) dateMillis.fromUtcMillis().formatShort() else ""
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(if (type == TYPE_TARGET) "Target deadline" else "Task due")
            .setContentText("$title${if (dateLabel.isNotEmpty()) " · $dateLabel" else ""}")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$title — $dateLabel"))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId(type, id), notification)
    }

    companion object {
        const val ACTION_REMIND = "com.example.goalhub.action.REMIND"
        const val EXTRA_TYPE = "reminder_type"
        const val EXTRA_ID = "reminder_id"
        const val EXTRA_TITLE = "reminder_title"
        const val EXTRA_DATE = "reminder_date"

        const val TYPE_TARGET = "Target deadline"
        const val TYPE_TASK = "Task due"

        /** Used by cancelAll(); collisions with real ids are avoided by the request-code layout. */
        const val WILDCARD_ID = Long.MAX_VALUE

        val ALL_TYPES = listOf(TYPE_TARGET, TYPE_TASK)

        const val CHANNEL_ID = "goalhub_deadlines"

        private const val REQUEST_BASE_TARGET = 10_000_000
        private const val REQUEST_BASE_TASK = 20_000_000
        private const val NOTIFY_BASE_TARGET = 1_000
        private const val NOTIFY_BASE_TASK = 2_000

        fun typeRequestBase(type: String): Int =
            if (type == TYPE_TARGET) REQUEST_BASE_TARGET else REQUEST_BASE_TASK

        private fun notificationId(type: String, itemId: Long): Int =
            (if (type == TYPE_TARGET) NOTIFY_BASE_TARGET else NOTIFY_BASE_TASK) + (itemId % 1_000_000).toInt()

        private fun ensureChannel(context: Context) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "Deadline reminders",
                        NotificationManager.IMPORTANCE_DEFAULT,
                    )
                )
            }
        }
    }
}

private fun Long.formatShort(): String = fromUtcMillis().formatShort()
