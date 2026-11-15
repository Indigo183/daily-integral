package com.dailyintegral.dailyintegral

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Difficulty {
    @SerialName("BEGINNER") Beginner,
    @SerialName("EASY") Easy,
    @SerialName("MEDIUM") Medium,
    @SerialName("HARD") Hard,
}

@Serializable
enum class GameMode {
    @SerialName("integral") Integral,
    @SerialName("derivative") Derivative,
    @SerialName("differential") Differential,
    @SerialName("limit") Limit,
    @SerialName("engineering") Engineering;

    fun difficulties(): List<Difficulty>? = when (this) {
        Integral -> Difficulty.entries
        Derivative, Differential -> listOf(Difficulty.Easy, Difficulty.Medium, Difficulty.Hard)
        else -> null
    }
}

typealias Puzzle = Pair<GameMode, Difficulty?>

interface DataStore {
    suspend fun getCachedDay(): Int?
    suspend fun getDefault(): Puzzle?
    suspend fun getCachedPuzzles(): MutableMap<Puzzle, String>?

    suspend fun setCachedDay(day: Int)
    suspend fun setGameMode(gameMode: GameMode)
    suspend fun setDifficulty(difficulty: Difficulty)
    suspend fun setCachedPuzzles(map: Map<Puzzle, String>)

    suspend fun addPuzzles(vararg keys: Pair<Puzzle, String>)

    suspend fun updateAllPuzzles(day: Int): Map<Puzzle, String>? {
        try {
            val map = getCachedPuzzles() ?: mutableMapOf()

            coroutineScope {
                GameMode.entries.flatMap { gameMode ->
                    gameMode.difficulties()?.map { difficulty ->
                        async {
                            Puzzle(gameMode, difficulty) to ApiClient.getDailyPuzzle(
                                day,
                                gameMode,
                                difficulty,
                            )
                        }
                    } ?: listOf(async {
                        Puzzle(gameMode, null) to ApiClient.getDailyPuzzle(
                            day,
                            gameMode,
                            null,
                        )
                    })
                }.awaitAll().toMap()
            }

            setCachedPuzzles(map)
            return map
        } catch (e: Exception) {
            // TODO
            println(e)
            return null
        }
    }
}
