package com.gridlab.app.core.geometry

/**
 * Formats primary grid cell labels: columns as 1-based numbers, rows as A, B, … Z, AA…
 */
object GridLabelFormatter {

    fun columnLabel(indexZeroBased: Int): String = (indexZeroBased + 1).toString()

    fun rowLabel(indexZeroBased: Int): String {
        var n = indexZeroBased
        val chars = StringBuilder()
        do {
            chars.append(('A'.code + (n % 26)).toChar())
            n = n / 26 - 1
        } while (n >= 0)
        return chars.reverse().toString()
    }
}
