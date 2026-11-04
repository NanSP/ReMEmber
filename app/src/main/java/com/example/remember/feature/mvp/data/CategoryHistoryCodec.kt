package com.example.remember.feature.mvp.data

import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.domain.CategoryHistory

internal object CategoryHistoryCodec {
    fun encode(histories: Map<GoalCategory, CategoryHistory>): String =
        GoalCategory.entries.joinToString(";") { category ->
            val history = histories[category] ?: CategoryHistory(category)
            listOf(
                category.name,
                history.currentStreak,
                history.longestStreak,
                history.totalCompletions,
                history.lastPrimaryCompletionDay
            ).joinToString(",")
        }

    fun decode(value: String?): Map<GoalCategory, CategoryHistory> = value
        ?.split(';')
        ?.mapNotNull { encoded ->
            val parts = encoded.split(',')
            if (parts.size != 5) return@mapNotNull null
            runCatching {
                val category = GoalCategory.valueOf(parts[0])
                category to CategoryHistory(
                    category = category,
                    currentStreak = parts[1].toInt(),
                    longestStreak = parts[2].toInt(),
                    totalCompletions = parts[3].toInt(),
                    lastPrimaryCompletionDay = parts[4].toLong()
                )
            }.getOrNull()
        }
        ?.toMap()
        .orEmpty()
}
