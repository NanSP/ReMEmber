package com.example.remember

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.remember.feature.avatar.data.AvatarCatalog
import com.example.remember.feature.avatar.domain.model.AvatarSlot
import com.example.remember.feature.mvp.data.MvpRepository.AvatarSelection
import com.example.remember.feature.mvp.domain.MvpState
import com.example.remember.feature.mvp.presentation.AvatarScreen
import com.example.remember.ui.theme.ReMEmberTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AvatarIntegrationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun avatarEditorRendersAndSelectsAnExpression() {
        var selection: Pair<AvatarSelection, String>? = null
        val state = MvpState(profileName = "Teste", selectedBodyId = "body_03")

        composeRule.setContent {
            ReMEmberTheme {
                AvatarScreen(
                    state = state,
                    avatarConfiguration = AvatarCatalog.defaultConfiguration,
                    contentPadding = PaddingValues(),
                    onSelectItem = { slot, itemId -> selection = slot to itemId },
                    onSelectColor = { _, _ -> }
                )
            }
        }

        composeRule
            .onNodeWithContentDescription("Avatar personalizado de Teste")
            .assertExists()
        composeRule.onNodeWithText("Expressão").performScrollTo().assertExists()
        composeRule.onNodeWithText("Surpreso").performScrollTo().performClick()

        composeRule.runOnIdle {
            assertEquals(AvatarSelection.EYES to "eyes_03", selection)
            assertEquals(6, AvatarCatalog.itemsFor(AvatarSlot.EYES).size)
        }
    }
}
