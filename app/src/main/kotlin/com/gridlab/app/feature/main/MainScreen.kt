package com.gridlab.app.feature.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.gridlab.app.R
import com.gridlab.app.ui.theme.GridLabTheme
import java.io.File
import kotlinx.coroutines.delay

private const val TAGLINE_INTERVAL_MS: Long = 2500L

@Composable
fun MainScreen(
    onOpenProject: (documentId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = GridLabTheme.colors
    var renameTarget by remember { mutableStateOf<ProjectRowUi?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        HomeTopBar()
        HomeHeroSection()
        HomeNewProjectButton(
            enabled = !uiState.isWorking && !uiState.isSelectionMode,
            onClick = { viewModel.createProject(onOpenProject) },
        )
        ProjectsSectionHeader(
            selectionMode = uiState.isSelectionMode,
            enabled = !uiState.isWorking,
            onDeleteClick = {
                val shouldConfirm = viewModel.onDeleteHeaderClick()
                if (shouldConfirm) {
                    showDeleteConfirm = true
                }
            },
        )
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = colors.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.actionBg)
                }
            }
            uiState.projects.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.projects_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.projects, key = { it.document.id }) { row ->
                        ProjectRow(
                            row = row,
                            selectionMode = uiState.isSelectionMode,
                            selected = row.document.id in uiState.selectedIds,
                            enabled = !uiState.isWorking,
                            onOpen = { onOpenProject(row.document.id) },
                            onToggleSelect = { viewModel.toggleProjectSelection(row.document.id) },
                            onEdit = { renameTarget = row },
                        )
                    }
                }
            }
        }
    }

    renameTarget?.let { target ->
        RenameDialog(
            initialName = target.document.name,
            onDismiss = { renameTarget = null },
            onConfirm = { newName ->
                viewModel.renameProject(target.document.id, newName)
                renameTarget = null
            },
        )
    }

    if (showDeleteConfirm) {
        val count = uiState.selectedIds.size
        DeleteProjectsDialog(
            count = count,
            onDismiss = {
                showDeleteConfirm = false
                viewModel.exitSelectionMode()
            },
            onConfirm = {
                showDeleteConfirm = false
                viewModel.confirmDeleteSelected()
            },
        )
    }
}

@Composable
private fun HomeTopBar(modifier: Modifier = Modifier) {
    val colors = GridLabTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.gridlab_icon),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.e2)
                .clickable(onClick = { /* settings later */ }),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_settings_filled),
                contentDescription = stringResource(R.string.home_settings_content_description),
                tint = colors.iconActive,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun HomeHeroSection(modifier: Modifier = Modifier) {
    val colors = GridLabTheme.colors
    val taglines = stringArrayResource(R.array.home_taglines)
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(taglines) {
        if (taglines.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(TAGLINE_INTERVAL_MS)
            index = (index + 1) % taglines.size
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.gridlab_banner_goku),
            contentDescription = stringResource(R.string.home_banner_content_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(20.dp)),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.e1)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.actionBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_brush_filled),
                    contentDescription = null,
                    tint = colors.actionOn,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (taglines.isNotEmpty()) {
                    AnimatedContent(
                        targetState = index,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "homeTagline",
                    ) { i ->
                        Text(
                            text = taglines[i],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.home_tagline_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeNewProjectButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GridLabTheme.colors
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.actionBg,
            contentColor = colors.actionOn,
            disabledContainerColor = colors.actionDisabledBg,
            disabledContentColor = colors.actionDisabledOn,
        ),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.projects_create),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun ProjectsSectionHeader(
    selectionMode: Boolean,
    enabled: Boolean,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GridLabTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_photo_library_filled),
            contentDescription = null,
            tint = colors.iconActive,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.projects_title),
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = onDeleteClick,
            enabled = enabled,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_delete_filled),
                contentDescription = stringResource(R.string.projects_delete_mode_content_description),
                tint = if (selectionMode) colors.error else colors.iconActive,
            )
        }
    }
}

@Composable
private fun ProjectRow(
    row: ProjectRowUi,
    selectionMode: Boolean,
    selected: Boolean,
    enabled: Boolean,
    onOpen: () -> Unit,
    onToggleSelect: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GridLabTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .clickable(enabled = enabled) {
                if (selectionMode) {
                    onToggleSelect()
                } else {
                    onOpen()
                }
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProjectThumbnail(
            absolutePath = row.thumbnailAbsolutePath,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = row.document.name,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (selectionMode) {
            Icon(
                painter = painterResource(
                    if (selected) {
                        R.drawable.ic_radio_button_checked
                    } else {
                        R.drawable.ic_radio_button_unchecked
                    },
                ),
                contentDescription = null,
                tint = if (selected) colors.actionBg else colors.iconInactive,
                modifier = Modifier.size(24.dp),
            )
        } else {
            IconButton(
                onClick = onEdit,
                enabled = enabled,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit_filled),
                    contentDescription = stringResource(R.string.projects_rename),
                    tint = colors.iconActive,
                )
            }
        }
    }
}

@Composable
private fun ProjectThumbnail(
    absolutePath: String?,
    modifier: Modifier = Modifier,
) {
    val colors = GridLabTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.e2),
        contentAlignment = Alignment.Center,
    ) {
        if (absolutePath != null) {
            AsyncImage(
                model = File(absolutePath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_photo_library_filled),
                contentDescription = null,
                tint = colors.iconInactive,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun RenameDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.projects_rename_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text(stringResource(R.string.projects_name_label)) },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.trim().isNotEmpty(),
            ) {
                Text(text = stringResource(R.string.projects_rename_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.projects_cancel))
            }
        },
    )
}

@Composable
private fun DeleteProjectsDialog(
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.projects_delete_multi_title)) },
        text = {
            Text(
                text = stringResource(R.string.projects_delete_multi_message, count),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.projects_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.projects_cancel))
            }
        },
    )
}
