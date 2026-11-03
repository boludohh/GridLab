package com.gridlab.app.feature.editor.grid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gridlab.app.R
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.ReferenceImage
import java.util.Locale

@Composable
internal fun GridSizeSection(grid: Grid, viewModel: GridConfigViewModel) {
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
internal fun GridVisibilitySection(grid: Grid, viewModel: GridConfigViewModel) {
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
internal fun GridStrokeSection(grid: Grid, viewModel: GridConfigViewModel) {
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
internal fun GridOpacitySection(grid: Grid, viewModel: GridConfigViewModel) {
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
internal fun GridColorSection(grid: Grid, viewModel: GridConfigViewModel) {
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
internal fun GridSaveSection(uiState: GridConfigUiState, onSave: () -> Unit) {
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
                uiState.isDirty -> stringResource(R.string.paper_dirty)
                else -> stringResource(R.string.paper_save)
            },
        )
    }
}

@Composable
internal fun CropSection(image: ReferenceImage, viewModel: GridConfigViewModel) {
    Text(text = stringResource(R.string.crop_section_title), style = MaterialTheme.typography.titleMedium)
    Text(
        text = stringResource(R.string.crop_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (image.hasCrop) {
        Text(
            text = stringResource(R.string.crop_active),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    CropEdgeSlider(
        label = stringResource(R.string.crop_left),
        value = image.cropLeft,
        valueRange = 0f..(image.cropRight - 0.05f).coerceAtLeast(0f),
        enabled = !image.locked,
        onValueChange = { left ->
            viewModel.setCrop(left, image.cropTop, image.cropRight, image.cropBottom)
        },
    )
    CropEdgeSlider(
        label = stringResource(R.string.crop_top),
        value = image.cropTop,
        valueRange = 0f..(image.cropBottom - 0.05f).coerceAtLeast(0f),
        enabled = !image.locked,
        onValueChange = { top ->
            viewModel.setCrop(image.cropLeft, top, image.cropRight, image.cropBottom)
        },
    )
    CropEdgeSlider(
        label = stringResource(R.string.crop_right),
        value = image.cropRight,
        valueRange = (image.cropLeft + 0.05f).coerceAtMost(1f)..1f,
        enabled = !image.locked,
        onValueChange = { right ->
            viewModel.setCrop(image.cropLeft, image.cropTop, right, image.cropBottom)
        },
    )
    CropEdgeSlider(
        label = stringResource(R.string.crop_bottom),
        value = image.cropBottom,
        valueRange = (image.cropTop + 0.05f).coerceAtMost(1f)..1f,
        enabled = !image.locked,
        onValueChange = { bottom ->
            viewModel.setCrop(image.cropLeft, image.cropTop, image.cropRight, bottom)
        },
    )
    OutlinedButton(
        onClick = { viewModel.clearCrop() },
        enabled = !image.locked && image.hasCrop,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.crop_clear))
    }
}

@Composable
private fun CropEdgeSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium)
    Slider(
        value = value.coerceIn(valueRange.start, valueRange.endInclusive),
        onValueChange = onValueChange,
        valueRange = if (valueRange.endInclusive > valueRange.start) valueRange else 0f..1f,
        enabled = enabled,
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
