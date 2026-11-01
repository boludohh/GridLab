package com.gridlab.app.feature.editor.grid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gridlab.app.core.data.repository.ProjectRepository
import com.gridlab.app.core.model.Document
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.Paper
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
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val lastSaveSucceeded: Boolean? = null,
)

@HiltViewModel
class GridConfigViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
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
                _uiState.update {
                    it.copy(
                        documentId = existing.id,
                        documentName = existing.name,
                        paper = existing.paper,
                        grid = existing.grid,
                        isLoading = false,
                        lastSaveSucceeded = null,
                    )
                }
            } else {
                val created = Document.newDefault(
                    id = UUID.randomUUID().toString(),
                    name = "Untitled",
                )
                projectRepository.saveDocument(created)
                _uiState.update {
                    it.copy(
                        documentId = created.id,
                        documentName = created.name,
                        paper = created.paper,
                        grid = created.grid,
                        isLoading = false,
                        lastSaveSucceeded = null,
                    )
                }
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
        val value = rows.coerceIn(1, MAX_DIVISIONS)
        updateGrid { copy(rows = value) }
    }

    fun setColumns(columns: Int) {
        val value = columns.coerceIn(1, MAX_DIVISIONS)
        updateGrid { copy(columns = value) }
    }

    fun setVisible(visible: Boolean) {
        updateGrid { copy(visible = visible) }
    }

    fun setStrokeWidthMm(widthMm: Double) {
        val w = widthMm.coerceIn(0.05, 5.0)
        updateGrid { copy(lineStyle = lineStyle.copy(strokeWidthMm = w)) }
    }

    fun setOpacity(opacity: Float) {
        val o = opacity.coerceIn(0.05f, 1.0f)
        updateGrid { copy(lineStyle = lineStyle.copy(opacity = o)) }
    }

    fun setColorArgb(colorArgb: Long) {
        updateGrid { copy(lineStyle = lineStyle.copy(colorArgb = colorArgb)) }
    }

    private fun updateGrid(transform: Grid.() -> Grid) {
        _uiState.update { state ->
            state.copy(grid = state.grid.transform(), lastSaveSucceeded = null)
        }
    }

    companion object {
        const val MAX_DIVISIONS: Int = 100
    }
}
