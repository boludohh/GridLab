package com.gridlab.app.feature.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gridlab.app.core.data.repository.ProjectRepository
import com.gridlab.app.core.model.Document
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ProjectListUiState(
    val projects: List<Document> = emptyList(),
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectListUiState())
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { projectRepository.listDocuments() }
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(projects = list, isLoading = false)
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
            val doc = Document.newDefault(id = id, name = "Untitled")
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

    fun deleteProject(id: String) {
        if (_uiState.value.isWorking) return
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, errorMessage = null) }
            runCatching { projectRepository.deleteDocument(id) }
                .onSuccess {
                    _uiState.update { it.copy(isWorking = false) }
                    refresh()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isWorking = false, errorMessage = "Could not delete project")
                    }
                }
        }
    }
}
