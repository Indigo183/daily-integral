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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App(dataStore: DataStore) {
    MaterialTheme {
        var selectedGameMode by remember { mutableStateOf(GameMode.Integral) }
        var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }
        var puzzles by remember { mutableStateOf<Map<Puzzle, String>?>(null) }

        LaunchedEffect(Unit) {
            val day = ApiClient.getServerDay()
            puzzles = if (dataStore.getCachedDay() != day) dataStore.updateAllPuzzles(day)
            else dataStore.getCachedPuzzles()
        }

        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            puzzles?.get(
                Puzzle(
                    selectedGameMode,
                    selectedDifficulty?.takeIf {
                        selectedGameMode.difficulties()?.contains(it) == true
                    })
            )?.let { LaTeXView(it) }

            Column(verticalArrangement = Arrangement.SpaceBetween) {
                for (gameMode in GameMode.entries) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedGameMode == gameMode,
                            onClick = { selectedGameMode = gameMode },
                        )
                        Text(gameMode.name)
                    }
                }
            }

            selectedGameMode.difficulties()?.let { difficulties ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (difficulty in difficulties) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            RadioButton(
                                selected = selectedDifficulty == difficulty,
                                onClick = { selectedDifficulty = difficulty },
                            )
                            Text(difficulty.name)
                        }
                    }
                }
            }// ?: run { selectedDifficulty = null }
        }
    }
}
