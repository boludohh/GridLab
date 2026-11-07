package com.gridlab.app.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gridlab.app.R
import android.graphics.Color as AndroidColor

/**
 * Reusable HSV color picker (saturation/value square + hue bar).
 *
 * Lives in core so editor panels (paper, grid, labels, measure) can share one control.
 * Colors are exchanged as packed ARGB [Long] (e.g. 0xFF1565C0), matching [GridLineStyle.colorArgb].
 */

/** Converts packed ARGB long to Compose [Color]. */
fun argbLongToColor(argb: Long): Color = Color(argb.toInt())

/** Packs Compose [Color] into ARGB long with full opacity unless [color] carries alpha. */
fun colorToArgbLong(color: Color): Long {
    val a = (color.alpha * 255f + 0.5f).toInt().coerceIn(0, 255)
    val r = (color.red * 255f + 0.5f).toInt().coerceIn(0, 255)
    val g = (color.green * 255f + 0.5f).toInt().coerceIn(0, 255)
    val b = (color.blue * 255f + 0.5f).toInt().coerceIn(0, 255)
    return ((a.toLong() and 0xFF) shl 24) or
        ((r.toLong() and 0xFF) shl 16) or
        ((g.toLong() and 0xFF) shl 8) or
        (b.toLong() and 0xFF)
}

private fun colorToHsv(color: Color): FloatArray {
    val hsv = FloatArray(3)
    AndroidColor.RGBToHSV(
        (color.red * 255f + 0.5f).toInt().coerceIn(0, 255),
        (color.green * 255f + 0.5f).toInt().coerceIn(0, 255),
        (color.blue * 255f + 0.5f).toInt().coerceIn(0, 255),
        hsv,
    )
    return hsv
}

private fun hsvToColor(h: Float, s: Float, v: Float): Color {
    val argb = AndroidColor.HSVToColor(floatArrayOf(h, s.coerceIn(0f, 1f), v.coerceIn(0f, 1f)))
    return Color(argb)
}

/**
 * Interactive HSV picker. Reports every intermediate change via [onColorChange].
 */
@Composable
fun HsvColorPicker(
    colorArgb: Long,
    onColorChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initial = argbLongToColor(colorArgb)
    val initialHsv = remember(colorArgb) { colorToHsv(initial) }
    var hue by remember(colorArgb) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember(colorArgb) { mutableFloatStateOf(initialHsv[1]) }
    var value by remember(colorArgb) { mutableFloatStateOf(initialHsv[2]) }

    fun emit(h: Float, s: Float, v: Float) {
        onColorChange(colorToArgbLong(hsvToColor(h, s, v)))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Saturation × Value square for the current hue
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(hue) {
                        detectTapGestures { offset ->
                            val s = (offset.x / size.width).coerceIn(0f, 1f)
                            val v = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                            saturation = s
                            value = v
                            emit(hue, s, v)
                        }
                    }
                    .pointerInput(hue) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val s = (change.position.x / size.width).coerceIn(0f, 1f)
                            val v = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                            saturation = s
                            value = v
                            emit(hue, s, v)
                        }
                    },
            ) {
                val pure = hsvToColor(hue, 1f, 1f)
                // White → pure hue horizontally, then black vertically
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.White, pure),
                    ),
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                    ),
                )
                val thumbX = saturation * size.width
                val thumbY = (1f - value) * size.height
                drawCircle(
                    color = Color.White,
                    radius = 10f,
                    center = Offset(thumbX, thumbY),
                    style = Stroke(width = 3f),
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.35f),
                    radius = 11f,
                    center = Offset(thumbX, thumbY),
                    style = Stroke(width = 1.5f),
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hue bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val h = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                            hue = h
                            emit(h, saturation, value)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val h = (change.position.x / size.width).coerceIn(0f, 1f) * 360f
                            hue = h
                            emit(h, saturation, value)
                        }
                    },
            ) {
                val hueColors = listOf(
                    Color.Red,
                    Color.Yellow,
                    Color.Green,
                    Color.Cyan,
                    Color.Blue,
                    Color.Magenta,
                    Color.Red,
                )
                drawRect(brush = Brush.horizontalGradient(colors = hueColors))
                val thumbX = (hue / 360f) * size.width
                drawCircle(
                    color = Color.White,
                    radius = size.height / 2f,
                    center = Offset(thumbX, size.height / 2f),
                    style = Stroke(width = 3f),
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.35f),
                    radius = size.height / 2f + 1f,
                    center = Offset(thumbX, size.height / 2f),
                    style = Stroke(width = 1.5f),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preview swatch
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(hsvToColor(hue, saturation, value))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
            )
            Text(
                text = String.format("#%06X", colorToArgbLong(hsvToColor(hue, saturation, value)).toInt() and 0xFFFFFF),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Label + color swatch. Tapping opens a dialog with [HsvColorPicker].
 */
@Composable
fun ColorPickerField(
    label: String,
    colorArgb: Long,
    onColorChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }
    var draft by remember(colorArgb) { mutableStateOf(colorArgb) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                draft = colorArgb
                showDialog = true
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(argbLongToColor(colorArgb))
                .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = label) },
            text = {
                HsvColorPicker(
                    colorArgb = draft,
                    onColorChange = { draft = it },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onColorChange(draft)
                        showDialog = false
                    },
                ) {
                    Text(stringResource(R.string.color_picker_apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.color_picker_cancel))
                }
            },
        )
    }
}
