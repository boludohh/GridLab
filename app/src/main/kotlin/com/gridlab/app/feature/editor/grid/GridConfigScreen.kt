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
fun GridConfigScreen(
    documentId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GridConfigViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.importImage(uri)
    }

    LaunchedEffect(documentId) {
        viewModel.openDocument(documentId)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.flushPendingSave()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
        onBack = onBack,
        onImportClick = {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        modifier = modifier,
    )
}

/**
 * Editor shell: fixed top bar, always-visible paper preview, tool toolbar and
 * a fixed-height bottom panel whose content scrolls internally. This keeps the
 * preview on screen while the user adjusts any setting.
 */
@Composable
private fun GridConfigContent(
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
    onBack: () -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeTool by remember { mutableStateOf<EditorTool?>(null) }
    var lastTool by remember { mutableStateOf(EditorTool.Grid) }
    val interaction = PreviewInteraction(
        measureMode = uiState.measureMode,
        measurePoint1Mm = uiState.measurePoint1Mm,
        measurePoint2Mm = uiState.measurePoint2Mm,
        onPan = { dx, dy -> viewModel.panImage(dx, dy) },
        onScale = { factor -> viewModel.scaleImageBy(factor) },
        onMeasureTap = { x, y, vt -> viewModel.placeMeasurePoint(x, y, vt) },
    )

    Column(modifier = modifier.fillMaxSize()) {
        EditorTopBar(
            documentName = uiState.documentName,
            uiState = uiState,
            onBack = {
                viewModel.flushPendingSave()
                onBack()
            },
            onSave = viewModel::saveDocument,
        )
        DocumentPreview(
            paper = uiState.paper,
            grid = uiState.grid,
            imagePath = uiState.imageAbsolutePath,
            referenceImage = uiState.referenceImage,
            interaction = interaction,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        EditorToolbar(
            selectedTool = activeTool,
            onToolClick = { tool ->
                lastTool = tool
                activeTool = if (activeTool == tool) null else tool
            },
        )
        AnimatedVisibility(
            visible = activeTool != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            EditorPanelHost(
                tool = activeTool ?: lastTool,
                uiState = uiState,
                viewModel = viewModel,
                onImportClick = onImportClick,
            )
        }
    }
}

@Composable
private fun EditorTopBar(
    documentName: String,
    uiState: GridConfigUiState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.surfaceVariant)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.projects_back),
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = documentName,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        GridSaveSection(
            uiState = uiState,
            onSave = onSave,
            modifier = Modifier,
        )
    }
}

/**
 * Fixed-height container for the active tool panel. Only its inner content
 * scrolls, so the panel keeps its size and shape while the preview stays put.
 */
@Composable
private fun EditorPanelHost(
    tool: EditorTool,
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.45f)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        key(tool) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (tool) {
                    EditorTool.Grid -> EditorGridPanel(
                        grid = uiState.grid,
                        viewModel = viewModel,
                    )
                    EditorTool.Image -> EditorImagePanel(
                        availability = uiState.imageAvailability,
                        isImporting = uiState.isImporting,
                        importError = uiState.importError,
                        image = uiState.referenceImage,
                        onImportClick = onImportClick,
                        onRemoveClick = { viewModel.removeReferenceImage() },
                        viewModel = viewModel,
                    )
                    EditorTool.Measure -> EditorMeasurePanel(
                        uiState = uiState,
                        viewModel = viewModel,
                    )
                    EditorTool.Export -> EditorExportPanel(
                        uiState = uiState,
                        viewModel = viewModel,
                    )
                }
            }
        }
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
            .then(
                if (image.hasCrop) {
                    Modifier.clip(
                        FractionalCropShape(
                            left = image.cropLeft,
                            top = image.cropTop,
                            right = image.cropRight,
                            bottom = image.cropBottom,
                        ),
                    )
                } else {
                    Modifier
                },
            )
            .graphicsLayer {
                scaleX = userScale
                scaleY = userScale
                rotationZ = image.rotationDegrees
                alpha = image.opacity
                transformOrigin = TransformOrigin.Center
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
        color = android.graphics.Color.BLACK
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