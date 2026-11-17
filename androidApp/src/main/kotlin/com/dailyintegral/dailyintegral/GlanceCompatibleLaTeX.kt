package com.dailyintegral.dailyintegral

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.noties.jlatexmath.JLatexMathDrawable
import kotlin.coroutines.CoroutineContext
import androidx.core.graphics.createBitmap

private const val RENDER_TEXT_SIZE_PX = 128f
private const val CACHE_SIZE_BYTES = 4 * 1024 * 1024
private val latexRenderingContext: CoroutineContext = Dispatchers.Default

private val bitmapCache = object : LruCache<String, Bitmap>(CACHE_SIZE_BYTES) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount
}

@SuppressLint("RestrictedApi")
@Composable
fun GlanceCompatibleLaTeX(
    latex: String?,
    backgroundColour: Color,
    foregroundColour: Color,
    fallback: String = "no latex :(",
    modifier: GlanceModifier = GlanceModifier,
) {
    Column(modifier.fillMaxSize().background(backgroundColour)) {
        if (latex.isNullOrBlank()) {
            Text(
                fallback,
                modifier = modifier,
                style = TextStyle(color = androidx.glance.unit.ColorProvider(foregroundColour)),
            )
        } else {
            val bitmap by produceState<Bitmap?>(initialValue = null, latex, foregroundColour) {
                value = withContext(latexRenderingContext) {
                    renderLaTeXToBitmap(latex = latex, colourInt = foregroundColour.toArgb())
                }
            }

            val rendered = bitmap
            if (rendered != null) {
                Image(
                    provider = ImageProvider(rendered),
                    contentDescription = latex,
                    modifier = modifier.fillMaxSize(),
                )
            }
        }
    }
}

private fun renderLaTeXToBitmap(
    latex: String,
    colourInt: Int,
): Bitmap? {
    val cacheKey = "$colourInt|$latex"
    bitmapCache.get(cacheKey)?.let { return it }

    val drawable = try {
        JLatexMathDrawable.builder(latex)
            .textSize(RENDER_TEXT_SIZE_PX)
            .color(colourInt)
            .build()
    } catch (_: Exception) { return null }

    val width = drawable.intrinsicWidth.coerceAtLeast(1)
    val height = drawable.intrinsicHeight.coerceAtLeast(1)

    val bitmap = createBitmap(width, height)
    drawable.setBounds(0, 0, width, height)
    drawable.draw(Canvas(bitmap))

    bitmapCache.put(cacheKey, bitmap)
    return bitmap
}
