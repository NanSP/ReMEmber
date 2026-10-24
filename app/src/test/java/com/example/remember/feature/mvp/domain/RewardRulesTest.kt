package com.example.remember.feature.mvp.domain

import com.example.remember.feature.goals.domain.model.GoalCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class RewardRulesTest {
    @Test
    fun `primary category grants a larger reward`() {
        val reward = RewardRules.completionReward(
            category = GoalCategory.STUDY,
            primaryCategory = GoalCategory.STUDY
        )

        assertEquals(40, reward.experience)
        assertEquals(15, reward.coins)
    }

    @Test
    fun `secondary category grants the base reward`() {
        val reward = RewardRules.completionReward(
            category = GoalCategory.EXERCISE,
            primaryCategory = GoalCategory.STUDY
        )

        assertEquals(25, reward.experience)
        assertEquals(10, reward.coins)
    }

    @Test
    fun `streak grows only on the following day`() {
        assertEquals(4, RewardRules.nextStreak(3, lastCompletionDay = 10, currentDay = 11))
        assertEquals(3, RewardRules.nextStreak(3, lastCompletionDay = 11, currentDay = 11))
    }

    @Test
    fun `streak restarts after a missed day`() {
        assertEquals(
            1,
            RewardRules.nextStreak(8, lastCompletionDay = 10, currentDay = 12)
        )
    }
}
