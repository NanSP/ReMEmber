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
        assertTrue(resolvedItems.all { it.assetPath.startsWith("avatar_studio/") })
        assertTrue(resolvedItems.all { it.assetPath.endsWith(".svg") })
    }

    @Test
    fun `default avatar layers follow render order`() {
        val slots = AvatarCatalog.resolve(AvatarCatalog.defaultConfiguration).map { it.slot }

        assertEquals(
            listOf(
                AvatarSlot.BODY,
                AvatarSlot.FACE,
                AvatarSlot.EYES,
                AvatarSlot.BEARD,
                AvatarSlot.HAIR,
                AvatarSlot.GLASSES,
                AvatarSlot.SHIRT,
                AvatarSlot.HAT
            ),
            slots
        )
    }

    @Test
    fun `configuration resolves only one selected item per slot`() {
        val configuration = AvatarConfiguration(
            AvatarCatalog.defaultConfiguration.equippedItemIds +
                (AvatarSlot.HAIR to "hair_03") +
                (AvatarSlot.GLASSES to "glasses_02")
        )

        val resolved = AvatarCatalog.resolve(configuration)
        assertEquals(listOf("hair_03"), resolved.filter { it.slot == AvatarSlot.HAIR }.map { it.id })
        assertEquals(listOf("glasses_02"), resolved.filter { it.slot == AvatarSlot.GLASSES }.map { it.id })
    }

    @Test
    fun `every catalog item belongs to its queried slot`() {
        AvatarSlot.entries.forEach { slot ->
            assertTrue(AvatarCatalog.itemsFor(slot).all { it.slot == slot })
        }
    }

    @Test
    fun `body customization exposes only three supported formats`() {
        assertEquals(
            listOf("body_01", "body_02", "body_03"),
            AvatarCatalog.itemsFor(AvatarSlot.BODY).map { it.id }
        )
    }

    @Test
    fun `expanded catalog exposes new hair and clothing options`() {
        assertEquals(15, AvatarCatalog.itemsFor(AvatarSlot.HAIR).size)
        assertEquals(8, AvatarCatalog.itemsFor(AvatarSlot.SHIRT).size)
        assertTrue(AvatarCatalog.contains(AvatarSlot.HAIR, "hair_15"))
        assertTrue(AvatarCatalog.contains(AvatarSlot.SHIRT, "shirt_08"))
    }
}
