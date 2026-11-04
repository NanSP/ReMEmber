package com.example.remember.feature.mvp.domain

import com.example.remember.feature.goals.domain.model.GoalCategory
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalRecurrenceTest {
    @Test
    fun `one time goal stays completed`() {
        val goal = goal(GoalRecurrence.NONE, completed = true, completionDay = 100)

        assertTrue(goal.isCompletedAt(100))
        assertTrue(goal.isCompletedAt(200))
    }

    @Test
    fun `daily goal becomes active on the next day`() {
        val goal = goal(GoalRecurrence.DAILY, completed = true, completionDay = 100)

        assertTrue(goal.isCompletedAt(100))
        assertFalse(goal.isCompletedAt(101))
    }

    @Test
    fun `weekly goal becomes active seven days after completion`() {
        val goal = goal(GoalRecurrence.WEEKLY, completed = true, completionDay = 100)

        assertTrue(goal.isCompletedAt(106))
        assertFalse(goal.isCompletedAt(107))
    }

    @Test
    fun `future reminder uses today when selected time has not passed`() {
        val now = ZonedDateTime.of(2026, 11, 4, 8, 0, 0, 0, ZoneId.of("America/Bahia"))
        val trigger = GoalReminderRules.nextTriggerMillis(
            reminderMinutesOfDay = 9 * 60,
            recurrence = GoalRecurrence.NONE,
            lastCompletedEpochDay = null,
            now = now
        )

        assertEquals(now.toLocalDate(), ZonedDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(trigger),
            now.zone
        ).toLocalDate())
    }

    @Test
    fun `past reminder rolls to tomorrow`() {
        val now = ZonedDateTime.of(2026, 11, 4, 10, 0, 0, 0, ZoneId.of("America/Bahia"))
        val trigger = GoalReminderRules.nextTriggerMillis(
            reminderMinutesOfDay = 9 * 60,
            recurrence = GoalRecurrence.NONE,
            lastCompletedEpochDay = null,
            now = now
        )

        assertEquals(now.toLocalDate().plusDays(1), ZonedDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(trigger),
            now.zone
        ).toLocalDate())
    }

    @Test
    fun `weekly reminder follows completion anchor`() {
        val zone = ZoneId.of("America/Bahia")
        val completionDay = LocalDate.of(2026, 11, 4).toEpochDay()
        val now = ZonedDateTime.of(2026, 11, 5, 8, 0, 0, 0, zone)
        val trigger = GoalReminderRules.nextTriggerMillis(
            reminderMinutesOfDay = 9 * 60,
            recurrence = GoalRecurrence.WEEKLY,
            lastCompletedEpochDay = completionDay,
            now = now
        )

        assertEquals(LocalDate.of(2026, 11, 11), ZonedDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(trigger),
            zone
        ).toLocalDate())
    }

    private fun goal(
        recurrence: GoalRecurrence,
        completed: Boolean,
        completionDay: Long
    ) = MvpGoal(
        id = 1,
        title = "Teste",
        category = GoalCategory.STUDY,
        isCompleted = completed,
        recurrence = recurrence,
        lastCompletedEpochDay = completionDay
    )
}
