package com.example.remember.feature.scenes.data

import com.example.remember.feature.scenes.domain.model.SceneTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SceneCatalogTest {
    @Test
    fun `catalog has unique ids and asset paths`() {
        val assets = SceneCatalog.assets

        assertEquals(31, assets.size)
        assertEquals(assets.size, assets.map { it.id }.distinct().size)
        assertEquals(assets.size, assets.map { it.assetPath }.distinct().size)
        assertTrue(assets.all { it.assetPath.startsWith("cenarios/") })
        assertTrue(assets.all { it.assetPath.endsWith(".svg") })
    }

    @Test
    fun `progressive scenes have consecutive steps`() {
        assertEquals((1..4).toList(), progressionFor(SceneTheme.LIBRARY))
        assertEquals((1..6).toList(), progressionFor(SceneTheme.PLANTATION))
    }

    private fun progressionFor(theme: SceneTheme): List<Int> =
        SceneCatalog.forTheme(theme)
            .mapNotNull { it.progressionStep }
            .sorted()
}
