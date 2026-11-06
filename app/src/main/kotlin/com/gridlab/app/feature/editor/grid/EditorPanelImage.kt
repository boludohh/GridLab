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
import com.gridlab.app.core.model.ReferenceImage
import java.util.Locale

/** Bottom panel with image import, transform, crop and filter controls. */
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ImageImportSection(
            availability = availability,
            isImporting = isImporting,
            importError = importError,
            onImportClick = onImportClick,
            onRemoveClick = onRemoveClick,
        )
        if (availability == ImageAvailability.Present && image != null) {
            ImageTransformSection(image = image, viewModel = viewModel)
            CropSection(image = image, viewModel = viewModel)
            ImageFilterSection(image = image, viewModel = viewModel)
        }
    }
}

@Composable
private fun ImageImportSection(
    availability: ImageAvailability,
    isImporting: Boolean,
    importError: Boolean,
    onImportClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Text(
        text = stringResource(R.string.image_section_title),
        style = MaterialTheme.typography.titleMedium,
    )
    when (availability) {
        ImageAvailability.Missing -> {
            Text(
                text = stringResource(R.string.image_missing_message),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        ImageAvailability.Corrupt -> {
            Text(
                text = stringResource(R.string.image_corrupt_message),
                color = MaterialTheme.colorScheme.error,
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
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ImageTransformSection(
    image: ReferenceImage,
    viewModel: GridConfigViewModel,
) {
    Text(
        text = stringResource(R.string.image_transform_title),
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = stringResource(R.string.image_transform_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    ImageVisibilityLockChips(image = image, viewModel = viewModel)
    ImageOpacityControls(image = image, viewModel = viewModel)
    ImageRotationControls(image = image, viewModel = viewModel)
}

@Composable
private fun ImageVisibilityLockChips(
    image: ReferenceImage,
    viewModel: GridConfigViewModel,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = image.visible,
            onClick = { viewModel.setImageVisible(!image.visible) },
            label = {
                Text(
                    text = if (image.visible) {
                        stringResource(R.string.image_visible_on)
                    } else {
                        stringResource(R.string.image_visible_off)
                    },
                )
            },
        )
        FilterChip(
            selected = image.locked,
            onClick = { viewModel.setImageLocked(!image.locked) },
            label = {
                Text(
                    text = if (image.locked) {
                        stringResource(R.string.image_locked_on)
                    } else {
                        stringResource(R.string.image_locked_off)
                    },
                )
            },
        )
    }
}

@Composable
private fun ImageOpacityControls(
    image: ReferenceImage,
    viewModel: GridConfigViewModel,
) {
    Text(
        text = stringResource(R.string.image_opacity_label),
        style = MaterialTheme.typography.bodyMedium,
    )
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
    OutlinedButton(
        onClick = { viewModel.resetImageTransform() },
        enabled = !image.locked,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.image_reset_transform))
    }
}

@Composable
private fun ImageRotationControls(
    image: ReferenceImage,
    viewModel: GridConfigViewModel,
) {
    Text(
        text = stringResource(R.string.image_rotation_label),
        style = MaterialTheme.typography.bodyMedium,
    )
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