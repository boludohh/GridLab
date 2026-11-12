package com.gridlab.app.ui.theme

import android.app.Activity
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalView
import kotlin.math.hypot
import kotlin.math.max

/**
 * Holds the screenshot overlay used during a Telegram-style circular theme reveal.
 */
class ThemeRevealState {
    var overlay: ImageBitmap? by mutableStateOf(null)
        private set
    var center: Offset by mutableStateOf(Offset.Zero)
        private set
    var progress: Float by mutableFloatStateOf(0f)
        private set
    var isRunning: Boolean by mutableStateOf(false)
        private set

    fun begin(overlay: ImageBitmap, center: Offset) {
        this.overlay = overlay
        this.center = center
        this.progress = 0f
        this.isRunning = true
    }

    fun updateProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
    }

    fun end() {
        overlay = null
        progress = 0f
        isRunning = false
    }
}

/**
 * Draws [state]'s overlay clipped by a growing hole (circular reveal of content underneath).
 */
@Composable
fun ThemeRevealOverlay(
    state: ThemeRevealState,
    modifier: Modifier = Modifier,
) {
    val image = state.overlay ?: return
    val center = state.center
    val progress = state.progress

    Canvas(modifier = modifier.fillMaxSize()) {
        val maxRadius = hypot(
            max(center.x, size.width - center.x).toDouble(),
            max(center.y, size.height - center.y).toDouble(),
        ).toFloat()
        val radius = maxRadius * progress
        val hole = Path().apply {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    center.x - radius,
                    center.y - radius,
                    center.x + radius,
                    center.y + radius,
                ),
            )
        }
        clipPath(hole, clipOp = ClipOp.Difference) {
            drawImage(image)
        }
    }
}

/**
 * Captures the activity window content into an [ImageBitmap] via [PixelCopy].
 * Invokes [onResult] on the main thread; [onResult] receives null on failure.
 */
fun captureActivityBitmap(activity: Activity, onResult: (ImageBitmap?) -> Unit) {
    val view: View = activity.window.decorView
    val width = view.width
    val height = view.height
    if (width <= 0 || height <= 0) {
        onResult(null)
        return
    }
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    try {
        PixelCopy.request(
            activity.window,
            bitmap,
            { result ->
                if (result == PixelCopy.SUCCESS) {
                    onResult(bitmap.asImageBitmap())
                } else {
                    bitmap.recycle()
                    onResult(null)
                }
            },
            Handler(Looper.getMainLooper()),
        )
    } catch (_: Exception) {
        bitmap.recycle()
        onResult(null)
    }
}
