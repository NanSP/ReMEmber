package com.example.remember.feature.scenes.data

import com.example.remember.feature.scenes.domain.model.SceneAsset
import com.example.remember.feature.scenes.domain.model.SceneTheme

object SceneCatalog {
    val assets = listOf(
        scene("gym_bench", SceneTheme.GYM, "academia/academia_banco.svg"),
        scene("gym_rope", SceneTheme.GYM, "academia/academia_corda.svg"),
        scene("gym_treadmill", SceneTheme.GYM, "academia/academia_esteira.svg"),
        scene("gym_dumbbell", SceneTheme.GYM, "academia/academia_halter.svg"),
        scene("gym_machine", SceneTheme.GYM, "academia/academia_maquina.svg"),
        scene("gym_blue_mat", SceneTheme.GYM, "academia/academia_tapete_azul.svg"),
        scene("gym_purple_mat", SceneTheme.GYM, "academia/academia_tapete_roxo.svg"),
        scene("library_lamp", SceneTheme.LIBRARY, "biblioteca/biblioteca_abajur.svg"),
        scene("library_full", SceneTheme.LIBRARY, "biblioteca/biblioteca_estante_cheia.svg", 4),
        scene("library_half", SceneTheme.LIBRARY, "biblioteca/biblioteca_estante_meio_cheia.svg", 3),
        scene("library_few", SceneTheme.LIBRARY, "biblioteca/biblioteca_estante_poucos_livros.svg", 2),
        scene("library_empty", SceneTheme.LIBRARY, "biblioteca/biblioteca_estante_vazia.svg", 1),
        scene("library_armchair", SceneTheme.LIBRARY, "biblioteca/biblioteca_poltrona.svg"),
        scene("garden_pumpkin", SceneTheme.GARDEN, "horta/horta_abobora.svg"),
        scene("garden_sprouts", SceneTheme.GARDEN, "horta/horta_brotos.svg"),
        scene("garden_corn", SceneTheme.GARDEN, "horta/horta_milho.svg"),
        scene("garden_mixed", SceneTheme.GARDEN, "horta/horta_misto.svg"),
        scene("garden_cabbage", SceneTheme.GARDEN, "horta/horta_repolho.svg"),
        scene("garden_soil", SceneTheme.GARDEN, "horta/horta_terra.svg"),
        scene("garden_tomato", SceneTheme.GARDEN, "horta/horta_tomate.svg"),
        scene("plantation_flowers", SceneTheme.PLANTATION, "plantacao/plantacao_arvore_florida.svg", 5),
        scene("plantation_fruits", SceneTheme.PLANTATION, "plantacao/plantacao_arvore_frutos.svg", 6),
        scene("plantation_small_tree", SceneTheme.PLANTATION, "plantacao/plantacao_arvore_pequena.svg", 4),
        scene("plantation_sprout", SceneTheme.PLANTATION, "plantacao/plantacao_broto.svg", 2),
        scene("plantation_seedling", SceneTheme.PLANTATION, "plantacao/plantacao_muda.svg", 3),
        scene("plantation_seed", SceneTheme.PLANTATION, "plantacao/plantacao_semente.svg", 1),
        scene("bedroom_boxes", SceneTheme.BEDROOM, "quarto/quarto_caixas_bagunca.svg"),
        scene("bedroom_shelf", SceneTheme.BEDROOM, "quarto/quarto_estante.svg"),
        scene("bedroom_scattered_books", SceneTheme.BEDROOM, "quarto/quarto_livros_espalhados.svg"),
        scene("bedroom_desk", SceneTheme.BEDROOM, "quarto/quarto_mesa.svg"),
        scene("bedroom_organized", SceneTheme.BEDROOM, "quarto/quarto_quarto_organizado.svg")
    )

    fun forTheme(theme: SceneTheme): List<SceneAsset> = assets.filter { it.theme == theme }

    private fun scene(id: String, theme: SceneTheme, path: String, step: Int? = null) =
        SceneAsset(id, theme, "cenarios/$path", step)
}
