package com.example.remember.feature.avatar.data

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
}
