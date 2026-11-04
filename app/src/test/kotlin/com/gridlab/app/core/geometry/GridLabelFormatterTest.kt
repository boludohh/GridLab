package com.gridlab.app.core.geometry

import org.junit.Assert.assertEquals
import org.junit.Test

class GridLabelFormatterTest {

    @Test
    fun columnLabelsAreOneBased() {
        assertEquals("1", GridLabelFormatter.columnLabel(0))
        assertEquals("10", GridLabelFormatter.columnLabel(9))
    }

    @Test
    fun rowLabelsUseLetters() {
        assertEquals("A", GridLabelFormatter.rowLabel(0))
        assertEquals("Z", GridLabelFormatter.rowLabel(25))
        assertEquals("AA", GridLabelFormatter.rowLabel(26))
    }
}
