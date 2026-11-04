package com.example.remember.feature.mvp.data

import com.example.remember.feature.goals.domain.model.GoalCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PendingCompletionCodecTest {
    @Test
    fun `pending completions survive serialization with punctuation and accents`() {
        val events = listOf(
            PendingCompletion(
                eventId = "42:20001",
                goalId = 42,
                goalTitle = "Beber água, chá; e café",
                category = GoalCategory.FOOD_AND_HYDRATION,
                completedEpochDay = 20_001,
                completedAtMillis = 1_700_000_000_000,
                experienceEarned = 40,
                coinsEarned = 15,
                advancesPrimaryStreak = true,
                localHour = 8
            )
        )

        assertEquals(events, PendingCompletionCodec.decode(PendingCompletionCodec.encode(events)))
    }

    @Test
    fun `invalid journal entries are ignored`() {
        assertTrue(PendingCompletionCodec.decode("incomplete,event").isEmpty())
    }
}
