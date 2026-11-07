package com.gridlab.app.feature.editor.grid

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.gridlab.app.R
import com.gridlab.app.core.geometry.GridCalculator
import com.gridlab.app.core.geometry.GridLabelFormatter
import com.gridlab.app.core.geometry.GridLayout
import com.gridlab.app.core.geometry.ImageFilterMatrices
import com.gridlab.app.core.geometry.ViewportTransform
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.ReferenceImage
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

private data class PreviewMetrics(
    val originX: Float,
    val originY: Float,
    val marginLeft: Float,
    val marginTop: Float,
    val contentW: Float,
    val contentH: Float,
    val scale: Float,
    val mmPerPx: Double,
    val paperW: Float,
    val paperH: Float,
)

@Composable
private fun rememberPreviewMetrics(
    paper: Paper,
    layout: GridLayout,
    maxWInt: Int,
    maxHInt: Int,
): PreviewMetrics {
    val maxW = maxWInt.toFloat()
    val maxH = maxHInt.toFloat()
    val scale = minOf(maxW / paper.widthMm.toFloat(), maxH / paper.heightMm.toFloat())
    val paperW = paper.widthMm.toFloat() * scale
    val paperH = paper.heightMm.toFloat() * scale
    val originX = (maxW - paperW) / 2f
    val originY = (maxH - paperH) / 2f
    return PreviewMetrics(
        originX = originX,
        originY = originY,
        marginLeft = paper.marginLeftMm.toFloat() * scale,
        marginTop = paper.marginTopMm.toFloat() * scale,
        contentW = layout.printableWidthMm.toFloat() * scale,
        contentH = layout.printableHeightMm.toFloat() * scale,
        scale = scale,
        mmPerPx = if (scale > 0f) 1.0 / scale.toDouble() else 1.0,
        paperW = paperW,
        paperH = paperH,
    )
}

@Composable
private fun PaperBackgroundCanvas(
    metrics: PreviewMetrics,
    paperColor: Color,
    borderColor: Color,
    measureMode: Boolean,
    onMeasureTap: (xPx: Float, yPx: Float, viewport: ViewportTransform) -> Unit,
    viewport: ViewportTransform,
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (measureMode) {
                    Modifier.pointerInput(measureMode) {
                        detectTapGestures { offset ->
                            onMeasureTap(offset.x, offset.y, viewport)
                        }
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        drawRect(
            color = paperColor,
            topLeft = Offset(metrics.originX, metrics.originY),
            size = Size(metrics.paperW, metrics.paperH),
        )
        drawRect(
            color = borderColor,
            topLeft = Offset(metrics.originX, metrics.originY),
            size = Size(metrics.paperW, metrics.paperH),
            style = Stroke(width = 2f),
        )
    }
}

/**
 * Draws the reference image clipped to the physical paper rectangle: content
 * outside the sheet is not rendered, while the portion inside remains visible.
 */
private fun DrawScope.drawMeasureOverlay(
    viewport: ViewportTransform,
    p1: Pair<Double, Double>?,
    p2: Pair<Double, Double>?,
    color: Color,
) {
    val radius = 8f
    if (p1 != null) {
        val (x, y) = viewport.documentToScreenPx(p1.first, p1.second)
        drawCircle(color = color, radius = radius, center = Offset(x, y))
    }
    if (p2 != null) {
        val (x, y) = viewport.documentToScreenPx(p2.first, p2.second)
        drawCircle(color = color, radius = radius, center = Offset(x, y))
    }
    if (p1 != null && p2 != null) {
        val (x1, y1) = viewport.documentToScreenPx(p1.first, p1.second)
        val (x2, y2) = viewport.documentToScreenPx(p2.first, p2.second)
        drawLine(color = color, start = Offset(x1, y1), end = Offset(x2, y2), strokeWidth = 3f)
    }
}

private fun DrawScope.drawGridLabels(
    layout: GridLayout,
    grid: Grid,
    originX: Float,
    originY: Float,
    scale: Float,
    paper: Paper,
) {
    val contentOriginX = originX + paper.marginLeftMm.toFloat() * scale
    val contentOriginY = originY + paper.marginTopMm.toFloat() * scale
    val cellW = (layout.cellWidthMm * scale).toFloat()
    val cellH = (layout.cellHeightMm * scale).toFloat()
    val textSizePx = (2.5f * scale).coerceIn(10f, 36f)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = grid.labelColorArgb.toInt()
        textSize = textSizePx
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = android.graphics.Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.apply {
        for (c in 0 until grid.columns) {
            val x = contentOriginX + cellW * (c + 0.5f)
            val y = (contentOriginY - 4f).coerceAtLeast(textSizePx)
            drawText(GridLabelFormatter.columnLabel(c), x, y, paint)
        }
        paint.textAlign = android.graphics.Paint.Align.RIGHT
        for (r in 0 until grid.rows) {
            val y = contentOriginY + cellH * (r + 0.5f) + textSizePx * 0.35f
            val x = contentOriginX - 4f
            if (x > 8f) {
                drawText(GridLabelFormatter.rowLabel(r), x, y, paint)
            } else {
                paint.textAlign = android.graphics.Paint.Align.LEFT
                drawText(GridLabelFormatter.rowLabel(r), contentOriginX + 4f, y, paint)
                paint.textAlign = android.graphics.Paint.Align.RIGHT
            }
        }
    }
}

private fun DrawScope.drawGridLines(
    layout: GridLayout,
    grid: Grid,
    originX: Float,
    originY: Float,
    scale: Float,
    paper: Paper,
) {
    val contentOriginX = originX + paper.marginLeftMm.toFloat() * scale
    val contentOriginY = originY + paper.marginTopMm.toFloat() * scale
    val lineColor = Color(grid.lineStyle.colorArgb.toInt()).copy(alpha = grid.lineStyle.opacity)
    val strokePx = (grid.lineStyle.strokeWidthMm * scale).toFloat().coerceAtLeast(1f)
    val contentH = (layout.printableHeightMm * scale).toFloat()
    val contentW = (layout.printableWidthMm * scale).toFloat()
    for (xMm in layout.verticalLineXsMm) {
        val x = contentOriginX + (xMm * scale).toFloat()
        drawLine(lineColor, Offset(x, contentOriginY), Offset(x, contentOriginY + contentH), strokePx)
    }
    for (yMm in layout.horizontalLineYsMm) {
        val y = contentOriginY + (yMm * scale).toFloat()
        drawLine(lineColor, Offset(contentOriginX, y), Offset(contentOriginX + contentW, y), strokePx)
    }
    if (layout.secondaryVerticalXsMm.isNotEmpty() || layout.secondaryHorizontalYsMm.isNotEmpty()) {
        val secondaryColor = Color(grid.secondaryLineStyle.colorArgb.toInt())
            .copy(alpha = grid.secondaryLineStyle.opacity)
        val secondaryStroke = (grid.secondaryLineStyle.strokeWidthMm * scale).toFloat().coerceAtLeast(0.5f)
        for (xMm in layout.secondaryVerticalXsMm) {
            val x = contentOriginX + (xMm * scale).toFloat()
            drawLine(
                secondaryColor,
                Offset(x, contentOriginY),
                Offset(x, contentOriginY + contentH),
                secondaryStroke,
            )
        }
        for (yMm in layout.secondaryHorizontalYsMm) {
            val y = contentOriginY + (yMm * scale).toFloat()
            drawLine(
                secondaryColor,
                Offset(contentOriginX, y),
                Offset(contentOriginX + contentW, y),
                secondaryStroke,
            )
        }
    }
}