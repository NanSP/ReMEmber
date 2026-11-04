package com.example.remember.feature.mvp.domain

import com.example.remember.feature.goals.domain.model.GoalCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryFiltersTest {
    private val today = 1_000L
    private val items = listOf(
        completion(1, 10, GoalCategory.STUDY, today),
        completion(2, 11, GoalCategory.EXERCISE, today - 6),
        completion(3, 10, GoalCategory.STUDY, today - 7),
        completion(4, 12, GoalCategory.SIMPLE_TASK, today - 29),
        completion(5, 13, GoalCategory.FOOD_AND_HYDRATION, today - 30)
    )

    @Test
    fun `seven day period includes today and previous six days`() {
        val result = filterCompletionHistory(
            items,
            HistoryPeriod.LAST_7_DAYS,
            category = null,
            goalId = null,
            currentEpochDay = today
        )

        assertEquals(listOf(1L, 2L), result.map { it.id })
    }

    @Test
    fun `period category and goal filters are combined`() {
        val result = filterCompletionHistory(
            items,
            HistoryPeriod.LAST_30_DAYS,
            category = GoalCategory.STUDY,
            goalId = 10,
            currentEpochDay = today
        )

        assertEquals(listOf(1L, 3L), result.map { it.id })
    }

    @Test
    fun `all period keeps old completions`() {
        val result = filterCompletionHistory(
            items,
            HistoryPeriod.ALL,
            category = null,
            goalId = null,
            currentEpochDay = today
        )

        assertEquals(items, result)
    }

    private fun completion(
        id: Long,
        goalId: Long,
        category: GoalCategory,
        epochDay: Long
    ) = CompletionHistoryItem(
        id = id,
        goalId = goalId,
        goalTitle = "Objetivo $goalId",
        category = category,
        completedEpochDay = epochDay,
        completedAtMillis = epochDay,
        experienceEarned = 10,
        coinsEarned = 2
    )
}
