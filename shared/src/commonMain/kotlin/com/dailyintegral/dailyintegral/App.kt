package com.dailyintegral.dailyintegral

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App(dataStore: DataStore) {
    var selectedGameMode by remember { mutableStateOf(GameMode.Integral) }
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }
    var puzzles by remember { mutableStateOf<Map<Puzzle, String>?>(null) }

    LaunchedEffect(Unit) {
        val temp = dataStore.loadCache()
        puzzles = temp.component1()
        temp.component2()?.component1()?.let { selectedGameMode = it }
        temp.component2()?.component2()?.let { selectedDifficulty = it }
    }

    LaunchedEffect(selectedGameMode) {
        dataStore.setGameMode(selectedGameMode)
    }
    LaunchedEffect(selectedDifficulty) {
        selectedDifficulty?.let { dataStore.setDifficulty(it) }
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
