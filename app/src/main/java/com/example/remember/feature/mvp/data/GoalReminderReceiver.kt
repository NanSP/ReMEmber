package com.example.remember.feature.mvp.data

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.remember.MainActivity
import com.example.remember.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoalReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val goalId = intent.getLongExtra(GoalReminderScheduler.EXTRA_GOAL_ID, Long.MIN_VALUE)
        if (goalId == Long.MIN_VALUE) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = MvpRepository(context.applicationContext).reminderSnapshot(goalId)
                    ?: return@launch
                ReminderNotification.show(
                    context = context,
                    goalId = goalId,
                    title = snapshot.goal.title,
                    soundEnabled = snapshot.soundEnabled,
                    vibrationEnabled = snapshot.vibrationEnabled
                )
                GoalReminderScheduler(context.applicationContext).scheduleNextAfterTrigger(
                    snapshot.goal,
                    System.currentTimeMillis()
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MvpRepository(context.applicationContext).rescheduleReminders()
            } finally {
                pendingResult.finish()
            }
        }
    }
}

private object ReminderNotification {
    fun show(
        context: Context,
        goalId: Long,
        title: String,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val channelId = "goal_reminders_${soundEnabled}_${vibrationEnabled}"
        createChannel(manager, channelId, soundEnabled, vibrationEnabled)
        val contentIntent = PendingIntent.getActivity(
            context,
            goalId.hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(title)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_REMINDER)
            .build()
        manager.notify(goalId.hashCode(), notification)
    }

    private fun createChannel(
        manager: NotificationManager,
        channelId: String,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        if (manager.getNotificationChannel(channelId) != null) return
        val channel = NotificationChannel(
            channelId,
            "Lembretes de objetivos",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Lembretes configurados para objetivos do ReMEmber"
            enableVibration(vibrationEnabled)
            if (!soundEnabled) setSound(null, null)
        }
        manager.createNotificationChannel(channel)
    }
}
