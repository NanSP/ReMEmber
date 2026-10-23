package com.example.remember.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.remember.feature.today.presentation.TodayScreen

@Composable
fun RememberMeApp() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TodayScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}
