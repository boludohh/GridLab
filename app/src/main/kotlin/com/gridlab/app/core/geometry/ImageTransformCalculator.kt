package com.gridlab.app.core.geometry

import com.gridlab.app.core.model.ImageFilter
import com.gridlab.app.core.model.ReferenceImage

/**
 * Pure-Kotlin helpers for reference-image transform and non-destructive crop.
 * Coordinates are in document millimeters relative to the printable-area origin
 * (top-left of the area inside margins). Crop uses normalized image coordinates (0..1).
 */
object ImageTransformCalculator {

    private const val MIN_SCALE: Double = 0.05
    private const val MAX_SCALE: Double = 20.0
    private const val MIN_CROP_SPAN: Float = 0.05f

    fun pan(image: ReferenceImage, deltaXMm: Double, deltaYMm: Double): ReferenceImage {
        if (image.locked) return image
        return image.copy(
            offsetXMm = image.offsetXMm + deltaXMm,
            offsetYMm = image.offsetYMm + deltaYMm,
        )
    }

    fun scaleBy(image: ReferenceImage, factor: Double): ReferenceImage {
        if (image.locked) return image
        val next = (image.scale * factor).coerceIn(MIN_SCALE, MAX_SCALE)
        return image.copy(scale = next)
    }

    fun withOpacity(image: ReferenceImage, opacity: Float): ReferenceImage =
        image.copy(opacity = opacity.coerceIn(0f, 1f))

    fun withLocked(image: ReferenceImage, locked: Boolean): ReferenceImage =
        image.copy(locked = locked)

    fun withVisible(image: ReferenceImage, visible: Boolean): ReferenceImage =
        image.copy(visible = visible)

    /** Sets rotation in degrees (normalized to 0..360). Does nothing when locked. */
    fun withFilter(image: ReferenceImage, filter: ImageFilter): ReferenceImage {
        if (image.locked) return image
        return image.copy(filter = filter)
    }

    fun withRotation(image: ReferenceImage, degrees: Float): ReferenceImage {
        if (image.locked) return image
        var d = degrees % 360f
        if (d < 0f) d += 360f
        return image.copy(rotationDegrees = d)
    }

    /**
     * Sets normalized crop rect. Edges are clamped and ordered so left < right and top < bottom
     * with a minimum span. Does nothing when locked.
     */
    fun withCrop(
        image: ReferenceImage,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): ReferenceImage {
        if (image.locked) return image
        var l = left.coerceIn(0f, 1f)
        var t = top.coerceIn(0f, 1f)
        var r = right.coerceIn(0f, 1f)
        var b = bottom.coerceIn(0f, 1f)
        if (r - l < MIN_CROP_SPAN) {
            r = (l + MIN_CROP_SPAN).coerceAtMost(1f)
            l = (r - MIN_CROP_SPAN).coerceAtLeast(0f)
        }
        if (b - t < MIN_CROP_SPAN) {
            b = (t + MIN_CROP_SPAN).coerceAtMost(1f)
            t = (b - MIN_CROP_SPAN).coerceAtLeast(0f)
        }
        return image.copy(cropLeft = l, cropTop = t, cropRight = r, cropBottom = b)
    }

    /** Restores full image (no crop). Does nothing when locked. */
    fun clearCrop(image: ReferenceImage): ReferenceImage {
        if (image.locked) return image
        return image.copy(cropLeft = 0f, cropTop = 0f, cropRight = 1f, cropBottom = 1f)
    }

    /**
     * Resets user transform to the initial fitted state (offset 0, scale 1).
     * Keeps path, opacity, locked, visibility and crop.
     */
    fun resetTransform(image: ReferenceImage): ReferenceImage =
        image.copy(
            offsetXMm = 0.0,
            offsetYMm = 0.0,
            scale = 1.0,
            rotationDegrees = 0f,
        )
}
