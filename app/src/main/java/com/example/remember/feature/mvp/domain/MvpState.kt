package com.example.remember.feature.mvp.domain

import com.example.remember.feature.goals.domain.model.GoalCategory

data class MvpGoal(
    val id: Long,
    val title: String,
    val category: GoalCategory,
    val isCompleted: Boolean = false
)

data class MvpState(
    val profileName: String = "",
    val primaryCategory: GoalCategory = GoalCategory.STUDY,
    val goals: List<MvpGoal> = emptyList(),
    val experience: Int = 0,
    val coins: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val selectedBodyId: String = "",
    val selectedHairId: String = "hair_curly",
    val selectedTopId: String = "top_tshirt",
    val roundGlassesEnabled: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
) {
    val isOnboardingComplete: Boolean
        get() = profileName.isNotBlank() && selectedBodyId.isNotBlank()

    val level: Int
        get() = experience / EXPERIENCE_PER_LEVEL + 1

    val levelProgress: Float
        get() = (experience % EXPERIENCE_PER_LEVEL) / EXPERIENCE_PER_LEVEL.toFloat()

    companion object {
        const val EXPERIENCE_PER_LEVEL = 100
    }
}
