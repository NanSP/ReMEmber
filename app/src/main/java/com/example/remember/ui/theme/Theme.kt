package com.example.remember.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.isSystemInDarkTheme
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
    outlineVariant = Color(0xFF60749A),
    scrim = NeoText,
    surfaceBright = NeoBackground,
    surfaceDim = NeoElement,
    surfaceContainer = NeoBackground,
    surfaceContainerLow = NeoBackground,
    surfaceContainerLowest = NeoBackground,
    surfaceContainerHigh = NeoElement,
    surfaceContainerHighest = NeoElement
)

private val DarkColorScheme = darkColorScheme(
    primary = NeoDarkPrimary,
    onPrimary = NeoDarkOnPrimary,
    primaryContainer = NeoDarkElement,
    onPrimaryContainer = NeoDarkText,
    secondary = Color(0xFFBCCBF2),
    onSecondary = NeoDarkOnPrimary,
    secondaryContainer = NeoDarkElement,
    onSecondaryContainer = NeoDarkText,
    tertiary = NeoDarkPrimary,
    onTertiary = NeoDarkOnPrimary,
    tertiaryContainer = NeoDarkElement,
    onTertiaryContainer = NeoDarkText,
    background = NeoDarkBackground,
    onBackground = NeoDarkText,
    surface = NeoDarkSurface,
    onSurface = NeoDarkText,
    surfaceVariant = NeoDarkElement,
    onSurfaceVariant = Color(0xFFD2DDF2),
    surfaceTint = Color.Transparent,
    inverseSurface = NeoDarkText,
    inverseOnSurface = NeoDarkBackground,
    inversePrimary = NeoButton,
    outline = NeoDarkOutline,
    outlineVariant = Color(0xFF7085AA),
    scrim = Color.Black,
    surfaceBright = Color(0xFF24324A),
    surfaceDim = NeoDarkBackground,
    surfaceContainer = NeoDarkSurface,
    surfaceContainerLow = Color(0xFF151F31),
    surfaceContainerLowest = Color(0xFF0C121E),
    surfaceContainerHigh = Color(0xFF223049),
    surfaceContainerHighest = NeoDarkElement
)

@Immutable
data class NeomorphicColors(
    val darkShadow: Color,
    val lightShadow: Color
)

val LocalNeomorphicColors = staticCompositionLocalOf {
    NeomorphicColors(
        darkShadow = NeoDarkShadow,
        lightShadow = NeoLightShadow
    )
}

@Composable
fun ReMEmberTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val neomorphicColors = if (darkTheme) {
        NeomorphicColors(NeoDarkDarkShadow, NeoDarkLightShadow)
    } else {
        NeomorphicColors(NeoDarkShadow, NeoLightShadow)
    }
    CompositionLocalProvider(LocalNeomorphicColors provides neomorphicColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides colorScheme.onBackground,
                content = content
            )
        }
    }
}
