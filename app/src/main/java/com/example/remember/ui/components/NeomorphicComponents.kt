package com.example.remember.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.remember.ui.theme.NeoBackground
import com.example.remember.ui.theme.NeoButton
import com.example.remember.ui.theme.NeoDarkShadow
import com.example.remember.ui.theme.NeoElement
import com.example.remember.ui.theme.NeoLightShadow

/** Shared Soft UI tokens. Keeping these values centralized prevents visual drift. */
object NeomorphicTokens {
    val background = NeoBackground
    val accent = NeoElement
    val foreground = NeoButton
    val darkShadow = NeoDarkShadow
    val lightShadow = NeoLightShadow

    val cardCornerRadius = 24.dp
    val controlCornerRadius = 16.dp
    val shadowRadius = 10.dp
    val shadowOffset = 5.dp
    val insetRadius = 7.dp
    val insetOffset = 3.dp
}

/** State-free dual external shadow implemented by Compose draw nodes. */
fun Modifier.neomorphicElevated(
    shape: Shape,
    backgroundColor: Color = NeomorphicTokens.background,
    shadowRadius: Dp = NeomorphicTokens.shadowRadius,
    shadowOffset: Dp = NeomorphicTokens.shadowOffset
): Modifier = this
    .dropShadow(
        shape = shape,
        shadow = Shadow(
            radius = shadowRadius,
            offset = DpOffset(shadowOffset, shadowOffset),
            color = NeomorphicTokens.darkShadow
        )
    )
    .dropShadow(
        shape = shape,
        shadow = Shadow(
            radius = shadowRadius,
            offset = DpOffset(-shadowOffset, -shadowOffset),
            color = NeomorphicTokens.lightShadow
        )
    )
    .background(backgroundColor, shape)

/** Opposing inner shadows make the surface appear carved into the background. */
fun Modifier.neomorphicInset(
    shape: Shape,
    backgroundColor: Color = NeomorphicTokens.background,
    shadowRadius: Dp = NeomorphicTokens.insetRadius,
    shadowOffset: Dp = NeomorphicTokens.insetOffset
): Modifier = this
    .background(backgroundColor, shape)
    .innerShadow(
        shape = shape,
        shadow = Shadow(
            radius = shadowRadius,
            offset = DpOffset(-shadowOffset, -shadowOffset),
            color = NeomorphicTokens.darkShadow
        )
    )
    .innerShadow(
        shape = shape,
        shadow = Shadow(
            radius = shadowRadius,
            offset = DpOffset(shadowOffset, shadowOffset),
            color = NeomorphicTokens.lightShadow
        )
    )

@Composable
fun NeomorphicIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    active: Boolean = false,
    size: Dp = 56.dp,
    shape: Shape = RoundedCornerShape(NeomorphicTokens.controlCornerRadius),
    backgroundColor: Color = NeomorphicTokens.background,
    iconColor: Color = NeomorphicTokens.foreground,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pointerPressed by interactionSource.collectIsPressedAsState()
    val inset = active || pointerPressed
    val stateModifier = if (inset) {
        Modifier.neomorphicInset(shape, backgroundColor)
    } else {
        Modifier.neomorphicElevated(shape, backgroundColor)
    }

    Box(
        modifier = modifier
            .size(size)
            .then(stateModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            },
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides iconColor) {
            content()
        }
    }
}

@Composable
fun NeomorphicProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color = NeomorphicTokens.foreground,
    trackColor: Color = NeomorphicTokens.background,
    shape: Shape = RoundedCornerShape(50),
    animate: Boolean = true
) {
    val target = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = if (animate) 350 else 0),
        label = "neomorphicProgress"
    )

    Box(
        modifier = modifier
            .height(18.dp)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = target,
                    range = 0f..1f
                )
            }
            .neomorphicInset(
                shape = shape,
                backgroundColor = trackColor,
                shadowRadius = 5.dp,
                shadowOffset = 2.dp
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(progressColor, shape)
        )
    }
}

@Composable
fun NeomorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(NeomorphicTokens.cardCornerRadius),
    backgroundColor: Color = NeomorphicTokens.background,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .neomorphicElevated(shape = shape, backgroundColor = backgroundColor)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun neomorphicTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = NeomorphicTokens.foreground,
    unfocusedTextColor = NeomorphicTokens.foreground,
    disabledTextColor = NeomorphicTokens.foreground,
    errorTextColor = NeomorphicTokens.foreground,
    focusedContainerColor = NeomorphicTokens.background,
    unfocusedContainerColor = NeomorphicTokens.background,
    disabledContainerColor = NeomorphicTokens.background,
    errorContainerColor = NeomorphicTokens.background,
    cursorColor = NeomorphicTokens.foreground,
    focusedBorderColor = NeomorphicTokens.foreground,
    unfocusedBorderColor = NeomorphicTokens.accent,
    disabledBorderColor = NeomorphicTokens.accent,
    focusedLabelColor = NeomorphicTokens.foreground,
    unfocusedLabelColor = NeomorphicTokens.foreground,
    disabledLabelColor = NeomorphicTokens.foreground,
    focusedPlaceholderColor = NeomorphicTokens.foreground,
    unfocusedPlaceholderColor = NeomorphicTokens.foreground,
    disabledPlaceholderColor = NeomorphicTokens.foreground,
    focusedSupportingTextColor = NeomorphicTokens.foreground,
    unfocusedSupportingTextColor = NeomorphicTokens.foreground,
    disabledSupportingTextColor = NeomorphicTokens.foreground
)
