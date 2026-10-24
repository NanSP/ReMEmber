package com.example.remember.feature.avatar.data

import com.example.remember.feature.avatar.domain.model.AvatarBodyType
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
            id = "body_slim",
            slot = AvatarSlot.BODY,
            assetPath = "avatar/corpo/03_corpo_magro.svg"
        ),
        AvatarItem(
            id = "body_wide",
            slot = AvatarSlot.BODY,
            assetPath = "avatar/corpo/03_corpo_largo.svg"
        ),
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
        ),
        AvatarItem(
            id = "hair_bangs",
            slot = AvatarSlot.HAIR,
            assetPath = "avatar/cabelos/07_cabelo_franja.svg"
        ),
        AvatarItem(
            id = "hair_long",
            slot = AvatarSlot.HAIR,
            assetPath = "avatar/cabelos/07_cabelo_longo.svg"
        ),
        AvatarItem(
            id = "top_hoodie",
            slot = AvatarSlot.TOP,
            assetPath = "avatar/roupa/13_roupa_moletom.svg"
        ),
        AvatarItem(
            id = "top_jacket",
            slot = AvatarSlot.TOP,
            assetPath = "avatar/roupa/13_roupa_jaqueta.svg"
        ),
        AvatarItem(
            id = "glasses_round",
            slot = AvatarSlot.GLASSES,
            assetPath = "avatar/oculos/09_oculos_redondo.svg"
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

    fun assetPathFor(item: AvatarItem, configuration: AvatarConfiguration): String {
        if (item.slot != AvatarSlot.TOP || item.id !in tailoredTopIds) return item.assetPath
        val bodyId = configuration.equippedItemIds[AvatarSlot.BODY]
        val folder = AvatarBodyType.fromItemId(bodyId).tailoredTopFolder
            ?: return item.assetPath
        val baseName = item.assetPath.substringAfterLast('/').substringBeforeLast(".svg")
        return "avatar/roupa/$folder/${baseName}_alinhada.svg"
    }

    private val tailoredTopIds = setOf(
        "top_tshirt",
        "top_hoodie",
        "top_jacket"
    )
}
