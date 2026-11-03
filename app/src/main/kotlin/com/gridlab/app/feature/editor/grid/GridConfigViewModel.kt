package com.gridlab.app.feature.editor.grid

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gridlab.app.core.data.repository.ProjectRepository
import com.gridlab.app.core.data.storage.ImageStorage
import com.gridlab.app.core.geometry.ExportSizeCalculator
import com.gridlab.app.core.render.DocumentRenderer
import com.gridlab.app.core.geometry.ImageTransformCalculator
import com.gridlab.app.core.geometry.UnitConverter
import com.gridlab.app.core.geometry.ViewportTransform
import com.gridlab.app.core.model.Document
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.ImageAvailability
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.ReferenceImage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class GridConfigUiState(
    val documentId: String = "",
    val documentName: String = "Untitled",
    val paper: Paper = Paper.defaultA4(),
    val grid: Grid = Grid.default(),
    val referenceImage: ReferenceImage? = null,
    /** Absolute path for Coil; only set when [imageAvailability] is Present. */
    val imageAbsolutePath: String? = null,
    val imageAvailability: ImageAvailability = ImageAvailability.None,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isImporting: Boolean = false,
    val lastSaveSucceeded: Boolean? = null,
    val importError: Boolean = false,
    /** True when local edits are not yet persisted. */
    val isDirty: Boolean = false,
    /** When true, taps on the preview place measure points instead of transforming the image. */
    val measureMode: Boolean = false,
    /** First measure point in document mm (paper top-left origin), or null. */
    val measurePoint1Mm: Pair<Double, Double>? = null,
    /** Second measure point in document mm, or null. */
    val measurePoint2Mm: Pair<Double, Double>? = null,
    val selectedExportDpi: Int = 150,
    val maxSafeExportDpi: Int = 300,
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
    val lastExportPath: String? = null,
)

/** Distance in the paper's display unit, or null if both points are not set. */
fun GridConfigUiState.measureDistanceDisplay(): Double? {
    val p1 = measurePoint1Mm ?: return null
    val p2 = measurePoint2Mm ?: return null
    val mm = ViewportTransform.distanceMm(p1, p2)
    return UnitConverter.fromMm(mm, paper.displayUnit)
}

