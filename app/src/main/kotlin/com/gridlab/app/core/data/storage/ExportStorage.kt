package com.gridlab.app.core.data.storage

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.gridlab.app.core.model.ExportFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves export destinations and manages file creation for exported documents.
 *
 * - API 29+ (Android 10+): Uses MediaStore with RELATIVE_PATH for automatic indexing
 *   and folder creation. No permission required.
 * - API 27-28 (Android 8.1-9): Uses direct File access with mkdirs() for folder
 *   recreation. Requires WRITE_EXTERNAL_STORAGE permission (requested at runtime).
 *
 * File naming: dd-MM-yy-HH-mm-GridLab.ext (date/time from system clock at export moment).
 *
 * Images (JPG/PNG) → Pictures/GridLab (indexed in gallery)
 * PDF → Documents/GridLab (indexed in file managers)
 */
@Singleton
class ExportStorage @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {

    /**
     * Opens an OutputStream for writing the exported file. Returns the stream and
     * a human-readable location description (e.g., "Pictures/GridLab/09-11-26-23-40-GridLab.jpg").
     *
     * Caller is responsible for closing the stream.
     */
    suspend fun openOutputStream(
        format: ExportFormat,
    ): Pair<OutputStream, String> = withContext(Dispatchers.IO) {
        val fileName = generateFileName(format)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            openMediaStoreStream(format, fileName)
        } else {
            openFileStream(format, fileName)
        }
    }

    /**
     * Checks if the app needs to request storage permission (only on API 27-28).
     */
    fun requiresStoragePermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    /**
     * Checks if the app has storage permission (always true on API 29+).
     */
    fun hasStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return true
        return appContext.checkSelfPermission(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun generateFileName(format: ExportFormat): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yy-HH-mm")
        val timestamp = now.format(formatter)
        val extension = when (format) {
            ExportFormat.Jpeg -> "jpg"
            ExportFormat.Png -> "png"
            ExportFormat.Pdf -> "pdf"
        }
        var baseName = "$timestamp-GridLab"
        var counter = 1
        // Ensure uniqueness by appending counter if file exists
        while (fileNameExists(baseName, extension)) {
            baseName = "$timestamp-GridLab-$counter"
            counter++
        }
        return "$baseName.$extension"
    }

    private fun fileNameExists(baseName: String, extension: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // MediaStore handles uniqueness automatically; assume no collision
            return false
        } else {
            val dir = getLegacyDirectory(
                if (extension == "pdf") ExportFormat.Pdf else ExportFormat.Jpeg,
            )
            return File(dir, "$baseName.$extension").exists()
        }
    }

    private fun openMediaStoreStream(
        format: ExportFormat,
        fileName: String,
    ): Pair<OutputStream, String> {
        val resolver = appContext.contentResolver
        val (collection, mimeType, relativePath) = when (format) {
            ExportFormat.Jpeg -> Triple(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/jpeg",
                "Pictures/GridLab",
            )
            ExportFormat.Png -> Triple(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/png",
                "Pictures/GridLab",
            )
            ExportFormat.Pdf -> Triple(
                MediaStore.Files.getContentUri("external"),
                "application/pdf",
                "Documents/GridLab",
            )
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values)
            ?: throw IllegalStateException("Failed to insert MediaStore entry")
        val stream = resolver.openOutputStream(uri)
            ?: throw IllegalStateException("Failed to open MediaStore stream")
        // Mark as not pending after successful write (caller closes stream first)
        return StreamWithFinalizer(stream, uri, resolver) to "$relativePath/$fileName"
    }

    private fun openFileStream(
        format: ExportFormat,
        fileName: String,
    ): Pair<OutputStream, String> {
        val dir = getLegacyDirectory(format)
        dir.mkdirs() // Recreate if user deleted it
        val file = File(dir, fileName)
        val stream = FileOutputStream(file)
        val relativePath = when (format) {
            ExportFormat.Jpeg, ExportFormat.Png -> "Pictures/GridLab"
            ExportFormat.Pdf -> "Documents/GridLab"
        }
        return StreamWithMediaScan(stream, file.absolutePath, appContext) to "$relativePath/$fileName"
    }

    private fun getLegacyDirectory(format: ExportFormat): File {
        val baseDir = when (format) {
            ExportFormat.Jpeg, ExportFormat.Png ->
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            ExportFormat.Pdf ->
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        }
        return File(baseDir, "GridLab")
    }

    /**
     * Wrapper that marks MediaStore entry as not pending when closed.
     */
    private class StreamWithFinalizer(
        private val delegate: OutputStream,
        private val uri: android.net.Uri,
        private val resolver: android.content.ContentResolver,
    ) : OutputStream() {
        override fun write(b: Int) = delegate.write(b)
        override fun write(b: ByteArray?) = delegate.write(b)
        override fun write(b: ByteArray?, off: Int, len: Int) = delegate.write(b, off, len)
        override fun flush() = delegate.flush()
        override fun close() {
            delegate.close()
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            resolver.update(uri, values, null, null)
        }
    }

    /**
     * Wrapper that triggers media scan after close (for API 27-28 gallery indexing).
     */
    private class StreamWithMediaScan(
        private val delegate: OutputStream,
        private val filePath: String,
        private val context: Context,
    ) : OutputStream() {
        override fun write(b: Int) = delegate.write(b)
        override fun write(b: ByteArray?) = delegate.write(b)
        override fun write(b: ByteArray?, off: Int, len: Int) = delegate.write(b, off, len)
        override fun flush() = delegate.flush()
        override fun close() {
            delegate.close()
            MediaScannerConnection.scanFile(
                context,
                arrayOf(filePath),
                null,
                null,
            )
        }
    }
}