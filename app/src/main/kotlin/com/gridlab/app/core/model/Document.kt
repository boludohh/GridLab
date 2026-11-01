package com.gridlab.app.core.model

/**
 * Root document model.
 *
 * [schemaVersion] is stored with the persisted payload for future upgrades.
 */
data class Document(
    val id: String,
    val name: String,
    val paper: Paper,
    val grid: Grid = Grid.default(),
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 2

        fun newDefault(id: String, name: String): Document =
            Document(
                id = id,
                name = name,
                paper = Paper.defaultA4(),
                grid = Grid.default(),
            )
    }
}
