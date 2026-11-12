package com.gridlab.app.ui.theme

import androidx.annotation.DrawableRes
import com.gridlab.app.R

/** Cycles Light → Dark → Night → Light. */
fun GridLabThemeMode.next(): GridLabThemeMode = when (this) {
    GridLabThemeMode.Light -> GridLabThemeMode.Dark
    GridLabThemeMode.Dark -> GridLabThemeMode.Night
    GridLabThemeMode.Night -> GridLabThemeMode.Light
}

@DrawableRes
fun GridLabThemeMode.iconRes(): Int = when (this) {
    GridLabThemeMode.Light -> R.drawable.ic_light_mode_filled
    GridLabThemeMode.Dark -> R.drawable.ic_dark_mode_filled
    GridLabThemeMode.Night -> R.drawable.ic_night_mode_filled
}
