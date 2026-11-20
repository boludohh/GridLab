package com.gridlab.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Theme mode supported by GridLab.
 * - Light: system light / expressive light
 * - Dark: standard dark
 * - Night: near-black (AMOLED-friendly)
 */
enum class GridLabThemeMode {
    Light,
    Dark,
    Night,
}

/**
 * Compatibility semantic layer.
 *
 * All tokens are derived from the active Material 3 [ColorScheme] so that existing
 * screens keep compiling while the app uses Material Expressive theming.
 * A later visual refactor can replace `GridLabTheme.colors.xxx` with direct
 * `MaterialTheme.colorScheme` / component defaults.
 */
@Immutable
data class GridLabSemanticColors(
    val bg: Color,
    val surface: Color,
    val e1: Color,
    val e2: Color,
    val e3: Color,
    val e4: Color,
    val borderDecoBg: Color,
    val borderDecoSurface: Color,
    val borderDecoE1: Color,
    val borderDecoE2: Color,
    val borderDecoE3: Color,
    val borderDecoE4: Color,
    val borderDisabledSurface: Color,
    val borderDisabledE1: Color,
    val borderDisabledE2: Color,
    val borderDisabledE3: Color,
    val borderDisabledE4: Color,
    val borderActive: Color,
    val iconDeco: Color,
    val iconInactive: Color,
    val iconActive: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textPlaceholder: Color,
    val textDisabled: Color,
    val textLink: Color,
    val textInverse: Color,
    val actionBg: Color,
    val actionOn: Color,
    val actionPressed: Color,
    val actionDisabledBg: Color,
    val actionDisabledOn: Color,
    val actionSecondaryBg: Color,
    val actionSecondaryOn: Color,
    val stateHover: Color,
    val statePressed: Color,
    val stateFocus: Color,
    val selectedFill: Color,
    val focusRing: Color,
    val scrim: Color,
    val shadow: Color,
    val skeletonBase: Color,
    val skeletonHighlight: Color,
    val controlTrack: Color,
    val controlFill: Color,
    val controlThumbOn: Color,
    val controlThumbOff: Color,
    val error: Color,
    val errorContainer: Color,
    val errorOnContainer: Color,
    val errorBorder: Color,
    val errorSolid: Color,
    val errorOnSolid: Color,
    val warning: Color,
    val warningContainer: Color,
    val warningOnContainer: Color,
    val warningBorder: Color,
    val warningSolid: Color,
    val warningOnSolid: Color,
    val success: Color,
    val successContainer: Color,
    val successOnContainer: Color,
    val successBorder: Color,
    val successSolid: Color,
    val successOnSolid: Color,
    // Direct Material pass-throughs used by a few screens
    val onSurfaceVariant: Color,
    val onSecondaryContainer: Color,
    val surfaceVariant: Color,
    val secondaryContainer: Color,
)

private fun ColorScheme.toGridLabSemanticColors(): GridLabSemanticColors {
    val onSurfaceDisabled = onSurface.copy(alpha = 0.38f)
    val onSurfaceHover = onSurface.copy(alpha = 0.06f)
    val onSurfacePressed = onSurface.copy(alpha = 0.10f)
    val onSurfaceSelected = onSurface.copy(alpha = 0.08f)
    val onSurfaceTrack = onSurface.copy(alpha = 0.24f)
    val primaryPressed = primary.copy(alpha = 0.85f)

    return GridLabSemanticColors(
        bg = background,
        surface = surface,
        e1 = surfaceContainerLow,
        e2 = surfaceContainer,
        e3 = surfaceContainerHigh,
        e4 = surfaceContainerHighest,
        borderDecoBg = outlineVariant,
        borderDecoSurface = outline,
        borderDecoE1 = outlineVariant,
        borderDecoE2 = outlineVariant,
        borderDecoE3 = outline,
        borderDecoE4 = outline,
        borderDisabledSurface = outlineVariant.copy(alpha = 0.5f),
        borderDisabledE1 = outlineVariant.copy(alpha = 0.5f),
        borderDisabledE2 = outlineVariant.copy(alpha = 0.5f),
        borderDisabledE3 = outlineVariant.copy(alpha = 0.5f),
        borderDisabledE4 = outlineVariant.copy(alpha = 0.5f),
        borderActive = primary,
        iconDeco = onSurfaceVariant,
        iconInactive = onSurfaceVariant,
        iconActive = onSurface,
        textPrimary = onSurface,
        textSecondary = onSurfaceVariant,
        textTertiary = onSurfaceVariant.copy(alpha = 0.8f),
        textPlaceholder = onSurfaceVariant.copy(alpha = 0.6f),
        textDisabled = onSurfaceDisabled,
        textLink = primary,
        textInverse = onPrimary,
        actionBg = primary,
        actionOn = onPrimary,
        actionPressed = primaryPressed,
        actionDisabledBg = onSurface.copy(alpha = 0.12f),
        actionDisabledOn = onSurfaceDisabled,
        actionSecondaryBg = secondaryContainer,
        actionSecondaryOn = onSecondaryContainer,
        stateHover = onSurfaceHover,
        statePressed = onSurfacePressed,
        stateFocus = onSurfacePressed,
        selectedFill = onSurfaceSelected,
        focusRing = primary,
        scrim = scrim,
        shadow = Color.Black.copy(alpha = 0.16f),
        skeletonBase = onSurfaceHover,
        skeletonHighlight = onSurfacePressed,
        controlTrack = onSurfaceTrack,
        controlFill = primary,
        controlThumbOn = primary,
        controlThumbOff = onSurfaceVariant,
        error = error,
        errorContainer = errorContainer,
        errorOnContainer = onErrorContainer,
        errorBorder = error,
        errorSolid = error,
        errorOnSolid = onError,
        // Warning / success: map to tertiary / primary containers as neutral stand-ins
        // until a dedicated visual pass defines brand colors.
        warning = tertiary,
        warningContainer = tertiaryContainer,
        warningOnContainer = onTertiaryContainer,
        warningBorder = tertiary,
        warningSolid = tertiary,
        warningOnSolid = onTertiary,
        success = tertiary,
        successContainer = tertiaryContainer,
        successOnContainer = onTertiaryContainer,
        successBorder = tertiary,
        successSolid = tertiary,
        successOnSolid = onTertiary,
        onSurfaceVariant = onSurfaceVariant,
        onSecondaryContainer = onSecondaryContainer,
        surfaceVariant = surfaceVariant,
        secondaryContainer = secondaryContainer,
    )
}

