package com.example.remember.feature.goals.domain.model

import java.time.DayOfWeek

enum class GoalCategory {
    EXERCISE,
    FOOD_AND_HYDRATION,
    SIMPLE_TASK,
    STUDY
}

enum class GoalMeasurement {
    CHECK,
    QUANTITY,
    DURATION
}

data class Goal(
    val id: Long = 0,
    val title: String,
    val category: GoalCategory,
    val measurement: GoalMeasurement,
    val targetValue: Int = 1,
    val unit: String? = null,
    val scheduledDays: Set<DayOfWeek> = emptySet(),
    val reminderTimeMinutes: Int? = null,
    val isPrimary: Boolean = false,
    val isActive: Boolean = true
)
