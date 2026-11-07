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
import com.gridlab.app.core.model.Document
import com.gridlab.app.core.model.ExportFormat
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.ImageFilter
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.PaperOrientation
import com.gridlab.app.core.model.ReferenceImage
import com.gridlab.app.core.model.StandardPaperFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

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
    private var documentLoadJob: Job? = null
    private val saveMutex = Mutex()

    fun openDocument(documentId: String) {
        if (documentId.isBlank()) return
        autosaveJob?.cancel()
        documentLoadJob?.cancel()
        documentLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isDirty = false) }
            val existing = projectRepository.getDocument(documentId)
            if (existing != null) {
                applyDocument(existing)
            } else {
                _uiState.update {
                    it.copy(
                        documentId = documentId,
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
                maxSafeExportDpi = run {
                    val safe = ExportSizeCalculator.maxSafeDpi(
                        widthMm = document.paper.widthMm,
                        heightMm = document.paper.heightMm,
                        maxMemoryBytes = Runtime.getRuntime().maxMemory(),
                    )
                    safe
                },
                selectedExportDpi = run {
                    val safe = ExportSizeCalculator.maxSafeDpi(
                        widthMm = document.paper.widthMm,
                        heightMm = document.paper.heightMm,
                        maxMemoryBytes = Runtime.getRuntime().maxMemory(),
                    )
                    val preferred = 150
                    minOf(preferred, safe).coerceAtLeast(1)
                },
                exportMessage = null,
                lastExportPath = null,
            )
        }
    }

    /**
     * Imports (or re-links) a gallery image. Always starts from default transform/crop
     * so a re-link does not keep offsets that belonged to a different photo.
     */
    fun importImage(uri: Uri) {
        val initialState = _uiState.value
        if (initialState.documentId.isEmpty() || initialState.isImporting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importError = false) }
            var copiedPath: String? = null
            runCatching {
                copiedPath = imageStorage.copyFromUri(initialState.documentId, uri)
                val current = _uiState.value
                check(current.documentId == initialState.documentId) {
                    "The active document changed while importing the image"
                }
                val updated = Document(
                    id = current.documentId,
                    name = current.documentName,
                    paper = current.paper,
                    grid = current.grid,
                    referenceImage = ReferenceImage.withPath(requireNotNull(copiedPath)),
                )
                saveMutex.withLock { projectRepository.saveDocument(updated) }
                var editsRemain = false
                _uiState.update { latest ->
                    editsRemain = latest.documentId != updated.id ||
                        latest.documentName != updated.name ||
                        latest.paper != updated.paper ||
                        latest.grid != updated.grid ||
                        latest.referenceImage != current.referenceImage
                    latest.copy(
                        referenceImage = updated.referenceImage,
                        imageAbsolutePath = imageStorage.resolveAbsoluteFile(requireNotNull(copiedPath)).absolutePath,
                        imageAvailability = ImageAvailability.Present,
                        isDirty = editsRemain,
                        isImporting = false,
                        importError = false,
                    )
                }
                if (editsRemain) scheduleAutosave()
                current.referenceImage?.relativePath?.takeIf { it != copiedPath }?.let { oldPath ->
                    runCatching { imageStorage.deleteRelativeFile(oldPath) }
                }
            }.onSuccess {
                _uiState.update { it.copy(isImporting = false, importError = false) }
            }.onFailure {
                copiedPath?.let { path -> imageStorage.deleteRelativeFile(path) }
                _uiState.update { it.copy(isImporting = false, importError = true) }
            }
        }
    }

    fun removeReferenceImage() {
        val initialState = _uiState.value
        if (initialState.documentId.isEmpty() || initialState.referenceImage == null) return
        viewModelScope.launch {
            val pathToDelete = initialState.referenceImage.relativePath
            runCatching {
                val current = _uiState.value
                check(current.documentId == initialState.documentId) {
                    "The active document changed while removing the image"
                }
                val updated = Document(
                    id = current.documentId,
                    name = current.documentName,
                    paper = current.paper,
                    grid = current.grid,
                    referenceImage = null,
                )
                saveMutex.withLock { projectRepository.saveDocument(updated) }
                var editsRemain = false
                _uiState.update { latest ->
                    editsRemain = latest.documentId != updated.id ||
                        latest.documentName != updated.name ||
                        latest.paper != updated.paper ||
                        latest.grid != updated.grid ||
                        latest.referenceImage != current.referenceImage
                    latest.copy(
                        referenceImage = null,
                        imageAbsolutePath = null,
                        imageAvailability = ImageAvailability.None,
                        isDirty = editsRemain,
                    )
                }
                if (editsRemain) scheduleAutosave()
                runCatching { imageStorage.deleteRelativeFile(pathToDelete) }
            }
        }
    }

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
        saveMutex.withLock {
            val state = _uiState.value
            if (!state.isDirty || state.documentId.isEmpty()) return@withLock
            val documentToSave = Document(
                id = state.documentId,
                name = state.documentName,
                paper = state.paper,
                grid = state.grid,
                referenceImage = state.referenceImage,
            )
            if (showStatus) {
                _uiState.update { it.copy(isSaving = true, lastSaveSucceeded = null) }
            }
            runCatching {
                projectRepository.saveDocument(documentToSave)
            }.onSuccess {
                var editsRemain = false
                _uiState.update { current ->
                    editsRemain = current.documentId != documentToSave.id ||
                        current.documentName != documentToSave.name ||
                        current.paper != documentToSave.paper ||
                        current.grid != documentToSave.grid ||
                        current.referenceImage != documentToSave.referenceImage
                    current.copy(
                        isDirty = editsRemain,
                        isSaving = false,
                        lastSaveSucceeded = if (showStatus) true else current.lastSaveSucceeded,
                    )
                }
                if (editsRemain) scheduleAutosave()
            }.onFailure {
                _uiState.update { current ->
                    current.copy(
                        isDirty = true,
                        isSaving = false,
                        lastSaveSucceeded = if (showStatus) false else current.lastSaveSucceeded,
                    )
                }
            }
        }
    }

    // --- Paper setters ------------------------------------------------------

    fun setPaperFormat(format: StandardPaperFormat) {
        if (_uiState.value.paper.format == format) return
        updatePaper {
            if (format == StandardPaperFormat.Custom) {
                copy(format = format)
            } else {
                Paper.fromStandard(
                    format = format,
                    orientation = orientation,
                    displayUnit = displayUnit,
                    marginLeftMm = marginLeftMm,
                    marginTopMm = marginTopMm,
                    marginRightMm = marginRightMm,
                    marginBottomMm = marginBottomMm,
                    colorArgb = colorArgb,
                )
            }
        }
    }

    fun setPaperOrientation(orientation: PaperOrientation) {
        if (_uiState.value.paper.orientation == orientation) return
        updatePaper {
            if (format == StandardPaperFormat.Custom) {
                copy(
                    orientation = orientation,
                    widthMm = heightMm,
                    heightMm = widthMm,
                )
            } else {
                Paper.fromStandard(
                    format = format,
                    orientation = orientation,
                    displayUnit = displayUnit,
                    marginLeftMm = marginLeftMm,
                    marginTopMm = marginTopMm,
                    marginRightMm = marginRightMm,
                    marginBottomMm = marginBottomMm,
                    colorArgb = colorArgb,
                )
            }
        }

    }

    fun setDisplayUnit(unit: LengthUnit) {
        if (_uiState.value.paper.displayUnit == unit) return
        updatePaper { copy(displayUnit = unit) }
    }

    fun setCustomWidth(displayValue: Double) {
        val mm = displayToMm(displayValue, _uiState.value.paper.displayUnit)
            .coerceAtLeast(MIN_PAPER_DIMENSION_MM)
        updatePaper { copy(format = StandardPaperFormat.Custom, widthMm = mm) }
    }

    fun setCustomHeight(displayValue: Double) {
        val mm = displayToMm(displayValue, _uiState.value.paper.displayUnit)
            .coerceAtLeast(MIN_PAPER_DIMENSION_MM)
        updatePaper { copy(format = StandardPaperFormat.Custom, heightMm = mm) }
    }

    fun setMarginLeft(displayValue: Double) {
        val mm = displayToMm(displayValue, _uiState.value.paper.displayUnit)
            .coerceAtLeast(0.0)
        updatePaper { copy(marginLeftMm = mm) }
    }

    fun setMarginTop(displayValue: Double) {
        val mm = displayToMm(displayValue, _uiState.value.paper.displayUnit)
            .coerceAtLeast(0.0)
        updatePaper { copy(marginTopMm = mm) }
    }

    fun setMarginRight(displayValue: Double) {
        val mm = displayToMm(displayValue, _uiState.value.paper.displayUnit)
            .coerceAtLeast(0.0)
        updatePaper { copy(marginRightMm = mm) }
    }

    fun setMarginBottom(displayValue: Double) {
        val mm = displayToMm(displayValue, _uiState.value.paper.displayUnit)
            .coerceAtLeast(0.0)
        updatePaper { copy(marginBottomMm = mm) }
    }

    private fun displayToMm(displayValue: Double, unit: LengthUnit): Double = when (unit) {
        LengthUnit.Millimeter -> displayValue
        LengthUnit.Centimeter -> displayValue * 10.0
        LengthUnit.Inch -> displayValue * 25.4
    }

    private fun updatePaper(transform: Paper.() -> Paper) {
        _uiState.update { state ->
            state.copy(
                paper = state.paper.transform(),
                lastSaveSucceeded = null,
                isDirty = true,
            )
        }
        scheduleAutosave()
    }

    // --- Grid setters -------------------------------------------------------

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

    fun setSecondaryColorArgb(colorArgb: Long) {
        updateGrid { copy(secondaryLineStyle = secondaryLineStyle.copy(colorArgb = colorArgb)) }
    }

    fun setLabelColorArgb(colorArgb: Long) {
        updateGrid { copy(labelColorArgb = colorArgb) }
    }

    fun setPaperColorArgb(colorArgb: Long) {
        updatePaper { copy(colorArgb = colorArgb) }
    }

    fun setMeasureColorArgb(colorArgb: Long) {
        _uiState.update { it.copy(measureColorArgb = colorArgb) }
    }

    fun setSecondaryDivisions(divisions: Int) {
        updateGrid { copy(secondaryDivisions = divisions.coerceIn(1, MAX_SECONDARY_DIVISIONS)) }
    }

    fun setSecondaryVisible(visible: Boolean) {
        updateGrid { copy(secondaryVisible = visible) }
    }

    fun setLabelsVisible(visible: Boolean) {
        updateGrid { copy(labelsVisible = visible) }
    }

    // --- Image setters ------------------------------------------------------

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

    fun setImageRotation(degrees: Float) {
        updateImage { ImageTransformCalculator.withRotation(this, degrees) }
    }

    fun setImageFilter(filter: ImageFilter) {
        updateImage { ImageTransformCalculator.withFilter(this, filter) }
    }

    // --- Measure tool -------------------------------------------------------

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

    // --- Crop ---------------------------------------------------------------

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

    // --- Export --------------------------------------------------------------

    fun setExportDpi(dpi: Int) {
        _uiState.update { it.copy(selectedExportDpi = dpi.coerceAtLeast(1), exportMessage = null) }
    }

    fun exportJpeg() {
        exportDocument(ExportFormat.Jpeg)
    }

    fun exportPng() {
        exportDocument(ExportFormat.Png)
    }

    fun exportPdf() {
        exportDocument(ExportFormat.Pdf)
    }

    private fun exportDocument(format: ExportFormat) {
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
                    val extension = when (format) {
                        ExportFormat.Jpeg -> "jpg"
                        ExportFormat.Png -> "png"
                        ExportFormat.Pdf -> "pdf"
                    }
                    val out = File(dir, safeName + "_" + dpi + "dpi." + extension)
                    when (format) {
                        ExportFormat.Jpeg ->
                            documentRenderer.exportJpeg(document, dpi, imageFile, out)
                        ExportFormat.Png ->
                            documentRenderer.exportPng(document, dpi, imageFile, out)
                        ExportFormat.Pdf ->
                            documentRenderer.exportPdf(document, dpi, imageFile, out)
                    }
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
        const val MAX_SECONDARY_DIVISIONS: Int = 8
        const val AUTOSAVE_DEBOUNCE_MS: Long = 800L
        const val MIN_PAPER_DIMENSION_MM: Double = 10.0
    }
}