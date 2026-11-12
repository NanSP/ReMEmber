package com.example.remember

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.filters.SdkSuppress
import com.example.remember.ui.components.NeomorphicChoice
import com.example.remember.ui.components.NeomorphicIconButton
import com.example.remember.ui.components.NeomorphicSwitch
import com.example.remember.ui.theme.NeoDarkBackground
import com.example.remember.ui.theme.ReMEmberTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@SdkSuppress(minSdkVersion = 30)
class AccessibilityComponentsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun switchExposesToggleStateAndMinimumTouchTarget() {
        composeRule.setContent {
            ReMEmberTheme {
                var checked by remember { mutableStateOf(true) }
                NeomorphicSwitch(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    contentDescription = "Som",
                    modifier = Modifier.testTag("sound_switch")
                )
            }
        }

        composeRule.onNodeWithTag("sound_switch")
            .assertIsOn()
            .assertHasClickAction()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
    }

    @Test
    fun choiceAndNavigationButtonExposeSelectedState() {
        composeRule.setContent {
            ReMEmberTheme {
                Column {
                    NeomorphicChoice(
                        selected = true,
                        onClick = {},
                        modifier = Modifier.testTag("selected_choice")
                    ) { }
                    NeomorphicIconButton(
                        onClick = {},
                        selected = true,
                        contentDescription = "Início",
                        modifier = Modifier.testTag("selected_navigation")
                    ) { }
                }
            }
        }

        composeRule.onNodeWithTag("selected_choice").assertIsSelected()
        composeRule.onNodeWithTag("selected_navigation").assertIsSelected()
    }

    @Test
    fun darkThemeUsesDarkBackgroundPalette() {
        var background = Color.Unspecified
        composeRule.setContent {
            ReMEmberTheme(darkTheme = true) {
                val themeBackground = MaterialTheme.colorScheme.background
                SideEffect { background = themeBackground }
            }
        }

        composeRule.runOnIdle {
            assertEquals(NeoDarkBackground, background)
        }
    }
}
