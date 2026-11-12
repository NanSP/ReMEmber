package com.example.remember.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.remember.ui.theme.LocalNeomorphicColors

/** Shared Soft UI tokens. Keeping these values centralized prevents visual drift. */
object NeomorphicTokens {
    val cardCornerRadius = 24.dp
    val controlCornerRadius = 16.dp
    val shadowRadius = 10.dp
    val shadowOffset = 5.dp
    val insetRadius = 7.dp
    val insetOffset = 3.dp
}

/** State-free dual external shadow implemented by Compose draw nodes. */
@Composable
fun Modifier.neomorphicElevated(
    shape: Shape,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    shadowRadius: Dp = NeomorphicTokens.shadowRadius,
    shadowOffset: Dp = NeomorphicTokens.shadowOffset
): Modifier {
    val shadows = LocalNeomorphicColors.current
    return this
    .dropShadow(
        shape = shape,
        shadow = Shadow(
            radius = shadowRadius,
            offset = DpOffset(shadowOffset, shadowOffset),
            color = shadows.darkShadow
        )
    )
    .dropShadow(
        shape = shape,
        shadow = Shadow(
            radius = shadowRadius,
            offset = DpOffset(-shadowOffset, -shadowOffset),
            color = shadows.lightShadow
        )
    )
        .background(backgroundColor, shape)
}

/** Opposing inner shadows make the surface appear carved into the background. */
@Composable
fun Modifier.neomorphicInset(
    shape: Shape,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    shadowRadius: Dp = NeomorphicTokens.insetRadius,
    shadowOffset: Dp = NeomorphicTokens.insetOffset
): Modifier {
    val shadows = LocalNeomorphicColors.current
    return this
    .background(backgroundColor, shape)
    .innerShadow(
        shape = shape,
        shadow = Shadow(
            radius = shadowRadius,
            offset = DpOffset(-shadowOffset, -shadowOffset),
            color = shadows.darkShadow
        )
    )
    .innerShadow(
        shape = shape,
        shadow = Shadow(
            radius = shadowRadius,
            offset = DpOffset(shadowOffset, shadowOffset),
            color = shadows.lightShadow
        )
        )
}

@Composable
fun NeomorphicIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    active: Boolean = false,
    selected: Boolean? = null,
    size: Dp = 56.dp,
    shape: Shape = RoundedCornerShape(NeomorphicTokens.controlCornerRadius),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    iconColor: Color = MaterialTheme.colorScheme.primary,
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
                if (selected != null) this.selected = selected
            },
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides iconColor) {
            Box(
                modifier = if (contentDescription != null) {
                    Modifier.clearAndSetSemantics { }
                } else {
                    Modifier
                },
                content = content
            )
        }
    }
}

@Composable
fun NeomorphicProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surface,
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
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .neomorphicElevated(shape = shape, backgroundColor = backgroundColor)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(contentPadding),
        content = content
    )
}

/** Full-width Soft UI action. Pointer state is reflected by the same inset treatment as icon buttons. */
@Composable
fun NeomorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: Shape = RoundedCornerShape(NeomorphicTokens.controlCornerRadius),
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val resolvedBackgroundColor = if (enabled) {
        backgroundColor
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val resolvedContentColor = if (enabled) {
        contentColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val surface = if (pressed && enabled) {
        Modifier.neomorphicInset(shape, resolvedBackgroundColor)
    } else {
        Modifier.neomorphicElevated(
            shape,
            resolvedBackgroundColor,
            shadowRadius = 7.dp,
            shadowOffset = 3.dp
        )
    }

    Row(
        modifier = modifier
            .then(surface)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(contentPadding),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentColor provides resolvedContentColor) {
            content()
        }
    }
}

/** Reusable inset/elevated selector used in onboarding, goals and avatar customization. */
@Composable
fun NeomorphicChoice(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val surface = if (selected) {
        Modifier.neomorphicInset(shape, background)
    } else {
        Modifier.neomorphicElevated(shape, background, shadowRadius = 6.dp, shadowOffset = 3.dp)
    }
    Box(
        modifier = modifier
            .then(surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .selectable(
                selected = selected,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                role = Role.RadioButton,
                onClick = onClick
            )
            .semantics { this.selected = selected }
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            content()
        }
    }
}

/** Compact Soft UI toggle with an inset track and elevated thumb. */
@Composable
fun NeomorphicSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 3.dp,
        animationSpec = tween(180),
        label = "neomorphicSwitchThumb"
    )
    val trackShape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .size(width = 52.dp, height = 48.dp)
            .toggleable(
                value = checked,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .semantics(mergeDescendants = true) {
                if (contentDescription != null) this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 52.dp, height = 30.dp)
                .neomorphicInset(
                    trackShape,
                    if (checked) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surface
                )
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .padding(start = thumbOffset)
                    .size(24.dp)
                    .neomorphicElevated(
                        shape = RoundedCornerShape(50),
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        shadowRadius = 4.dp,
                        shadowOffset = 2.dp
                    )
            )
        }
    }
}

@Composable
fun neomorphicTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
    errorTextColor = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    errorBorderColor = Color.Transparent,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
    disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
    focusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
)
