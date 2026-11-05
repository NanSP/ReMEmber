package com.example.remember

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import com.example.remember.feature.avatar.data.AvatarCatalog
import com.example.remember.feature.avatar.domain.model.AvatarSlot
import com.example.remember.feature.mvp.presentation.AvatarGroupPanel
import com.example.remember.ui.theme.ReMEmberTheme
import androidx.test.filters.SdkSuppress
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@SdkSuppress(minSdkVersion = 30)
class AvatarIntegrationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test(timeout = 30_000)
    fun expressionPanelSelectsAnExpression() {
        var selection: String? = null
        val choices = AvatarCatalog.itemsFor(AvatarSlot.EYES).map { it.id to it.labelRes }

        composeRule.setContent {
            ReMEmberTheme {
                AvatarGroupPanel(
                    title = R.string.avatar_expression,
                    choices = choices,
                    selected = "eyes_02",
                    onSelect = { selection = it }
                )
            }
        }

        composeRule.onNodeWithTag("avatar_option_eyes_01")
            .performSemanticsAction(SemanticsActions.OnClick)

        composeRule.runOnIdle {
            assertEquals("eyes_01", selection)
            assertEquals(6, AvatarCatalog.itemsFor(AvatarSlot.EYES).size)
        }
    }
}
