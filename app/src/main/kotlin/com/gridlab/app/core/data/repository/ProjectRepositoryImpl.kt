package com.gridlab.app.core.data.repository

import com.gridlab.app.core.data.database.DocumentEntity
import com.gridlab.app.core.data.database.ProjectDao
import com.gridlab.app.core.model.Document
import com.gridlab.app.core.model.Paper
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
        return Document(
            id = id,
            name = name,
            paper = paper,
            schemaVersion = schemaVersion,
        )
    }

    private fun Document.toEntity(): DocumentEntity =
        DocumentEntity(
            id = id,
            name = name,
            paperJson = json.encodeToString(paper),
            schemaVersion = schemaVersion,
            updatedAtEpochMs = System.currentTimeMillis(),
        )
}
