package com.gridlab.app.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gridlab.app.core.data.repository.ProjectRepository
import com.gridlab.app.core.data.storage.ImageStorage
import com.gridlab.app.core.model.Document
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ProjectRowUi(
    val document: Document,
    /** Absolute path for Coil when the reference image file exists. */
    val thumbnailAbsolutePath: String?,
)

data class MainUiState(
    val projects: List<ProjectRowUi> = emptyList(),
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val errorMessage: String? = null,
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val imageStorage: ImageStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { projectRepository.listDocuments() }
                .onSuccess { list ->
                    val rows = list.map { doc ->
                        val path = doc.referenceImage?.relativePath?.let { relative ->
                            val file = imageStorage.resolveAbsoluteFile(relative)
                            if (file.isFile) file.absolutePath else null
                        }
                        ProjectRowUi(document = doc, thumbnailAbsolutePath = path)
                    }
                    _uiState.update {
                        it.copy(
                            projects = rows,
                            isLoading = false,
                            selectedIds = it.selectedIds.intersect(rows.map { row -> row.document.id }.toSet()),
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Could not load projects")
                    }
                }
        }
    }

    fun createProject(onCreated: (String) -> Unit) {
        if (_uiState.value.isWorking) return
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, errorMessage = null) }
            val id = UUID.randomUUID().toString()
            val doc = Document.newDefault(id = id, name = "Project " + id.take(8))
            runCatching {
                projectRepository.saveDocument(doc)
                id
            }.onSuccess { newId ->
                _uiState.update { it.copy(isWorking = false) }
                refresh()
                onCreated(newId)
            }.onFailure {
                _uiState.update {
                    it.copy(isWorking = false, errorMessage = "Could not create project")
                }
            }
        }
    }

    fun renameProject(id: String, newName: String) {
        val name = newName.trim()
        if (name.isEmpty() || _uiState.value.isWorking) return
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, errorMessage = null) }
            runCatching {
                val existing = projectRepository.getDocument(id) ?: return@runCatching
                projectRepository.saveDocument(existing.copy(name = name))
            }.onSuccess {
                _uiState.update { it.copy(isWorking = false) }
                refresh()
            }.onFailure {
                _uiState.update {
                    it.copy(isWorking = false, errorMessage = "Could not rename project")
                }
            }
        }
    }

    /**
     * First tap enters selection mode.
     * Second tap with one or more selected projects should be followed by the delete
     * confirmation dialog in the UI; call [confirmDeleteSelected] after accept.
     * Returns true when the UI should show the multi-delete confirmation dialog.
     */
    fun onDeleteHeaderClick(): Boolean {
        val state = _uiState.value
        if (!state.isSelectionMode) {
            _uiState.update {
                it.copy(isSelectionMode = true, selectedIds = emptySet())
            }
            return false
        }
        return state.selectedIds.isNotEmpty()
    }

    fun toggleProjectSelection(id: String) {
        if (!_uiState.value.isSelectionMode) return
        _uiState.update { state ->
            val next = if (id in state.selectedIds) {
                state.selectedIds - id
            } else {
                state.selectedIds + id
            }
            state.copy(selectedIds = next)
        }
    }

    fun exitSelectionMode() {
        _uiState.update {
            it.copy(isSelectionMode = false, selectedIds = emptySet())
        }
    }

    fun confirmDeleteSelected() {
        val ids = _uiState.value.selectedIds
        if (ids.isEmpty() || _uiState.value.isWorking) {
            exitSelectionMode()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, errorMessage = null) }
            runCatching {
                ids.forEach { id -> projectRepository.deleteDocument(id) }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        isSelectionMode = false,
                        selectedIds = emptySet(),
                    )
                }
                refresh()
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        errorMessage = "Could not delete projects",
                        isSelectionMode = false,
                        selectedIds = emptySet(),
                    )
                }
            }
        }
    }
}
