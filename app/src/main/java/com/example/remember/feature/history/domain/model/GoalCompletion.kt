package com.example.remember.feature.history.domain.model

import java.time.Instant

data class GoalCompletion(
    val id: Long = 0,
    val goalId: Long,
    val completedAt: Instant,
    val achievedValue: Int,
    val experienceEarned: Int
)
