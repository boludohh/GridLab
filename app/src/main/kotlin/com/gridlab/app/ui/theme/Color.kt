package com.gridlab.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Semantic color tokens for GridLab.
 *
 * Three themes:
 * - Light (Claro)
 * - Dark (Oscuro)
 * - Night (Full Negro)
 *
 * Layers (back to front): bg → surface → e1 → e2 → e3 → e4
 */

// ---------------------------------------------------------------------------
// Light (Claro)
// ---------------------------------------------------------------------------

val LightBg = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFEEEEEE)
val LightE1 = Color(0xFFE4E4E4)
val LightE2 = Color(0xFFE0E0E0)
val LightE3 = Color(0xFFDCDCDC)
val LightE4 = Color(0xFFD8D8D8)

val LightBorderDecoBg = Color(0xFFD4D4D4)
val LightBorderDecoSurface = Color(0xFFD4D4D4)
val LightBorderDecoE1 = Color(0xFFC6C6C6)
val LightBorderDecoE2 = Color(0xFFBEBEBE)
val LightBorderDecoE3 = Color(0xFFB8B8B8)
val LightBorderDecoE4 = Color(0xFFB2B2B2)

val LightBorderDisabledSurface = Color(0xFFE2E2E2)
val LightBorderDisabledE1 = Color(0xFFD8D8D8)
val LightBorderDisabledE2 = Color(0xFFD4D4D4)
val LightBorderDisabledE3 = Color(0xFFD0D0D0)
val LightBorderDisabledE4 = Color(0xFFCCCCCC)

val LightBorderActive = Color(0xFF1C1B1F)

val LightIconDeco = Color(0xFF5C5C62)
val LightIconInactive = Color(0xFF6E6E73)
val LightIconActive = Color(0xFF1C1B1F)

val LightTextPrimary = Color(0xFF1C1B1F)
val LightTextSecondary = Color(0xFF5C5C62)
val LightTextTertiary = Color(0xFF6E6E73)
val LightTextPlaceholder = Color(0xFF77777C)
val LightTextDisabled = Color(0x611C1B1F) // 38%
val LightTextLink = Color(0xFF1C1B1F)
val LightTextInverse = Color(0xFFFFFFFF)

val LightActionBg = Color(0xFF1C1B1F)
val LightActionOn = Color(0xFFFFFFFF)
val LightActionPressed = Color(0xFF3A393D)
val LightActionDisabledBg = Color(0x1F1C1B1F) // 12%
val LightActionDisabledOn = Color(0x611C1B1F) // 38%
val LightActionSecondaryBg = Color(0x141C1B1F) // 8%
val LightActionSecondaryOn = Color(0xFF1C1B1F)

val LightStateHover = Color(0x0F1C1B1F) // 6%
val LightStatePressed = Color(0x1A1C1B1F) // 10%
val LightStateFocus = Color(0x1A1C1B1F) // 10%
val LightSelectedFill = Color(0x141C1B1F) // 8%
val LightFocusRing = Color(0xFF1C1B1F)
val LightScrim = Color(0x52000000) // 32%
val LightShadow = Color(0x29000000) // 16%
val LightSkeletonBase = Color(0x0F1C1B1F) // 6%
val LightSkeletonHighlight = Color(0x1A1C1B1F) // 10%

val LightControlTrack = Color(0x3D1C1B1F) // 24%
val LightControlFill = Color(0xFF1C1B1F)
val LightControlThumbOn = Color(0xFF1C1B1F)
val LightControlThumbOff = Color(0xFF6E6E73)

val LightError = Color(0xFFBA1A1A)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightErrorOnContainer = Color(0xFF410002)
val LightErrorBorder = Color(0xFFBA1A1A)
val LightErrorSolid = Color(0xFFBA1A1A)
val LightErrorOnSolid = Color(0xFFFFFFFF)

val LightWarning = Color(0xFF7A5900)
val LightWarningContainer = Color(0xFFFFDF9B)
val LightWarningOnContainer = Color(0xFF271900)
val LightWarningBorder = Color(0xFF7A5900)
val LightWarningSolid = Color(0xFF7A5900)
val LightWarningOnSolid = Color(0xFFFFFFFF)

val LightSuccess = Color(0xFF2E6E3E)
val LightSuccessContainer = Color(0xFFC3F2CB)
val LightSuccessOnContainer = Color(0xFF002106)
val LightSuccessBorder = Color(0xFF2E6E3E)
val LightSuccessSolid = Color(0xFF2E6E3E)
val LightSuccessOnSolid = Color(0xFFFFFFFF)

// ---------------------------------------------------------------------------
// Dark (Oscuro)
// ---------------------------------------------------------------------------

