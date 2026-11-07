package com.gridlab.app.feature.editor.grid

import com.gridlab.app.core.geometry.UnitConverter
import com.gridlab.app.core.geometry.ViewportTransform
import com.gridlab.app.core.model.Grid
import com.gridlab.app.core.model.ImageAvailability
import com.gridlab.app.core.model.LengthUnit
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.ReferenceImage

data class GridConfigUiState(
    val documentId: String = "",
    val documentName: String = "Untitled",
    val paper: Paper = Paper.defaultA4(),
    val grid: Grid = Grid.default(),
    val referenceImage: ReferenceImage? = null,
    /** Absolute path for Coil; only set when [imageAvailability] is Present. */
    val imageAbsolutePath: String? = null,
    val imageAvailability: ImageAvailability = ImageAvailability.None,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isImporting: Boolean = false,
    val lastSaveSucceeded: Boolean? = null,
    val importError: Boolean = false,
    /** True when local edits are not yet persisted. */
    val isDirty: Boolean = false,
    /** When true, taps on the preview place measure points instead of transforming the image. */
    val measureMode: Boolean = false,
    /** First measure point in document mm (paper top-left origin), or null. */
    val measurePoint1Mm: Pair<Double, Double>? = null,
    /** Second measure point in document mm, or null. */
    val measurePoint2Mm: Pair<Double, Double>? = null,
    /** Packed ARGB for measure overlay (tool chrome; not part of the document). */
    val measureColorArgb: Long = 0xFF1565C0,
    val selectedExportDpi: Int = 150,
    val maxSafeExportDpi: Int = 300,
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
    val lastExportPath: String? = null,
)

/** Distance in the paper's display unit, or null if both points are not set. */
fun GridConfigUiState.measureDistanceDisplay(): Double? {
    val p1 = measurePoint1Mm ?: return null
    val p2 = measurePoint2Mm ?: return null
    val mm = ViewportTransform.distanceMm(p1, p2)
    return UnitConverter.fromMm(mm, paper.displayUnit)
}

