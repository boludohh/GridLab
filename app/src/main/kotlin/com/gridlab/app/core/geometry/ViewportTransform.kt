package com.gridlab.app.core.geometry

import kotlin.math.hypot

/**
 * Maps between screen pixels and document millimeters for the paper preview.
 *
 * [originXPx] / [originYPx] are the screen coordinates of the paper's top-left corner.
 * [scale] is screen-pixels per millimeter (paper size in px / paper size in mm).
 *
 * Pure Kotlin — no Android dependencies.
 */
data class ViewportTransform(
    val originXPx: Float,
    val originYPx: Float,
    val scale: Float,
) {
    init {
        require(scale > 0f) { "scale must be > 0" }
    }

    val mmPerPx: Double
        get() = 1.0 / scale.toDouble()

    /** Converts a screen point to document coordinates (mm from paper top-left). */
    fun screenToDocumentMm(xPx: Float, yPx: Float): Pair<Double, Double> {
        val xMm = (xPx - originXPx) * mmPerPx
        val yMm = (yPx - originYPx) * mmPerPx
        return xMm to yMm
    }

    /** Converts a document point (mm from paper top-left) to screen pixels. */
    fun documentToScreenPx(xMm: Double, yMm: Double): Pair<Float, Float> {
        val xPx = originXPx + (xMm * scale).toFloat()
        val yPx = originYPx + (yMm * scale).toFloat()
        return xPx to yPx
    }

    companion object {
        /** Euclidean distance in millimeters between two document points. */
        fun distanceMm(a: Pair<Double, Double>, b: Pair<Double, Double>): Double =
            hypot(b.first - a.first, b.second - a.second)
    }
}