val DarkBg = Color(0xFF151515)
val DarkSurface = Color(0xFF262626)
val DarkE1 = Color(0xFF2C2C2C)
val DarkE2 = Color(0xFF333333)
val DarkE3 = Color(0xFF3A3A3A)
val DarkE4 = Color(0xFF424242)

val DarkBorderDecoBg = Color(0xFF2A2A2A)
val DarkBorderDecoSurface = Color(0xFF3A3A3A)
val DarkBorderDecoE1 = Color(0xFF404040)
val DarkBorderDecoE2 = Color(0xFF474747)
val DarkBorderDecoE3 = Color(0xFF4E4E4E)
val DarkBorderDecoE4 = Color(0xFF565656)

val DarkBorderDisabledSurface = Color(0xFF2F2F2F)
val DarkBorderDisabledE1 = Color(0xFF353535)
val DarkBorderDisabledE2 = Color(0xFF3C3C3C)
val DarkBorderDisabledE3 = Color(0xFF434343)
val DarkBorderDisabledE4 = Color(0xFF4A4A4A)

val DarkBorderActive = Color(0xFFF2F2F2)

val DarkIconDecoBg = Color(0xFF6A6A6A)
val DarkIconDecoSurface = Color(0xFF727272)
val DarkIconDecoE1 = Color(0xFF787878)
val DarkIconDecoE2 = Color(0xFF7F7F7F)
val DarkIconDecoE3 = Color(0xFF868686)
val DarkIconDecoE4 = Color(0xFF8D8D8D)

val DarkIconInactiveBg = Color(0xFF8A8A8A)
val DarkIconInactiveSurface = Color(0xFF959595)
val DarkIconInactiveE1 = Color(0xFF9A9A9A)
val DarkIconInactiveE2 = Color(0xFFA0A0A0)
val DarkIconInactiveE3 = Color(0xFFA6A6A6)
val DarkIconInactiveE4 = Color(0xFFACACAC)

val DarkIconActive = Color(0xFFFAFAFA)

val DarkTextPrimary = Color(0xFFF5F5F5)
val DarkTextSecondary = Color(0xFFD0D0D0)
val DarkTextTertiary = Color(0xFFB0B0B0)
val DarkTextPlaceholder = Color(0xFF9A9A9A)
val DarkTextDisabled = Color(0xFF6B6B6B)
val DarkTextLink = Color(0xFFFFFFFF)
val DarkTextInverse = Color(0xFF141414)

val DarkActionBg = Color(0xFFF2F2F2)
val DarkActionOn = Color(0xFF141414)
val DarkActionPressed = Color(0xFFD6D6D6)
val DarkActionDisabledBg = Color(0x1FFFFFFF) // 12%
val DarkActionDisabledOn = Color(0xFF6B6B6B)
val DarkActionSecondaryBg = Color(0x1FFFFFFF) // 12%
val DarkActionSecondaryOn = Color(0xFFF5F5F5)

val DarkStateHover = Color(0x14FFFFFF) // 8%
val DarkStatePressed = Color(0x1FFFFFFF) // 12%
val DarkStateFocus = Color(0x1FFFFFFF) // 12%
val DarkSelectedFill = Color(0x24FFFFFF) // 14%
val DarkFocusRing = Color(0xFFFFFFFF)
val DarkScrim = Color(0x99000000) // 60%
val DarkShadow = Color(0x80000000) // 50%
val DarkSkeletonBase = Color(0x14FFFFFF) // 8%
val DarkSkeletonHighlight = Color(0x24FFFFFF) // 14%

val DarkControlTrack = Color(0x4DFFFFFF) // 30%
val DarkControlFill = Color(0xFFF2F2F2)
val DarkControlThumbOn = Color(0xFFF2F2F2)
val DarkControlThumbOff = Color(0xFFB0B0B0)

val DarkError = Color(0xFFFF9088)
val DarkErrorContainer = Color(0xFF3D1F1F)
val DarkErrorOnContainer = Color(0xFFFFABA3)
val DarkErrorBorder = Color(0xFFC24A4A)
val DarkErrorSolid = Color(0xFFD93A3A)
val DarkErrorOnSolid = Color(0xFFFFFFFF)

val DarkWarning = Color(0xFFF2C14E)
val DarkWarningContainer = Color(0xFF3A2D0D)
val DarkWarningOnContainer = Color(0xFFF7D27A)
val DarkWarningBorder = Color(0xFFB8891F)
val DarkWarningSolid = Color(0xFFF2C14E)
val DarkWarningOnSolid = Color(0xFF2A1F00)

val DarkSuccess = Color(0xFF6FD08C)
val DarkSuccessContainer = Color(0xFF14301C)
val DarkSuccessOnContainer = Color(0xFF8EE3A6)
val DarkSuccessBorder = Color(0xFF3F9E5A)
val DarkSuccessSolid = Color(0xFF4CBE74)
val DarkSuccessOnSolid = Color(0xFF0B2A14)

