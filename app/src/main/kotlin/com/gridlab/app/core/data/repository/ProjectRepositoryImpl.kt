package com.gridlab.app.core.data.repository

import com.gridlab.app.core.data.database.DocumentEntity
import com.gridlab.app.core.data.database.ProjectDao
import com.gridlab.app.core.data.storage.ImageStorage
import com.gridlab.app.core.model.Document
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.ReferenceImage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao,
    private val imageStorage: ImageStorage,
) : ProjectRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun getDocument(id: String): Document? =
        projectDao.getById(id)?.toDomainOrNull()

    override suspend fun getLatestDocument(): Document? =
        projectDao.getLatest()?.toDomainOrNull()

    /**
     * Returns every document that can be decoded.
     * A single malformed JSON row is skipped so the rest of the list still loads.
     */
    override suspend fun listDocuments(): List<Document> =
        projectDao.getAll().mapNotNull { entity -> entity.toDomainOrNull() }

    override suspend fun saveDocument(document: Document) {
        projectDao.upsert(document.toEntity())
    }

    override suspend fun deleteDocument(id: String) {
        projectDao.deleteById(id)
        imageStorage.deleteProjectDir(id)
    }

    /**
     * Decodes entity fields; returns null on any serialization/validation failure
     * so callers can isolate bad rows without failing the whole query.
     */
    private fun DocumentEntity.toDomainOrNull(): Document? =
        runCatching { toDomain() }.getOrNull()

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
