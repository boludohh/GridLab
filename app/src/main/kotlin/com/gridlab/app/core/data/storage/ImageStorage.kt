@file:Suppress("ImportOrdering")

package com.gridlab.app.core.data.storage

import android.content.Context
import android.net.Uri
import com.gridlab.app.core.model.ImageAvailability
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
        val dir = File(context.filesDir, "projects/" + documentId).apply { mkdirs() }
        val extension = fileExtensionFor(uri)
        val fileName = "original_${UUID.randomUUID()}.$extension"
        val dest = File(dir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IOException("Cannot open image uri: " + uri)
        "projects/" + documentId + "/" + dest.name
    }

    fun resolveAbsoluteFile(relativePath: String): File =
        File(context.filesDir, relativePath)

    fun exists(relativePath: String): Boolean =
        resolveAbsoluteFile(relativePath).isFile

    /**
     * Probes on-disk availability for a relative path stored in the document model.
     * Empty or unreadable files are treated as [ImageAvailability.Corrupt].
     */
    fun probe(relativePath: String?): ImageAvailability {
        if (relativePath.isNullOrBlank()) return ImageAvailability.None
        val file = resolveAbsoluteFile(relativePath)
        if (!file.isFile) return ImageAvailability.Missing
        if (file.length() <= 0L) return ImageAvailability.Corrupt
        return runCatching {
            file.inputStream().use { stream ->
                val header = ByteArray(16)
                val read = stream.read(header)
                if (read <= 0) ImageAvailability.Corrupt else ImageAvailability.Present
            }
        }.getOrDefault(ImageAvailability.Corrupt)
    }

    /** Deletes a single relative file if present. Safe if missing. */
    suspend fun deleteRelativeFile(relativePath: String) = withContext(Dispatchers.IO) {
        val file = resolveAbsoluteFile(relativePath)
        if (file.isFile) {
            file.delete()
        }
    }

    /** Deletes `projects/<documentId>/` recursively if present. Safe if missing. */
    suspend fun deleteProjectDir(documentId: String) = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "projects/" + documentId)
        if (dir.exists()) {
            dir.deleteRecursively()
        }
    }

    private fun fileExtensionFor(uri: Uri): String {
        val type = context.contentResolver.getType(uri)?.substringBefore(';')
        return when (type) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/jpeg", "image/jpg" -> "jpg"
            else -> "jpg"
        }
    }
}
