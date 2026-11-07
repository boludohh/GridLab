package com.gridlab.app.feature.editor.grid

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.gridlab.app.R
import com.gridlab.app.core.geometry.GridCalculator
import com.gridlab.app.core.geometry.GridLabelFormatter
import com.gridlab.app.core.geometry.GridLayout
import com.gridlab.app.core.geometry.ImageFilterMatrices
import com.gridlab.app.core.geometry.ViewportTransform
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.ReferenceImage
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

/** Measure state + interaction callbacks for the paper preview (keeps DocumentPreview ≤ 8 params). */
internal data class PreviewInteraction(
    val measureMode: Boolean,
    val measurePoint1Mm: Pair<Double, Double>?,
    val measurePoint2Mm: Pair<Double, Double>?,
    val onPan: (deltaXMm: Double, deltaYMm: Double) -> Unit,
    val onScale: (factor: Double) -> Unit,
    val onMeasureTap: (xPx: Float, yPx: Float, viewport: ViewportTransform) -> Unit,
)

@Composable
fun GridConfigScreen(
    documentId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GridConfigViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.importImage(uri)
    }

    LaunchedEffect(documentId) {
        viewModel.openDocument(documentId)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.flushPendingSave()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    GridConfigContent(
        uiState = uiState,
        viewModel = viewModel,
        onBack = onBack,
        onImportClick = {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        modifier = modifier,
    )
}

/**
 * Editor shell: fixed top bar, always-visible paper preview, tool toolbar and
 * a fixed-height bottom panel whose content scrolls internally. This keeps the
 * preview on screen while the user adjusts any setting.
 */
@Composable
private fun GridConfigContent(
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
    onBack: () -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeTool by remember { mutableStateOf<EditorTool?>(null) }
    var lastTool by remember { mutableStateOf(EditorTool.Paper) }
    val interaction = PreviewInteraction(
        measureMode = uiState.measureMode,
        measurePoint1Mm = uiState.measurePoint1Mm,
        measurePoint2Mm = uiState.measurePoint2Mm,
        onPan = { dx, dy -> viewModel.panImage(dx, dy) },
        onScale = { factor -> viewModel.scaleImageBy(factor) },
        onMeasureTap = { x, y, vt -> viewModel.placeMeasurePoint(x, y, vt) },
    )

    Column(modifier = modifier.fillMaxSize()) {
        EditorTopBar(
            documentName = uiState.documentName,
            uiState = uiState,
            onBack = {
                viewModel.flushPendingSave()
                onBack()
            },
            onSave = viewModel::saveDocument,
        )
        DocumentPreview(
            paper = uiState.paper,
            grid = uiState.grid,
            imagePath = uiState.imageAbsolutePath,
            referenceImage = uiState.referenceImage,
            interaction = interaction,
            measureColor = Color(uiState.measureColorArgb.toInt()),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        EditorToolbar(
            selectedTool = activeTool,
            onToolClick = { tool ->
                lastTool = tool
                activeTool = if (activeTool == tool) null else tool
            },
        )
        AnimatedVisibility(
            visible = activeTool != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            EditorPanelHost(
                tool = activeTool ?: lastTool,
                uiState = uiState,
                viewModel = viewModel,
                onImportClick = onImportClick,
            )
        }
    }
}

@Composable
private fun EditorTopBar(
    documentName: String,
    uiState: GridConfigUiState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.surfaceVariant)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.projects_back),
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = documentName,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        GridSaveSection(
            uiState = uiState,
            onSave = onSave,
            modifier = Modifier,
        )
    }
}

/**
 * Fixed-height container for the active tool panel. Only its inner content
 * scrolls, so the panel keeps its size and shape while the preview stays put.
 */
@Composable
private fun EditorPanelHost(
    tool: EditorTool,
    uiState: GridConfigUiState,
    viewModel: GridConfigViewModel,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.45f)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        key(tool) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (tool) {
                    EditorTool.Paper -> EditorPaperPanel(
                        paper = uiState.paper,
                        viewModel = viewModel,
                    )
                    EditorTool.Grid -> EditorGridPanel(
                        grid = uiState.grid,
                        viewModel = viewModel,
                    )
                    EditorTool.Image -> EditorImagePanel(
                        availability = uiState.imageAvailability,
                        isImporting = uiState.isImporting,
                        importError = uiState.importError,
                        image = uiState.referenceImage,
                        onImportClick = onImportClick,
                        onRemoveClick = { viewModel.removeReferenceImage() },
                        viewModel = viewModel,
                    )
                    EditorTool.Measure -> EditorMeasurePanel(
                        uiState = uiState,
                        viewModel = viewModel,
                    )
                    EditorTool.Export -> EditorExportPanel(
                        uiState = uiState,
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}

