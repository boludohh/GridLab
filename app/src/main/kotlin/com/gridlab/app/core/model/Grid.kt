package com.gridlab.app.core.model

import kotlinx.serialization.Serializable

/**
 * Main grid configuration for a document.
 *
 * [labelsVisible] draws column numbers (1..N) along the top and row letters
 * (A, B, …) along the left edge of the printable area.
 */
@Serializable
data class Grid(
    val rows: Int = DEFAULT_ROWS,
    val columns: Int = DEFAULT_COLUMNS,
    val visible: Boolean = true,
    val lineStyle: GridLineStyle = GridLineStyle(),
    val secondaryDivisions: Int = DEFAULT_SECONDARY_DIVISIONS,
    val secondaryVisible: Boolean = false,
    val secondaryLineStyle: GridLineStyle = GridLineStyle(
        colorArgb = 0xFF9E9E9E,
        strokeWidthMm = 0.08,
        opacity = 0.45f,
    ),
    val labelsVisible: Boolean = false,
) {
    init {
        require(rows >= 1) { "rows must be >= 1" }
        require(columns >= 1) { "columns must be >= 1" }
        require(secondaryDivisions >= 1) { "secondaryDivisions must be >= 1" }
    }

    companion object {
        const val DEFAULT_ROWS: Int = 10
        const val DEFAULT_COLUMNS: Int = 10
        const val DEFAULT_SECONDARY_DIVISIONS: Int = 2

        fun default(): Grid = Grid()
    }
}