// ---------------------------------------------------------------------------
// Night (Full Negro)
// ---------------------------------------------------------------------------

val NightBg = Color(0xFF000000)
val NightSurface = Color(0xFF121212)
val NightE1 = Color(0xFF1A1A1A)
val NightE2 = Color(0xFF222222)
val NightE3 = Color(0xFF2B2B2B)
val NightE4 = Color(0xFF353535)

val NightBorderDecoBg = Color(0xFF1C1C1C)
val NightBorderDecoSurface = Color(0xFF262626)
val NightBorderDecoE1 = Color(0xFF2E2E2E)
val NightBorderDecoE2 = Color(0xFF363636)
val NightBorderDecoE3 = Color(0xFF3F3F3F)
val NightBorderDecoE4 = Color(0xFF494949)

val NightBorderDisabledSurface = Color(0xFF1D1D1D)
val NightBorderDisabledE1 = Color(0xFF252525)
val NightBorderDisabledE2 = Color(0xFF2D2D2D)
val NightBorderDisabledE3 = Color(0xFF363636)
val NightBorderDisabledE4 = Color(0xFF404040)

val NightBorderActive = Color(0xFFF2F2F2)

val NightIconDecoBg = Color(0xFF5C5C5C)
val NightIconDecoSurface = Color(0xFF636363)
val NightIconDecoE1 = Color(0xFF6A6A6A)
val NightIconDecoE2 = Color(0xFF717171)
val NightIconDecoE3 = Color(0xFF787878)
val NightIconDecoE4 = Color(0xFF808080)

val NightIconInactiveBg = Color(0xFF858585)
val NightIconInactiveSurface = Color(0xFF8C8C8C)
val NightIconInactiveE1 = Color(0xFF929292)
val NightIconInactiveE2 = Color(0xFF989898)
val NightIconInactiveE3 = Color(0xFF9E9E9E)
val NightIconInactiveE4 = Color(0xFFA5A5A5)

val NightIconActive = Color(0xFFFAFAFA)

val NightTextPrimary = Color(0xFFF5F5F5)
val NightTextSecondary = Color(0xFFD0D0D0)
val NightTextTertiary = Color(0xFFB0B0B0)
val NightTextPlaceholder = Color(0xFF9A9A9A)
val NightTextDisabled = Color(0xFF6B6B6B)
val NightTextLink = Color(0xFFFFFFFF)
val NightTextInverse = Color(0xFF141414)

val NightActionBg = Color(0xFFF2F2F2)
val NightActionOn = Color(0xFF141414)
val NightActionPressed = Color(0xFFD6D6D6)
val NightActionDisabledBg = Color(0x1FFFFFFF) // 12%
val NightActionDisabledOn = Color(0xFF6B6B6B)
val NightActionSecondaryBg = Color(0x1FFFFFFF) // 12%
val NightActionSecondaryOn = Color(0xFFF5F5F5)

val NightStateHover = Color(0x14FFFFFF) // 8%
val NightStatePressed = Color(0x1FFFFFFF) // 12%
val NightStateFocus = Color(0x1FFFFFFF) // 12%
val NightSelectedFill = Color(0x24FFFFFF) // 14%
val NightFocusRing = Color(0xFFFFFFFF)
val NightScrim = Color(0xB8000000) // 72%
val NightShadow = Color(0x99000000) // 60%
val NightSkeletonBase = Color(0x14FFFFFF) // 8%
val NightSkeletonHighlight = Color(0x24FFFFFF) // 14%

val NightControlTrack = Color(0x4DFFFFFF) // 30%
val NightControlFill = Color(0xFFF2F2F2)
val NightControlThumbOn = Color(0xFFF2F2F2)
val NightControlThumbOff = Color(0xFFB0B0B0)

val NightError = Color(0xFFFF9088)
val NightErrorContainer = Color(0xFF331717)
val NightErrorOnContainer = Color(0xFFFFABA3)
val NightErrorBorder = Color(0xFFC24A4A)
val NightErrorSolid = Color(0xFFD93A3A)
val NightErrorOnSolid = Color(0xFFFFFFFF)

val NightWarning = Color(0xFFF2C14E)
val NightWarningContainer = Color(0xFF2E2409)
val NightWarningOnContainer = Color(0xFFF7D27A)
val NightWarningBorder = Color(0xFFB8891F)
val NightWarningSolid = Color(0xFFF2C14E)
val NightWarningOnSolid = Color(0xFF2A1F00)

val NightSuccess = Color(0xFF6FD08C)
val NightSuccessContainer = Color(0xFF0F2616)
val NightSuccessOnContainer = Color(0xFF8EE3A6)
val NightSuccessBorder = Color(0xFF3F9E5A)
val NightSuccessSolid = Color(0xFF4CBE74)
val NightSuccessOnSolid = Color(0xFF0B2A14)
