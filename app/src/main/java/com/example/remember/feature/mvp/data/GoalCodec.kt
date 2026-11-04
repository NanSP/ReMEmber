package com.example.remember.feature.mvp.data

import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.domain.GoalRecurrence
import com.example.remember.feature.mvp.domain.MvpGoal
import java.nio.charset.StandardCharsets
import java.util.Base64

internal object GoalCodec {
    fun encode(goals: List<MvpGoal>): String = goals.joinToString(";") { goal ->
        listOf(
            goal.id.toString(),
            encodeText(goal.title),
            goal.category.name,
            goal.isCompleted.toString(),
            goal.recurrence.name,
            goal.lastCompletedEpochDay?.toString().orEmpty(),
            goal.reminderMinutesOfDay?.toString().orEmpty(),
            goal.reminderEnabled.toString()
        ).joinToString(",")
    }

    fun decode(value: String?): List<MvpGoal> = value
        ?.split(';')
        ?.mapNotNull { encodedGoal ->
            val parts = encodedGoal.split(',')
            if (parts.size != 4 && parts.size != 8) return@mapNotNull null
            runCatching {
                MvpGoal(
                    id = parts[0].toLong(),
                    title = decodeText(parts[1]),
                    category = GoalCategory.valueOf(parts[2]),
                    isCompleted = parts[3].toBooleanStrict(),
                    recurrence = parts.getOrNull(4)?.let(GoalRecurrence::valueOf)
                        ?: GoalRecurrence.NONE,
                    lastCompletedEpochDay = parts.getOrNull(5)?.toLongOrNull(),
                    reminderMinutesOfDay = parts.getOrNull(6)?.toIntOrNull(),
                    reminderEnabled = parts.getOrNull(7)?.toBooleanStrictOrNull() ?: false
                )
            }.getOrNull()
        }
        .orEmpty()

    private fun encodeText(value: String): String = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decodeText(value: String): String = String(
        Base64.getUrlDecoder().decode(value),
        StandardCharsets.UTF_8
    )
}
