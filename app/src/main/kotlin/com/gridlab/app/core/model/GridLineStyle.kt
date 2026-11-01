package com.gridlab.app.core.model

import kotlinx.serialization.Serializable

/**
 * Visual style for grid lines.
 *
 * [colorArgb] is packed ARGB (e.g. 0xFF000000 for opaque black).
 * [strokeWidthMm] is the physical stroke width in millimeters.
 * [opacity] is in range 0.0–1.0 and multiplies the color alpha at draw time.
 */
@Serializable
data class GridLineStyle(
    val colorArgb: Long = 0xFF000000,
    val strokeWidthMm: Double = 0.2,
    val opacity: Float = 1.0f,
)
