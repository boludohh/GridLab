package com.gridlab.app.core.render

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Rect
import android.graphics.RectF
import com.gridlab.app.core.geometry.ExportSizeCalculator
import com.gridlab.app.core.geometry.ReferenceImagePlacement
import com.gridlab.app.core.geometry.GridLabelFormatter
import com.gridlab.app.core.geometry.ImageFilterMatrices
import com.gridlab.app.core.geometry.GridCalculator
import com.gridlab.app.core.model.Document
import com.gridlab.app.core.model.ReferenceImage
import java.io.File
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Renders a [Document] to an offscreen [Bitmap] and writes to OutputStream.
 * Uses [Bitmap.Config.RGB_565] for export memory efficiency.
 */
@Singleton
class DocumentRenderer @Inject constructor() {

    companion object {
        /** PDF points per millimeter (72 pt/in ÷ 25.4 mm/in). */
        private const val PDF_POINTS_PER_MM = 72.0 / 25.4
    }

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
        canvas.drawColor(document.paper.colorArgb.toInt())

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
            if (document.grid.labelsVisible) {
                drawGridLabels(canvas, document, marginLeft, marginTop, pxPerMm)
            }
        }

        return bitmap
    }

    /**
     * Renders and compresses JPEG into [outputStream].
     */
    fun exportJpeg(
        document: Document,
        dpi: Int,
        imageFile: File?,
        outputStream: OutputStream,
        quality: Int = 90,
    ) {
        val bitmap = renderToBitmap(document, dpi, imageFile)
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(50, 100), outputStream)
        } finally {
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    /**
     * Renders and compresses PNG into [outputStream].
     */
    fun exportPng(
        document: Document,
        dpi: Int,
        imageFile: File?,
        outputStream: OutputStream,
    ) {
        val bitmap = renderToBitmap(document, dpi, imageFile)
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        } finally {
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    /**
     * Renders the document at [dpi] and writes a single-page PDF into [outputStream].
     */
    fun exportPdf(
        document: Document,
        dpi: Int,
        imageFile: File?,
        outputStream: OutputStream,
    ) {
        val bitmap = renderToBitmap(document, dpi, imageFile)
        val pdf = PdfDocument()
        try {
            // Page size is physical paper in PDF points (1/72"), not bitmap pixels.
            val paper = document.paper
            val pageWidthPt = (paper.widthMm * PDF_POINTS_PER_MM).toInt().coerceAtLeast(1)
            val pageHeightPt = (paper.heightMm * PDF_POINTS_PER_MM).toInt().coerceAtLeast(1)
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, 1).create()
            val page = pdf.startPage(pageInfo)
            val dest = Rect(0, 0, pageWidthPt, pageHeightPt)
            page.canvas.drawBitmap(bitmap, null, dest, null)
            pdf.finishPage(page)
            pdf.writeTo(outputStream)
        } finally {
            pdf.close()
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
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

        val crop = ReferenceImagePlacement.sourceCrop(
            srcW = srcW,
            srcH = srcH,
            cropLeft = image.cropLeft,
            cropTop = image.cropTop,
            cropRight = image.cropRight,
            cropBottom = image.cropBottom,
        )

        val decodeOptions = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
            inJustDecodeBounds = false
            inSampleSize = calculateInSampleSize(
                crop.width,
                crop.height,
                contentW.toInt(),
                contentH.toInt(),
            )
        }
        val decoded = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return
        try {
            val sample = decodeOptions.inSampleSize.coerceAtLeast(1)
            val srcRect = Rect(
                (crop.left / sample).coerceIn(0, decoded.width),
                (crop.top / sample).coerceIn(0, decoded.height),
                (crop.right / sample).coerceIn(0, decoded.width),
                (crop.bottom / sample).coerceIn(0, decoded.height),
            )
            if (srcRect.width() <= 0 || srcRect.height() <= 0) return

            val destPx = ReferenceImagePlacement.destinationRect(
                contentW = contentW,
                contentH = contentH,
                cropWidthPx = srcRect.width(),
                cropHeightPx = srcRect.height(),
                scale = image.scale,
                offsetXMm = image.offsetXMm,
                offsetYMm = image.offsetYMm,
                pxPerMm = pxPerMm,
            )
            val dst = RectF(
                destPx.left + marginLeft,
                destPx.top + marginTop,
                destPx.right + marginLeft,
                destPx.bottom + marginTop,
            )
            val centerX = destPx.centerX + marginLeft
            val centerY = destPx.centerY + marginTop

            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                alpha = (image.opacity * 255).toInt().coerceIn(0, 255)
                val coeffs = ImageFilterMatrices.matrix(image.filter)
                if (coeffs != null) {
                    colorFilter = ColorMatrixColorFilter(ColorMatrix(coeffs))
                }
            }
            val save = canvas.save()
            if (image.rotationDegrees != 0f) {
                canvas.rotate(image.rotationDegrees, centerX, centerY)
            }
            canvas.drawBitmap(decoded, srcRect, dst, paint)
            canvas.restoreToCount(save)
        } finally {
            if (!decoded.isRecycled) {
                decoded.recycle()
            }
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
