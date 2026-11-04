package com.example.remember.feature.store.data

import com.example.remember.feature.avatar.data.AvatarCatalog
import com.example.remember.feature.avatar.domain.model.AvatarSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreCatalogTest {
    @Test
    fun `store exposes punk and witch bundles with unique ids`() {
        assertEquals(
            listOf(StoreCatalog.PUNK_ID, StoreCatalog.WITCH_ID),
            StoreCatalog.bundles.map { it.id }
        )
        assertEquals(StoreCatalog.bundles.size, StoreCatalog.bundles.map { it.id }.distinct().size)
        assertTrue(StoreCatalog.bundles.all { it.price > 0 })
    }

    @Test
    fun `every bundle references valid avatar items`() {
        StoreCatalog.bundles.forEach { bundle ->
            bundle.equippedItemIds.forEach { (slot, itemId) ->
                assertTrue("Invalid $slot item: $itemId", AvatarCatalog.contains(slot, itemId))
            }
        }
    }

    @Test
    fun `witch bundle applies its complete visual preset`() {
        val result = StoreCatalog.find(StoreCatalog.WITCH_ID)!!
            .applyTo(AvatarCatalog.defaultConfiguration)

        assertEquals("hair_11", result.equippedItemIds[AvatarSlot.HAIR])
        assertEquals("shirt_08", result.equippedItemIds[AvatarSlot.SHIRT])
        assertEquals("hat_03", result.equippedItemIds[AvatarSlot.HAT])
        assertEquals("#7566B2", result.colors.hat)
    }

    @Test
    fun `premium punk hair and witch hat require their bundles`() {
        assertTrue(!StoreCatalog.isItemUnlocked("hair_03", emptySet()))
        assertTrue(StoreCatalog.isItemUnlocked("hair_03", setOf(StoreCatalog.PUNK_ID)))
        assertTrue(!StoreCatalog.isItemUnlocked("hat_03", emptySet()))
        assertTrue(StoreCatalog.isItemUnlocked("hat_03", setOf(StoreCatalog.WITCH_ID)))
        assertTrue(StoreCatalog.isItemUnlocked("hair_01", emptySet()))
    }
}
