package com.gridlab.app.feature.editor.grid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gridlab.app.R
import com.gridlab.app.core.geometry.UnitConverter
import com.gridlab.app.core.model.LengthUnit
import com.gridlab.app.core.ui.ColorPickerField

/** Bottom panel for the measure tool (mode toggle, distance readout, color). */
@Composable
internal fun EditorMeasurePanel(
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.measure_section_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.measureMode,
                onClick = { viewModel.setMeasureMode(true) },
                label = { Text(stringResource(R.string.measure_mode_on)) },
            )
            FilterChip(
                selected = !uiState.measureMode,
                onClick = { viewModel.setMeasureMode(false) },
                label = { Text(stringResource(R.string.measure_mode_off)) },
            )
        }
        ColorPickerField(
            label = stringResource(R.string.measure_color_label),
            colorArgb = uiState.measureColorArgb,
            onColorChange = viewModel::setMeasureColorArgb,
        )
        val distance = uiState.measureDistanceDisplay()
        if (distance != null) {
            val unitLabel = when (uiState.paper.displayUnit) {
                LengthUnit.Millimeter -> stringResource(R.string.unit_mm)
                LengthUnit.Centimeter -> stringResource(R.string.unit_cm)
                LengthUnit.Inch -> stringResource(R.string.unit_in)
            }
            Text(
                text = stringResource(
                    R.string.measure_distance_value,
                    formatDecimalForDisplay(distance),
                    unitLabel,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            Text(
                text = stringResource(R.string.measure_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (uiState.measurePoint1Mm != null || uiState.measurePoint2Mm != null) {
            androidx.compose.material3.OutlinedButton(
                onClick = viewModel::clearMeasurePoints,
            ) {
                Text(stringResource(R.string.measure_clear))
            }
        }
    }
}
