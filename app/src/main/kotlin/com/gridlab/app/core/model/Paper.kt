package com.gridlab.app.core.model

import kotlinx.serialization.Serializable

/**
 * Physical paper definition.
 *
 * All linear values are stored in millimeters (internal unit).
 * [displayUnit] only affects how values are shown or edited in the UI.
 * [colorArgb] is the paper fill color (packed ARGB); default is opaque white.
 */
@Serializable
data class Paper(
    val format: StandardPaperFormat,
    val widthMm: Double,
    val heightMm: Double,
    val orientation: PaperOrientation,
    val marginLeftMm: Double = 0.0,
    val marginTopMm: Double = 0.0,
    val marginRightMm: Double = 0.0,
    val marginBottomMm: Double = 0.0,
    val displayUnit: LengthUnit = LengthUnit.Millimeter,
    val colorArgb: Long = DEFAULT_COLOR_ARGB,
) {
    val printableWidthMm: Double
        get() = (widthMm - marginLeftMm - marginRightMm).coerceAtLeast(0.0)

    val printableHeightMm: Double
        get() = (heightMm - marginTopMm - marginBottomMm).coerceAtLeast(0.0)

    companion object {
        const val DEFAULT_COLOR_ARGB: Long = 0xFFFFFFFF

        fun fromStandard(
            format: StandardPaperFormat,
            orientation: PaperOrientation = PaperOrientation.Portrait,
            displayUnit: LengthUnit = LengthUnit.Millimeter,
            marginLeftMm: Double = 0.0,
            marginTopMm: Double = 0.0,
            marginRightMm: Double = 0.0,
            marginBottomMm: Double = 0.0,
            colorArgb: Long = DEFAULT_COLOR_ARGB,
        ): Paper {
            require(format != StandardPaperFormat.Custom) {
                "Use the primary constructor for Custom paper"
            }
            val w = format.portraitWidthMm!!
            val h = format.portraitHeightMm!!
            val (width, height) = when (orientation) {
                PaperOrientation.Portrait -> w to h
                PaperOrientation.Landscape -> h to w
            }
            return Paper(
                format = format,
                widthMm = width,
                heightMm = height,
                orientation = orientation,
                marginLeftMm = marginLeftMm,
                marginTopMm = marginTopMm,
                marginRightMm = marginRightMm,
                marginBottomMm = marginBottomMm,
                displayUnit = displayUnit,
                colorArgb = colorArgb,
            )
        }

        fun defaultA4(): Paper = fromStandard(StandardPaperFormat.A4)
    }
}
