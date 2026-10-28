package com.example.remember.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NeoButton,
    onPrimary = Color.White,
    primaryContainer = NeoElement,
    onPrimaryContainer = NeoText,
    secondary = NeoElement,
    onSecondary = NeoText,
    secondaryContainer = NeoElement,
    onSecondaryContainer = NeoText,
    tertiary = NeoButton,
    onTertiary = Color.White,
    tertiaryContainer = NeoElement,
    onTertiaryContainer = NeoText,
    background = NeoBackground,
    onBackground = NeoText,
    surface = NeoBackground,
    onSurface = NeoText,
    surfaceVariant = NeoElement,
    onSurfaceVariant = NeoText,
    surfaceTint = Color.Transparent,
    inverseSurface = NeoButton,
    inverseOnSurface = Color.White,
    inversePrimary = NeoElement,
    outline = NeoButton,
    outlineVariant = NeoElement,
    scrim = NeoText,
    surfaceBright = NeoBackground,
    surfaceDim = NeoElement,
    surfaceContainer = NeoBackground,
    surfaceContainerLow = NeoBackground,
    surfaceContainerLowest = NeoBackground,
    surfaceContainerHigh = NeoElement,
    surfaceContainerHighest = NeoElement
)

@Composable
fun ReMEmberTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
