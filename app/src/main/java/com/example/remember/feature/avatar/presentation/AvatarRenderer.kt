package com.example.remember.feature.avatar.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import coil3.compose.AsyncImage
import com.example.remember.feature.avatar.data.AvatarCatalog
import com.example.remember.feature.avatar.domain.model.AvatarColors
import com.example.remember.feature.avatar.domain.model.AvatarConfiguration
import com.example.remember.feature.avatar.domain.model.AvatarSlot

private val colorMarker = Regex(
    """((?:fill|stroke)=")[^"]+(" data-(?:stroke-)?color="(SKIN|HAIR|EYE|SHIRT|BEARD|GLASSES|HAT)")"""
)
private val outerSvgTag = Regex("""</?svg\b[^>]*>""")
private val firstGroupTag = Regex("""<g\b""")
private val firstHairPath = Regex("""<path\b[^>]*/>""")

@Composable
fun AvatarRenderer(
    configuration: AvatarConfiguration,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    val avatarSvg = remember(configuration) {
        val selectedFaceId = configuration.equippedItemIds[AvatarSlot.FACE]
        val selectedBodyId = configuration.equippedItemIds[AvatarSlot.BODY]
        val selectedHatId = configuration.equippedItemIds[AvatarSlot.HAT]
        val svgLayers = AvatarCatalog.resolve(configuration).map { item ->
            val source = context.assets.open(item.assetPath).bufferedReader().use { it.readText() }
            when (item.slot) {
                AvatarSlot.HAIR -> adaptHairToFace(
                    maskHairForHat(source, selectedHatId),
                    selectedFaceId
                )
                AvatarSlot.HAT -> adaptHatToFace(source, selectedFaceId)
                AvatarSlot.SHIRT -> adaptShirtToBody(source, item.id, selectedBodyId)
                else -> source
            }
        }
        composeAvatarSvg(svgLayers, configuration.colors).encodeToByteArray()
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .then(
                if (contentDescription == null) Modifier
                else Modifier.semantics { this.contentDescription = contentDescription }
            )
    ) {
        AsyncImage(
            model = avatarSvg,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.matchParentSize()
        )
    }
}

/**
 * Hair assets use the round face as their 1024x1024 reference. This transform keeps
 * the same style while fitting its temples and hairline to the selected face shape.
 */
internal fun adaptHairToFace(svg: String, faceId: String?): String {
    val transform = when (faceId) {
        "face_02" -> "matrix(0.90 0 0 1 51.2 -4)"
        "face_03" -> "matrix(1.08 0 0 1 -40.96 6)"
        else -> return svg
    }
    return firstGroupTag.replaceFirst(svg, "<g transform=\"$transform\"")
}

internal fun maskHairForHat(svg: String, hatId: String?): String {
    val cutLine = when (hatId) {
        "hat_02" -> 300
        "hat_03", "hat_04" -> 280
        else -> return svg
    }
    val withoutTopHair = firstHairPath.replaceFirst(svg, "")
    val svgOpeningEnd = withoutTopHair.indexOf('>')
    if (svgOpeningEnd < 0) return svg
    val visibleHeight = 1024 - cutLine
    val clipDefinition =
        "<defs><clipPath id=\"avatarHairBesideHat\">" +
            "<rect x=\"0\" y=\"$cutLine\" width=\"390\" height=\"$visibleHeight\"/>" +
            "<rect x=\"634\" y=\"$cutLine\" width=\"390\" height=\"$visibleHeight\"/>" +
            "</clipPath></defs>"
    val withClip = withoutTopHair.substring(0, svgOpeningEnd + 1) +
        clipDefinition +
        withoutTopHair.substring(svgOpeningEnd + 1)
    return firstGroupTag.replaceFirst(withClip, "<g clip-path=\"url(#avatarHairBesideHat)\"")
}

internal fun adaptHatToFace(svg: String, faceId: String?): String {
    val transform = when (faceId) {
        "face_02" -> "matrix(0.90 0 0 1 51.2 0)"
        "face_03" -> "matrix(1.08 0 0 1 -40.96 0)"
        else -> return svg
    }
    return firstGroupTag.replaceFirst(svg, "<g transform=\"$transform\"")
}

internal fun adaptShirtToBody(svg: String, shirtId: String, bodyId: String?): String {
    if (shirtId !in setOf("shirt_06", "shirt_07", "shirt_08")) return svg
    val transform = when (bodyId) {
        "body_01" -> "matrix(0.8876 0 0 1 57.55 0)"
        "body_02" -> "matrix(0.9408 0 0 1 30.31 0)"
        else -> return svg
    }
    return firstGroupTag.replaceFirst(svg, "<g transform=\"$transform\"")
}

internal fun composeAvatarSvg(svgLayers: List<String>, colors: AvatarColors): String {
    val content = svgLayers.joinToString(separator = "") { layer ->
        outerSvgTag.replace(layer, "")
    }
    return colorizeSvg(
        """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024">$content</svg>""",
        colors
    )
}

internal fun colorizeSvg(svg: String, colors: AvatarColors): String =
    colorMarker.replace(svg) { match ->
        val marker = match.groupValues[3]
        val color = colors.replacementFor(marker) ?: return@replace match.value
        match.groupValues[1] + color + match.groupValues[2]
    }
