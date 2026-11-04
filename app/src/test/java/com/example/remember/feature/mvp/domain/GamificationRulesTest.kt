package com.example.remember.feature.mvp.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GamificationRulesTest {
    @Test
    fun `achievement catalog unlocks all supported requirement criteria`() {
        val achievements = GamificationRules.unlockedAchievements(
            AchievementStats(
                totalCompletions = 1,
                highestCategoryCompletions = 10,
                highestDailyCompletions = 3,
                nightCompletions = 5,
                bonusMissionsCompleted = 5,
                longestStreak = 7,
                activeCategories = 2,
                totalCoinsEarned = 500,
                totalExperience = 1_000,
                accessoriesOwned = 2
            )
        )

        assertEquals(AchievementId.entries.toSet(), achievements)
    }

    @Test
    fun `achievement thresholds do not unlock early`() {
        val achievements = GamificationRules.unlockedAchievements(
            AchievementStats(
                totalCompletions = 0,
                highestCategoryCompletions = 9,
                highestDailyCompletions = 2,
                nightCompletions = 4,
                bonusMissionsCompleted = 4,
                longestStreak = 6,
                activeCategories = 1,
                totalCoinsEarned = 499,
                totalExperience = 999,
                accessoriesOwned = 1
            )
        )

        assertTrue(achievements.isEmpty())
    }

    @Test
    fun `streak recovery is available only on day after loss`() {
        assertTrue(
            GamificationRules.canRecoverStreak(
                currentStreak = 8,
                lastPrimaryCompletionDay = 100,
                currentDay = 102,
                recoveriesUsedThisMonth = 0
            )
        )
        assertFalse(
            GamificationRules.canRecoverStreak(
                currentStreak = 8,
                lastPrimaryCompletionDay = 100,
                currentDay = 103,
                recoveriesUsedThisMonth = 0
            )
        )
    }

    @Test
    fun `only two global recoveries are available each month`() {
        assertTrue(
            GamificationRules.canRecoverStreak(5, 100, 102, recoveriesUsedThisMonth = 1)
        )
        assertFalse(
            GamificationRules.canRecoverStreak(5, 100, 102, recoveriesUsedThisMonth = 2)
        )
    }

    @Test
    fun `empty streak cannot be recovered`() {
        assertFalse(
            GamificationRules.canRecoverStreak(0, 100, 102, recoveriesUsedThisMonth = 0)
        )
    }
}
