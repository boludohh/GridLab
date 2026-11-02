package com.gridlab.app.feature.editor.grid

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.gridlab.app.R
import com.gridlab.app.core.geometry.GridCalculator
import com.gridlab.app.core.geometry.GridLayout
import com.gridlab.app.core.geometry.ViewportTransform
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.LengthUnit
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.ReferenceImage
import java.io.File
import java.util.Locale

/** Layout metrics for placing the reference image inside the paper preview. */
private data class ImageLayerLayout(
    val originX: Float,
    val originY: Float,
    val marginLeft: Float,
    val marginTop: Float,
    val contentW: Float,
    val contentH: Float,
    val scale: Float,
    val mmPerPx: Double,
)

/** Measure state + interaction callbacks for the paper preview (keeps DocumentPreview ≤ 8 params). */
private data class PreviewInteraction(
    val measureMode: Boolean,
    val measurePoint1Mm: Pair<Double, Double>?,
    val measurePoint2Mm: Pair<Double, Double>?,
    val onPan: (deltaXMm: Double, deltaYMm: Double) -> Unit,
    val onScale: (factor: Double) -> Unit,
    val onMeasureTap: (xPx: Float, yPx: Float, viewport: ViewportTransform) -> Unit,
)

@Composable
fun GridConfigScreen(
    modifier: Modifier = Modifier,
    viewModel: GridConfigViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.importImage(uri)
    }

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    GridConfigContent(
        uiState = uiState,
        viewModel = viewModel,
        onImportClick = {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun GridConfigContent(
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val grid = uiState.grid
    val paper = uiState.paper
    val interaction = PreviewInteraction(
        measureMode = uiState.measureMode,
        measurePoint1Mm = uiState.measurePoint1Mm,
        measurePoint2Mm = uiState.measurePoint2Mm,
        onPan = { dx, dy -> viewModel.panImage(dx, dy) },
        onScale = { factor -> viewModel.scaleImageBy(factor) },
        onMeasureTap = { x, y, vt -> viewModel.placeMeasurePoint(x, y, vt) },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.grid_config_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        DocumentPreview(
            paper = paper,
            grid = grid,
            imagePath = uiState.imageAbsolutePath,
            referenceImage = uiState.referenceImage,
            interaction = interaction,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(
                    ratio = (paper.widthMm / paper.heightMm).toFloat().coerceIn(0.3f, 3f),
                ),
        )
        MeasureSection(uiState = uiState, viewModel = viewModel)
        ImageImportSection(
            hasImage = uiState.imageAbsolutePath != null,
            isImporting = uiState.isImporting,
            importError = uiState.importError,
            onImportClick = onImportClick,
        )
        if (uiState.referenceImage != null) {
            ImageTransformSection(
                image = uiState.referenceImage,
                viewModel = viewModel,
            )
            CropSection(
                image = uiState.referenceImage,
                viewModel = viewModel,
            )
        }
        GridSizeSection(grid = grid, viewModel = viewModel)
        GridVisibilitySection(grid = grid, viewModel = viewModel)
        GridStrokeSection(grid = grid, viewModel = viewModel)
        GridOpacitySection(grid = grid, viewModel = viewModel)
        GridColorSection(grid = grid, viewModel = viewModel)
        GridSaveSection(uiState = uiState, onSave = viewModel::saveDocument)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MeasureSection(
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
) {
    Text(
        text = stringResource(R.string.measure_section_title),
        style = MaterialTheme.typography.titleMedium,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = uiState.measureMode,
            onClick = { viewModel.setMeasureMode(!uiState.measureMode) },
            label = {
                Text(
                    text = if (uiState.measureMode) {
                        stringResource(R.string.measure_mode_on)
                    } else {
                        stringResource(R.string.measure_mode_off)
                    },
                )
            },
        )
        if (uiState.measureMode) {
            OutlinedButton(onClick = { viewModel.clearMeasurePoints() }) {
                Text(text = stringResource(R.string.measure_clear))
            }
        }
    }
    if (uiState.measureMode) {
        Text(
            text = stringResource(R.string.measure_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val distance = uiState.measureDistanceDisplay()
        if (distance != null) {
            val unitLabel = unitLabel(uiState.paper.displayUnit)
            Text(
                text = stringResource(
                    R.string.measure_distance_value,
                    String.format(Locale.US, "%.2f", distance),
                    unitLabel,
                ),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun unitLabel(unit: LengthUnit): String =
    when (unit) {
        LengthUnit.Millimeter -> stringResource(R.string.unit_mm)
        LengthUnit.Centimeter -> stringResource(R.string.unit_cm)
        LengthUnit.Inch -> stringResource(R.string.unit_in)
    }

@Composable
private fun ImageImportSection(
    hasImage: Boolean,
    isImporting: Boolean,
    importError: Boolean,
    onImportClick: () -> Unit,
) {
    Text(text = stringResource(R.string.image_section_title), style = MaterialTheme.typography.titleMedium)
    OutlinedButton(
        onClick = onImportClick,
        enabled = !isImporting,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = when {
                isImporting -> stringResource(R.string.image_importing)
                hasImage -> stringResource(R.string.image_replace)
                else -> stringResource(R.string.image_import)
            },
        )
    }
    if (importError) {
        Text(
            text = stringResource(R.string.image_import_error),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ImageTransformSection(
    image: ReferenceImage,
    viewModel: GridConfigViewModel,
) {
    Text(
        text = stringResource(R.string.image_transform_title),
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = stringResource(R.string.image_transform_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    ImageVisibilityLockChips(image = image, viewModel = viewModel)
    ImageOpacityControls(image = image, viewModel = viewModel)
}

@Composable
private fun ImageVisibilityLockChips(
    image: ReferenceImage,
    viewModel: GridConfigViewModel,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = image.visible,
            onClick = { viewModel.setImageVisible(!image.visible) },
            label = {
                Text(
                    text = if (image.visible) {
                        stringResource(R.string.image_visible_on)
                    } else {
                        stringResource(R.string.image_visible_off)
                    },
                )
            },
        )
        FilterChip(
            selected = image.locked,
            onClick = { viewModel.setImageLocked(!image.locked) },
            label = {
                Text(
                    text = if (image.locked) {
                        stringResource(R.string.image_locked_on)
                    } else {
                        stringResource(R.string.image_locked_off)
                    },
                )
            },
        )
    }
}

@Composable
private fun ImageOpacityControls(
    image: ReferenceImage,
    viewModel: GridConfigViewModel,
) {
    Text(
        text = stringResource(R.string.image_opacity_label),
        style = MaterialTheme.typography.bodyMedium,
    )
    Slider(
        value = image.opacity,
        onValueChange = { viewModel.setImageOpacity(it) },
        valueRange = 0.05f..1f,
        enabled = !image.locked,
    )
    Text(
        text = stringResource(R.string.image_opacity_value, (image.opacity * 100).toInt()),
        style = MaterialTheme.typography.bodySmall,
    )
    OutlinedButton(
        onClick = { viewModel.resetImageTransform() },
        enabled = !image.locked,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.image_reset_transform))
    }
}

@Composable
private fun DocumentPreview(
    paper: Paper,
    grid: Grid,
    imagePath: String?,
    referenceImage: ReferenceImage?,
    interaction: PreviewInteraction,
    modifier: Modifier = Modifier,
) {
    val layout = remember(paper, grid) { GridCalculator.calculate(paper, grid) }
    val paperColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outline
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val measureColor = MaterialTheme.colorScheme.primary

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

@Composable
private fun PreviewImageLayer(
    imagePath: String?,
    referenceImage: ReferenceImage?,
    metrics: PreviewMetrics,
    measureMode: Boolean,
    onPan: (deltaXMm: Double, deltaYMm: Double) -> Unit,
    onScale: (factor: Double) -> Unit,
) {
    val show = imagePath != null && referenceImage != null && referenceImage.visible
    if (!show) return
    val panHandler: (Double, Double) -> Unit =
        if (measureMode) { _, _ -> } else onPan
    val scaleHandler: (Double) -> Unit =
        if (measureMode) { _ -> } else onScale
    ReferenceImageLayer(
        imagePath = imagePath!!,
        image = referenceImage!!,
        layout = ImageLayerLayout(
            originX = metrics.originX,
            originY = metrics.originY,
            marginLeft = metrics.marginLeft,
            marginTop = metrics.marginTop,
            contentW = metrics.contentW,
            contentH = metrics.contentH,
            scale = metrics.scale,
            mmPerPx = metrics.mmPerPx,
        ),
        onPan = panHandler,
        onScale = scaleHandler,
    )
}

@Composable
private fun ReferenceImageLayer(
    imagePath: String,
    image: ReferenceImage,
    layout: ImageLayerLayout,
    onPan: (deltaXMm: Double, deltaYMm: Double) -> Unit,
    onScale: (factor: Double) -> Unit,
) {
    val density = LocalDensity.current
    val userScale = image.scale.toFloat()
    val offsetXPx = (image.offsetXMm * layout.scale).toFloat()
    val offsetYPx = (image.offsetYMm * layout.scale).toFloat()
    val gesturesEnabled = !image.locked

    AsyncImage(
        model = File(imagePath),
        contentDescription = stringResource(R.string.image_content_description),
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .offset(
                x = with(density) {
                    (layout.originX + layout.marginLeft + offsetXPx).toDp()
                },
                y = with(density) {
                    (layout.originY + layout.marginTop + offsetYPx).toDp()
                },
            )
            .size(
                width = with(density) { layout.contentW.toDp() },
                height = with(density) { layout.contentH.toDp() },
            )
            .graphicsLayer {
                scaleX = userScale
                scaleY = userScale
                alpha = image.opacity
                transformOrigin = TransformOrigin.Center
            }
            .drawWithContent {
                if (image.hasCrop) {
                    val w = size.width
                    val h = size.height
                    clipRect(
                        left = w * image.cropLeft,
                        top = h * image.cropTop,
                        right = w * image.cropRight,
                        bottom = h * image.cropBottom,
                    ) {
                        hannah.h@example.com()
                    }
                } else {
                    hannah.h@example.com()
                }
            }
            .then(
                if (gesturesEnabled) {
                    Modifier.pointerInput(image.locked, layout.scale) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            if (pan != Offset.Zero) {
                                onPan(pan.x * layout.mmPerPx, pan.y * layout.mmPerPx)
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
}
