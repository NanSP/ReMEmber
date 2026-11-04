package com.example.remember.feature.mvp.data

import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.domain.GoalRecurrence
import com.example.remember.feature.mvp.domain.MvpGoal
import java.nio.charset.StandardCharsets
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Test

class GoalCodecTest {
    @Test
    fun `goal recurrence and reminder survive persistence round trip`() {
        val goal = MvpGoal(
            id = 42,
            title = "Beber água, estudar e revisar",
            category = GoalCategory.FOOD_AND_HYDRATION,
            isCompleted = true,
            recurrence = GoalRecurrence.DAILY,
            lastCompletedEpochDay = 20_401,
            reminderMinutesOfDay = 8 * 60 + 30,
            reminderEnabled = true
        )

        assertEquals(listOf(goal), GoalCodec.decode(GoalCodec.encode(listOf(goal))))
    }

    @Test
    fun `legacy four field goal remains compatible`() {
        val title = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("Objetivo antigo".toByteArray(StandardCharsets.UTF_8))
        val legacy = "7,$title,STUDY,false"
        val decoded = GoalCodec.decode(legacy).single()

        assertEquals(GoalRecurrence.NONE, decoded.recurrence)
        assertEquals(null, decoded.reminderMinutesOfDay)
        assertEquals(false, decoded.reminderEnabled)
    }
}
