package com.gridlab.app.core.geometry

/**
 * Shared placement for reference images so preview and export use the same
 * normalized crop → fit-in-content → scale → offset model.
 *
 * Crop values are fractions of the **original source image** (0..1).
 */
object ReferenceImagePlacement {

    data class SourceCrop(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
    ) {
        val width: Int get() = (right - left).coerceAtLeast(1)
        val height: Int get() = (bottom - top).coerceAtLeast(1)
    }

    data class DestRect(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
        val centerX: Float,
        val centerY: Float,
    )

    fun sourceCrop(
        srcW: Int,
        srcH: Int,
        cropLeft: Double,
        cropTop: Double,
        cropRight: Double,
        cropBottom: Double,
    ): SourceCrop {
        val w = srcW.coerceAtLeast(1)
        val h = srcH.coerceAtLeast(1)
        val left = (w * cropLeft).toInt().coerceIn(0, w - 1)
        val top = (h * cropTop).toInt().coerceIn(0, h - 1)
        val right = (w * cropRight).toInt().coerceIn(left + 1, w)
        val bottom = (h * cropBottom).toInt().coerceIn(top + 1, h)
        return SourceCrop(left, top, right, bottom)
    }

    /**
     * Destination rectangle in content-local pixels (origin at content top-left),
     * after fitting the cropped source into [contentW]×[contentH], applying
     * user [scale] about the fitted center, then [offsetXMm]/[offsetYMm].
     */
    fun destinationRect(
        contentW: Float,
        contentH: Float,
        cropWidthPx: Int,
        cropHeightPx: Int,
        scale: Double,
        offsetXMm: Double,
        offsetYMm: Double,
        pxPerMm: Double,
    ): DestRect {
        val cw = cropWidthPx.coerceAtLeast(1).toFloat()
        val ch = cropHeightPx.coerceAtLeast(1).toFloat()
        val fit = minOf(contentW / cw, contentH / ch)
        val baseW = cw * fit
        val baseH = ch * fit
        val baseLeft = (contentW - baseW) / 2f
        val baseTop = (contentH - baseH) / 2f
        val offsetXPx = (offsetXMm * pxPerMm).toFloat()
        val offsetYPx = (offsetYMm * pxPerMm).toFloat()
        val centerX = baseLeft + baseW / 2f + offsetXPx
        val centerY = baseTop + baseH / 2f + offsetYPx
        val drawW = baseW * scale.toFloat()
        val drawH = baseH * scale.toFloat()
        return DestRect(
            left = centerX - drawW / 2f,
            top = centerY - drawH / 2f,
            right = centerX + drawW / 2f,
            bottom = centerY + drawH / 2f,
            centerX = centerX,
            centerY = centerY,
        )
    }
}
