package com.example.remember.feature.scenes.data

import com.example.remember.feature.scenes.domain.model.SceneAsset
import com.example.remember.feature.scenes.domain.model.SceneTheme
import com.example.remember.feature.goals.domain.model.GoalCategory

object SceneCatalog {
    const val DAYS_PER_STAGE = 7
    const val MAX_STAGE = 8

    val assets = listOf(
        scene("gym_bench", SceneTheme.GYM, "academia/academia_banco.svg", 3),
        scene("gym_rope", SceneTheme.GYM, "academia/academia_corda.svg"),
        scene("gym_treadmill", SceneTheme.GYM, "academia/academia_esteira.svg", 4),
        scene("gym_dumbbell", SceneTheme.GYM, "academia/academia_halter.svg", 2),
        scene("gym_machine", SceneTheme.GYM, "academia/academia_maquina.svg", 5),
        scene("gym_blue_mat", SceneTheme.GYM, "academia/academia_tapete_azul.svg", 1),
        scene("gym_purple_mat", SceneTheme.GYM, "academia/academia_tapete_roxo.svg"),
        scene("library_lamp", SceneTheme.LIBRARY, "biblioteca/biblioteca_abajur.svg"),
        scene("library_full", SceneTheme.LIBRARY, "biblioteca/biblioteca_estante_cheia.svg", 4),
        scene("library_half", SceneTheme.LIBRARY, "biblioteca/biblioteca_estante_meio_cheia.svg", 3),
        scene("library_few", SceneTheme.LIBRARY, "biblioteca/biblioteca_estante_poucos_livros.svg", 2),
        scene("library_empty", SceneTheme.LIBRARY, "biblioteca/biblioteca_estante_vazia.svg", 1),
        scene("library_armchair", SceneTheme.LIBRARY, "biblioteca/biblioteca_poltrona.svg"),
        scene("garden_pumpkin", SceneTheme.GARDEN, "horta/horta_abobora.svg"),
        scene("garden_sprouts", SceneTheme.GARDEN, "horta/horta_brotos.svg", 2),
        scene("garden_corn", SceneTheme.GARDEN, "horta/horta_milho.svg"),
        scene("garden_mixed", SceneTheme.GARDEN, "horta/horta_misto.svg", 5),
        scene("garden_cabbage", SceneTheme.GARDEN, "horta/horta_repolho.svg", 3),
        scene("garden_soil", SceneTheme.GARDEN, "horta/horta_terra.svg", 1),
        scene("garden_tomato", SceneTheme.GARDEN, "horta/horta_tomate.svg", 4),
        scene("plantation_flowers", SceneTheme.PLANTATION, "plantacao/plantacao_arvore_florida.svg", 5),
        scene("plantation_fruits", SceneTheme.PLANTATION, "plantacao/plantacao_arvore_frutos.svg", 6),
        scene("plantation_small_tree", SceneTheme.PLANTATION, "plantacao/plantacao_arvore_pequena.svg", 4),
        scene("plantation_sprout", SceneTheme.PLANTATION, "plantacao/plantacao_broto.svg", 2),
        scene("plantation_seedling", SceneTheme.PLANTATION, "plantacao/plantacao_muda.svg", 3),
        scene("plantation_seed", SceneTheme.PLANTATION, "plantacao/plantacao_semente.svg", 1),
        scene("bedroom_boxes", SceneTheme.BEDROOM, "quarto/quarto_caixas_bagunca.svg", 1),
        scene("bedroom_shelf", SceneTheme.BEDROOM, "quarto/quarto_estante.svg"),
        scene("bedroom_scattered_books", SceneTheme.BEDROOM, "quarto/quarto_livros_espalhados.svg", 2),
        scene("bedroom_desk", SceneTheme.BEDROOM, "quarto/quarto_mesa.svg", 3),
        scene("bedroom_organized", SceneTheme.BEDROOM, "quarto/quarto_quarto_organizado.svg", 4)
    )

    fun forTheme(theme: SceneTheme): List<SceneAsset> = assets.filter { it.theme == theme }

    fun progressionFor(theme: SceneTheme): List<SceneAsset> = forTheme(theme)
        .filter { it.progressionStep != null }
        .sortedBy { it.progressionStep }

