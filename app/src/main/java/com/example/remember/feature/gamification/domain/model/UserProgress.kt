package com.example.remember.feature.gamification.domain.model

data class UserProgress(
    val level: Int = 1,
    val experience: Int = 0,
    val coins: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val monthlyRecoveries: Int = 2
)
