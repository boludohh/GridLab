package com.gridlab.app.feature.editor.grid

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gridlab.app.core.data.repository.ProjectRepository
import com.gridlab.app.core.data.storage.ExportStorage
import com.gridlab.app.core.data.storage.ImageStorage
import com.gridlab.app.core.geometry.ExportSizeCalculator
import com.gridlab.app.core.render.DocumentRenderer
import com.gridlab.app.core.geometry.ImageTransformCalculator
import com.gridlab.app.core.geometry.UnitConverter
import com.gridlab.app.core.geometry.ViewportTransform
import com.gridlab.app.core.model.Document
import com.gridlab.app.core.model.ExportFormat
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.ImageFilter
import com.gridlab.app.core.model.ImageAvailability
import com.gridlab.app.core.model.LengthUnit
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.PaperOrientation
import com.gridlab.app.core.model.ReferenceImage
import com.gridlab.app.core.model.StandardPaperFormat
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
import javax.inject.Inject

data class GridConfigUiState(
    val documentId: String = "",
    val documentName: String = "Untitled",
    val paper: Paper = Paper.defaultA4(),
    val grid: Grid = Grid.default(),
    val referenceImage: ReferenceImage? = null,
    val imageAbsolutePath: String? = null,
    val imageAvailability: ImageAvailability = ImageAvailability.None,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isImporting: Boolean = false,
    val lastSaveSucceeded: Boolean? = null,
    val importError: Boolean = false,
    val isDirty: Boolean = false,
    val measureMode: Boolean = false,
    val measurePoint1Mm: Pair<Double, Double>? = null,
    val measurePoint2Mm: Pair<Double, Double>? = null,
    val measureColorArgb: Long = 0xFF1565C0,
    val selectedExportDpi: Int = 150,
    val maxSafeExportDpi: Int = 300,
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
    val lastExportLocation: String? = null,
)

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
    private val exportStorage: ExportStorage,
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
                lastExportLocation = null,
            )
        }
    }

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

    fun saveDocument() {
        autosaveJob?.cancel()
        viewModelScope.launch { persistIfDirty(showStatus = true) }
    }

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

    fun setMeasureMode(enabled: Boolean) {
        _uiState.update {
            it.copy(
                measureMode = enabled,
                measurePoint1Mm = if (enabled) it.measurePoint1Mm else null,
                measurePoint2Mm = if (enabled) it.measurePoint2Mm else null,
            )
        }
    }

    fun placeMeasurePoint(xPx: Float, yPx: Float, viewport: ViewportTransform) {
        if (!_uiState.value.measureMode) return
        val docPoint = viewport.screenToDocumentMm(xPx, yPx)
        val paper = _uiState.value.paper
        if (docPoint.first < 0.0 || docPoint.first > paper.widthMm ||
            docPoint.second < 0.0 || docPoint.second > paper.heightMm
        ) {
            return
        }
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
        exportDocument(ExportFormat.Jpeg)
    }

    fun exportPng() {
        exportDocument(ExportFormat.Png)
    }

    fun exportPdf() {
        exportDocument(ExportFormat.Pdf)
    }

    fun requiresStoragePermission(): Boolean = exportStorage.requiresStoragePermission()

    fun hasStoragePermission(): Boolean = exportStorage.hasStoragePermission()

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
                it.copy(isExporting = true, exportMessage = null, lastExportLocation = null)
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
                    val (outputStream, location) = exportStorage.openOutputStream(format)
                    outputStream.use { stream ->
                        when (format) {
                            ExportFormat.Jpeg ->
                                documentRenderer.exportJpeg(document, dpi, imageFile, stream)
                            ExportFormat.Png ->
                                documentRenderer.exportPng(document, dpi, imageFile, stream)
                            ExportFormat.Pdf ->
                                documentRenderer.exportPdf(document, dpi, imageFile, stream)
                        }
                    }
                    location
                }
            }.onSuccess { location ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        lastExportLocation = location,
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