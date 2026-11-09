package com.example.remember.feature.mvp.domain

import com.example.remember.feature.goals.domain.model.GoalCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyBonusMissionTest {
    @Test
    fun timedMissionOnlyCompletesAfterTheRequiredDuration() {
        val start = 1_000_000L
        val mission = DailyBonusMission(
            category = GoalCategory.STUDY,
            completed = false,
            startedAtMillis = start
        )

        assertFalse(mission.canComplete(start))
        assertEquals(
            GamificationRules.STUDY_BONUS_DURATION_MILLIS,
            mission.remainingMillis(start)
        )
        assertFalse(
            mission.canComplete(start + GamificationRules.STUDY_BONUS_DURATION_MILLIS - 1L)
        )
        assertTrue(
            mission.canComplete(start + GamificationRules.STUDY_BONUS_DURATION_MILLIS)
        )
    }

    @Test
    fun missionsWithoutDurationCanBeCompletedImmediately() {
        val mission = DailyBonusMission(
            category = GoalCategory.FOOD_AND_HYDRATION,
            completed = false
        )

        assertFalse(mission.requiresTimer)
        assertTrue(mission.canComplete(0L))
    }
}
