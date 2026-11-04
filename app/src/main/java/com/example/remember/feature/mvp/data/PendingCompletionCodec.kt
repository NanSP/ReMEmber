package com.example.remember.feature.mvp.data

import com.example.remember.feature.goals.domain.model.GoalCategory
import java.nio.charset.StandardCharsets
import java.util.Base64

internal data class PendingCompletion(
    val eventId: String,
    val goalId: Long,
    val goalTitle: String,
    val category: GoalCategory,
    val completedEpochDay: Long,
    val completedAtMillis: Long,
    val experienceEarned: Int,
    val coinsEarned: Int,
    val advancesPrimaryStreak: Boolean,
    val localHour: Int
)

internal object PendingCompletionCodec {
    fun encode(events: List<PendingCompletion>): String = events.joinToString(";") { event ->
        listOf(
            event.eventId,
            event.goalId,
            encodeText(event.goalTitle),
            event.category.name,
            event.completedEpochDay,
            event.completedAtMillis,
            event.experienceEarned,
            event.coinsEarned,
            event.advancesPrimaryStreak,
            event.localHour
        ).joinToString(",")
    }

    fun decode(value: String?): List<PendingCompletion> = value
        ?.split(';')
        ?.mapNotNull { encoded ->
            val parts = encoded.split(',')
            if (parts.size != 10) return@mapNotNull null
            runCatching {
                PendingCompletion(
                    eventId = parts[0],
                    goalId = parts[1].toLong(),
                    goalTitle = decodeText(parts[2]),
                    category = GoalCategory.valueOf(parts[3]),
                    completedEpochDay = parts[4].toLong(),
                    completedAtMillis = parts[5].toLong(),
                    experienceEarned = parts[6].toInt(),
                    coinsEarned = parts[7].toInt(),
                    advancesPrimaryStreak = parts[8].toBooleanStrict(),
                    localHour = parts[9].toInt()
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