    fun progressFor(category: GoalCategory, longestStreak: Int): SceneProgress {
        val safeStreak = longestStreak.coerceAtLeast(0)
        val stage = (safeStreak / DAYS_PER_STAGE).coerceAtMost(MAX_STAGE)
        val theme = category.sceneTheme()
        val recipe = stageRecipes.getValue(theme)[stage]
        return SceneProgress(
            layers = recipe.map { definition ->
                SceneLayer(
                    asset = assetById(definition.assetId),
                    scale = definition.scale,
                    offsetX = definition.offsetX,
                    offsetY = definition.offsetY,
                    alpha = definition.alpha,
                    zIndex = definition.zIndex
                )
            },
            currentStep = stage,
            totalSteps = MAX_STAGE,
            longestStreak = safeStreak,
            daysUntilNextStage = if (stage == MAX_STAGE) {
                0
            } else {
                (stage + 1) * DAYS_PER_STAGE - safeStreak
            }
        )
    }

    private val stageRecipes: Map<SceneTheme, List<List<LayerDefinition>>> = mapOf(
        SceneTheme.LIBRARY to listOf(
            listOf(layer("library_empty", scale = 0.82f, x = 0.31f)),
            listOf(layer("library_few", scale = 0.83f, x = 0.31f)),
            listOf(layer("library_half", scale = 0.84f, x = 0.31f)),
            listOf(layer("library_full", scale = 0.85f, x = 0.31f)),
            listOf(
                layer("library_full", scale = 0.85f, x = 0.31f),
                layer("library_lamp", scale = 0.54f, x = -0.34f, y = 0.06f, alpha = 0.58f, z = 1f)
            ),
            listOf(
                layer("library_full", scale = 0.85f, x = 0.31f),
                layer("library_armchair", scale = 0.56f, x = -0.32f, y = 0.17f, alpha = 0.55f, z = 1f)
            ),
            listOf(
                layer("library_full", scale = 0.86f, x = 0.31f),
                layer("library_lamp", scale = 0.54f, x = -0.35f, y = 0.04f, alpha = 0.60f, z = 1f),
                layer("library_armchair", scale = 0.55f, x = -0.24f, y = 0.20f, alpha = 0.52f, z = 2f)
            ),
            listOf(
                layer("library_full", scale = 0.88f, x = 0.30f, alpha = 0.60f),
                layer("library_lamp", scale = 0.57f, x = -0.36f, y = 0.02f, alpha = 0.64f, z = 1f),
                layer("library_armchair", scale = 0.57f, x = -0.23f, y = 0.20f, alpha = 0.56f, z = 2f)
            ),
            listOf(
                layer("library_full", scale = 0.90f, x = 0.29f, alpha = 0.64f),
                layer("library_lamp", scale = 0.60f, x = -0.37f, y = 0.00f, alpha = 0.68f, z = 1f),
                layer("library_armchair", scale = 0.60f, x = -0.22f, y = 0.19f, alpha = 0.60f, z = 2f)
            )
        ),
        SceneTheme.GARDEN to listOf(
            listOf(layer("garden_soil", scale = 0.82f, x = 0.28f, y = 0.16f)),
            listOf(layer("garden_sprouts", scale = 0.84f, x = 0.28f, y = 0.15f)),
            listOf(layer("garden_cabbage", scale = 0.86f, x = 0.28f, y = 0.14f)),
            listOf(layer("garden_tomato", scale = 0.88f, x = 0.28f, y = 0.13f)),
            listOf(layer("garden_corn", scale = 0.90f, x = 0.28f, y = 0.12f)),
            listOf(layer("garden_pumpkin", scale = 0.92f, x = 0.28f, y = 0.11f)),
            listOf(layer("garden_mixed", scale = 0.94f, x = 0.28f, y = 0.10f, alpha = 0.58f)),
            listOf(layer("garden_mixed", scale = 0.98f, x = 0.27f, y = 0.09f, alpha = 0.64f)),
            listOf(layer("garden_mixed", scale = 1.02f, x = 0.26f, y = 0.08f, alpha = 0.70f))
        ),
        SceneTheme.GYM to listOf(
            listOf(layer("gym_blue_mat", scale = 0.82f, x = 0.27f, y = 0.17f)),
            listOf(layer("gym_purple_mat", scale = 0.84f, x = 0.27f, y = 0.16f)),
            listOf(
                layer("gym_purple_mat", scale = 0.86f, x = 0.27f, y = 0.16f),
                layer("gym_dumbbell", scale = 0.43f, x = 0.35f, y = 0.20f, alpha = 0.66f, z = 1f)
            ),
            listOf(
                layer("gym_blue_mat", scale = 0.88f, x = 0.27f, y = 0.16f),
                layer("gym_rope", scale = 0.55f, x = 0.31f, y = 0.18f, alpha = 0.62f, z = 1f)
            ),
            listOf(layer("gym_bench", scale = 0.78f, x = 0.29f, y = 0.14f)),
            listOf(
                layer("gym_bench", scale = 0.78f, x = 0.29f, y = 0.14f),
                layer("gym_dumbbell", scale = 0.40f, x = 0.38f, y = 0.20f, alpha = 0.68f, z = 1f)
            ),
            listOf(layer("gym_treadmill", scale = 0.82f, x = 0.31f, y = 0.10f, alpha = 0.60f)),
            listOf(layer("gym_machine", scale = 0.84f, x = 0.31f, y = 0.04f, alpha = 0.62f)),
            listOf(
                layer("gym_machine", scale = 0.86f, x = 0.31f, y = 0.03f, alpha = 0.64f),
                layer("gym_rope", scale = 0.47f, x = -0.34f, y = 0.19f, alpha = 0.62f, z = 1f),
                layer("gym_dumbbell", scale = 0.38f, x = 0.39f, y = 0.21f, alpha = 0.70f, z = 2f)
            )
        ),
        SceneTheme.BEDROOM to listOf(
            listOf(layer("bedroom_boxes", scale = 0.64f, x = 0.33f, y = 0.18f)),
            listOf(layer("bedroom_scattered_books", scale = 0.72f, x = 0.30f, y = 0.20f)),
            listOf(
                layer("bedroom_boxes", scale = 0.62f, x = 0.34f, y = 0.17f),
                layer("bedroom_scattered_books", scale = 0.70f, x = -0.25f, y = 0.21f, alpha = 0.58f, z = 1f)
            ),
            listOf(layer("bedroom_desk", scale = 0.76f, x = 0.29f, y = 0.12f)),
            listOf(
                layer("bedroom_desk", scale = 0.76f, x = 0.29f, y = 0.12f),
                layer("bedroom_scattered_books", scale = 0.60f, x = -0.31f, y = 0.21f, alpha = 0.58f, z = 1f)
            ),
            listOf(layer("bedroom_shelf", scale = 0.76f, x = 0.32f, y = 0.01f)),
            listOf(
                layer("bedroom_shelf", scale = 0.70f, x = 0.37f, y = -0.01f),
                layer("bedroom_desk", scale = 0.64f, x = -0.31f, y = 0.15f, alpha = 0.56f, z = 1f)
            ),
            listOf(layer("bedroom_organized", scale = 0.86f, x = 0.27f, y = 0.06f, alpha = 0.62f)),
            listOf(
                layer("bedroom_organized", scale = 0.90f, x = 0.26f, y = 0.05f, alpha = 0.68f),
                layer("bedroom_scattered_books", scale = 0.50f, x = 0.38f, y = 0.22f, alpha = 0.46f, z = 1f)
            )
        )
    )

