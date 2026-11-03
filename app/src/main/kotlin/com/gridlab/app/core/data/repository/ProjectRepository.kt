package com.gridlab.app.core.data.repository

import com.gridlab.app.core.model.Document

interface ProjectRepository {
    suspend fun getDocument(id: String): Document?
    suspend fun getLatestDocument(): Document?
    suspend fun listDocuments(): List<Document>
    suspend fun saveDocument(document: Document)
    /** Deletes the Room row first, then project files under filesDir. */
    suspend fun deleteDocument(id: String)
}
