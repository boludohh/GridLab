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
    /** X positions of primary vertical lines, including left (0) and right edges. */
    val verticalLineXsMm: List<Double>,
    /** Y positions of primary horizontal lines, including top (0) and bottom edges. */
    val horizontalLineYsMm: List<Double>,
    /**
     * Secondary vertical lines only (excludes positions that coincide with primary lines).
     * Empty when secondary is off or divisions == 1.
     */
    val secondaryVerticalXsMm: List<Double> = emptyList(),
    /** Secondary horizontal lines only (excludes primary positions). */
    val secondaryHorizontalYsMm: List<Double> = emptyList(),
)

/**
 * Pure-Kotlin grid math. No Android dependencies.
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

        val secondaryV: List<Double>
        val secondaryH: List<Double>
        val div = grid.secondaryDivisions
        if (grid.secondaryVisible && div > 1) {
            secondaryV = buildList {
                for (c in 0 until cols) {
                    for (s in 1 until div) {
                        add(c * cellWidth + cellWidth * s / div)
                    }
                }
            }
            secondaryH = buildList {
                for (r in 0 until rows) {
                    for (s in 1 until div) {
                        add(r * cellHeight + cellHeight * s / div)
                    }
                }
            }
        } else {
            secondaryV = emptyList()
            secondaryH = emptyList()
        }

        return GridLayout(
            printableWidthMm = width,
            printableHeightMm = height,
            cellWidthMm = cellWidth,
            cellHeightMm = cellHeight,
            verticalLineXsMm = vertical,
            horizontalLineYsMm = horizontal,
            secondaryVerticalXsMm = secondaryV,
            secondaryHorizontalYsMm = secondaryH,
        )
    }
}
