package com.example.remember.feature.mvp.domain

import com.example.remember.feature.goals.domain.model.GoalCategory

data class MvpGoal(
    val id: Long,
    val title: String,
    val category: GoalCategory,
    val isCompleted: Boolean = false,
    val recurrence: GoalRecurrence = GoalRecurrence.NONE,
    val lastCompletedEpochDay: Long? = null,
    val reminderMinutesOfDay: Int? = null,
    val reminderEnabled: Boolean = false
) {
    fun isCompletedAt(epochDay: Long): Boolean = when (recurrence) {
        GoalRecurrence.NONE -> isCompleted
        GoalRecurrence.DAILY -> lastCompletedEpochDay == epochDay
        GoalRecurrence.WEEKLY -> lastCompletedEpochDay?.let { epochDay < it + 7 } == true
    }

    fun normalizedAt(epochDay: Long): MvpGoal = copy(isCompleted = isCompletedAt(epochDay))
}

enum class GoalRecurrence { NONE, DAILY, WEEKLY }

data class CompletionHistoryItem(
    val id: Long,
    val goalId: Long,
    val goalTitle: String,
    val category: GoalCategory,
    val completedEpochDay: Long,
    val completedAtMillis: Long,
    val experienceEarned: Int,
    val coinsEarned: Int
)

data class MvpState(
    val profileName: String = "",
    val primaryCategory: GoalCategory = GoalCategory.STUDY,
    val goals: List<MvpGoal> = emptyList(),
    val completionHistory: List<CompletionHistoryItem> = emptyList(),
    val experience: Int = 0,
    val coins: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val categoryHistory: Map<GoalCategory, CategoryHistory> = emptyMap(),
    val primaryCategoryChangeDaysRemaining: Int = 0,
    val bonusMission: DailyBonusMission = DailyBonusMission(
        category = GoalCategory.STUDY,
        completed = false
    ),
    val unlockedAchievements: Set<AchievementId> = emptySet(),
    val streakRecoveriesRemaining: Int = GamificationRules.MONTHLY_RECOVERIES,
    val canRecoverStreak: Boolean = false,
    val selectedBodyId: String = "",
    val selectedFaceId: String = "face_01",
    val selectedEyesId: String = "eyes_01",
    val selectedBeardId: String = "beard_02",
    val selectedHairId: String = "hair_01",
    val selectedGlassesId: String = "glasses_01",
    val selectedShirtId: String = "shirt_01",
    val selectedHatId: String = "hat_01",
    val avatarSkinColor: String = "#87513A",
    val avatarHairColor: String = "#29232A",
    val avatarEyeColor: String = "#382E34",
    val avatarShirtColor: String = "#4FA6D4",
    val avatarBeardColor: String = "#29232A",
    val avatarGlassesColor: String = "#30333E",
    val avatarHatColor: String = "#4B5367",
    val ownedBundleIds: Set<String> = emptySet(),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
) {
    val isOnboardingComplete: Boolean
        get() = profileName.isNotBlank() && selectedBodyId.isNotBlank()

    val level: Int
        get() = experience / EXPERIENCE_PER_LEVEL + 1

    val levelProgress: Float
        get() = (experience % EXPERIENCE_PER_LEVEL) / EXPERIENCE_PER_LEVEL.toFloat()

    val hasSeasonTitle: Boolean
        get() = RewardRules.hasSeasonTitle(longestStreak)

    val completedSeasonRewardCycles: Int
        get() = RewardRules.completedSeasonRewardCycles(longestStreak)

    val canChangePrimaryCategory: Boolean
        get() = primaryCategoryChangeDaysRemaining == 0

    fun historyFor(category: GoalCategory): CategoryHistory =
        categoryHistory[category] ?: CategoryHistory(category)

    companion object {
        const val EXPERIENCE_PER_LEVEL = 100
    }
}
