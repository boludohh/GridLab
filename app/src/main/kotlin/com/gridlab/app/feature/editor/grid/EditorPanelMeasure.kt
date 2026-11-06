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
import java.util.Locale

/** Bottom panel with the distance measurement tool. */
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
}

@Composable
private fun unitLabel(unit: LengthUnit): String =
    when (unit) {
        LengthUnit.Millimeter -> stringResource(R.string.unit_mm)
        LengthUnit.Centimeter -> stringResource(R.string.unit_cm)
        LengthUnit.Inch -> stringResource(R.string.unit_in)
    }