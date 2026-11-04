package com.example.remember.feature.mvp.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.remember.R
import com.example.remember.ui.theme.NeoBackground

/** Debug-only visual host enabled by MainActivity after checking FLAG_DEBUGGABLE. */
@Composable
fun LauncherIconPreviewScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(NeoBackground),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(216.dp).clip(CircleShape)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Prévia do ícone do ReMEmber",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
