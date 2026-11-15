package com.gridlab.app.feature.editor.export

import com.gridlab.app.core.model.ExportFormat
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Destination for a single export write. Call [commit] only after a full successful write;
 * call [abort] on any failure so partial/pending entries are not published.
 */
class ExportSink internal constructor(
    val stream: OutputStream,
    val displayPath: String,
    private val onCommit: () -> Unit,
    private val onAbort: () -> Unit,
) {
    private var finished = false

    fun commit() {
        if (finished) return
        finished = true
        try {
            stream.flush()
            stream.close()
        } finally {
            onCommit()
        }
    }

    fun abort() {
        if (finished) return
        finished = true
        try {
            runCatching { stream.close() }
        } finally {
            onAbort()
        }
    }
}

/**
 * Creates public export files under Pictures/GridLab or Documents/GridLab.
 */
@Singleton
class ExportStorage @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {

    /**
     * Opens a sink for [format]. Write fully to [ExportSink.stream], then [ExportSink.commit].
     * On any failure, call [ExportSink.abort] instead of commit.
     */
    fun openSink(format: ExportFormat): ExportSink {
        val fileName = generateFileName(format)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            openMediaStoreSink(format, fileName)
        } else {
            openLegacySink(format, fileName)
        }
    }

    /** @deprecated Prefer [openSink] with explicit commit/abort. */
    fun openOutputStream(format: ExportFormat): Pair<OutputStream, String> {
        val sink = openSink(format)
        // Legacy API: closing stream commits — kept for any remaining callers.
        return object : OutputStream() {
            override fun write(b: Int) = sink.stream.write(b)
            override fun write(b: ByteArray?) = sink.stream.write(b)
            override fun write(b: ByteArray?, off: Int, len: Int) = sink.stream.write(b, off, len)
            override fun flush() = sink.stream.flush()
            override fun close() = sink.commit()
        } to sink.displayPath
    }

    fun requiresStoragePermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

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
        while (fileNameExists(baseName, extension, format)) {
            baseName = "$timestamp-GridLab-$counter"
            counter++
        }
        return "$baseName.$extension"
    }

    private fun fileNameExists(baseName: String, extension: String, format: ExportFormat): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return false
        val dir = getLegacyDirectory(format)
        return File(dir, "$baseName.$extension").exists()
    }

    private fun openMediaStoreSink(format: ExportFormat, fileName: String): ExportSink {
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
            ?: throw IOException("MediaStore insert failed for $fileName")
        val stream = resolver.openOutputStream(uri)
        if (stream == null) {
            resolver.delete(uri, null, null)
            throw IOException("Cannot open MediaStore output stream for $uri")
        }
        val display = "$relativePath/$fileName"
        return ExportSink(
            stream = stream,
            displayPath = display,
            onCommit = {
                val done = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }
                resolver.update(uri, done, null, null)
            },
            onAbort = {
                runCatching { resolver.delete(uri, null, null) }
            },
        )
    }

    private fun openLegacySink(format: ExportFormat, fileName: String): ExportSink {
        val dir = getLegacyDirectory(format)
        dir.mkdirs()
        val file = File(dir, fileName)
        val stream = FileOutputStream(file)
        val relativePath = when (format) {
            ExportFormat.Jpeg, ExportFormat.Png -> "Pictures/GridLab"
            ExportFormat.Pdf -> "Documents/GridLab"
        }
        return ExportSink(
            stream = stream,
            displayPath = "$relativePath/$fileName",
            onCommit = {
                MediaScannerConnection.scanFile(
                    appContext,
                    arrayOf(file.absolutePath),
                    null,
                    null,
                )
            },
            onAbort = {
                runCatching { if (file.exists()) file.delete() }
            },
        )
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
}
