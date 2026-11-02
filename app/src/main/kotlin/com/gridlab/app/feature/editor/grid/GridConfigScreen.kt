package com.gridlab.app.feature.editor.grid

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.gridlab.app.R
import com.gridlab.app.core.geometry.GridCalculator
import com.gridlab.app.core.geometry.GridLayout
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.ReferenceImage
import java.io.File
import java.util.Locale

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
            onPan = { dx, dy -> viewModel.panImage(dx, dy) },
            onScale = { factor -> viewModel.scaleImageBy(factor) },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(
                    ratio = (paper.widthMm / paper.heightMm).toFloat().coerceIn(0.3f, 3f),
                ),
        )
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
    onPan: (deltaXMm: Double, deltaYMm: Double) -> Unit,
    onScale: (factor: Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = remember(paper, grid) { GridCalculator.calculate(paper, grid) }
    val paperColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outline
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    BoxWithConstraints(
        modifier = modifier
            .background(surfaceVariant)
            .padding(8.dp),
    ) {
        val density = LocalDensity.current
        val maxW = constraints.maxWidth.toFloat()
        val maxH = constraints.maxHeight.toFloat()
        val scale = minOf(maxW / paper.widthMm.toFloat(), maxH / paper.heightMm.toFloat())
        val paperW = paper.widthMm.toFloat() * scale
        val paperH = paper.heightMm.toFloat() * scale
        val originX = (maxW - paperW) / 2f
        val originY = (maxH - paperH) / 2f
        val marginLeft = paper.marginLeftMm.toFloat() * scale
        val marginTop = paper.marginTopMm.toFloat() * scale
        val contentW = layout.printableWidthMm.toFloat() * scale
        val contentH = layout.printableHeightMm.toFloat() * scale

        // mm per screen pixel in the current preview
        val mmPerPx = if (scale > 0f) 1.0 / scale.toDouble() else 1.0

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = paperColor,
                topLeft = Offset(originX, originY),
                size = Size(paperW, paperH),
            )
            drawRect(
                color = borderColor,
                topLeft = Offset(originX, originY),
                size = Size(paperW, paperH),
                style = Stroke(width = 2f),
            )
        }

        val showImage = imagePath != null &&
            referenceImage != null &&
            referenceImage.visible

        if (showImage) {
            val img = referenceImage!!
            val userScale = img.scale.toFloat()
            val offsetXPx = (img.offsetXMm * scale).toFloat()
            val offsetYPx = (img.offsetYMm * scale).toFloat()

            AsyncImage(
                model = File(imagePath!!),
                contentDescription = stringResource(R.string.image_content_description),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .offset(
                        x = with(density) { (originX + marginLeft + offsetXPx).toDp() },
                        y = with(density) { (originY + marginTop + offsetYPx).toDp() },
                    )
                    .size(
                        width = with(density) { contentW.toDp() },
                        height = with(density) { contentH.toDp() },
                    )
                    .graphicsLayer {
                        scaleX = userScale
                        scaleY = userScale
                        alpha = img.opacity
                        // Scale around the center of the fitted box
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                    }
                    .then(
                        if (!img.locked) {
                            Modifier.pointerInput(img.locked, scale) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    if (pan != Offset.Zero) {
                                        onPan(pan.x * mmPerPx, pan.y * mmPerPx)
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

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (grid.visible) {
                drawGridLines(layout, grid, originX, originY, scale, paper)
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
}

@Composable
private fun GridSizeSection(grid: Grid, viewModel: GridConfigViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IntField(
            label = stringResource(R.string.grid_rows_label),
            value = grid.rows,
            onValueChange = viewModel::setRows,
            modifier = Modifier.weight(1f),
        )
        IntField(
            label = stringResource(R.string.grid_columns_label),
            value = grid.columns,
            onValueChange = viewModel::setColumns,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun GridVisibilitySection(grid: Grid, viewModel: GridConfigViewModel) {
    Text(text = stringResource(R.string.grid_visibility_label), style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = grid.visible,
            onClick = { viewModel.setVisible(true) },
            label = { Text(stringResource(R.string.grid_visible_on)) },
        )
        FilterChip(
            selected = !grid.visible,
            onClick = { viewModel.setVisible(false) },
            label = { Text(stringResource(R.string.grid_visible_off)) },
        )
    }
}

@Composable
private fun GridStrokeSection(grid: Grid, viewModel: GridConfigViewModel) {
    Text(text = stringResource(R.string.grid_stroke_label), style = MaterialTheme.typography.titleMedium)
    Slider(
        value = grid.lineStyle.strokeWidthMm.toFloat(),
        onValueChange = { viewModel.setStrokeWidthMm(it.toDouble()) },
        valueRange = 0.05f..5f,
    )
    Text(
        text = stringResource(
            R.string.grid_stroke_value,
            String.format(Locale.US, "%.2f", grid.lineStyle.strokeWidthMm),
        ),
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun GridOpacitySection(grid: Grid, viewModel: GridConfigViewModel) {
    Text(text = stringResource(R.string.grid_opacity_label), style = MaterialTheme.typography.titleMedium)
    Slider(
        value = grid.lineStyle.opacity,
        onValueChange = viewModel::setOpacity,
        valueRange = 0.05f..1f,
    )
    Text(
        text = stringResource(R.string.grid_opacity_value, (grid.lineStyle.opacity * 100).toInt()),
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun GridColorSection(grid: Grid, viewModel: GridConfigViewModel) {
    Text(text = stringResource(R.string.grid_color_label), style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = grid.lineStyle.colorArgb == 0xFF000000,
            onClick = { viewModel.setColorArgb(0xFF000000) },
            label = { Text(stringResource(R.string.grid_color_black)) },
        )
        FilterChip(
            selected = grid.lineStyle.colorArgb == 0xFF808080,
            onClick = { viewModel.setColorArgb(0xFF808080) },
            label = { Text(stringResource(R.string.grid_color_gray)) },
        )
        FilterChip(
            selected = grid.lineStyle.colorArgb == 0xFF1565C0,
            onClick = { viewModel.setColorArgb(0xFF1565C0) },
            label = { Text(stringResource(R.string.grid_color_blue)) },
        )
    }
}

@Composable
private fun GridSaveSection(uiState: GridConfigUiState, onSave: () -> Unit) {
    Button(
        onClick = onSave,
        enabled = !uiState.isSaving,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = when {
                uiState.isSaving -> stringResource(R.string.paper_saving)
                uiState.lastSaveSucceeded == true -> stringResource(R.string.paper_save_success)
                uiState.lastSaveSucceeded == false -> stringResource(R.string.paper_save_error)
                else -> stringResource(R.string.paper_save)
            },
        )
    }
}

@Composable
private fun IntField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { text ->
            text.toIntOrNull()?.let(onValueChange)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}
