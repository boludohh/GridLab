package com.gridlab.app.feature.editor.grid

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gridlab.app.R
import com.gridlab.app.core.model.ExportFormat
import com.gridlab.app.ui.theme.GridLabTheme

@Composable
internal fun EditorExportPanel(
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val requiresPermission = viewModel.requiresStoragePermission()
    var pendingExportFormat by remember { mutableStateOf<ExportFormat?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            pendingExportFormat?.let { format ->
                when (format) {
                    ExportFormat.Jpeg -> viewModel.exportJpeg()
                    ExportFormat.Png -> viewModel.exportPng()
                    ExportFormat.Pdf -> viewModel.exportPdf()
                }
            }
        }
        pendingExportFormat = null
    }

    fun requestExport(format: ExportFormat) {
        if (requiresPermission && !viewModel.hasStoragePermission()) {
            pendingExportFormat = format
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            when (format) {
                ExportFormat.Jpeg -> viewModel.exportJpeg()
                ExportFormat.Png -> viewModel.exportPng()
                ExportFormat.Pdf -> viewModel.exportPdf()
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
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

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_file_export_filled,
                title = stringResource(R.string.export_format_label),
            )
            ExportFormatSection(
                uiState = uiState,
                onExportClick = { format -> requestExport(format) },
            )
        }

        if (requiresPermission && !viewModel.hasStoragePermission()) {
            val colors = GridLabTheme.colors
            Text(
                text = stringResource(R.string.export_permission_required),
                style = MaterialTheme.typography.bodySmall,
                color = colors.warning,
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
    onExportClick: (ExportFormat) -> Unit,
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
        onClick = { onExportClick(ExportFormat.Jpeg) },
        enabled = exportEnabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.export_action_jpg))
    }
    OutlinedButton(
        onClick = { onExportClick(ExportFormat.Png) },
        enabled = exportEnabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.export_action_png))
    }
    OutlinedButton(
        onClick = { onExportClick(ExportFormat.Pdf) },
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
                    uiState.lastExportLocation.orEmpty(),
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
    }
}