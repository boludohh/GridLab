package com.gridlab.app.core.geometry

import com.gridlab.app.core.model.ReferenceImage

/**
 * Pure-Kotlin helpers for reference-image transform.
 * Coordinates are in document millimeters relative to the printable-area origin
 * (top-left of the area inside margins).
 */
object ImageTransformCalculator {

    private const val MIN_SCALE: Double = 0.05
    private const val MAX_SCALE: Double = 20.0

    /**
     * Applies a pan delta (in mm) to the image offset.
     * Does nothing when the image is locked.
     */
    fun pan(image: ReferenceImage, deltaXMm: Double, deltaYMm: Double): ReferenceImage {
        if (image.locked) return image
        return image.copy(
            offsetXMm = image.offsetXMm + deltaXMm,
            offsetYMm = image.offsetYMm + deltaYMm,
        )
    }

    /**
     * Multiplies the current scale by [factor], clamping to a safe range.
     * Does nothing when the image is locked.
     */
    fun scaleBy(image: ReferenceImage, factor: Double): ReferenceImage {
        if (image.locked) return image
        val next = (image.scale * factor).coerceIn(MIN_SCALE, MAX_SCALE)
        return image.copy(scale = next)
    }

    /**
     * Sets absolute opacity (0..1).
     */
    fun withOpacity(image: ReferenceImage, opacity: Float): ReferenceImage =
        image.copy(opacity = opacity.coerceIn(0f, 1f))

    fun withLocked(image: ReferenceImage, locked: Boolean): ReferenceImage =
        image.copy(locked = locked)

    fun withVisible(image: ReferenceImage, visible: Boolean): ReferenceImage =
        image.copy(visible = visible)

    /**
     * Resets user transform to the initial fitted state (offset 0, scale 1).
     * Keeps path, opacity, locked and visibility.
     */
    fun resetTransform(image: ReferenceImage): ReferenceImage =
        image.copy(
            offsetXMm = 0.0,
            offsetYMm = 0.0,
            scale = 1.0,
            rotationDegrees = 0f,
        )
}
