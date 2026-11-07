package com.gridlab.app.feature.editor.grid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gridlab.app.core.model.Grid

/** Bottom panel with all primary/secondary grid and label controls. */
@Composable
internal fun EditorGridPanel(
    grid: Grid,
    viewModel: GridConfigViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GridSizeSection(grid = grid, viewModel = viewModel)
        GridVisibilitySection(grid = grid, viewModel = viewModel)
        GridStrokeSection(grid = grid, viewModel = viewModel)
        GridOpacitySection(grid = grid, viewModel = viewModel)
        GridColorSection(grid = grid, viewModel = viewModel)
        GridSecondarySection(grid = grid, viewModel = viewModel)
        if (grid.secondaryVisible) {
            GridSecondaryColorSection(grid = grid, viewModel = viewModel)
        }
        GridLabelsSection(grid = grid, viewModel = viewModel)
    }
}
