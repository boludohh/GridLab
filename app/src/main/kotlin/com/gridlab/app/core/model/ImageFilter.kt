package com.gridlab.app.core.model

import kotlinx.serialization.Serializable

/**
 * Non-destructive display/export filter applied to the reference image.
 * The file on disk is never modified.
 */
@Serializable
enum class ImageFilter {
    None,
    Grayscale,
    HighContrast,
}
