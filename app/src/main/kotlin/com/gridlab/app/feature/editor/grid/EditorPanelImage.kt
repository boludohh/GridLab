package com.gridlab.app.feature.editor.grid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gridlab.app.R
import com.gridlab.app.core.model.ImageAvailability
import com.gridlab.app.core.model.ImageFilter
import com.gridlab.app.core.model.ReferenceImage
import com.gridlab.app.ui.theme.GridLabTheme
import java.util.Locale

/**
 * Bottom panel for image settings. Toolbar already shows "Image"; this panel
 * only uses short block subtitles with leading icons (Source, Visibility, …).
 */
@Composable
internal fun EditorImagePanel(
    availability: ImageAvailability,
    isImporting: Boolean,
    importError: Boolean,
    image: ReferenceImage?,
    onImportClick: () -> Unit,
    onRemoveClick: () -> Unit,
    viewModel: GridConfigViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Block 1: Source (import/replace/remove)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorSectionHeader(
                iconRes = R.drawable.ic_add_photo_alternate_filled,
                title = stringResource(R.string.image_source_label),
            )
            ImageSourceSection(
                availability = availability,
                isImporting = isImporting,
                importError = importError,
                onImportClick = onImportClick,
                onRemoveClick = onRemoveClick,
            )
        }

        if (availability == ImageAvailability.Present && image != null) {
            // Block 2: Visibility
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EditorSectionHeader(
                    iconRes = R.drawable.ic_visibility_filled,
                    title = stringResource(R.string.image_visibility_label),
                )
                ImageVisibilitySection(image = image, viewModel = viewModel)
            }

            // Block 3: Lock
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EditorSectionHeader(
                    iconRes = R.drawable.ic_lock_filled,
                    title = stringResource(R.string.image_lock_label),
                )
                ImageLockSection(image = image, viewModel = viewModel)
            }

            // Block 4: Opacity
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EditorSectionHeader(
                    iconRes = R.drawable.ic_opacity,
                    title = stringResource(R.string.image_opacity_section_label),
                )
                ImageOpacitySection(image = image, viewModel = viewModel)
            }

            // Block 5: Rotation
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EditorSectionHeader(
                    iconRes = R.drawable.ic_rotate_right,
                    title = stringResource(R.string.image_rotation_label),
                )
                ImageRotationSection(image = image, viewModel = viewModel)
            }

            // Block 6: Position (hint + reset)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EditorSectionHeader(
                    iconRes = R.drawable.ic_open_with,
                    title = stringResource(R.string.image_position_label),
                )
                ImagePositionSection(image = image, viewModel = viewModel)
            }

            // Block 7: Crop
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EditorSectionHeader(
                    iconRes = R.drawable.ic_crop,
                    title = stringResource(R.string.image_crop_label),
                )
                ImageCropSection(image = image, viewModel = viewModel)
            }

            // Block 8: Filter
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EditorSectionHeader(
                    iconRes = R.drawable.ic_tune,
                    title = stringResource(R.string.image_filter_section_label),
                )
                ImageFilterSection(image = image, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun ImageSourceSection(
    availability: ImageAvailability,
    isImporting: Boolean,
    importError: Boolean,
    onImportClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    val colors = GridLabTheme.colors
    when (availability) {
        ImageAvailability.Missing -> {
            Text(
                text = stringResource(R.string.image_missing_message),
                color = colors.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        ImageAvailability.Corrupt -> {
            Text(
                text = stringResource(R.string.image_corrupt_message),
                color = colors.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        else -> Unit
    }
    OutlinedButton(
        onClick = onImportClick,
        enabled = !isImporting,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = when {
                isImporting -> stringResource(R.string.image_importing)
                availability == ImageAvailability.Present -> stringResource(R.string.image_replace)
                availability == ImageAvailability.Missing ||
                    availability == ImageAvailability.Corrupt -> stringResource(R.string.image_relink)
                else -> stringResource(R.string.image_import)
            },
        )
    }
    if (availability != ImageAvailability.None) {
        OutlinedButton(
            onClick = onRemoveClick,
            enabled = !isImporting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.image_remove_reference))
        }
    }
    if (importError) {
        Text(
            text = stringResource(R.string.image_import_error),
            color = colors.error,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ImageVisibilitySection(
    image: ReferenceImage,
    viewModel: GridConfigViewModel,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = image.visible,
            onClick = { viewModel.setImageVisible(!image.visible) },
            label = { Text(stringResource(R.string.image_visible_on)) },
        )
        FilterChip(
            selected = !image.visible,
            onClick = { viewModel.setImageVisible(!image.visible) },
            label = { Text(stringResource(R.string.image_visible_off)) },
        )
    }
}

@Composable
private fun ImageLockSection(
    image: ReferenceImage,
    viewModel: GridConfigViewModel,
) {
    val colors = GridLabTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = image.locked,
            onClick = { viewModel.setImageLocked(!image.locked) },
            label = { Text(stringResource(R.string.image_locked_on)) },
        )
        FilterChip(
            selected = !image.locked,
            onClick = { viewModel.setImageLocked(!image.locked) },
            label = { Text(stringResource(R.string.image_locked_off)) },
        )
    }
    Text(
        text = stringResource(R.string.image_lock_hint),
        style = MaterialTheme.typography.bodySmall,
        color = colors.textTertiary,
    )
}

@Composable
private fun ImageOpacitySection(
    image: ReferenceImage,
    viewModel: GridConfigViewModel,
) {
    Slider(
        value = image.opacity,
        onValueChange = { viewModel.setImageOpacity(it) },
        valueRange = 0.05f..1f,
        enabled = !image.locked,
    )
    Text(
        text = stringResource(R.string.image_opacity_value, (image.opacity * 100).toInt()),
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun ImageRotationSection(
    image: ReferenceImage,
    viewModel: GridConfigViewModel,
) {
    Slider(
        value = image.rotationDegrees,
        onValueChange = { viewModel.setImageRotation(it) },
        valueRange = 0f..360f,
        enabled = !image.locked,
    )
    Text(
        text = stringResource(
            R.string.image_rotation_value,
            String.format(Locale.US, "%.0f", image.rotationDegrees),
        ),
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun ImagePositionSection(
    image: ReferenceImage,
    viewModel: GridConfigViewModel,
) {
    val colors = GridLabTheme.colors
    Text(
        text = stringResource(R.string.image_transform_hint),
        style = MaterialTheme.typography.bodySmall,
        color = colors.textTertiary,
    )
    OutlinedButton(
        onClick = { viewModel.resetImageTransform() },
        enabled = !image.locked,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.image_reset_transform))
    }
}

@Composable
private fun ImageCropSection(
    image: ReferenceImage,
    viewModel: GridConfigViewModel,
) {
    val colors = GridLabTheme.colors
    Text(
        text = stringResource(R.string.crop_hint),
        style = MaterialTheme.typography.bodySmall,
        color = colors.textTertiary,
    )
    if (image.hasCrop) {
        Text(
            text = stringResource(R.string.crop_active),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.primary,
        )
    }
    CropEdgeSlider(
        label = stringResource(R.string.crop_left),
        value = image.cropLeft,
        valueRange = 0f..(image.cropRight - 0.05f).coerceAtLeast(0f),
        enabled = !image.locked,
        onValueChange = { left ->
            viewModel.setCrop(left, image.cropTop, image.cropRight, image.cropBottom)
        },
    )
    CropEdgeSlider(
        label = stringResource(R.string.crop_top),
        value = image.cropTop,
        valueRange = 0f..(image.cropBottom - 0.05f).coerceAtLeast(0f),
        enabled = !image.locked,
        onValueChange = { top ->
            viewModel.setCrop(image.cropLeft, top, image.cropRight, image.cropBottom)
        },
    )
    CropEdgeSlider(
        label = stringResource(R.string.crop_right),
        value = image.cropRight,
        valueRange = (image.cropLeft + 0.05f).coerceAtMost(1f)..1f,
        enabled = !image.locked,
        onValueChange = { right ->
            viewModel.setCrop(image.cropLeft, image.cropTop, right, image.cropBottom)
        },
    )
    CropEdgeSlider(
        label = stringResource(R.string.crop_bottom),
        value = image.cropBottom,
        valueRange = (image.cropTop + 0.05f).coerceAtMost(1f)..1f,
        enabled = !image.locked,
        onValueChange = { bottom ->
            viewModel.setCrop(image.cropLeft, image.cropTop, image.cropRight, bottom)
        },
    )
    OutlinedButton(
        onClick = { viewModel.clearCrop() },
        enabled = !image.locked && image.hasCrop,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.crop_clear))
    }
}

@Composable
private fun CropEdgeSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium)
    Slider(
        value = value.coerceIn(valueRange.start, valueRange.endInclusive),
        onValueChange = onValueChange,
        valueRange = if (valueRange.endInclusive > valueRange.start) valueRange else 0f..1f,
        enabled = enabled,
    )
}

@Composable
private fun ImageFilterSection(
    image: ReferenceImage,
    viewModel: GridConfigViewModel,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = image.filter == ImageFilter.None,
            onClick = { viewModel.setImageFilter(ImageFilter.None) },
            enabled = !image.locked,
            label = { Text(stringResource(R.string.image_filter_none)) },
        )
        FilterChip(
            selected = image.filter == ImageFilter.Grayscale,
            onClick = { viewModel.setImageFilter(ImageFilter.Grayscale) },
            enabled = !image.locked,
            label = { Text(stringResource(R.string.image_filter_grayscale)) },
        )
        FilterChip(
            selected = image.filter == ImageFilter.HighContrast,
            onClick = { viewModel.setImageFilter(ImageFilter.HighContrast) },
            enabled = !image.locked,
            label = { Text(stringResource(R.string.image_filter_high_contrast)) },
        )
    }
}
