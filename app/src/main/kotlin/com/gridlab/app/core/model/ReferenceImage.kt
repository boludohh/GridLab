package com.gridlab.app.core.model

import kotlinx.serialization.Serializable

/**
 * Reference image linked to a document.
 *
 * [relativePath] is relative to the app [android.content.Context.getFilesDir]
 * (e.g. `projects/<documentId>/original.jpg`).
 *
 * Transform is relative to the initial "fit" placement inside the printable area:
 * - [offsetXMm] / [offsetYMm]: extra translation in millimeters from the fitted top-left.
 * - [scale]: multiplier on top of the fitted size (1.0 = fitted).
 * - [rotationDegrees]: prepared for a future UI; not exposed in 0.8.0.
 */
@Serializable
data class ReferenceImage(
    val relativePath: String,
    val offsetXMm: Double = 0.0,
    val offsetYMm: Double = 0.0,
    val scale: Double = 1.0,
    val rotationDegrees: Float = 0f,
    val opacity: Float = 1f,
    val locked: Boolean = false,
    val visible: Boolean = true,
) {
    init {
        require(scale > 0.0) { "scale must be > 0" }
        require(opacity in 0f..1f) { "opacity must be in 0..1" }
    }

    companion object {
        fun withPath(relativePath: String): ReferenceImage =
            ReferenceImage(relativePath = relativePath)
    }
}
