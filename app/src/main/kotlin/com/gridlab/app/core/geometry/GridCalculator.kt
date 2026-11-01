package com.gridlab.app.core.geometry

import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.Paper

/**
 * Resolved grid geometry in millimeters, relative to the printable-area origin
 * (top-left of the area inside margins).
 */
data class GridLayout(
    val printableWidthMm: Double,
    val printableHeightMm: Double,
    val cellWidthMm: Double,
    val cellHeightMm: Double,
    /** X positions of vertical lines, including left (0) and right edges. */
    val verticalLineXsMm: List<Double>,
    /** Y positions of horizontal lines, including top (0) and bottom edges. */
    val horizontalLineYsMm: List<Double>,
)

/**
 * Pure-Kotlin grid math. No Android dependencies.
 *
 * Division uses floating point so non-integer remainders are distributed
 * evenly across cells (last line lands exactly on the printable edge).
 */
object GridCalculator {

    fun calculate(paper: Paper, grid: Grid): GridLayout {
        val width = paper.printableWidthMm
        val height = paper.printableHeightMm
        val cols = grid.columns
        val rows = grid.rows

        val cellWidth = width / cols
        val cellHeight = height / rows

        val vertical = buildList(cols + 1) {
            for (i in 0..cols) {
                add(i * cellWidth)
            }
        }
        val horizontal = buildList(rows + 1) {
            for (i in 0..rows) {
                add(i * cellHeight)
            }
        }

        return GridLayout(
            printableWidthMm = width,
            printableHeightMm = height,
            cellWidthMm = cellWidth,
            cellHeightMm = cellHeight,
            verticalLineXsMm = vertical,
            horizontalLineYsMm = horizontal,
        )
    }
}
