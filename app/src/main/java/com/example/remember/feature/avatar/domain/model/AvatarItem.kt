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

enum class AvatarBodyType(
    val itemId: String,
    val tailoredTopFolder: String?
) {
    SLIM("body_slim", "corpo_magro"),
    MEDIUM("body_medium", "corpo_medio"),
    WIDE("body_wide", "corpo_largo");

    companion object {
        fun fromItemId(itemId: String?): AvatarBodyType =
            entries.firstOrNull { it.itemId == itemId } ?: MEDIUM
    }
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
