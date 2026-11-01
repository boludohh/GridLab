package com.gridlab.app.core.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** JSON payload of [com.gridlab.app.core.model.Paper]. */
    val paperJson: String,
    /** JSON payload of [com.gridlab.app.core.model.Grid]. */
    val gridJson: String = "{}",
    /** JSON payload of [com.gridlab.app.core.model.ReferenceImage], or empty if none. */
    val referenceImageJson: String = "",
    val schemaVersion: Int,
    val updatedAtEpochMs: Long,
)
