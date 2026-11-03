package com.gridlab.app.core.geometry

import kotlin.math.floor
import kotlin.math.sqrt

/**
 * Pure-Kotlin helpers for export pixel size and memory-safe DPI.
 * Millimeters → pixels uses [MM_PER_INCH] (25.4).
 */
object ExportSizeCalculator {

    const val MM_PER_INCH: Double = 25.4
    /** Bytes per pixel for [android.graphics.Bitmap.Config.RGB_565]. */
    const val BYTES_PER_PIXEL_RGB_565: Int = 2
    /** Fraction of max heap allowed for the export bitmap. */
    const val MEMORY_SAFETY_FACTOR: Double = 0.40

    val SUGGESTED_DPI: List<Int> = listOf(72, 100, 150, 200, 300)

    fun widthPx(widthMm: Double, dpi: Int): Int =
        floor(widthMm * dpi / MM_PER_INCH).toInt().coerceAtLeast(1)

    fun heightPx(heightMm: Double, dpi: Int): Int =
        floor(heightMm * dpi / MM_PER_INCH).toInt().coerceAtLeast(1)

    fun estimatedBytes(widthMm: Double, heightMm: Double, dpi: Int): Long {
        val w = widthPx(widthMm, dpi).toLong()
        val h = heightPx(heightMm, dpi).toLong()
        return w * h * BYTES_PER_PIXEL_RGB_565
    }

    /**
     * Highest integer DPI such that the RGB_565 bitmap fits in
     * [maxMemoryBytes] * [MEMORY_SAFETY_FACTOR].
     */
    fun maxSafeDpi(widthMm: Double, heightMm: Double, maxMemoryBytes: Long): Int {
        if (widthMm <= 0.0 || heightMm <= 0.0 || maxMemoryBytes <= 0L) return 1
        val budget = maxMemoryBytes.toDouble() * MEMORY_SAFETY_FACTOR
        val areaMm2 = widthMm * heightMm
        // dpi^2 * areaMm2 * bpp / 25.4^2 <= budget
        val dpiSquared = budget * MM_PER_INCH * MM_PER_INCH /
            (areaMm2 * BYTES_PER_PIXEL_RGB_565)
        if (dpiSquared <= 1.0) return 1
        return floor(sqrt(dpiSquared)).toInt().coerceAtLeast(1)
    }

    fun isDpiSafe(widthMm: Double, heightMm: Double, dpi: Int, maxMemoryBytes: Long): Boolean =
        estimatedBytes(widthMm, heightMm, dpi) <=
            (maxMemoryBytes.toDouble() * MEMORY_SAFETY_FACTOR).toLong()
}
