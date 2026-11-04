package com.example.remember.feature.mvp.domain

import com.example.remember.feature.goals.domain.model.GoalCategory

data class CategoryHistory(
    val category: GoalCategory,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val lastPrimaryCompletionDay: Long = Long.MIN_VALUE
)

object PrimaryCategoryRules {
    const val CHANGE_LOCK_DAYS = 5

    fun daysUntilChange(lastChangeDay: Long?, currentDay: Long): Int {
        if (lastChangeDay == null) return 0
        val remaining = lastChangeDay + CHANGE_LOCK_DAYS - currentDay
        return remaining.coerceAtLeast(0L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }
}
