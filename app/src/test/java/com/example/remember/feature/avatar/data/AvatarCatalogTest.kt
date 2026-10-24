package com.example.remember.feature.avatar.data

import com.example.remember.feature.avatar.domain.model.AvatarConfiguration
import com.example.remember.feature.avatar.domain.model.AvatarSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarCatalogTest {
    @Test
    fun `default configuration resolves every equipped item`() {
        val configuration = AvatarCatalog.defaultConfiguration
        val resolvedItems = AvatarCatalog.resolve(configuration)

        assertEquals(configuration.equippedItemIds.size, resolvedItems.size)
        assertEquals(resolvedItems.size, resolvedItems.map { it.slot }.distinct().size)
        assertTrue(resolvedItems.all { it.assetPath.startsWith("avatar/") })
        assertTrue(resolvedItems.all { it.assetPath.endsWith(".svg") })
    }

    @Test
    fun `default avatar layers follow render order`() {
        val slots = AvatarCatalog.resolve(AvatarCatalog.defaultConfiguration).map { it.slot }

        assertEquals(
            listOf(
                AvatarSlot.BODY,
                AvatarSlot.PANTS,
                AvatarSlot.SHOES,
                AvatarSlot.TOP,
                AvatarSlot.FACE,
                AvatarSlot.EYES,
                AvatarSlot.EXPRESSION,
                AvatarSlot.HAIR
            ),
            slots
        )
    }

    @Test
    fun `configuration resolves only one selected body`() {
        val configuration = AvatarConfiguration(
            AvatarCatalog.defaultConfiguration.equippedItemIds +
                (AvatarSlot.BODY to "body_wide")
        )

        val bodyItems = AvatarCatalog.resolve(configuration).filter { it.slot == AvatarSlot.BODY }
        assertEquals(listOf("body_wide"), bodyItems.map { it.id })
    }

    @Test
    fun `tops resolve a tailored asset for each body`() {
        val base = AvatarCatalog.defaultConfiguration.equippedItemIds
        val slim = AvatarConfiguration(base + (AvatarSlot.BODY to "body_slim"))
        val medium = AvatarConfiguration(base + (AvatarSlot.BODY to "body_medium"))
        val wide = AvatarConfiguration(base + (AvatarSlot.BODY to "body_wide"))
        val top = AvatarCatalog.resolve(slim).first { it.slot == AvatarSlot.TOP }
        val pants = AvatarCatalog.resolve(slim).first { it.slot == AvatarSlot.PANTS }

        assertEquals(
            "avatar/roupa/corpo_magro/13_roupa_camiseta_alinhada.svg",
            AvatarCatalog.assetPathFor(top, slim)
        )
        assertEquals(
            "avatar/roupa/corpo_medio/13_roupa_camiseta_alinhada.svg",
            AvatarCatalog.assetPathFor(top, medium)
        )
        assertEquals(
            "avatar/roupa/corpo_largo/13_roupa_camiseta_alinhada.svg",
            AvatarCatalog.assetPathFor(top, wide)
        )
        assertEquals(pants.assetPath, AvatarCatalog.assetPathFor(pants, wide))
    }
}
