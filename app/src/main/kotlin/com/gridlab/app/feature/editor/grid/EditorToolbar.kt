package com.gridlab.app.feature.editor.grid

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gridlab.app.R

/** Tools available in the editor toolbar; each one opens its own bottom panel. */
enum class EditorTool {
    Paper,
    Grid,
    Image,
    Measure,
    Export,
}

/**
 * Row of toggleable tool icons between the paper preview and the config panel.
 * The selected tool expands into a pill with icon + short title (Studio-style);
 * tapping it again collapses the panel and hides the title.
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
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EditorToolButton(
            tool = EditorTool.Paper,
            iconRes = R.drawable.ic_article_filled,
            label = stringResource(R.string.editor_tool_paper_label),
            contentDescription = stringResource(R.string.editor_tool_paper_content_description),
            selected = selectedTool == EditorTool.Paper,
            onClick = onToolClick,
        )
        EditorToolButton(
            tool = EditorTool.Grid,
            iconRes = R.drawable.ic_grid_on_outline,
            label = stringResource(R.string.editor_tool_grid_label),
            contentDescription = stringResource(R.string.editor_tool_grid_content_description),
            selected = selectedTool == EditorTool.Grid,
            onClick = onToolClick,
        )
        EditorToolButton(
            tool = EditorTool.Image,
            iconRes = R.drawable.ic_image_filled,
            label = stringResource(R.string.editor_tool_image_label),
            contentDescription = stringResource(R.string.editor_tool_image_content_description),
            selected = selectedTool == EditorTool.Image,
            onClick = onToolClick,
        )
        EditorToolButton(
            tool = EditorTool.Measure,
            iconRes = R.drawable.ic_square_foot_filled,
            label = stringResource(R.string.editor_tool_measure_label),
            contentDescription = stringResource(R.string.editor_tool_measure_content_description),
            selected = selectedTool == EditorTool.Measure,
            onClick = onToolClick,
        )
        EditorToolButton(
            tool = EditorTool.Export,
            iconRes = R.drawable.ic_ios_share,
            label = stringResource(R.string.editor_tool_export_label),
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
    label: String,
    contentDescription: String,
    selected: Boolean,
    onClick: (EditorTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = if (selected) RoundedCornerShape(22.dp) else CircleShape
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .background(if (selected) colors.secondaryContainer else Color.Transparent)
            .clickable { onClick(tool) }
            .animateContentSize()
            .then(
                if (selected) {
                    Modifier.padding(horizontal = 12.dp)
                } else {
                    Modifier.size(44.dp)
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        if (selected) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}