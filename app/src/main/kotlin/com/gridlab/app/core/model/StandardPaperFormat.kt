package com.gridlab.app.core.model

import kotlinx.serialization.Serializable

/**
 * ISO 216 standard sizes plus Custom.
 * Dimensions below are portrait width × height in millimeters.
 */
@Serializable
enum class StandardPaperFormat(
    val portraitWidthMm: Double?,
    val portraitHeightMm: Double?,
) {
    A0(841.0, 1189.0),
    A1(594.0, 841.0),
    A2(420.0, 594.0),
    A3(297.0, 420.0),
    A4(210.0, 297.0),
    A5(148.0, 210.0),
    Custom(null, null),
}
