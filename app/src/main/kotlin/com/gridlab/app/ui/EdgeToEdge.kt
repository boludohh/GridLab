package com.gridlab.app.ui

import android.graphics.Color
import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat

/**
 * Global edge-to-edge setup for GridLab (API 27–37+).
 *
 * Applies:
 * 1. [enableEdgeToEdge]
 * 2. Clears legacy translucent status/navigation flags (API 27–28 gradients)
 * 3. Explicit transparent system bar colors (API 27–34)
 * 4. Disables contrast scrims and force-dark (API 29+)
 * 5. Content in display cutout (API 28+)
 *
 * Pair with transparent colors in `themes.xml` and contrast/force-dark flags
 * in `values-v29/themes.xml`. System bar icon appearance (light/dark) is applied
 * from Compose theme via [applySystemBarIconAppearance].
 */
fun ComponentActivity.configureEdgeToEdge() {
    enableEdgeToEdge()
    WindowCompat.setDecorFitsSystemWindows(window, false)

    @Suppress("DEPRECATION")
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    @Suppress("DEPRECATION")
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

    @Suppress("DEPRECATION")
    window.statusBarColor = Color.TRANSPARENT
    @Suppress("DEPRECATION")
    window.navigationBarColor = Color.TRANSPARENT

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        @Suppress("DEPRECATION")
        window.isStatusBarContrastEnforced = false
        window.isNavigationBarContrastEnforced = false
        window.decorView.setForceDarkAllowed(false)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val params = window.attributes
        params.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.attributes = params
    }
}
