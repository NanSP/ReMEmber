package com.example.remember.feature.mvp.presentation

import android.animation.ValueAnimator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.remember.R
import com.example.remember.ui.theme.NeoBackground
import com.example.remember.ui.theme.NeoButton
import kotlinx.coroutines.delay

private val AlfaSlabOne = FontFamily(Font(R.font.alfa_slab_one_regular, FontWeight.Normal))
private val AnticSlab = FontFamily(Font(R.font.antic_slab_regular, FontWeight.Normal))

@Composable
fun LoadingScreen(
    readyToContinue: Boolean,
    previewStage: Int? = null,
    onFinished: () -> Unit
) {
    val animationsEnabled = remember { ValueAnimator.areAnimatorsEnabled() }
    var stage by remember(previewStage) {
        mutableIntStateOf(previewStage ?: if (animationsEnabled) 0 else 3)
    }
    var revealFinished by remember(previewStage) {
        mutableStateOf(previewStage == null && !animationsEnabled)
    }
    var logoVisible by remember { mutableStateOf(true) }
    val loadingDescription = stringResource(R.string.loading_brand_description)
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val logoFontSize = when {
        screenWidthDp < 380 -> 52.sp
        screenWidthDp < 412 -> 56.sp
        else -> 64.sp
    }

    LaunchedEffect(animationsEnabled, previewStage) {
        if (previewStage != null) return@LaunchedEffect
        if (!animationsEnabled) return@LaunchedEffect
        delay(320)
        stage = 1
        delay(320)
        stage = 2
        delay(380)
        stage = 3
        revealFinished = true
    }

    LaunchedEffect(revealFinished, readyToContinue, animationsEnabled, previewStage) {
        if (previewStage != null) return@LaunchedEffect
        if (!revealFinished || !readyToContinue) return@LaunchedEffect
        if (animationsEnabled) {
            delay(700)
            logoVisible = false
            delay(280)
        } else {
            delay(120)
        }
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoBackground)
            .semantics {
                contentDescription = loadingDescription
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = logoVisible,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(280))
        ) {
            AnimatedContent(
                targetState = stage,
                transitionSpec = {
                    (fadeIn(tween(150)) + scaleIn(tween(150), initialScale = 0.97f))
                        .togetherWith(fadeOut(tween(90)))
                },
                label = "rememberBrandReveal"
            ) { currentStage ->
                BrandStage(currentStage, logoFontSize)
            }
        }
    }
}

@Composable
private fun BrandStage(stage: Int, fontSize: androidx.compose.ui.unit.TextUnit) {
    val text = when (stage) {
        0 -> "re"
        1 -> "remem"
        2 -> "remember"
        else -> null
    }
    if (text != null) {
        Text(
            text = text,
            color = NeoButton,
            fontFamily = AlfaSlabOne,
            fontSize = fontSize,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            softWrap = false
        )
        return
    }

    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontFamily = AlfaSlabOne, color = NeoButton)) {
                append("re")
            }
            withStyle(
                SpanStyle(
                    fontFamily = AnticSlab,
                    color = Color.White,
                    shadow = Shadow(
                        color = NeoButton,
                        offset = Offset.Zero,
                        blurRadius = 2.2f
                    )
                )
            ) {
                append("ME")
            }
            withStyle(SpanStyle(fontFamily = AlfaSlabOne, color = NeoButton)) {
                append("mber")
            }
        },
        fontSize = fontSize,
        maxLines = 1,
        softWrap = false
    )
}
