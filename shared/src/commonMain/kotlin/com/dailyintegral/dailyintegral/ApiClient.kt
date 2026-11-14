package com.dailyintegral.dailyintegral

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
enum class Difficulty { BEGINNER, EASY, MEDIUM, HARD }

@Serializable
enum class GameMode {
    integral, derivative, limit, differential, engineering;

    fun difficulties(): List<Difficulty>? = when (this) {
        integral -> Difficulty.entries
        derivative, differential -> listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)
        else -> null
    }
}


@Serializable
@JsonIgnoreUnknownKeys
data class ServerDay(val day: Int)

@Serializable
@JsonIgnoreUnknownKeys
data class DailyPuzzle(
    val title: String,
    val latex: String,
)

@Serializable
@JsonIgnoreUnknownKeys
data class PuzzleOuterThingy(val puzzle: DailyPuzzle)

@Serializable
data class PuzzleRequest(
    val day: Int,
    val difficulty: Difficulty?,
    val type: GameMode,
)

class ApiClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { explicitNulls = false })
        }
    }

    suspend fun getServerDay(): Int {
        val response = client.post("https://btulehndzikuesmrzmhd.supabase.co/functions/v1/get-server-day") {
            contentType(ContentType.Application.Json)
        }

        return Json.decodeFromString<ServerDay>(response.bodyAsText()).day
    }

    suspend fun getDailyPuzzle(day: Int, difficulty: Difficulty?, gameMode: GameMode): String {
        val response = client.post("https://btulehndzikuesmrzmhd.supabase.co/functions/v1/get-daily-puzzle") {
            contentType(ContentType.Application.Json)
            println()
            setBody(PuzzleRequest(day, difficulty, gameMode))
        }

        println(response.request.content)
        println(response.bodyAsText())
        val latex = Json.decodeFromString<PuzzleOuterThingy>(response.bodyAsText()).puzzle.latex
        return latex
            .split("\n\n")
            .dropLast(1)
            .joinToString("\\\\")
    }
}