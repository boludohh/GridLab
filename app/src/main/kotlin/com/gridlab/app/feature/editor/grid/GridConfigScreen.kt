package com.gridlab.app.feature.editor.grid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gridlab.app.R
import com.gridlab.app.core.geometry.GridCalculator
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.Paper
import java.util.Locale

@Composable
fun GridConfigScreen(
    modifier: Modifier = Modifier,
    viewModel: GridConfigViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    GridConfigContent(
        uiState = uiState,
        viewModel = viewModel,
        modifier = modifier,
    )
}

@Composable
private fun GridConfigContent(
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
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

        GridPreview(
            paper = paper,
            grid = grid,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(
                    ratio = (paper.widthMm / paper.heightMm).toFloat().coerceIn(0.3f, 3f),
                ),
        )

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

        Text(text = stringResource(R.string.grid_stroke_label), style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(
                R.string.grid_stroke_value,
                String.format(Locale.US, "%.2f", grid.lineStyle.strokeWidthMm),
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Slider(
            value = grid.lineStyle.strokeWidthMm.toFloat(),
            onValueChange = { viewModel.setStrokeWidthMm(it.toDouble()) },
            valueRange = 0.05f..2.0f,
        )

        Text(text = stringResource(R.string.grid_opacity_label), style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(
                R.string.grid_opacity_value,
                (grid.lineStyle.opacity * 100).toInt(),
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Slider(
            value = grid.lineStyle.opacity,
            onValueChange = viewModel::setOpacity,
            valueRange = 0.05f..1.0f,
        )

        Text(text = stringResource(R.string.grid_color_label), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorChip(
                label = stringResource(R.string.grid_color_black),
                selected = grid.lineStyle.colorArgb == 0xFF000000,
                onClick = { viewModel.setColorArgb(0xFF000000) },
            )
            ColorChip(
                label = stringResource(R.string.grid_color_gray),
                selected = grid.lineStyle.colorArgb == 0xFF888888,
                onClick = { viewModel.setColorArgb(0xFF888888) },
            )
            ColorChip(
                label = stringResource(R.string.grid_color_blue),
                selected = grid.lineStyle.colorArgb == 0xFF1565C0,
                onClick = { viewModel.setColorArgb(0xFF1565C0) },
            )
        }

        Button(
            onClick = viewModel::saveDocument,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = if (uiState.isSaving) {
                    stringResource(R.string.paper_saving)
                } else {
                    stringResource(R.string.paper_save)
                },
            )
        }

        when (uiState.lastSaveSucceeded) {
            true -> Text(
                text = stringResource(R.string.paper_save_success),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
            false -> Text(
                text = stringResource(R.string.paper_save_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            null -> Unit
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun GridPreview(
    paper: Paper,
    grid: Grid,
    modifier: Modifier = Modifier,
) {
    val layout = remember(paper, grid) {
        GridCalculator.calculate(paper, grid)
    }
    val paperColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outline

    Canvas(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
    ) {
        val pad = 8f
        val availW = size.width - pad * 2
        val availH = size.height - pad * 2
        val scale = minOf(
            availW / paper.widthMm.toFloat(),
            availH / paper.heightMm.toFloat(),
        )
        val paperW = paper.widthMm.toFloat() * scale
        val paperH = paper.heightMm.toFloat() * scale
        val originX = (size.width - paperW) / 2f
        val originY = (size.height - paperH) / 2f

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

        if (!grid.visible) return@Canvas

        val marginLeft = paper.marginLeftMm.toFloat() * scale
        val marginTop = paper.marginTopMm.toFloat() * scale
        val contentOriginX = originX + marginLeft
        val contentOriginY = originY + marginTop

        val baseColor = Color(grid.lineStyle.colorArgb.toInt())
        val lineColor = baseColor.copy(alpha = grid.lineStyle.opacity)
        val strokePx = (grid.lineStyle.strokeWidthMm * scale).toFloat().coerceAtLeast(1f)

        for (xMm in layout.verticalLineXsMm) {
            val x = contentOriginX + (xMm * scale).toFloat()
            drawLine(
                color = lineColor,
                start = Offset(x, contentOriginY),
                end = Offset(x, contentOriginY + (layout.printableHeightMm * scale).toFloat()),
                strokeWidth = strokePx,
            )
        }
        for (yMm in layout.horizontalLineYsMm) {
            val y = contentOriginY + (yMm * scale).toFloat()
            drawLine(
                color = lineColor,
                start = Offset(contentOriginX, y),
                end = Offset(contentOriginX + (layout.printableWidthMm * scale).toFloat(), y),
                strokeWidth = strokePx,
            )
        }
    }
}

@Composable
private fun ColorChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
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