    private fun layer(
        assetId: String,
        scale: Float = 0.80f,
        x: Float = 0f,
        y: Float = 0f,
        alpha: Float = 0.54f,
        z: Float = 0f
    ) = LayerDefinition(assetId, scale, x, y, alpha, z)

    private val assetsById by lazy { assets.associateBy(SceneAsset::id) }

    private fun assetById(id: String): SceneAsset =
        checkNotNull(assetsById[id]) { "Unknown scene asset: $id" }

    private fun GoalCategory.sceneTheme(): SceneTheme = when (this) {
        GoalCategory.STUDY -> SceneTheme.LIBRARY
        GoalCategory.FOOD_AND_HYDRATION -> SceneTheme.GARDEN
        GoalCategory.EXERCISE -> SceneTheme.GYM
        GoalCategory.SIMPLE_TASK -> SceneTheme.BEDROOM
    }

    private fun scene(id: String, theme: SceneTheme, path: String, step: Int? = null) =
        SceneAsset(id, theme, "cenarios/$path", step)
}

private data class LayerDefinition(
    val assetId: String,
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
    val alpha: Float,
    val zIndex: Float
)

data class SceneLayer(
    val asset: SceneAsset,
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
    val alpha: Float,
    val zIndex: Float
)

data class SceneProgress(
    val layers: List<SceneLayer>,
    val currentStep: Int,
    val totalSteps: Int,
    val longestStreak: Int,
    val daysUntilNextStage: Int
) {
    val assets: List<SceneAsset> get() = layers.map(SceneLayer::asset)
}
