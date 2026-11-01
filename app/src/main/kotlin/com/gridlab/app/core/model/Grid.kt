package com.gridlab.app.core.model

import kotlinx.serialization.Serializable

/**
 * Main grid configuration for a document.
 *
 * Cell size is derived from the paper printable area and [rows]/[columns];
 * it is not stored as independent state.
 */
@Serializable
data class Grid(
    val rows: Int = DEFAULT_ROWS,
    val columns: Int = DEFAULT_COLUMNS,
    val visible: Boolean = true,
    val lineStyle: GridLineStyle = GridLineStyle(),
) {
    init {
        require(rows >= 1) { "rows must be >= 1" }
        require(columns >= 1) { "columns must be >= 1" }
    }

    companion object {
        const val DEFAULT_ROWS: Int = 10
        const val DEFAULT_COLUMNS: Int = 10

        fun default(): Grid = Grid()
    }
}
