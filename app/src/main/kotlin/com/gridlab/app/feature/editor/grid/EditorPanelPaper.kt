package com.gridlab.app.feature.editor.grid

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gridlab.app.R
import com.gridlab.app.core.geometry.UnitConverter
import com.gridlab.app.core.model.LengthUnit
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.PaperOrientation
import com.gridlab.app.core.model.StandardPaperFormat
import com.gridlab.app.core.ui.ColorPickerField
import com.gridlab.app.ui.theme.GridLabTheme

/**
 * Bottom panel with paper settings. The toolbar already shows "Paper", so this
 * panel only uses short block subtitles with leading icons (Color, Format, …).
 */
@Composable
internal fun EditorPaperPanel(
    paper: Paper,
    viewModel: GridConfigViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ColorPickerField(
            label = stringResource(R.string.paper_color_label),
            colorArgb = paper.colorArgb,
            onColorChange = viewModel::setPaperColorArgb,
            iconRes = R.drawable.ic_palette_filled,
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_article_filled,
                title = stringResource(R.string.paper_format_label),
            )
            PaperFormatSection(
                selected = paper.format,
                onSelect = viewModel::setPaperFormat,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_screen_rotation_alt,
                title = stringResource(R.string.paper_orientation_label),
            )
            PaperOrientationSection(
                selected = paper.orientation,
                onSelect = viewModel::setPaperOrientation,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_straighten_filled,
                title = stringResource(R.string.paper_unit_label),
            )
            PaperUnitSection(
                selected = paper.displayUnit,
                onSelect = viewModel::setDisplayUnit,
            )
        }

        if (paper.format == StandardPaperFormat.Custom) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EditorSectionHeader(
                    iconRes = R.drawable.ic_aspect_ratio_filled,
                    title = stringResource(R.string.paper_custom_size_label),
                )
                PaperCustomSizeSection(
                    paper = paper,
                    onWidthChange = viewModel::setCustomWidth,
                    onHeightChange = viewModel::setCustomHeight,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_border_outer,
                title = stringResource(R.string.paper_margins_label),
            )
            PaperMarginsSection(
                paper = paper,
                onLeft = viewModel::setMarginLeft,
                onTop = viewModel::setMarginTop,
                onRight = viewModel::setMarginRight,
                onBottom = viewModel::setMarginBottom,
            )
        }

        PaperPrintableSummary(paper = paper)
    }
}

/** Shared block header: leading vector icon + short subtitle. */
@Composable
internal fun EditorSectionHeader(
    @DrawableRes iconRes: Int,
    title: String,
    modifier: Modifier = Modifier,
) {
    val colors = GridLabTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = colors.iconActive,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun PaperFormatSection(
    selected: StandardPaperFormat,
    onSelect: (StandardPaperFormat) -> Unit,
) {
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
                        label = { Text(text = format.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PaperOrientationSection(
    selected: PaperOrientation,
    onSelect: (PaperOrientation) -> Unit,
) {
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
private fun PaperUnitSection(
    selected: LengthUnit,
    onSelect: (LengthUnit) -> Unit,
) {
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
private fun PaperCustomSizeSection(
    paper: Paper,
    onWidthChange: (Double) -> Unit,
    onHeightChange: (Double) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EditorDecimalField(
            label = stringResource(R.string.paper_width_label),
            value = UnitConverter.fromMm(paper.widthMm, paper.displayUnit),
            onValueChange = onWidthChange,
            modifier = Modifier.weight(1f),
        )
        EditorDecimalField(
            label = stringResource(R.string.paper_height_label),
            value = UnitConverter.fromMm(paper.heightMm, paper.displayUnit),
            onValueChange = onHeightChange,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PaperMarginsSection(
    paper: Paper,
    onLeft: (Double) -> Unit,
    onTop: (Double) -> Unit,
    onRight: (Double) -> Unit,
    onBottom: (Double) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EditorDecimalField(
                label = stringResource(R.string.paper_margin_left),
                value = UnitConverter.fromMm(paper.marginLeftMm, paper.displayUnit),
                onValueChange = onLeft,
                modifier = Modifier.weight(1f),
            )
            EditorDecimalField(
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
            EditorDecimalField(
                label = stringResource(R.string.paper_margin_top),
                value = UnitConverter.fromMm(paper.marginTopMm, paper.displayUnit),
                onValueChange = onTop,
                modifier = Modifier.weight(1f),
            )
            EditorDecimalField(
                label = stringResource(R.string.paper_margin_bottom),
                value = UnitConverter.fromMm(paper.marginBottomMm, paper.displayUnit),
                onValueChange = onBottom,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PaperPrintableSummary(paper: Paper) {
    val colors = GridLabTheme.colors
    val unit = paper.displayUnit
    val width = UnitConverter.fromMm(paper.printableWidthMm, unit)
    val height = UnitConverter.fromMm(paper.printableHeightMm, unit)
    val unitLabel = when (unit) {
        LengthUnit.Millimeter -> stringResource(R.string.unit_mm)
        LengthUnit.Centimeter -> stringResource(R.string.unit_cm)
        LengthUnit.Inch -> stringResource(R.string.unit_in)
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(
                R.string.paper_printable_area,
                formatDecimalForDisplay(width),
                formatDecimalForDisplay(height),
                unitLabel,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
        Text(
            text = stringResource(
                R.string.paper_full_size,
                formatDecimalForDisplay(UnitConverter.fromMm(paper.widthMm, unit)),
                formatDecimalForDisplay(UnitConverter.fromMm(paper.heightMm, unit)),
                unitLabel,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textTertiary,
        )
    }
}
