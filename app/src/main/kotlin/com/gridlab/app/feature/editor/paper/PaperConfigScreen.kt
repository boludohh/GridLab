package com.gridlab.app.feature.editor.paper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gridlab.app.R
import com.gridlab.app.core.geometry.UnitConverter
import com.gridlab.app.core.model.LengthUnit
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.PaperOrientation
import com.gridlab.app.core.model.StandardPaperFormat
import java.util.Locale

@Composable
fun PaperConfigScreen(
    modifier: Modifier = Modifier,
    viewModel: PaperConfigViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    PaperConfigContent(
        uiState = uiState,
        viewModel = viewModel,
        modifier = modifier,
    )
}

@Composable
private fun PaperConfigContent(
    uiState: PaperConfigUiState,
    viewModel: PaperConfigViewModel,
    modifier: Modifier = Modifier,
) {
    val paper = uiState.paper

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.paper_config_title),
            style = MaterialTheme.typography.headlineSmall,
        )

        FormatSection(selected = paper.format, onSelect = viewModel::setFormat)
        OrientationSection(selected = paper.orientation, onSelect = viewModel::setOrientation)
        UnitSection(selected = paper.displayUnit, onSelect = viewModel::setDisplayUnit)

        if (paper.format == StandardPaperFormat.Custom) {
            CustomSizeSection(
                paper = paper,
                onWidthChange = viewModel::setCustomWidth,
                onHeightChange = viewModel::setCustomHeight,
            )
        }

        MarginsSection(
            paper = paper,
            onLeft = viewModel::setMarginLeft,
            onTop = viewModel::setMarginTop,
            onRight = viewModel::setMarginRight,
            onBottom = viewModel::setMarginBottom,
        )

        PrintableAreaSummary(paper = paper)

        Button(
            onClick = viewModel::saveDocument,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = if (uiState.isSaving) {
                    stringResource(R.string.paper_saving)
                } else {
                    stringResource(R.string.paper_save)
                },
            )
        }

        when (uiState.lastSaveSucceeded) {
            true -> Text(
                text = stringResource(R.string.paper_save_success),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
            false -> Text(
                text = stringResource(R.string.paper_save_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            null -> Unit
        }
    }
}

@Composable
private fun FormatSection(
    selected: StandardPaperFormat,
    onSelect: (StandardPaperFormat) -> Unit,
) {
    Text(text = stringResource(R.string.paper_format_label), style = MaterialTheme.typography.titleMedium)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StandardPaperFormat.entries.chunked(4).forEach { chunk ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                chunk.forEach { format ->
                    FilterChip(
                        selected = selected == format,
                        onClick = { onSelect(format) },
                        label = { Text(format.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun OrientationSection(
    selected: PaperOrientation,
    onSelect: (PaperOrientation) -> Unit,
) {
    Text(text = stringResource(R.string.paper_orientation_label), style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == PaperOrientation.Portrait,
            onClick = { onSelect(PaperOrientation.Portrait) },
            label = { Text(stringResource(R.string.paper_orientation_portrait)) },
        )
        FilterChip(
            selected = selected == PaperOrientation.Landscape,
            onClick = { onSelect(PaperOrientation.Landscape) },
            label = { Text(stringResource(R.string.paper_orientation_landscape)) },
        )
    }
}

@Composable
private fun UnitSection(
    selected: LengthUnit,
    onSelect: (LengthUnit) -> Unit,
) {
    Text(text = stringResource(R.string.paper_unit_label), style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == LengthUnit.Millimeter,
            onClick = { onSelect(LengthUnit.Millimeter) },
            label = { Text(stringResource(R.string.unit_mm)) },
        )
        FilterChip(
            selected = selected == LengthUnit.Centimeter,
            onClick = { onSelect(LengthUnit.Centimeter) },
            label = { Text(stringResource(R.string.unit_cm)) },
        )
        FilterChip(
            selected = selected == LengthUnit.Inch,
            onClick = { onSelect(LengthUnit.Inch) },
            label = { Text(stringResource(R.string.unit_in)) },
        )
    }
}

@Composable
private fun CustomSizeSection(
    paper: Paper,
    onWidthChange: (Double) -> Unit,
    onHeightChange: (Double) -> Unit,
) {
    Text(text = stringResource(R.string.paper_custom_size_label), style = MaterialTheme.typography.titleMedium)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NumericField(
            label = stringResource(R.string.paper_width_label),
            value = UnitConverter.fromMm(paper.widthMm, paper.displayUnit),
            onValueChange = onWidthChange,
            modifier = Modifier.weight(1f),
        )
        NumericField(
            label = stringResource(R.string.paper_height_label),
            value = UnitConverter.fromMm(paper.heightMm, paper.displayUnit),
            onValueChange = onHeightChange,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MarginsSection(
    paper: Paper,
    onLeft: (Double) -> Unit,
    onTop: (Double) -> Unit,
    onRight: (Double) -> Unit,
    onBottom: (Double) -> Unit,
) {
    Text(text = stringResource(R.string.paper_margins_label), style = MaterialTheme.typography.titleMedium)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NumericField(
            label = stringResource(R.string.paper_margin_left),
            value = UnitConverter.fromMm(paper.marginLeftMm, paper.displayUnit),
            onValueChange = onLeft,
            modifier = Modifier.weight(1f),
        )
        NumericField(
            label = stringResource(R.string.paper_margin_right),
            value = UnitConverter.fromMm(paper.marginRightMm, paper.displayUnit),
            onValueChange = onRight,
            modifier = Modifier.weight(1f),
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NumericField(
            label = stringResource(R.string.paper_margin_top),
            value = UnitConverter.fromMm(paper.marginTopMm, paper.displayUnit),
            onValueChange = onTop,
            modifier = Modifier.weight(1f),
        )
        NumericField(
            label = stringResource(R.string.paper_margin_bottom),
            value = UnitConverter.fromMm(paper.marginBottomMm, paper.displayUnit),
            onValueChange = onBottom,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PrintableAreaSummary(paper: Paper) {
    val unit = paper.displayUnit
    val width = UnitConverter.fromMm(paper.printableWidthMm, unit)
    val height = UnitConverter.fromMm(paper.printableHeightMm, unit)
    val unitLabel = when (unit) {
        LengthUnit.Millimeter -> stringResource(R.string.unit_mm)
        LengthUnit.Centimeter -> stringResource(R.string.unit_cm)
        LengthUnit.Inch -> stringResource(R.string.unit_in)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(
            R.string.paper_printable_area,
            formatNumber(width),
            formatNumber(height),
            unitLabel,
        ),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(
        text = stringResource(
            R.string.paper_full_size,
            formatNumber(UnitConverter.fromMm(paper.widthMm, unit)),
            formatNumber(UnitConverter.fromMm(paper.heightMm, unit)),
            unitLabel,
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun NumericField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = formatNumber(value),
        onValueChange = { text ->
            text.toDoubleOrNull()?.let(onValueChange)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
    )
}

private fun formatNumber(value: Double): String =
    if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", value)
    }
