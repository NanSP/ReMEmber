package com.example.remember.feature.mvp.domain

import com.example.remember.feature.goals.domain.model.GoalCategory

data class CompletionReward(
    val experience: Int,
    val coins: Int
)

object RewardRules {
    private const val BASE_EXPERIENCE = 25
    private const val BASE_COINS = 10
    private const val PRIMARY_EXPERIENCE_BONUS = 15
    private const val PRIMARY_COINS_BONUS = 5

    fun completionReward(
        category: GoalCategory,
        primaryCategory: GoalCategory
    ): CompletionReward {
        val isPrimary = category == primaryCategory
        return CompletionReward(
            experience = BASE_EXPERIENCE + if (isPrimary) PRIMARY_EXPERIENCE_BONUS else 0,
            coins = BASE_COINS + if (isPrimary) PRIMARY_COINS_BONUS else 0
        )
    }

    fun nextStreak(current: Int, lastCompletionDay: Long, currentDay: Long): Int = when {
        lastCompletionDay == currentDay -> current
        lastCompletionDay == currentDay - 1 -> current + 1
        else -> 1
    }
}
