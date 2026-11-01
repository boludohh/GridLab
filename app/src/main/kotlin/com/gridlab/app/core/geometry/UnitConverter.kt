package com.gridlab.app.core.geometry

import com.gridlab.app.core.model.LengthUnit

/**
 * Converts between display units and the internal millimeter unit.
 * Pure Kotlin — no Android dependencies.
 */
object UnitConverter {

    private const val MM_PER_CM = 10.0
    private const val MM_PER_INCH = 25.4

    fun toMm(value: Double, unit: LengthUnit): Double =
        when (unit) {
            LengthUnit.Millimeter -> value
            LengthUnit.Centimeter -> value * MM_PER_CM
            LengthUnit.Inch -> value * MM_PER_INCH
        }

    fun fromMm(valueMm: Double, unit: LengthUnit): Double =
        when (unit) {
            LengthUnit.Millimeter -> valueMm
            LengthUnit.Centimeter -> valueMm / MM_PER_CM
            LengthUnit.Inch -> valueMm / MM_PER_INCH
        }
}
