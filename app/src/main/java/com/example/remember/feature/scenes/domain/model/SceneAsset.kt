package com.example.remember.feature.scenes.domain.model

enum class SceneTheme {
    GYM,
    LIBRARY,
    GARDEN,
    PLANTATION,
    BEDROOM
}

data class SceneAsset(
    val id: String,
    val theme: SceneTheme,
    val assetPath: String,
    val progressionStep: Int? = null
)
