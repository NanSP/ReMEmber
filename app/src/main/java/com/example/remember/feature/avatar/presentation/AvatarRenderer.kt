package com.example.remember.feature.avatar.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.example.remember.core.assets.AssetLocation
import com.example.remember.feature.avatar.data.AvatarCatalog
import com.example.remember.feature.avatar.domain.model.AvatarConfiguration

@Composable
fun AvatarRenderer(
    configuration: AvatarConfiguration,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val layers = remember(configuration) {
        AvatarCatalog.resolve(configuration)
    }

    Box(modifier = modifier) {
        layers.forEachIndexed { index, item ->
            AsyncImage(
                model = AssetLocation.uri(item.assetPath),
                contentDescription = if (index == layers.lastIndex) {
                    contentDescription
                } else {
                    null
                },
                contentScale = ContentScale.Fit,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}
