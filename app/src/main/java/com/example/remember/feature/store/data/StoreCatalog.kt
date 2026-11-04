package com.example.remember.feature.store.data

import androidx.annotation.StringRes
import com.example.remember.R
import com.example.remember.feature.avatar.domain.model.AvatarConfiguration
import com.example.remember.feature.avatar.domain.model.AvatarSlot

data class StoreBundle(
    val id: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    @param:StringRes val contentsRes: Int,
    val price: Int,
    val equippedItemIds: Map<AvatarSlot, String>,
    val hairColor: String,
    val shirtColor: String,
    val glassesColor: String,
    val hatColor: String
) {
    fun applyTo(configuration: AvatarConfiguration): AvatarConfiguration = configuration.copy(
        equippedItemIds = configuration.equippedItemIds + equippedItemIds,
        colors = configuration.colors.copy(
            hair = hairColor,
            shirt = shirtColor,
            glasses = glassesColor,
            hat = hatColor
        )
    )
}

enum class StorePurchaseResult {
    PURCHASED,
    ALREADY_OWNED,
    INSUFFICIENT_COINS,
    UNKNOWN_BUNDLE
}

object StoreCatalog {
    const val PUNK_ID = "punk_set"
    const val WITCH_ID = "witch_set"

    val bundles = listOf(
        StoreBundle(
            id = PUNK_ID,
            titleRes = R.string.store_punk_title,
            descriptionRes = R.string.store_punk_description,
            contentsRes = R.string.store_punk_contents,
            price = 120,
            equippedItemIds = mapOf(
                AvatarSlot.HAIR to "hair_03",
                AvatarSlot.GLASSES to "glasses_04",
                AvatarSlot.SHIRT to "shirt_04",
                AvatarSlot.HAT to "hat_01"
            ),
            hairColor = "#9E3E48",
            shirtColor = "#333D4B",
            glassesColor = "#2C3443",
            hatColor = "#4B5367"
        ),
        StoreBundle(
            id = WITCH_ID,
            titleRes = R.string.store_witch_title,
            descriptionRes = R.string.store_witch_description,
            contentsRes = R.string.store_witch_contents,
            price = 180,
            equippedItemIds = mapOf(
                AvatarSlot.HAIR to "hair_11",
                AvatarSlot.GLASSES to "glasses_01",
                AvatarSlot.SHIRT to "shirt_08",
                AvatarSlot.HAT to "hat_03"
            ),
            hairColor = "#29232A",
            shirtColor = "#7566B2",
            glassesColor = "#2C3443",
            hatColor = "#7566B2"
        )
    )

    private val bundlesById = bundles.associateBy(StoreBundle::id)
    private val premiumItemOwners = mapOf(
        "hair_03" to PUNK_ID,
        "hat_03" to WITCH_ID
    )

    fun find(id: String): StoreBundle? = bundlesById[id]

    fun isItemUnlocked(itemId: String, ownedBundleIds: Set<String>): Boolean =
        premiumItemOwners[itemId]?.let(ownedBundleIds::contains) ?: true
}
