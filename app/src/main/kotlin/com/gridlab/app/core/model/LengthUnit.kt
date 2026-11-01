package com.gridlab.app.core.model

import kotlinx.serialization.Serializable

/**
 * Display / input unit for physical lengths.
 * Internal storage is always millimeters.
 */
@Serializable
enum class LengthUnit {
    Millimeter,
    Centimeter,
    Inch,
}
