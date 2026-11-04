package com.example.remember.feature.mvp.domain

import java.time.LocalDate
import java.time.ZonedDateTime

object GoalReminderRules {
    fun nextTriggerMillis(
        reminderMinutesOfDay: Int,
        recurrence: GoalRecurrence,
        lastCompletedEpochDay: Long?,
        now: ZonedDateTime
    ): Long {
        require(reminderMinutesOfDay in 0 until 24 * 60)
        val firstEligibleDay = when {
            lastCompletedEpochDay == null -> now.toLocalDate()
            recurrence == GoalRecurrence.DAILY -> LocalDate.ofEpochDay(lastCompletedEpochDay + 1)
            recurrence == GoalRecurrence.WEEKLY -> LocalDate.ofEpochDay(lastCompletedEpochDay + 7)
            else -> now.toLocalDate()
        }
        var trigger = firstEligibleDay
            .atTime(reminderMinutesOfDay / 60, reminderMinutesOfDay % 60)
            .atZone(now.zone)
        if (!trigger.isAfter(now)) trigger = trigger.plusDays(1)
        return trigger.toInstant().toEpochMilli()
    }
}
