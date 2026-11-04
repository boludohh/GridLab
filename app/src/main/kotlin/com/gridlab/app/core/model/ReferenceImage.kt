package com.gridlab.app.core.model

import kotlinx.serialization.Serializable

/**
 * Reference image linked to a document.
 *
 * [relativePath] is relative to the app filesDir
 * (e.g. `projects/<documentId>/original.jpg`).
 *
 * Transform is relative to the initial "fit" placement inside the printable area.
 * [filter] is a non-destructive display/export effect; the file is never modified.
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
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 1f,
    val cropBottom: Float = 1f,
    val filter: ImageFilter = ImageFilter.None,
) {
    init {
        require(scale > 0.0) { "scale must be > 0" }
        require(opacity in 0f..1f) { "opacity must be in 0..1" }
        require(cropLeft in 0f..1f && cropTop in 0f..1f) { "crop left/top must be in 0..1" }
        require(cropRight in 0f..1f && cropBottom in 0f..1f) { "crop right/bottom must be in 0..1" }
        require(cropLeft < cropRight && cropTop < cropBottom) { "crop edges must form a valid rect" }
    }

    val hasCrop: Boolean
        get() = cropLeft > 0f || cropTop > 0f || cropRight < 1f || cropBottom < 1f

    companion object {
        fun withPath(relativePath: String): ReferenceImage =
            ReferenceImage(relativePath = relativePath)
    }
}
