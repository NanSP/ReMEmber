package com.example.remember.feature.avatar.domain.model

enum class AvatarSlot {
    BODY,
    PANTS,
    SHOES,
    TOP,
    FACE,
    EYES,
    EXPRESSION,
    FACIAL_DETAIL,
    HAIR,
    EARRINGS,
    GLASSES,
    SCARF,
    WATCH,
    HAT,
    SPECIAL_ITEM
}

data class AvatarItem(
    val id: String,
    val slot: AvatarSlot,
    val assetPath: String,
    val unlockedByDefault: Boolean = false
)

data class AvatarConfiguration(
    val equippedItemIds: Map<AvatarSlot, String>
)
