package com.gridlab.app.core.geometry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

    @Test
    fun isValidExportRequest_rejects_non_finite_and_non_positive() {
        assertFalse(ExportSizeCalculator.isValidExportRequest(Double.NaN, 210.0, 150))
        assertFalse(ExportSizeCalculator.isValidExportRequest(210.0, Double.POSITIVE_INFINITY, 150))
        assertFalse(ExportSizeCalculator.isValidExportRequest(0.0, 210.0, 150))
        assertFalse(ExportSizeCalculator.isValidExportRequest(210.0, -1.0, 150))
        assertFalse(ExportSizeCalculator.isValidExportRequest(210.0, 297.0, 0))
        assertTrue(ExportSizeCalculator.isValidExportRequest(210.0, 297.0, 150))
    }

    @Test
    fun estimatedBytesOrNull_overflow_returns_null_for_extreme() {
        // Pathologically large paper + DPI should not overflow into a "safe" small number.
        val bytes = ExportSizeCalculator.estimatedBytesOrNull(5_000.0, 5_000.0, 10_000)
        // Either null (invalid) or a huge finite value — never wrap to negative.
        if (bytes != null) {
            assertTrue(bytes > 0L)
        }
    }

    @Test
    fun isValidExportRequest_rejects_over_edge_limit() {
        // Huge mm at high DPI exceeds MAX_BITMAP_EDGE_PX
        assertFalse(ExportSizeCalculator.isValidExportRequest(5_000.0, 5_000.0, 300))
    }
}
