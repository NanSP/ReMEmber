package com.example.remember.feature.mvp.domain

import com.example.remember.feature.goals.domain.model.GoalCategory

data class DailyBonusMission(
    val category: GoalCategory,
    val completed: Boolean
)

enum class AchievementId {
    FIRST_COMPLETION,
    CATEGORY_SPECIALIST,
    PRODUCTIVE_DAY,
    NIGHT_OWL,
    BONUS_HUNTER,
    SEVEN_DAY_STREAK,
    CATEGORY_EXPLORER,
    COIN_COLLECTOR,
    XP_VETERAN,
    STYLE_COLLECTOR
}

data class AchievementStats(
    val totalCompletions: Int,
    val highestCategoryCompletions: Int,
    val highestDailyCompletions: Int,
    val nightCompletions: Int,
    val bonusMissionsCompleted: Int,
    val longestStreak: Int,
    val activeCategories: Int,
    val totalCoinsEarned: Int,
    val totalExperience: Int,
    val accessoriesOwned: Int
)

object GamificationRules {
    const val BONUS_EXPERIENCE = 20
    const val BONUS_COINS = 10
    const val ACHIEVEMENT_EXPERIENCE = 50
    const val ACHIEVEMENT_COINS = 20
    const val MONTHLY_RECOVERIES = 2

    fun unlockedAchievements(stats: AchievementStats): Set<AchievementId> = buildSet {
        if (stats.totalCompletions >= 1) add(AchievementId.FIRST_COMPLETION)
        if (stats.highestCategoryCompletions >= 10) add(AchievementId.CATEGORY_SPECIALIST)
        if (stats.highestDailyCompletions >= 3) add(AchievementId.PRODUCTIVE_DAY)
        if (stats.nightCompletions >= 5) add(AchievementId.NIGHT_OWL)
        if (stats.bonusMissionsCompleted >= 5) add(AchievementId.BONUS_HUNTER)
        if (stats.longestStreak >= 7) add(AchievementId.SEVEN_DAY_STREAK)
        if (stats.activeCategories >= 2) add(AchievementId.CATEGORY_EXPLORER)
        if (stats.totalCoinsEarned >= 500) add(AchievementId.COIN_COLLECTOR)
        if (stats.totalExperience >= 1_000) add(AchievementId.XP_VETERAN)
        if (stats.accessoriesOwned >= 2) add(AchievementId.STYLE_COLLECTOR)
    }

    fun canRecoverStreak(
        currentStreak: Int,
        lastPrimaryCompletionDay: Long,
        currentDay: Long,
        recoveriesUsedThisMonth: Int
    ): Boolean = currentStreak > 0 &&
        lastPrimaryCompletionDay == currentDay - 2 &&
        recoveriesUsedThisMonth < MONTHLY_RECOVERIES
}
