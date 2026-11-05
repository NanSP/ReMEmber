package com.example.remember.feature.mvp.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.remember.feature.mvp.domain.MvpGoal

/** Creates reminder intents and explicitly models the absence of a scheduled alarm. */
internal class GoalReminderPendingIntentFactory(private val context: Context) {
    fun findExisting(goalId: Long): PendingIntent? = PendingIntent.getBroadcast(
        context,
        goalId.hashCode(),
        reminderIntent(goalId),
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )

    fun createOrUpdate(goal: MvpGoal): PendingIntent = PendingIntent.getBroadcast(
        context,
        goal.id.hashCode(),
        reminderIntent(goal.id).apply {
            putExtra(GoalReminderScheduler.EXTRA_TRIGGER_HINT, System.currentTimeMillis())
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun reminderIntent(goalId: Long) = Intent(context, GoalReminderReceiver::class.java).apply {
        action = GoalReminderScheduler.ACTION_REMIND_GOAL
        putExtra(GoalReminderScheduler.EXTRA_GOAL_ID, goalId)
    }
}
