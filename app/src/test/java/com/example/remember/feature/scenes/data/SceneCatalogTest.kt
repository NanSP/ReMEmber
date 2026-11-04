package com.example.remember.feature.scenes.data

import com.example.remember.feature.scenes.domain.model.SceneTheme
import com.example.remember.feature.goals.domain.model.GoalCategory
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
        assertEquals((1..5).toList(), progressionFor(SceneTheme.GYM))
        assertEquals((1..4).toList(), progressionFor(SceneTheme.LIBRARY))
        assertEquals((1..5).toList(), progressionFor(SceneTheme.GARDEN))
        assertEquals((1..6).toList(), progressionFor(SceneTheme.PLANTATION))
        assertEquals((1..4).toList(), progressionFor(SceneTheme.BEDROOM))
    }

    @Test
    fun `scene advances every seven streak days and stops at stage eight`() {
        val initial = SceneCatalog.progressFor(GoalCategory.STUDY, longestStreak = 0)
        val beforeFirstStage = SceneCatalog.progressFor(GoalCategory.STUDY, longestStreak = 6)
        val firstStage = SceneCatalog.progressFor(GoalCategory.STUDY, longestStreak = 7)
        val secondStage = SceneCatalog.progressFor(GoalCategory.STUDY, longestStreak = 14)
        val finalStage = SceneCatalog.progressFor(GoalCategory.STUDY, longestStreak = 56)
        val saturated = SceneCatalog.progressFor(GoalCategory.STUDY, longestStreak = 200)

        assertEquals("library_empty", initial.assets.single().id)
        assertEquals(0, initial.currentStep)
        assertEquals(7, initial.daysUntilNextStage)
        assertEquals(0, beforeFirstStage.currentStep)
        assertEquals(1, beforeFirstStage.daysUntilNextStage)
        assertEquals(1, firstStage.currentStep)
        assertEquals("library_few", firstStage.assets.single().id)
        assertEquals(2, secondStage.currentStep)
        assertEquals("library_half", secondStage.assets.single().id)
        assertEquals(8, finalStage.currentStep)
        assertEquals(8, finalStage.totalSteps)
        assertEquals(0, finalStage.daysUntilNextStage)
        assertEquals(8, saturated.currentStep)
        assertEquals(0, saturated.daysUntilNextStage)
    }

    @Test
    fun `all primary categories expose nine scene states from zero to eight`() {
        GoalCategory.entries.forEach { category ->
            val states = (0..8).map { stage ->
                SceneCatalog.progressFor(category, longestStreak = stage * 7)
            }

            assertEquals((0..8).toList(), states.map { it.currentStep })
            assertTrue(states.all { it.assets.isNotEmpty() })
            assertTrue(states.flatMap { it.assets }.all { it in SceneCatalog.assets })
        }
    }

    @Test
    fun `every scene stage has a distinct visual composition`() {
        GoalCategory.entries.forEach { category ->
            val signatures = (0..8).map { stage ->
                SceneCatalog.progressFor(category, longestStreak = stage * 7)
                    .layers
                    .joinToString("|") { layer ->
                        listOf(
                            layer.asset.id,
                            layer.scale,
                            layer.offsetX,
                            layer.offsetY,
                            layer.alpha,
                            layer.zIndex
                        ).joinToString(":")
                    }
            }

            assertEquals("Repeated visual stage for $category", 9, signatures.distinct().size)
        }
    }

    @Test
    fun `scene layers stay inside safe layout limits and never duplicate an asset`() {
        GoalCategory.entries.forEach { category ->
            (0..8).forEach { stage ->
                val layers = SceneCatalog.progressFor(category, longestStreak = stage * 7).layers

                assertEquals(layers.size, layers.map { it.asset.id }.distinct().size)
                assertTrue(layers.all { it.scale in 0.35f..1.05f })
                assertTrue(layers.all { it.offsetX in -0.40f..0.40f })
                assertTrue(layers.all { it.offsetY in -0.10f..0.25f })
                assertTrue(layers.all { it.alpha in 0.40f..0.75f })
                assertEquals(layers.map { it.zIndex }.sorted(), layers.map { it.zIndex })
            }
        }
    }

    @Test
    fun `garden progression uses a single soil base and organized bedroom is not duplicated`() {
        (0..8).forEach { stage ->
            val garden = SceneCatalog.progressFor(
                GoalCategory.FOOD_AND_HYDRATION,
                longestStreak = stage * 7
            )
            assertEquals("Duplicated garden base at stage $stage", 1, garden.layers.size)
        }

        val finalBedroom = SceneCatalog.progressFor(
            GoalCategory.SIMPLE_TASK,
            longestStreak = 56
        )
        assertTrue(finalBedroom.assets.any { it.id == "bedroom_organized" })
        assertTrue(finalBedroom.assets.none { it.id == "bedroom_shelf" })
    }

    private fun progressionFor(theme: SceneTheme): List<Int> =
        SceneCatalog.forTheme(theme)
            .mapNotNull { it.progressionStep }
            .sorted()
}
