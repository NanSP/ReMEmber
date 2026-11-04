package com.example.remember.feature.avatar.domain.model

import androidx.annotation.StringRes

enum class AvatarSlot {
    BODY, FACE, EYES, BEARD, HAIR, GLASSES, SHIRT, HAT
}

data class AvatarItem(
    val id: String,
    val slot: AvatarSlot,
    val assetPath: String,
    @param:StringRes val labelRes: Int,
    val unlockedByDefault: Boolean = false
)

data class AvatarColors(
    val skin: String = "#87513A",
    val hair: String = "#29232A",
    val eyes: String = "#382E34",
    val shirt: String = "#4FA6D4",
    val beard: String = "#29232A",
    val glasses: String = "#30333E",
    val hat: String = "#4B5367"
) {
    fun replacementFor(marker: String): String? = when (marker) {
        "SKIN" -> skin
        "HAIR" -> hair
        "EYE" -> eyes
        "SHIRT" -> shirt
        "BEARD" -> beard
        "GLASSES" -> glasses
        "HAT" -> hat
        else -> null
    }
}

data class AvatarConfiguration(
    val equippedItemIds: Map<AvatarSlot, String>,
    val colors: AvatarColors = AvatarColors()
)
