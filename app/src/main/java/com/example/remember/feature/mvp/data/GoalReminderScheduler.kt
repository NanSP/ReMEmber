package com.example.remember.feature.mvp.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.remember.feature.mvp.domain.GoalRecurrence
import com.example.remember.feature.mvp.domain.GoalReminderRules
import com.example.remember.feature.mvp.domain.MvpGoal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

internal class GoalReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun sync(goal: MvpGoal) {
        cancel(goal.id)
        if (!goal.reminderEnabled || goal.reminderMinutesOfDay == null) return
        if (goal.recurrence == GoalRecurrence.NONE && goal.isCompleted) return
        scheduleAt(
            goal,
            GoalReminderRules.nextTriggerMillis(
                reminderMinutesOfDay = goal.reminderMinutesOfDay,
                recurrence = goal.recurrence,
                lastCompletedEpochDay = goal.lastCompletedEpochDay,
                now = ZonedDateTime.now()
            )
        )
    }

    fun scheduleNextAfterTrigger(goal: MvpGoal, triggerMillis: Long) {
        val days = when (goal.recurrence) {
            GoalRecurrence.NONE -> return
            GoalRecurrence.DAILY -> 1L
            GoalRecurrence.WEEKLY -> 7L
        }
        val next = Instant.ofEpochMilli(triggerMillis)
            .atZone(ZoneId.systemDefault())
            .plusDays(days)
            .toInstant()
            .toEpochMilli()
        scheduleAt(goal, next)
    }

    fun cancel(goalId: Long) {
        alarmManager.cancel(pendingIntent(goalId, PendingIntent.FLAG_NO_CREATE))
    }

    private fun scheduleAt(goal: MvpGoal, triggerMillis: Long) {
        val operation = pendingIntent(goal.id, PendingIntent.FLAG_UPDATE_CURRENT, goal)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                operation
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                operation
            )
        }
    }

    private fun pendingIntent(
        goalId: Long,
        behaviorFlag: Int,
        goal: MvpGoal? = null
    ): PendingIntent {
        val intent = Intent(context, GoalReminderReceiver::class.java).apply {
            action = ACTION_REMIND_GOAL
            putExtra(EXTRA_GOAL_ID, goalId)
            goal?.let { putExtra(EXTRA_TRIGGER_HINT, System.currentTimeMillis()) }
        }
        return PendingIntent.getBroadcast(
            context,
            goalId.hashCode(),
            intent,
            behaviorFlag or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_REMIND_GOAL = "com.example.remember.REMIND_GOAL"
        const val EXTRA_GOAL_ID = "goal_id"
        const val EXTRA_TRIGGER_HINT = "trigger_hint"
    }
}