@HiltViewModel
@Suppress("TooManyFunctions")
class GridConfigViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val imageStorage: ImageStorage,
    private val documentRenderer: DocumentRenderer,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GridConfigUiState())
    val uiState: StateFlow<GridConfigUiState> = _uiState.asStateFlow()

    private var autosaveJob: Job? = null

    fun openDocument(documentId: String) {
        if (documentId.isBlank()) return
        autosaveJob?.cancel()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isDirty = false) }
            val existing = projectRepository.getDocument(documentId)
            if (existing != null) {
                applyDocument(existing)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        importError = false,
                        isDirty = false,
                        imageAvailability = ImageAvailability.None,
                        imageAbsolutePath = null,
                    )
                }
            }
        }
    }

    fun renameDocument(newName: String) {
        val name = newName.trim()
        val state = _uiState.value
        if (name.isEmpty() || state.documentId.isEmpty()) return
        _uiState.update {
            it.copy(documentName = name, lastSaveSucceeded = null, isDirty = true)
        }
        scheduleAutosave()
    }

    private fun applyDocument(document: Document) {
        val availability = imageStorage.probe(document.referenceImage?.relativePath)
        val absPath = if (availability == ImageAvailability.Present) {
            document.referenceImage?.relativePath?.let { relative ->
                imageStorage.resolveAbsoluteFile(relative).absolutePath
            }
        } else {
            null
        }
        _uiState.update {
            it.copy(
                documentId = document.id,
                documentName = document.name,
                paper = document.paper,
                grid = document.grid,
                referenceImage = document.referenceImage,
                imageAbsolutePath = absPath,
                imageAvailability = availability,
                isLoading = false,
                lastSaveSucceeded = null,
                importError = false,
                isDirty = false,
                maxSafeExportDpi = ExportSizeCalculator.maxSafeDpi(
                    widthMm = document.paper.widthMm,
                    heightMm = document.paper.heightMm,
                    maxMemoryBytes = Runtime.getRuntime().maxMemory(),
                ),
                exportMessage = null,
            )
        }
    }

    /**
     * Imports (or re-links) a gallery image. Always starts from default transform/crop
     * so a re-link does not keep offsets that belonged to a different photo.
     */
    fun importImage(uri: Uri) {
        val state = _uiState.value
        if (state.documentId.isEmpty() || state.isImporting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importError = false) }
            runCatching {
                val previousPath = state.referenceImage?.relativePath
                val relative = imageStorage.copyFromUri(state.documentId, uri)
                if (previousPath != null && previousPath != relative) {
                    imageStorage.deleteRelativeFile(previousPath)
                }
                val ref = ReferenceImage.withPath(relative)
                val updated = Document(
                    id = state.documentId,
                    name = state.documentName,
                    paper = state.paper,
                    grid = state.grid,
                    referenceImage = ref,
                )
                projectRepository.saveDocument(updated)
                applyDocument(updated)
            }.onSuccess {
                _uiState.update { it.copy(isImporting = false, isDirty = false) }
            }.onFailure {
                _uiState.update { it.copy(isImporting = false, importError = true) }
            }
        }
    }

    /**
     * Removes the reference image from the document without changing paper or grid.
     * Deletes the private file when present.
     */
    fun removeReferenceImage() {
        val state = _uiState.value
        if (state.documentId.isEmpty() || state.referenceImage == null) return
        viewModelScope.launch {
            val path = state.referenceImage.relativePath
            runCatching {
                imageStorage.deleteRelativeFile(path)
                val updated = Document(
                    id = state.documentId,
                    name = state.documentName,
                    paper = state.paper,
                    grid = state.grid,
                    referenceImage = null,
                )
                projectRepository.saveDocument(updated)
                applyDocument(updated)
            }
        }
    }

    /** Manual save from UI; also cancels a pending debounced autosave. */
    fun saveDocument() {
        autosaveJob?.cancel()
        viewModelScope.launch { persistIfDirty(showStatus = true) }
    }

    /**
     * Immediate flush for lifecycle ON_STOP (and back navigation).
     * Does not toggle the manual-save status labels.
     */
    fun flushPendingSave() {
        autosaveJob?.cancel()
        viewModelScope.launch { persistIfDirty(showStatus = false) }
    }

    private fun scheduleAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(AUTOSAVE_DEBOUNCE_MS)
            persistIfDirty(showStatus = false)
        }
    }

    private suspend fun persistIfDirty(showStatus: Boolean) {
        val state = _uiState.value
        if (!state.isDirty || state.documentId.isEmpty()) return
        if (showStatus) {
            _uiState.update { it.copy(isSaving = true, lastSaveSucceeded = null) }
        }
        runCatching {
            projectRepository.saveDocument(
                Document(
                    id = state.documentId,
                    name = state.documentName,
                    paper = state.paper,
                    grid = state.grid,
                    referenceImage = state.referenceImage,
                ),
            )
        }.onSuccess {
            _uiState.update {
                it.copy(
                    isDirty = false,
                    isSaving = false,
                    lastSaveSucceeded = if (showStatus) true else it.lastSaveSucceeded,
                )
            }
        }.onFailure {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    lastSaveSucceeded = if (showStatus) false else it.lastSaveSucceeded,
                )
            }
        }
    }

    fun setRows(rows: Int) {
        updateGrid { copy(rows = rows.coerceIn(1, MAX_DIVISIONS)) }
    }

    fun setColumns(columns: Int) {
        updateGrid { copy(columns = columns.coerceIn(1, MAX_DIVISIONS)) }
    }

    fun setVisible(visible: Boolean) {
        updateGrid { copy(visible = visible) }
    }

    fun setStrokeWidthMm(widthMm: Double) {
        updateGrid { copy(lineStyle = lineStyle.copy(strokeWidthMm = widthMm.coerceIn(0.05, 5.0))) }
    }

    fun setOpacity(opacity: Float) {
        updateGrid { copy(lineStyle = lineStyle.copy(opacity = opacity.coerceIn(0.05f, 1.0f))) }
    }

    fun setColorArgb(colorArgb: Long) {
        updateGrid { copy(lineStyle = lineStyle.copy(colorArgb = colorArgb)) }
    }

    fun panImage(deltaXMm: Double, deltaYMm: Double) {
        updateImage { ImageTransformCalculator.pan(this, deltaXMm, deltaYMm) }
    }

    fun scaleImageBy(factor: Double) {
        updateImage { ImageTransformCalculator.scaleBy(this, factor) }
    }

    fun setImageOpacity(opacity: Float) {
        updateImage { ImageTransformCalculator.withOpacity(this, opacity) }
    }

    fun setImageLocked(locked: Boolean) {
        updateImage { ImageTransformCalculator.withLocked(this, locked) }
    }

    fun setImageVisible(visible: Boolean) {
        updateImage { ImageTransformCalculator.withVisible(this, visible) }
    }

    fun resetImageTransform() {
        updateImage { ImageTransformCalculator.resetTransform(this) }
    }

    fun setMeasureMode(enabled: Boolean) {
        _uiState.update {
            it.copy(
                measureMode = enabled,
                measurePoint1Mm = if (enabled) it.measurePoint1Mm else null,
                measurePoint2Mm = if (enabled) it.measurePoint2Mm else null,
            )
        }
    }

    /**
     * Places the next measure point from a screen tap, converted via [viewport].
     * First tap → point1, second → point2, third → restarts at point1.
     */
    fun placeMeasurePoint(xPx: Float, yPx: Float, viewport: ViewportTransform) {
        if (!_uiState.value.measureMode) return
        val docPoint = viewport.screenToDocumentMm(xPx, yPx)
        _uiState.update { state ->
            when {
                state.measurePoint1Mm == null -> state.copy(measurePoint1Mm = docPoint)
                state.measurePoint2Mm == null -> state.copy(measurePoint2Mm = docPoint)
                else -> state.copy(measurePoint1Mm = docPoint, measurePoint2Mm = null)
            }
        }
    }

    fun clearMeasurePoints() {
        _uiState.update { it.copy(measurePoint1Mm = null, measurePoint2Mm = null) }
    }

    fun setCrop(left: Float, top: Float, right: Float, bottom: Float) {
        updateImage { ImageTransformCalculator.withCrop(this, left, top, right, bottom) }
    }

    fun clearCrop() {
        updateImage { ImageTransformCalculator.clearCrop(this) }
    }

    private fun updateGrid(transform: Grid.() -> Grid) {
        _uiState.update { state ->
            state.copy(
                grid = state.grid.transform(),
                lastSaveSucceeded = null,
                isDirty = true,
            )
        }
        scheduleAutosave()
    }

    private fun updateImage(transform: ReferenceImage.() -> ReferenceImage) {
        _uiState.update { state ->
            val current = state.referenceImage ?: return@update state
            state.copy(
                referenceImage = current.transform(),
                lastSaveSucceeded = null,
                isDirty = true,
            )
        }
        scheduleAutosave()
    }


    fun setExportDpi(dpi: Int) {
        _uiState.update { it.copy(selectedExportDpi = dpi.coerceAtLeast(1), exportMessage = null) }
    }

    fun exportJpeg() {
        val state = _uiState.value
        if (state.documentId.isEmpty() || state.isExporting) return
        val paper = state.paper
        val maxMemory = Runtime.getRuntime().maxMemory()
        val safeDpi = ExportSizeCalculator.maxSafeDpi(paper.widthMm, paper.heightMm, maxMemory)
        val dpi = state.selectedExportDpi
        if (!ExportSizeCalculator.isDpiSafe(paper.widthMm, paper.heightMm, dpi, maxMemory)) {
            _uiState.update {
                it.copy(
                    maxSafeExportDpi = safeDpi,
                    exportMessage = "DPI_TOO_HIGH",
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(isExporting = true, exportMessage = null, lastExportPath = null)
            }
            runCatching {
                withContext(Dispatchers.IO) {
                    flushPendingSaveSuspend()
                    val document = Document(
                        id = state.documentId,
                        name = state.documentName,
                        paper = state.paper,
                        grid = state.grid,
                        referenceImage = state.referenceImage,
                    )
                    val imageFile = state.referenceImage?.relativePath?.let { relative ->
                        val file = imageStorage.resolveAbsoluteFile(relative)
                        if (file.isFile) file else null
                    }
                    val dir = File(appContext.getExternalFilesDir(null), "exports")
                    val safeName = state.documentName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                        .ifBlank { "export" }
                    val out = File(dir, safeName + "_" + dpi + "dpi.jpg")
                    documentRenderer.exportJpeg(document, dpi, imageFile, out)
                    out.absolutePath
                }
            }.onSuccess { path ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        lastExportPath = path,
                        exportMessage = "OK",
                        maxSafeExportDpi = safeDpi,
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportMessage = "FAILED",
                        maxSafeExportDpi = safeDpi,
                    )
                }
            }
        }
    }

    private suspend fun flushPendingSaveSuspend() {
        autosaveJob?.cancel()
        persistIfDirty(showStatus = false)
    }

    companion object {
        const val MAX_DIVISIONS: Int = 100
        const val AUTOSAVE_DEBOUNCE_MS: Long = 800L
    }
}
