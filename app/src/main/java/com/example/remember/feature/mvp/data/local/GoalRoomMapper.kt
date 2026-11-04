package com.example.remember.feature.mvp.data.local

import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.domain.GoalRecurrence
import com.example.remember.feature.mvp.domain.MvpGoal

internal fun MvpGoal.toEntity(createdAtMillis: Long = id): GoalEntity = GoalEntity(
    id = id,
    title = title,
    category = category.name,
    isCompleted = isCompleted,
    recurrence = recurrence.name,
    lastCompletedEpochDay = lastCompletedEpochDay,
    reminderMinutesOfDay = reminderMinutesOfDay,
    reminderEnabled = reminderEnabled,
    createdAtMillis = createdAtMillis
)

internal fun GoalEntity.toDomain(): MvpGoal? = runCatching {
    MvpGoal(
        id = id,
        title = title,
        category = GoalCategory.valueOf(category),
        isCompleted = isCompleted,
        recurrence = GoalRecurrence.valueOf(recurrence),
        lastCompletedEpochDay = lastCompletedEpochDay,
        reminderMinutesOfDay = reminderMinutesOfDay,
        reminderEnabled = reminderEnabled
    )
}.getOrNull()
