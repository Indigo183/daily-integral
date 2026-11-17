package com.dailyintegral.dailyintegral

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent

class WidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = Widget()
}

class Widget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = AndroidDataStore(context)

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running
        // operations.

        val puzzle = dataStore.getDefault()

        // no internet? no problem!
        val day = try { ApiClient.getServerDay() } catch (e: Exception) { null }
        val puzzles = if (day != null && dataStore.getCachedDay() != day)
            dataStore.updateAllPuzzles(day)
        else dataStore.getCachedPuzzles()

        val latex = puzzle?.let {
            puzzles?.get(
                Puzzle(
                    puzzle.component1(),
                    puzzle.component2().takeIf {
                        puzzle.component1().difficulties()?.contains(it) == true
                    }
                )
            )
        }

        provideContent {
            GlanceCompatibleLaTeX(
                latex,
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}