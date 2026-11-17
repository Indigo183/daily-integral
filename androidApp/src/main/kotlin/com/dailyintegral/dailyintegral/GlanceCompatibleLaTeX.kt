package com.dailyintegral.dailyintegral

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.noties.jlatexmath.JLatexMathDrawable
import kotlin.coroutines.CoroutineContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import androidx.core.graphics.createBitmap
import androidx.glance.unit.ColorProvider
import androidx.core.graphics.withTranslation

private const val RENDER_TEXT_SIZE_PX = 128f
private const val CACHE_SIZE_BYTES = 4 * 1024 * 1024

// RemoteViews caps the total bitmap memory per widget update (~6.2 MB, i.e.
// ~1.55M pixels in ARGB_8888) and throws if exceeded. Renders are therefore
// capped at 80% of that budget, leaving room for other layers.
private const val MAX_BITMAP_PIXELS = 1_200_000L

// Corner radius and inner padding, in dp, applied when drawing the card into
// the bitmap. GlanceModifier.cornerRadius is only implemented on API 31+ (it
// logs "Cannot set the rounded corner of views before Api 31" and no-ops),
// so the rounded background is baked into the bitmap instead.
private const val CARD_CORNER_RADIUS_DP = 8f
private const val CARD_PADDING_DP = 8f

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
    // The widget's current size, used to render the card at the same aspect
    // ratio as the widget so the Image (ContentScale.Fit) fills it exactly.
    val widgetSize = LocalSize.current

    val bitmap by produceState<Bitmap?>(
        initialValue = null,
        latex,
        backgroundColour,
        foregroundColour,
        widgetSize,
    ) {
        value = withContext(latexRenderingContext) {
            renderLaTeXCard(
                latex = latex ?: fallback,
                backgroundInt = backgroundColour.toArgb(),
                foregroundInt = foregroundColour.toArgb(),
                widgetSize = widgetSize,
            )
        }
    }

    val rendered = bitmap
    if (rendered != null) {
        Image(
            provider = ImageProvider(rendered),
            contentDescription = latex,
            modifier = modifier
                .fillMaxSize()
                .padding(4.dp),
        )
    }
}

/**
 * Draws [latex] (or plain text when it is not a valid formula) centered on a
 * rounded card of [backgroundInt], sized to the widget's aspect ratio, and
 * returns the resulting bitmap (cached).
 */
private fun renderLaTeXCard(
    latex: String,
    backgroundInt: Int,
    foregroundInt: Int,
    widgetSize: androidx.compose.ui.unit.DpSize,
): Bitmap {
    val drawable = try {
        JLatexMathDrawable.builder(latex)
            .textSize(RENDER_TEXT_SIZE_PX)
            .color(foregroundInt)
            .build()
    } catch (_: Exception) {
        // Invalid LaTeX (parse errors, unsupported commands, ...).
        null
    }

    // Choose the card aspect: the widget's when known (so the card fills the
    // widget after ContentScale.Fit), otherwise hug the formula itself.
    val aspect = if (widgetSize.width.value > 0f && widgetSize.height.value > 0f) {
        widgetSize.width.value / widgetSize.height.value
    } else {
        drawable?.intrinsicWidth?.toFloat()?.div(max(drawable.intrinsicHeight, 1)) ?: 2f
    }

    // Fit into the RemoteViews pixel budget.
    val height = sqrt(MAX_BITMAP_PIXELS / aspect).toInt().coerceAtLeast(1)
    val width = (height * aspect).toInt().coerceAtLeast(1)

    val cacheKey = "$backgroundInt|$foregroundInt|$width x $height|$latex"
    bitmapCache.get(cacheKey)?.let { return it }

    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)

    // Pixels per dp: the card is widgetMinDim dp tall/wide at bitmapMinDim px.
    val minDimDp = if (widgetSize.width.value > 0f && widgetSize.height.value > 0f) {
        min(widgetSize.width.value, widgetSize.height.value)
    } else {
        150f
    }
    val pxPerDp = min(width, height) / minDimDp
    val cornerRadius = CARD_CORNER_RADIUS_DP * pxPerDp
    val padding = CARD_PADDING_DP * pxPerDp

    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = backgroundInt }
    canvas.drawRoundRect(
        RectF(0f, 0f, width.toFloat(), height.toFloat()),
        cornerRadius,
        cornerRadius,
        cardPaint,
    )

    if (drawable != null) {
        // Scale the formula to fit inside the padded box, centered.
        val intrinsicWidth = drawable.intrinsicWidth.coerceAtLeast(1)
        val intrinsicHeight = drawable.intrinsicHeight.coerceAtLeast(1)
        val scale = min(
            (width - 2 * padding) / intrinsicWidth,
            (height - 2 * padding) / intrinsicHeight,
        )
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        canvas.withTranslation(
            (width - intrinsicWidth * scale) / 2f,
            (height - intrinsicHeight * scale) / 2f
        ) {
            scale(scale, scale)
            drawable.draw(this)
        }
    } else {
        // Fallback text, centered.
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = foregroundInt
            textSize = 0.18f * (height - 2 * padding)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(latex, width / 2f, height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f, textPaint)
    }

    bitmapCache.put(cacheKey, bitmap)
    return bitmap
}
