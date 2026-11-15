package com.gridlab.app.core.model

/**
 * Availability of the on-disk reference image for a document.
 * Checked when opening a project; independent of transform/crop fields.
 */
enum class ImageAvailability {
    /** No reference image linked in the document model. */
    None,
    /** File exists and Android can decode positive image bounds. */
    Present,
    /** Model points to a path but the file is absent. */
    Missing,
    /** File exists but is empty, unreadable, or not a decodable image. */
    Corrupt,
}
