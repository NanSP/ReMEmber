package com.example.remember.feature.mvp.data.local

import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.domain.GoalRecurrence
import com.example.remember.feature.mvp.domain.MvpGoal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GoalRoomMapperTest {
    @Test
    fun `goal survives room mapping without losing recurrence or reminder`() {
        val goal = MvpGoal(
            id = 42,
            title = "Beber água",
            category = GoalCategory.FOOD_AND_HYDRATION,
            isCompleted = true,
            recurrence = GoalRecurrence.DAILY,
            lastCompletedEpochDay = 20_000,
            reminderMinutesOfDay = 9 * 60 + 30,
            reminderEnabled = true
        )

        val entity = goal.toEntity(createdAtMillis = 10)

        assertEquals(goal, entity.toDomain())
        assertEquals(10, entity.createdAtMillis)
    }

    @Test
    fun `invalid persisted enum is ignored instead of crashing state flow`() {
        val entity = GoalEntity(
            id = 1,
            title = "Inválido",
            category = "UNKNOWN",
            isCompleted = false,
            recurrence = GoalRecurrence.NONE.name,
            lastCompletedEpochDay = null,
            reminderMinutesOfDay = null,
            reminderEnabled = false,
            createdAtMillis = 1
        )

        assertNull(entity.toDomain())
    }

    @Test
    fun `completion entity maps to history snapshot`() {
        val entity = GoalCompletionEntity(
            id = 7,
            goalId = 42,
            goalTitle = "Beber água",
            category = GoalCategory.FOOD_AND_HYDRATION.name,
            completedEpochDay = 20_000,
            completedAtMillis = 123_456,
            experienceEarned = 15,
            coinsEarned = 3
        )

        val history = entity.toHistoryItem()

        assertEquals(7L, history?.id)
        assertEquals("Beber água", history?.goalTitle)
        assertEquals(GoalCategory.FOOD_AND_HYDRATION, history?.category)
        assertEquals(15, history?.experienceEarned)
        assertEquals(3, history?.coinsEarned)
    }
}
