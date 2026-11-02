package com.gridlab.app.core.geometry

import org.junit.Assert.assertEquals
import org.junit.Test

class ViewportTransformTest {

    @Test
    fun screenToDocument_and_back_roundTrip() {
        val vt = ViewportTransform(originXPx = 10f, originYPx = 20f, scale = 2f)
        val (xMm, yMm) = vt.screenToDocumentMm(30f, 40f)
        assertEquals(10.0, xMm, 1e-6)
        assertEquals(10.0, yMm, 1e-6)
        val (xPx, yPx) = vt.documentToScreenPx(xMm, yMm)
        assertEquals(30f, xPx, 1e-4f)
        assertEquals(40f, yPx, 1e-4f)
    }

    @Test
    fun distanceMm_knownLength() {
        val a = 0.0 to 0.0
        val b = 30.0 to 40.0
        assertEquals(50.0, ViewportTransform.distanceMm(a, b), 1e-6)
    }

    @Test
    fun distanceMm_paperEdge_independentOfViewportScale() {
        // A4 width 210 mm measured in document space must stay 210 regardless of zoom
        val a = 0.0 to 0.0
        val b = 210.0 to 0.0
        assertEquals(210.0, ViewportTransform.distanceMm(a, b), 1e-6)
    }
}
