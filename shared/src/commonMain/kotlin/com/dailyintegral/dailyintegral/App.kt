package com.dailyintegral.dailyintegral

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App(apiClient: ApiClient) {
    MaterialTheme {
        var selectedDifficulty by remember { mutableStateOf(Difficulty.HARD) }
        var latex by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(selectedDifficulty) {
            val day = apiClient.getServerDay()
            latex = apiClient.getDailyPuzzle(PuzzleRequest(
                day = day,
                difficulty = selectedDifficulty,
                type = "integral"
            ))
        }

        latex?.let { latex ->
            Column(
                Modifier.padding(12.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                LaTeXView(latex)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    for (difficulty in Difficulty.entries) {
                        Column {
                            Text(difficulty.name)
                            RadioButton(
                                selected = selectedDifficulty == difficulty,
                                onClick = { selectedDifficulty = difficulty },
                            )
                        }
                    }
                }
            }
        }
    }
}