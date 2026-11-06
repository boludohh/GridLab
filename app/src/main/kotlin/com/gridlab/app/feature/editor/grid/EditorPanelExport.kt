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

/** Bottom panel with DPI selection and the JPG/PNG/PDF export actions. */
@Composable
internal fun EditorExportPanel(
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.export_section_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.export_max_safe_dpi, uiState.maxSafeExportDpi),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        val exportEnabled = !uiState.isExporting && uiState.documentId.isNotEmpty()
        if (uiState.isExporting) {
            Text(
                text = stringResource(R.string.export_in_progress),
                style = MaterialTheme.typography.bodyMedium,
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
                )
            }
            "DPI_TOO_HIGH" -> {
                Text(
                    text = stringResource(
                        R.string.export_dpi_too_high,
                        uiState.selectedExportDpi,
                        uiState.maxSafeExportDpi,
                    ),
                    color = MaterialTheme.colorScheme.error,
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
            "FAILED" -> {
                Text(
                    text = stringResource(R.string.export_failed),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}