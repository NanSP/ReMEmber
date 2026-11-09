package com.example.remember.feature.mvp.domain

import com.example.remember.feature.goals.domain.model.GoalCategory

data class DailyBonusMission(
    val category: GoalCategory,
    val completed: Boolean,
    val startedAtMillis: Long? = null
) {
    val requiredDurationMillis: Long
        get() = GamificationRules.bonusDurationMillis(category)

    val requiresTimer: Boolean
        get() = requiredDurationMillis > 0L

    val isStarted: Boolean
        get() = startedAtMillis != null

    fun remainingMillis(nowMillis: Long): Long = when {
        completed || !requiresTimer -> 0L
        startedAtMillis == null -> requiredDurationMillis
        else -> (requiredDurationMillis - (nowMillis - startedAtMillis)).coerceAtLeast(0L)
    }

    fun canComplete(nowMillis: Long): Boolean =
        !completed && (!requiresTimer || isStarted && remainingMillis(nowMillis) == 0L)
}

enum class BonusMissionCompletionResult {
    COMPLETED,
    ALREADY_COMPLETED,
    NOT_STARTED,
    TIMER_RUNNING
}

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
    const val STUDY_BONUS_DURATION_MILLIS = 15 * 60 * 1_000L
    const val EXERCISE_BONUS_DURATION_MILLIS = 10 * 60 * 1_000L

    fun bonusDurationMillis(category: GoalCategory): Long = when (category) {
        GoalCategory.STUDY -> STUDY_BONUS_DURATION_MILLIS
        GoalCategory.EXERCISE -> EXERCISE_BONUS_DURATION_MILLIS
        GoalCategory.FOOD_AND_HYDRATION,
        GoalCategory.SIMPLE_TASK -> 0L
    }

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
