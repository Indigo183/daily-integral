package com.dailyintegral.dailyintegral

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*

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

        latex?.let { Text(it) }
    }
}