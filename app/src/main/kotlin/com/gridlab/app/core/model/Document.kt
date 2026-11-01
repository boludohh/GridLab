package com.gridlab.app.core.model

/**
 * Root document model.
 *
 * [schemaVersion] is reserved for future JSON persistence upgrades (Room).
 * Grid and reference image are added in later versions.
 */
data class Document(
    val id: String,
    val name: String,
    val paper: Paper,
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 1

        fun newWithDefaultPaper(id: String, name: String): Document =
            Document(
                id = id,
                name = name,
                paper = Paper.defaultA4(),
            )
    }
}

