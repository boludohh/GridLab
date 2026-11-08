package com.gridlab.app.feature.editor.grid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gridlab.app.R
import com.gridlab.app.core.model.Grid

/**
 * Bottom panel for grid settings. Toolbar already shows "Grid"; this panel only
 * uses short block subtitles with leading icons (Size, Visibility, Stroke, …).
 */
@Composable
internal fun EditorGridPanel(
    grid: Grid,
    viewModel: GridConfigViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_grid_3x3,
                title = stringResource(R.string.grid_size_section_label),
            )
            GridSizeSection(grid = grid, viewModel = viewModel)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_visibility_filled,
                title = stringResource(R.string.grid_visibility_label),
            )
            GridVisibilitySection(grid = grid, viewModel = viewModel)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_line_weight,
                title = stringResource(R.string.grid_stroke_section_label),
            )
            GridStrokeSection(grid = grid, viewModel = viewModel)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_opacity,
                title = stringResource(R.string.grid_opacity_label),
            )
            GridOpacitySection(grid = grid, viewModel = viewModel)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            GridColorSection(grid = grid, viewModel = viewModel)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_grid_4x4,
                title = stringResource(R.string.grid_secondary_section_label),
            )
            GridSecondarySection(grid = grid, viewModel = viewModel)
            if (grid.secondaryVisible) {
                GridSecondaryColorSection(grid = grid, viewModel = viewModel)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_label_filled,
                title = stringResource(R.string.grid_labels_section_label),
            )
            GridLabelsSection(grid = grid, viewModel = viewModel)
        }
    }
}
