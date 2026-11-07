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

/** Layout metrics for placing the reference image inside the paper preview. */
internal data class ImageLayerLayout(
    val marginLeft: Float,
    val marginTop: Float,
    val contentW: Float,
    val contentH: Float,
    val scale: Float,
    val mmPerPx: Double,
)

/** Clips to a normalized crop rect (0..1) of the layout bounds. */
private class FractionalCropShape(
    private val left: Float,
    private val top: Float,
    private val right: Float,
    private val bottom: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        return Outline.Rectangle(
            Rect(
                left = size.width * left,
                top = size.height * top,
                right = size.width * right,
                bottom = size.height * bottom,
            ),
        )
    }
}

@Composable
internal fun DocumentPreview(
    paper: Paper,
    grid: Grid,
    imagePath: String?,
    referenceImage: ReferenceImage?,
    interaction: PreviewInteraction,
    measureColor: Color,
    modifier: Modifier = Modifier,
) {
    val layout = remember(paper, grid) { GridCalculator.calculate(paper, grid) }
    val paperColor = Color(paper.colorArgb.toInt())
    val borderColor = MaterialTheme.colorScheme.outline
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    BoxWithConstraints(
        modifier = modifier.background(surfaceVariant).padding(8.dp),
    ) {
        val metrics = rememberPreviewMetrics(paper, layout, constraints.maxWidth, constraints.maxHeight)
        val viewport = ViewportTransform(
            originXPx = metrics.originX,
            originYPx = metrics.originY,
            scale = metrics.scale,
        )
        PaperBackgroundCanvas(
            metrics = metrics,
            paperColor = paperColor,
            borderColor = borderColor,
            measureMode = interaction.measureMode,
            onMeasureTap = interaction.onMeasureTap,
            viewport = viewport,
        )
        PreviewImageLayer(
            imagePath = imagePath,
            referenceImage = referenceImage,
            metrics = metrics,
            measureMode = interaction.measureMode,
            onPan = interaction.onPan,
            onScale = interaction.onScale,
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (grid.visible) {
                drawGridLines(layout, grid, metrics.originX, metrics.originY, metrics.scale, paper)
                if (grid.labelsVisible) {
                    drawGridLabels(layout, grid, metrics.originX, metrics.originY, metrics.scale, paper)
                }
            }
            drawMeasureOverlay(
                viewport,
                interaction.measurePoint1Mm,
                interaction.measurePoint2Mm,
                measureColor,
            )
        }
    }
}

@Composable
internal fun PreviewImageLayer(
    imagePath: String?,
    referenceImage: ReferenceImage?,
    metrics: PreviewMetrics,
    measureMode: Boolean,
    onPan: (deltaXMm: Double, deltaYMm: Double) -> Unit,
    onScale: (factor: Double) -> Unit,
) {
    val resolvedImagePath = imagePath ?: return
    val resolvedImage = referenceImage?.takeIf { it.visible } ?: return
    val density = LocalDensity.current
    val panHandler: (Double, Double) -> Unit =
        if (measureMode) { _, _ -> } else onPan
    val scaleHandler: (Double) -> Unit =
        if (measureMode) { _ -> } else onScale
    Box(
        modifier = Modifier
            .offset(
                x = with(density) { metrics.originX.toDp() },
                y = with(density) { metrics.originY.toDp() },
            )
            .size(
                width = with(density) { metrics.paperW.toDp() },
                height = with(density) { metrics.paperH.toDp() },
            )
            .clip(RectangleShape),
    ) {
        ReferenceImageLayer(
            imagePath = resolvedImagePath,
            image = resolvedImage,
            layout = ImageLayerLayout(
                marginLeft = metrics.marginLeft,
                marginTop = metrics.marginTop,
                contentW = metrics.contentW,
                contentH = metrics.contentH,
                scale = metrics.scale,
                mmPerPx = metrics.mmPerPx,
            ),
            measureMode = measureMode,
            onPan = panHandler,
            onScale = scaleHandler,
        )
    }
}

@Composable
private fun ReferenceImageLayer(
    imagePath: String,
    image: ReferenceImage,
    layout: ImageLayerLayout,
    measureMode: Boolean,
    onPan: (deltaXMm: Double, deltaYMm: Double) -> Unit,
    onScale: (factor: Double) -> Unit,
) {
    val density = LocalDensity.current
    val userScale = image.scale.toFloat()
    val offsetXPx = (image.offsetXMm * layout.scale).toFloat()
    val offsetYPx = (image.offsetYMm * layout.scale).toFloat()
    // Disable transform gestures when the image is locked OR when measure mode is active,
    // so taps pass through to the paper's measure point detector.
    val gesturesEnabled = !image.locked && !measureMode
    // Latest transform values read at gesture time so a simultaneous pinch or a
    // rotation change never uses stale compensation factors, without restarting
    // the pointerInput detector mid-gesture.
    val currentScale = rememberUpdatedState(userScale)
    val currentRotation = rememberUpdatedState(image.rotationDegrees)
    val colorFilter = ImageFilterMatrices.matrix(image.filter)?.let { coeffs ->
        ColorFilter.colorMatrix(ColorMatrix(coeffs))
    }
    AsyncImage(
        model = File(imagePath),
        contentDescription = stringResource(R.string.image_content_description),
        contentScale = ContentScale.Fit,
        colorFilter = colorFilter,
        modifier = Modifier
            .offset(
                x = with(density) { (layout.marginLeft + offsetXPx).toDp() },
                y = with(density) { (layout.marginTop + offsetYPx).toDp() },
            )
            .size(
                width = with(density) { layout.contentW.toDp() },
                height = with(density) { layout.contentH.toDp() },
            )
            .graphicsLayer {
                scaleX = userScale
                scaleY = userScale
                rotationZ = image.rotationDegrees
                alpha = image.opacity
                transformOrigin = TransformOrigin.Center
                // Apply crop inside the graphicsLayer so it rotates with the image
                if (image.hasCrop) {
                    shape = FractionalCropShape(
                        left = image.cropLeft,
                        top = image.cropTop,
                        right = image.cropRight,
                        bottom = image.cropBottom,
                    )
                    clip = true
                }
            }
            .then(
                if (gesturesEnabled) {
                    Modifier.pointerInput(image.locked, layout.scale, measureMode) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            if (pan != Offset.Zero) {
                                // The pan arrives in the image's transformed space
                                // (divided by userScale and rotated by -rotation because
                                // this pointerInput lives inside the graphicsLayer). Map it
                                // back to preview space so dragging feels 1:1 with the
                                // finger at any scale and any rotation.
                                val scale = currentScale.value
                                val radians = Math.toRadians(currentRotation.value.toDouble())
                                val cos = cos(radians).toFloat()
                                val sin = sin(radians).toFloat()
                                val scaledX = pan.x * scale
                                val scaledY = pan.y * scale
                                val previewX = scaledX * cos - scaledY * sin
                                val previewY = scaledX * sin + scaledY * cos
                                onPan(previewX * layout.mmPerPx, previewY * layout.mmPerPx)
                            }
                            if (zoom != 1f) {
                                onScale(zoom.toDouble())
                            }
                        }
                    }
                } else {
                    Modifier
                },
            ),
    )
}

