package com.dailyintegral.dailyintegral

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

private const val USER_PREFERENCES_NAME = "user_preferences"

private val Context.dataStore by preferencesDataStore(USER_PREFERENCES_NAME)

class AndroidDataStore(private val context: Context) : DataStore {
    private suspend fun dataStore() = context.dataStore.data.first()
    private val json = Json {
        explicitNulls = false
        allowStructuredMapKeys = true
    }

    override suspend fun getCachedDay(): Int? =
        dataStore()[stringPreferencesKey("day")]?.toInt()

    override suspend fun getDefault(): Puzzle? {
        val gameMode: GameMode = json.decodeFromString(
            dataStore()[stringPreferencesKey("gamemode")] ?: return null)
        val difficulty = dataStore()[stringPreferencesKey("difficulty")]?.let {
            json.decodeFromString<Difficulty>(it)
        }?.takeIf { gameMode.difficulties()?.contains(it) == true }

        return Puzzle(
            gameMode,
            difficulty,
        )
    }

    override suspend fun getCachedPuzzles(): MutableMap<Puzzle, String>? =
        json.decodeFromString(dataStore()[stringPreferencesKey("puzzles")] ?: return null)

    override suspend fun setCachedDay(day: Int) {
        context.dataStore.edit {
            it[stringPreferencesKey("day")] = "$day"
        }
    }

    override suspend fun setGameMode(gameMode: GameMode) {
        context.dataStore.edit {
            it[stringPreferencesKey("gamemode")] = json.encodeToString(gameMode)
        }
    }

    override suspend fun setDifficulty(difficulty: Difficulty) {
        context.dataStore.edit {
            it[stringPreferencesKey("difficulty")] = json.encodeToString(difficulty)
        }
    }

    override suspend fun setCachedPuzzles(map: Map<Puzzle, String>) {
        context.dataStore.edit {
            it[stringPreferencesKey("puzzles")] = json.encodeToString(map)
        }
    }

    override suspend fun addPuzzles(vararg keys: Pair<Puzzle, String>) {
        TODO("Not yet implemented")
    }
}
