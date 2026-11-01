package com.gridlab.app.core.model

import kotlinx.serialization.Serializable

/**
 * Reference image linked to a document.
 *
 * [relativePath] is relative to the app [android.content.Context.getFilesDir]
 * (e.g. `projects/<documentId>/original.jpg`).
 * Transform fields are added in 0.8.0; for now the image is drawn at a fixed fit.
 */
@Serializable
data class ReferenceImage(
    val relativePath: String,
)
