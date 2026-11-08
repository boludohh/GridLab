package com.gridlab.app.feature.editor.grid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gridlab.app.R
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.ui.ColorPickerField
import java.util.Locale

@Composable
internal fun GridSizeSection(grid: Grid, viewModel: GridConfigViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EditorIntegerField(
            label = stringResource(R.string.grid_rows_label),
            value = grid.rows,
            onValueChange = viewModel::setRows,
            modifier = Modifier.weight(1f),
        )
        EditorIntegerField(
            label = stringResource(R.string.grid_columns_label),
            value = grid.columns,
            onValueChange = viewModel::setColumns,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun GridVisibilitySection(grid: Grid, viewModel: GridConfigViewModel) {
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
internal fun GridSecondarySection(grid: Grid, viewModel: GridConfigViewModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = grid.secondaryVisible,
            onClick = { viewModel.setSecondaryVisible(true) },
            label = { Text(stringResource(R.string.grid_secondary_on)) },
        )
        FilterChip(
            selected = !grid.secondaryVisible,
            onClick = { viewModel.setSecondaryVisible(false) },
            label = { Text(stringResource(R.string.grid_secondary_off)) },
        )
    }
    Text(
        text = stringResource(R.string.grid_secondary_divisions_label),
        style = MaterialTheme.typography.bodyMedium,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (n in listOf(2, 3, 4, 5)) {
            FilterChip(
                selected = grid.secondaryDivisions == n,
                onClick = { viewModel.setSecondaryDivisions(n) },
                enabled = grid.secondaryVisible,
                label = { Text(text = stringResource(R.string.grid_secondary_divisions_value, n)) },
            )
        }
    }
}

@Composable
internal fun GridLabelsSection(grid: Grid, viewModel: GridConfigViewModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = grid.labelsVisible,
            onClick = { viewModel.setLabelsVisible(true) },
            label = { Text(stringResource(R.string.grid_labels_on)) },
        )
        FilterChip(
            selected = !grid.labelsVisible,
            onClick = { viewModel.setLabelsVisible(false) },
            label = { Text(stringResource(R.string.grid_labels_off)) },
        )
    }
    if (grid.labelsVisible) {
        ColorPickerField(
            label = stringResource(R.string.grid_label_color_label),
            colorArgb = grid.labelColorArgb,
            onColorChange = viewModel::setLabelColorArgb,
            iconRes = R.drawable.ic_palette_filled,
        )
    }
}

@Composable
internal fun GridStrokeSection(grid: Grid, viewModel: GridConfigViewModel) {
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
internal fun GridOpacitySection(grid: Grid, viewModel: GridConfigViewModel) {
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
internal fun GridColorSection(grid: Grid, viewModel: GridConfigViewModel) {
    ColorPickerField(
        label = stringResource(R.string.grid_color_label),
        colorArgb = grid.lineStyle.colorArgb,
        onColorChange = viewModel::setColorArgb,
        iconRes = R.drawable.ic_palette_filled,
    )
}

@Composable
internal fun GridSecondaryColorSection(grid: Grid, viewModel: GridConfigViewModel) {
    ColorPickerField(
        label = stringResource(R.string.grid_secondary_color_label),
        colorArgb = grid.secondaryLineStyle.colorArgb,
        onColorChange = viewModel::setSecondaryColorArgb,
        iconRes = R.drawable.ic_palette_filled,
    )
}

/**
 * Manual save button with its status label. The [modifier] parameter lets the
 * editor top bar render it compact instead of full width.
 */
@Composable
internal fun GridSaveSection(
    uiState: GridConfigUiState,
    onSave: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    Button(
        onClick = onSave,
        enabled = !uiState.isSaving,
        modifier = modifier,
    ) {
        Text(
            text = when {
                uiState.isSaving -> stringResource(R.string.paper_saving)
                uiState.lastSaveSucceeded == true -> stringResource(R.string.paper_save_success)
                uiState.lastSaveSucceeded == false -> stringResource(R.string.paper_save_error)
                uiState.isDirty -> stringResource(R.string.paper_dirty)
                else -> stringResource(R.string.paper_save)
            },
        )
    }
}