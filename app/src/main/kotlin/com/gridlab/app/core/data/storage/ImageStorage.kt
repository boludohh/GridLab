package com.gridlab.app.core.data.storage

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Copies reference images into a private per-document directory under filesDir.
 */
@Singleton
class ImageStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Copies [uri] into `projects/<documentId>/` and returns the path relative to filesDir.
     */
    suspend fun copyFromUri(documentId: String, uri: Uri): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "projects/$documentId").apply { mkdirs() }
        val extension = guessExtension(uri)
        val dest = File(dir, "original_$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IOException("Cannot open image uri: $uri")
        "projects/\( documentId/ \){dest.name}"
    }

    fun resolveAbsoluteFile(relativePath: String): File =
        File(context.filesDir, relativePath)

    fun exists(relativePath: String): Boolean =
        resolveAbsoluteFile(relativePath).isFile

    private fun guessExtension(uri: Uri): String {
        val type = context.contentResolver.getType(uri) ?: return "${UUID.randomUUID()}.jpg"
        return when (type) {
            "image/png" -> "${UUID.randomUUID()}.png"
            "image/webp" -> "${UUID.randomUUID()}.webp"
            "image/jpeg", "image/jpg" -> "${UUID.randomUUID()}.jpg"
            else -> "${UUID.randomUUID()}.jpg"
        }
    }
}