package com.example.remember.feature.onboarding.domain.model

data class OnboardingState(
    val displayName: String = "",
    val selectedPrimaryGoalId: Long? = null,
    val notificationsEnabled: Boolean = false,
    val isComplete: Boolean = false
)
