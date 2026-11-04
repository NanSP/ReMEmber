package com.example.remember.feature.mvp.domain

import com.example.remember.feature.goals.domain.model.GoalCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class RewardRulesTest {
    @Test
    fun `primary category grants a larger reward`() {
        val reward = RewardRules.completionReward(
            category = GoalCategory.STUDY,
            primaryCategory = GoalCategory.STUDY,
            isFirstPrimaryCompletionToday = true
        )

        assertEquals(40, reward.experience)
        assertEquals(15, reward.coins)
    }

    @Test
    fun `secondary category grants the base reward`() {
        val reward = RewardRules.completionReward(
            category = GoalCategory.EXERCISE,
            primaryCategory = GoalCategory.STUDY,
            isFirstPrimaryCompletionToday = false
        )

        assertEquals(25, reward.experience)
        assertEquals(10, reward.coins)
    }

    @Test
    fun `additional primary completion receives only base reward`() {
        val reward = RewardRules.completionReward(
            category = GoalCategory.STUDY,
            primaryCategory = GoalCategory.STUDY,
            isFirstPrimaryCompletionToday = false
        )

        assertEquals(25, reward.experience)
        assertEquals(10, reward.coins)
    }

    @Test
    fun `only first primary completion of device day advances daily rules`() {
        assertEquals(
            true,
            RewardRules.isFirstPrimaryCompletionToday(
                GoalCategory.STUDY,
                GoalCategory.STUDY,
                lastPrimaryCompletionDay = 10,
                currentDay = 11
            )
        )
        assertEquals(
            false,
            RewardRules.isFirstPrimaryCompletionToday(
                GoalCategory.STUDY,
                GoalCategory.STUDY,
                lastPrimaryCompletionDay = 11,
                currentDay = 11
            )
        )
        assertEquals(
            false,
            RewardRules.isFirstPrimaryCompletionToday(
                GoalCategory.EXERCISE,
                GoalCategory.STUDY,
                lastPrimaryCompletionDay = 10,
                currentDay = 11
            )
        )
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

    @Test
    fun `inactive streak displays zero after a missed day`() {
        assertEquals(5, RewardRules.activeStreak(5, lastCompletionDay = 10, currentDay = 11))
        assertEquals(0, RewardRules.activeStreak(5, lastCompletionDay = 10, currentDay = 12))
    }

    @Test
    fun `clock rollback neither increments streak nor moves it backwards`() {
        assertEquals(5, RewardRules.nextStreak(5, lastCompletionDay = 12, currentDay = 11))
        assertEquals(5, RewardRules.activeStreak(5, lastCompletionDay = 12, currentDay = 11))
    }

    @Test
    fun `first completion always starts at one`() {
        assertEquals(1, RewardRules.nextStreak(0, lastCompletionDay = 20, currentDay = 20))
        assertEquals(1, RewardRules.nextStreak(0, lastCompletionDay = Long.MIN_VALUE, currentDay = 20))
    }

    @Test
    fun `season title unlocks when the eight scene stages are complete`() {
        assertEquals(false, RewardRules.hasSeasonTitle(55))
        assertEquals(true, RewardRules.hasSeasonTitle(56))
        assertEquals(true, RewardRules.hasSeasonTitle(120))
    }

    @Test
    fun `first recurring season reward is granted thirty days after title`() {
        assertEquals(
            CompletionReward(experience = 0, coins = 0),
            RewardRules.seasonReward(previousLongestStreak = 84, newLongestStreak = 85)
        )
        assertEquals(
            CompletionReward(experience = 100, coins = 50),
            RewardRules.seasonReward(previousLongestStreak = 85, newLongestStreak = 86)
        )
    }

    @Test
    fun `season reward is not repeated below the next record milestone`() {
        assertEquals(
            CompletionReward(experience = 0, coins = 0),
            RewardRules.seasonReward(previousLongestStreak = 86, newLongestStreak = 86)
        )
        assertEquals(
            CompletionReward(experience = 0, coins = 0),
            RewardRules.seasonReward(previousLongestStreak = 86, newLongestStreak = 100)
        )
        assertEquals(
            CompletionReward(experience = 100, coins = 50),
            RewardRules.seasonReward(previousLongestStreak = 115, newLongestStreak = 116)
        )
    }

    @Test
    fun `season reward countdown restarts after each milestone`() {
        assertEquals(null, RewardRules.daysUntilNextSeasonReward(55))
        assertEquals(30, RewardRules.daysUntilNextSeasonReward(56))
        assertEquals(1, RewardRules.daysUntilNextSeasonReward(85))
        assertEquals(30, RewardRules.daysUntilNextSeasonReward(86))
    }
}
