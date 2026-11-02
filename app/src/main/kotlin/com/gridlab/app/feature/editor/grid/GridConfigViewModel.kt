package com.gridlab.app.feature.editor.grid

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gridlab.app.core.data.repository.ProjectRepository
import com.gridlab.app.core.data.storage.ImageStorage
import com.gridlab.app.core.geometry.ImageTransformCalculator
import com.gridlab.app.core.model.Document
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.ReferenceImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class GridConfigUiState(
    val documentId: String = "",
    val documentName: String = "Untitled",
    val paper: Paper = Paper.defaultA4(),
    val grid: Grid = Grid.default(),
    val referenceImage: ReferenceImage? = null,
    /** Absolute path for Coil; null if no image or file missing. */
    val imageAbsolutePath: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isImporting: Boolean = false,
    val lastSaveSucceeded: Boolean? = null,
    val importError: Boolean = false,
)

@HiltViewModel
class GridConfigViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val imageStorage: ImageStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GridConfigUiState())
    val uiState: StateFlow<GridConfigUiState> = _uiState.asStateFlow()

    init {
        loadOrCreateDocument()
    }

    private fun loadOrCreateDocument() {
        viewModelScope.launch {
            val existing = projectRepository.getLatestDocument()
            if (existing != null) {
                applyDocument(existing)
            } else {
                val created = Document.newDefault(
                    id = UUID.randomUUID().toString(),
                    name = "Untitled",
                )
                projectRepository.saveDocument(created)
                applyDocument(created)
            }
        }
    }

    private fun applyDocument(document: Document) {
        val absPath = document.referenceImage?.relativePath?.let { relative ->
            val file = imageStorage.resolveAbsoluteFile(relative)
            if (file.isFile) file.absolutePath else null
        }
        _uiState.update {
            it.copy(
                documentId = document.id,
                documentName = document.name,
                paper = document.paper,
                grid = document.grid,
                referenceImage = document.referenceImage,
                imageAbsolutePath = absPath,
                isLoading = false,
                lastSaveSucceeded = null,
                importError = false,
            )
        }
    }

    fun importImage(uri: Uri) {
        val state = _uiState.value
        if (state.documentId.isEmpty() || state.isImporting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importError = false) }
            runCatching {
                val relative = imageStorage.copyFromUri(state.documentId, uri)
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
                _uiState.update { it.copy(isImporting = false) }
            }.onFailure {
                _uiState.update { it.copy(isImporting = false, importError = true) }
            }
        }
    }

    fun saveDocument() {
        val state = _uiState.value
        if (state.documentId.isEmpty() || state.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, lastSaveSucceeded = null) }
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
                _uiState.update { it.copy(isSaving = false, lastSaveSucceeded = true) }
            }.onFailure {
                _uiState.update { it.copy(isSaving = false, lastSaveSucceeded = false) }
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

    // --- Image transform (0.8.0) ---

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

    private fun updateGrid(transform: Grid.() -> Grid) {
        _uiState.update { state ->
            state.copy(grid = state.grid.transform(), lastSaveSucceeded = null)
        }
    }

    private fun updateImage(transform: ReferenceImage.() -> ReferenceImage) {
        _uiState.update { state ->
            val current = state.referenceImage ?: return@update state
            state.copy(
                referenceImage = current.transform(),
                lastSaveSucceeded = null,
            )
        }
    }

    companion object {
        const val MAX_DIVISIONS: Int = 100
    }
}
