package com.example.remember.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ForestLight,
    onPrimary = Night,
    secondary = TangerineLight,
    background = Night,
    onBackground = NightInk,
    surface = NightSurface,
    onSurface = NightInk
)

private val LightColorScheme = lightColorScheme(
    primary = Forest,
    onPrimary = Cream,
    secondary = Tangerine,
    onSecondary = Cream,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = MintSurface,
    onSurfaceVariant = Moss
)

@Composable
fun ReMEmberTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
