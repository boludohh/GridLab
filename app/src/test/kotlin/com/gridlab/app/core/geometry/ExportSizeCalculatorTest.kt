package com.gridlab.app.core.geometry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportSizeCalculatorTest {

    @Test
    fun widthPx_a4_at_150dpi() {
        // A4 width 210mm at 150 DPI ≈ 1240 px
        val px = ExportSizeCalculator.widthPx(210.0, 150)
        assertEquals(1240, px)
    }

    @Test
    fun heightPx_a4_at_150dpi() {
        val px = ExportSizeCalculator.heightPx(297.0, 150)
        assertEquals(1755, px)
    }

    @Test
    fun maxSafeDpi_rejects_huge_request() {
        // Tiny heap: 16 MB
        val max = ExportSizeCalculator.maxSafeDpi(841.0, 1189.0, 16L * 1024L * 1024L)
        assertTrue(max < 300)
        assertTrue(max >= 1)
        assertFalse(
            ExportSizeCalculator.isDpiSafe(841.0, 1189.0, 300, 16L * 1024L * 1024L),
        )
    }

    @Test
    fun isDpiSafe_small_paper_ok() {
        assertTrue(
            ExportSizeCalculator.isDpiSafe(148.0, 210.0, 150, 256L * 1024L * 1024L),
        )
    }
}
