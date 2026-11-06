package com.gridlab.app.feature.editor.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gridlab.app.R

/** Tools available in the editor toolbar; each one opens its own bottom panel. */
enum class EditorTool {
    Grid,
    Image,
    Measure,
    Export,
}

/**
 * Row of toggleable tool icons displayed between the paper preview and the
 * configuration panel. Tapping the selected tool closes the panel.
 */
@Composable
fun EditorToolbar(
    selectedTool: EditorTool?,
    onToolClick: (EditorTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EditorToolButton(
            tool = EditorTool.Grid,
            iconRes = R.drawable.ic_grid_on_outline,
            contentDescription = stringResource(R.string.editor_tool_grid_content_description),
            selected = selectedTool == EditorTool.Grid,
            onClick = onToolClick,
        )
        EditorToolButton(
            tool = EditorTool.Image,
            iconRes = R.drawable.ic_image_filled,
            contentDescription = stringResource(R.string.editor_tool_image_content_description),
            selected = selectedTool == EditorTool.Image,
            onClick = onToolClick,
        )
        EditorToolButton(
            tool = EditorTool.Measure,
            iconRes = R.drawable.ic_square_foot_filled,
            contentDescription = stringResource(R.string.editor_tool_measure_content_description),
            selected = selectedTool == EditorTool.Measure,
            onClick = onToolClick,
        )
        EditorToolButton(
            tool = EditorTool.Export,
            iconRes = R.drawable.ic_ios_share,
            contentDescription = stringResource(R.string.editor_tool_export_content_description),
            selected = selectedTool == EditorTool.Export,
            onClick = onToolClick,
        )
    }
}

@Composable
private fun EditorToolButton(
    tool: EditorTool,
    iconRes: Int,
    contentDescription: String,
    selected: Boolean,
    onClick: (EditorTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (selected) colors.secondaryContainer else Color.Transparent)
            .clickable { onClick(tool) },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
    }
}