/**
 * Neutral dark scheme (standard dark, not pure black).
 * Built so surface containers remain distinct for elevation.
 */
private val DarkNeutralColorScheme = darkColorScheme(
    primary = Color(0xFFE4E2E6),
    onPrimary = Color(0xFF1C1B1F),
    primaryContainer = Color(0xFF47464A),
    onPrimaryContainer = Color(0xFFE4E2E6),
    secondary = Color(0xFFCAC5CA),
    onSecondary = Color(0xFF323035),
    secondaryContainer = Color(0xFF49464B),
    onSecondaryContainer = Color(0xFFE6E1E6),
    tertiary = Color(0xFFCFC2BE),
    onTertiary = Color(0xFF37312F),
    tertiaryContainer = Color(0xFF4E4745),
    onTertiaryContainer = Color(0xFFECDFDB),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF141316),
    onBackground = Color(0xFFE5E1E6),
    surface = Color(0xFF141316),
    onSurface = Color(0xFFE5E1E6),
    surfaceVariant = Color(0xFF49454E),
    onSurfaceVariant = Color(0xFFCAC4CF),
    outline = Color(0xFF948F99),
    outlineVariant = Color(0xFF49454E),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE5E1E6),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = Color(0xFF5F5E62),
    surfaceDim = Color(0xFF141316),
    surfaceBright = Color(0xFF3A383C),
    surfaceContainerLowest = Color(0xFF0F0E11),
    surfaceContainerLow = Color(0xFF1C1B1F),
    surfaceContainer = Color(0xFF201F23),
    surfaceContainerHigh = Color(0xFF2B292D),
    surfaceContainerHighest = Color(0xFF363438),
)

/**
 * Night = near-black surfaces for OLED, still readable contrast.
 */
private val NightColorScheme = darkColorScheme(
    primary = Color(0xFFE4E2E6),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF3A393D),
    onPrimaryContainer = Color(0xFFE4E2E6),
    secondary = Color(0xFFCAC5CA),
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFF2C2C2C),
    onSecondaryContainer = Color(0xFFE6E1E6),
    tertiary = Color(0xFFCFC2BE),
    onTertiary = Color(0xFF1A1A1A),
    tertiaryContainer = Color(0xFF2C2C2C),
    onTertiaryContainer = Color(0xFFECDFDB),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF000000),
    onBackground = Color(0xFFE5E1E6),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFE5E1E6),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFCAC4CF),
    outline = Color(0xFF8A8A8A),
    outlineVariant = Color(0xFF2C2C2C),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE5E1E6),
    inverseOnSurface = Color(0xFF1A1A1A),
    inversePrimary = Color(0xFF5F5E62),
    surfaceDim = Color(0xFF000000),
    surfaceBright = Color(0xFF2C2C2C),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainer = Color(0xFF121212),
    surfaceContainerHigh = Color(0xFF1A1A1A),
    surfaceContainerHighest = Color(0xFF242424),
)

val LocalGridLabColors = staticCompositionLocalOf {
    // Fallback; real value is always provided by GridLabTheme.
    DarkNeutralColorScheme.toGridLabSemanticColors()
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GridLabTheme(
    themeMode: GridLabThemeMode = if (isSystemInDarkTheme()) GridLabThemeMode.Dark else GridLabThemeMode.Light,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeMode) {
        GridLabThemeMode.Light -> expressiveLightColorScheme()
        GridLabThemeMode.Dark -> DarkNeutralColorScheme
        GridLabThemeMode.Night -> NightColorScheme
    }
    val semanticColors = colorScheme.toGridLabSemanticColors()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            // Light theme → dark status/nav icons; Dark/Night → light icons
            val lightIcons = themeMode == GridLabThemeMode.Light
            controller.isAppearanceLightStatusBars = lightIcons
            controller.isAppearanceLightNavigationBars = lightIcons
        }
    }

    CompositionLocalProvider(LocalGridLabColors provides semanticColors) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            typography = Typography,
            content = content,
        )
    }
}

/** Convenience accessor for semantic colors (compatibility layer). */
object GridLabTheme {
    val colors: GridLabSemanticColors
        @Composable
        get() = LocalGridLabColors.current
}
