package com.example.remember.feature.mvp.domain

import com.example.remember.feature.goals.domain.model.GoalCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrimaryCategoryRulesTest {
    @Test
    fun `existing profile without change date may change immediately`() {
        assertEquals(0, PrimaryCategoryRules.daysUntilChange(null, currentDay = 100))
    }

    @Test
    fun `category remains locked for five calendar days`() {
        assertEquals(5, PrimaryCategoryRules.daysUntilChange(lastChangeDay = 100, currentDay = 100))
        assertEquals(4, PrimaryCategoryRules.daysUntilChange(lastChangeDay = 100, currentDay = 101))
        assertEquals(1, PrimaryCategoryRules.daysUntilChange(lastChangeDay = 100, currentDay = 104))
        assertEquals(0, PrimaryCategoryRules.daysUntilChange(lastChangeDay = 100, currentDay = 105))
    }

    @Test
    fun `clock rollback cannot bypass category lock`() {
        assertTrue(PrimaryCategoryRules.daysUntilChange(lastChangeDay = 100, currentDay = 99) > 0)
    }

    @Test
    fun `state exposes active category history independently`() {
        val study = CategoryHistory(
            category = GoalCategory.STUDY,
            currentStreak = 8,
            longestStreak = 14,
            totalCompletions = 20
        )
        val exercise = CategoryHistory(
            category = GoalCategory.EXERCISE,
            currentStreak = 2,
            longestStreak = 5,
            totalCompletions = 9
        )
        val state = MvpState(
            primaryCategory = GoalCategory.EXERCISE,
            currentStreak = exercise.currentStreak,
            longestStreak = exercise.longestStreak,
            categoryHistory = mapOf(
                GoalCategory.STUDY to study,
                GoalCategory.EXERCISE to exercise
            ),
            primaryCategoryChangeDaysRemaining = 2
        )

        assertEquals(study, state.historyFor(GoalCategory.STUDY))
        assertEquals(exercise, state.historyFor(GoalCategory.EXERCISE))
        assertEquals(0, state.historyFor(GoalCategory.SIMPLE_TASK).totalCompletions)
        assertFalse(state.canChangePrimaryCategory)
        assertEquals(2, state.currentStreak)
        assertEquals(5, state.longestStreak)
    }

    @Test
    fun `state permits category change when countdown reaches zero`() {
        assertTrue(MvpState(primaryCategoryChangeDaysRemaining = 0).canChangePrimaryCategory)
    }
}
