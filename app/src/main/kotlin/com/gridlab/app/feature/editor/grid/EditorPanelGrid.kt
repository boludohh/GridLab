package com.gridlab.app.feature.editor.grid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gridlab.app.R
import com.gridlab.app.core.model.Grid

/** Bottom panel grouping every primary and secondary grid setting. */
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
        Text(
            text = stringResource(R.string.grid_config_title),
            style = MaterialTheme.typography.titleMedium,
        )
        GridSizeSection(grid = grid, viewModel = viewModel)
        GridVisibilitySection(grid = grid, viewModel = viewModel)
        GridSecondarySection(grid = grid, viewModel = viewModel)
        GridLabelsSection(grid = grid, viewModel = viewModel)
        GridStrokeSection(grid = grid, viewModel = viewModel)
        GridOpacitySection(grid = grid, viewModel = viewModel)
        GridColorSection(grid = grid, viewModel = viewModel)
    }
}