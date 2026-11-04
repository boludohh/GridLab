package com.gridlab.app.feature.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gridlab.app.R
import com.gridlab.app.core.model.Document
import kotlinx.coroutines.delay

private const val TAGLINE_INTERVAL_MS: Long = 2500L

@Composable
fun MainScreen(
    onOpenProject: (documentId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var renameTarget by remember { mutableStateOf<Document?>(null) }
    var deleteTarget by remember { mutableStateOf<Document?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        HomeHero()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.createProject(onOpenProject) },
                enabled = !uiState.isWorking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add_box_filled),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.projects_create))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.projects_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
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
                    items(uiState.projects, key = { it.id }) { project ->
                        ProjectRow(
                            project = project,
                            enabled = !uiState.isWorking,
                            onOpen = { onOpenProject(project.id) },
                            onRename = { renameTarget = project },
                            onDelete = { deleteTarget = project },
                        )
                    }
                }
            }
        }
    }

    renameTarget?.let { target ->
        RenameDialog(
            initialName = target.name,
            onDismiss = { renameTarget = null },
            onConfirm = { newName ->
                viewModel.renameProject(target.id, newName)
                renameTarget = null
            },
        )
    }

    deleteTarget?.let { target ->
        DeleteDialog(
            projectName = target.name,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deleteProject(target.id)
                deleteTarget = null
            },
        )
    }
}

@Composable
private fun HomeHero(modifier: Modifier = Modifier) {
    val taglines = stringArrayResource(R.array.home_taglines)
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(taglines) {
        if (taglines.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(TAGLINE_INTERVAL_MS)
            index = (index + 1) % taglines.size
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(R.drawable.gridlab_banner_goku),
            contentDescription = stringResource(R.string.home_banner_content_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (taglines.isNotEmpty()) {
            AnimatedContent(
                targetState = index,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "homeTagline",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) { i ->
                Text(
                    text = taglines[i],
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ProjectRow(
    project: Document,
    enabled: Boolean,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onOpen)
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = project.name,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.projects_id_hint, project.id.take(8)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp),
        ) {
            OutlinedButton(onClick = onRename, enabled = enabled) {
                Text(text = stringResource(R.string.projects_rename))
            }
            OutlinedButton(onClick = onDelete, enabled = enabled) {
                Text(text = stringResource(R.string.projects_delete))
            }
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
private fun DeleteDialog(
    projectName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.projects_delete_title)) },
        text = {
            Text(text = stringResource(R.string.projects_delete_message, projectName))
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
