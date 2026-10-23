package com.example.remember.feature.avatar.data

import com.example.remember.feature.avatar.domain.model.AvatarConfiguration
import com.example.remember.feature.avatar.domain.model.AvatarItem
import com.example.remember.feature.avatar.domain.model.AvatarSlot

object AvatarCatalog {
    private val renderOrder = listOf(
        AvatarSlot.BODY,
        AvatarSlot.PANTS,
        AvatarSlot.SHOES,
        AvatarSlot.TOP,
        AvatarSlot.FACE,
        AvatarSlot.EYES,
        AvatarSlot.EXPRESSION,
        AvatarSlot.FACIAL_DETAIL,
        AvatarSlot.HAIR,
        AvatarSlot.EARRINGS,
        AvatarSlot.GLASSES,
        AvatarSlot.SCARF,
        AvatarSlot.WATCH,
        AvatarSlot.HAT,
        AvatarSlot.SPECIAL_ITEM
    )

    val items = listOf(
        AvatarItem(
            id = "body_medium",
            slot = AvatarSlot.BODY,
            assetPath = "avatar/corpo/03_corpo_medio.svg",
            unlockedByDefault = true
        ),
        AvatarItem(
            id = "pants_jeans",
            slot = AvatarSlot.PANTS,
            assetPath = "avatar/calca/14_calca_jeans.svg",
            unlockedByDefault = true
        ),
        AvatarItem(
            id = "shoes_sneakers",
            slot = AvatarSlot.SHOES,
            assetPath = "avatar/calcado/15_calcado_tenis.svg",
            unlockedByDefault = true
        ),
        AvatarItem(
            id = "top_tshirt",
            slot = AvatarSlot.TOP,
            assetPath = "avatar/roupa/13_roupa_camiseta.svg",
            unlockedByDefault = true
        ),
        AvatarItem(
            id = "face_base",
            slot = AvatarSlot.FACE,
            assetPath = "avatar/01_rosto_base.svg",
            unlockedByDefault = true
        ),
        AvatarItem(
            id = "eyes_01",
            slot = AvatarSlot.EYES,
            assetPath = "avatar/olhos/04_olhos_cor_01.svg",
            unlockedByDefault = true
        ),
        AvatarItem(
            id = "expression_happy",
            slot = AvatarSlot.EXPRESSION,
            assetPath = "avatar/expressao/05_expressao_feliz.svg",
            unlockedByDefault = true
        ),
        AvatarItem(
            id = "hair_curly",
            slot = AvatarSlot.HAIR,
            assetPath = "avatar/cabelos/07_cabelo_cacheado.svg",
            unlockedByDefault = true
        )
    )

    val defaultConfiguration = AvatarConfiguration(
        equippedItemIds = items
            .filter(AvatarItem::unlockedByDefault)
            .associate { it.slot to it.id }
    )

    fun resolve(configuration: AvatarConfiguration): List<AvatarItem> {
        val byId = items.associateBy(AvatarItem::id)
        return renderOrder.mapNotNull { slot ->
            configuration.equippedItemIds[slot]?.let(byId::get)
        }
    }
}
