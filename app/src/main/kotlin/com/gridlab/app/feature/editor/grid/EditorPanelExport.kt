package com.gridlab.app.feature.editor.grid

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gridlab.app.R
import com.gridlab.app.ui.theme.GridLabTheme

/**
 * Bottom panel for export. Toolbar already shows "Export"; this panel only uses
 * short block subtitles with leading icons (Resolution, Format).
 */
@Composable
internal fun EditorExportPanel(
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Block 1: Resolution (DPI selection + safety info)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_high_quality_filled,
                title = stringResource(R.string.export_resolution_label),
            )
            ExportResolutionSection(
                uiState = uiState,
                viewModel = viewModel,
            )
        }

        // Block 2: Format (JPG/PNG/PDF actions + status)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_file_export_filled,
                title = stringResource(R.string.export_format_label),
            )
            ExportFormatSection(
                uiState = uiState,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun ExportResolutionSection(
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
) {
    val colors = GridLabTheme.colors
    Text(
        text = stringResource(R.string.export_max_safe_dpi, uiState.maxSafeExportDpi),
        style = MaterialTheme.typography.bodySmall,
        color = colors.textTertiary,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (dpi in listOf(72, 100, 150, 200, 300)) {
            val safe = dpi <= uiState.maxSafeExportDpi
            FilterChip(
                selected = uiState.selectedExportDpi == dpi,
                onClick = { viewModel.setExportDpi(dpi) },
                enabled = !uiState.isExporting,
                label = {
                    Text(
                        text = if (safe) {
                            stringResource(R.string.export_dpi_label, dpi)
                        } else {
                            stringResource(R.string.export_dpi_label_unsafe, dpi)
                        },
                    )
                },
            )
        }
    }
    if (uiState.exportMessage == "DPI_TOO_HIGH") {
        Text(
            text = stringResource(
                R.string.export_dpi_too_high,
                uiState.selectedExportDpi,
                uiState.maxSafeExportDpi,
            ),
            color = colors.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedButton(
            onClick = {
                viewModel.setExportDpi(uiState.maxSafeExportDpi)
                viewModel.exportJpeg()
            },
            enabled = !uiState.isExporting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(
                    R.string.export_use_safe_dpi,
                    uiState.maxSafeExportDpi,
                ),
            )
        }
    }
}

@Composable
private fun ExportFormatSection(
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
) {
    val colors = GridLabTheme.colors
    val exportEnabled = !uiState.isExporting && uiState.documentId.isNotEmpty()
    if (uiState.isExporting) {
        Text(
            text = stringResource(R.string.export_in_progress),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
    }
    OutlinedButton(
        onClick = { viewModel.exportJpeg() },
        enabled = exportEnabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.export_action_jpg))
    }
    OutlinedButton(
        onClick = { viewModel.exportPng() },
        enabled = exportEnabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.export_action_png))
    }
    OutlinedButton(
        onClick = { viewModel.exportPdf() },
        enabled = exportEnabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.export_action_pdf))
    }
    when (uiState.exportMessage) {
        "OK" -> {
            Text(
                text = stringResource(
                    R.string.export_success,
                    uiState.lastExportPath.orEmpty(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = colors.success,
            )
        }
        "FAILED" -> {
            Text(
                text = stringResource(R.string.export_failed),
                color = colors.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        // "DPI_TOO_HIGH" is handled inside ExportResolutionSection next to the chips.
    }
}