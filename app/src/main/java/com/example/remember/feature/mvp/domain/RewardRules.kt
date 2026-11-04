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
    const val SEASON_TITLE_STREAK = 56
    const val SEASON_REWARD_INTERVAL_DAYS = 30
    const val SEASON_REWARD_EXPERIENCE = 100
    const val SEASON_REWARD_COINS = 50

    fun completionReward(
        category: GoalCategory,
        primaryCategory: GoalCategory,
        isFirstPrimaryCompletionToday: Boolean
    ): CompletionReward {
        val receivesDailyPrimaryBonus =
            category == primaryCategory && isFirstPrimaryCompletionToday
        return CompletionReward(
            experience = BASE_EXPERIENCE + if (receivesDailyPrimaryBonus) {
                PRIMARY_EXPERIENCE_BONUS
            } else {
                0
            },
            coins = BASE_COINS + if (receivesDailyPrimaryBonus) PRIMARY_COINS_BONUS else 0
        )
    }

    fun isFirstPrimaryCompletionToday(
        category: GoalCategory,
        primaryCategory: GoalCategory,
        lastPrimaryCompletionDay: Long,
        currentDay: Long
    ): Boolean = category == primaryCategory && currentDay > lastPrimaryCompletionDay

    fun nextStreak(current: Int, lastCompletionDay: Long, currentDay: Long): Int = when {
        lastCompletionDay > currentDay -> current.coerceAtLeast(1)
        lastCompletionDay == currentDay -> current.coerceAtLeast(1)
        lastCompletionDay == currentDay - 1 -> current + 1
        else -> 1
    }

    fun activeStreak(current: Int, lastCompletionDay: Long, currentDay: Long): Int = when {
        current <= 0 -> 0
        lastCompletionDay >= currentDay - 1 -> current
        else -> 0
    }

    fun hasSeasonTitle(longestStreak: Int): Boolean = longestStreak >= SEASON_TITLE_STREAK

    fun completedSeasonRewardCycles(longestStreak: Int): Int =
        ((longestStreak - SEASON_TITLE_STREAK).coerceAtLeast(0)) / SEASON_REWARD_INTERVAL_DAYS

    fun daysUntilNextSeasonReward(longestStreak: Int): Int? {
        if (!hasSeasonTitle(longestStreak)) return null
        val daysAfterTitle = longestStreak - SEASON_TITLE_STREAK
        val progressInCycle = daysAfterTitle % SEASON_REWARD_INTERVAL_DAYS
        return if (progressInCycle == 0 && daysAfterTitle > 0) {
            SEASON_REWARD_INTERVAL_DAYS
        } else {
            SEASON_REWARD_INTERVAL_DAYS - progressInCycle
        }
    }

    fun seasonReward(
        previousLongestStreak: Int,
        newLongestStreak: Int
    ): CompletionReward {
        val crossedCycles = (
            completedSeasonRewardCycles(newLongestStreak) -
                completedSeasonRewardCycles(previousLongestStreak)
            ).coerceAtLeast(0)
        return CompletionReward(
            experience = crossedCycles * SEASON_REWARD_EXPERIENCE,
            coins = crossedCycles * SEASON_REWARD_COINS
        )
    }
}
