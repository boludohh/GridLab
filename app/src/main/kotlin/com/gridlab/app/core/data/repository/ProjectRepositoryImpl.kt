package com.gridlab.app.core.data.repository

import com.gridlab.app.core.data.database.DocumentEntity
import com.gridlab.app.core.data.database.ProjectDao
import com.gridlab.app.core.model.Document
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.ReferenceImage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao,
) : ProjectRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun getDocument(id: String): Document? =
        projectDao.getById(id)?.toDomain()

    override suspend fun getLatestDocument(): Document? =
        projectDao.getLatest()?.toDomain()

    override suspend fun saveDocument(document: Document) {
        projectDao.upsert(document.toEntity())
    }

    private fun DocumentEntity.toDomain(): Document {
        val paper = json.decodeFromString<Paper>(paperJson)
        val grid = if (gridJson.isBlank() || gridJson == "{}") {
            Grid.default()
        } else {
            json.decodeFromString<Grid>(gridJson)
        }
        val referenceImage = if (referenceImageJson.isBlank()) {
            null
        } else {
            json.decodeFromString<ReferenceImage>(referenceImageJson)
        }
        return Document(
            id = id,
            name = name,
            paper = paper,
            grid = grid,
            referenceImage = referenceImage,
            schemaVersion = schemaVersion,
        )
    }

    private fun Document.toEntity(): DocumentEntity =
        DocumentEntity(
            id = id,
            name = name,
            paperJson = json.encodeToString(paper),
            gridJson = json.encodeToString(grid),
            referenceImageJson = referenceImage?.let { json.encodeToString(it) }.orEmpty(),
            schemaVersion = schemaVersion,
            updatedAtEpochMs = System.currentTimeMillis(),
        )
}
