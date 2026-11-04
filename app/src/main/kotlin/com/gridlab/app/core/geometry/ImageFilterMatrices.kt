package com.gridlab.app.core.geometry

import com.gridlab.app.core.model.ImageFilter

/**
 * Color-matrix coefficients for [ImageFilter] (4x5 row-major, Android/Compose layout).
 */
object ImageFilterMatrices {

    fun matrix(filter: ImageFilter): FloatArray? = when (filter) {
        ImageFilter.None -> null
        ImageFilter.Grayscale -> floatArrayOf(
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
        ImageFilter.HighContrast -> floatArrayOf(
            1.5f, 0f, 0f, 0f, -0.25f * 255f,
            0f, 1.5f, 0f, 0f, -0.25f * 255f,
            0f, 0f, 1.5f, 0f, -0.25f * 255f,
            0f, 0f, 0f, 1f, 0f,
        )
    }
}
