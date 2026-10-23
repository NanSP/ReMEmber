package com.example.remember.feature.settings.domain.model

data class AppSettings(
    val remindersEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val reducedMotion: Boolean = false
)
