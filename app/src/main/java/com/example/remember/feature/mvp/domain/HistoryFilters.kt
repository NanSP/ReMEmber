package com.example.remember.feature.mvp.domain

import com.example.remember.feature.goals.domain.model.GoalCategory

enum class HistoryPeriod(val days: Int?) {
    LAST_7_DAYS(7),
    LAST_30_DAYS(30),
    ALL(null)
}

fun filterCompletionHistory(
    items: List<CompletionHistoryItem>,
    period: HistoryPeriod,
    category: GoalCategory?,
    goalId: Long?,
    currentEpochDay: Long
): List<CompletionHistoryItem> {
    val firstDay = period.days?.let { currentEpochDay - it + 1 }
    return items.filter { item ->
        (firstDay == null || item.completedEpochDay in firstDay..currentEpochDay) &&
            (category == null || item.category == category) &&
            (goalId == null || item.goalId == goalId)
    }
}
