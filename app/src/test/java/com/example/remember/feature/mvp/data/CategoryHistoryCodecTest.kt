package com.example.remember.feature.mvp.data

import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.domain.CategoryHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryHistoryCodecTest {
    @Test
    fun `category history survives persistence round trip`() {
        val histories = GoalCategory.entries.associateWith { category ->
            CategoryHistory(
                category = category,
                currentStreak = category.ordinal + 1,
                longestStreak = category.ordinal + 10,
                totalCompletions = category.ordinal + 20,
                lastPrimaryCompletionDay = 20_000L + category.ordinal
            )
        }

        assertEquals(histories, CategoryHistoryCodec.decode(CategoryHistoryCodec.encode(histories)))
    }

    @Test
    fun `missing categories are encoded with an empty history`() {
        val encoded = CategoryHistoryCodec.encode(
            mapOf(
                GoalCategory.STUDY to CategoryHistory(
                    category = GoalCategory.STUDY,
                    longestStreak = 12
                )
            )
        )
        val decoded = CategoryHistoryCodec.decode(encoded)

        assertEquals(12, decoded.getValue(GoalCategory.STUDY).longestStreak)
        assertEquals(0, decoded.getValue(GoalCategory.EXERCISE).longestStreak)
        assertEquals(GoalCategory.entries.size, decoded.size)
    }

    @Test
    fun `malformed history entries are ignored`() {
        assertTrue(CategoryHistoryCodec.decode("invalid;STUDY,not-a-number,2,3,4").isEmpty())
    }
}
