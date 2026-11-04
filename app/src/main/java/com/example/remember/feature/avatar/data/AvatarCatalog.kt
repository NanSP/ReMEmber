package com.example.remember.feature.avatar.data

import com.example.remember.R
import com.example.remember.feature.avatar.domain.model.AvatarConfiguration
import com.example.remember.feature.avatar.domain.model.AvatarItem
import com.example.remember.feature.avatar.domain.model.AvatarSlot

object AvatarCatalog {
    private const val ROOT = "avatar_studio"

    private val renderOrder = listOf(
        AvatarSlot.BODY,
        AvatarSlot.FACE,
        AvatarSlot.EYES,
        AvatarSlot.BEARD,
        AvatarSlot.HAIR,
        AvatarSlot.GLASSES,
        AvatarSlot.SHIRT,
        AvatarSlot.HAT
    )

    val items: List<AvatarItem> = buildList {
        addNumbered(AvatarSlot.BODY, "body", bodyLabels(), defaultIndex = 3)
        addNumbered(AvatarSlot.FACE, "face", faceLabels(), defaultIndex = 1)
        addNumbered(AvatarSlot.EYES, "eyes", eyesLabels(), defaultIndex = 1)
        addNumbered(AvatarSlot.BEARD, "beard", beardLabels(), defaultIndex = 2)
        addNumbered(AvatarSlot.HAIR, "hair", hairLabels(), defaultIndex = 1)
        addNumbered(AvatarSlot.GLASSES, "glasses", glassesLabels(), defaultIndex = 1)
        addNumbered(AvatarSlot.SHIRT, "shirt", shirtLabels(), defaultIndex = 1)
        addNumbered(AvatarSlot.HAT, "hat", hatLabels(), defaultIndex = 1)
    }

    val defaultConfiguration = AvatarConfiguration(
        equippedItemIds = items
            .filter(AvatarItem::unlockedByDefault)
            .associate { it.slot to it.id }
    )

    private val itemsById = items.associateBy(AvatarItem::id)

    fun resolve(configuration: AvatarConfiguration): List<AvatarItem> = renderOrder.mapNotNull { slot ->
        configuration.equippedItemIds[slot]?.let(itemsById::get)
    }

    fun itemsFor(slot: AvatarSlot): List<AvatarItem> = items.filter { it.slot == slot }

    fun contains(slot: AvatarSlot, itemId: String): Boolean = itemsById[itemId]?.slot == slot

    private fun MutableList<AvatarItem>.addNumbered(
        slot: AvatarSlot,
        folder: String,
        labels: List<Int>,
        defaultIndex: Int
    ) {
        labels.forEachIndexed { zeroBasedIndex, labelRes ->
            val index = zeroBasedIndex + 1
            val number = index.toString().padStart(2, '0')
            add(
                AvatarItem(
                    id = "${folder}_$number",
                    slot = slot,
                    assetPath = "$ROOT/$folder/$number.svg",
                    labelRes = labelRes,
                    unlockedByDefault = index == defaultIndex
                )
            )
        }
    }

    private fun bodyLabels() = listOf(
        R.string.avatar_body_classic,
        R.string.avatar_body_athletic,
        R.string.avatar_body_wide
    )
    private fun faceLabels() = listOf(
        R.string.avatar_face_round,
        R.string.avatar_face_oval,
        R.string.avatar_face_square
    )
    private fun eyesLabels() = listOf(
        R.string.avatar_eyes_curious,
        R.string.avatar_eyes_happy,
        R.string.avatar_eyes_surprised,
        R.string.avatar_eyes_relaxed,
        R.string.avatar_eyes_smiling,
        R.string.avatar_eyes_calm
    )
    private fun hairLabels() = listOf(
        R.string.avatar_hair_curls,
        R.string.avatar_hair_wavy,
        R.string.avatar_hair_punk,
        R.string.avatar_hair_voluminous,
        R.string.avatar_hair_short,
        R.string.avatar_hair_coily,
        R.string.avatar_hair_side,
        R.string.avatar_hair_shaggy,
        R.string.avatar_hair_stylized,
        R.string.avatar_hair_social,
        R.string.avatar_hair_long_layered,
        R.string.avatar_hair_bob_cut,
        R.string.avatar_hair_ponytail,
        R.string.avatar_hair_high_bun,
        R.string.avatar_hair_braids
    )
    private fun beardLabels() = listOf(
        R.string.avatar_none,
        R.string.avatar_beard_goatee,
        R.string.avatar_beard_full,
        R.string.avatar_beard_mustache,
        R.string.avatar_beard_outline,
        R.string.avatar_beard_chin
    )
    private fun glassesLabels() = listOf(
        R.string.avatar_glasses_none,
        R.string.avatar_glasses_round,
        R.string.avatar_glasses_square,
        R.string.avatar_glasses_sun
    )
    private fun shirtLabels() = listOf(
        R.string.avatar_shirt_basic,
        R.string.avatar_shirt_button,
        R.string.avatar_shirt_striped,
        R.string.avatar_shirt_print,
        R.string.avatar_shirt_suit,
        R.string.avatar_shirt_tank,
        R.string.avatar_shirt_hoodie,
        R.string.avatar_shirt_turtleneck
    )
    private fun hatLabels() = listOf(
        R.string.avatar_hat_none,
        R.string.avatar_hat_cap,
        R.string.avatar_hat_witch,
        R.string.avatar_hat_fedora
    )
}
