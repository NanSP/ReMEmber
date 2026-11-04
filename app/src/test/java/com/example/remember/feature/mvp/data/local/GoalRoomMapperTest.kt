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
}
