package com.gridlab.app.feature.editor.grid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gridlab.app.R
import com.gridlab.app.core.model.LengthUnit
import com.gridlab.app.core.ui.ColorPickerField
import com.gridlab.app.ui.theme.GridLabTheme

/**
 * Bottom panel for the measure tool. Toolbar already shows "Measure"; this panel
 * only uses short block subtitles with leading icons (Mode, Color, Distance).
 * The Distance block is visible only when Mode is On.
 *
 * Note: the Color block uses ColorPickerField with iconRes directly (same pattern
 * as Paper/Grid), not a separate EditorSectionHeader, to avoid duplicating the
 * "Color" label that the field already renders.
 */
@Composable
internal fun EditorMeasurePanel(
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Block 1: Mode (On/Off toggle)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_architecture,
                title = stringResource(R.string.measure_mode_label),
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
            if (uiState.measureMode) {
                val colors = GridLabTheme.colors
                Text(
                    text = stringResource(R.string.measure_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }

        // Block 2: Color (ColorPickerField with iconRes; no separate header needed)
        ColorPickerField(
            label = stringResource(R.string.measure_color_label),
            colorArgb = uiState.measureColorArgb,
            onColorChange = viewModel::setMeasureColorArgb,
            iconRes = R.drawable.ic_palette_filled,
        )

        // Block 3: Distance (only when mode is On)
        if (uiState.measureMode) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EditorSectionHeader(
                    iconRes = R.drawable.ic_straighten_filled,
                    title = stringResource(R.string.measure_distance_label),
                )
                MeasureDistanceSection(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun MeasureDistanceSection(
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
) {
    val colors = GridLabTheme.colors
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
            color = colors.textPrimary,
        )
    } else {
        Text(
            text = stringResource(R.string.measure_no_points),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textTertiary,
        )
    }
    if (uiState.measurePoint1Mm != null || uiState.measurePoint2Mm != null) {
        OutlinedButton(
            onClick = viewModel::clearMeasurePoints,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.measure_clear))
        }
    }
}