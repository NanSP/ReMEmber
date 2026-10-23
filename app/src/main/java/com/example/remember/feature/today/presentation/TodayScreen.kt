package com.example.remember.feature.today.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.remember.feature.avatar.data.AvatarCatalog
import com.example.remember.feature.avatar.presentation.AvatarRenderer
import com.example.remember.ui.theme.ReMEmberTheme

@Composable
fun TodayScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ReMEmber",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Seu progresso de hoje",
            style = MaterialTheme.typography.bodyLarge
        )
        AvatarRenderer(
            configuration = AvatarCatalog.defaultConfiguration,
            contentDescription = "Avatar atual do usuário",
            modifier = Modifier
                .fillMaxWidth()
                .size(320.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenPreview() {
    ReMEmberTheme {
        TodayScreen()
    }
}
