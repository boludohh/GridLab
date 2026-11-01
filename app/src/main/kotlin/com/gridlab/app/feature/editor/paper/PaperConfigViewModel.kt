package com.gridlab.app.feature.editor.paper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gridlab.app.core.data.repository.ProjectRepository
import com.gridlab.app.core.geometry.UnitConverter
import com.gridlab.app.core.model.Document
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.LengthUnit
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.PaperOrientation
import com.gridlab.app.core.model.StandardPaperFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaperConfigUiState(
    val documentId: String = "",
    val documentName: String = "Untitled",
    val paper: Paper = Paper.defaultA4(),
    val grid: Grid = Grid.default(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val lastSaveSucceeded: Boolean? = null,
)

@HiltViewModel
class PaperConfigViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaperConfigUiState())
    val uiState: StateFlow<PaperConfigUiState> = _uiState.asStateFlow()

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

    fun setFormat(format: StandardPaperFormat) {
        _uiState.update { state ->
            val current = state.paper
            val updated = if (format == StandardPaperFormat.Custom) {
                current.copy(format = StandardPaperFormat.Custom)
            } else {
                Paper.fromStandard(
                    format = format,
                    orientation = current.orientation,
                    displayUnit = current.displayUnit,
                    marginLeftMm = current.marginLeftMm,
                    marginTopMm = current.marginTopMm,
                    marginRightMm = current.marginRightMm,
                    marginBottomMm = current.marginBottomMm,
                )
            }
            state.copy(paper = updated, lastSaveSucceeded = null)
        }
    }

    fun setOrientation(orientation: PaperOrientation) {
        _uiState.update { state ->
            val current = state.paper
            val updated = if (current.format == StandardPaperFormat.Custom) {
                current.copy(
                    orientation = orientation,
                    widthMm = current.heightMm,
                    heightMm = current.widthMm,
                )
            } else {
                Paper.fromStandard(
                    format = current.format,
                    orientation = orientation,
                    displayUnit = current.displayUnit,
                    marginLeftMm = current.marginLeftMm,
                    marginTopMm = current.marginTopMm,
                    marginRightMm = current.marginRightMm,
                    marginBottomMm = current.marginBottomMm,
                )
            }
            state.copy(paper = updated, lastSaveSucceeded = null)
        }
    }

    fun setDisplayUnit(unit: LengthUnit) {
        _uiState.update { state ->
            state.copy(paper = state.paper.copy(displayUnit = unit), lastSaveSucceeded = null)
        }
    }

    fun setCustomWidth(valueInDisplayUnit: Double) {
        _uiState.update { state ->
            val mm = UnitConverter.toMm(valueInDisplayUnit, state.paper.displayUnit)
            state.copy(
                paper = state.paper.copy(
                    format = StandardPaperFormat.Custom,
                    widthMm = mm.coerceAtLeast(1.0),
                ),
                lastSaveSucceeded = null,
            )
        }
    }

    fun setCustomHeight(valueInDisplayUnit: Double) {
        _uiState.update { state ->
            val mm = UnitConverter.toMm(valueInDisplayUnit, state.paper.displayUnit)
            state.copy(
                paper = state.paper.copy(
                    format = StandardPaperFormat.Custom,
                    heightMm = mm.coerceAtLeast(1.0),
                ),
                lastSaveSucceeded = null,
            )
        }
    }

    fun setMarginLeft(valueInDisplayUnit: Double) {
        updateMargin { copy(marginLeftMm = UnitConverter.toMm(valueInDisplayUnit, displayUnit).coerceAtLeast(0.0)) }
    }

    fun setMarginTop(valueInDisplayUnit: Double) {
        updateMargin { copy(marginTopMm = UnitConverter.toMm(valueInDisplayUnit, displayUnit).coerceAtLeast(0.0)) }
    }

    fun setMarginRight(valueInDisplayUnit: Double) {
        updateMargin { copy(marginRightMm = UnitConverter.toMm(valueInDisplayUnit, displayUnit).coerceAtLeast(0.0)) }
    }

    fun setMarginBottom(valueInDisplayUnit: Double) {
        updateMargin { copy(marginBottomMm = UnitConverter.toMm(valueInDisplayUnit, displayUnit).coerceAtLeast(0.0)) }
    }

    private fun updateMargin(transform: Paper.() -> Paper) {
        _uiState.update { state ->
            state.copy(paper = state.paper.transform(), lastSaveSucceeded = null)
        }
    }
}
