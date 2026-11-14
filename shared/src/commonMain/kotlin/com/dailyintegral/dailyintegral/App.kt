package com.dailyintegral.dailyintegral

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App(apiClient: ApiClient) {
    MaterialTheme {
        var latex by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(Unit) {
            val day = apiClient.getServerDay()
            latex = apiClient.getDailyPuzzle(PuzzleRequest(
                day = day,
                difficulty = "EASY",
                type = "integral"
            ))
        }

        latex?.let { latex ->
            Column(
                Modifier.padding(12.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                LaTeXView(latex)
            }
        }
    }
}