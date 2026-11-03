package com.gridlab.app.core.render

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.gridlab.app.core.geometry.ExportSizeCalculator
import com.gridlab.app.core.geometry.GridCalculator
import com.gridlab.app.core.model.Document
import com.gridlab.app.core.model.ReferenceImage
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Renders a [Document] to an offscreen [Bitmap] and writes JPEG files.
 * Uses [Bitmap.Config.RGB_565] for export memory efficiency.
 */
@Singleton
class DocumentRenderer @Inject constructor() {

    /**
     * Renders [document] at [dpi]. [imageFile] is the resolved reference image file, or null.
     */
    fun renderToBitmap(
        document: Document,
        dpi: Int,
        imageFile: File?,
    ): Bitmap {
        val paper = document.paper
        val widthPx = ExportSizeCalculator.widthPx(paper.widthMm, dpi)
        val heightPx = ExportSizeCalculator.heightPx(paper.heightMm, dpi)
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val pxPerMm = dpi / ExportSizeCalculator.MM_PER_INCH
        val marginLeft = (paper.marginLeftMm * pxPerMm).toFloat()
        val marginTop = (paper.marginTopMm * pxPerMm).toFloat()
        val contentW = (paper.printableWidthMm * pxPerMm).toFloat()
        val contentH = (paper.printableHeightMm * pxPerMm).toFloat()

        val ref = document.referenceImage
        if (ref != null && ref.visible && imageFile != null && imageFile.isFile &&
            contentW > 0f && contentH > 0f
        ) {
            drawReferenceImage(
                canvas = canvas,
                image = ref,
                file = imageFile,
                marginLeft = marginLeft,
                marginTop = marginTop,
                contentW = contentW,
                contentH = contentH,
                pxPerMm = pxPerMm,
            )
        }

        if (document.grid.visible) {
            drawGrid(canvas, document, marginLeft, marginTop, pxPerMm)
        }

        return bitmap
    }

    /**
     * Renders and compresses JPEG into [outputFile]. Returns [outputFile] on success.
     */
    fun exportJpeg(
        document: Document,
        dpi: Int,
        imageFile: File?,
        outputFile: File,
        quality: Int = 90,
    ): File {
        val bitmap = renderToBitmap(document, dpi, imageFile)
        try {
            outputFile.parentFile?.mkdirs()
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(50, 100), out)
            }
        } finally {
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        return outputFile
    }

    private fun drawReferenceImage(
        canvas: Canvas,
        image: ReferenceImage,
        file: File,
        marginLeft: Float,
        marginTop: Float,
        contentW: Float,
        contentH: Float,
        pxPerMm: Double,
    ) {
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
        val srcW = boundsOptions.outWidth.coerceAtLeast(1)
        val srcH = boundsOptions.outHeight.coerceAtLeast(1)

        val cropLeft = (srcW * image.cropLeft).toInt().coerceIn(0, srcW - 1)
        val cropTop = (srcH * image.cropTop).toInt().coerceIn(0, srcH - 1)
        val cropRight = (srcW * image.cropRight).toInt().coerceIn(cropLeft + 1, srcW)
        val cropBottom = (srcH * image.cropBottom).toInt().coerceIn(cropTop + 1, srcH)
        val cropW = (cropRight - cropLeft).coerceAtLeast(1)
        val cropH = (cropBottom - cropTop).coerceAtLeast(1)

        val decodeOptions = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
            inJustDecodeBounds = false
            inSampleSize = calculateInSampleSize(cropW, cropH, contentW.toInt(), contentH.toInt())
        }
        val decoded = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return
        try {
            val sample = decodeOptions.inSampleSize.coerceAtLeast(1)
            val srcRect = Rect(
                (cropLeft / sample).coerceIn(0, decoded.width),
                (cropTop / sample).coerceIn(0, decoded.height),
                (cropRight / sample).coerceIn(0, decoded.width),
                (cropBottom / sample).coerceIn(0, decoded.height),
            )
            if (srcRect.width() <= 0 || srcRect.height() <= 0) return

            val fit = minOf(
                contentW / srcRect.width().toFloat(),
                contentH / srcRect.height().toFloat(),
            )
            val drawW = srcRect.width() * fit * image.scale.toFloat()
            val drawH = srcRect.height() * fit * image.scale.toFloat()
            val baseLeft = marginLeft + (contentW - srcRect.width() * fit) / 2f
            val baseTop = marginTop + (contentH - srcRect.height() * fit) / 2f
            val offsetXPx = (image.offsetXMm * pxPerMm).toFloat()
            val offsetYPx = (image.offsetYMm * pxPerMm).toFloat()
            // Center of fitted image moves with scale around center
            val centerX = baseLeft + srcRect.width() * fit / 2f + offsetXPx
            val centerY = baseTop + srcRect.height() * fit / 2f + offsetYPx
            val dst = RectF(
                centerX - drawW / 2f,
                centerY - drawH / 2f,
                centerX + drawW / 2f,
                centerY + drawH / 2f,
            )
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                alpha = (image.opacity * 255).toInt().coerceIn(0, 255)
            }
            canvas.drawBitmap(decoded, srcRect, dst, paint)
        } finally {
            if (!decoded.isRecycled) {
                decoded.recycle()
            }
        }
    }

    private fun drawGrid(
        canvas: Canvas,
        document: Document,
        marginLeft: Float,
        marginTop: Float,
        pxPerMm: Double,
    ) {
        val layout = GridCalculator.calculate(document.paper, document.grid)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = document.grid.lineStyle.colorArgb.toInt()
            alpha = (document.grid.lineStyle.opacity * 255).toInt().coerceIn(0, 255)
            strokeWidth = (document.grid.lineStyle.strokeWidthMm * pxPerMm).toFloat().coerceAtLeast(1f)
            style = Paint.Style.STROKE
        }
        val contentH = (layout.printableHeightMm * pxPerMm).toFloat()
        val contentW = (layout.printableWidthMm * pxPerMm).toFloat()
        for (xMm in layout.verticalLineXsMm) {
            val x = marginLeft + (xMm * pxPerMm).toFloat()
            canvas.drawLine(x, marginTop, x, marginTop + contentH, paint)
        }
        for (yMm in layout.horizontalLineYsMm) {
            val y = marginTop + (yMm * pxPerMm).toFloat()
            canvas.drawLine(marginLeft, y, marginLeft + contentW, y, paint)
        }
    }

    private fun calculateInSampleSize(srcW: Int, srcH: Int, reqW: Int, reqH: Int): Int {
        var inSampleSize = 1
        val safeReqW = reqW.coerceAtLeast(1)
        val safeReqH = reqH.coerceAtLeast(1)
        if (srcH > safeReqH || srcW > safeReqW) {
            val halfH = srcH / 2
            val halfW = srcW / 2
            while (halfH / inSampleSize >= safeReqH && halfW / inSampleSize >= safeReqW) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }
}
