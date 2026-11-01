package com.gridlab.app.core.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** JSON payload of [com.gridlab.app.core.model.Paper]. */
    val paperJson: String,
    /** Explicit schema version stored alongside the row for future migrations. */
    val schemaVersion: Int,
    val updatedAtEpochMs: Long,
)
