package com.gridlab.app.core.geometry

import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.PaperOrientation
import com.gridlab.app.core.model.StandardPaperFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GridCalculatorTest {

    @Test
    fun `A4 portrait 10x10 produces expected cell size`() {
        val paper = Paper.fromStandard(StandardPaperFormat.A4)
        val grid = Grid(rows = 10, columns = 10)
        val layout = GridCalculator.calculate(paper, grid)

        assertEquals(21.0, layout.cellWidthMm, 1e-9)
        assertEquals(29.7, layout.cellHeightMm, 1e-9)
        assertEquals(11, layout.verticalLineXsMm.size)
        assertEquals(11, layout.horizontalLineYsMm.size)
        assertEquals(0.0, layout.verticalLineXsMm.first(), 1e-9)
        assertEquals(paper.printableWidthMm, layout.verticalLineXsMm.last(), 1e-9)
        assertEquals(paper.printableHeightMm, layout.horizontalLineYsMm.last(), 1e-9)
    }

    @Test
    fun `non divisible column count still ends on printable edge`() {
        val paper = Paper.fromStandard(StandardPaperFormat.A4)
        val grid = Grid(rows = 3, columns = 7)
        val layout = GridCalculator.calculate(paper, grid)

        assertEquals(8, layout.verticalLineXsMm.size)
        assertEquals(0.0, layout.verticalLineXsMm.first(), 1e-9)
        assertEquals(paper.printableWidthMm, layout.verticalLineXsMm.last(), 1e-9)
        // Spacing is uniform
        val step = layout.cellWidthMm
        for (i in 1 until layout.verticalLineXsMm.size) {
            assertEquals(step, layout.verticalLineXsMm[i] - layout.verticalLineXsMm[i - 1], 1e-9)
        }
    }

    @Test
    fun `margins reduce printable area used by grid`() {
        val paper = Paper.fromStandard(
            format = StandardPaperFormat.A4,
            marginLeftMm = 10.0,
            marginRightMm = 10.0,
            marginTopMm = 15.0,
            marginBottomMm = 15.0,
        )
        val grid = Grid(rows = 5, columns = 5)
        val layout = GridCalculator.calculate(paper, grid)

        assertEquals(190.0, layout.printableWidthMm, 1e-9)
        assertEquals(267.0, layout.printableHeightMm, 1e-9)
        assertEquals(38.0, layout.cellWidthMm, 1e-9)
        assertEquals(53.4, layout.cellHeightMm, 1e-9)
    }

    @Test
    fun `single cell grid has only outer lines`() {
        val paper = Paper.fromStandard(StandardPaperFormat.A5)
        val grid = Grid(rows = 1, columns = 1)
        val layout = GridCalculator.calculate(paper, grid)

        assertEquals(2, layout.verticalLineXsMm.size)
        assertEquals(2, layout.horizontalLineYsMm.size)
        assertEquals(paper.printableWidthMm, layout.cellWidthMm, 1e-9)
        assertEquals(paper.printableHeightMm, layout.cellHeightMm, 1e-9)
    }

    @Test
    fun `landscape orientation uses swapped printable size`() {
        val paper = Paper.fromStandard(StandardPaperFormat.A4, PaperOrientation.Landscape)
        val grid = Grid(rows = 2, columns = 4)
        val layout = GridCalculator.calculate(paper, grid)

        assertTrue(layout.printableWidthMm > layout.printableHeightMm)
        assertEquals(paper.printableWidthMm / 4.0, layout.cellWidthMm, 1e-9)
        assertEquals(paper.printableHeightMm / 2.0, layout.cellHeightMm, 1e-9)
    }
}
