package com.gridlab.app.core.geometry

import kotlin.math.floor
import kotlin.math.sqrt

/**
 * Pure-Kotlin helpers for export pixel size and memory-safe DPI.
 * Millimeters → pixels uses [MM_PER_INCH] (25.4).
 *
 * Rejects non-finite dimensions, non-positive sizes, invalid DPI, and values
 * that would overflow pixel/memory calculations or exceed bitmap limits.
 */
object ExportSizeCalculator {

    const val MM_PER_INCH: Double = 25.4
    /** Bytes per pixel for [android.graphics.Bitmap.Config.RGB_565]. */
    const val BYTES_PER_PIXEL_RGB_565: Int = 2
    /** Fraction of max heap allowed for the export bitmap. */
    const val MEMORY_SAFETY_FACTOR: Double = 0.40

    /**
     * Soft upper bound for a single bitmap edge (Android canvas/bitmap practical limit).
     * Values above this are rejected before [android.graphics.Bitmap.createBitmap].
     */
    const val MAX_BITMAP_EDGE_PX: Int = 16_384

    /** Soft upper bound for total pixels (width × height). */
    const val MAX_BITMAP_PIXELS: Long = 100_000_000L

    /** Reasonable maximum physical paper edge for custom sizes (mm). */
    const val MAX_PAPER_EDGE_MM: Double = 5_000.0

    val SUGGESTED_DPI: List<Int> = listOf(72, 100, 150, 200, 300)

    /**
     * Returns false when dimensions or DPI cannot produce a valid export bitmap.
     */
    fun isValidExportRequest(widthMm: Double, heightMm: Double, dpi: Int): Boolean {
        if (dpi < 1) return false
        if (!widthMm.isFinite() || !heightMm.isFinite()) return false
        if (widthMm <= 0.0 || heightMm <= 0.0) return false
        if (widthMm > MAX_PAPER_EDGE_MM || heightMm > MAX_PAPER_EDGE_MM) return false
        val w = pixelExtent(widthMm, dpi) ?: return false
        val h = pixelExtent(heightMm, dpi) ?: return false
        if (w > MAX_BITMAP_EDGE_PX || h > MAX_BITMAP_EDGE_PX) return false
        val pixels = checkedMultiply(w.toLong(), h.toLong()) ?: return false
        return pixels <= MAX_BITMAP_PIXELS
    }

    fun widthPx(widthMm: Double, dpi: Int): Int {
        require(widthMm.isFinite() && widthMm > 0.0 && dpi >= 1) {
            "Invalid widthMm/dpi for export"
        }
        return pixelExtent(widthMm, dpi)?.coerceIn(1, MAX_BITMAP_EDGE_PX)
            ?: error("widthMm/dpi produce an invalid pixel size")
    }

    fun heightPx(heightMm: Double, dpi: Int): Int {
        require(heightMm.isFinite() && heightMm > 0.0 && dpi >= 1) {
            "Invalid heightMm/dpi for export"
        }
        return pixelExtent(heightMm, dpi)?.coerceIn(1, MAX_BITMAP_EDGE_PX)
            ?: error("heightMm/dpi produce an invalid pixel size")
    }

    /**
     * Estimated RGB_565 bytes, or null if the product overflows.
     */
    fun estimatedBytesOrNull(widthMm: Double, heightMm: Double, dpi: Int): Long? {
        if (!isValidExportRequest(widthMm, heightMm, dpi)) return null
        val w = pixelExtent(widthMm, dpi)?.toLong() ?: return null
        val h = pixelExtent(heightMm, dpi)?.toLong() ?: return null
        val pixels = checkedMultiply(w, h) ?: return null
        return checkedMultiply(pixels, BYTES_PER_PIXEL_RGB_565.toLong())
    }

    fun estimatedBytes(widthMm: Double, heightMm: Double, dpi: Int): Long =
        estimatedBytesOrNull(widthMm, heightMm, dpi) ?: Long.MAX_VALUE

    /**
     * Highest integer DPI such that the RGB_565 bitmap fits in
     * [maxMemoryBytes] * [MEMORY_SAFETY_FACTOR], and respects bitmap edge/pixel caps.
     */
    fun maxSafeDpi(widthMm: Double, heightMm: Double, maxMemoryBytes: Long): Int {
        if (!widthMm.isFinite() || !heightMm.isFinite()) return 1
        if (widthMm <= 0.0 || heightMm <= 0.0 || maxMemoryBytes <= 0L) return 1
        if (widthMm > MAX_PAPER_EDGE_MM || heightMm > MAX_PAPER_EDGE_MM) return 1
        val budget = maxMemoryBytes.toDouble() * MEMORY_SAFETY_FACTOR
        if (!budget.isFinite() || budget <= 0.0) return 1
        val areaMm2 = widthMm * heightMm
        if (!areaMm2.isFinite() || areaMm2 <= 0.0) return 1
        val dpiSquared = budget * MM_PER_INCH * MM_PER_INCH /
            (areaMm2 * BYTES_PER_PIXEL_RGB_565)
        if (!dpiSquared.isFinite() || dpiSquared <= 1.0) return 1
        var dpi = floor(sqrt(dpiSquared)).toInt().coerceAtLeast(1)
        // Cap by edge/pixel limits as well as memory.
        while (dpi > 1 && !isDpiSafe(widthMm, heightMm, dpi, maxMemoryBytes)) {
            dpi /= 2
        }
        while (dpi > 1 && !isValidExportRequest(widthMm, heightMm, dpi)) {
            dpi /= 2
        }
        return dpi.coerceAtLeast(1)
    }

    fun isDpiSafe(widthMm: Double, heightMm: Double, dpi: Int, maxMemoryBytes: Long): Boolean {
        if (maxMemoryBytes <= 0L) return false
        if (!isValidExportRequest(widthMm, heightMm, dpi)) return false
        val bytes = estimatedBytesOrNull(widthMm, heightMm, dpi) ?: return false
        val budget = (maxMemoryBytes.toDouble() * MEMORY_SAFETY_FACTOR)
        if (!budget.isFinite()) return false
        return bytes <= budget.toLong()
    }

    private fun pixelExtent(mm: Double, dpi: Int): Int? {
        if (!mm.isFinite() || mm <= 0.0 || dpi < 1) return null
        val raw = floor(mm * dpi / MM_PER_INCH)
        if (!raw.isFinite() || raw <= 0.0 || raw > Int.MAX_VALUE.toDouble()) return null
        return raw.toInt().coerceAtLeast(1)
    }

    private fun checkedMultiply(a: Long, b: Long): Long? =
        try {
            Math.multiplyExact(a, b)
        } catch (_: ArithmeticException) {
            null
        }
}
